/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Kohsuke Kawaguchi, Tom Huybrechts,
 * Yahoo!, Inc.
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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.StreamException;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.Indenter;
import hudson.Util;
import hudson.model.Descriptor.FormException;
import hudson.model.labels.LabelAtomPropertyDescriptor;
import hudson.model.listeners.ItemListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.search.CollectionSearchIndex;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.tasks.UserAvatarResolver;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.AlternativeUiTextProvider.Message;
import hudson.util.DescribableList;
import hudson.util.DescriptorList;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.RunList;
import hudson.util.XStream2;
import hudson.views.ListViewColumn;
import hudson.widgets.Widget;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.model.item_category.Categories;
import jenkins.model.item_category.Category;
import jenkins.model.item_category.ItemCategory;
import jenkins.scm.RunWithSCM;
import jenkins.util.ProgressiveRendering;
import jenkins.util.xml.XMLUtils;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jenkins.scm.RunWithSCM.*;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.xml.sax.SAXException;

/**
 * Encapsulates the rendering of the list of {@link TopLevelItem}s
 * that {@link Jenkins} owns.
 *
 * <p>
 * This is an extension point in Hudson, allowing different kind of
 * rendering to be added as plugins.
 *
 * <h2>Note for implementers</h2>
 * <ul>
 * <li>
 * {@link View} subtypes need the <tt>newViewDetail.jelly</tt> page,
 * which is included in the "new view" page. This page should have some
 * description of what the view is about. 
 * </ul>
 *
 * @author Kohsuke Kawaguchi
 * @see ViewDescriptor
 * @see ViewGroup
 */
@ExportedBean
public abstract class View extends AbstractModelObject implements AccessControlled, Describable<View>, ExtensionPoint, Saveable, ModelObjectWithChildren {

    /**
     * Container of this view. Set right after the construction
     * and never change thereafter.
     */
    protected /*final*/ ViewGroup owner;

    /**
     * Name of this view.
     */
    protected String name;

    /**
     * Message displayed in the view page.
     */
    protected String description;
    
    /**
     * If true, only show relevant executors
     */
    protected boolean filterExecutors;

    /**
     * If true, only show relevant queue items
     */
    protected boolean filterQueue;
    
    protected transient List<Action> transientActions;

    /**
     * List of {@link ViewProperty}s configured for this view.
     * @since 1.406
     */
    private volatile DescribableList<ViewProperty,ViewPropertyDescriptor> properties = new PropertyList(this);

    protected View(String name) {
        this.name = name;
    }

    protected View(String name, ViewGroup owner) {
        this.name = name;
        this.owner = owner;
    }

    /**
     * Gets all the items in this collection in a read-only view.
     */
    @Exported(name="jobs")
    public abstract Collection<TopLevelItem> getItems();

    /**
     * Gets all the items recursively contained in this collection in a read-only view.
     * <p>
     * The default implementation recursively adds the items of all contained Views
     * in case this view implements {@link ViewGroup}, which should be enough for most cases.
     *
     * @since 1.520
     */
    public Collection<TopLevelItem> getAllItems() {

        if (this instanceof ViewGroup) {
            final Collection<TopLevelItem> items = new LinkedHashSet<TopLevelItem>(getItems());

            for(View view: ((ViewGroup) this).getViews()) {
                items.addAll(view.getAllItems());
            }
            return Collections.unmodifiableCollection(items);
        } else {
            return getItems();
        }
    }

    /**
     * Gets the {@link TopLevelItem} of the given name.
     */
    public TopLevelItem getItem(String name) {
        return getOwner().getItemGroup().getItem(name);
    }

    /**
     * Alias for {@link #getItem(String)}. This is the one used in the URL binding.
     */
    public final TopLevelItem getJob(String name) {
        return getItem(name);
    }

    /**
     * Checks if the job is in this collection.
     */
    public abstract boolean contains(TopLevelItem item);

    /**
     * Gets the name of all this collection.
     *
     * @see #rename(String)
     */
    @Exported(visibility=2,name="name")
    @Nonnull
    public String getViewName() {
        return name;
    }

    /**
     * Renames this view.
     */
    public void rename(String newName) throws Failure, FormException {
        if(name.equals(newName))    return; // noop
        Jenkins.checkGoodName(newName);
        if(owner.getView(newName)!=null)
            throw new FormException(Messages.Hudson_ViewAlreadyExists(newName),"name");
        String oldName = name;
        name = newName;
        owner.onViewRenamed(this,oldName,newName);
    }

    /**
     * Gets the {@link ViewGroup} that this view belongs to.
     */
    public ViewGroup getOwner() {
        return owner;
    }

    /** @deprecated call {@link ViewGroup#getItemGroup} directly */
    @Deprecated
    public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() {
        return owner.getItemGroup();
    }

    /** @deprecated call {@link ViewGroup#getPrimaryView} directly */
    @Deprecated
    public View getOwnerPrimaryView() {
        return owner.getPrimaryView();
    }

    /** @deprecated call {@link ViewGroup#getViewActions} directly */
    @Deprecated
    public List<Action> getOwnerViewActions() {
        return owner.getViewActions();
    }

    /**
     * Message displayed in the top page. Can be null. Includes HTML.
     */
    @Exported
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the view properties configured for this view.
     * @since 1.406
     */
    public DescribableList<ViewProperty,ViewPropertyDescriptor> getProperties() {
        // readResolve was the best place to do this, but for compatibility reasons,
        // this class can no longer have readResolve() (the mechanism itself isn't suitable for class hierarchy)
        // see JENKINS-9431
        //
        // until we have that, putting this logic here.
        synchronized (PropertyList.class) {
            if (properties == null) {
                properties = new PropertyList(this);
            } else {
                properties.setOwner(this);
            }
            return properties;
        }
    }

    /**
     * Returns all the {@link LabelAtomPropertyDescriptor}s that can be potentially configured
     * on this label.
     */
    public List<ViewPropertyDescriptor> getApplicablePropertyDescriptors() {
        List<ViewPropertyDescriptor> r = new ArrayList<ViewPropertyDescriptor>();
        for (ViewPropertyDescriptor pd : ViewProperty.all()) {
            if (pd.isEnabledFor(this))
                r.add(pd);
        }
        return r;
    }

    public void save() throws IOException {
        // persistence is a part of the owner
        // due to initialization timing issue, it can be null when this method is called
        if (owner != null) {
            owner.save();
        }
    }

    /**
     * List of all {@link ViewProperty}s exposed primarily for the remoting API.
     * @since 1.406
     */
    @Exported(name="property",inline=true)
    public List<ViewProperty> getAllProperties() {
        return getProperties().toList();
    }

    public ViewDescriptor getDescriptor() {
        return (ViewDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public String getDisplayName() {
        return getViewName();
    }

    public String getNewPronoun() {
        return AlternativeUiTextProvider.get(NEW_PRONOUN, this, Messages.AbstractItem_Pronoun());
    }

    /**
     * By default, return true to render the "Edit view" link on the page.
     * This method is really just for the default "All" view to hide the edit link
     * so that the default Hudson top page remains the same as before 1.316.
     *
     * @since 1.316
     */
    public boolean isEditable() {
        return true;
    }
    
    /**
     * Enables or disables automatic refreshes of the view.
     * By default, automatic refreshes are enabled.
     * @since 1.557
     */
    public boolean isAutomaticRefreshEnabled() {
        return true;
    }
    
    /**
     * If true, only show relevant executors
     */
    public boolean isFilterExecutors() {
        return filterExecutors;
    }
    
    /**
     * If true, only show relevant queue items
     */
    public boolean isFilterQueue() {
        return filterQueue;
    }

    /**
     * Gets the {@link Widget}s registered on this object.
     *
     * <p>
     * For now, this just returns the widgets registered to Hudson.
     */
    public List<Widget> getWidgets() {
        return Collections.unmodifiableList(Jenkins.getInstance().getWidgets());
    }

    /**
     * If this view uses {@code <t:projectView>} for rendering, this method returns columns to be displayed.
     */
    public Iterable<? extends ListViewColumn> getColumns() {
        return ListViewColumn.createDefaultInitialColumnList(this);
    }

    /**
     * If this view uses {@code t:projectView} for rendering, this method returns the indenter used
     * to indent each row.
     */
    public Indenter getIndenter() {
        return null;
    }

    /**
     * If true, this is a view that renders the top page of Hudson.
     */
    public boolean isDefault() {
        return getOwner().getPrimaryView()==this;
    }
    
    public List<Computer> getComputers() {
        Computer[] computers = Jenkins.getInstance().getComputers();

        if (!isFilterExecutors()) {
            return Arrays.asList(computers);
        }

        List<Computer> result = new ArrayList<Computer>();

        HashSet<Label> labels = new HashSet<Label>();
        for (Item item : getItems()) {
            if (item instanceof AbstractProject<?, ?>) {
                labels.addAll(((AbstractProject<?, ?>) item).getRelevantLabels());
            }
        }

        for (Computer c : computers) {
            if (isRelevant(labels, c)) result.add(c);
        }

        return result;
    }

    private boolean isRelevant(Collection<Label> labels, Computer computer) {
        Node node = computer.getNode();
        if (node == null) return false;
        if (labels.contains(null) && node.getMode() == Node.Mode.NORMAL) return true;

        for (Label l : labels)
            if (l != null && l.contains(node))
                return true;
        return false;
    }

    private final static int FILTER_LOOP_MAX_COUNT = 10;

    private List<Queue.Item> filterQueue(List<Queue.Item> base) {
        if (!isFilterQueue()) {
            return base;
        }
        Collection<TopLevelItem> items = getItems();
        return base.stream().filter(qi -> filterQueueItemTest(qi, items))
                .collect(Collectors.toList());
    }

    private boolean filterQueueItemTest(Queue.Item item, Collection<TopLevelItem> viewItems) {
        // Check if the task of parent tasks are in the list of viewItems.
        // Pipeline jobs and other jobs which allow parts require us to
        // check owner tasks as well.
        Queue.Task currentTask = item.task;
        for (int count = 1;; count++) {
            if (viewItems.contains(currentTask)) {
                return true;
            }
            Queue.Task next = currentTask.getOwnerTask();
            if (next == currentTask) {
                break;
            } else {
                currentTask = next;
            }
            if (count == FILTER_LOOP_MAX_COUNT) {
                LOGGER.warning(String.format(
                        "Failed to find root task for queue item '%s' for " +
                        "view '%s' in under %d iterations, aborting!",
                        item.getDisplayName(), getDisplayName(),
                        FILTER_LOOP_MAX_COUNT));
                break;
            }
        }
        // Check root project for sub-job projects (e.g. matrix jobs).
        if (item.task instanceof AbstractProject<?, ?>) {
            AbstractProject<?,?> project = (AbstractProject<?, ?>) item.task;
            if (viewItems.contains(project.getRootProject())) {
                return true;
            }
        }
        return false;
    }

    public List<Queue.Item> getQueueItems() {
        return filterQueue(Arrays.asList(Jenkins.getInstance().getQueue().getItems()));
    }

    /**
     * @deprecated Use {@link #getQueueItems()}. As of 1.607 the approximation is no longer needed.
     * @return The items in the queue.
     */
    @Deprecated
    public List<Queue.Item> getApproximateQueueItemsQuickly() {
        return filterQueue(Jenkins.getInstance().getQueue().getApproximateItemsQuickly());
    }

    /**
     * Returns the path relative to the context root.
     *
     * Doesn't start with '/' but ends with '/' (except returns
     * empty string when this is the default view).
     */
    public String getUrl() {
        return isDefault() ? (owner!=null ? owner.getUrl() : "") : getViewUrl();
    }

    /**
     * Same as {@link #getUrl()} except this returns a view/{name} path
     * even for the default view.
     */
    public String getViewUrl() {
        return (owner!=null ? owner.getUrl() : "") + "view/" + Util.rawEncode(getViewName()) + '/';
    }

    @Override public String toString() {
        return super.toString() + "[" + getViewUrl() + "]";
    }

    public String getSearchUrl() {
        return getUrl();
    }

    /**
     * Returns the transient {@link Action}s associated with the top page.
     *
     * <p>
     * If views don't want to show top-level actions, this method
     * can be overridden to return different objects.
     *
     * @see Jenkins#getActions()
     */
    public List<Action> getActions() {
    	List<Action> result = new ArrayList<Action>();
    	result.addAll(getOwner().getViewActions());
    	synchronized (this) {
    		if (transientActions == null) {
                updateTransientActions();
    		}
    		result.addAll(transientActions);
    	}
    	return result;
    }
    
    public synchronized void updateTransientActions() {
        transientActions = TransientViewActionFactory.createAllFor(this); 
    }
    
    public Object getDynamic(String token) {
        for (Action a : getActions()) {
            String url = a.getUrlName();
            if (url==null)  continue;
            if (url.equals(token))
                return a;
        }
        return null;
    }

    /**
     * Gets the absolute URL of this view.
     */
    @Exported(visibility=2,name="url")
    public String getAbsoluteUrl() {
        return Jenkins.getInstance().getRootUrl()+getUrl();
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the page to redirect the user to, after the view is created.
     *
     * The returned string is appended to "/view/foobar/", so for example
     * to direct the user to the top page of the view, return "", etc.
     */
    public String getPostConstructLandingPage() {
        return "configure";
    }

    /**
     * Returns the {@link ACL} for this object.
     */
    public ACL getACL() {
        return Jenkins.getInstance().getAuthorizationStrategy().getACL(this);
    }

    /** @deprecated Does not work properly with moved jobs. Use {@link ItemListener#onLocationChanged} instead. */
    @Deprecated
    public void onJobRenamed(Item item, String oldName, String newName) {}

    @ExportedBean(defaultVisibility=2)
    public static final class UserInfo implements Comparable<UserInfo> {
        private final User user;
        /**
         * When did this user made a last commit on any of our projects? Can be null.
         */
        private Calendar lastChange;
        /**
         * Which project did this user commit? Can be null.
         */
        private Job<?,?> project;

        /** @see UserAvatarResolver */
        String avatar;

        UserInfo(User user, Job<?,?> p, Calendar lastChange) {
            this.user = user;
            this.project = p;
            this.lastChange = lastChange;
        }

        @Exported
        public User getUser() {
            return user;
        }

        @Exported
        public Calendar getLastChange() {
            return lastChange;
        }

        @Deprecated
        public AbstractProject getProject() {
            return project instanceof AbstractProject ? (AbstractProject)project : null;
        }

        @Exported(name="project")
        public Job<?,?> getJob() {
            return project;
        }

        /**
         * Returns a human-readable string representation of when this user was last active.
         */
        public String getLastChangeTimeString() {
            if(lastChange==null)    return "N/A";
            long duration = new GregorianCalendar().getTimeInMillis()- ordinal();
            return Util.getTimeSpanString(duration);
        }

        public String getTimeSortKey() {
            if(lastChange==null)    return "-";
            return Util.XS_DATETIME_FORMATTER.format(lastChange.getTime());
        }

        public int compareTo(UserInfo that) {
            long rhs = that.ordinal();
            long lhs = this.ordinal();
            if(rhs>lhs) return 1;
            if(rhs<lhs) return -1;
            return 0;
        }

        private long ordinal() {
            if(lastChange==null)    return 0;
            return lastChange.getTimeInMillis();
        }
    }

    /**
     * Does this {@link View} has any associated user information recorded?
     * @deprecated Potentially very expensive call; do not use from Jelly views.
     */
    @Deprecated
    public boolean hasPeople() {
        return People.isApplicable(getItems());
    }

    /**
     * Gets the users that show up in the changelog of this job collection.
     */
    public People getPeople() {
        return new People(this);
    }

    /**
     * @since 1.484
     */
    public AsynchPeople getAsynchPeople() {
        return new AsynchPeople(this);
    }

    @ExportedBean
    public static final class People  {
        @Exported
        public final List<UserInfo> users;

        public final ModelObject parent;

        public People(Jenkins parent) {
            this.parent = parent;
            // for Hudson, really load all users
            Map<User,UserInfo> users = getUserInfo(parent.getItems());
            User unknown = User.getUnknown();
            for (User u : User.getAll()) {
                if(u==unknown)  continue;   // skip the special 'unknown' user
                if(!users.containsKey(u))
                    users.put(u,new UserInfo(u,null,null));
            }
            this.users = toList(users);
        }

        public People(View parent) {
            this.parent = parent;
            this.users = toList(getUserInfo(parent.getItems()));
        }

        private Map<User,UserInfo> getUserInfo(Collection<? extends Item> items) {
            Map<User,UserInfo> users = new HashMap<User,UserInfo>();
            for (Item item : items) {
                for (Job<?, ?> job : item.getAllJobs()) {
                    RunList<? extends Run<?, ?>> runs = job.getBuilds();
                    for (Run<?, ?> r : runs) {
                        if (r instanceof RunWithSCM) {
                            RunWithSCM<?,?> runWithSCM = (RunWithSCM<?,?>) r;

                            for (ChangeLogSet<? extends Entry> c : runWithSCM.getChangeSets()) {
                                for (Entry entry : c) {
                                    User user = entry.getAuthor();

                                    UserInfo info = users.get(user);
                                    if (info == null)
                                        users.put(user, new UserInfo(user, job, r.getTimestamp()));
                                    else if (info.getLastChange().before(r.getTimestamp())) {
                                        info.project = job;
                                        info.lastChange = r.getTimestamp();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return users;
        }

        private List<UserInfo> toList(Map<User,UserInfo> users) {
            ArrayList<UserInfo> list = new ArrayList<UserInfo>();
            list.addAll(users.values());
            Collections.sort(list);
            return Collections.unmodifiableList(list);
        }

        public Api getApi() {
            return new Api(this);
        }

        /**
         * @deprecated Potentially very expensive call; do not use from Jelly views.
         */
        @Deprecated
        public static boolean isApplicable(Collection<? extends Item> items) {
            for (Item item : items) {
                for (Job job : item.getAllJobs()) {
                    RunList<? extends Run<?, ?>> runs = job.getBuilds();

                    for (Run<?,?> r : runs) {
                        if (r instanceof RunWithSCM) {
                            RunWithSCM<?,?> runWithSCM = (RunWithSCM<?,?>) r;
                            for (ChangeLogSet<? extends Entry> c : runWithSCM.getChangeSets()) {
                                for (Entry entry : c) {
                                    User user = entry.getAuthor();
                                    if (user != null)
                                        return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * Variant of {@link People} which can be displayed progressively, since it may be slow.
     * @since 1.484
     */
    public static final class AsynchPeople extends ProgressiveRendering { // JENKINS-15206

        private final Collection<TopLevelItem> items;
        private final User unknown;
        private final Map<User,UserInfo> users = new HashMap<User,UserInfo>();
        private final Set<User> modified = new HashSet<User>();
        private final String iconSize;
        public final ModelObject parent;

        /** @see Jenkins#getAsynchPeople */
        public AsynchPeople(Jenkins parent) {
            this.parent = parent;
            items = parent.getItems();
            unknown = User.getUnknown();
        }

        /** @see View#getAsynchPeople */
        public AsynchPeople(View parent) {
            this.parent = parent;
            items = parent.getItems();
            unknown = null;
        }

        {
            StaplerRequest req = Stapler.getCurrentRequest();
            iconSize = req != null ? Functions.validateIconSize(Functions.getCookie(req, "iconSize", "32x32")) : "32x32";
        }

        @Override protected void compute() throws Exception {
            int itemCount = 0;
            for (Item item : items) {
                for (Job<?,?> job : item.getAllJobs()) {
                    RunList<? extends Run<?, ?>> builds = job.getBuilds();
                    int buildCount = 0;
                    for (Run<?, ?> r : builds) {
                        if (canceled()) {
                            return;
                        }
                        if (!(r instanceof RunWithSCM)) {
                            continue;
                        }

                        RunWithSCM<?, ?> runWithSCM = (RunWithSCM<?, ?>) r;
                        for (ChangeLogSet<? extends ChangeLogSet.Entry> c : runWithSCM.getChangeSets()) {
                            for (ChangeLogSet.Entry entry : c) {
                                User user = entry.getAuthor();
                                UserInfo info = users.get(user);
                                if (info == null) {
                                    UserInfo userInfo = new UserInfo(user, job, r.getTimestamp());
                                    userInfo.avatar = UserAvatarResolver.resolveOrNull(user, iconSize);
                                    synchronized (this) {
                                        users.put(user, userInfo);
                                        modified.add(user);
                                    }
                                } else if (info.getLastChange().before(r.getTimestamp())) {
                                    synchronized (this) {
                                        info.project = job;
                                        info.lastChange = r.getTimestamp();
                                        modified.add(user);
                                    }
                                }
                            }
                        }
                        // TODO consider also adding the user of the UserCause when applicable
                        buildCount++;
                        // TODO this defeats lazy-loading. Should rather do a breadth-first search, as in hudson.plugins.view.dashboard.builds.LatestBuilds
                        // (though currently there is no quick implementation of RunMap.size() ~ idOnDisk.size(), which would be needed for proper progress)
                        progress((itemCount + 1.0 * buildCount / builds.size()) / (items.size() + 1));
                    }
                }
                itemCount++;
                progress(1.0 * itemCount / (items.size() + /* handling User.getAll */1));
            }
            if (unknown != null) {
                if (canceled()) {
                    return;
                }
                for (User u : User.getAll()) { // TODO nice to have a method to iterate these lazily
                    if (canceled()) {
                        return;
                    }
                    if (u == unknown) {
                        continue;
                    }
                    if (!users.containsKey(u)) {
                        UserInfo userInfo = new UserInfo(u, null, null);
                        userInfo.avatar = UserAvatarResolver.resolveOrNull(u, iconSize);
                        synchronized (this) {
                            users.put(u, userInfo);
                            modified.add(u);
                        }
                    }
                }
            }
        }

        @Override protected synchronized JSON data() {
            JSONArray r = new JSONArray();
            for (User u : modified) {
                UserInfo i = users.get(u);
                JSONObject entry = new JSONObject().
                        accumulate("id", u.getId()).
                        accumulate("fullName", u.getFullName()).
                        accumulate("url", u.getUrl()).
                        accumulate("avatar", i.avatar != null ? i.avatar : Stapler.getCurrentRequest().getContextPath() + Functions.getResourcePath() + "/images/" + iconSize + "/user.png").
                        accumulate("timeSortKey", i.getTimeSortKey()).
                        accumulate("lastChangeTimeString", i.getLastChangeTimeString());
                Job<?,?> p = i.getJob();
                if (p != null) {
                    entry.accumulate("projectUrl", p.getUrl()).accumulate("projectFullDisplayName", p.getFullDisplayName());
                }
                r.add(entry);
            }
            modified.clear();
            return r;
        }

        public Api getApi() {
            return new Api(new People());
        }

        /** JENKINS-16397 workaround */
        @Restricted(NoExternalUse.class)
        @ExportedBean
        public final class People {

            private View.People people;

            @Exported public synchronized List<UserInfo> getUsers() {
                if (people == null) {
                    people = parent instanceof Jenkins ? new View.People((Jenkins) parent) : new View.People((View) parent);
                }
                return people.users;
            }
        }

    }

    void addDisplayNamesToSearchIndex(SearchIndexBuilder sib, Collection<TopLevelItem> items) {
        for(TopLevelItem item : items) {
            
            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine((String.format("Adding url=%s,displayName=%s",
                            item.getSearchUrl(), item.getDisplayName())));
            }
            sib.add(item.getSearchUrl(), item.getDisplayName());
        }        
    }
    
    @Override
    public SearchIndexBuilder makeSearchIndex() {
        SearchIndexBuilder sib = super.makeSearchIndex();
        sib.add(new CollectionSearchIndex<TopLevelItem>() {// for jobs in the view
                protected TopLevelItem get(String key) { return getItem(key); }
                protected Collection<TopLevelItem> all() { return getItems(); }
                @Override
                protected String getName(TopLevelItem o) {
                    // return the name instead of the display for suggestion searching
                    return o.getName();
                }
            });
        
        // add the display name for each item in the search index
        addDisplayNamesToSearchIndex(sib, getItems());

        return sib;
    }

    /**
     * Accepts the new description.
     */
    @RequirePOST
    public synchronized void doSubmitDescription( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        checkPermission(CONFIGURE);

        description = req.getParameter("description");
        save();
        rsp.sendRedirect(".");  // go to the top page
    }

    /**
     * Accepts submission from the configuration page.
     *
     * Subtypes should override the {@link #submit(StaplerRequest)} method.
     */
    @RequirePOST
    public final synchronized void doConfigSubmit( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        checkPermission(CONFIGURE);

        submit(req);

        description = Util.nullify(req.getParameter("description"));
        filterExecutors = req.getParameter("filterExecutors") != null;
        filterQueue = req.getParameter("filterQueue") != null;

        rename(req.getParameter("name"));

        getProperties().rebuild(req, req.getSubmittedForm(), getApplicablePropertyDescriptors());
        updateTransientActions();  

        save();

        FormApply.success("../" + Util.rawEncode(name)).generateResponse(req,rsp,this);
    }

    /**
     * Handles the configuration submission.
     *
     * Load view-specific properties here.
     */
    protected abstract void submit(StaplerRequest req) throws IOException, ServletException, FormException;

    /**
     * Deletes this view.
     */
    @RequirePOST
    public synchronized void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(DELETE);

        owner.deleteView(this);

        rsp.sendRedirect2(req.getContextPath()+"/" + owner.getUrl());
    }


    /**
     * Creates a new {@link Item} in this collection.
     *
     * <p>
     * This method should call {@link ModifiableItemGroup#doCreateItem(StaplerRequest, StaplerResponse)}
     * and then add the newly created item to this view.
     * 
     * @return
     *      null if fails.
     */
    public abstract Item doCreateItem( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException;

    /**
     * Makes sure that the given name is good as a job name.
     * For use from {@code newJob}.
     */
    @Restricted(DoNotUse.class) // called from newJob view
    public FormValidation doCheckJobName(@QueryParameter String value) {
        // this method can be used to check if a file exists anywhere in the file system,
        // so it should be protected.
        getOwner().checkPermission(Item.CREATE);

        if (Util.fixEmpty(value) == null) {
            return FormValidation.ok();
        }

        try {
            Jenkins.checkGoodName(value);
            value = value.trim(); // why trim *after* checkGoodName? not sure, but ItemGroupMixIn.createTopLevelItem does the same
            Jenkins.getInstance().getProjectNamingStrategy().checkName(value);
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }

        if (getOwner().getItemGroup().getItem(value) != null) {
            return FormValidation.error(Messages.Hudson_JobAlreadyExists(value));
        }

        // looks good
        return FormValidation.ok();
    }

    /**
     * An API REST method to get the allowed {$link TopLevelItem}s and its categories.
     *
     * @return A {@link Categories} entity that is shown as JSON file.
     */
    @Restricted(DoNotUse.class)
    public Categories doItemCategories(StaplerRequest req, StaplerResponse rsp, @QueryParameter String iconStyle) throws IOException, ServletException {
        getOwner().checkPermission(Item.CREATE);

        rsp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        rsp.addHeader("Pragma", "no-cache");
        rsp.addHeader("Expires", "0");
        Categories categories = new Categories();
        int order = 0;
        JellyContext ctx;

        if (StringUtils.isNotBlank(iconStyle)) {
            ctx = new JellyContext();
            ctx.setVariable("resURL", req.getContextPath() + Jenkins.RESOURCE_PATH);
        } else {
            ctx = null;
        }
        for (TopLevelItemDescriptor descriptor : DescriptorVisibilityFilter.apply(getOwner().getItemGroup(), Items.all(Jenkins.getAuthentication(), getOwner().getItemGroup()))) {
            ItemCategory ic = ItemCategory.getCategory(descriptor);
            Map<String, Serializable> metadata = new HashMap<String, Serializable>();

            // Information about Item.
            metadata.put("class", descriptor.getId());
            metadata.put("order", ++order);
            metadata.put("displayName", descriptor.getDisplayName());
            metadata.put("description", descriptor.getDescription());
            metadata.put("iconFilePathPattern", descriptor.getIconFilePathPattern());
            String iconClassName = descriptor.getIconClassName();
            if (StringUtils.isNotBlank(iconClassName)) {
                metadata.put("iconClassName", iconClassName);
                if (ctx != null) {
                    Icon icon = IconSet.icons
                            .getIconByClassSpec(StringUtils.join(new String[]{iconClassName, iconStyle}, " "));
                    if (icon != null) {
                        metadata.put("iconQualifiedUrl", icon.getQualifiedUrl(ctx));
                    }
                }
            }

            Category category = categories.getItem(ic.getId());
            if (category != null) {
                category.getItems().add(metadata);
            } else {
                List<Map<String, Serializable>> temp = new ArrayList<Map<String, Serializable>>();
                temp.add(metadata);
                category = new Category(ic.getId(), ic.getDisplayName(), ic.getDescription(), ic.getOrder(), ic.getMinToShow(), temp);
                categories.getItems().add(category);
            }
        }
        return categories;
    }

    public void doRssAll( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        rss(req, rsp, " all builds", getBuilds());
    }

    public void doRssFailed( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        rss(req, rsp, " failed builds", getBuilds().failureOnly());
    }
    
    public RunList getBuilds() {
        return new RunList(this);
    }
    
    public BuildTimelineWidget getTimeline() {
        return new BuildTimelineWidget(getBuilds());
    }

    private void rss(StaplerRequest req, StaplerResponse rsp, String suffix, RunList runs) throws IOException, ServletException {
        RSS.forwardToRss(getDisplayName()+ suffix, getUrl(),
            runs.newBuilds(), Run.FEED_ADAPTER, req, rsp );
    }

    public void doRssLatest( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        List<Run> lastBuilds = new ArrayList<Run>();
        for (TopLevelItem item : getItems()) {
            if (item instanceof Job) {
                Job job = (Job) item;
                Run lb = job.getLastBuild();
                if(lb!=null)    lastBuilds.add(lb);
            }
        }
        RSS.forwardToRss(getDisplayName()+" last builds only", getUrl(),
            lastBuilds, Run.FEED_ADAPTER_LATEST, req, rsp );
    }

    /**
     * Accepts <tt>config.xml</tt> submission, as well as serve it.
     */
    @WebMethod(name = "config.xml")
    public HttpResponse doConfigDotXml(StaplerRequest req) throws IOException {
        if (req.getMethod().equals("GET")) {
            // read
            checkPermission(READ);
            return new HttpResponse() {
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                    rsp.setContentType("application/xml");
                    View.this.writeXml(rsp.getOutputStream());
                }
            };
        }
        if (req.getMethod().equals("POST")) {
            // submission
            updateByXml(new StreamSource(req.getReader()));
            return HttpResponses.ok();
        }

        // huh?
        return HttpResponses.error(SC_BAD_REQUEST, "Unexpected request method " + req.getMethod());
    }

    /**
     * @since 1.538
     */
    public void writeXml(OutputStream out) throws IOException {
        // pity we don't have a handy way to clone Jenkins.XSTREAM to temp add the omit Field
        XStream2 xStream2 = new XStream2();
        xStream2.omitField(View.class, "owner");
        xStream2.toXMLUTF8(View.this,  out);
    }

    /**
     * Updates the View with the new XML definition.
     * @param source source of the Item's new definition.
     *               The source should be either a <code>StreamSource</code> or <code>SAXSource</code>, other sources
     *               may not be handled.
     */
    public void updateByXml(Source source) throws IOException {
        checkPermission(CONFIGURE);
        StringWriter out = new StringWriter();
        try {
            // this allows us to use UTF-8 for storing data,
            // plus it checks any well-formedness issue in the submitted
            // data
            XMLUtils.safeTransform(source, new StreamResult(out));
            out.close();
        } catch (TransformerException|SAXException e) {
            throw new IOException("Failed to persist configuration.xml", e);
        }

        // try to reflect the changes by reloading
        try (InputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toString().getBytes("UTF-8")))){
            // Do not allow overwriting view name as it might collide with another
            // view in same ViewGroup and might not satisfy Jenkins.checkGoodName.
            String oldname = name;
            ViewGroup oldOwner = owner; // oddly, this field is not transient
            Object o = Jenkins.XSTREAM2.unmarshal(XStream2.getDefaultDriver().createReader(in), this, null, true);
            if (!o.getClass().equals(getClass())) {
                // ensure that we've got the same view type. extending this code to support updating
                // to different view type requires destroying & creating a new view type
                throw new IOException("Expecting view type: "+this.getClass()+" but got: "+o.getClass()+" instead." +
                    "\nShould you needed to change to a new view type, you must first delete and then re-create " +
                    "the view with the new view type.");
            }
            name = oldname;
            owner = oldOwner;
        } catch (StreamException | ConversionException | Error e) {// mostly reflection errors
            throw new IOException("Unable to read",e);
        }
        save();
    }

    public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        ModelObjectWithContextMenu.ContextMenu m = new ModelObjectWithContextMenu.ContextMenu();
        for (TopLevelItem i : getItems())
            m.add(i.getShortUrl(),i.getDisplayName());
        return m;
    }

    /**
     * A list of available view types.
     * @deprecated as of 1.286
     *      Use {@link #all()} for read access, and use {@link Extension} for registration.
     */
    @Deprecated
    public static final DescriptorList<View> LIST = new DescriptorList<View>(View.class);

    /**
     * Returns all the registered {@link ViewDescriptor}s.
     */
    public static DescriptorExtensionList<View,ViewDescriptor> all() {
        return Jenkins.getInstance().<View,ViewDescriptor>getDescriptorList(View.class);
    }

    /**
     * Returns the {@link ViewDescriptor} instances that can be instantiated for the {@link ViewGroup} in the current
     * {@link StaplerRequest}.
     * <p>
     * <strong>NOTE: Historically this method is only ever called from a {@link StaplerRequest}</strong>
     * @return the list of instantiable {@link ViewDescriptor} instances for the current {@link StaplerRequest}
     */
    @Nonnull
    public static List<ViewDescriptor> allInstantiable() {
        List<ViewDescriptor> r = new ArrayList<ViewDescriptor>();
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request == null) {
            throw new IllegalStateException("This method can only be invoked from a stapler request");
        }
        ViewGroup owner = request.findAncestorObject(ViewGroup.class);
        if (owner == null) {
            throw new IllegalStateException("This method can only be invoked from a request with a ViewGroup ancestor");
        }
        for (ViewDescriptor d : DescriptorVisibilityFilter.apply(owner, all())) {
            if (d.isApplicableIn(owner) && d.isInstantiable()
                    && owner.getACL().hasCreatePermission(Jenkins.getAuthentication(), owner, d)) {
                r.add(d);
            }
        }
        return r;
    }

    public static final Comparator<View> SORTER = new Comparator<View>() {
        public int compare(View lhs, View rhs) {
            return lhs.getViewName().compareTo(rhs.getViewName());
        }
    };

    public static final PermissionGroup PERMISSIONS = new PermissionGroup(View.class,Messages._View_Permissions_Title());
    /**
     * Permission to create new views.
     */
    public static final Permission CREATE = new Permission(PERMISSIONS,"Create", Messages._View_CreatePermission_Description(), Permission.CREATE, PermissionScope.ITEM_GROUP);
    public static final Permission DELETE = new Permission(PERMISSIONS,"Delete", Messages._View_DeletePermission_Description(), Permission.DELETE, PermissionScope.ITEM_GROUP);
    public static final Permission CONFIGURE = new Permission(PERMISSIONS,"Configure", Messages._View_ConfigurePermission_Description(), Permission.CONFIGURE, PermissionScope.ITEM_GROUP);
    public static final Permission READ = new Permission(PERMISSIONS,"Read", Messages._View_ReadPermission_Description(), Permission.READ, PermissionScope.ITEM_GROUP);

    // to simplify access from Jelly
    public static Permission getItemCreatePermission() {
        return Item.CREATE;
    }
    
    public static View create(StaplerRequest req, StaplerResponse rsp, ViewGroup owner)
            throws FormException, IOException, ServletException {
        String mode = req.getParameter("mode");

        String requestContentType = req.getContentType();
        if (requestContentType == null
                && !(mode != null && mode.equals("copy")))
            throw new Failure("No Content-Type header set");

        boolean isXmlSubmission = requestContentType != null
                && (requestContentType.startsWith("application/xml")
                        || requestContentType.startsWith("text/xml"));

        String name = req.getParameter("name");
        Jenkins.checkGoodName(name);
        if(owner.getView(name)!=null)
            throw new Failure(Messages.Hudson_ViewAlreadyExists(name));

        if (mode==null || mode.length()==0) {
            if(isXmlSubmission) {
                View v = createViewFromXML(name, req.getInputStream());
                owner.getACL().checkCreatePermission(owner, v.getDescriptor());
                v.owner = owner;
                rsp.setStatus(HttpServletResponse.SC_OK);
                return v;
            } else
                throw new Failure(Messages.View_MissingMode());
        }

        View v;
        if ("copy".equals(mode)) {
            v = copy(req, owner, name);
        } else {
            ViewDescriptor descriptor = all().findByName(mode);
            if (descriptor == null) {
                throw new Failure("No view type ‘" + mode + "’ is known");
            }

            // create a view
            v = descriptor.newInstance(req,req.getSubmittedForm());
        }
        owner.getACL().checkCreatePermission(owner, v.getDescriptor());
        v.owner = owner;

        // redirect to the config screen
        rsp.sendRedirect2(req.getContextPath()+'/'+v.getUrl()+v.getPostConstructLandingPage());

        return v;
    }

    private static View copy(StaplerRequest req, ViewGroup owner, String name) throws IOException {
        View v;
        String from = req.getParameter("from");
        View src = src = owner.getView(from);

        if(src==null) {
            if(Util.fixEmpty(from)==null)
                throw new Failure("Specify which view to copy");
            else
                throw new Failure("No such view: "+from);
        }
        String xml = Jenkins.XSTREAM.toXML(src);
        v = createViewFromXML(name, new StringInputStream(xml));
        return v;
    }

    /**
     * Instantiate View subtype from XML stream.
     *
     * @param name Alternative name to use or <tt>null</tt> to keep the one in xml.
     */
    public static View createViewFromXML(String name, InputStream xml) throws IOException {

        try (InputStream in = new BufferedInputStream(xml)) {
            View v = (View) Jenkins.XSTREAM.fromXML(in);
            if (name != null) v.name = name;
            Jenkins.checkGoodName(v.name);
            return v;
        } catch(StreamException|ConversionException|Error e) {// mostly reflection errors
            throw new IOException("Unable to read",e);
        }
    }

    public static class PropertyList extends DescribableList<ViewProperty,ViewPropertyDescriptor> {
        private PropertyList(View owner) {
            super(owner);
        }

        public PropertyList() {// needed for XStream deserialization
        }

        public View getOwner() {
            return (View)owner;
        }

        @Override
        protected void onModified() throws IOException {
            for (ViewProperty p : this)
                p.setView(getOwner());
        }
    }

    /**
     * "Job" in "New Job". When a view is used in a context that restricts the child type,
     * It might be useful to override this.
     */
    public static final Message<View> NEW_PRONOUN = new Message<View>();

    private final static Logger LOGGER = Logger.getLogger(View.class.getName());
}
