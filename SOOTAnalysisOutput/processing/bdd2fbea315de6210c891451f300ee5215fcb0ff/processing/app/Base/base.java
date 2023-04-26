package processing.app;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import processing.app.contrib.*;
import processing.app.ui.*;
import processing.core.*;
import processing.data.StringList;

public class Base {

    static private final int REVISION = 241;

    static private String VERSION_NAME = "0241";

    static public boolean DEBUG = false;

    static HashMap<Integer, String> platformNames = new HashMap<Integer, String>();

    static {
        platformNames.put(PConstants.WINDOWS, "windows");
        platformNames.put(PConstants.MACOSX, "macosx");
        platformNames.put(PConstants.LINUX, "linux");
    }

    static HashMap<String, Integer> platformIndices = new HashMap<String, Integer>();

    static {
        platformIndices.put("windows", PConstants.WINDOWS);
        platformIndices.put("macosx", PConstants.MACOSX);
        platformIndices.put("linux", PConstants.LINUX);
    }

    static Platform platform;

    static int nativeBits;

    static {
        nativeBits = 32;
        String bits = System.getProperty("sun.arch.data.model");
        if (bits != null) {
            if (bits.equals("64")) {
                nativeBits = 64;
            }
        } else {
            if (System.getProperty("java.vm.name").contains("64")) {
                nativeBits = 64;
            }
        }
    }

    static private boolean commandLine;

    PreferencesFrame preferencesFrame;

    ContributionManagerDialog contributionManagerFrame;

    static File untitledFolder;

    protected List<Editor> editors = Collections.synchronizedList(new ArrayList<Editor>());

    protected Editor activeEditor;

    static public JMenu defaultFileMenu;

    private Mode nextMode;

    private Mode[] coreModes;

    protected ArrayList<ModeContribution> modeContribs;

    protected ArrayList<ExamplesContribution> exampleContribs;

    private JMenu sketchbookMenu;

    private Recent recent;

    private JFileChooser openChooser;

    static protected File sketchbookFolder;

    static public void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    createAndShowGUI(args);
                } catch (Throwable t) {
                    showBadnessTrace("It was not meant to be", "A serious problem happened during startup. Please report:\n" + "http://github.com/processing/processing/issues/new", t, true);
                }
            }
        });
    }

    static private void createAndShowGUI(String[] args) {
        try {
            File versionFile = getContentFile("lib/version.txt");
            if (versionFile.exists()) {
                String version = PApplet.loadStrings(versionFile)[0];
                if (!version.equals(VERSION_NAME)) {
                    VERSION_NAME = version;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initPlatform();
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        Language.init();
        Preferences.init();
        if (!SingleInstance.alreadyRunning(args)) {
            try {
                platform.setLookAndFeel();
            } catch (Exception e) {
                loge("Could not set the Look & Feel", e);
            }
            boolean sketchbookPrompt = false;
            if (Preferences.getBoolean("welcome.show")) {
                if (!Preferences.getBoolean("welcome.seen")) {
                    String oldPath = Preferences.getOldSketchbookPath();
                    if (oldPath != null) {
                        String newPath = Preferences.getSketchbookPath();
                        if (newPath == null) {
                            sketchbookPrompt = true;
                        } else if (oldPath.equals(newPath)) {
                            sketchbookPrompt = true;
                        }
                    }
                }
            }
            locateSketchbookFolder();
            try {
                untitledFolder = Base.createTempFolder("untitled", "sketches", null);
                untitledFolder.deleteOnExit();
            } catch (IOException e) {
                Base.showError("Trouble without a name", "Could not create a place to store untitled sketches.\n" + "That's gonna prevent us from continuing.", e);
            }
            log("about to create base...");
            try {
                final Base base = new Base(args);
                SingleInstance.startServer(base);
                if (Preferences.getBoolean("welcome.show")) {
                    final boolean prompt = sketchbookPrompt;
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                new Welcome(base, prompt);
                            } catch (IOException e) {
                                Base.showBadnessTrace("Unwelcoming", "Please report this error to\n" + "https://github.com/processing/processing/issues", e, false);
                            }
                        }
                    });
                }
            } catch (Throwable t) {
                if (t.getCause() != null) {
                    t = t.getCause();
                }
                showBadnessTrace("We're off on the wrong foot", "An error occurred during startup.", t, true);
            }
            log("done creating base...");
        }
    }

    public static void setCommandLine() {
        commandLine = true;
    }

    static public boolean isCommandLine() {
        return commandLine;
    }

    static public void initPlatform() {
        try {
            Class<?> platformClass = Class.forName("processing.app.Platform");
            if (Base.isMacOS()) {
                platformClass = Class.forName("processing.app.platform.MacPlatform");
            } else if (Base.isWindows()) {
                platformClass = Class.forName("processing.app.platform.WindowsPlatform");
            } else if (Base.isLinux()) {
                platformClass = Class.forName("processing.app.platform.LinuxPlatform");
            }
            platform = (Platform) platformClass.newInstance();
        } catch (Exception e) {
            Base.showError("Problem Setting the Platform", "An unknown error occurred while trying to load\n" + "platform-specific code for your machine.", e);
        }
    }

    private String getDefaultModeIdentifier() {
        return "processing.mode.java.JavaMode";
    }

    private void buildCoreModes() {
        Mode javaMode = ModeContribution.load(this, getContentFile("modes/java"), getDefaultModeIdentifier()).getMode();
        coreModes = new Mode[] { javaMode };
    }

    void rebuildContribModes() {
        if (modeContribs == null) {
            modeContribs = new ArrayList<ModeContribution>();
        }
        ModeContribution.loadMissing(this);
    }

    void rebuildContribExamples() {
        if (exampleContribs == null) {
            exampleContribs = new ArrayList<ExamplesContribution>();
        }
        ExamplesContribution.loadMissing(this);
    }

    public Base(String[] args) throws Exception {
        ContributionManager.cleanup(this);
        buildCoreModes();
        rebuildContribModes();
        rebuildContribExamples();
        recent = new Recent(this);
        String lastModeIdentifier = Preferences.get("mode.last");
        if (lastModeIdentifier == null) {
            nextMode = getDefaultMode();
            log("Nothing set for last.sketch.mode, using default.");
        } else {
            for (Mode m : getModeList()) {
                if (m.getIdentifier().equals(lastModeIdentifier)) {
                    logf("Setting next mode to %s.", lastModeIdentifier);
                    nextMode = m;
                }
            }
            if (nextMode == null) {
                nextMode = getDefaultMode();
                logf("Could not find mode %s, using default.", lastModeIdentifier);
            }
        }
        contributionManagerFrame = new ContributionManagerDialog();
        nextMode.rebuildLibraryList();
        platform.init(this);
        boolean opened = false;
        for (int i = 0; i < args.length; i++) {
            String path = args[i];
            if (isWindows()) {
                try {
                    File file = new File(args[i]);
                    path = file.getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (handleOpen(path) != null) {
                opened = true;
            }
        }
        if (!opened) {
            handleNew();
        }
        new UpdateCheck(this);
    }

    public Editor getActiveEditor() {
        return activeEditor;
    }

    public List<Editor> getEditors() {
        return editors;
    }

    public void changeMode(Mode mode) {
        if (activeEditor.getMode() != mode) {
            Sketch sketch = activeEditor.getSketch();
            nextMode = mode;
            if (sketch.isUntitled()) {
                handleClose(activeEditor, true);
                handleNew();
            } else {
                boolean newModeCanHandleCurrentSource = true;
                for (final SketchCode code : sketch.getCode()) {
                    if (!mode.validExtension(code.getExtension())) {
                        newModeCanHandleCurrentSource = false;
                        break;
                    }
                }
                if (newModeCanHandleCurrentSource) {
                    final File props = new File(sketch.getCodeFolder(), "sketch.properties");
                    saveModeSettings(props, nextMode);
                    handleClose(activeEditor, true);
                    handleOpen(sketch.getMainFilePath());
                }
            }
        }
    }

    public List<ModeContribution> getModeContribs() {
        return modeContribs;
    }

    public List<Mode> getModeList() {
        ArrayList<Mode> allModes = new ArrayList<Mode>();
        allModes.addAll(Arrays.asList(coreModes));
        if (modeContribs != null) {
            for (ModeContribution contrib : modeContribs) {
                allModes.add(contrib.getMode());
            }
        }
        return allModes;
    }

    public List<ExamplesContribution> getExampleContribs() {
        return exampleContribs;
    }

    private List<Contribution> getInstalledContribs() {
        List<Contribution> contributions = new ArrayList<Contribution>();
        List<ModeContribution> modeContribs = getModeContribs();
        contributions.addAll(modeContribs);
        for (ModeContribution modeContrib : modeContribs) {
            Mode mode = modeContrib.getMode();
            contributions.addAll(new ArrayList<Library>(mode.contribLibraries));
        }
        contributions.addAll(ToolContribution.loadAll(getSketchbookToolsFolder()));
        contributions.addAll(getExampleContribs());
        return contributions;
    }

    public byte[] getInstalledContribsInfo() {
        List<Contribution> contribs = getInstalledContribs();
        StringList entries = new StringList();
        for (Contribution c : contribs) {
            String entry = c.getTypeName() + "=" + PApplet.urlEncode(String.format("name=%s\nurl=%s\nrevision=%d\nversion=%s", c.getName(), c.getUrl(), c.getVersion(), c.getPrettyVersion()));
            entries.append(entry);
        }
        String joined = "id=" + Preferences.get("update.id") + "&" + entries.join("&");
        return joined.getBytes();
    }

    public void handleActivated(Editor whichEditor) {
        activeEditor = whichEditor;
        EditorConsole.setEditor(activeEditor);
        nextMode = whichEditor.getMode();
        Preferences.set("mode.last", nextMode.getIdentifier());
    }

    boolean breakTime = false;

    String[] months = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

    public void handleNew() {
        try {
            File newbieDir = null;
            String newbieName = null;
            File newbieParentDir = untitledFolder;
            String prefix = Preferences.get("editor.untitled.prefix");
            int index = 0;
            String format = Preferences.get("editor.untitled.suffix");
            String suffix = null;
            if (format == null) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                suffix = months[month] + PApplet.nf(day, 2);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat(format);
                suffix = formatter.format(new Date());
            }
            do {
                if (index == 26) {
                    if (!breakTime) {
                        Base.showWarning("Time for a Break", "You've reached the limit for auto naming of new sketches\n" + "for the day. How about going for a walk instead?", null);
                        breakTime = true;
                    } else {
                        Base.showWarning("Sunshine", "No really, time for some fresh air for you.", null);
                    }
                    return;
                }
                newbieName = prefix + suffix + ((char) ('a' + index));
                newbieName = Sketch.sanitizeName(newbieName);
                newbieDir = new File(newbieParentDir, newbieName);
                index++;
            } while (newbieDir.exists() || new File(sketchbookFolder, newbieName).exists());
            newbieDir.mkdirs();
            File newbieFile = new File(newbieDir, newbieName + "." + nextMode.getDefaultExtension());
            if (!newbieFile.createNewFile()) {
                throw new IOException(newbieFile + " already exists.");
            }
            if (!nextMode.equals(getDefaultMode())) {
                saveModeSettings(new File(newbieDir, "sketch.properties"), nextMode);
            }
            String path = newbieFile.getAbsolutePath();
            handleOpen(path, true);
        } catch (IOException e) {
            Base.showWarning("That's new to me", "A strange and unexplainable error occurred\n" + "while trying to create a new sketch.", e);
        }
    }

    private void saveModeSettings(final File sketchProps, final Mode mode) {
        try {
            final Settings settings = new Settings(sketchProps);
            settings.set("mode", mode.getTitle());
            settings.set("mode.id", mode.getIdentifier());
            settings.save();
        } catch (IOException e) {
            System.err.println("While creating " + sketchProps + ": " + e.getMessage());
        }
    }

    public Mode getDefaultMode() {
        return coreModes[0];
    }

    public Mode getNextMode() {
        return nextMode;
    }

    public void handleOpenPrompt() {
        final ArrayList<String> extensions = new ArrayList<String>();
        for (Mode mode : getModeList()) {
            extensions.add(mode.getDefaultExtension());
        }
        final String prompt = Language.text("open");
        if (Preferences.getBoolean("chooser.files.native")) {
            FileDialog openDialog = new FileDialog(activeEditor, prompt, FileDialog.LOAD);
            openDialog.setFilenameFilter(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    for (String ext : extensions) {
                        if (name.toLowerCase().endsWith("." + ext)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            openDialog.setVisible(true);
            String directory = openDialog.getDirectory();
            String filename = openDialog.getFile();
            if (filename != null) {
                File inputFile = new File(directory, filename);
                handleOpen(inputFile.getAbsolutePath());
            }
        } else {
            if (openChooser == null) {
                openChooser = new JFileChooser();
            }
            openChooser.setDialogTitle(prompt);
            openChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    for (String ext : extensions) {
                        if (file.getName().toLowerCase().endsWith("." + ext)) {
                            return true;
                        }
                    }
                    return false;
                }

                public String getDescription() {
                    return "Processing Sketch";
                }
            });
            if (openChooser.showOpenDialog(activeEditor) == JFileChooser.APPROVE_OPTION) {
                handleOpen(openChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public Editor handleOpen(String path) {
        return handleOpen(path, false);
    }

    public Editor handleOpen(String path, boolean untitled) {
        return handleOpen(path, untitled, new EditorState(editors));
    }

    protected Editor handleOpen(String path, boolean untitled, EditorState state) {
        try {
            final File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            for (Editor editor : editors) {
                for (SketchCode tab : editor.getSketch().getCode()) {
                    if (tab.getFile().equals(file)) {
                        editor.toFront();
                        handleRecent(editor);
                        return editor;
                    }
                }
            }
            if (!Sketch.isSanitaryName(file.getName())) {
                Base.showWarning("You're tricky, but not tricky enough", file.getName() + " is not a valid name for a sketch.\n" + "Better to stick to ASCII, no spaces, and make sure\n" + "it doesn't start with a number.", null);
                return null;
            }
            if (!nextMode.canEdit(file)) {
                final Mode mode = selectMode(file);
                if (mode == null) {
                    return null;
                }
                nextMode = mode;
            }
            Editor editor = null;
            try {
                editor = nextMode.createEditor(this, path, state);
            } catch (NoSuchMethodError nsme) {
                Base.showWarning("Mode out of date", nextMode.getTitle() + " is not compatible with this version of Processing.\n" + "Try updating the Mode or contact its author for a new version.", nsme);
            } catch (Throwable t) {
                showBadnessTrace("Mode Problems", "A nasty error occurred while trying to use " + nextMode.getTitle() + ".\n" + "It may not be compatible with this version of Processing.\n" + "Try updating the Mode or contact its author for a new version.", t, false);
            }
            if (editor == null) {
                Mode defaultMode = getDefaultMode();
                if (nextMode == defaultMode) {
                    Base.showError("Editor Problems", "An error occurred while trying to change modes.\n" + "We'll have to quit for now because it's an\n" + "unfortunate bit of indigestion with the default Mode.", null);
                } else {
                    editor = defaultMode.createEditor(this, path, state);
                }
            }
            Sketch sketch = editor.getSketch();
            if (sketch == null) {
                return null;
            }
            sketch.setUntitled(untitled);
            editors.add(editor);
            handleRecent(editor);
            editor.setVisible(true);
            return editor;
        } catch (Throwable t) {
            showBadnessTrace("Terrible News", "A serious error occurred while " + "trying to create a new editor window.", t, nextMode == getDefaultMode());
            nextMode = getDefaultMode();
            return null;
        }
    }

    private static class ModeInfo {

        public final String title;

        public final String id;

        public ModeInfo(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    private static ModeInfo modeInfoFor(final File sketch) {
        final File sketchFolder = sketch.getParentFile();
        final File sketchProps = new File(sketchFolder, "sketch.properties");
        if (!sketchProps.exists()) {
            return null;
        }
        try {
            final Settings settings = new Settings(sketchProps);
            final String title = settings.get("mode");
            final String id = settings.get("mode.id");
            if (title == null || id == null) {
                return null;
            }
            return new ModeInfo(id, title);
        } catch (IOException e) {
            System.err.println("While trying to read " + sketchProps + ": " + e.getMessage());
        }
        return null;
    }

    private Mode promptForMode(final File sketch, final ModeInfo preferredMode) {
        final String extension = sketch.getName().substring(sketch.getName().lastIndexOf('.') + 1);
        final List<Mode> possibleModes = new ArrayList<Mode>();
        for (final Mode mode : getModeList()) {
            if (mode.canEdit(sketch)) {
                possibleModes.add(mode);
            }
        }
        if (possibleModes.size() == 1 && possibleModes.get(0).getIdentifier().equals(getDefaultModeIdentifier())) {
            return possibleModes.get(0);
        }
        if (possibleModes.size() == 0) {
            if (preferredMode == null) {
                Base.showWarning("Modeless Dialog", "I don't know how to open a sketch with the \"" + extension + "\"\nfile extension. You'll have to install a different" + "\nProcessing mode for that.");
            } else {
                Base.showWarning("Modeless Dialog", "You'll have to install " + preferredMode.title + " Mode " + "\nin order to open that sketch.");
            }
            return null;
        }
        final Mode[] modes = possibleModes.toArray(new Mode[possibleModes.size()]);
        final String message = preferredMode == null ? (nextMode.getTitle() + " Mode can't open ." + extension + " files, " + "but you have one or more modes\ninstalled that can. " + "Would you like to try one?") : ("That's a " + preferredMode.title + " Mode sketch, " + "but you don't have " + preferredMode.title + " installed.\n" + "Would you like to try a different mode for opening a " + "." + extension + " sketch?");
        return (Mode) JOptionPane.showInputDialog(null, message, "Choose Wisely", JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
    }

    private Mode selectMode(final File sketch) {
        final ModeInfo modeInfo = modeInfoFor(sketch);
        final Mode specifiedMode = modeInfo == null ? null : findMode(modeInfo.id);
        if (specifiedMode != null) {
            return specifiedMode;
        }
        return promptForMode(sketch, modeInfo);
    }

    protected Mode findMode(String id) {
        for (Mode mode : getModeList()) {
            if (mode.getIdentifier().equals(id)) {
                return mode;
            }
        }
        return null;
    }

    public boolean handleClose(Editor editor, boolean modeSwitch) {
        if (!editor.checkModified()) {
            return false;
        }
        editor.internalCloseRunner();
        if (editors.size() == 1) {
            if (Base.isMacOS()) {
                if (defaultFileMenu == null) {
                    Object[] options = { Language.text("prompt.ok"), Language.text("prompt.cancel") };
                    String prompt = "<html> " + "<head> <style type=\"text/css\">" + "b { font: 13pt \"Lucida Grande\" }" + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px; width: 300px }" + "</style> </head>" + "<b>Are you sure you want to Quit?</b>" + "<p>Closing the last open sketch will quit Processing.";
                    int result = JOptionPane.showOptionDialog(editor, prompt, "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
                        return false;
                    }
                }
            }
            Preferences.unset("server.port");
            Preferences.unset("server.key");
            editors.remove(editor);
            Preferences.save();
            if (defaultFileMenu == null) {
                if (modeSwitch) {
                    editor.setVisible(false);
                    editor.dispose();
                    activeEditor = null;
                    editors.remove(editor);
                } else {
                    System.exit(0);
                }
            } else {
                editor.setVisible(false);
                editor.dispose();
                defaultFileMenu.insert(getRecentMenu(), 2);
                activeEditor = null;
                editors.remove(editor);
            }
        } else {
            editor.setVisible(false);
            editor.dispose();
            editors.remove(editor);
        }
        return true;
    }

    public boolean handleQuit() {
        if (handleQuitEach()) {
            for (Editor editor : editors) {
                editor.internalCloseRunner();
            }
            Preferences.save();
            if (!Base.isMacOS()) {
                System.exit(0);
            }
            return true;
        }
        return false;
    }

    protected boolean handleQuitEach() {
        for (Editor editor : editors) {
            if (!editor.checkModified()) {
                return false;
            }
        }
        return true;
    }

    protected void rebuildSketchbookMenusAsync() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                rebuildSketchbookMenus();
            }
        });
    }

    public void thinkDifferentExamples() {
        nextMode.showExamplesFrame();
    }

    protected void rebuildSketchbookMenus() {
        for (Mode mode : getModeList()) {
            mode.rebuildImportMenu();
            mode.rebuildToolbarMenu();
            mode.rebuildExamplesFrame();
            mode.rebuildSketchbookFrame();
        }
    }

    protected void rebuildSketchbookMenu() {
        sketchbookMenu.removeAll();
        populateSketchbookMenu(sketchbookMenu);
    }

    public void populateSketchbookMenu(JMenu menu) {
        boolean found = false;
        try {
            found = addSketches(menu, sketchbookFolder, false);
        } catch (IOException e) {
            Base.showWarning("Sketchbook Menu Error", "An error occurred while trying to list the sketchbook.", e);
        }
        if (!found) {
            JMenuItem empty = new JMenuItem(Language.text("menu.file.sketchbook.empty"));
            empty.setEnabled(false);
            menu.add(empty);
        }
    }

    public JMenu getRecentMenu() {
        return recent.getMenu();
    }

    public JMenu getToolbarRecentMenu() {
        return recent.getToolbarMenu();
    }

    public void handleRecent(Editor editor) {
        recent.handle(editor);
    }

    public void handleRecentRename(Editor editor, String oldPath) {
        recent.handleRename(editor, oldPath);
    }

    public void removeRecent(Editor editor) {
        recent.remove(editor);
    }

    protected boolean addSketches(JMenu menu, File folder, final boolean replaceExisting) throws IOException {
        if (!folder.isDirectory()) {
            return false;
        }
        if (folder.getName().equals("libraries")) {
            return false;
        }
        String[] list = folder.list();
        if (list == null) {
            return false;
        }
        Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String path = e.getActionCommand();
                if (new File(path).exists()) {
                    boolean replace = replaceExisting;
                    if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                        replace = !replace;
                    }
                    handleOpen(path);
                } else {
                    showWarning("Sketch Disappeared", "The selected sketch no longer exists.\n" + "You may need to restart Processing to update\n" + "the sketchbook menu.", null);
                }
            }
        };
        boolean found = false;
        for (String name : list) {
            if (name.charAt(0) == '.') {
                continue;
            }
            File subfolder = new File(folder, name);
            if (subfolder.isDirectory()) {
                File entry = checkSketchFolder(subfolder, name);
                if (entry != null) {
                    JMenuItem item = new JMenuItem(name);
                    item.addActionListener(listener);
                    item.setActionCommand(entry.getAbsolutePath());
                    menu.add(item);
                    found = true;
                } else {
                    JMenu submenu = new JMenu(name);
                    boolean anything = addSketches(submenu, subfolder, replaceExisting);
                    if (anything && !name.equals("old")) {
                        menu.add(submenu);
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    protected boolean addSketches(DefaultMutableTreeNode node, File folder) throws IOException {
        if (!folder.isDirectory()) {
            return false;
        }
        if (folder.getName().equals("libraries")) {
            return false;
        }
        String[] fileList = folder.list();
        if (fileList == null) {
            return false;
        }
        Arrays.sort(fileList, String.CASE_INSENSITIVE_ORDER);
        boolean found = false;
        for (String name : fileList) {
            if (name.charAt(0) == '.') {
                continue;
            }
            File subfolder = new File(folder, name);
            if (subfolder.isDirectory()) {
                File entry = checkSketchFolder(subfolder, name);
                if (entry != null) {
                    DefaultMutableTreeNode item = new DefaultMutableTreeNode(new SketchReference(name, entry));
                    node.add(item);
                    found = true;
                } else {
                    DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(name);
                    boolean anything = addSketches(subnode, subfolder);
                    if (anything) {
                        node.add(subnode);
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    File checkSketchFolder(File subfolder, String item) {
        for (Mode mode : getModeList()) {
            File entry = new File(subfolder, item + "." + mode.getDefaultExtension());
            if (entry.exists()) {
                return entry;
            }
        }
        return null;
    }

    public void handlePrefs() {
        if (preferencesFrame == null) {
            preferencesFrame = new PreferencesFrame(this);
        }
        preferencesFrame.showFrame();
    }

    public void handleOpenLibraryManager() {
        contributionManagerFrame.showFrame(activeEditor, ContributionType.LIBRARY);
    }

    public void handleOpenToolManager() {
        contributionManagerFrame.showFrame(activeEditor, ContributionType.TOOL);
    }

    public void handleOpenModeManager() {
        contributionManagerFrame.showFrame(activeEditor, ContributionType.MODE);
    }

    public void handleOpenExampleManager() {
        contributionManagerFrame.showFrame(activeEditor, ContributionType.EXAMPLES);
    }

    public void handleShowUpdates() {
        contributionManagerFrame.showFrame(activeEditor, null);
    }

    static public int getRevision() {
        return REVISION;
    }

    static public String getVersionName() {
        return VERSION_NAME;
    }

    static public Platform getPlatform() {
        return platform;
    }

    static public String getPlatformName() {
        return PConstants.platformNames[PApplet.platform];
    }

    static public int getNativeBits() {
        return nativeBits;
    }

    static public String getPlatformName(int which) {
        return platformNames.get(which);
    }

    static public int getPlatformIndex(String what) {
        Integer entry = platformIndices.get(what);
        return (entry == null) ? -1 : entry.intValue();
    }

    static public boolean isMacOS() {
        return System.getProperty("os.name").indexOf("Mac") != -1;
    }

    static public boolean isWindows() {
        return System.getProperty("os.name").indexOf("Windows") != -1;
    }

    static public boolean isLinux() {
        return System.getProperty("os.name").indexOf("Linux") != -1;
    }

    static public File getSettingsFolder() {
        File settingsFolder = null;
        try {
            settingsFolder = platform.getSettingsFolder();
        } catch (Exception e) {
            showError("Problem getting the settings folder", "Error getting the Processing the settings folder.", e);
        }
        if (!settingsFolder.exists()) {
            if (!settingsFolder.mkdirs()) {
                showError("Settings issues", "Processing cannot run because it could not\n" + "create a folder to store your settings.", null);
            }
        }
        return settingsFolder;
    }

    static public File getSettingsFile(String filename) {
        return new File(getSettingsFolder(), filename);
    }

    static public File createTempFolder(String prefix, String suffix, File directory) throws IOException {
        int fillChars = 3 - prefix.length();
        for (int i = 0; i < fillChars; i++) {
            prefix += '_';
        }
        File folder = File.createTempFile(prefix, suffix, directory);
        folder.delete();
        folder.mkdirs();
        return folder;
    }

    static public File getToolsFolder() {
        return getContentFile("tools");
    }

    static public void locateSketchbookFolder() {
        String sketchbookPath = Preferences.getSketchbookPath();
        if (sketchbookPath != null) {
            sketchbookFolder = new File(sketchbookPath);
            if (!sketchbookFolder.exists()) {
                Base.showWarning("Sketchbook folder disappeared", "The sketchbook folder no longer exists.\n" + "Processing will switch to the default sketchbook\n" + "location, and create a new sketchbook folder if\n" + "necessary. Processing will then stop talking\n" + "about himself in the third person.", null);
                sketchbookFolder = null;
            }
        }
        if (sketchbookFolder == null) {
            sketchbookFolder = getDefaultSketchbookFolder();
            Preferences.setSketchbookPath(sketchbookFolder.getAbsolutePath());
            if (!sketchbookFolder.exists()) {
                sketchbookFolder.mkdirs();
            }
        }
        makeSketchbookSubfolders();
    }

    public void setSketchbookFolder(File folder) {
        sketchbookFolder = folder;
        Preferences.setSketchbookPath(folder.getAbsolutePath());
        rebuildSketchbookMenus();
        makeSketchbookSubfolders();
    }

    static protected void makeSketchbookSubfolders() {
        getSketchbookLibrariesFolder().mkdirs();
        getSketchbookToolsFolder().mkdirs();
        getSketchbookModesFolder().mkdirs();
        getSketchbookExamplesFolder().mkdirs();
    }

    static public File getSketchbookFolder() {
        return sketchbookFolder;
    }

    static public File getSketchbookLibrariesFolder() {
        return new File(sketchbookFolder, "libraries");
    }

    static public File getSketchbookToolsFolder() {
        return new File(sketchbookFolder, "tools");
    }

    static public File getSketchbookModesFolder() {
        return new File(sketchbookFolder, "modes");
    }

    static public File getSketchbookExamplesFolder() {
        return new File(sketchbookFolder, "examples");
    }

    static protected File getDefaultSketchbookFolder() {
        File sketchbookFolder = null;
        try {
            sketchbookFolder = platform.getDefaultSketchbookFolder();
        } catch (Exception e) {
        }
        if (sketchbookFolder == null) {
            showError("No sketchbook", "Problem while trying to get the sketchbook", null);
        }
        boolean result = true;
        if (!sketchbookFolder.exists()) {
            result = sketchbookFolder.mkdirs();
        }
        if (!result) {
            showError("You forgot your sketchbook", "Processing cannot run because it could not\n" + "create a folder to store your sketchbook.", null);
        }
        return sketchbookFolder;
    }

    static public void openURL(String url) {
        try {
            platform.openURL(url);
        } catch (Exception e) {
            showWarning("Problem Opening URL", "Could not open the URL\n" + url, e);
        }
    }

    static public boolean openFolderAvailable() {
        return platform.openFolderAvailable();
    }

    static public void openFolder(File file) {
        try {
            platform.openFolder(file);
        } catch (Exception e) {
            showWarning("Problem Opening Folder", "Could not open the folder\n" + file.getAbsolutePath(), e);
        }
    }

    static public void showMessage(String title, String message) {
        if (title == null)
            title = "Message";
        if (commandLine) {
            System.out.println(title + ": " + message);
        } else {
            JOptionPane.showMessageDialog(new Frame(), message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    static public void showWarning(String title, String message) {
        showWarning(title, message, null);
    }

    static public void showWarning(String title, String message, Throwable e) {
        if (title == null)
            title = "Warning";
        if (commandLine) {
            System.out.println(title + ": " + message);
        } else {
            JOptionPane.showMessageDialog(new Frame(), message, title, JOptionPane.WARNING_MESSAGE);
        }
        if (e != null)
            e.printStackTrace();
    }

    static public void showWarningTiered(String title, String primary, String secondary, Throwable e) {
        if (title == null)
            title = "Warning";
        final String message = primary + "\n" + secondary;
        if (commandLine) {
            System.out.println(title + ": " + message);
        } else {
            if (!Base.isMacOS()) {
                JOptionPane.showMessageDialog(new JFrame(), "<html><body>" + "<b>" + primary + "</b>" + "<br>" + secondary, title, JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane pane = new JOptionPane("<html> " + "<head> <style type=\"text/css\">" + "b { font: 13pt \"Lucida Grande\" }" + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px; width: 300px }" + "</style> </head>" + "<b>" + primary + "</b>" + "<p>" + secondary + "</p>", JOptionPane.WARNING_MESSAGE);
                JDialog dialog = pane.createDialog(new JFrame(), null);
                dialog.setVisible(true);
            }
        }
        if (e != null)
            e.printStackTrace();
    }

    static public void showError(String title, String message, Throwable e) {
        if (title == null)
            title = "Error";
        if (commandLine) {
            System.err.println(title + ": " + message);
        } else {
            JOptionPane.showMessageDialog(new Frame(), message, title, JOptionPane.ERROR_MESSAGE);
        }
        if (e != null)
            e.printStackTrace();
        System.exit(1);
    }

    static private void showBadnessTrace(String title, String message, Throwable t, boolean fatal) {
        if (title == null)
            title = fatal ? "Error" : "Warning";
        if (commandLine) {
            System.err.println(title + ": " + message);
            if (t != null) {
                t.printStackTrace();
            }
        } else {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            message = ("<html>" + message + "<br/><font size=2><br/>" + sw + "</html>").replaceAll("\n", "<br/>");
            JOptionPane.showMessageDialog(new Frame(), message, title, fatal ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
            if (fatal) {
                System.exit(1);
            }
        }
    }

    static public int showYesNoCancelQuestion(Editor editor, String title, String primary, String secondary) {
        if (!Base.isMacOS()) {
            int result = JOptionPane.showConfirmDialog(null, primary + "\n" + secondary, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            return result;
        } else {
            JOptionPane pane = new JOptionPane("<html> " + "<head> <style type=\"text/css\">" + "b { font: 13pt \"Lucida Grande\" }" + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px; width: 300px }" + "</style> </head>" + "<b>" + Language.text("save.title") + "</b>" + "<p>" + Language.text("save.hint") + "</p>", JOptionPane.QUESTION_MESSAGE);
            String[] options = new String[] { Language.text("save.btn.save"), Language.text("prompt.cancel"), Language.text("save.btn.dont_save") };
            pane.setOptions(options);
            pane.setInitialValue(options[0]);
            pane.putClientProperty("Quaqua.OptionPane.destructiveOption", Integer.valueOf(2));
            JDialog dialog = pane.createDialog(editor, null);
            dialog.setVisible(true);
            Object result = pane.getValue();
            if (result == options[0]) {
                return JOptionPane.YES_OPTION;
            } else if (result == options[1]) {
                return JOptionPane.CANCEL_OPTION;
            } else if (result == options[2]) {
                return JOptionPane.NO_OPTION;
            } else {
                return JOptionPane.CLOSED_OPTION;
            }
        }
    }

    static public int showYesNoQuestion(Frame editor, String title, String primary, String secondary) {
        if (!Base.isMacOS()) {
            return JOptionPane.showConfirmDialog(editor, "<html><body>" + "<b>" + primary + "</b>" + "<br>" + secondary, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else {
            JOptionPane pane = new JOptionPane("<html> " + "<head> <style type=\"text/css\">" + "b { font: 13pt \"Lucida Grande\" }" + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px; width: 300px }" + "</style> </head>" + "<b>" + primary + "</b>" + "<p>" + secondary + "</p>", JOptionPane.QUESTION_MESSAGE);
            String[] options = new String[] { "Yes", "No" };
            pane.setOptions(options);
            pane.setInitialValue(options[0]);
            JDialog dialog = pane.createDialog(editor, null);
            dialog.setVisible(true);
            Object result = pane.getValue();
            if (result == options[0]) {
                return JOptionPane.YES_OPTION;
            } else if (result == options[1]) {
                return JOptionPane.NO_OPTION;
            } else {
                return JOptionPane.CLOSED_OPTION;
            }
        }
    }

    static protected File processingRoot;

    static public File getContentFile(String name) {
        if (processingRoot == null) {
            String path = Base.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = PApplet.urlDecode(path);
            if (decodedPath.contains("/app/bin")) {
                if (Base.isMacOS()) {
                    processingRoot = new File(path, "../../build/macosx/work/Processing.app/Contents/Java");
                } else if (Base.isWindows()) {
                    processingRoot = new File(path, "../../build/windows/work");
                } else if (Base.isLinux()) {
                    processingRoot = new File(path, "../../build/linux/work");
                }
            } else {
                File jarFolder = new File(decodedPath).getParentFile();
                if (jarFolder.getName().equals("lib")) {
                    processingRoot = jarFolder.getParentFile();
                } else if (Base.isMacOS()) {
                    processingRoot = jarFolder;
                }
                if (processingRoot == null || !processingRoot.exists()) {
                    System.err.println("Could not find lib folder via " + jarFolder.getAbsolutePath() + ", switching to user.dir");
                    processingRoot = new File(System.getProperty("user.dir"));
                }
            }
        }
        return new File(processingRoot, name);
    }

    static public File getJavaHome() {
        if (isMacOS()) {
            File[] plugins = getContentFile("../PlugIns").listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return dir.isDirectory() && name.endsWith(".jdk") && !name.startsWith(".");
                }
            });
            return new File(plugins[0], "Contents/Home/jre");
        }
        return getContentFile("java");
    }

    static public String getJavaPath() {
        String javaPath = "bin/java" + (isWindows() ? ".exe" : "");
        File javaFile = new File(getJavaHome(), javaPath);
        try {
            return javaFile.getCanonicalPath();
        } catch (IOException e) {
            return javaFile.getAbsolutePath();
        }
    }

    static public File getLibFile(String filename) throws IOException {
        return new File(getContentFile("lib"), filename);
    }

    static public InputStream getLibStream(String filename) throws IOException {
        return new FileInputStream(getLibFile(filename));
    }

    static public void log(Object from, String message) {
        if (DEBUG) {
            System.out.println(from.getClass().getName() + ": " + message);
        }
    }

    static public void log(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    static public void logf(String message, Object... args) {
        if (DEBUG) {
            System.out.println(String.format(message, args));
        }
    }

    static public void loge(String message, Throwable e) {
        if (DEBUG) {
            System.err.println(message);
            e.printStackTrace();
        }
    }

    static public void loge(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}
