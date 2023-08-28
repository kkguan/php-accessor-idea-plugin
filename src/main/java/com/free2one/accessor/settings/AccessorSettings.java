package com.free2one.accessor.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@State(
        name = "com.free2one.accessor.settings.AccessorSettings",
        storages = @Storage("php-accessor-plugin.xml")
)
public final class AccessorSettings implements PersistentStateComponent<AccessorSettings> {
    public String proxyRootDirectory;
    public String[] extraProxyDirectories;

    @Transient
    private Project project;

    public AccessorSettings() {
    }

    public AccessorSettings(@NotNull Project project) {
        this.project = project;
//        System.out.println(project);
    }

    public static AccessorSettings getInstance(Project project) {
        return project.getService(AccessorSettings.class);
    }

    public String getExtraProxyDirectories2String() {
        StringBuilder builder = new StringBuilder();
        for (String text : getExtraProxyDirectories()
        ) {
            builder.append(text).append("\n");
        }
        return builder.toString();
    }

    public String[] getExtraProxyDirectories() {
        return Objects.requireNonNullElseGet(extraProxyDirectories, () -> new String[]{project.getBasePath() + getFileSeparator() + "runtime"});
    }

    public void setExtraProxyDirectories(String[] extraProxyDirectories) {
        this.extraProxyDirectories = extraProxyDirectories;
    }

    private String getFileSeparator() {
        String separator;
        if (project.getBasePath().lastIndexOf("/") != -1) {
            separator = "/";
        } else if (project.getBasePath().lastIndexOf("\\") != -1) {
            separator = "\\";
        } else {
            separator = File.separator;
        }

        return separator;
    }

    public void setExtraProxyDirectoriesFromText(String text) {
        this.extraProxyDirectories = Arrays.stream(text.trim().split("\\R")).filter(x -> !StringUtils.isBlank(x)).toArray(String[]::new);
    }

    @Override
    public AccessorSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AccessorSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean containSettingDirectories(String path) {
        if (path.contains(getProxyRootDirectory())) {
            return true;
        }

        for (String extraProxyDirectory : getExtraProxyDirectories()) {
            if (path.contains(extraProxyDirectory)) {
                return true;
            }
        }

        return false;
    }

    public String getProxyRootDirectory() {
        if (proxyRootDirectory == null) {
            return project.getBasePath() + getFileSeparator() + ".php-accessor";
        }
        return proxyRootDirectory;
    }

    public void setProxyRootDirectory(String proxyRootDirectory) {
        this.proxyRootDirectory = proxyRootDirectory;
    }

    public boolean containProxyDirectory(String path) {
        return path.contains(getProxyRootDirectory());
    }
}
