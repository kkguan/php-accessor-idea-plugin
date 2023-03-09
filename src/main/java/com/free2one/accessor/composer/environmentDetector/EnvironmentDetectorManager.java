package com.free2one.accessor.composer.environmentDetector;

import com.free2one.accessor.composer.environmentDetector.detector.ComposerConfigDetector;
import com.free2one.accessor.composer.environmentDetector.detector.ComposerExecutableDetector;
import com.free2one.accessor.composer.environmentDetector.detector.EnvironmentDetector;
import com.free2one.accessor.composer.environmentDetector.detector.PackageInstallationDetector;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.jetbrains.php.composer.ComposerConfigListener;
import com.jetbrains.php.composer.ComposerDataService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentDetectorManager {

    private final Project project;

    private final Map<Class<? extends EnvironmentDetector>, EnvironmentDetector> environmentDetectors;

    {
        environmentDetectors = new HashMap<>() {
            {
                put(ComposerExecutableDetector.class, new ComposerExecutableDetector());
                put(ComposerConfigDetector.class, new ComposerConfigDetector());
                put(PackageInstallationDetector.class, new PackageInstallationDetector());
            }
        };
    }

    public EnvironmentDetectorManager(@NotNull Project project) {
        this.project = project;
        environmentDetectors.forEach((aClass, environmentDetector) -> environmentDetector.init(project));
        registerListener();
    }

    private void registerListener() {
        project.getService(ComposerDataService.class).addConfigListener(new MyComposerConfigListener(this));
//        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new MyComposerJsonListener(this));
    }

    public Project getProject() {
        return project;
    }

    public void scan(Class<? extends EnvironmentDetector> detector) {
        EnvironmentDetector environmentDetector = environmentDetectors.get(detector);
        if (environmentDetector == null) {
            return;
        }

        Task.Backgroundable task = new Task.Backgroundable(project, "Environment scan") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                environmentDetector.scan();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(task);
        } else {
            SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(task));
        }
    }

    public void scan() {
        Task.Backgroundable task = new Task.Backgroundable(project, "Environment scan") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                environmentDetectors.forEach((aClass, environmentDetector) -> environmentDetector.scan());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(task);
        } else {
            SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(task));
        }
    }


    public static class MyComposerConfigListener implements ComposerConfigListener {

        private final EnvironmentDetectorManager manager;

        public MyComposerConfigListener(EnvironmentDetectorManager manager) {
            this.manager = manager;
        }

        @Override
        public void configPathOrLibraryStatusChanged(boolean oldStatus, boolean newStatus) {
        }

        @Override
        public void loadUpdateAvailabilityStatusChanged() {
        }

        @Override
        public void notifyAboutMissingVendorChanged(boolean newStatus) {
            manager.scan();
        }
    }


    public static class MyComposerJsonListener implements BulkFileListener {

        private final EnvironmentDetectorManager manager;

        public MyComposerJsonListener(EnvironmentDetectorManager manager) {
            this.manager = manager;
        }

        @Override
        public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
            ComposerDataService composerDataService = manager.getProject().getService(ComposerDataService.class);
            String configPath = composerDataService.getConfigPath();
            if (configPath == null) {
                return;
            }

            boolean isPending = events.stream().anyMatch(vFileEvent -> {
                        System.out.println(vFileEvent.getPath());
                        return configPath.equals(vFileEvent.getPath());
                    }
            );
            if (isPending) {
                manager.scan(PackageInstallationDetector.class);
            }
        }
    }

}
