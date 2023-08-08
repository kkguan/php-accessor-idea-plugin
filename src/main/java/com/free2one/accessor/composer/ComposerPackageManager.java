package com.free2one.accessor.composer;

import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.ComposerUtils;
import com.jetbrains.php.composer.actions.ComposerCommandExecutor;
import com.jetbrains.php.composer.actions.ComposerCommandRunner;
import com.jetbrains.php.composer.actions.ComposerUpdateAction;
import com.jetbrains.php.composer.addDependency.ComposerPackage;
import com.jetbrains.php.composer.addDependency.ComposerPackagesUtil;
import com.jetbrains.php.composer.execution.ComposerExecution;
import com.jetbrains.php.composer.statistics.ComposerActionStatistics;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ComposerPackageManager {

    private final Project project;

    public ComposerPackageManager(Project project) {
        this.project = project;
    }

    public ComposerPackage findPackage(DependentPackage dependentPackage) {
        ComposerExecution execution = ComposerDataService.getInstance(project).getComposerExecution();
        ComposerCommandRunner executor = new ComposerCommandRunner(execution, project, project.getBasePath(), new ComposerProgressIndicator());
        CapturingProcessAdapter outputCapturingAdapter = new CapturingProcessAdapter();
        List<String> command = Arrays.asList("show", dependentPackage.getName(), "--no-ansi", "-d", project.getBasePath());
        ComposerCommandRunner.ExecutionResult executionResult = executor.runCommand(command, outputCapturingAdapter);
        if (executionResult.myProgressIndicatorCancelled || !executionResult.isSuccess()) {
            return null;
        }

        try {
            return ComposerPackagesUtil.parsePackageDescriptionCommandOutput(dependentPackage.getName(), outputCapturingAdapter.getOutput().getStdout());
        } catch (IOException e) {
            return null;
        }
    }

    public void installPackage(DependentPackage dependentPackage, VirtualFile configFile) {
        ComposerCommandExecutor commandExecutor = ComposerUpdateAction.createExecutor(
                project,
                configFile,
                "--no-interaction --no-ansi -d " + project.getBasePath(),
                "",
                ComposerActionStatistics.Action.REQUIRE,
                ComposerUtils.getInstallationCommand(dependentPackage.getName(), dependentPackage.getVersion()));
        commandExecutor.execute();
    }

    public enum DependentPackage {
        PHP_ACCESSOR("free2one/php-accessor", "~0.4"),
        HYPERF_PHP_ACCESSOR("free2one/hyperf-php-accessor", "~0.3"),
        HYPERF_FRAMEWORK("hyperf/framework", "*");

        private final String name;
        private final String version;

        DependentPackage(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }
}
