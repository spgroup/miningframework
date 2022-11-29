package build.tools.cldrconverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

abstract class AbstractLDMLHandler<V> extends DefaultHandler {

    static final Map<String, String> DAY_OF_WEEK_MAP = new HashMap<>();

    static {
        DAY_OF_WEEK_MAP.put("sun", "1");
        DAY_OF_WEEK_MAP.put("mon", "2");
        DAY_OF_WEEK_MAP.put("tue", "3");
        DAY_OF_WEEK_MAP.put("wed", "4");
        DAY_OF_WEEK_MAP.put("thu", "5");
        DAY_OF_WEEK_MAP.put("fri", "6");
        DAY_OF_WEEK_MAP.put("sat", "7");
    }

    private Map<String, V> data = new HashMap<>();

    Container currentContainer = new Container("$ROOT", null);

    AbstractLDMLHandler() {
    }

    Map<String, V> getData() {
        return data;
    }

    V put(String key, V value) {
        return data.put(key, value);
    }

    V get(String key) {
        return data.get(key);
    }

    Set<String> keySet() {
        return data.keySet();
    }

    boolean isIgnored(Attributes attributes) {
        if (attributes.getValue("alt") != null) {
            return true;
        }
        String draftValue = attributes.getValue("draft");
        if (draftValue != null) {
            return CLDRConverter.draftType > CLDRConverter.DRAFT_MAP.get(draftValue);
        }
        return false;
    }

    void pushContainer(String qName, Attributes attributes) {
        if (isIgnored(attributes) || currentContainer instanceof IgnoredContainer) {
            currentContainer = new IgnoredContainer(qName, currentContainer);
        } else {
            currentContainer = new Container(qName, currentContainer);
        }
    }

    void pushIgnoredContainer(String qName) {
        currentContainer = new IgnoredContainer(qName, currentContainer);
    }

    void pushKeyContainer(String qName, Attributes attributes, String key) {
        if (!pushIfIgnored(qName, attributes)) {
            currentContainer = new KeyContainer(qName, currentContainer, key);
        }
    }

    void pushStringEntry(String qName, Attributes attributes, String key) {
        if (!pushIfIgnored(qName, attributes)) {
            currentContainer = new StringEntry(qName, currentContainer, key);
        }
    }

    void pushStringEntry(String qName, Attributes attributes, String key, String value) {
        if (!pushIfIgnored(qName, attributes)) {
            currentContainer = new StringEntry(qName, currentContainer, key, value);
        }
    }

    void pushStringArrayEntry(String qName, Attributes attributes, String key, int length) {
        if (!pushIfIgnored(qName, attributes)) {
            currentContainer = new StringArrayEntry(qName, currentContainer, key, length);
        }
    }

    void pushStringArrayElement(String qName, Attributes attributes, int index) {
        if (!pushIfIgnored(qName, attributes)) {
            currentContainer = new StringArrayElement(qName, currentContainer, index);
        }
    }

    private boolean pushIfIgnored(String qName, Attributes attributes) {
        if (isIgnored(attributes) || currentContainer instanceof IgnoredContainer) {
            pushIgnoredContainer(qName);
            return true;
        }
        return false;
    }

    String getContainerKey() {
        Container current = currentContainer;
        while (current != null) {
            if (current instanceof KeyContainer) {
                return ((KeyContainer) current).getKey();
            }
            current = current.getParent();
        }
        return null;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentContainer.addCharacters(ch, start, length);
    }

    @SuppressWarnings(value = "CallToThreadDumpStack")
    @Override
    public void warning(SAXParseException e) throws SAXException {
        e.printStackTrace();
    }

    @SuppressWarnings(value = "CallToThreadDumpStack")
    @Override
    public void error(SAXParseException e) throws SAXException {
        e.printStackTrace();
    }

    @SuppressWarnings(value = "CallToThreadDumpStack")
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        e.printStackTrace();
        super.fatalError(e);
    }
}