package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.json.*;

@Named
@javax.enterprise.context.SessionScoped
public class DataverseLocaleBean implements Serializable {

    private static final Logger logger = Logger.getLogger(DataverseLocaleBean.class.getCanonicalName());

    @Inject
    SettingsWrapper settingsWrapper;

    {
        if (FacesContext.getCurrentInstance() == null) {
            localeCode = "en";
        } else if (FacesContext.getCurrentInstance().getViewRoot() == null) {
            localeCode = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getLanguage();
        } else if (FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage() == "en_US") {
            localeCode = "en";
        } else {
            localeCode = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        }
    }

    private Map<String, String> dataverseLocales;

    private String localeCode;

    public void init() {
        dataverseLocales = new LinkedHashMap<>();
        try {
            JSONArray entries = new JSONArray(settingsWrapper.getValueForKey(SettingsServiceBean.Key.Languages, "[]"));
            for (Object obj : entries) {
                JSONObject entry = (JSONObject) obj;
                String locale = entry.getString("locale");
                String title = entry.getString("title");
                dataverseLocales.put(locale, title);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getDataverseLocales() {
        return dataverseLocales;
    }

    public boolean useLocale() {
        if (dataverseLocales == null) {
            init();
        }
        return dataverseLocales.size() > 1;
    }

    public String getLocaleCode() {
        if (localeCode == null) {
            init();
        }
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getLocaleTitle() {
        if (dataverseLocales == null) {
            init();
        }
        return dataverseLocales.get(localeCode);
    }

    public void countryLocaleCodeChanged(String code) {
        if (dataverseLocales == null) {
            init();
        }
        localeCode = code;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(dataverseLocales.get(code)));
        try {
            String url = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getHeader("referer");
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}