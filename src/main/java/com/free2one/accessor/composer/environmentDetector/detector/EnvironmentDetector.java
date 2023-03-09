package com.free2one.accessor.composer.environmentDetector.detector;

import com.intellij.openapi.project.Project;

public interface EnvironmentDetector {

    void init(Project project);

    void scan();
}
