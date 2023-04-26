package hudson.model;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import hudson.diagnosis.OldDataMonitor;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import jenkins.util.NonLocalizable;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExportedBean(defaultVisibility = 2)
public class HealthReport implements Serializable, Comparable<HealthReport> {

    private static final String HEALTH_OVER_80 = "icon-health-80plus";

    private static final String HEALTH_61_TO_80 = "icon-health-60to79";

    private static final String HEALTH_41_TO_60 = "icon-health-40to59";

    private static final String HEALTH_21_TO_40 = "icon-health-20to39";

    private static final String HEALTH_0_TO_20 = "icon-health-00to19";

    private static final String HEALTH_OVER_80_IMG = "health-80plus.png";

    private static final String HEALTH_61_TO_80_IMG = "health-60to79.png";

    private static final String HEALTH_41_TO_60_IMG = "health-40to59.png";

    private static final String HEALTH_21_TO_40_IMG = "health-20to39.png";

    private static final String HEALTH_0_TO_20_IMG = "health-00to19.png";

    private static final String HEALTH_UNKNOWN_IMG = "empty.png";

    private static final Map<String, String> iconIMGToClassMap = new HashMap<String, String>();

    static {
        iconIMGToClassMap.put(HEALTH_OVER_80_IMG, HEALTH_OVER_80);
        iconIMGToClassMap.put(HEALTH_61_TO_80_IMG, HEALTH_61_TO_80);
        iconIMGToClassMap.put(HEALTH_41_TO_60_IMG, HEALTH_41_TO_60);
        iconIMGToClassMap.put(HEALTH_21_TO_40_IMG, HEALTH_21_TO_40);
        iconIMGToClassMap.put(HEALTH_0_TO_20_IMG, HEALTH_0_TO_20);
    }

    private int score;

    private String iconClassName;

    private String iconUrl;

    @Deprecated
    private transient String description;

    private Localizable localizibleDescription;

    @Deprecated
    public HealthReport(int score, String iconUrl, String description) {
        this(score, iconUrl, new NonLocalizable(description));
    }

    public HealthReport(int score, String iconUrl, Localizable description) {
        this.score = score;
        if (score <= 20) {
            this.iconClassName = HEALTH_0_TO_20;
        } else if (score <= 40) {
            this.iconClassName = HEALTH_21_TO_40;
        } else if (score <= 60) {
            this.iconClassName = HEALTH_41_TO_60;
        } else if (score <= 80) {
            this.iconClassName = HEALTH_61_TO_80;
        } else {
            this.iconClassName = HEALTH_OVER_80;
        }
        if (iconUrl == null) {
            if (score <= 20) {
                this.iconUrl = HEALTH_0_TO_20_IMG;
            } else if (score <= 40) {
                this.iconUrl = HEALTH_21_TO_40_IMG;
            } else if (score <= 60) {
                this.iconUrl = HEALTH_41_TO_60_IMG;
            } else if (score <= 80) {
                this.iconUrl = HEALTH_61_TO_80_IMG;
            } else {
                this.iconUrl = HEALTH_OVER_80_IMG;
            }
        } else {
            this.iconUrl = iconUrl;
        }
        this.description = null;
        setLocalizibleDescription(description);
    }

    @Deprecated
    public HealthReport(int score, String description) {
        this(score, null, description);
    }

    public HealthReport(int score, Localizable description) {
        this(score, null, description);
    }

    public HealthReport() {
        this(100, HEALTH_UNKNOWN_IMG, Messages._HealthReport_EmptyString());
    }

    @Exported
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Exported
    public String getIconUrl() {
        return iconUrl;
    }

    @Exported
    public String getIconClassName() {
        return iconClassName;
    }

    public String getIconUrl(String size) {
        if (iconUrl == null) {
            return Jenkins.RESOURCE_PATH + "/images/" + size + "/" + HEALTH_UNKNOWN_IMG;
        }
        if (iconUrl.startsWith("/")) {
            return iconUrl.replace("/32x32/", "/" + size + "/");
        }
        return Jenkins.RESOURCE_PATH + "/images/" + size + "/" + iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Exported
    public String getDescription() {
        return getLocalizableDescription().toString();
    }

    public void setDescription(String description) {
        setLocalizibleDescription(new NonLocalizable(description));
    }

    public Localizable getLocalizableDescription() {
        return localizibleDescription;
    }

    public void setLocalizibleDescription(Localizable localizibleDescription) {
        this.localizibleDescription = localizibleDescription;
    }

    public List<HealthReport> getAggregatedReports() {
        return Collections.emptyList();
    }

    public boolean isAggregateReport() {
        return false;
    }

    public int compareTo(HealthReport o) {
        return (this.score < o.score ? -1 : (this.score == o.score ? 0 : 1));
    }

    public static HealthReport min(HealthReport a, HealthReport b) {
        if (a == null && b == null)
            return null;
        if (a == null)
            return b;
        if (b == null)
            return a;
        if (a.compareTo(b) <= 0)
            return a;
        return b;
    }

    public static HealthReport max(HealthReport a, HealthReport b) {
        if (a == null && b == null)
            return null;
        if (a == null)
            return b;
        if (b == null)
            return a;
        if (a.compareTo(b) >= 0)
            return a;
        return b;
    }

    public static class ConverterImpl extends XStream2.PassthruConverter<HealthReport> {

        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        protected void callback(HealthReport hr, UnmarshallingContext context) {
            if (hr.localizibleDescription == null) {
                hr.localizibleDescription = new NonLocalizable(hr.description == null ? "" : hr.description);
                OldDataMonitor.report(context, "1.256");
            }
            if (hr.iconClassName == null && hr.iconUrl != null && iconIMGToClassMap.containsKey(hr.iconUrl)) {
                hr.iconClassName = iconIMGToClassMap.get(hr.iconUrl);
            }
        }
    }
}
