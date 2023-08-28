package com.free2one.accessor.findUsages;

import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessorFindUsagesSearchScope extends GlobalSearchScope {

    public AccessorFindUsagesSearchScope(@Nullable Project project) {
        super(project);
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull Module aModule) {
        return false;
    }

    @Override
    public boolean isSearchInLibraries() {
        return false;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        if (getProject() == null) {
            return false;
        }

        AccessorSettings settings = AccessorSettings.getInstance(getProject());
        return !settings.containSettingDirectories(file.getPath());
    }
}
