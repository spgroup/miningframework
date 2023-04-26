package org.geoserver.web.wicket.browser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.web.wicket.ParamResourceModel;

@SuppressWarnings("serial")
public class GeoServerFileChooser extends Panel {

    static Boolean HIDE_FS = null;

    static {
        HIDE_FS = Boolean.valueOf(GeoServerExtensions.getProperty("GEOSERVER_FILEBROWSER_HIDEFS"));
    }

    static File USER_HOME = null;

    static {
        try {
            File hf = null;
            String home = System.getProperty("user.home");
            if (home != null) {
                hf = new File(home);
            }
            if (hf != null && hf.exists()) {
                USER_HOME = hf;
            }
        } catch (Throwable t) {
        }
    }

    FileBreadcrumbs breadcrumbs;

    FileDataView fileTable;

    boolean hideFileSystem = false;

    IModel file;

    public GeoServerFileChooser(String id, IModel file) {
        this(id, file, HIDE_FS);
    }

    public GeoServerFileChooser(String id, IModel file, boolean hideFileSystem) {
        super(id, file);
        this.file = file;
        this.hideFileSystem = hideFileSystem;
        ArrayList<File> roots = new ArrayList<File>();
        if (!hideFileSystem) {
            roots.addAll(Arrays.asList(File.listRoots()));
        }
        Collections.sort(roots);
        GeoServerResourceLoader loader = GeoServerExtensions.bean(GeoServerResourceLoader.class);
        File dataDirectory = loader.getBaseDirectory();
        roots.add(0, dataDirectory);
        if (!hideFileSystem && USER_HOME != null) {
            roots.add(1, USER_HOME);
        }
        File selection = (File) file.getObject();
        if (selection != null) {
            File relativeToDataDir = loader.url(selection.getPath());
            if (relativeToDataDir != null) {
                selection = relativeToDataDir;
            }
        }
        File selectionRoot = null;
        if (selection != null && selection.exists()) {
            for (File root : roots) {
                if (isSubfile(root, selection.getAbsoluteFile())) {
                    selectionRoot = root;
                    break;
                }
            }
            if (selectionRoot == null) {
                selectionRoot = dataDirectory;
                file = new Model(selectionRoot);
            } else {
                if (!selection.isDirectory()) {
                    file = new Model(selection.getParentFile());
                } else {
                    file = new Model(selection);
                }
            }
        } else {
            selectionRoot = dataDirectory;
            file = new Model(selectionRoot);
        }
        this.file = file;
        setDefaultModel(file);
        final DropDownChoice choice = new DropDownChoice("roots", new Model(selectionRoot), new Model(roots), new FileRootsRenderer());
        choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                File selection = (File) choice.getModelObject();
                breadcrumbs.setRootFile(selection);
                updateFileBrowser(selection, target);
            }
        });
        choice.setOutputMarkupId(true);
        add(choice);
        breadcrumbs = new FileBreadcrumbs("breadcrumbs", new Model(selectionRoot), file) {

            @Override
            protected void pathItemClicked(File file, AjaxRequestTarget target) {
                updateFileBrowser(file, target);
            }
        };
        breadcrumbs.setOutputMarkupId(true);
        add(breadcrumbs);
        fileTable = new FileDataView("fileTable", new FileProvider(file)) {

            @Override
            protected void linkNameClicked(File file, AjaxRequestTarget target) {
                updateFileBrowser(file, target);
            }
        };
        fileTable.setOutputMarkupId(true);
        add(fileTable);
    }

    void updateFileBrowser(File file, AjaxRequestTarget target) {
        if (file.isDirectory()) {
            directoryClicked(file, target);
        } else if (file.isFile()) {
            fileClicked(file, target);
        }
    }

    protected void fileClicked(File file, AjaxRequestTarget target) {
    }

    protected void directoryClicked(File file, AjaxRequestTarget target) {
        GeoServerFileChooser.this.file.setObject(file);
        fileTable.getProvider().setDirectory(new Model(file));
        breadcrumbs.setSelection(file);
        target.addComponent(fileTable);
        target.addComponent(breadcrumbs);
    }

    private boolean isSubfile(File root, File selection) {
        if (selection == null || "".equals(selection.getPath()))
            return false;
        if (selection.equals(root))
            return true;
        return isSubfile(root, selection.getParentFile());
    }

    public void setFilter(IModel<? extends FileFilter> fileFilter) {
        fileTable.provider.setFileFilter(fileFilter);
    }

    public void setFileTableHeight(String height) {
        fileTable.setTableHeight(height);
    }

    class FileRootsRenderer implements IChoiceRenderer {

        public Object getDisplayValue(Object o) {
            File f = (File) o;
            if (f == USER_HOME) {
                return new ParamResourceModel("userHome", GeoServerFileChooser.this).getString();
            } else {
                GeoServerResourceLoader loader = GeoServerExtensions.bean(GeoServerResourceLoader.class);
                if (f.equals(loader.getBaseDirectory())) {
                    return new ParamResourceModel("dataDirectory", GeoServerFileChooser.this).getString();
                }
            }
            try {
                final String displayName = FileSystemView.getFileSystemView().getSystemDisplayName(f);
                if (displayName != null && displayName.length() > 0) {
                    return displayName;
                }
                return FilenameUtils.getPrefix(f.getAbsolutePath());
            } catch (Exception e) {
            }
            return f.getName();
        }

        public String getIdValue(Object o, int count) {
            File f = (File) o;
            return "" + count;
        }
    }
}