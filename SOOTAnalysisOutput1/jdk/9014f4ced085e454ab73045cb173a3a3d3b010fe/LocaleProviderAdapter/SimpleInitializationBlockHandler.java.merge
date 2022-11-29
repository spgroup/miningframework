package sun.util.locale.provider;

import java.security.AccessController;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;
import sun.util.cldr.CLDRLocaleProviderAdapter;
import sun.util.resources.LocaleData;

public abstract class LocaleProviderAdapter {

    public static enum Type {

        JRE("sun.util.resources", "sun.text.resources"), CLDR("sun.util.resources.cldr", "sun.text.resources.cldr"), SPI, HOST;

        private final String UTIL_RESOURCES_PACKAGE;

        private final String TEXT_RESOURCES_PACKAGE;

        private Type() {
            this(null, null);
        }

        private Type(String util, String text) {
            UTIL_RESOURCES_PACKAGE = util;
            TEXT_RESOURCES_PACKAGE = text;
        }

        public String getUtilResourcesPackage() {
            return UTIL_RESOURCES_PACKAGE;
        }

        public String getTextResourcesPackage() {
            return TEXT_RESOURCES_PACKAGE;
        }
    }

    private static Type[] adapterPreference = { Type.JRE, Type.SPI };

    private static LocaleProviderAdapter jreLocaleProviderAdapter = new JRELocaleProviderAdapter();

    private static LocaleProviderAdapter spiLocaleProviderAdapter = new SPILocaleProviderAdapter();

    private static LocaleProviderAdapter cldrLocaleProviderAdapter = null;

    private static LocaleProviderAdapter hostLocaleProviderAdapter = null;

    static {
        String order = AccessController.doPrivileged(new sun.security.action.GetPropertyAction("java.locale.providers"));
        if (order != null && order.length() != 0) {
            String[] types = order.split(",");
            List<Type> typeList = new ArrayList<>();
            for (String type : types) {
                try {
                    Type aType = Type.valueOf(type.trim().toUpperCase(Locale.ROOT));
                    switch(aType) {
                        case CLDR:
                            cldrLocaleProviderAdapter = new CLDRLocaleProviderAdapter();
                            break;
                        case HOST:
                            hostLocaleProviderAdapter = new HostLocaleProviderAdapter();
                            break;
                    }
                    typeList.add(aType);
                } catch (IllegalArgumentException | UnsupportedOperationException e) {
                    LocaleServiceProviderPool.config(LocaleProviderAdapter.class, e.toString());
                }
            }
            if (!typeList.contains(Type.JRE)) {
                typeList.add(Type.JRE);
            }
            adapterPreference = typeList.toArray(new Type[0]);
        }
    }

    public static LocaleProviderAdapter forType(Type type) {
        switch(type) {
            case JRE:
                return jreLocaleProviderAdapter;
            case CLDR:
                return cldrLocaleProviderAdapter;
            case SPI:
                return spiLocaleProviderAdapter;
            case HOST:
                return hostLocaleProviderAdapter;
            default:
                throw new InternalError("unknown locale data adapter type");
        }
    }

    public static LocaleProviderAdapter forJRE() {
        return jreLocaleProviderAdapter;
    }

    public static LocaleProviderAdapter getResourceBundleBased() {
        for (Type type : getAdapterPreference()) {
            if (type == Type.JRE || type == Type.CLDR) {
                return forType(type);
            }
        }
        throw new InternalError();
    }

    public static Type[] getAdapterPreference() {
        return adapterPreference;
    }

    public static LocaleProviderAdapter getAdapter(Class<? extends LocaleServiceProvider> providerClass, Locale locale) {
        LocaleProviderAdapter adapter = findAdapter(providerClass, locale);
        if (adapter != null) {
            return adapter;
        }
        List<Locale> lookupLocales = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT).getCandidateLocales("", locale);
        for (Locale loc : lookupLocales) {
            if (loc.equals(locale)) {
                continue;
            }
            adapter = findAdapter(providerClass, loc);
            if (adapter != null) {
                return adapter;
            }
        }
        return jreLocaleProviderAdapter;
    }

    private static LocaleProviderAdapter findAdapter(Class<? extends LocaleServiceProvider> providerClass, Locale locale) {
        for (Type type : getAdapterPreference()) {
            LocaleProviderAdapter adapter = forType(type);
            LocaleServiceProvider provider = adapter.getLocaleServiceProvider(providerClass);
            if (provider != null) {
                if (provider.isSupportedLocale(locale)) {
                    return adapter;
                }
            }
        }
        return null;
    }

    static boolean isSupportedLocale(Locale locale, LocaleProviderAdapter.Type type, Set<String> langtags) {
        assert type == Type.JRE || type == Type.CLDR;
        if (locale == Locale.ROOT) {
            return true;
        }
        locale = locale.stripExtensions();
        if (langtags.contains(locale.toLanguageTag())) {
            return true;
        }
        if (type == LocaleProviderAdapter.Type.JRE) {
            String oldname = locale.toString().replace('_', '-');
            return langtags.contains(oldname);
        }
        return false;
    }

    public static Locale[] toLocaleArray(Set<String> tags) {
        Locale[] locs = new Locale[tags.size() + 1];
        int index = 0;
        locs[index++] = Locale.ROOT;
        for (String tag : tags) {
            switch(tag) {
                case "ja-JP-JP":
                    locs[index++] = JRELocaleConstants.JA_JP_JP;
                    break;
                case "th-TH-TH":
                    locs[index++] = JRELocaleConstants.TH_TH_TH;
                    break;
                default:
                    locs[index++] = Locale.forLanguageTag(tag);
                    break;
            }
        }
        return locs;
    }

    public abstract LocaleProviderAdapter.Type getAdapterType();

    public abstract <P extends LocaleServiceProvider> P getLocaleServiceProvider(Class<P> c);

    public abstract BreakIteratorProvider getBreakIteratorProvider();

    public abstract CollatorProvider getCollatorProvider();

    public abstract DateFormatProvider getDateFormatProvider();

    public abstract DateFormatSymbolsProvider getDateFormatSymbolsProvider();

    public abstract DecimalFormatSymbolsProvider getDecimalFormatSymbolsProvider();

    public abstract NumberFormatProvider getNumberFormatProvider();

    public abstract CurrencyNameProvider getCurrencyNameProvider();

    public abstract LocaleNameProvider getLocaleNameProvider();

    public abstract TimeZoneNameProvider getTimeZoneNameProvider();

    public abstract CalendarDataProvider getCalendarDataProvider();

    public abstract LocaleResources getLocaleResources(Locale locale);

    public abstract LocaleData getLocaleData();

    public abstract Locale[] getAvailableLocales();
}