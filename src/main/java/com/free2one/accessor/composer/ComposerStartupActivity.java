package com.free2one.accessor.composer;

import com.free2one.accessor.composer.environmentDetector.EnvironmentDetectorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

public class ComposerStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new ComposerBulkFileListener(project));
        EnvironmentDetectorManager environmentDetector = new EnvironmentDetectorManager(project);
        environmentDetector.scan();
    }

}
