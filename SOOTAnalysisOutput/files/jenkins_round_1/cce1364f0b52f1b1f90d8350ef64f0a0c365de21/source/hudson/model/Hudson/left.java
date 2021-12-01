/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Erik Ramfelt, Koichi Fujikawa, Red Hat, Inc., Seiji Sogabe,
 * Stephen Connolly, Tom Huybrechts, Yahoo! Inc., Alan Harder, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.model;

import hudson.ExtensionListView;
import hudson.Functions;
import hudson.Platform;
import hudson.PluginManager;
import hudson.cli.declarative.CLIResolver;
import hudson.model.listeners.ItemListener;
import hudson.slaves.ComputerListener;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jvnet.hudson.reactor.ReactorException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static hudson.Util.fixEmpty;

public class Hudson extends Jenkins {

    /**
     * List of registered {@link hudson.model.listeners.ItemListener}s.
     * @deprecated as of 1.286
     */
    private transient final CopyOnWriteList<ItemListener> itemListeners = ExtensionListView.createCopyOnWriteList(ItemListener.class);

    /**
    * List of registered {@link hudson.slaves.ComputerListener}s.
     * @deprecated as of 1.286
     */
    private transient final CopyOnWriteList<ComputerListener> computerListeners = ExtensionListView.createCopyOnWriteList(ComputerListener.class);


    @CLIResolver
    public static Hudson getInstance() {
        return (Hudson)Jenkins.getInstance();
    }

    public Hudson(File root, ServletContext context) throws IOException, InterruptedException, ReactorException {
        this(root,context,null);
    }

    public Hudson(File root, ServletContext context, PluginManager pluginManager) throws IOException, InterruptedException, ReactorException {
        super(root, context, pluginManager);
    }

    /**
     * Gets all the installed {@link ItemListener}s.
     *
     * @deprecated as of 1.286.
     *      Use {@link ItemListener#all()}.
     */
    public CopyOnWriteList<ItemListener> getJobListeners() {
        return itemListeners;
    }

    /**
     * Gets all the installed {@link ComputerListener}s.
     *
     * @deprecated as of 1.286.
     *      Use {@link ComputerListener#all()}.
     */
    public CopyOnWriteList<ComputerListener> getComputerListeners() {
        return computerListeners;
    }

    /**
     * Gets the slave node of the give name, hooked under this Hudson.
     *
     * @deprecated
     *      Use {@link #getNode(String)}. Since 1.252.
     */
    public Slave getSlave(String name) {
        Node n = getNode(name);
        if (n instanceof Slave)
            return (Slave)n;
        return null;
    }

    /**
     * @deprecated
     *      Use {@link #getNodes()}. Since 1.252.
     */
    public List<Slave> getSlaves() {
        return (List)Collections.unmodifiableList(slaves);
    }

    /**
     * Updates the slave list.
     *
     * @deprecated
     *      Use {@link #setNodes(List)}. Since 1.252.
     */
    public void setSlaves(List<Slave> slaves) throws IOException {
        setNodes(slaves);
    }

    /**
     * @deprecated
     *      Left only for the compatibility of URLs.
     *      Should not be invoked for any other purpose.
     */
    public TopLevelItem getJob(String name) {
        return getItem(name);
    }

    /**
     * @deprecated
     *      Used only for mapping jobs to URL in a case-insensitive fashion.
     */
    public TopLevelItem getJobCaseInsensitive(String name) {
        String match = Functions.toEmailSafeString(name);
        for(TopLevelItem item : getItems()) {
            if(Functions.toEmailSafeString(item.getName()).equalsIgnoreCase(match)) {
        return item;
    }
                }
        return null;
    }

    /**
     * @deprecated as of 1.317
     *      Use {@link #doQuietDown()} instead.
     */
    public synchronized void doQuietDown(StaplerResponse rsp) throws IOException, ServletException {
        doQuietDown().generateResponse(null, rsp, this);
    }

    /**
     * RSS feed for log entries.
     *
     * @deprecated
     *   As on 1.267, moved to "/log/rss..."
     */
    public void doLogRss( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        String qs = req.getQueryString();
        rsp.sendRedirect2("./log/rss"+(qs==null?"":'?'+qs));
    }

    /**
     * @deprecated as of 1.294
     *      Define your own check method, instead of relying on this generic one.
     */
    public void doFieldCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        doFieldCheck(
                fixEmpty(req.getParameter("value")),
                fixEmpty(req.getParameter("type")),
                fixEmpty(req.getParameter("errorText")),
                fixEmpty(req.getParameter("warningText"))).generateResponse(req,rsp,this);
    }

    /**
     * Checks if the value for a field is set; if not an error or warning text is displayed.
     * If the parameter "value" is not set then the parameter "errorText" is displayed
     * as an error text. If the parameter "errorText" is not set, then the parameter "warningText"
     * is displayed as a warning text.
     * <p>
     * If the text is set and the parameter "type" is set, it will validate that the value is of the
     * correct type. Supported types are "number, "number-positive" and "number-negative".
     *
     * @deprecated as of 1.324
     *      Either use client-side validation (e.g. class="required number")
     *      or define your own check method, instead of relying on this generic one.
     */
    public FormValidation doFieldCheck(@QueryParameter(fixEmpty=true) String value,
                                       @QueryParameter(fixEmpty=true) String type,
                                       @QueryParameter(fixEmpty=true) String errorText,
                                       @QueryParameter(fixEmpty=true) String warningText) {
        if (value == null) {
            if (errorText != null)
                return FormValidation.error(errorText);
            if (warningText != null)
                return FormValidation.warning(warningText);
            return FormValidation.error("No error or warning text was set for fieldCheck().");
        }

        if (type != null) {
            try {
                if (type.equalsIgnoreCase("number")) {
                    NumberFormat.getInstance().parse(value);
                } else if (type.equalsIgnoreCase("number-positive")) {
                    if (NumberFormat.getInstance().parse(value).floatValue() <= 0)
                        return FormValidation.error(Messages.Hudson_NotAPositiveNumber());
                } else if (type.equalsIgnoreCase("number-negative")) {
                    if (NumberFormat.getInstance().parse(value).floatValue() >= 0)
                        return FormValidation.error(Messages.Hudson_NotANegativeNumber());
                }
            } catch (ParseException e) {
                return FormValidation.error(Messages.Hudson_NotANumber());
            }
        }

        return FormValidation.ok();
    }

    /**
     * @deprecated
     *      Use {@link Functions#isWindows()}.
     */
    public static boolean isWindows() {
        return File.pathSeparatorChar==';';
    }

    /**
     * @deprecated
     *      Use {@link hudson.Platform#isDarwin()}
     */
    public static boolean isDarwin() {
        return Platform.isDarwin();
    }

    /**
     * @deprecated since 2007-12-18.
     *      Use {@link #checkPermission(hudson.security.Permission)}
     */
    public static boolean adminCheck() throws IOException {
        return adminCheck(Stapler.getCurrentRequest(), Stapler.getCurrentResponse());
    }

    /**
     * @deprecated since 2007-12-18.
     *      Use {@link #checkPermission(hudson.security.Permission)}
     */
    public static boolean adminCheck(StaplerRequest req,StaplerResponse rsp) throws IOException {
        if (isAdmin(req)) return true;

        rsp.sendError(StaplerResponse.SC_FORBIDDEN);
        return false;
    }

    /**
     * Checks if the current user (for which we are processing the current request)
     * has the admin access.
     *
     * @deprecated since 2007-12-18.
     *      This method is deprecated when Hudson moved from simple Unix root-like model
     *      of "admin gets to do everything, and others don't have any privilege" to more
     *      complex {@link hudson.security.ACL} and {@link hudson.security.Permission} based scheme.
     *
     *      <p>
     *      For a quick migration, use {@code Hudson.getInstance().getACL().hasPermission(Hudson.ADMINISTER)}
     *      To check if the user has the 'administer' role in Hudson.
     *
     *      <p>
     *      But ideally, your plugin should first identify a suitable {@link hudson.security.Permission} (or create one,
     *      if appropriate), then identify a suitable {@link hudson.security.AccessControlled} object to check its permission
     *      against.
     */
    public static boolean isAdmin() {
        return Jenkins.getInstance().getACL().hasPermission(ADMINISTER);
    }

    /**
     * @deprecated since 2007-12-18.
     *      Define a custom {@link hudson.security.Permission} and check against ACL.
     *      See {@link #isAdmin()} for more instructions.
     */
    public static boolean isAdmin(StaplerRequest req) {
        return isAdmin();
    }

    static {
        XSTREAM.alias("hudson",Hudson.class);
    }

    /**
     * @deprecated  only here for backward comp
     */
    public static final class MasterComputer extends Jenkins.MasterComputer {
        // no op
    }

    /**
     * @deprecated  only here for backward comp
     */
    public static class CloudList extends Jenkins.CloudList {
        // no op
    }
}
