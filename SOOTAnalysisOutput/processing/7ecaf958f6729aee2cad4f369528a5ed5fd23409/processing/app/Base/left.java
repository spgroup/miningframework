package processing.app;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.tree.*;
import processing.app.contrib.*;
import processing.core.*;

public class Base {

    static private final int REVISION = 221;

    static private String VERSION_NAME = "0221";

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

    Preferences preferencesFrame;

    ContributionManagerDialog libraryManagerFrame;

    ContributionManagerDialog toolManagerFrame;

    ContributionManagerDialog modeManagerFrame;

    ContributionManagerDialog updateManagerFrame;

    boolean builtOnce;

    static File untitledFolder;

    protected List<Editor> editors = Collections.synchronizedList(new ArrayList<Editor>());

    protected Editor activeEditor;

    static public JMenu defaultFileMenu;

    private Mode nextMode;

    private Mode[] coreModes;

    protected ArrayList<ModeContribution> modeContribs;

    private JMenu sketchbookMenu;

    private Recent recent;

    static protected File sketchbookFolder;

    static public void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(args);
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
        initRequirements();
        Language.init();
        Preferences.init();
        locateSketchbookFolder();
        if (!SingleInstance.alreadyRunning(args)) {
            try {
                platform.setLookAndFeel();
            } catch (Exception e) {
                log("Could not set the Look & Feel", e);
            }
            try {
                untitledFolder = Base.createTempFolder("untitled", "sketches", null);
                untitledFolder.deleteOnExit();
            } catch (IOException e) {
                Base.showError("Trouble without a name", "Could not create a place to store untitled sketches.\n" + "That's gonna prevent us from continuing.", e);
            }
            log("about to create base...");
            try {
                Base base = new Base(args);
                SingleInstance.startServer(base);
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Base.showError("We're off on the wrong foot", "An error occurred during startup.\n" + sw, e);
            }
            log("done creating base...");
        }
    }

    public static void setCommandLine() {
        commandLine = true;
    }

    static protected boolean isCommandLine() {
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

    public static void initRequirements() {
        try {
            Class.forName("com.sun.jdi.VirtualMachine");
        } catch (ClassNotFoundException cnfe) {
            Base.openURL("http://wiki.processing.org/w/Supported_Platforms");
            Base.showError("Missing required files", "Processing requires a JRE with tools.jar (or a\n" + "full JDK) installed in (or linked to) a folder\n" + "named â€œjavaâ€? next to the Processing application.\n" + "More information can be found on the Wiki.", cnfe);
        }
    }

    private void buildCoreModes() {
        Mode javaMode = ModeContribution.load(this, getContentFile("modes/java"), "processing.mode.java.JavaMode").getMode();
        coreModes = new Mode[] { javaMode };
        ModeContribution experimentalContrib = ModeContribution.load(this, getContentFile("modes/experimental"), "processing.mode.experimental.ExperimentalMode");
        if (experimentalContrib != null) {
            Mode experimentalMode = experimentalContrib.getMode();
            coreModes = new Mode[] { javaMode, experimentalMode };
        }
    }

    void rebuildContribModes() {
        if (modeContribs == null) {
            modeContribs = new ArrayList<ModeContribution>();
        }
        ModeContribution.loadMissing(this);
    }

    public Base(String[] args) throws Exception {
        ContributionManager.cleanup();
        buildCoreModes();
        rebuildContribModes();
        recent = new Recent(this);
        String lastModeIdentifier = Preferences.get("last.sketch.mode");
        if (lastModeIdentifier == null) {
            nextMode = coreModes[0];
            log("Nothing set for last.sketch.mode, using coreMode[0].");
        } else {
            for (Mode m : getModeList()) {
                if (m.getIdentifier().equals(lastModeIdentifier)) {
                    logf("Setting next mode to {0}.", lastModeIdentifier);
                    nextMode = m;
                }
            }
            if (nextMode == null) {
                nextMode = coreModes[0];
                logf("Could not find mode {0}, using default.", lastModeIdentifier);
            }
        }
        libraryManagerFrame = new ContributionManagerDialog(ContributionType.LIBRARY);
        toolManagerFrame = new ContributionManagerDialog(ContributionType.TOOL);
        modeManagerFrame = new ContributionManagerDialog(ContributionType.MODE);
        updateManagerFrame = new ContributionManagerDialog(null);
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
        if (Preferences.getBoolean("update.check")) {
            new UpdateCheck(this);
        }
    }

    public Editor getActiveEditor() {
        return activeEditor;
    }

    public List<Editor> getEditors() {
        return editors;
    }

    protected void changeMode(Mode mode) {
        if (activeEditor.getMode() != mode) {
            Sketch sketch = activeEditor.getSketch();
            if (sketch.isModified()) {
                Base.showWarning("Save", "Please save the sketch before changing the mode.", null);
            } else {
                String mainPath = sketch.getMainFilePath();
                boolean wasUntitled = sketch.isUntitled();
                File sketchProps = new File(sketch.getFolder(), "sketch.properties");
                try {
                    Settings props = new Settings(sketchProps);
                    props.set("mode", mode.getTitle());
                    props.set("mode.id", mode.getIdentifier());
                    props.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EditorState state = activeEditor.state;
                handleClose(activeEditor, true);
                handleOpen(mainPath, wasUntitled, state);
            }
        }
    }

    public ArrayList<ModeContribution> getModeContribs() {
        return modeContribs;
    }

    public ArrayList<Mode> getModeList() {
        ArrayList<Mode> allModes = new ArrayList<Mode>();
        allModes.addAll(Arrays.asList(coreModes));
        if (modeContribs != null) {
            for (ModeContribution contrib : modeContribs) {
                allModes.add(contrib.getMode());
            }
        }
        return allModes;
    }

    protected void handleActivated(Editor whichEditor) {
        activeEditor = whichEditor;
        EditorConsole.setEditor(activeEditor);
        nextMode = whichEditor.getMode();
        Preferences.set("last.sketch.mode", nextMode.getIdentifier());
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
            String path = newbieFile.getAbsolutePath();
            handleOpen(path, true);
        } catch (IOException e) {
            Base.showWarning("That's new to me", "A strange and unexplainable error occurred\n" + "while trying to create a new sketch.", e);
        }
    }

    public void handleOpenPrompt() {
        final ArrayList<String> extensions = new ArrayList<String>();
        for (Mode mode : getModeList()) {
            extensions.add(mode.getDefaultExtension());
        }
        final String prompt = Language.text("open");
        if (Preferences.getBoolean("chooser.files.native")) {
            FileDialog fd = new FileDialog(activeEditor, prompt, FileDialog.LOAD);
            fd.setFilenameFilter(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    for (String ext : extensions) {
                        if (name.toLowerCase().endsWith("." + ext)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            fd.setVisible(true);
            String directory = fd.getDirectory();
            String filename = fd.getFile();
            if (filename != null) {
                File inputFile = new File(directory, filename);
                handleOpen(inputFile.getAbsolutePath());
            }
        } else {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(prompt);
            fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

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
            if (fc.showOpenDialog(activeEditor) == JFileChooser.APPROVE_OPTION) {
                handleOpen(fc.getSelectedFile().getAbsolutePath());
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
        File file = new File(path);
        if (!file.exists())
            return null;
        if (!Sketch.isSanitaryName(file.getName())) {
            Base.showWarning("You're tricky, but not tricky enough", file.getName() + " is not a valid name for a sketch.\n" + "Better to stick to ASCII, no spaces, and make sure\n" + "it doesn't start with a number.", null);
            return null;
        }
        for (Editor editor : editors) {
            if (editor.getSketch().getMainFilePath().equals(path)) {
                editor.toFront();
                handleRecent(editor);
                return editor;
            }
        }
        try {
            File sketchFolder = new File(path).getParentFile();
            File sketchProps = new File(sketchFolder, "sketch.properties");
            if (sketchProps.exists()) {
                Settings props = new Settings(sketchProps);
                String modeTitle = props.get("mode");
                String modeIdentifier = props.get("mode.id");
                if (modeTitle != null && modeIdentifier != null) {
                    Mode mode = findMode(modeIdentifier);
                    if (mode != null) {
                        nextMode = mode;
                    } else {
                        final String msg = "This sketch was last used in â€œ" + modeTitle + "â€? mode,\n" + "which does not appear to be installed. The sketch will\n" + "be opened in â€œ" + nextMode.getTitle() + "â€? mode instead.";
                        Base.showWarning("Depeche Mode", msg, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Editor editor = nextMode.createEditor(this, path, state);
        if (editor == null) {
            if (nextMode == coreModes[0]) {
                Base.showError("Editor Problems", "An error occurred while trying to change modes.\n" + "We'll have to quit for now because it's an\n" + "unfortunate bit of indigestion.", null);
            } else {
                editor = coreModes[0].createEditor(this, path, state);
            }
        }
        if (editor.getSketch() == null) {
            return null;
        }
        editor.getSketch().setUntitled(untitled);
        editors.add(editor);
        handleRecent(editor);
        editor.setVisible(true);
        return editor;
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
                    Object[] options = { "OK", "Cancel" };
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
                defaultFileMenu.insert(sketchbookMenu, 2);
                defaultFileMenu.insert(getRecentMenu(), 3);
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
        rebuildSketchbookMenu();
        for (Mode mode : getModeList()) {
            mode.rebuildImportMenu();
            mode.rebuildToolbarMenu();
            mode.resetExamples();
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
            JMenuItem empty = new JMenuItem("Empty Sketchbook");
            empty.setEnabled(false);
            menu.add(empty);
        }
    }

    public JMenu getSketchbookMenu() {
        if (sketchbookMenu == null) {
            sketchbookMenu = new JMenu("Sketchbook");
            rebuildSketchbookMenu();
        }
        return sketchbookMenu;
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
                    if (anything) {
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
        String[] list = folder.list();
        if (list == null) {
            return false;
        }
        Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
        boolean found = false;
        for (String name : list) {
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
            preferencesFrame = new Preferences(this);
        }
        preferencesFrame.showFrame();
    }

    public void handleOpenLibraryManager() {
        libraryManagerFrame.showFrame(activeEditor);
    }

    public void handleOpenToolManager() {
        toolManagerFrame.showFrame(activeEditor);
    }

    public void handleOpenModeManager() {
        modeManagerFrame.showFrame(activeEditor);
    }

    public void handleShowUpdates() {
        updateManagerFrame.showFrame(activeEditor);
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
        if (Base.isMacOS()) {
            return Preferences.getInteger("run.options.bits");
        }
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

    static private Boolean usableOracleJava;

    static public boolean isUsableOracleJava() {
        if (usableOracleJava == null) {
            usableOracleJava = false;
            if (Base.isMacOS() && System.getProperty("java.vendor").contains("Oracle")) {
                String version = System.getProperty("java.version");
                String[] m = PApplet.match(version, "1.(\\d).*_(\\d+)");
                if (m != null && PApplet.parseInt(m[1]) >= 7 && PApplet.parseInt(m[2]) >= 40) {
                    usableOracleJava = true;
                }
            }
        }
        return usableOracleJava;
    }

    static public boolean isWindows() {
        return System.getProperty("os.name").indexOf("Windows") != -1;
    }

    static public boolean isLinux() {
        return System.getProperty("os.name").indexOf("Linux") != -1;
    }

    static public File getSettingsFolder() {
        File settingsFolder = null;
        String preferencesPath = Preferences.get("settings.path");
        if (preferencesPath != null) {
            settingsFolder = new File(preferencesPath);
        } else {
            try {
                settingsFolder = platform.getSettingsFolder();
            } catch (Exception e) {
                showError("Problem getting data folder", "Error getting the Processing data folder.", e);
            }
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
        String sketchbookPath = Preferences.get("sketchbook.path");
        if (sketchbookPath != null) {
            sketchbookFolder = new File(sketchbookPath);
            if (!sketchbookFolder.exists()) {
                Base.showWarning("Sketchbook folder disappeared", "The sketchbook folder no longer exists.\n" + "Processing will switch to the default sketchbook\n" + "location, and create a new sketchbook folder if\n" + "necessary. Processing will then stop talking\n" + "about himself in the third person.", null);
                sketchbookFolder = null;
            }
        }
        if (sketchbookFolder == null) {
            sketchbookFolder = getDefaultSketchbookFolder();
            Preferences.set("sketchbook.path", sketchbookFolder.getAbsolutePath());
            if (!sketchbookFolder.exists()) {
                sketchbookFolder.mkdirs();
            }
        }
        getSketchbookLibrariesFolder().mkdir();
        getSketchbookToolsFolder().mkdir();
        getSketchbookModesFolder().mkdir();
    }

    public void setSketchbookFolder(File folder) {
        sketchbookFolder = folder;
        Preferences.set("sketchbook.path", folder.getAbsolutePath());
        rebuildSketchbookMenus();
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

    static protected boolean openFolderAvailable() {
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

    static public void showWarning(String title, String message, Exception e) {
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

    static public void showWarningTiered(String title, String primary, String secondary, Exception e) {
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

    static public int showYesNoCancelQuestion(Editor editor, String title, String primary, String secondary) {
        if (!Base.isMacOS()) {
            int result = JOptionPane.showConfirmDialog(null, primary + "\n" + secondary, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            return result;
        } else {
            JOptionPane pane = new JOptionPane("<html> " + "<head> <style type=\"text/css\">" + "b { font: 13pt \"Lucida Grande\" }" + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px; width: 300px }" + "</style> </head>" + "<b>" + Language.text("save.title") + "</b>" + "<p>" + Language.text("save.hint") + "</p>", JOptionPane.QUESTION_MESSAGE);
            String[] options = new String[] { Language.text("save.btn.save"), Language.text("prompt.cancel"), Language.text("save.btn.dont_save") };
            pane.setOptions(options);
            pane.setInitialValue(options[0]);
            pane.putClientProperty("Quaqua.OptionPane.destructiveOption", new Integer(2));
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
            File jarFolder = new File(decodedPath).getParentFile();
            if (jarFolder.getName().equals("lib")) {
                processingRoot = jarFolder.getParentFile();
            } else if (Base.isMacOS()) {
                processingRoot = jarFolder;
            }
            if (processingRoot == null || !processingRoot.exists()) {
                Base.log("Could not find lib folder via " + jarFolder.getAbsolutePath() + ", switching to user.dir");
                processingRoot = new File(System.getProperty("user.dir"));
            }
        }
        return new File(processingRoot, name);
    }

    static public InputStream getLibStream(String filename) throws IOException {
        return new FileInputStream(new File(getContentFile("lib"), filename));
    }

    static public int countLines(String what) {
        int count = 1;
        for (char c : what.toCharArray()) {
            if (c == '\n')
                count++;
        }
        return count;
    }

    static public byte[] loadBytesRaw(File file) throws IOException {
        int size = (int) file.length();
        FileInputStream input = new FileInputStream(file);
        byte[] buffer = new byte[size];
        int offset = 0;
        int bytesRead;
        while ((bytesRead = input.read(buffer, offset, size - offset)) != -1) {
            offset += bytesRead;
            if (bytesRead == 0)
                break;
        }
        input.close();
        input = null;
        return buffer;
    }

    static public HashMap<String, String> readSettings(File inputFile) {
        HashMap<String, String> outgoing = new HashMap<String, String>();
        if (inputFile.exists()) {
            String[] lines = PApplet.loadStrings(inputFile);
            readSettings(inputFile.toString(), lines, outgoing);
        }
        return outgoing;
    }

    static public void readSettings(String filename, String[] lines, HashMap<String, String> settings) {
        for (String line : lines) {
            int commentMarker = line.indexOf('#');
            if (commentMarker != -1) {
                line = line.substring(0, commentMarker);
            }
            line = line.trim();
            if (line.length() != 0) {
                int equals = line.indexOf('=');
                if (equals == -1) {
                    if (filename != null) {
                        System.err.println("Ignoring illegal line in " + filename);
                        System.err.println("  " + line);
                    }
                } else {
                    String attr = line.substring(0, equals).trim();
                    String valu = line.substring(equals + 1).trim();
                    settings.put(attr, valu);
                }
            }
        }
    }

    static public void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream from = new BufferedInputStream(new FileInputStream(sourceFile));
        BufferedOutputStream to = new BufferedOutputStream(new FileOutputStream(targetFile));
        byte[] buffer = new byte[16 * 1024];
        int bytesRead;
        while ((bytesRead = from.read(buffer)) != -1) {
            to.write(buffer, 0, bytesRead);
        }
        from.close();
        from = null;
        to.flush();
        to.close();
        to = null;
        targetFile.setLastModified(sourceFile.lastModified());
    }

    static public String loadFile(File file) throws IOException {
        String[] contents = PApplet.loadStrings(file);
        if (contents == null)
            return null;
        return PApplet.join(contents, "\n");
    }

    static public void saveFile(String str, File file) throws IOException {
        File temp = File.createTempFile(file.getName(), null, file.getParentFile());
        try {
            File canon = file.getCanonicalFile();
            file = canon;
        } catch (IOException e) {
            throw new IOException("Could not resolve canonical representation of " + file.getAbsolutePath());
        }
        PApplet.saveStrings(temp, new String[] { str });
        if (file.exists()) {
            boolean result = file.delete();
            if (!result) {
                throw new IOException("Could not remove old version of " + file.getAbsolutePath());
            }
        }
        boolean result = temp.renameTo(file);
        if (!result) {
            throw new IOException("Could not replace " + file.getAbsolutePath());
        }
    }

    static public void copyDir(File sourceDir, File targetDir) throws IOException {
        if (sourceDir.equals(targetDir)) {
            final String urDum = "source and target directories are identical";
            throw new IllegalArgumentException(urDum);
        }
        targetDir.mkdirs();
        String[] files = sourceDir.list();
        for (int i = 0; i < files.length; i++) {
            if (files[i].charAt(0) == '.')
                continue;
            File source = new File(sourceDir, files[i]);
            File target = new File(targetDir, files[i]);
            if (source.isDirectory()) {
                copyDir(source, target);
                target.setLastModified(source.lastModified());
            } else {
                copyFile(source, target);
            }
        }
    }

    static public boolean platformDelete(File file) throws IOException {
        return platform.deleteFile(file);
    }

    static public void removeDir(File dir) {
        if (dir.exists()) {
            removeDescendants(dir);
            if (!dir.delete()) {
                System.err.println("Could not delete " + dir);
            }
        }
    }

    static public void removeDescendants(File dir) {
        if (!dir.exists())
            return;
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(".") || files[i].equals(".."))
                continue;
            File dead = new File(dir, files[i]);
            if (!dead.isDirectory()) {
                if (!Preferences.getBoolean("compiler.save_build_files")) {
                    if (!dead.delete()) {
                        System.err.println("Could not delete " + dead);
                    }
                }
            } else {
                removeDir(dead);
            }
        }
    }

    static public int calcFolderSize(File folder) {
        int size = 0;
        String[] files = folder.list();
        if (files == null)
            return -1;
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(".") || files[i].equals("..") || files[i].equals(".DS_Store"))
                continue;
            File fella = new File(folder, files[i]);
            if (fella.isDirectory()) {
                size += calcFolderSize(fella);
            } else {
                size += (int) fella.length();
            }
        }
        return size;
    }

    static public String[] listFiles(File folder, boolean relative) {
        String path = folder.getAbsolutePath();
        Vector<String> vector = new Vector<String>();
        listFiles(relative ? (path + File.separator) : "", path, null, vector);
        String[] outgoing = new String[vector.size()];
        vector.copyInto(outgoing);
        return outgoing;
    }

    static public String[] listFiles(File folder, boolean relative, String extension) {
        String path = folder.getAbsolutePath();
        Vector<String> vector = new Vector<String>();
        if (extension != null) {
            if (!extension.startsWith(".")) {
                extension = "." + extension;
            }
        }
        listFiles(relative ? (path + File.separator) : "", path, extension, vector);
        String[] outgoing = new String[vector.size()];
        vector.copyInto(outgoing);
        return outgoing;
    }

    static protected void listFiles(String basePath, String path, String extension, Vector<String> vector) {
        File folder = new File(path);
        String[] list = folder.list();
        if (list != null) {
            for (String item : list) {
                if (item.charAt(0) == '.')
                    continue;
                if (extension == null || item.toLowerCase().endsWith(extension)) {
                    File file = new File(path, item);
                    String newPath = file.getAbsolutePath();
                    if (newPath.startsWith(basePath)) {
                        newPath = newPath.substring(basePath.length());
                    }
                    if (extension == null || item.toLowerCase().endsWith(extension)) {
                        vector.add(newPath);
                    }
                    if (file.isDirectory()) {
                        listFiles(basePath, file.getAbsolutePath(), extension, vector);
                    }
                }
            }
        }
    }

    static public File[] listJarFiles(File folder) {
        return folder.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return (!name.startsWith(".") && (name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".zip")));
            }
        });
    }

    static public String contentsToClassPath(File folder) {
        if (folder == null)
            return "";
        StringBuffer abuffer = new StringBuffer();
        String sep = System.getProperty("path.separator");
        try {
            String path = folder.getCanonicalPath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            String[] list = folder.list();
            for (int i = 0; i < list.length; i++) {
                if (list[i].startsWith("."))
                    continue;
                if (list[i].toLowerCase().endsWith(".jar") || list[i].toLowerCase().endsWith(".zip")) {
                    abuffer.append(sep);
                    abuffer.append(path);
                    abuffer.append(list[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return abuffer.toString();
    }

    static public String[] packageListFromClassPath(String path) {
        Hashtable table = new Hashtable();
        String[] pieces = PApplet.split(path, File.pathSeparatorChar);
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i].length() == 0)
                continue;
            if (pieces[i].toLowerCase().endsWith(".jar") || pieces[i].toLowerCase().endsWith(".zip")) {
                packageListFromZip(pieces[i], table);
            } else {
                File dir = new File(pieces[i]);
                if (dir.exists() && dir.isDirectory()) {
                    packageListFromFolder(dir, null, table);
                }
            }
        }
        int tableCount = table.size();
        String[] output = new String[tableCount];
        int index = 0;
        Enumeration e = table.keys();
        while (e.hasMoreElements()) {
            output[index++] = ((String) e.nextElement()).replace('/', '.');
        }
        return output;
    }

    static private void packageListFromZip(String filename, Hashtable table) {
        try {
            ZipFile file = new ZipFile(filename);
            Enumeration entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        int slash = name.lastIndexOf('/');
                        if (slash == -1)
                            continue;
                        String pname = name.substring(0, slash);
                        if (table.get(pname) == null) {
                            table.put(pname, new Object());
                        }
                    }
                }
            }
            file.close();
        } catch (IOException e) {
            System.err.println("Ignoring " + filename + " (" + e.getMessage() + ")");
        }
    }

    static private void packageListFromFolder(File dir, String sofar, Hashtable table) {
        boolean foundClass = false;
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(".") || files[i].equals(".."))
                continue;
            File sub = new File(dir, files[i]);
            if (sub.isDirectory()) {
                String nowfar = (sofar == null) ? files[i] : (sofar + "." + files[i]);
                packageListFromFolder(sub, nowfar, table);
            } else if (!foundClass) {
                if (files[i].endsWith(".class")) {
                    table.put(sofar, new Object());
                    foundClass = true;
                }
            }
        }
    }

    static public void unzip(File zipFile, File dest) {
        try {
            FileInputStream fis = new FileInputStream(zipFile);
            CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
            ZipEntry next = null;
            while ((next = zis.getNextEntry()) != null) {
                File currentFile = new File(dest, next.getName());
                if (next.isDirectory()) {
                    currentFile.mkdirs();
                } else {
                    File parentDir = currentFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    currentFile.createNewFile();
                    unzipEntry(zis, currentFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static protected void unzipEntry(ZipInputStream zin, File f) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        byte[] b = new byte[512];
        int len = 0;
        while ((len = zin.read(b)) != -1) {
            out.write(b, 0, len);
        }
        out.flush();
        out.close();
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

    static public void log(String message, Exception e) {
        if (DEBUG) {
            System.out.println(message);
            e.printStackTrace();
        }
    }
}
