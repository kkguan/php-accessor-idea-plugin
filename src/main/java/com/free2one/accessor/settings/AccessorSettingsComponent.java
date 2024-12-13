package com.free2one.accessor.settings;

import com.free2one.accessor.AccessorBundle;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import com.jetbrains.php.debug.PhpDebugUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

public class AccessorSettingsComponent {
    private final JPanel myMainPanel;
    private final TextFieldWithBrowseButton proxyRootDirectory = new TextFieldWithBrowseButton();
    private final TableView<Ref<String>> extraProxyDirectories = new TableView<>();
    private final Project myProject;

    public AccessorSettingsComponent(Project myProject) {
        this.myProject = myProject;
        this.extraProxyDirectories.setModelAndUpdateColumns(new ListTableModel<>(new ColumnInfo[]{new ColumnInfo<Ref<String>, String>("") {
            final TableCellRenderer myRenderer = new ColoredTableCellRenderer() {
                protected void customizeCellRenderer(@NotNull JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
                    this.append(row + 1 + ". ");
                    if (value instanceof String) {
                        String path = (String) value;
                        boolean hasError;
                        if (FileUtil.isAbsolute(path)) {
                            hasError = LocalFileSystem.getInstance().findFileByPath(path) == null;
                        } else {
                            hasError = StringUtil.isEmptyOrSpaces(path);
                        }

                        this.append("\"" + path + "\"", !hasError ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.ERROR_ATTRIBUTES);
                    }
                }
            };

            public String valueOf(Ref<String> stringRef) {
                return stringRef.get();
            }

            public boolean isCellEditable(Ref<String> stringRef) {
                return true;
            }

            public void setValue(Ref<String> stringRef, String value) {
                stringRef.set(value);
            }

            public TableCellRenderer getRenderer(Ref<String> stringRef) {
                return this.myRenderer;
            }

            public TableCellEditor getEditor(Ref<String> o) {
                return new AbstractTableCellEditor() {
                    final TextFieldWithBrowseButton myComponent = new TextFieldWithBrowseButton();

                    {
                        this.myComponent.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor(), AccessorSettingsComponent.this.myProject) {
//                            protected @NotNull String expandPath(@NotNull String path) {
//                                return AccessorSettingsComponent.doExpandPath(path, AccessorSettingsComponent.this.myProject);
//                            }
                        });
                    }

                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                        this.myComponent.getChildComponent().setText((String) value);
                        return this.myComponent;
                    }

                    public Object getCellEditorValue() {
                        return this.getText(this.myComponent.getChildComponent());
                    }

                    private @NlsSafe String getText(JTextField component) {
                        return component.getText();
                    }
                };
            }
        }}));
        this.extraProxyDirectories.setShowGrid(false);
        this.extraProxyDirectories.setTableHeader(null);
//        this.extraProxyDirectories.getEmptyText().setText(PhpBundle.message("PhpProjectConfigurable.provide.include.path"));


        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(this.extraProxyDirectories, null);
        toolbarDecorator.setAddAction(new AnActionButtonRunnable() {
            public void run(AnActionButton button) {
                AccessorSettingsComponent.this.doSpecifyOther();
            }
        });
        AnActionButtonUpdater moveUpDownUpdater = (e) -> {
            RowSorter<? extends TableModel> sorter = this.extraProxyDirectories.getRowSorter();
            if (sorter == null) {
                return true;
            } else {
                List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
                if (keys == null) {
                    return true;
                } else {
                    Iterator<? extends RowSorter.SortKey> var4 = keys.iterator();
                    RowSorter.SortKey key;
                    do {
                        if (!var4.hasNext()) {
                            return true;
                        }

                        key = var4.next();
                    } while (key.getSortOrder() == SortOrder.UNSORTED);

                    return false;
                }
            }
        };
        toolbarDecorator.setMoveUpActionUpdater(moveUpDownUpdater);
        toolbarDecorator.setMoveDownActionUpdater(moveUpDownUpdater);

//        proxyRootDirectory.addActionListener(
//                new ComponentWithBrowseButton.BrowseFolderActionListener<>(
//                        AccessorBundle.message("settings.extra-proxy-directories.text"),
//                        AccessorBundle.message("settings.extra-proxy-directories.text"),
//                        this.proxyRootDirectory,
//                        myProject,
//                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
//                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
//                ) {
////            protected @NotNull String expandPath(@NotNull String path) {
////                return AccessorSettingsComponent.doExpandPath(path, AccessorSettingsComponent.this.myProject);
////            }
//        });

        proxyRootDirectory.addActionListener(
                new ComponentWithBrowseButton.BrowseFolderActionListener<>(
//                        AccessorBundle.message("settings.extra-proxy-directories.text"),
//                        AccessorBundle.message("settings.extra-proxy-directories.text"),
                        this.proxyRootDirectory,
                        myProject,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
                ) {
//            protected @NotNull String expandPath(@NotNull String path) {
//                return AccessorSettingsComponent.doExpandPath(path, AccessorSettingsComponent.this.myProject);
//            }
                });

        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(AccessorBundle.message("settings.proxy-root-directory.text")), proxyRootDirectory, 1, false)
                .addComponent(new JBLabel(AccessorBundle.message("settings.extra-proxy-directories.text")))
                .addComponent(toolbarDecorator.createPanel())
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void doSpecifyOther() {
        VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), this.myProject, null);
        if (file == null) {
            this.addPathToIncludePathTable("");
        } else {
            this.addPathToIncludePathTable(file.getPresentableUrl());
        }
    }

    private void addPathToIncludePathTable(@NotNull String path) {
        this.addPathToIncludePathTable(Collections.singletonList(path));
    }

    private void addPathToIncludePathTable(@NotNull List<String> paths) {
        addToTable(paths, this.extraProxyDirectories);
    }

    private static void addToTable(@NotNull List<String> paths, @NotNull TableView<Ref<String>> table) {
        ListTableModel<Ref<String>> tableModel = table.getListTableModel();
        TableUtil.stopEditing(table);
        int rowCount = table.getRowCount();
        tableModel.addRows(PhpDebugUtil.wrap(paths));
        if (rowCount != table.getRowCount()) {
            int index = tableModel.getRowCount() - 1;
            table.editCellAt(index, 0);
            table.setRowSelectionInterval(index - paths.size() + 1, index);
            table.setColumnSelectionInterval(0, 0);
            table.getParent().repaint();
            Component editorComponent = table.getEditorComponent();
            if (editorComponent != null) {
                Rectangle bounds = editorComponent.getBounds();
                table.scrollRectToVisible(bounds);
                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
                    IdeFocusManager.getGlobalInstance().requestFocus(editorComponent, true);
                });
            }
        }
    }

    private static @NotNull @NlsSafe String doExpandPath(@NotNull String path, @NotNull Project project) {
        if (FileUtil.isAbsolute(path)) {
            return path;
        } else {
//            VirtualFile baseDir = project.getBaseDir();
            String basePath = project.getBasePath();
            if (basePath == null) {
                return path;
            } else {
//                String var10000 = baseDir.getPath();
//                var10000 = FileUtil.toCanonicalPath(var10000 + "/" + path);
//
//
//                return var10000;
                return FileUtil.toCanonicalPath(basePath + "/" + path);
            }
        }
    }

    private static void addToTable(@NotNull String path, @NotNull TableView<Ref<String>> table) {
        addToTable(Collections.singletonList(path), table);
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

    public String[] getExtraProxyDirectories() {
        return this.extraProxyDirectories.getListTableModel().getItems().stream().map(Ref::get).toArray(String[]::new);
    }

    public void setExtraProxyDirectories(String[] directories) {
        this.extraProxyDirectories.getListTableModel().setItems(PhpDebugUtil.wrap(new ArrayList<>(Arrays.asList(directories))));
    }

}
