package hudson.model;

import hudson.BulkChange;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.Initializer;
import hudson.model.Descriptor.FormException;
import hudson.model.listeners.SaveableListener;
import hudson.node_monitors.NodeMonitor;
import hudson.slaves.NodeDescriptor;
import hudson.triggers.SafeTimerTask;
import hudson.util.DescribableList;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu.ContextMenu;
import jenkins.util.Timer;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import static hudson.init.InitMilestone.JOB_LOADED;

@ExportedBean
public final class ComputerSet extends AbstractModelObject implements Describable<ComputerSet>, ModelObjectWithChildren {

    private static final Saveable MONITORS_OWNER = new Saveable() {

        public void save() throws IOException {
            getConfigFile().write(monitors);
            SaveableListener.fireOnChange(this, getConfigFile());
        }
    };

    private static final DescribableList<NodeMonitor, Descriptor<NodeMonitor>> monitors = new DescribableList<>(MONITORS_OWNER);

    @Exported
    public String getDisplayName() {
        return Messages.ComputerSet_DisplayName();
    }

    @Deprecated
    public static List<NodeMonitor> get_monitors() {
        return monitors.toList();
    }

    @Exported(name = "computer", inline = true)
    public Computer[] get_all() {
        return Jenkins.getInstance().getComputers();
    }

    public ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        ContextMenu m = new ContextMenu();
        for (Computer c : get_all()) {
            m.add(c);
        }
        return m;
    }

    public DescriptorExtensionList<NodeMonitor, Descriptor<NodeMonitor>> getNodeMonitorDescriptors() {
        return NodeMonitor.all();
    }

    public static DescribableList<NodeMonitor, Descriptor<NodeMonitor>> getMonitors() {
        return monitors;
    }

    public static Map<Descriptor<NodeMonitor>, NodeMonitor> getNonIgnoredMonitors() {
        Map<Descriptor<NodeMonitor>, NodeMonitor> r = new HashMap<>();
        for (NodeMonitor m : monitors) {
            if (!m.isIgnored())
                r.put(m.getDescriptor(), m);
        }
        return r;
    }

    public List<String> get_slaveNames() {
        return new AbstractList<String>() {

            final List<Node> nodes = Jenkins.getInstance().getNodes();

            public String get(int index) {
                return nodes.get(index).getNodeName();
            }

            public int size() {
                return nodes.size();
            }
        };
    }

    @Exported
    public int getTotalExecutors() {
        int r = 0;
        for (Computer c : get_all()) {
            if (c.isOnline())
                r += c.countExecutors();
        }
        return r;
    }

    @Exported
    public int getBusyExecutors() {
        int r = 0;
        for (Computer c : get_all()) {
            if (c.isOnline())
                r += c.countBusy();
        }
        return r;
    }

    public int getIdleExecutors() {
        int r = 0;
        for (Computer c : get_all()) if ((c.isOnline() || c.isConnecting()) && c.isAcceptingTasks())
            r += c.countIdle();
        return r;
    }

    public String getSearchUrl() {
        return "/computers/";
    }

    public Computer getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        return Jenkins.getInstance().getComputer(token);
    }

    @RequirePOST
    public void do_launchAll(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        for (Computer c : get_all()) {
            if (c.isLaunchSupported())
                c.connect(true);
        }
        rsp.sendRedirect(".");
    }

    @RequirePOST
    public void doUpdateNow(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        for (NodeMonitor nodeMonitor : NodeMonitor.getAll()) {
            Thread t = nodeMonitor.triggerUpdate();
            String columnCaption = nodeMonitor.getColumnCaption();
            if (columnCaption != null) {
                t.setName(columnCaption);
            }
        }
        rsp.forwardToPreviousPage(req);
    }

    @RequirePOST
    public synchronized void doCreateItem(StaplerRequest req, StaplerResponse rsp, @QueryParameter String name, @QueryParameter String mode, @QueryParameter String from) throws IOException, ServletException {
        final Jenkins app = Jenkins.getInstance();
        app.checkPermission(Computer.CREATE);
        if (mode != null && mode.equals("copy")) {
            name = checkName(name);
            Node src = app.getNode(from);
            if (src == null) {
                if (Util.fixEmpty(from) == null) {
                    throw new Failure(Messages.ComputerSet_SpecifySlaveToCopy());
                } else {
                    throw new Failure(Messages.ComputerSet_NoSuchSlave(from));
                }
            }
            String xml = Jenkins.XSTREAM.toXML(src);
            Node result = (Node) Jenkins.XSTREAM.fromXML(xml);
            result.setNodeName(name);
            if (result instanceof Slave) {
                User user = User.current();
                ((Slave) result).setUserId(user == null ? "anonymous" : user.getId());
            }
            result.holdOffLaunchUntilSave = true;
            app.addNode(result);
            rsp.sendRedirect2(result.getNodeName() + "/configure");
        } else {
            if (mode == null) {
                throw new Failure("No mode given");
            }
            NodeDescriptor d = NodeDescriptor.all().findByName(mode);
            if (d == null) {
                throw new Failure("No node type ‘" + mode + "’ is known");
            }
            d.handleNewNodePage(this, name, req, rsp);
        }
    }

    @RequirePOST
    public synchronized void doDoCreateItem(StaplerRequest req, StaplerResponse rsp, @QueryParameter String name, @QueryParameter String type) throws IOException, ServletException, FormException {
        final Jenkins app = Jenkins.getInstance();
        app.checkPermission(Computer.CREATE);
        String fixedName = Util.fixEmptyAndTrim(name);
        checkName(fixedName);
        JSONObject formData = req.getSubmittedForm();
        formData.put("name", fixedName);
        Node result = NodeDescriptor.all().find(type).newInstance(req, formData);
        app.addNode(result);
        rsp.sendRedirect2(".");
    }

    public String checkName(String name) throws Failure {
        if (name == null)
            throw new Failure("Query parameter 'name' is required");
        name = name.trim();
        Jenkins.checkGoodName(name);
        if (Jenkins.getInstance().getNode(name) != null)
            throw new Failure(Messages.ComputerSet_SlaveAlreadyExists(name));
        return name;
    }

    public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
        Jenkins.getInstance().checkPermission(Computer.CREATE);
        if (Util.fixEmpty(value) == null)
            return FormValidation.ok();
        try {
            checkName(value);
            return FormValidation.ok();
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }
    }

    @RequirePOST
    public synchronized HttpResponse doConfigSubmit(StaplerRequest req) throws IOException, ServletException, FormException {
        BulkChange bc = new BulkChange(MONITORS_OWNER);
        try {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            monitors.rebuild(req, req.getSubmittedForm(), getNodeMonitorDescriptors());
            for (Descriptor<NodeMonitor> d : NodeMonitor.all()) if (monitors.get(d) == null) {
                NodeMonitor i = createDefaultInstance(d, true);
                if (i != null)
                    monitors.add(i);
            }
            for (NodeMonitor nm : monitors) {
                nm.triggerUpdate();
            }
            return FormApply.success(".");
        } finally {
            bc.commit();
        }
    }

    private static XmlFile getConfigFile() {
        return new XmlFile(new File(Jenkins.getInstance().getRootDir(), "nodeMonitors.xml"));
    }

    public Api getApi() {
        return new Api(this);
    }

    public Descriptor<ComputerSet> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(ComputerSet.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerSet> {

        public AutoCompletionCandidates doAutoCompleteCopyNewItemFrom(@QueryParameter final String value) {
            final AutoCompletionCandidates r = new AutoCompletionCandidates();
            for (Node n : Jenkins.getInstance().getNodes()) {
                if (n.getNodeName().startsWith(value))
                    r.add(n.getNodeName());
            }
            return r;
        }
    }

    public static void initialize() {
    }

    @Initializer(after = JOB_LOADED)
    public static void init() {
        Timer.get().schedule(new SafeTimerTask() {

            public void doRun() {
                ComputerSet.initialize();
            }
        }, 10, TimeUnit.SECONDS);
    }

    @Nonnull
    public static List<String> getComputerNames() {
        final ArrayList<String> names = new ArrayList<>();
        for (Computer c : Jenkins.getInstance().getComputers()) {
            if (!c.getName().isEmpty()) {
                names.add(c.getName());
            }
        }
        return names;
    }

    private static final Logger LOGGER = Logger.getLogger(ComputerSet.class.getName());

    static {
        try {
            DescribableList<NodeMonitor, Descriptor<NodeMonitor>> r = new DescribableList<>(Saveable.NOOP);
            XmlFile xf = getConfigFile();
            if (xf.exists()) {
                DescribableList<NodeMonitor, Descriptor<NodeMonitor>> persisted = (DescribableList<NodeMonitor, Descriptor<NodeMonitor>>) xf.read();
                List<NodeMonitor> sanitized = new ArrayList<>();
                for (NodeMonitor nm : persisted) {
                    try {
                        nm.getDescriptor();
                        sanitized.add(nm);
                    } catch (Throwable e) {
                    }
                }
                r.replaceBy(sanitized);
            }
            for (Descriptor<NodeMonitor> d : NodeMonitor.all()) if (r.get(d) == null) {
                NodeMonitor i = createDefaultInstance(d, false);
                if (i != null)
                    r.add(i);
            }
            monitors.replaceBy(r.toList());
        } catch (Throwable x) {
            LOGGER.log(Level.WARNING, "Failed to instantiate NodeMonitors", x);
        }
    }

    private static NodeMonitor createDefaultInstance(Descriptor<NodeMonitor> d, boolean ignored) {
        try {
            NodeMonitor nm = d.clazz.newInstance();
            nm.setIgnored(ignored);
            return nm;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Failed to instantiate " + d.clazz, e);
        }
        return null;
    }
}
