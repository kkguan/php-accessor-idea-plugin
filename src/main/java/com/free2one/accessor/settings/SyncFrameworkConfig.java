package com.free2one.accessor.settings;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.composer.ComposerPackageManager;
import com.free2one.accessor.composer.PackageInstalledNotifier;
import com.free2one.accessor.util.NotificationUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.jetbrains.php.composer.addDependency.ComposerPackage;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class SyncFrameworkConfig implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(SyncFrameworkConfig.class);

    @Override
    public void runActivity(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(
                PackageInstalledNotifier.ACCESSOR_PACKAGE_INSTALL_TOPIC,
                new PackageInstalledNotifier() {
                    @Override
                    public void installed(ComposerPackage composerPackage, ComposerPackageManager.DependentPackage dependentPackage) {
                        if (dependentPackage != ComposerPackageManager.DependentPackage.HYPERF_PHP_ACCESSOR) {
                            return;
                        }

                        for (String version : composerPackage.getVersions()) {
                            if (ComposerPackage.VERSIONS_COMPARATOR.compare(version, "0.5.1") > 0) {
                                System.out.println("version: " + version);
                                return;
                            }
                        }

                        try {
                            String workingDirectory = project.getBasePath();
                            if (workingDirectory == null) {
                                return;
                            }

                            String frameworkProxyRootDirectory = getFrameworkProxyRootDirectory(workingDirectory);
                            if (frameworkProxyRootDirectory == null) {
                                return;
                            }

                            AccessorSettings settings = project.getService(AccessorSettings.class);
                            if (settings.getProxyRootDirectory().equals(frameworkProxyRootDirectory)) {
                                return;
                            }

                            NotificationUtil.notify(project, AccessorBundle.message("settings.sync-hyperf-config.text"), new NotificationAction(AccessorBundle.message("settings.sync-hyperf-config.yes")) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                    settings.setProxyRootDirectory(frameworkProxyRootDirectory);
                                    notification.expire();
                                }
                            });
                        } catch (IOException | InterruptedException e) {
                            LOG.error("get framework accessor config error:" + e.getMessage(), e);
                        }
                    }

                    private static String getFrameworkProxyRootDirectory(String workingDirectory) throws IOException, InterruptedException {
                        String frameworkAccessorConfig = "";

                        ProcessBuilder processBuilder = new ProcessBuilder("php", "bin/hyperf.php", "hyperf-php-accessor:config");
                        processBuilder.directory(new File(workingDirectory));
                        Process process = processBuilder.start();
                        InputStream inputStream = process.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        String sign = "[framework-accessor-config]";
                        while ((line = reader.readLine()) != null) {
                            int indexOfSign = line.indexOf(sign);
                            if (indexOfSign == -1) {
                                continue;
                            }

                            frameworkAccessorConfig = line.substring(sign.length()).trim();
                            break;
                        }
                        process.waitFor();

                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, Object>>() {
                        }.getType();
                        Map<String, Object> resultMap = gson.fromJson(frameworkAccessorConfig, type);

                        return (String) resultMap.get("proxy_root_directory");
                    }
                }
        );
    }
}
