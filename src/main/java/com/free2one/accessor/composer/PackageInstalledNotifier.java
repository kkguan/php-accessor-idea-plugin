package com.free2one.accessor.composer;

import com.intellij.util.messages.Topic;
import com.jetbrains.php.composer.addDependency.ComposerPackage;

public interface PackageInstalledNotifier {
    @Topic.ProjectLevel
    Topic<PackageInstalledNotifier> ACCESSOR_PACKAGE_INSTALL_TOPIC =
            Topic.create("AccessorPackageInstall", PackageInstalledNotifier.class);

    void installed(ComposerPackage composerPackage, ComposerPackageManager.DependentPackage dependentPackage);
}
