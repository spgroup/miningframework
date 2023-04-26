package hudson.util;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLFilter;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.apache.commons.jelly.XMLOutput;
import java.util.Locale;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class TableNestChecker extends XMLFilterImpl {

    private final Stack<Checker> elements = new Stack<>();

    private final Stack<String> tagNames = new Stack<>();

    public static void applyTo(XMLOutput xo) {
        xo.setContentHandler(new TableNestChecker(xo.getContentHandler()));
    }

    public TableNestChecker() {
        elements.push(ALL_ALLOWED);
    }

    public TableNestChecker(ContentHandler target) {
        this();
        setContentHandler(target);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        String tagName = localName.toUpperCase(Locale.ENGLISH);
        if (!elements.peek().isAllowed(tagName))
            throw new SAXException(tagName + " is not allowed inside " + tagNames.peek());
        Checker next = CHECKERS.get(tagName);
        if (next == null)
            next = ALL_ALLOWED;
        elements.push(next);
        tagNames.push(tagName);
        super.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elements.pop();
        tagNames.pop();
        super.endElement(uri, localName, qName);
    }

    private interface Checker {

        boolean isAllowed(String childTag);
    }

    private static final Checker ALL_ALLOWED = new Checker() {

        public boolean isAllowed(String childTag) {
            return true;
        }
    };

    private static final class InList implements Checker {

        private final Set<String> tags;

        private InList(String... tags) {
            this.tags = new HashSet<>(Arrays.asList(tags));
        }

        public boolean isAllowed(String childTag) {
            return tags.contains(childTag);
        }
    }

    private static final Map<String, Checker> CHECKERS = new HashMap<>();

    static {
        CHECKERS.put("TABLE", new InList("TR", "THEAD", "TBODY"));
        InList rows = new InList("TR");
        CHECKERS.put("THEAD", rows);
        CHECKERS.put("THEAD", rows);
        CHECKERS.put("TR", new InList("TD", "TH"));
    }
}
