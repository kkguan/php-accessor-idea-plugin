package com.free2one.accessor.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AccessorSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField proxyRootDirectory = new JBTextField();
    private final JTextArea extraProxyDirectories = new JTextArea(2, 1);

    public AccessorSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Proxy root directory: "), proxyRootDirectory, 1, false)
                .addComponent(new JBLabel("Extra proxy directories(one per line)."))
                .addComponent(new JBScrollPane(extraProxyDirectories))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return proxyRootDirectory;
    }

    @NotNull
    public String getProxyRootDirectoryText() {
        return proxyRootDirectory.getText();
    }

    public void setProxyRootDirectoryText(@NotNull String newText) {
        proxyRootDirectory.setText(newText);
    }


    public String getExtraProxyDirectoriesText() {
        return extraProxyDirectories.getText();
    }

    public void setExtraProxyDirectoriesText(@NotNull String newText) {
        extraProxyDirectories.setText(newText);
    }

}
