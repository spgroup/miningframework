package hudson.markup;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import org.owasp.html.Handler;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamRenderer;
import org.owasp.html.PolicyFactory;
import java.io.IOException;
import java.util.regex.Pattern;

public class MyspacePolicy {

    public static final PolicyFactory POLICY_DEFINITION;

    private static final Pattern ONSITE_URL = Pattern.compile("(?:[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]+|\\#(\\w)+)");

    private static final Pattern OFFSITE_URL = Pattern.compile("\\s*(?:(?:ht|f)tps?://|mailto:)[\\p{L}\\p{N}]" + "[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*\\s*");

    private static final Predicate<String> ONSITE_OR_OFFSITE_URL = new Predicate<String>() {

        public boolean apply(String s) {
            return ONSITE_URL.matcher(s).matches() || OFFSITE_URL.matcher(s).matches();
        }
    };

    static {
        POLICY_DEFINITION = new HtmlPolicyBuilder2() {

            {
                allowAttributes("id", "class", "lang", "title", "alt", "style", "media", "href", "name", "shape", "border", "cellpadding", "cellspacing", "colspan", "rowspan", "background", "bgcolor", "abbr", "headers", "charoff", "char", "aixs", "nowrap", "width", "height", "align", "valign", "scope", "tabindex", "disabled", "readonly", "accesskey", "size", "autocomplete", "rows", "cols").globally();
                disallowElements("script", "noscript", "frameset", "frame");
                tag("label", "for");
                tag("form", "action", ONSITE_URL, "method");
                tag("button", "value", "type");
                tag("input", "maxlength", "checked", "src", ONSITE_OR_OFFSITE_URL, "usemap", ONSITE_URL, "type", "value");
                tag("select", "multiple");
                tag("option", "value", "label", "selected");
                tag("textarea");
                tag("h1,h2,h3,h4,h5,h6,p,i,b,u,strong,em,small,big,pre,code,cite,samp,sub,sup,strike,center,blockquote");
                tag("hr,br,col");
                tag("font", "color", "face", "size");
                tag("a", "nohref", "rel");
                tag("style", "type");
                tag("span,div");
                tag("img", "src", ONSITE_OR_OFFSITE_URL, "hspace", "vspace");
<<<<<<< MINE
                tag("iframe", "src");
=======
                tag("link", "type", "rel");
>>>>>>> YOURS
                tag("ul,ol,li,dd,dl,dt,thead,tbody,tfoot");
                tag("table", "noresize");
                tag("td,th,tr");
                tag("colgroup", "span");
                tag("col", "span");
                tag("fieldset,legend");
                allowStandardUrlProtocols();
            }
        }.toFactory();
    }

    public static void main(String[] args) throws IOException {
        String html = "<a href='http://www.google.com/'>Google</a><img src='http://www.yahoo.com'>";
        HtmlStreamRenderer renderer = HtmlStreamRenderer.create(System.out, new Handler<IOException>() {

            public void handle(IOException ex) {
                Throwables.propagate(ex);
            }
        }, new Handler<String>() {

            public void handle(String x) {
                throw new AssertionError(x);
            }
        });
        HtmlSanitizer.sanitize(html, POLICY_DEFINITION.apply(renderer));
    }
}