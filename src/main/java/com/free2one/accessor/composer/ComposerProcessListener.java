package com.free2one.accessor.composer;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ComposerProcessListener implements ProcessListener {
    private static final Logger LOG = Logger.getInstance(ComposerProcessListener.class);

    @Override
    public void startNotified(@NotNull ProcessEvent event) {
        LOG.debug("process start: " + event.getText());
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
//        System.out.println("processTerminated");
    }

    @Override
    public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
        ProcessListener.super.processWillTerminate(event, willBeDestroyed);
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        LOG.info("composer: " + event.getText());
        String text = event.getText();
        String sign = "[generated-file]";
        int indexOfSign = text.indexOf(sign);
        if (indexOfSign == -1) {
            return;
        }

        String proxyPath = text.substring(sign.length()).trim();
        Path path = PathManager.findBinFile(proxyPath);
        if (path == null) {
            return;
        }

        VirtualFile file = VfsUtil.findFile(path, true);
        if (file == null) {
            return;
        }

        file.refresh(false, false);
    }
}
