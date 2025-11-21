/*
 * SPDX-FileCopyrightText: Octavia Togami <octy@octyl.net>
 * SPDX-License-Identifier: MPL-2.0
 */

package net.octyl.levelheadered;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * The LevelHeadered plugin. Applies {@link LevelHeaderedBasePlugin}, registers {@link LevelHeaderedExtension} and wires
 * tasks for all Java source sets.
 *
 * <p>
 * Only {@code abstract} for Gradle, not to be implemented by users.
 * </p>
 */
public abstract class LevelHeaderedPlugin implements Plugin<Project> {
    @SuppressWarnings("doclint:missing")
    public LevelHeaderedPlugin() {
    }

    @SuppressWarnings("doclint:missing")
    @Inject
    protected abstract ObjectFactory getObjects();

    @SuppressWarnings("doclint:missing")
    @Inject
    protected abstract ProviderFactory getProviders();

    public void apply(Project project) {
        project.getPluginManager().apply(LevelHeaderedBasePlugin.class);

        LevelHeaderedExtension levelHeadered = project.getExtensions().create(
            "levelHeadered", LevelHeaderedExtension.class, project.getResources().getText()
        );
        LevelHeaderedBasePlugin.applyConventions(levelHeadered, getProviders());
        levelHeadered.getHeaderTemplate().convention(getProviders().provider(() -> {
            throw new InvalidUserDataException("No header template configured for LevelHeadered plugin. " +
                "Please configure the 'levelHeadered.headerTemplate' property.");
        }));

        // All HeaderWorkerSourceTasks conventionally use the extension configuration
        project.getTasks().withType(HeaderWorkerSourceTask.class).configureEach(task ->
            LevelHeaderedBasePlugin.copyViaConvention(levelHeadered, task)
        );

        project.getPluginManager().withPlugin("java-base", p -> addTasksForJavaSourceSets(project));
    }

    private void addTasksForJavaSourceSets(Project project) {
        List<TaskProvider<?>> allApplyTasks = new ArrayList<>();
        List<TaskProvider<?>> allVerifyTasks = new ArrayList<>();
        // Eagerly evaluate like java-base does. If java-base becomes lazy in the future, we want to still eagerly
        // evaluate this in existing releases, then update the code to use the lazy APIs that java-base does.
        project.getExtensions().getByType(SourceSetContainer.class).all(sourceSet -> {
            TaskProvider<HeaderApplyTask> applyTaskProvider = project.getTasks().register(
                sourceSet.getTaskName("apply", "header"), HeaderApplyTask.class,
                task -> {
                    task.getSource().convention(sourceSet.getAllSource());
                    task.setDescription("Applies a file header to the " + sourceSet.getName() + " source set.");
                    task.setGroup("formatting");
                }
            );
            allApplyTasks.add(applyTaskProvider);
            TaskProvider<HeaderVerifyTask> verifyTaskProvider = project.getTasks().register(
                sourceSet.getTaskName("verify", "header"), HeaderVerifyTask.class,
                task -> {
                    task.getSource().convention(sourceSet.getAllSource());
                    task.setDescription("Verifies the file headers of the " + sourceSet.getName() + " source set.");
                    task.setGroup("verification");
                    task.getHeaderApplyTaskPath().convention(applyTaskProvider.map(Task::getPath));
                }
            );
            allVerifyTasks.add(verifyTaskProvider);
        });

        project.getTasks().register("applyHeaderToAll", task -> {
            task.setDescription("Applies file headers to all source sets.");
            task.setGroup("formatting");
            task.dependsOn(allApplyTasks);
        });
        project.getTasks().named("check", check -> check.dependsOn(allVerifyTasks));
    }
}
