package hudson.model;

import com.thoughtworks.xstream.XStream;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.listeners.ItemListener;
import hudson.remoting.Callable;
import hudson.triggers.Trigger;
import hudson.util.DescriptorList;
import hudson.util.EditDistance;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.annotation.CheckForNull;
import jenkins.model.DirectlyModifiableTopLevelItemGroup;
import org.apache.commons.io.FileUtils;

public class Items {

    public static final List<TopLevelItemDescriptor> LIST = (List) new DescriptorList<TopLevelItem>(TopLevelItem.class);

    private static final ThreadLocal<Boolean> updatingByXml = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static <V, T extends Throwable> V whileUpdatingByXml(Callable<V, T> callable) throws T {
        updatingByXml.set(true);
        try {
            return callable.call();
        } finally {
            updatingByXml.set(false);
        }
    }

    public static boolean currentlyUpdatingByXml() {
        return updatingByXml.get();
    }

    public static DescriptorExtensionList<TopLevelItem, TopLevelItemDescriptor> all() {
        return Jenkins.getInstance().<TopLevelItem, TopLevelItemDescriptor>getDescriptorList(TopLevelItem.class);
    }

    public static TopLevelItemDescriptor getDescriptor(String fqcn) {
        return Descriptor.find(all(), fqcn);
    }

    public static String toNameList(Collection<? extends Item> items) {
        StringBuilder buf = new StringBuilder();
        for (Item item : items) {
            if (buf.length() > 0)
                buf.append(", ");
            buf.append(item.getFullName());
        }
        return buf.toString();
    }

    public static <T extends Item> List<T> fromNameList(String list, Class<T> type) {
        return fromNameList(null, list, type);
    }

    public static <T extends Item> List<T> fromNameList(ItemGroup context, String list, Class<T> type) {
        Jenkins hudson = Jenkins.getInstance();
        List<T> r = new ArrayList<T>();
        StringTokenizer tokens = new StringTokenizer(list, ",");
        while (tokens.hasMoreTokens()) {
            String fullName = tokens.nextToken().trim();
            T item = hudson.getItem(fullName, context, type);
            if (item != null)
                r.add(item);
        }
        return r;
    }

    public static String getCanonicalName(ItemGroup context, String path) {
        String[] c = context.getFullName().split("/");
        String[] p = path.split("/");
        Stack name = new Stack();
        for (int i = 0; i < c.length; i++) {
            if (i == 0 && c[i].equals(""))
                continue;
            name.push(c[i]);
        }
        for (int i = 0; i < p.length; i++) {
            if (i == 0 && p[i].equals("")) {
                name.clear();
                continue;
            }
            if (p[i].equals("..")) {
                name.pop();
                continue;
            }
            if (p[i].equals(".")) {
                continue;
            }
            name.push(p[i]);
        }
        return StringUtils.join(name, '/');
    }

    public static String computeRelativeNamesAfterRenaming(String oldFullName, String newFullName, String relativeNames, ItemGroup context) {
        StringTokenizer tokens = new StringTokenizer(relativeNames, ",");
        List<String> newValue = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            String relativeName = tokens.nextToken().trim();
            String canonicalName = getCanonicalName(context, relativeName);
            if (canonicalName.equals(oldFullName) || canonicalName.startsWith(oldFullName + '/')) {
                String newCanonicalName = newFullName + canonicalName.substring(oldFullName.length());
                if (relativeName.startsWith("/")) {
                    newValue.add("/" + newCanonicalName);
                } else {
                    newValue.add(getRelativeNameFrom(newCanonicalName, context.getFullName()));
                }
            } else {
                newValue.add(relativeName);
            }
        }
        return StringUtils.join(newValue, ",");
    }

    static String getRelativeNameFrom(String itemFullName, String groupFullName) {
        String[] itemFullNameA = itemFullName.isEmpty() ? new String[0] : itemFullName.split("/");
        String[] groupFullNameA = groupFullName.isEmpty() ? new String[0] : groupFullName.split("/");
        for (int i = 0; ; i++) {
            if (i == itemFullNameA.length) {
                if (i == groupFullNameA.length) {
                    return ".";
                } else {
                    StringBuilder b = new StringBuilder();
                    for (int j = 0; j < groupFullNameA.length - itemFullNameA.length; j++) {
                        if (j > 0) {
                            b.append('/');
                        }
                        b.append("..");
                    }
                    return b.toString();
                }
            } else if (i == groupFullNameA.length) {
                StringBuilder b = new StringBuilder();
                for (int j = i; j < itemFullNameA.length; j++) {
                    if (j > i) {
                        b.append('/');
                    }
                    b.append(itemFullNameA[j]);
                }
                return b.toString();
            } else if (itemFullNameA[i].equals(groupFullNameA[i])) {
                continue;
            } else {
                StringBuilder b = new StringBuilder();
                for (int j = i; j < groupFullNameA.length; j++) {
                    if (j > i) {
                        b.append('/');
                    }
                    b.append("..");
                }
                for (int j = i; j < itemFullNameA.length; j++) {
                    b.append('/').append(itemFullNameA[j]);
                }
                return b.toString();
            }
        }
    }

    public static Item load(ItemGroup parent, File dir) throws IOException {
        Item item = (Item) getConfigFile(dir).read();
        item.onLoad(parent, dir.getName());
        return item;
    }

    public static XmlFile getConfigFile(File dir) {
        return new XmlFile(XSTREAM, new File(dir, "config.xml"));
    }

    public static XmlFile getConfigFile(Item item) {
        return getConfigFile(item.getRootDir());
    }

    public static <T extends Item> List<T> getAllItems(final ItemGroup root, Class<T> type) {
        List<T> r = new ArrayList<T>();
        getAllItems(root, type, r);
        return r;
    }

    private static <T extends Item> void getAllItems(final ItemGroup root, Class<T> type, List<T> r) {
        List<Item> items = new ArrayList<Item>(((ItemGroup<?>) root).getItems());
        Collections.sort(items, new Comparator<Item>() {

            @Override
            public int compare(Item i1, Item i2) {
                return name(i1).compareToIgnoreCase(name(i2));
            }

            String name(Item i) {
                String n = i.getName();
                if (i instanceof ItemGroup) {
                    n += '/';
                }
                return n;
            }
        });
        for (Item i : items) {
            if (type.isInstance(i)) {
                if (i.hasPermission(Item.READ)) {
                    r.add(type.cast(i));
                }
            }
            if (i instanceof ItemGroup) {
                getAllItems((ItemGroup) i, type, r);
            }
        }
    }

    @CheckForNull
    public static <T extends Item> T findNearest(Class<T> type, String name, ItemGroup context) {
        List<T> projects = Jenkins.getInstance().getAllItems(type);
        String[] names = new String[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            names[i] = projects.get(i).getRelativeNameFrom(context);
        }
        String nearest = EditDistance.findNearest(name, names);
        return Jenkins.getInstance().getItem(nearest, context, type);
    }

    public static <I extends AbstractItem & TopLevelItem> I move(I item, DirectlyModifiableTopLevelItemGroup destination) throws IOException, IllegalArgumentException {
        DirectlyModifiableTopLevelItemGroup oldParent = (DirectlyModifiableTopLevelItemGroup) item.getParent();
        if (oldParent == destination) {
            throw new IllegalArgumentException();
        }
        if (!destination.canAdd(item)) {
            throw new IllegalArgumentException();
        }
        String name = item.getName();
        if (destination.getItem(name) != null) {
            throw new IllegalArgumentException(name + " already exists");
        }
        String oldFullName = item.getFullName();
        File destDir = destination.getRootDirFor(item);
        FileUtils.forceMkdir(destDir.getParentFile());
        FileUtils.moveDirectory(item.getRootDir(), destDir);
        oldParent.remove(item);
        I newItem = destination.add(item, name);
<<<<<<< MINE
        newItem.onLoad(destination, name);
=======
        item.movedTo(destination, newItem, destDir);
>>>>>>> YOURS
        ItemListener.fireLocationChange(newItem, oldFullName);
        return newItem;
    }

    public static final XStream XSTREAM = new XStream2();

    public static final XStream2 XSTREAM2 = (XStream2) XSTREAM;

    static {
        XSTREAM.alias("project", FreeStyleProject.class);
    }
}
