package com.free2one.accessor.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AccessorSettingsConfigurable implements Configurable {
    private final AccessorSettings settings;
    private final Project project;
    private AccessorSettingsComponent mySettingsComponent;


    public AccessorSettingsConfigurable(Project project) {
        this.settings = AccessorSettings.getInstance(project);
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "PHP Accessor";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AccessorSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        settings.setProxyRootDirectory(mySettingsComponent.getProxyRootDirectoryText());
        settings.setExtraProxyDirectoriesFromText(mySettingsComponent.getExtraProxyDirectoriesText());
//        settings.setExtraProxyDirectories(mySettingsComponent.getExtraProxyDirectoriesText());
    }

    @Override
    public void reset() {
        mySettingsComponent.setProxyRootDirectoryText(settings.getProxyRootDirectory());
        mySettingsComponent.setExtraProxyDirectoriesText(settings.getExtraProxyDirectories2String());
//        mySettingsComponent.setExtraProxyDirectoriesText(settings.getExtraProxyDirectories(project));
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
