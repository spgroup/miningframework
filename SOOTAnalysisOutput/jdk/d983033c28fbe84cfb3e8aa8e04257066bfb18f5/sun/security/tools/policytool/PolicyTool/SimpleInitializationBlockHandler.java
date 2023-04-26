package sun.security.tools.policytool;

import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.*;
import java.text.Collator;
import java.text.MessageFormat;
import sun.security.util.PropertyExpander;
import sun.security.util.PropertyExpander.ExpandException;
import java.awt.*;
import java.awt.event.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.*;
import sun.security.provider.*;
import sun.security.util.PolicyUtil;
import javax.security.auth.x500.X500Principal;

public class PolicyTool {

    static final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("sun.security.tools.policytool.Resources");

    static final Collator collator = Collator.getInstance();

    static {
        collator.setStrength(Collator.PRIMARY);
    }

    Vector<String> warnings;

    boolean newWarning = false;

    boolean modified = false;

    private static final boolean testing = false;

    private static final Class[] TWOPARAMS = { String.class, String.class };

    private static final Class[] ONEPARAMS = { String.class };

    private static final Class[] NOPARAMS = {};

    private static String policyFileName = null;

    private Vector<PolicyEntry> policyEntries = null;

    private PolicyParser parser = null;

    private KeyStore keyStore = null;

    private String keyStoreName = " ";

    private String keyStoreType = " ";

    private String keyStoreProvider = " ";

    private String keyStorePwdURL = " ";

    private static final String P11KEYSTORE = "PKCS11";

    private static final String NONE = "NONE";

    private PolicyTool() {
        policyEntries = new Vector<PolicyEntry>();
        parser = new PolicyParser();
        warnings = new Vector<String>();
    }

    String getPolicyFileName() {
        return policyFileName;
    }

    void setPolicyFileName(String policyFileName) {
        PolicyTool.policyFileName = policyFileName;
    }

    void clearKeyStoreInfo() {
        this.keyStoreName = null;
        this.keyStoreType = null;
        this.keyStoreProvider = null;
        this.keyStorePwdURL = null;
        this.keyStore = null;
    }

    String getKeyStoreName() {
        return keyStoreName;
    }

    String getKeyStoreType() {
        return keyStoreType;
    }

    String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    String getKeyStorePwdURL() {
        return keyStorePwdURL;
    }

    void openPolicy(String filename) throws FileNotFoundException, PolicyParser.ParsingException, KeyStoreException, CertificateException, InstantiationException, MalformedURLException, IOException, NoSuchAlgorithmException, IllegalAccessException, NoSuchMethodException, UnrecoverableKeyException, NoSuchProviderException, ClassNotFoundException, PropertyExpander.ExpandException, InvocationTargetException {
        newWarning = false;
        policyEntries = new Vector<PolicyEntry>();
        parser = new PolicyParser();
        warnings = new Vector<String>();
        setPolicyFileName(null);
        clearKeyStoreInfo();
        if (filename == null) {
            modified = false;
            return;
        }
        setPolicyFileName(filename);
        parser.read(new FileReader(filename));
        openKeyStore(parser.getKeyStoreUrl(), parser.getKeyStoreType(), parser.getKeyStoreProvider(), parser.getStorePassURL());
        Enumeration<PolicyParser.GrantEntry> enum_ = parser.grantElements();
        while (enum_.hasMoreElements()) {
            PolicyParser.GrantEntry ge = enum_.nextElement();
            if (ge.signedBy != null) {
                String[] signers = parseSigners(ge.signedBy);
                for (int i = 0; i < signers.length; i++) {
                    PublicKey pubKey = getPublicKeyAlias(signers[i]);
                    if (pubKey == null) {
                        newWarning = true;
                        MessageFormat form = new MessageFormat(rb.getString("Warning.A.public.key.for.alias.signers.i.does.not.exist.Make.sure.a.KeyStore.is.properly.configured."));
                        Object[] source = { signers[i] };
                        warnings.addElement(form.format(source));
                    }
                }
            }
            ListIterator<PolicyParser.PrincipalEntry> prinList = ge.principals.listIterator(0);
            while (prinList.hasNext()) {
                PolicyParser.PrincipalEntry pe = prinList.next();
                try {
                    verifyPrincipal(pe.getPrincipalClass(), pe.getPrincipalName());
                } catch (ClassNotFoundException fnfe) {
                    newWarning = true;
                    MessageFormat form = new MessageFormat(rb.getString("Warning.Class.not.found.class"));
                    Object[] source = { pe.getPrincipalClass() };
                    warnings.addElement(form.format(source));
                }
            }
            Enumeration<PolicyParser.PermissionEntry> perms = ge.permissionElements();
            while (perms.hasMoreElements()) {
                PolicyParser.PermissionEntry pe = perms.nextElement();
                try {
                    verifyPermission(pe.permission, pe.name, pe.action);
                } catch (ClassNotFoundException fnfe) {
                    newWarning = true;
                    MessageFormat form = new MessageFormat(rb.getString("Warning.Class.not.found.class"));
                    Object[] source = { pe.permission };
                    warnings.addElement(form.format(source));
                } catch (InvocationTargetException ite) {
                    newWarning = true;
                    MessageFormat form = new MessageFormat(rb.getString("Warning.Invalid.argument.s.for.constructor.arg"));
                    Object[] source = { pe.permission };
                    warnings.addElement(form.format(source));
                }
                if (pe.signedBy != null) {
                    String[] signers = parseSigners(pe.signedBy);
                    for (int i = 0; i < signers.length; i++) {
                        PublicKey pubKey = getPublicKeyAlias(signers[i]);
                        if (pubKey == null) {
                            newWarning = true;
                            MessageFormat form = new MessageFormat(rb.getString("Warning.A.public.key.for.alias.signers.i.does.not.exist.Make.sure.a.KeyStore.is.properly.configured."));
                            Object[] source = { signers[i] };
                            warnings.addElement(form.format(source));
                        }
                    }
                }
            }
            PolicyEntry pEntry = new PolicyEntry(this, ge);
            policyEntries.addElement(pEntry);
        }
        modified = false;
    }

    void savePolicy(String filename) throws FileNotFoundException, IOException {
        parser.setKeyStoreUrl(keyStoreName);
        parser.setKeyStoreType(keyStoreType);
        parser.setKeyStoreProvider(keyStoreProvider);
        parser.setStorePassURL(keyStorePwdURL);
        parser.write(new FileWriter(filename));
        modified = false;
    }

    void openKeyStore(String name, String type, String provider, String pwdURL) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException, NoSuchProviderException, ExpandException {
        if (name == null && type == null && provider == null && pwdURL == null) {
            this.keyStoreName = null;
            this.keyStoreType = null;
            this.keyStoreProvider = null;
            this.keyStorePwdURL = null;
            return;
        }
        URL policyURL = null;
        if (policyFileName != null) {
            File pfile = new File(policyFileName);
            policyURL = new URL("file:" + pfile.getCanonicalPath());
        }
        if (name != null && name.length() > 0) {
            name = PropertyExpander.expand(name).replace(File.separatorChar, '/');
        }
        if (type == null || type.length() == 0) {
            type = KeyStore.getDefaultType();
        }
        if (pwdURL != null && pwdURL.length() > 0) {
            pwdURL = PropertyExpander.expand(pwdURL).replace(File.separatorChar, '/');
        }
        try {
            this.keyStore = PolicyUtil.getKeyStore(policyURL, name, type, provider, pwdURL, null);
        } catch (IOException ioe) {
            String MSG = "no password provided, and no callback handler " + "available for retrieving password";
            Throwable cause = ioe.getCause();
            if (cause != null && cause instanceof javax.security.auth.login.LoginException && MSG.equals(cause.getMessage())) {
                throw new IOException(MSG);
            } else {
                throw ioe;
            }
        }
        this.keyStoreName = name;
        this.keyStoreType = type;
        this.keyStoreProvider = provider;
        this.keyStorePwdURL = pwdURL;
    }

    boolean addEntry(PolicyEntry pe, int index) {
        if (index < 0) {
            policyEntries.addElement(pe);
            parser.add(pe.getGrantEntry());
        } else {
            PolicyEntry origPe = policyEntries.elementAt(index);
            parser.replace(origPe.getGrantEntry(), pe.getGrantEntry());
            policyEntries.setElementAt(pe, index);
        }
        return true;
    }

    boolean addPrinEntry(PolicyEntry pe, PolicyParser.PrincipalEntry newPrin, int index) {
        PolicyParser.GrantEntry grantEntry = pe.getGrantEntry();
        if (grantEntry.contains(newPrin) == true)
            return false;
        LinkedList<PolicyParser.PrincipalEntry> prinList = grantEntry.principals;
        if (index != -1)
            prinList.set(index, newPrin);
        else
            prinList.add(newPrin);
        modified = true;
        return true;
    }

    boolean addPermEntry(PolicyEntry pe, PolicyParser.PermissionEntry newPerm, int index) {
        PolicyParser.GrantEntry grantEntry = pe.getGrantEntry();
        if (grantEntry.contains(newPerm) == true)
            return false;
        Vector<PolicyParser.PermissionEntry> permList = grantEntry.permissionEntries;
        if (index != -1)
            permList.setElementAt(newPerm, index);
        else
            permList.addElement(newPerm);
        modified = true;
        return true;
    }

    boolean removePermEntry(PolicyEntry pe, PolicyParser.PermissionEntry perm) {
        PolicyParser.GrantEntry ppge = pe.getGrantEntry();
        modified = ppge.remove(perm);
        return modified;
    }

    boolean removeEntry(PolicyEntry pe) {
        parser.remove(pe.getGrantEntry());
        modified = true;
        return (policyEntries.removeElement(pe));
    }

    PolicyEntry[] getEntry() {
        if (policyEntries.size() > 0) {
            PolicyEntry[] entries = new PolicyEntry[policyEntries.size()];
            for (int i = 0; i < policyEntries.size(); i++) entries[i] = policyEntries.elementAt(i);
            return entries;
        }
        return null;
    }

    PublicKey getPublicKeyAlias(String name) throws KeyStoreException {
        if (keyStore == null) {
            return null;
        }
        Certificate cert = keyStore.getCertificate(name);
        if (cert == null) {
            return null;
        }
        PublicKey pubKey = cert.getPublicKey();
        return pubKey;
    }

    String[] getPublicKeyAlias() throws KeyStoreException {
        int numAliases = 0;
        String[] aliases = null;
        if (keyStore == null) {
            return null;
        }
        Enumeration<String> enum_ = keyStore.aliases();
        while (enum_.hasMoreElements()) {
            enum_.nextElement();
            numAliases++;
        }
        if (numAliases > 0) {
            aliases = new String[numAliases];
            numAliases = 0;
            enum_ = keyStore.aliases();
            while (enum_.hasMoreElements()) {
                aliases[numAliases] = new String(enum_.nextElement());
                numAliases++;
            }
        }
        return aliases;
    }

    String[] parseSigners(String signedBy) {
        String[] signers = null;
        int numSigners = 1;
        int signedByIndex = 0;
        int commaIndex = 0;
        int signerNum = 0;
        while (commaIndex >= 0) {
            commaIndex = signedBy.indexOf(',', signedByIndex);
            if (commaIndex >= 0) {
                numSigners++;
                signedByIndex = commaIndex + 1;
            }
        }
        signers = new String[numSigners];
        commaIndex = 0;
        signedByIndex = 0;
        while (commaIndex >= 0) {
            if ((commaIndex = signedBy.indexOf(',', signedByIndex)) >= 0) {
                signers[signerNum] = signedBy.substring(signedByIndex, commaIndex).trim();
                signerNum++;
                signedByIndex = commaIndex + 1;
            } else {
                signers[signerNum] = signedBy.substring(signedByIndex).trim();
            }
        }
        return signers;
    }

    void verifyPrincipal(String type, String name) throws ClassNotFoundException, InstantiationException {
        if (type.equals(PolicyParser.PrincipalEntry.WILDCARD_CLASS) || type.equals(PolicyParser.PrincipalEntry.REPLACE_NAME)) {
            return;
        }
        Class<?> PRIN = Class.forName("java.security.Principal");
        Class<?> pc = Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        if (!PRIN.isAssignableFrom(pc)) {
            MessageFormat form = new MessageFormat(rb.getString("Illegal.Principal.Type.type"));
            Object[] source = { type };
            throw new InstantiationException(form.format(source));
        }
        if (ToolDialog.X500_PRIN_CLASS.equals(pc.getName())) {
            X500Principal newP = new X500Principal(name);
        }
    }

    @SuppressWarnings("fallthrough")
    void verifyPermission(String type, String name, String actions) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> pc = Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        Constructor<?> c = null;
        Vector<String> objects = new Vector<>(2);
        if (name != null)
            objects.add(name);
        if (actions != null)
            objects.add(actions);
        switch(objects.size()) {
            case 0:
                try {
                    c = pc.getConstructor(NOPARAMS);
                    break;
                } catch (NoSuchMethodException ex) {
                    objects.add(null);
                }
            case 1:
                try {
                    c = pc.getConstructor(ONEPARAMS);
                    break;
                } catch (NoSuchMethodException ex) {
                    objects.add(null);
                }
            case 2:
                c = pc.getConstructor(TWOPARAMS);
                break;
        }
        Object[] parameters = objects.toArray();
        Permission p = (Permission) c.newInstance(parameters);
    }

    static void parseArgs(String[] args) {
        int n = 0;
        for (n = 0; (n < args.length) && args[n].startsWith("-"); n++) {
            String flags = args[n];
            if (collator.compare(flags, "-file") == 0) {
                if (++n == args.length)
                    usage();
                policyFileName = args[n];
            } else {
                MessageFormat form = new MessageFormat(rb.getString("Illegal.option.option"));
                Object[] source = { flags };
                System.err.println(form.format(source));
                usage();
            }
        }
    }

    static void usage() {
        System.out.println(rb.getString("Usage.policytool.options."));
        System.out.println();
        System.out.println(rb.getString(".file.file.policy.file.location"));
        System.out.println();
        System.exit(1);
    }

    public static void main(String[] args) {
        parseArgs(args);
        ToolWindow tw = new ToolWindow(new PolicyTool());
        tw.displayToolWindow(args);
    }

    static String splitToWords(String instr) {
        return instr.replaceAll("([A-Z])", " $1");
    }
}

class PolicyEntry {

    private CodeSource codesource;

    private PolicyTool tool;

    private PolicyParser.GrantEntry grantEntry;

    private boolean testing = false;

    PolicyEntry(PolicyTool tool, PolicyParser.GrantEntry ge) throws MalformedURLException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        this.tool = tool;
        URL location = null;
        if (ge.codeBase != null)
            location = new URL(ge.codeBase);
        this.codesource = new CodeSource(location, (java.security.cert.Certificate[]) null);
        if (testing) {
            System.out.println("Adding Policy Entry:");
            System.out.println("    CodeBase = " + location);
            System.out.println("    Signers = " + ge.signedBy);
            System.out.println("    with " + ge.principals.size() + " Principals");
        }
        this.grantEntry = ge;
    }

    CodeSource getCodeSource() {
        return codesource;
    }

    PolicyParser.GrantEntry getGrantEntry() {
        return grantEntry;
    }

    String headerToString() {
        String pString = principalsToString();
        if (pString.length() == 0) {
            return codebaseToString();
        } else {
            return codebaseToString() + ", " + pString;
        }
    }

    String codebaseToString() {
        String stringEntry = new String();
        if (grantEntry.codeBase != null && grantEntry.codeBase.equals("") == false)
            stringEntry = stringEntry.concat("CodeBase \"" + grantEntry.codeBase + "\"");
        if (grantEntry.signedBy != null && grantEntry.signedBy.equals("") == false)
            stringEntry = ((stringEntry.length() > 0) ? stringEntry.concat(", SignedBy \"" + grantEntry.signedBy + "\"") : stringEntry.concat("SignedBy \"" + grantEntry.signedBy + "\""));
        if (stringEntry.length() == 0)
            return new String("CodeBase <ALL>");
        return stringEntry;
    }

    String principalsToString() {
        String result = "";
        if ((grantEntry.principals != null) && (!grantEntry.principals.isEmpty())) {
            StringBuffer buffer = new StringBuffer(200);
            ListIterator<PolicyParser.PrincipalEntry> list = grantEntry.principals.listIterator();
            while (list.hasNext()) {
                PolicyParser.PrincipalEntry pppe = list.next();
                buffer.append(" Principal " + pppe.getDisplayClass() + " " + pppe.getDisplayName(true));
                if (list.hasNext())
                    buffer.append(", ");
            }
            result = buffer.toString();
        }
        return result;
    }

    PolicyParser.PermissionEntry toPermissionEntry(Permission perm) {
        String actions = null;
        if (perm.getActions() != null && perm.getActions().trim() != "")
            actions = perm.getActions();
        PolicyParser.PermissionEntry pe = new PolicyParser.PermissionEntry(perm.getClass().getName(), perm.getName(), actions);
        return pe;
    }
}

class ToolWindow extends Frame {

    private static final long serialVersionUID = 5682568601210376777L;

    public static final Insets TOP_PADDING = new Insets(25, 0, 0, 0);

    public static final Insets BOTTOM_PADDING = new Insets(0, 0, 25, 0);

    public static final Insets LITE_BOTTOM_PADDING = new Insets(0, 0, 10, 0);

    public static final Insets LR_PADDING = new Insets(0, 10, 0, 10);

    public static final Insets TOP_BOTTOM_PADDING = new Insets(15, 0, 15, 0);

    public static final Insets L_TOP_BOTTOM_PADDING = new Insets(5, 10, 15, 0);

    public static final Insets LR_BOTTOM_PADDING = new Insets(0, 10, 5, 10);

    public static final Insets L_BOTTOM_PADDING = new Insets(0, 10, 5, 0);

    public static final Insets R_BOTTOM_PADDING = new Insets(0, 0, 5, 10);

    public static final String NEW_POLICY_FILE = PolicyTool.rb.getString("New");

    public static final String OPEN_POLICY_FILE = PolicyTool.rb.getString("Open");

    public static final String SAVE_POLICY_FILE = PolicyTool.rb.getString("Save");

    public static final String SAVE_AS_POLICY_FILE = PolicyTool.rb.getString("Save.As");

    public static final String VIEW_WARNINGS = PolicyTool.rb.getString("View.Warning.Log");

    public static final String QUIT = PolicyTool.rb.getString("Exit");

    public static final String ADD_POLICY_ENTRY = PolicyTool.rb.getString("Add.Policy.Entry");

    public static final String EDIT_POLICY_ENTRY = PolicyTool.rb.getString("Edit.Policy.Entry");

    public static final String REMOVE_POLICY_ENTRY = PolicyTool.rb.getString("Remove.Policy.Entry");

    public static final String EDIT_KEYSTORE = PolicyTool.rb.getString("Edit");

    public static final String ADD_PUBKEY_ALIAS = PolicyTool.rb.getString("Add.Public.Key.Alias");

    public static final String REMOVE_PUBKEY_ALIAS = PolicyTool.rb.getString("Remove.Public.Key.Alias");

    public static final int MW_FILENAME_LABEL = 0;

    public static final int MW_FILENAME_TEXTFIELD = 1;

    public static final int MW_PANEL = 2;

    public static final int MW_ADD_BUTTON = 0;

    public static final int MW_EDIT_BUTTON = 1;

    public static final int MW_REMOVE_BUTTON = 2;

    public static final int MW_POLICY_LIST = 3;

    private PolicyTool tool;

    ToolWindow(PolicyTool tool) {
        this.tool = tool;
    }

    private void initWindow() {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu(PolicyTool.rb.getString("File"));
        menu.add(NEW_POLICY_FILE);
        menu.add(OPEN_POLICY_FILE);
        menu.add(SAVE_POLICY_FILE);
        menu.add(SAVE_AS_POLICY_FILE);
        menu.add(VIEW_WARNINGS);
        menu.add(QUIT);
        menu.addActionListener(new FileMenuListener(tool, this));
        menuBar.add(menu);
        setMenuBar(menuBar);
        menu = new Menu(PolicyTool.rb.getString("KeyStore"));
        menu.add(EDIT_KEYSTORE);
        menu.addActionListener(new MainWindowListener(tool, this));
        menuBar.add(menu);
        setMenuBar(menuBar);
        Label label = new Label(PolicyTool.rb.getString("Policy.File."));
        addNewComponent(this, label, MW_FILENAME_LABEL, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, TOP_BOTTOM_PADDING);
        TextField tf = new TextField(50);
        tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Policy.File."));
        tf.setEditable(false);
        addNewComponent(this, tf, MW_FILENAME_TEXTFIELD, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, TOP_BOTTOM_PADDING);
        Panel panel = new Panel();
        panel.setLayout(new GridBagLayout());
        Button button = new Button(ADD_POLICY_ENTRY);
        button.addActionListener(new MainWindowListener(tool, this));
        addNewComponent(panel, button, MW_ADD_BUTTON, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, LR_PADDING);
        button = new Button(EDIT_POLICY_ENTRY);
        button.addActionListener(new MainWindowListener(tool, this));
        addNewComponent(panel, button, MW_EDIT_BUTTON, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, LR_PADDING);
        button = new Button(REMOVE_POLICY_ENTRY);
        button.addActionListener(new MainWindowListener(tool, this));
        addNewComponent(panel, button, MW_REMOVE_BUTTON, 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, LR_PADDING);
        addNewComponent(this, panel, MW_PANEL, 0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.BOTH, BOTTOM_PADDING);
        String policyFile = tool.getPolicyFileName();
        if (policyFile == null) {
            String userHome;
            userHome = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("user.home"));
            policyFile = userHome + File.separatorChar + ".java.policy";
        }
        try {
            tool.openPolicy(policyFile);
            List list = new List(40, false);
            list.addActionListener(new PolicyListListener(tool, this));
            PolicyEntry[] entries = tool.getEntry();
            if (entries != null) {
                for (int i = 0; i < entries.length; i++) list.add(entries[i].headerToString());
            }
            TextField newFilename = (TextField) getComponent(MW_FILENAME_TEXTFIELD);
            newFilename.setText(policyFile);
            initPolicyList(list);
        } catch (FileNotFoundException fnfe) {
            List list = new List(40, false);
            list.addActionListener(new PolicyListListener(tool, this));
            initPolicyList(list);
            tool.setPolicyFileName(null);
            tool.modified = false;
            setVisible(true);
            tool.warnings.addElement(fnfe.toString());
        } catch (Exception e) {
            List list = new List(40, false);
            list.addActionListener(new PolicyListListener(tool, this));
            initPolicyList(list);
            tool.setPolicyFileName(null);
            tool.modified = false;
            setVisible(true);
            MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Could.not.open.policy.file.policyFile.e.toString."));
            Object[] source = { policyFile, e.toString() };
            displayErrorDialog(null, form.format(source));
        }
    }

    void addNewComponent(Container container, Component component, int index, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int fill, Insets is) {
        container.add(component, index);
        GridBagLayout gbl = (GridBagLayout) container.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = fill;
        if (is != null)
            gbc.insets = is;
        gbl.setConstraints(component, gbc);
    }

    void addNewComponent(Container container, Component component, int index, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int fill) {
        addNewComponent(container, component, index, gridx, gridy, gridwidth, gridheight, weightx, weighty, fill, null);
    }

    void initPolicyList(List policyList) {
        addNewComponent(this, policyList, MW_POLICY_LIST, 0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.BOTH);
    }

    void replacePolicyList(List policyList) {
        List list = (List) getComponent(MW_POLICY_LIST);
        list.removeAll();
        String[] newItems = policyList.getItems();
        for (int i = 0; i < newItems.length; i++) list.add(newItems[i]);
    }

    void displayToolWindow(String[] args) {
        setTitle(PolicyTool.rb.getString("Policy.Tool"));
        setResizable(true);
        addWindowListener(new ToolWindowListener(this));
        setBounds(135, 80, 500, 500);
        setLayout(new GridBagLayout());
        initWindow();
        setVisible(true);
        if (tool.newWarning == true) {
            displayStatusDialog(this, PolicyTool.rb.getString("Errors.have.occurred.while.opening.the.policy.configuration.View.the.Warning.Log.for.more.information."));
        }
    }

    void displayErrorDialog(Window w, String error) {
        ToolDialog ed = new ToolDialog(PolicyTool.rb.getString("Error"), tool, this, true);
        Point location = ((w == null) ? getLocationOnScreen() : w.getLocationOnScreen());
        ed.setBounds(location.x + 50, location.y + 50, 600, 100);
        ed.setLayout(new GridBagLayout());
        Label label = new Label(error);
        addNewComponent(ed, label, 0, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        Button okButton = new Button(PolicyTool.rb.getString("OK"));
        okButton.addActionListener(new ErrorOKButtonListener(ed));
        addNewComponent(ed, okButton, 1, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL);
        ed.pack();
        ed.setVisible(true);
    }

    void displayErrorDialog(Window w, Throwable t) {
        if (t instanceof NoDisplayException) {
            return;
        }
        displayErrorDialog(w, t.toString());
    }

    void displayStatusDialog(Window w, String status) {
        ToolDialog sd = new ToolDialog(PolicyTool.rb.getString("Status"), tool, this, true);
        Point location = ((w == null) ? getLocationOnScreen() : w.getLocationOnScreen());
        sd.setBounds(location.x + 50, location.y + 50, 500, 100);
        sd.setLayout(new GridBagLayout());
        Label label = new Label(status);
        addNewComponent(sd, label, 0, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        Button okButton = new Button(PolicyTool.rb.getString("OK"));
        okButton.addActionListener(new StatusOKButtonListener(sd));
        addNewComponent(sd, okButton, 1, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL);
        sd.pack();
        sd.setVisible(true);
    }

    void displayWarningLog(Window w) {
        ToolDialog wd = new ToolDialog(PolicyTool.rb.getString("Warning"), tool, this, true);
        Point location = ((w == null) ? getLocationOnScreen() : w.getLocationOnScreen());
        wd.setBounds(location.x + 50, location.y + 50, 500, 100);
        wd.setLayout(new GridBagLayout());
        TextArea ta = new TextArea();
        ta.setEditable(false);
        for (int i = 0; i < tool.warnings.size(); i++) {
            ta.append(tool.warnings.elementAt(i));
            ta.append(PolicyTool.rb.getString("NEWLINE"));
        }
        addNewComponent(wd, ta, 0, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, BOTTOM_PADDING);
        ta.setFocusable(false);
        Button okButton = new Button(PolicyTool.rb.getString("OK"));
        okButton.addActionListener(new CancelButtonListener(wd));
        addNewComponent(wd, okButton, 1, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, LR_PADDING);
        wd.pack();
        wd.setVisible(true);
    }

    char displayYesNoDialog(Window w, String title, String prompt, String yes, String no) {
        final ToolDialog tw = new ToolDialog(title, tool, this, true);
        Point location = ((w == null) ? getLocationOnScreen() : w.getLocationOnScreen());
        tw.setBounds(location.x + 75, location.y + 100, 400, 150);
        tw.setLayout(new GridBagLayout());
        TextArea ta = new TextArea(prompt, 10, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
        ta.setEditable(false);
        addNewComponent(tw, ta, 0, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        ta.setFocusable(false);
        Panel panel = new Panel();
        panel.setLayout(new GridBagLayout());
        final StringBuffer chooseResult = new StringBuffer();
        Button button = new Button(yes);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                chooseResult.append('Y');
                tw.setVisible(false);
                tw.dispose();
            }
        });
        addNewComponent(panel, button, 0, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, LR_PADDING);
        button = new Button(no);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                chooseResult.append('N');
                tw.setVisible(false);
                tw.dispose();
            }
        });
        addNewComponent(panel, button, 1, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, LR_PADDING);
        addNewComponent(tw, panel, 1, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL);
        tw.pack();
        tw.setVisible(true);
        if (chooseResult.length() > 0) {
            return chooseResult.charAt(0);
        } else {
            return 'N';
        }
    }
}

class ToolDialog extends Dialog {

    private static final long serialVersionUID = -372244357011301190L;

    public static final int NOACTION = 0;

    public static final int QUIT = 1;

    public static final int NEW = 2;

    public static final int OPEN = 3;

    public static final String ALL_PERM_CLASS = "java.security.AllPermission";

    public static final String FILE_PERM_CLASS = "java.io.FilePermission";

    public static final String X500_PRIN_CLASS = "javax.security.auth.x500.X500Principal";

    public static final String PERM = PolicyTool.rb.getString("Permission.");

    public static final String PRIN_TYPE = PolicyTool.rb.getString("Principal.Type.");

    public static final String PRIN_NAME = PolicyTool.rb.getString("Principal.Name.");

    public static final String PERM_NAME = PolicyTool.rb.getString("Target.Name.");

    public static final String PERM_ACTIONS = PolicyTool.rb.getString("Actions.");

    public static final int PE_CODEBASE_LABEL = 0;

    public static final int PE_CODEBASE_TEXTFIELD = 1;

    public static final int PE_SIGNEDBY_LABEL = 2;

    public static final int PE_SIGNEDBY_TEXTFIELD = 3;

    public static final int PE_PANEL0 = 4;

    public static final int PE_ADD_PRIN_BUTTON = 0;

    public static final int PE_EDIT_PRIN_BUTTON = 1;

    public static final int PE_REMOVE_PRIN_BUTTON = 2;

    public static final int PE_PRIN_LABEL = 5;

    public static final int PE_PRIN_LIST = 6;

    public static final int PE_PANEL1 = 7;

    public static final int PE_ADD_PERM_BUTTON = 0;

    public static final int PE_EDIT_PERM_BUTTON = 1;

    public static final int PE_REMOVE_PERM_BUTTON = 2;

    public static final int PE_PERM_LIST = 8;

    public static final int PE_PANEL2 = 9;

    public static final int PE_CANCEL_BUTTON = 1;

    public static final int PE_DONE_BUTTON = 0;

    public static final int PRD_DESC_LABEL = 0;

    public static final int PRD_PRIN_CHOICE = 1;

    public static final int PRD_PRIN_TEXTFIELD = 2;

    public static final int PRD_NAME_LABEL = 3;

    public static final int PRD_NAME_TEXTFIELD = 4;

    public static final int PRD_CANCEL_BUTTON = 6;

    public static final int PRD_OK_BUTTON = 5;

    public static final int PD_DESC_LABEL = 0;

    public static final int PD_PERM_CHOICE = 1;

    public static final int PD_PERM_TEXTFIELD = 2;

    public static final int PD_NAME_CHOICE = 3;

    public static final int PD_NAME_TEXTFIELD = 4;

    public static final int PD_ACTIONS_CHOICE = 5;

    public static final int PD_ACTIONS_TEXTFIELD = 6;

    public static final int PD_SIGNEDBY_LABEL = 7;

    public static final int PD_SIGNEDBY_TEXTFIELD = 8;

    public static final int PD_CANCEL_BUTTON = 10;

    public static final int PD_OK_BUTTON = 9;

    public static final int EDIT_KEYSTORE = 0;

    public static final int KSD_NAME_LABEL = 0;

    public static final int KSD_NAME_TEXTFIELD = 1;

    public static final int KSD_TYPE_LABEL = 2;

    public static final int KSD_TYPE_TEXTFIELD = 3;

    public static final int KSD_PROVIDER_LABEL = 4;

    public static final int KSD_PROVIDER_TEXTFIELD = 5;

    public static final int KSD_PWD_URL_LABEL = 6;

    public static final int KSD_PWD_URL_TEXTFIELD = 7;

    public static final int KSD_CANCEL_BUTTON = 9;

    public static final int KSD_OK_BUTTON = 8;

    public static final int USC_LABEL = 0;

    public static final int USC_PANEL = 1;

    public static final int USC_YES_BUTTON = 0;

    public static final int USC_NO_BUTTON = 1;

    public static final int USC_CANCEL_BUTTON = 2;

    public static final int CRPE_LABEL1 = 0;

    public static final int CRPE_LABEL2 = 1;

    public static final int CRPE_PANEL = 2;

    public static final int CRPE_PANEL_OK = 0;

    public static final int CRPE_PANEL_CANCEL = 1;

    private static final int PERMISSION = 0;

    private static final int PERMISSION_NAME = 1;

    private static final int PERMISSION_ACTIONS = 2;

    private static final int PERMISSION_SIGNEDBY = 3;

    private static final int PRINCIPAL_TYPE = 4;

    private static final int PRINCIPAL_NAME = 5;

    public static java.util.ArrayList<Perm> PERM_ARRAY;

    public static java.util.ArrayList<Prin> PRIN_ARRAY;

    PolicyTool tool;

    ToolWindow tw;

    static {
        PERM_ARRAY = new java.util.ArrayList<Perm>();
        PERM_ARRAY.add(new AllPerm());
        PERM_ARRAY.add(new AudioPerm());
        PERM_ARRAY.add(new AuthPerm());
        PERM_ARRAY.add(new AWTPerm());
        PERM_ARRAY.add(new DelegationPerm());
        PERM_ARRAY.add(new FilePerm());
        PERM_ARRAY.add(new HttpURLPerm());
        PERM_ARRAY.add(new InqSecContextPerm());
        PERM_ARRAY.add(new LogPerm());
        PERM_ARRAY.add(new MgmtPerm());
        PERM_ARRAY.add(new MBeanPerm());
        PERM_ARRAY.add(new MBeanSvrPerm());
        PERM_ARRAY.add(new MBeanTrustPerm());
        PERM_ARRAY.add(new NetPerm());
        PERM_ARRAY.add(new PrivCredPerm());
        PERM_ARRAY.add(new PropPerm());
        PERM_ARRAY.add(new ReflectPerm());
        PERM_ARRAY.add(new RuntimePerm());
        PERM_ARRAY.add(new SecurityPerm());
        PERM_ARRAY.add(new SerialPerm());
        PERM_ARRAY.add(new ServicePerm());
        PERM_ARRAY.add(new SocketPerm());
        PERM_ARRAY.add(new SQLPerm());
        PERM_ARRAY.add(new SSLPerm());
        PERM_ARRAY.add(new SubjDelegPerm());
        PRIN_ARRAY = new java.util.ArrayList<Prin>();
        PRIN_ARRAY.add(new KrbPrin());
        PRIN_ARRAY.add(new X500Prin());
    }

    ToolDialog(String title, PolicyTool tool, ToolWindow tw, boolean modal) {
        super(tw, modal);
        setTitle(title);
        this.tool = tool;
        this.tw = tw;
        addWindowListener(new ChildWindowListener(this));
    }

    static Perm getPerm(String clazz, boolean fullClassName) {
        for (int i = 0; i < PERM_ARRAY.size(); i++) {
            Perm next = PERM_ARRAY.get(i);
            if (fullClassName) {
                if (next.FULL_CLASS.equals(clazz)) {
                    return next;
                }
            } else {
                if (next.CLASS.equals(clazz)) {
                    return next;
                }
            }
        }
        return null;
    }

    static Prin getPrin(String clazz, boolean fullClassName) {
        for (int i = 0; i < PRIN_ARRAY.size(); i++) {
            Prin next = PRIN_ARRAY.get(i);
            if (fullClassName) {
                if (next.FULL_CLASS.equals(clazz)) {
                    return next;
                }
            } else {
                if (next.CLASS.equals(clazz)) {
                    return next;
                }
            }
        }
        return null;
    }

    void displayPolicyEntryDialog(boolean edit) {
        int listIndex = 0;
        PolicyEntry[] entries = null;
        TaggedList prinList = new TaggedList(3, false);
        prinList.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Principal.List"));
        prinList.addActionListener(new EditPrinButtonListener(tool, tw, this, edit));
        TaggedList permList = new TaggedList(10, false);
        permList.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Permission.List"));
        permList.addActionListener(new EditPermButtonListener(tool, tw, this, edit));
        Point location = tw.getLocationOnScreen();
        setBounds(location.x + 75, location.y + 200, 650, 500);
        setLayout(new GridBagLayout());
        setResizable(true);
        if (edit) {
            entries = tool.getEntry();
            List policyList = (List) tw.getComponent(ToolWindow.MW_POLICY_LIST);
            listIndex = policyList.getSelectedIndex();
            LinkedList<PolicyParser.PrincipalEntry> principals = entries[listIndex].getGrantEntry().principals;
            for (int i = 0; i < principals.size(); i++) {
                String prinString = null;
                PolicyParser.PrincipalEntry nextPrin = principals.get(i);
                prinList.addTaggedItem(PrincipalEntryToUserFriendlyString(nextPrin), nextPrin);
            }
            Vector<PolicyParser.PermissionEntry> permissions = entries[listIndex].getGrantEntry().permissionEntries;
            for (int i = 0; i < permissions.size(); i++) {
                String permString = null;
                PolicyParser.PermissionEntry nextPerm = permissions.elementAt(i);
                permList.addTaggedItem(ToolDialog.PermissionEntryToUserFriendlyString(nextPerm), nextPerm);
            }
        }
        Label label = new Label(PolicyTool.rb.getString("CodeBase."));
        tw.addNewComponent(this, label, PE_CODEBASE_LABEL, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        TextField tf;
        tf = (edit ? new TextField(entries[listIndex].getGrantEntry().codeBase, 60) : new TextField(60));
        tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Code.Base"));
        tw.addNewComponent(this, tf, PE_CODEBASE_TEXTFIELD, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        label = new Label(PolicyTool.rb.getString("SignedBy."));
        tw.addNewComponent(this, label, PE_SIGNEDBY_LABEL, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        tf = (edit ? new TextField(entries[listIndex].getGrantEntry().signedBy, 60) : new TextField(60));
        tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Signed.By."));
        tw.addNewComponent(this, tf, PE_SIGNEDBY_TEXTFIELD, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        Panel panel = new Panel();
        panel.setLayout(new GridBagLayout());
        Button button = new Button(PolicyTool.rb.getString("Add.Principal"));
        button.addActionListener(new AddPrinButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_ADD_PRIN_BUTTON, 0, 0, 1, 1, 100.0, 0.0, GridBagConstraints.HORIZONTAL);
        button = new Button(PolicyTool.rb.getString("Edit.Principal"));
        button.addActionListener(new EditPrinButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_EDIT_PRIN_BUTTON, 1, 0, 1, 1, 100.0, 0.0, GridBagConstraints.HORIZONTAL);
        button = new Button(PolicyTool.rb.getString("Remove.Principal"));
        button.addActionListener(new RemovePrinButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_REMOVE_PRIN_BUTTON, 2, 0, 1, 1, 100.0, 0.0, GridBagConstraints.HORIZONTAL);
        tw.addNewComponent(this, panel, PE_PANEL0, 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL);
        label = new Label(PolicyTool.rb.getString("Principals."));
        tw.addNewComponent(this, label, PE_PRIN_LABEL, 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
        tw.addNewComponent(this, prinList, PE_PRIN_LIST, 1, 3, 3, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
        panel = new Panel();
        panel.setLayout(new GridBagLayout());
        button = new Button(PolicyTool.rb.getString(".Add.Permission"));
        button.addActionListener(new AddPermButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_ADD_PERM_BUTTON, 0, 0, 1, 1, 100.0, 0.0, GridBagConstraints.HORIZONTAL);
        button = new Button(PolicyTool.rb.getString(".Edit.Permission"));
        button.addActionListener(new EditPermButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_EDIT_PERM_BUTTON, 1, 0, 1, 1, 100.0, 0.0, GridBagConstraints.HORIZONTAL);
        button = new Button(PolicyTool.rb.getString("Remove.Permission"));
        button.addActionListener(new RemovePermButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_REMOVE_PERM_BUTTON, 2, 0, 1, 1, 100.0, 0.0, GridBagConstraints.HORIZONTAL);
        tw.addNewComponent(this, panel, PE_PANEL1, 0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL, ToolWindow.LITE_BOTTOM_PADDING);
        tw.addNewComponent(this, permList, PE_PERM_LIST, 0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
        panel = new Panel();
        panel.setLayout(new GridBagLayout());
        button = new Button(PolicyTool.rb.getString("Done"));
        button.addActionListener(new AddEntryDoneButtonListener(tool, tw, this, edit));
        tw.addNewComponent(panel, button, PE_DONE_BUTTON, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_PADDING);
        button = new Button(PolicyTool.rb.getString("Cancel"));
        button.addActionListener(new CancelButtonListener(this));
        tw.addNewComponent(panel, button, PE_CANCEL_BUTTON, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_PADDING);
        tw.addNewComponent(this, panel, PE_PANEL2, 0, 6, 2, 1, 0.0, 0.0, GridBagConstraints.VERTICAL);
        setVisible(true);
    }

    PolicyEntry getPolicyEntryFromDialog() throws InvalidParameterException, MalformedURLException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, CertificateException, IOException, Exception {
        TextField tf = (TextField) getComponent(PE_CODEBASE_TEXTFIELD);
        String codebase = null;
        if (tf.getText().trim().equals("") == false)
            codebase = new String(tf.getText().trim());
        tf = (TextField) getComponent(PE_SIGNEDBY_TEXTFIELD);
        String signedby = null;
        if (tf.getText().trim().equals("") == false)
            signedby = new String(tf.getText().trim());
        PolicyParser.GrantEntry ge = new PolicyParser.GrantEntry(signedby, codebase);
        LinkedList<PolicyParser.PrincipalEntry> prins = new LinkedList<>();
        TaggedList prinList = (TaggedList) getComponent(PE_PRIN_LIST);
        for (int i = 0; i < prinList.getItemCount(); i++) {
            prins.add((PolicyParser.PrincipalEntry) prinList.getObject(i));
        }
        ge.principals = prins;
        Vector<PolicyParser.PermissionEntry> perms = new Vector<>();
        TaggedList permList = (TaggedList) getComponent(PE_PERM_LIST);
        for (int i = 0; i < permList.getItemCount(); i++) {
            perms.addElement((PolicyParser.PermissionEntry) permList.getObject(i));
        }
        ge.permissionEntries = perms;
        PolicyEntry entry = new PolicyEntry(tool, ge);
        return entry;
    }

    void keyStoreDialog(int mode) {
        Point location = tw.getLocationOnScreen();
        setBounds(location.x + 25, location.y + 100, 500, 300);
        setLayout(new GridBagLayout());
        if (mode == EDIT_KEYSTORE) {
            Label label = new Label(PolicyTool.rb.getString("KeyStore.URL."));
            tw.addNewComponent(this, label, KSD_NAME_LABEL, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            TextField tf = new TextField(tool.getKeyStoreName(), 30);
            tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore.U.R.L."));
            tw.addNewComponent(this, tf, KSD_NAME_TEXTFIELD, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            label = new Label(PolicyTool.rb.getString("KeyStore.Type."));
            tw.addNewComponent(this, label, KSD_TYPE_LABEL, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            tf = new TextField(tool.getKeyStoreType(), 30);
            tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore.Type."));
            tw.addNewComponent(this, tf, KSD_TYPE_TEXTFIELD, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            label = new Label(PolicyTool.rb.getString("KeyStore.Provider."));
            tw.addNewComponent(this, label, KSD_PROVIDER_LABEL, 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            tf = new TextField(tool.getKeyStoreProvider(), 30);
            tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore.Provider."));
            tw.addNewComponent(this, tf, KSD_PROVIDER_TEXTFIELD, 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            label = new Label(PolicyTool.rb.getString("KeyStore.Password.URL."));
            tw.addNewComponent(this, label, KSD_PWD_URL_LABEL, 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            tf = new TextField(tool.getKeyStorePwdURL(), 30);
            tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore.Password.U.R.L."));
            tw.addNewComponent(this, tf, KSD_PWD_URL_TEXTFIELD, 1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            Button okButton = new Button(PolicyTool.rb.getString("OK"));
            okButton.addActionListener(new ChangeKeyStoreOKButtonListener(tool, tw, this));
            tw.addNewComponent(this, okButton, KSD_OK_BUTTON, 0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL);
            Button cancelButton = new Button(PolicyTool.rb.getString("Cancel"));
            cancelButton.addActionListener(new CancelButtonListener(this));
            tw.addNewComponent(this, cancelButton, KSD_CANCEL_BUTTON, 1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL);
        }
        setVisible(true);
    }

    void displayPrincipalDialog(boolean editPolicyEntry, boolean edit) {
        PolicyParser.PrincipalEntry editMe = null;
        TaggedList prinList = (TaggedList) getComponent(PE_PRIN_LIST);
        int prinIndex = prinList.getSelectedIndex();
        if (edit) {
            editMe = (PolicyParser.PrincipalEntry) prinList.getObject(prinIndex);
        }
        ToolDialog newTD = new ToolDialog(PolicyTool.rb.getString("Principals"), tool, tw, true);
        newTD.addWindowListener(new ChildWindowListener(newTD));
        Point location = getLocationOnScreen();
        newTD.setBounds(location.x + 50, location.y + 100, 650, 190);
        newTD.setLayout(new GridBagLayout());
        newTD.setResizable(true);
        Label label = (edit ? new Label(PolicyTool.rb.getString(".Edit.Principal.")) : new Label(PolicyTool.rb.getString(".Add.New.Principal.")));
        tw.addNewComponent(newTD, label, PRD_DESC_LABEL, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.TOP_BOTTOM_PADDING);
        Choice choice = new Choice();
        choice.add(PRIN_TYPE);
        choice.getAccessibleContext().setAccessibleName(PRIN_TYPE);
        for (int i = 0; i < PRIN_ARRAY.size(); i++) {
            Prin next = PRIN_ARRAY.get(i);
            choice.add(next.CLASS);
        }
        choice.addItemListener(new PrincipalTypeMenuListener(newTD));
        if (edit) {
            if (PolicyParser.PrincipalEntry.WILDCARD_CLASS.equals(editMe.getPrincipalClass())) {
                choice.select(PRIN_TYPE);
            } else {
                Prin inputPrin = getPrin(editMe.getPrincipalClass(), true);
                if (inputPrin != null) {
                    choice.select(inputPrin.CLASS);
                }
            }
        }
        tw.addNewComponent(newTD, choice, PRD_PRIN_CHOICE, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        TextField tf;
        tf = (edit ? new TextField(editMe.getDisplayClass(), 30) : new TextField(30));
        tf.getAccessibleContext().setAccessibleName(PRIN_TYPE);
        tw.addNewComponent(newTD, tf, PRD_PRIN_TEXTFIELD, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        label = new Label(PRIN_NAME);
        tf = (edit ? new TextField(editMe.getDisplayName(), 40) : new TextField(40));
        tf.getAccessibleContext().setAccessibleName(PRIN_NAME);
        tw.addNewComponent(newTD, label, PRD_NAME_LABEL, 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        tw.addNewComponent(newTD, tf, PRD_NAME_TEXTFIELD, 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        Button okButton = new Button(PolicyTool.rb.getString("OK"));
        okButton.addActionListener(new NewPolicyPrinOKButtonListener(tool, tw, this, newTD, edit));
        tw.addNewComponent(newTD, okButton, PRD_OK_BUTTON, 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.TOP_BOTTOM_PADDING);
        Button cancelButton = new Button(PolicyTool.rb.getString("Cancel"));
        cancelButton.addActionListener(new CancelButtonListener(newTD));
        tw.addNewComponent(newTD, cancelButton, PRD_CANCEL_BUTTON, 1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.TOP_BOTTOM_PADDING);
        newTD.setVisible(true);
    }

    void displayPermissionDialog(boolean editPolicyEntry, boolean edit) {
        PolicyParser.PermissionEntry editMe = null;
        TaggedList permList = (TaggedList) getComponent(PE_PERM_LIST);
        int permIndex = permList.getSelectedIndex();
        if (edit) {
            editMe = (PolicyParser.PermissionEntry) permList.getObject(permIndex);
        }
        ToolDialog newTD = new ToolDialog(PolicyTool.rb.getString("Permissions"), tool, tw, true);
        newTD.addWindowListener(new ChildWindowListener(newTD));
        Point location = getLocationOnScreen();
        newTD.setBounds(location.x + 50, location.y + 100, 700, 250);
        newTD.setLayout(new GridBagLayout());
        newTD.setResizable(true);
        Label label = (edit ? new Label(PolicyTool.rb.getString(".Edit.Permission.")) : new Label(PolicyTool.rb.getString(".Add.New.Permission.")));
        tw.addNewComponent(newTD, label, PD_DESC_LABEL, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.TOP_BOTTOM_PADDING);
        Choice choice = new Choice();
        choice.add(PERM);
        choice.getAccessibleContext().setAccessibleName(PERM);
        for (int i = 0; i < PERM_ARRAY.size(); i++) {
            Perm next = PERM_ARRAY.get(i);
            choice.add(next.CLASS);
        }
        choice.addItemListener(new PermissionMenuListener(newTD));
        tw.addNewComponent(newTD, choice, PD_PERM_CHOICE, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        TextField tf;
        tf = (edit ? new TextField(editMe.permission, 30) : new TextField(30));
        tf.getAccessibleContext().setAccessibleName(PERM);
        if (edit) {
            Perm inputPerm = getPerm(editMe.permission, true);
            if (inputPerm != null) {
                choice.select(inputPerm.CLASS);
            }
        }
        tw.addNewComponent(newTD, tf, PD_PERM_TEXTFIELD, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        choice = new Choice();
        choice.add(PERM_NAME);
        choice.getAccessibleContext().setAccessibleName(PERM_NAME);
        choice.addItemListener(new PermissionNameMenuListener(newTD));
        tf = (edit ? new TextField(editMe.name, 40) : new TextField(40));
        tf.getAccessibleContext().setAccessibleName(PERM_NAME);
        if (edit) {
            setPermissionNames(getPerm(editMe.permission, true), choice, tf);
        }
        tw.addNewComponent(newTD, choice, PD_NAME_CHOICE, 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        tw.addNewComponent(newTD, tf, PD_NAME_TEXTFIELD, 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        choice = new Choice();
        choice.add(PERM_ACTIONS);
        choice.getAccessibleContext().setAccessibleName(PERM_ACTIONS);
        choice.addItemListener(new PermissionActionsMenuListener(newTD));
        tf = (edit ? new TextField(editMe.action, 40) : new TextField(40));
        tf.getAccessibleContext().setAccessibleName(PERM_ACTIONS);
        if (edit) {
            setPermissionActions(getPerm(editMe.permission, true), choice, tf);
        }
        tw.addNewComponent(newTD, choice, PD_ACTIONS_CHOICE, 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        tw.addNewComponent(newTD, tf, PD_ACTIONS_TEXTFIELD, 1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        label = new Label(PolicyTool.rb.getString("Signed.By."));
        tw.addNewComponent(newTD, label, PD_SIGNEDBY_LABEL, 0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        tf = (edit ? new TextField(editMe.signedBy, 40) : new TextField(40));
        tf.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Signed.By."));
        tw.addNewComponent(newTD, tf, PD_SIGNEDBY_TEXTFIELD, 1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.LR_PADDING);
        Button okButton = new Button(PolicyTool.rb.getString("OK"));
        okButton.addActionListener(new NewPolicyPermOKButtonListener(tool, tw, this, newTD, edit));
        tw.addNewComponent(newTD, okButton, PD_OK_BUTTON, 0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.TOP_BOTTOM_PADDING);
        Button cancelButton = new Button(PolicyTool.rb.getString("Cancel"));
        cancelButton.addActionListener(new CancelButtonListener(newTD));
        tw.addNewComponent(newTD, cancelButton, PD_CANCEL_BUTTON, 1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.TOP_BOTTOM_PADDING);
        newTD.setVisible(true);
    }

    PolicyParser.PrincipalEntry getPrinFromDialog() throws Exception {
        TextField tf = (TextField) getComponent(PRD_PRIN_TEXTFIELD);
        String pclass = new String(tf.getText().trim());
        tf = (TextField) getComponent(PRD_NAME_TEXTFIELD);
        String pname = new String(tf.getText().trim());
        if (pclass.equals("*")) {
            pclass = PolicyParser.PrincipalEntry.WILDCARD_CLASS;
        }
        if (pname.equals("*")) {
            pname = PolicyParser.PrincipalEntry.WILDCARD_NAME;
        }
        PolicyParser.PrincipalEntry pppe = null;
        if ((pclass.equals(PolicyParser.PrincipalEntry.WILDCARD_CLASS)) && (!pname.equals(PolicyParser.PrincipalEntry.WILDCARD_NAME))) {
            throw new Exception(PolicyTool.rb.getString("Cannot.Specify.Principal.with.a.Wildcard.Class.without.a.Wildcard.Name"));
        } else if (pname.equals("")) {
            throw new Exception(PolicyTool.rb.getString("Cannot.Specify.Principal.without.a.Name"));
        } else if (pclass.equals("")) {
            pclass = PolicyParser.PrincipalEntry.REPLACE_NAME;
            tool.warnings.addElement("Warning: Principal name '" + pname + "' specified without a Principal class.\n" + "\t'" + pname + "' will be interpreted " + "as a key store alias.\n" + "\tThe final principal class will be " + ToolDialog.X500_PRIN_CLASS + ".\n" + "\tThe final principal name will be " + "determined by the following:\n" + "\n" + "\tIf the key store entry identified by '" + pname + "'\n" + "\tis a key entry, then the principal name will be\n" + "\tthe subject distinguished name from the first\n" + "\tcertificate in the entry's certificate chain.\n" + "\n" + "\tIf the key store entry identified by '" + pname + "'\n" + "\tis a trusted certificate entry, then the\n" + "\tprincipal name will be the subject distinguished\n" + "\tname from the trusted public key certificate.");
            tw.displayStatusDialog(this, "'" + pname + "' will be interpreted as a key " + "store alias.  View Warning Log for details.");
        }
        return new PolicyParser.PrincipalEntry(pclass, pname);
    }

    PolicyParser.PermissionEntry getPermFromDialog() {
        TextField tf = (TextField) getComponent(PD_PERM_TEXTFIELD);
        String permission = new String(tf.getText().trim());
        tf = (TextField) getComponent(PD_NAME_TEXTFIELD);
        String name = null;
        if (tf.getText().trim().equals("") == false)
            name = new String(tf.getText().trim());
        if (permission.equals("") || (!permission.equals(ALL_PERM_CLASS) && name == null)) {
            throw new InvalidParameterException(PolicyTool.rb.getString("Permission.and.Target.Name.must.have.a.value"));
        }
        if (permission.equals(FILE_PERM_CLASS) && name.lastIndexOf("\\\\") > 0) {
            char result = tw.displayYesNoDialog(this, PolicyTool.rb.getString("Warning"), PolicyTool.rb.getString("Warning.File.name.may.include.escaped.backslash.characters.It.is.not.necessary.to.escape.backslash.characters.the.tool.escapes"), PolicyTool.rb.getString("Retain"), PolicyTool.rb.getString("Edit"));
            if (result != 'Y') {
                throw new NoDisplayException();
            }
        }
        tf = (TextField) getComponent(PD_ACTIONS_TEXTFIELD);
        String actions = null;
        if (tf.getText().trim().equals("") == false)
            actions = new String(tf.getText().trim());
        tf = (TextField) getComponent(PD_SIGNEDBY_TEXTFIELD);
        String signedBy = null;
        if (tf.getText().trim().equals("") == false)
            signedBy = new String(tf.getText().trim());
        PolicyParser.PermissionEntry pppe = new PolicyParser.PermissionEntry(permission, name, actions);
        pppe.signedBy = signedBy;
        if (signedBy != null) {
            String[] signers = tool.parseSigners(pppe.signedBy);
            for (int i = 0; i < signers.length; i++) {
                try {
                    PublicKey pubKey = tool.getPublicKeyAlias(signers[i]);
                    if (pubKey == null) {
                        MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Warning.A.public.key.for.alias.signers.i.does.not.exist.Make.sure.a.KeyStore.is.properly.configured."));
                        Object[] source = { signers[i] };
                        tool.warnings.addElement(form.format(source));
                        tw.displayStatusDialog(this, form.format(source));
                    }
                } catch (Exception e) {
                    tw.displayErrorDialog(this, e);
                }
            }
        }
        return pppe;
    }

    void displayConfirmRemovePolicyEntry() {
        List list = (List) tw.getComponent(ToolWindow.MW_POLICY_LIST);
        int index = list.getSelectedIndex();
        PolicyEntry[] entries = tool.getEntry();
        Point location = tw.getLocationOnScreen();
        setBounds(location.x + 25, location.y + 100, 600, 400);
        setLayout(new GridBagLayout());
        Label label = new Label(PolicyTool.rb.getString("Remove.this.Policy.Entry."));
        tw.addNewComponent(this, label, CRPE_LABEL1, 0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
        label = new Label(entries[index].codebaseToString());
        tw.addNewComponent(this, label, CRPE_LABEL2, 0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        label = new Label(entries[index].principalsToString().trim());
        tw.addNewComponent(this, label, CRPE_LABEL2 + 1, 0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.BOTH);
        Vector<PolicyParser.PermissionEntry> perms = entries[index].getGrantEntry().permissionEntries;
        for (int i = 0; i < perms.size(); i++) {
            PolicyParser.PermissionEntry nextPerm = perms.elementAt(i);
            String permString = ToolDialog.PermissionEntryToUserFriendlyString(nextPerm);
            label = new Label("    " + permString);
            if (i == (perms.size() - 1)) {
                tw.addNewComponent(this, label, CRPE_LABEL2 + 2 + i, 1, 3 + i, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.BOTTOM_PADDING);
            } else {
                tw.addNewComponent(this, label, CRPE_LABEL2 + 2 + i, 1, 3 + i, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
            }
        }
        Panel panel = new Panel();
        panel.setLayout(new GridBagLayout());
        Button okButton = new Button(PolicyTool.rb.getString("OK"));
        okButton.addActionListener(new ConfirmRemovePolicyEntryOKButtonListener(tool, tw, this));
        tw.addNewComponent(panel, okButton, CRPE_PANEL_OK, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_PADDING);
        Button cancelButton = new Button(PolicyTool.rb.getString("Cancel"));
        cancelButton.addActionListener(new CancelButtonListener(this));
        tw.addNewComponent(panel, cancelButton, CRPE_PANEL_CANCEL, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_PADDING);
        tw.addNewComponent(this, panel, CRPE_LABEL2 + 2 + perms.size(), 0, 3 + perms.size(), 2, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.TOP_BOTTOM_PADDING);
        pack();
        setVisible(true);
    }

    void displaySaveAsDialog(int nextEvent) {
        FileDialog fd = new FileDialog(tw, PolicyTool.rb.getString("Save.As"), FileDialog.SAVE);
        fd.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                e.getWindow().setVisible(false);
            }
        });
        fd.setVisible(true);
        if (fd.getFile() == null || fd.getFile().equals(""))
            return;
        File saveAsFile = new File(fd.getDirectory(), fd.getFile());
        String filename = saveAsFile.getPath();
        fd.dispose();
        try {
            tool.savePolicy(filename);
            MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Policy.successfully.written.to.filename"));
            Object[] source = { filename };
            tw.displayStatusDialog(null, form.format(source));
            TextField newFilename = (TextField) tw.getComponent(ToolWindow.MW_FILENAME_TEXTFIELD);
            newFilename.setText(filename);
            tw.setVisible(true);
            userSaveContinue(tool, tw, this, nextEvent);
        } catch (FileNotFoundException fnfe) {
            if (filename == null || filename.equals("")) {
                tw.displayErrorDialog(null, new FileNotFoundException(PolicyTool.rb.getString("null.filename")));
            } else {
                tw.displayErrorDialog(null, fnfe);
            }
        } catch (Exception ee) {
            tw.displayErrorDialog(null, ee);
        }
    }

    void displayUserSave(int select) {
        if (tool.modified == true) {
            Point location = tw.getLocationOnScreen();
            setBounds(location.x + 75, location.y + 100, 400, 150);
            setLayout(new GridBagLayout());
            Label label = new Label(PolicyTool.rb.getString("Save.changes."));
            tw.addNewComponent(this, label, USC_LABEL, 0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.BOTH, ToolWindow.L_TOP_BOTTOM_PADDING);
            Panel panel = new Panel();
            panel.setLayout(new GridBagLayout());
            Button yesButton = new Button(PolicyTool.rb.getString("Yes"));
            yesButton.addActionListener(new UserSaveYesButtonListener(this, tool, tw, select));
            tw.addNewComponent(panel, yesButton, USC_YES_BUTTON, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_BOTTOM_PADDING);
            Button noButton = new Button(PolicyTool.rb.getString("No"));
            noButton.addActionListener(new UserSaveNoButtonListener(this, tool, tw, select));
            tw.addNewComponent(panel, noButton, USC_NO_BUTTON, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_BOTTOM_PADDING);
            Button cancelButton = new Button(PolicyTool.rb.getString("Cancel"));
            cancelButton.addActionListener(new UserSaveCancelButtonListener(this));
            tw.addNewComponent(panel, cancelButton, USC_CANCEL_BUTTON, 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.VERTICAL, ToolWindow.LR_BOTTOM_PADDING);
            tw.addNewComponent(this, panel, USC_PANEL, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
            pack();
            setVisible(true);
        } else {
            userSaveContinue(tool, tw, this, select);
        }
    }

    @SuppressWarnings("fallthrough")
    void userSaveContinue(PolicyTool tool, ToolWindow tw, ToolDialog us, int select) {
        switch(select) {
            case ToolDialog.QUIT:
                tw.setVisible(false);
                tw.dispose();
                System.exit(0);
            case ToolDialog.NEW:
                try {
                    tool.openPolicy(null);
                } catch (Exception ee) {
                    tool.modified = false;
                    tw.displayErrorDialog(null, ee);
                }
                List list = new List(40, false);
                list.addActionListener(new PolicyListListener(tool, tw));
                tw.replacePolicyList(list);
                TextField newFilename = (TextField) tw.getComponent(ToolWindow.MW_FILENAME_TEXTFIELD);
                newFilename.setText("");
                tw.setVisible(true);
                break;
            case ToolDialog.OPEN:
                FileDialog fd = new FileDialog(tw, PolicyTool.rb.getString("Open"), FileDialog.LOAD);
                fd.addWindowListener(new WindowAdapter() {

                    public void windowClosing(WindowEvent e) {
                        e.getWindow().setVisible(false);
                    }
                });
                fd.setVisible(true);
                if (fd.getFile() == null || fd.getFile().equals(""))
                    return;
                String policyFile = new File(fd.getDirectory(), fd.getFile()).getPath();
                try {
                    tool.openPolicy(policyFile);
                    list = new List(40, false);
                    list.addActionListener(new PolicyListListener(tool, tw));
                    PolicyEntry[] entries = tool.getEntry();
                    if (entries != null) {
                        for (int i = 0; i < entries.length; i++) list.add(entries[i].headerToString());
                    }
                    tw.replacePolicyList(list);
                    tool.modified = false;
                    newFilename = (TextField) tw.getComponent(ToolWindow.MW_FILENAME_TEXTFIELD);
                    newFilename.setText(policyFile);
                    tw.setVisible(true);
                    if (tool.newWarning == true) {
                        tw.displayStatusDialog(null, PolicyTool.rb.getString("Errors.have.occurred.while.opening.the.policy.configuration.View.the.Warning.Log.for.more.information."));
                    }
                } catch (Exception e) {
                    list = new List(40, false);
                    list.addActionListener(new PolicyListListener(tool, tw));
                    tw.replacePolicyList(list);
                    tool.setPolicyFileName(null);
                    tool.modified = false;
                    newFilename = (TextField) tw.getComponent(ToolWindow.MW_FILENAME_TEXTFIELD);
                    newFilename.setText("");
                    tw.setVisible(true);
                    MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Could.not.open.policy.file.policyFile.e.toString."));
                    Object[] source = { policyFile, e.toString() };
                    tw.displayErrorDialog(null, form.format(source));
                }
                break;
        }
    }

    void setPermissionNames(Perm inputPerm, Choice names, TextField field) {
        names.removeAll();
        names.add(PERM_NAME);
        if (inputPerm == null) {
            field.setEditable(true);
        } else if (inputPerm.TARGETS == null) {
            field.setEditable(false);
        } else {
            field.setEditable(true);
            for (int i = 0; i < inputPerm.TARGETS.length; i++) {
                names.add(inputPerm.TARGETS[i]);
            }
        }
    }

    void setPermissionActions(Perm inputPerm, Choice actions, TextField field) {
        actions.removeAll();
        actions.add(PERM_ACTIONS);
        if (inputPerm == null) {
            field.setEditable(true);
        } else if (inputPerm.ACTIONS == null) {
            field.setEditable(false);
        } else {
            field.setEditable(true);
            for (int i = 0; i < inputPerm.ACTIONS.length; i++) {
                actions.add(inputPerm.ACTIONS[i]);
            }
        }
    }

    static String PermissionEntryToUserFriendlyString(PolicyParser.PermissionEntry pppe) {
        String result = pppe.permission;
        if (pppe.name != null) {
            result += " " + pppe.name;
        }
        if (pppe.action != null) {
            result += ", \"" + pppe.action + "\"";
        }
        if (pppe.signedBy != null) {
            result += ", signedBy " + pppe.signedBy;
        }
        return result;
    }

    static String PrincipalEntryToUserFriendlyString(PolicyParser.PrincipalEntry pppe) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pppe.write(pw);
        return sw.toString();
    }
}

class ToolWindowListener implements WindowListener {

    private ToolWindow tw;

    ToolWindowListener(ToolWindow tw) {
        this.tw = tw;
    }

    public void windowOpened(WindowEvent we) {
    }

    public void windowClosing(WindowEvent we) {
        tw.setVisible(false);
        tw.dispose();
        System.exit(0);
    }

    public void windowClosed(WindowEvent we) {
        System.exit(0);
    }

    public void windowIconified(WindowEvent we) {
    }

    public void windowDeiconified(WindowEvent we) {
    }

    public void windowActivated(WindowEvent we) {
    }

    public void windowDeactivated(WindowEvent we) {
    }
}

class PolicyListListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    PolicyListListener(PolicyTool tool, ToolWindow tw) {
        this.tool = tool;
        this.tw = tw;
    }

    public void actionPerformed(ActionEvent e) {
        ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Policy.Entry"), tool, tw, true);
        td.displayPolicyEntryDialog(true);
    }
}

class FileMenuListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    FileMenuListener(PolicyTool tool, ToolWindow tw) {
        this.tool = tool;
        this.tw = tw;
    }

    public void actionPerformed(ActionEvent e) {
        if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.QUIT) == 0) {
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Save.Changes"), tool, tw, true);
            td.displayUserSave(ToolDialog.QUIT);
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.NEW_POLICY_FILE) == 0) {
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Save.Changes"), tool, tw, true);
            td.displayUserSave(ToolDialog.NEW);
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.OPEN_POLICY_FILE) == 0) {
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Save.Changes"), tool, tw, true);
            td.displayUserSave(ToolDialog.OPEN);
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.SAVE_POLICY_FILE) == 0) {
            String filename = ((TextField) tw.getComponent(ToolWindow.MW_FILENAME_TEXTFIELD)).getText();
            if (filename == null || filename.length() == 0) {
                ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Save.As"), tool, tw, true);
                td.displaySaveAsDialog(ToolDialog.NOACTION);
            } else {
                try {
                    tool.savePolicy(filename);
                    MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Policy.successfully.written.to.filename"));
                    Object[] source = { filename };
                    tw.displayStatusDialog(null, form.format(source));
                } catch (FileNotFoundException fnfe) {
                    if (filename == null || filename.equals("")) {
                        tw.displayErrorDialog(null, new FileNotFoundException(PolicyTool.rb.getString("null.filename")));
                    } else {
                        tw.displayErrorDialog(null, fnfe);
                    }
                } catch (Exception ee) {
                    tw.displayErrorDialog(null, ee);
                }
            }
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.SAVE_AS_POLICY_FILE) == 0) {
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Save.As"), tool, tw, true);
            td.displaySaveAsDialog(ToolDialog.NOACTION);
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.VIEW_WARNINGS) == 0) {
            tw.displayWarningLog(null);
        }
    }
}

class MainWindowListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    MainWindowListener(PolicyTool tool, ToolWindow tw) {
        this.tool = tool;
        this.tw = tw;
    }

    public void actionPerformed(ActionEvent e) {
        if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.ADD_POLICY_ENTRY) == 0) {
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Policy.Entry"), tool, tw, true);
            td.displayPolicyEntryDialog(false);
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.REMOVE_POLICY_ENTRY) == 0) {
            List list = (List) tw.getComponent(ToolWindow.MW_POLICY_LIST);
            int index = list.getSelectedIndex();
            if (index < 0) {
                tw.displayErrorDialog(null, new Exception(PolicyTool.rb.getString("No.Policy.Entry.selected")));
                return;
            }
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Remove.Policy.Entry"), tool, tw, true);
            td.displayConfirmRemovePolicyEntry();
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.EDIT_POLICY_ENTRY) == 0) {
            List list = (List) tw.getComponent(ToolWindow.MW_POLICY_LIST);
            int index = list.getSelectedIndex();
            if (index < 0) {
                tw.displayErrorDialog(null, new Exception(PolicyTool.rb.getString("No.Policy.Entry.selected")));
                return;
            }
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("Policy.Entry"), tool, tw, true);
            td.displayPolicyEntryDialog(true);
        } else if (PolicyTool.collator.compare(e.getActionCommand(), ToolWindow.EDIT_KEYSTORE) == 0) {
            ToolDialog td = new ToolDialog(PolicyTool.rb.getString("KeyStore"), tool, tw, true);
            td.keyStoreDialog(ToolDialog.EDIT_KEYSTORE);
        }
    }
}

class AddEntryDoneButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean edit;

    AddEntryDoneButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean edit) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.edit = edit;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            PolicyEntry newEntry = td.getPolicyEntryFromDialog();
            PolicyParser.GrantEntry newGe = newEntry.getGrantEntry();
            if (newGe.signedBy != null) {
                String[] signers = tool.parseSigners(newGe.signedBy);
                for (int i = 0; i < signers.length; i++) {
                    PublicKey pubKey = tool.getPublicKeyAlias(signers[i]);
                    if (pubKey == null) {
                        MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Warning.A.public.key.for.alias.signers.i.does.not.exist.Make.sure.a.KeyStore.is.properly.configured."));
                        Object[] source = { signers[i] };
                        tool.warnings.addElement(form.format(source));
                        tw.displayStatusDialog(td, form.format(source));
                    }
                }
            }
            List policyList = (List) tw.getComponent(ToolWindow.MW_POLICY_LIST);
            if (edit) {
                int listIndex = policyList.getSelectedIndex();
                tool.addEntry(newEntry, listIndex);
                String newCodeBaseStr = newEntry.headerToString();
                if (PolicyTool.collator.compare(newCodeBaseStr, policyList.getItem(listIndex)) != 0)
                    tool.modified = true;
                policyList.replaceItem(newCodeBaseStr, listIndex);
            } else {
                tool.addEntry(newEntry, -1);
                policyList.add(newEntry.headerToString());
                tool.modified = true;
            }
            td.setVisible(false);
            td.dispose();
        } catch (Exception eee) {
            tw.displayErrorDialog(td, eee);
        }
    }
}

class ChangeKeyStoreOKButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    ChangeKeyStoreOKButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
    }

    public void actionPerformed(ActionEvent e) {
        String URLString = ((TextField) td.getComponent(ToolDialog.KSD_NAME_TEXTFIELD)).getText().trim();
        String type = ((TextField) td.getComponent(ToolDialog.KSD_TYPE_TEXTFIELD)).getText().trim();
        String provider = ((TextField) td.getComponent(ToolDialog.KSD_PROVIDER_TEXTFIELD)).getText().trim();
        String pwdURL = ((TextField) td.getComponent(ToolDialog.KSD_PWD_URL_TEXTFIELD)).getText().trim();
        try {
            tool.openKeyStore((URLString.length() == 0 ? null : URLString), (type.length() == 0 ? null : type), (provider.length() == 0 ? null : provider), (pwdURL.length() == 0 ? null : pwdURL));
            tool.modified = true;
        } catch (Exception ex) {
            MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Unable.to.open.KeyStore.ex.toString."));
            Object[] source = { ex.toString() };
            tw.displayErrorDialog(td, form.format(source));
            return;
        }
        td.dispose();
    }
}

class AddPrinButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean editPolicyEntry;

    AddPrinButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean editPolicyEntry) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.editPolicyEntry = editPolicyEntry;
    }

    public void actionPerformed(ActionEvent e) {
        td.displayPrincipalDialog(editPolicyEntry, false);
    }
}

class AddPermButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean editPolicyEntry;

    AddPermButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean editPolicyEntry) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.editPolicyEntry = editPolicyEntry;
    }

    public void actionPerformed(ActionEvent e) {
        td.displayPermissionDialog(editPolicyEntry, false);
    }
}

class NewPolicyPrinOKButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog listDialog;

    private ToolDialog infoDialog;

    private boolean edit;

    NewPolicyPrinOKButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog listDialog, ToolDialog infoDialog, boolean edit) {
        this.tool = tool;
        this.tw = tw;
        this.listDialog = listDialog;
        this.infoDialog = infoDialog;
        this.edit = edit;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            PolicyParser.PrincipalEntry pppe = infoDialog.getPrinFromDialog();
            if (pppe != null) {
                try {
                    tool.verifyPrincipal(pppe.getPrincipalClass(), pppe.getPrincipalName());
                } catch (ClassNotFoundException cnfe) {
                    MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Warning.Class.not.found.class"));
                    Object[] source = { pppe.getPrincipalClass() };
                    tool.warnings.addElement(form.format(source));
                    tw.displayStatusDialog(infoDialog, form.format(source));
                }
                TaggedList prinList = (TaggedList) listDialog.getComponent(ToolDialog.PE_PRIN_LIST);
                String prinString = ToolDialog.PrincipalEntryToUserFriendlyString(pppe);
                if (edit) {
                    int index = prinList.getSelectedIndex();
                    prinList.replaceTaggedItem(prinString, pppe, index);
                } else {
                    prinList.addTaggedItem(prinString, pppe);
                }
            }
            infoDialog.dispose();
        } catch (Exception ee) {
            tw.displayErrorDialog(infoDialog, ee);
        }
    }
}

class NewPolicyPermOKButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog listDialog;

    private ToolDialog infoDialog;

    private boolean edit;

    NewPolicyPermOKButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog listDialog, ToolDialog infoDialog, boolean edit) {
        this.tool = tool;
        this.tw = tw;
        this.listDialog = listDialog;
        this.infoDialog = infoDialog;
        this.edit = edit;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            PolicyParser.PermissionEntry pppe = infoDialog.getPermFromDialog();
            try {
                tool.verifyPermission(pppe.permission, pppe.name, pppe.action);
            } catch (ClassNotFoundException cnfe) {
                MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Warning.Class.not.found.class"));
                Object[] source = { pppe.permission };
                tool.warnings.addElement(form.format(source));
                tw.displayStatusDialog(infoDialog, form.format(source));
            }
            TaggedList permList = (TaggedList) listDialog.getComponent(ToolDialog.PE_PERM_LIST);
            String permString = ToolDialog.PermissionEntryToUserFriendlyString(pppe);
            if (edit) {
                int which = permList.getSelectedIndex();
                permList.replaceTaggedItem(permString, pppe, which);
            } else {
                permList.addTaggedItem(permString, pppe);
            }
            infoDialog.dispose();
        } catch (InvocationTargetException ite) {
            tw.displayErrorDialog(infoDialog, ite.getTargetException());
        } catch (Exception ee) {
            tw.displayErrorDialog(infoDialog, ee);
        }
    }
}

class RemovePrinButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean edit;

    RemovePrinButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean edit) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.edit = edit;
    }

    public void actionPerformed(ActionEvent e) {
        TaggedList prinList = (TaggedList) td.getComponent(ToolDialog.PE_PRIN_LIST);
        int prinIndex = prinList.getSelectedIndex();
        if (prinIndex < 0) {
            tw.displayErrorDialog(td, new Exception(PolicyTool.rb.getString("No.principal.selected")));
            return;
        }
        prinList.removeTaggedItem(prinIndex);
    }
}

class RemovePermButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean edit;

    RemovePermButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean edit) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.edit = edit;
    }

    public void actionPerformed(ActionEvent e) {
        TaggedList permList = (TaggedList) td.getComponent(ToolDialog.PE_PERM_LIST);
        int permIndex = permList.getSelectedIndex();
        if (permIndex < 0) {
            tw.displayErrorDialog(td, new Exception(PolicyTool.rb.getString("No.permission.selected")));
            return;
        }
        permList.removeTaggedItem(permIndex);
    }
}

class EditPrinButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean editPolicyEntry;

    EditPrinButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean editPolicyEntry) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.editPolicyEntry = editPolicyEntry;
    }

    public void actionPerformed(ActionEvent e) {
        TaggedList list = (TaggedList) td.getComponent(ToolDialog.PE_PRIN_LIST);
        int prinIndex = list.getSelectedIndex();
        if (prinIndex < 0) {
            tw.displayErrorDialog(td, new Exception(PolicyTool.rb.getString("No.principal.selected")));
            return;
        }
        td.displayPrincipalDialog(editPolicyEntry, true);
    }
}

class EditPermButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog td;

    private boolean editPolicyEntry;

    EditPermButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog td, boolean editPolicyEntry) {
        this.tool = tool;
        this.tw = tw;
        this.td = td;
        this.editPolicyEntry = editPolicyEntry;
    }

    public void actionPerformed(ActionEvent e) {
        List list = (List) td.getComponent(ToolDialog.PE_PERM_LIST);
        int permIndex = list.getSelectedIndex();
        if (permIndex < 0) {
            tw.displayErrorDialog(td, new Exception(PolicyTool.rb.getString("No.permission.selected")));
            return;
        }
        td.displayPermissionDialog(editPolicyEntry, true);
    }
}

class PrincipalTypeMenuListener implements ItemListener {

    private ToolDialog td;

    PrincipalTypeMenuListener(ToolDialog td) {
        this.td = td;
    }

    public void itemStateChanged(ItemEvent e) {
        Choice prin = (Choice) td.getComponent(ToolDialog.PRD_PRIN_CHOICE);
        TextField prinField = (TextField) td.getComponent(ToolDialog.PRD_PRIN_TEXTFIELD);
        TextField nameField = (TextField) td.getComponent(ToolDialog.PRD_NAME_TEXTFIELD);
        prin.getAccessibleContext().setAccessibleName(PolicyTool.splitToWords((String) e.getItem()));
        if (((String) e.getItem()).equals(ToolDialog.PRIN_TYPE)) {
            if (prinField.getText() != null && prinField.getText().length() > 0) {
                Prin inputPrin = ToolDialog.getPrin(prinField.getText(), true);
                prin.select(inputPrin.CLASS);
            }
            return;
        }
        if (prinField.getText().indexOf((String) e.getItem()) == -1) {
            nameField.setText("");
        }
        Prin inputPrin = ToolDialog.getPrin((String) e.getItem(), false);
        if (inputPrin != null) {
            prinField.setText(inputPrin.FULL_CLASS);
        }
    }
}

class PermissionMenuListener implements ItemListener {

    private ToolDialog td;

    PermissionMenuListener(ToolDialog td) {
        this.td = td;
    }

    public void itemStateChanged(ItemEvent e) {
        Choice perms = (Choice) td.getComponent(ToolDialog.PD_PERM_CHOICE);
        Choice names = (Choice) td.getComponent(ToolDialog.PD_NAME_CHOICE);
        Choice actions = (Choice) td.getComponent(ToolDialog.PD_ACTIONS_CHOICE);
        TextField nameField = (TextField) td.getComponent(ToolDialog.PD_NAME_TEXTFIELD);
        TextField actionsField = (TextField) td.getComponent(ToolDialog.PD_ACTIONS_TEXTFIELD);
        TextField permField = (TextField) td.getComponent(ToolDialog.PD_PERM_TEXTFIELD);
        TextField signedbyField = (TextField) td.getComponent(ToolDialog.PD_SIGNEDBY_TEXTFIELD);
        perms.getAccessibleContext().setAccessibleName(PolicyTool.splitToWords((String) e.getItem()));
        if (PolicyTool.collator.compare((String) e.getItem(), ToolDialog.PERM) == 0) {
            if (permField.getText() != null && permField.getText().length() > 0) {
                Perm inputPerm = ToolDialog.getPerm(permField.getText(), true);
                if (inputPerm != null) {
                    perms.select(inputPerm.CLASS);
                }
            }
            return;
        }
        if (permField.getText().indexOf((String) e.getItem()) == -1) {
            nameField.setText("");
            actionsField.setText("");
            signedbyField.setText("");
        }
        Perm inputPerm = ToolDialog.getPerm((String) e.getItem(), false);
        if (inputPerm == null) {
            permField.setText("");
        } else {
            permField.setText(inputPerm.FULL_CLASS);
        }
        td.setPermissionNames(inputPerm, names, nameField);
        td.setPermissionActions(inputPerm, actions, actionsField);
    }
}

class PermissionNameMenuListener implements ItemListener {

    private ToolDialog td;

    PermissionNameMenuListener(ToolDialog td) {
        this.td = td;
    }

    public void itemStateChanged(ItemEvent e) {
        Choice names = (Choice) td.getComponent(ToolDialog.PD_NAME_CHOICE);
        names.getAccessibleContext().setAccessibleName(PolicyTool.splitToWords((String) e.getItem()));
        if (((String) e.getItem()).indexOf(ToolDialog.PERM_NAME) != -1)
            return;
        TextField tf = (TextField) td.getComponent(ToolDialog.PD_NAME_TEXTFIELD);
        tf.setText((String) e.getItem());
    }
}

class PermissionActionsMenuListener implements ItemListener {

    private ToolDialog td;

    PermissionActionsMenuListener(ToolDialog td) {
        this.td = td;
    }

    public void itemStateChanged(ItemEvent e) {
        Choice actions = (Choice) td.getComponent(ToolDialog.PD_ACTIONS_CHOICE);
        actions.getAccessibleContext().setAccessibleName((String) e.getItem());
        if (((String) e.getItem()).indexOf(ToolDialog.PERM_ACTIONS) != -1)
            return;
        TextField tf = (TextField) td.getComponent(ToolDialog.PD_ACTIONS_TEXTFIELD);
        if (tf.getText() == null || tf.getText().equals("")) {
            tf.setText((String) e.getItem());
        } else {
            if (tf.getText().indexOf((String) e.getItem()) == -1)
                tf.setText(tf.getText() + ", " + (String) e.getItem());
        }
    }
}

class ChildWindowListener implements WindowListener {

    private ToolDialog td;

    ChildWindowListener(ToolDialog td) {
        this.td = td;
    }

    public void windowOpened(WindowEvent we) {
    }

    public void windowClosing(WindowEvent we) {
        td.setVisible(false);
        td.dispose();
    }

    public void windowClosed(WindowEvent we) {
    }

    public void windowIconified(WindowEvent we) {
    }

    public void windowDeiconified(WindowEvent we) {
    }

    public void windowActivated(WindowEvent we) {
    }

    public void windowDeactivated(WindowEvent we) {
    }
}

class CancelButtonListener implements ActionListener {

    private ToolDialog td;

    CancelButtonListener(ToolDialog td) {
        this.td = td;
    }

    public void actionPerformed(ActionEvent e) {
        td.setVisible(false);
        td.dispose();
    }
}

class ErrorOKButtonListener implements ActionListener {

    private ToolDialog ed;

    ErrorOKButtonListener(ToolDialog ed) {
        this.ed = ed;
    }

    public void actionPerformed(ActionEvent e) {
        ed.setVisible(false);
        ed.dispose();
    }
}

class StatusOKButtonListener implements ActionListener {

    private ToolDialog sd;

    StatusOKButtonListener(ToolDialog sd) {
        this.sd = sd;
    }

    public void actionPerformed(ActionEvent e) {
        sd.setVisible(false);
        sd.dispose();
    }
}

class UserSaveYesButtonListener implements ActionListener {

    private ToolDialog us;

    private PolicyTool tool;

    private ToolWindow tw;

    private int select;

    UserSaveYesButtonListener(ToolDialog us, PolicyTool tool, ToolWindow tw, int select) {
        this.us = us;
        this.tool = tool;
        this.tw = tw;
        this.select = select;
    }

    public void actionPerformed(ActionEvent e) {
        us.setVisible(false);
        us.dispose();
        try {
            String filename = ((TextField) tw.getComponent(ToolWindow.MW_FILENAME_TEXTFIELD)).getText();
            if (filename == null || filename.equals("")) {
                us.displaySaveAsDialog(select);
            } else {
                tool.savePolicy(filename);
                MessageFormat form = new MessageFormat(PolicyTool.rb.getString("Policy.successfully.written.to.filename"));
                Object[] source = { filename };
                tw.displayStatusDialog(null, form.format(source));
                us.userSaveContinue(tool, tw, us, select);
            }
        } catch (Exception ee) {
            tw.displayErrorDialog(null, ee);
        }
    }
}

class UserSaveNoButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog us;

    private int select;

    UserSaveNoButtonListener(ToolDialog us, PolicyTool tool, ToolWindow tw, int select) {
        this.us = us;
        this.tool = tool;
        this.tw = tw;
        this.select = select;
    }

    public void actionPerformed(ActionEvent e) {
        us.setVisible(false);
        us.dispose();
        us.userSaveContinue(tool, tw, us, select);
    }
}

class UserSaveCancelButtonListener implements ActionListener {

    private ToolDialog us;

    UserSaveCancelButtonListener(ToolDialog us) {
        this.us = us;
    }

    public void actionPerformed(ActionEvent e) {
        us.setVisible(false);
        us.dispose();
    }
}

class ConfirmRemovePolicyEntryOKButtonListener implements ActionListener {

    private PolicyTool tool;

    private ToolWindow tw;

    private ToolDialog us;

    ConfirmRemovePolicyEntryOKButtonListener(PolicyTool tool, ToolWindow tw, ToolDialog us) {
        this.tool = tool;
        this.tw = tw;
        this.us = us;
    }

    public void actionPerformed(ActionEvent e) {
        List list = (List) tw.getComponent(ToolWindow.MW_POLICY_LIST);
        int index = list.getSelectedIndex();
        PolicyEntry[] entries = tool.getEntry();
        tool.removeEntry(entries[index]);
        list = new List(40, false);
        list.addActionListener(new PolicyListListener(tool, tw));
        entries = tool.getEntry();
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) list.add(entries[i].headerToString());
        }
        tw.replacePolicyList(list);
        us.setVisible(false);
        us.dispose();
    }
}

class NoDisplayException extends RuntimeException {

    private static final long serialVersionUID = -4611761427108719794L;
}

class TaggedList extends List {

    private static final long serialVersionUID = -5676238110427785853L;

    private java.util.List<Object> data = new LinkedList<>();

    public TaggedList(int i, boolean b) {
        super(i, b);
    }

    public Object getObject(int index) {
        return data.get(index);
    }

    @Override
    @Deprecated
    public void add(String string) {
        throw new AssertionError("should not call add in TaggedList");
    }

    public void addTaggedItem(String string, Object object) {
        super.add(string);
        data.add(object);
    }

    @Override
    @Deprecated
    public void replaceItem(String string, int index) {
        throw new AssertionError("should not call replaceItem in TaggedList");
    }

    public void replaceTaggedItem(String string, Object object, int index) {
        super.replaceItem(string, index);
        data.set(index, object);
    }

    @Override
    @Deprecated
    public void remove(int index) {
        super.remove(index);
    }

    public void removeTaggedItem(int index) {
        super.remove(index);
        data.remove(index);
    }
}

class Prin {

    public final String CLASS;

    public final String FULL_CLASS;

    public Prin(String clazz, String fullClass) {
        this.CLASS = clazz;
        this.FULL_CLASS = fullClass;
    }
}

class KrbPrin extends Prin {

    public KrbPrin() {
        super("KerberosPrincipal", "javax.security.auth.kerberos.KerberosPrincipal");
    }
}

class X500Prin extends Prin {

    public X500Prin() {
        super("X500Principal", "javax.security.auth.x500.X500Principal");
    }
}

class Perm {

    public final String CLASS;

    public final String FULL_CLASS;

    public final String[] TARGETS;

    public final String[] ACTIONS;

    public Perm(String clazz, String fullClass, String[] targets, String[] actions) {
        this.CLASS = clazz;
        this.FULL_CLASS = fullClass;
        this.TARGETS = targets;
        this.ACTIONS = actions;
    }
}

class AllPerm extends Perm {

    public AllPerm() {
        super("AllPermission", "java.security.AllPermission", null, null);
    }
}

class AudioPerm extends Perm {

    public AudioPerm() {
        super("AudioPermission", "javax.sound.sampled.AudioPermission", new String[] { "play", "record" }, null);
    }
}

class AuthPerm extends Perm {

    public AuthPerm() {
        super("AuthPermission", "javax.security.auth.AuthPermission", new String[] { "doAs", "doAsPrivileged", "getSubject", "getSubjectFromDomainCombiner", "setReadOnly", "modifyPrincipals", "modifyPublicCredentials", "modifyPrivateCredentials", "refreshCredential", "destroyCredential", "createLoginContext.<" + PolicyTool.rb.getString("name") + ">", "getLoginConfiguration", "setLoginConfiguration", "createLoginConfiguration.<" + PolicyTool.rb.getString("configuration.type") + ">", "refreshLoginConfiguration" }, null);
    }
}

class AWTPerm extends Perm {

    public AWTPerm() {
        super("AWTPermission", "java.awt.AWTPermission", new String[] { "accessClipboard", "accessEventQueue", "accessSystemTray", "createRobot", "fullScreenExclusive", "listenToAllAWTEvents", "readDisplayPixels", "replaceKeyboardFocusManager", "setAppletStub", "setWindowAlwaysOnTop", "showWindowWithoutWarningBanner", "toolkitModality", "watchMousePointer" }, null);
    }
}

class DelegationPerm extends Perm {

    public DelegationPerm() {
        super("DelegationPermission", "javax.security.auth.kerberos.DelegationPermission", new String[] {}, null);
    }
}

class FilePerm extends Perm {

    public FilePerm() {
        super("FilePermission", "java.io.FilePermission", new String[] { "<<ALL FILES>>" }, new String[] { "read", "write", "delete", "execute" });
    }
}

class HttpURLPerm extends Perm {

    public HttpURLPerm() {
        super("HttpURLPermission", "java.net.HttpURLPermission", new String[] { "<" + PolicyTool.rb.getString("url") + ">" }, new String[] { "<" + PolicyTool.rb.getString("method.list") + ">:<" + PolicyTool.rb.getString("request.headers.list") + ">" });
    }
}

class InqSecContextPerm extends Perm {

    public InqSecContextPerm() {
        super("InquireSecContextPermission", "com.sun.security.jgss.InquireSecContextPermission", new String[] { "KRB5_GET_SESSION_KEY", "KRB5_GET_TKT_FLAGS", "KRB5_GET_AUTHZ_DATA", "KRB5_GET_AUTHTIME" }, null);
    }
}

class LogPerm extends Perm {

    public LogPerm() {
        super("LoggingPermission", "java.util.logging.LoggingPermission", new String[] { "control" }, null);
    }
}

class MgmtPerm extends Perm {

    public MgmtPerm() {
        super("ManagementPermission", "java.lang.management.ManagementPermission", new String[] { "control", "monitor" }, null);
    }
}

class MBeanPerm extends Perm {

    public MBeanPerm() {
        super("MBeanPermission", "javax.management.MBeanPermission", new String[] {}, new String[] { "addNotificationListener", "getAttribute", "getClassLoader", "getClassLoaderFor", "getClassLoaderRepository", "getDomains", "getMBeanInfo", "getObjectInstance", "instantiate", "invoke", "isInstanceOf", "queryMBeans", "queryNames", "registerMBean", "removeNotificationListener", "setAttribute", "unregisterMBean" });
    }
}

class MBeanSvrPerm extends Perm {

    public MBeanSvrPerm() {
        super("MBeanServerPermission", "javax.management.MBeanServerPermission", new String[] { "createMBeanServer", "findMBeanServer", "newMBeanServer", "releaseMBeanServer" }, null);
    }
}

class MBeanTrustPerm extends Perm {

    public MBeanTrustPerm() {
        super("MBeanTrustPermission", "javax.management.MBeanTrustPermission", new String[] { "register" }, null);
    }
}

class NetPerm extends Perm {

    public NetPerm() {
        super("NetPermission", "java.net.NetPermission", new String[] { "setDefaultAuthenticator", "requestPasswordAuthentication", "specifyStreamHandler", "setProxySelector", "getProxySelector", "setCookieHandler", "getCookieHandler", "setResponseCache", "getResponseCache" }, null);
    }
}

class PrivCredPerm extends Perm {

    public PrivCredPerm() {
        super("PrivateCredentialPermission", "javax.security.auth.PrivateCredentialPermission", new String[] {}, new String[] { "read" });
    }
}

class PropPerm extends Perm {

    public PropPerm() {
        super("PropertyPermission", "java.util.PropertyPermission", new String[] {}, new String[] { "read", "write" });
    }
}

class ReflectPerm extends Perm {

    public ReflectPerm() {
        super("ReflectPermission", "java.lang.reflect.ReflectPermission", new String[] { "suppressAccessChecks" }, null);
    }
}

class RuntimePerm extends Perm {

    public RuntimePerm() {
        super("RuntimePermission", "java.lang.RuntimePermission", new String[] { "createClassLoader", "getClassLoader", "setContextClassLoader", "enableContextClassLoaderOverride", "setSecurityManager", "createSecurityManager", "getenv.<" + PolicyTool.rb.getString("environment.variable.name") + ">", "exitVM", "shutdownHooks", "setFactory", "setIO", "modifyThread", "stopThread", "modifyThreadGroup", "getProtectionDomain", "readFileDescriptor", "writeFileDescriptor", "loadLibrary.<" + PolicyTool.rb.getString("library.name") + ">", "accessClassInPackage.<" + PolicyTool.rb.getString("package.name") + ">", "defineClassInPackage.<" + PolicyTool.rb.getString("package.name") + ">", "accessDeclaredMembers", "queuePrintJob", "getStackTrace", "setDefaultUncaughtExceptionHandler", "preferences", "usePolicy" }, null);
    }
}

class SecurityPerm extends Perm {

    public SecurityPerm() {
        super("SecurityPermission", "java.security.SecurityPermission", new String[] { "createAccessControlContext", "getDomainCombiner", "getPolicy", "setPolicy", "createPolicy.<" + PolicyTool.rb.getString("policy.type") + ">", "getProperty.<" + PolicyTool.rb.getString("property.name") + ">", "setProperty.<" + PolicyTool.rb.getString("property.name") + ">", "insertProvider.<" + PolicyTool.rb.getString("provider.name") + ">", "removeProvider.<" + PolicyTool.rb.getString("provider.name") + ">", "clearProviderProperties.<" + PolicyTool.rb.getString("provider.name") + ">", "putProviderProperty.<" + PolicyTool.rb.getString("provider.name") + ">", "removeProviderProperty.<" + PolicyTool.rb.getString("provider.name") + ">" }, null);
    }
}

class SerialPerm extends Perm {

    public SerialPerm() {
        super("SerializablePermission", "java.io.SerializablePermission", new String[] { "enableSubclassImplementation", "enableSubstitution" }, null);
    }
}

class ServicePerm extends Perm {

    public ServicePerm() {
        super("ServicePermission", "javax.security.auth.kerberos.ServicePermission", new String[] {}, new String[] { "initiate", "accept" });
    }
}

class SocketPerm extends Perm {

    public SocketPerm() {
        super("SocketPermission", "java.net.SocketPermission", new String[] {}, new String[] { "accept", "connect", "listen", "resolve" });
    }
}

class SQLPerm extends Perm {

    public SQLPerm() {
        super("SQLPermission", "java.sql.SQLPermission", new String[] { "setLog", "callAbort", "setSyncFactory", "setNetworkTimeout" }, null);
    }
}

class SSLPerm extends Perm {

    public SSLPerm() {
        super("SSLPermission", "javax.net.ssl.SSLPermission", new String[] { "setHostnameVerifier", "getSSLSessionContext" }, null);
    }
}

class SubjDelegPerm extends Perm {

    public SubjDelegPerm() {
        super("SubjectDelegationPermission", "javax.management.remote.SubjectDelegationPermission", new String[] {}, null);
    }
}