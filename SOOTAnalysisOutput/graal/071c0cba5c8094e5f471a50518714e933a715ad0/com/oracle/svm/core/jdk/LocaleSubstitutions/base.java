package com.oracle.svm.core.jdk;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.LocaleServiceProvider;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.KeepOriginal;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.annotate.TargetElement;
import com.oracle.svm.core.util.VMError;
import sun.util.locale.provider.JRELocaleProviderAdapter;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.locale.provider.LocaleServiceProviderPool.LocalizedObjectGetter;

@TargetClass(java.util.Locale.class)
final class Target_java_util_Locale {

    static {
        Locale.getDefault();
        for (Locale.Category category : Locale.Category.values()) {
            Locale.getDefault(category);
        }
    }

    @Substitute
    private static Object initDefault() {
        throw VMError.unsupportedFeature("initalization of Locale");
    }

    @Substitute
    private static Object initDefault(Locale.Category category) {
        throw VMError.unsupportedFeature("initalization of Locale with category " + category);
    }
}

@Substitute
@TargetClass(sun.util.locale.provider.LocaleServiceProviderPool.class)
@SuppressWarnings({ "unchecked" })
final class Target_sun_util_locale_provider_LocaleServiceProviderPool {

    private static final Map<Class<? extends LocaleServiceProvider>, Object> cachedPools;

    private final LocaleServiceProvider cachedProvider;

    @Platforms(Platform.HOSTED_ONLY.class)
    protected static Class<LocaleServiceProvider>[] spiClasses() {
        return (Class<LocaleServiceProvider>[]) new Class<?>[] { java.text.spi.BreakIteratorProvider.class, java.text.spi.CollatorProvider.class, java.text.spi.DateFormatProvider.class, java.text.spi.DateFormatSymbolsProvider.class, java.text.spi.DecimalFormatSymbolsProvider.class, java.text.spi.NumberFormatProvider.class, java.util.spi.CurrencyNameProvider.class, java.util.spi.LocaleNameProvider.class, java.util.spi.TimeZoneNameProvider.class, java.util.spi.CalendarDataProvider.class, java.util.spi.CalendarNameProvider.class };
    }

    static {
        cachedPools = new HashMap<>();
        try {
            for (Class<LocaleServiceProvider> providerClass : spiClasses()) {
                final LocaleProviderAdapter lda = LocaleProviderAdapter.forJRE();
                final LocaleServiceProvider provider = lda.getLocaleServiceProvider(providerClass);
                assert provider != null : "Target_sun_util_locale_provider_LocaleServiceProviderPool: There should be no null LocaleServiceProviders.";
                cachedPools.put(providerClass, new Target_sun_util_locale_provider_LocaleServiceProviderPool(provider));
            }
        } catch (Throwable ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    Target_sun_util_locale_provider_LocaleServiceProviderPool(LocaleServiceProvider cachedProvider) {
        this.cachedProvider = cachedProvider;
    }

    @Substitute
    private static LocaleServiceProviderPool getPool(Class<? extends LocaleServiceProvider> providerClass) {
        LocaleServiceProviderPool result = (LocaleServiceProviderPool) cachedPools.get(providerClass);
        if (result == null) {
            throw VMError.unsupportedFeature("LocaleServiceProviderPool.getPool " + providerClass.getName());
        }
        return result;
    }

    @Substitute
    @SuppressWarnings({ "static-method" })
    @TargetElement(onlyWith = JDK8OrEarlier.class)
    private boolean hasProviders() {
        return false;
    }

    @KeepOriginal
    private native <P extends LocaleServiceProvider, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, Object... params);

    @KeepOriginal
    private native <P extends LocaleServiceProvider, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, String key, Object... params);

    @SuppressWarnings({ "unused" })
    @Substitute
    private <P extends LocaleServiceProvider, S> S getLocalizedObjectImpl(LocalizedObjectGetter<P, S> getter, Locale locale, boolean isObjectProvider, String key, Object... params) {
        if (locale == null) {
            throw new NullPointerException();
        }
        return getter.getObject((P) cachedProvider, locale, key, params);
    }

    @KeepOriginal
    @TargetElement(onlyWith = JDK9OrLater.class)
    public native <P extends LocaleServiceProvider, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, Boolean isObjectProvider, String key, Object... params);

    @KeepOriginal
    @TargetElement(onlyWith = JDK9OrLater.class)
    static native void config(Class<? extends Object> caller, String message);
}

@Delete
@TargetClass(sun.util.locale.provider.AuxLocaleProviderAdapter.class)
final class Target_sun_util_locale_provider_AuxLocaleProviderAdapter {
}

@TargetClass(sun.util.locale.provider.TimeZoneNameUtility.class)
final class Target_sun_util_locale_provider_TimeZoneNameUtility {

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    static ConcurrentHashMap<Locale, SoftReference<String[][]>> cachedZoneData = new ConcurrentHashMap<>();

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    static Map<String, SoftReference<Map<Locale, String[]>>> cachedDisplayNames = new ConcurrentHashMap<>();
}

@TargetClass(java.text.BreakIterator.class)
final class Target_java_text_BreakIterator {

    @Substitute
    private static BreakIterator getWordInstance(Locale locale) {
        assert locale == Locale.getDefault();
        return (BreakIterator) Util_java_text_BreakIterator.WORD_INSTANCE.clone();
    }

    @Substitute
    private static BreakIterator getLineInstance(Locale locale) {
        assert locale == Locale.getDefault();
        return (BreakIterator) Util_java_text_BreakIterator.LINE_INSTANCE.clone();
    }

    @Substitute
    private static BreakIterator getCharacterInstance(Locale locale) {
        assert locale == Locale.getDefault();
        return (BreakIterator) Util_java_text_BreakIterator.CHARACTER_INSTANCE.clone();
    }

    @Substitute
    private static BreakIterator getSentenceInstance(Locale locale) {
        assert locale == Locale.getDefault();
        return (BreakIterator) Util_java_text_BreakIterator.SENTENCE_INSTANCE.clone();
    }
}

@TargetClass(LocaleResources.class)
final class Target_sun_util_locale_provider_LocaleResources {

    @RecomputeFieldValue(kind = Kind.NewInstance, declClass = ConcurrentHashMap.class)
    @Alias
    private ConcurrentMap<?, ?> cache = new ConcurrentHashMap<>();

    @RecomputeFieldValue(kind = Kind.NewInstance, declClass = ReferenceQueue.class)
    @Alias
    private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
}

@TargetClass(JRELocaleProviderAdapter.class)
final class Target_sun_util_locale_provider_JRELocaleProviderAdapter {

    @RecomputeFieldValue(kind = Kind.NewInstance, declClass = ConcurrentHashMap.class)
    @Alias
    private final ConcurrentMap<String, Set<String>> langtagSets = new ConcurrentHashMap<>();

    @RecomputeFieldValue(kind = Kind.NewInstance, declClass = ConcurrentHashMap.class)
    @Alias
    private final ConcurrentMap<Locale, LocaleResources> localeResourcesMap = new ConcurrentHashMap<>();

    @Alias
    @TargetElement(onlyWith = JDK8OrEarlier.class)
    static Boolean isNonENSupported;

    @Substitute
    @TargetElement(onlyWith = JDK8OrEarlier.class)
    private static boolean isNonENLangSupported() {
        VMError.guarantee(isNonENSupported != null, "isNonENSupported must be initialized during image generation");
        return isNonENSupported;
    }
}

final class Util_java_text_BreakIterator {

    static final BreakIterator WORD_INSTANCE = BreakIterator.getWordInstance();

    static final BreakIterator LINE_INSTANCE = BreakIterator.getLineInstance();

    static final BreakIterator CHARACTER_INSTANCE = BreakIterator.getCharacterInstance();

    static final BreakIterator SENTENCE_INSTANCE = BreakIterator.getSentenceInstance();
}

public final class LocaleSubstitutions {
}
