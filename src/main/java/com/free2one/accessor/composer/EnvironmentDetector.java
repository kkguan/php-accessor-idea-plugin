package com.free2one.accessor.composer;

import com.free2one.accessor.composer.environmentDetector.EnvironmentDetectorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnvironmentDetector implements ProjectActivity {

//    @Override
//    public void runActivity(@NotNull Project project) {
//        EnvironmentDetectorManager environmentDetector = new EnvironmentDetectorManager(project);
//        environmentDetector.scan();
//    }

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        EnvironmentDetectorManager environmentDetector = new EnvironmentDetectorManager(project);
        environmentDetector.scan();
        return null;
    }
}
