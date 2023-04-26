package org.dspace.text.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MARC21InitialArticleWord extends InitialArticleWord {

    public MARC21InitialArticleWord() {
        super(true);
    }

    public MARC21InitialArticleWord(boolean stripWord) {
        super(stripWord);
    }

    protected String[] getArticleWords(String lang) {
        if (lang == null)
            return null;
        Language l = Language.getLanguage(lang);
        if (l != null && ianaArticleMap.containsKey(l.IANA)) {
            ArticlesForLang articles = (ArticlesForLang) ianaArticleMap.get(l.IANA);
            if (articles != null)
                return articles.words;
        }
        return null;
    }

    private static Map ianaArticleMap = new HashMap();

    static {
        Object[][] articleWordArray = { { "a", Language.ENGLISH, Language.GALICIAN, Language.HUNGARIAN, Language.PORTUGUESE, Language.ROMANIAN, Language.SCOTS, Language.YIDDISH }, { "a'", Language.SCOTTISH_GAELIC }, { "al", Language.ROMANIAN }, { "al-", Language.ARABIC, Language.BALUCHI, Language.BRAHUI, Language.PANJABI, Language.PERSIAN, Language.TURKISH, Language.URDU }, { "am", Language.SCOTTISH_GAELIC }, { "an", Language.ENGLISH, Language.IRISH, Language.SCOTS, Language.SCOTTISH_GAELIC, Language.YIDDISH }, { "an t-", Language.IRISH, Language.SCOTTISH_GAELIC }, { "ane", Language.SCOTS }, { "ang", Language.TAGALOG }, { "ang mga", Language.TAGALOG }, { "as", Language.GALICIAN, Language.PORTUGUESE }, { "az", Language.HUNGARIAN }, { "bat", Language.BASQUE }, { "bir", Language.TURKISH }, { "d'", Language.ENGLISH }, { "da", Language.SHETLAND_ENGLISH }, { "das", Language.GERMAN }, { "de", Language.DANISH, Language.DUTCH, Language.ENGLISH, Language.FRISIAN, Language.NORWEGIAN, Language.SWEDISH }, { "dei", Language.NORWEGIAN }, { "dem", Language.GERMAN }, { "den", Language.DANISH, Language.GERMAN, Language.NORWEGIAN, Language.SWEDISH }, { "der", Language.GERMAN, Language.YIDDISH }, { "des", Language.GERMAN, Language.WALLOON }, { "det", Language.DANISH, Language.NORWEGIAN, Language.SWEDISH }, { "di", Language.YIDDISH }, { "die", Language.AFRIKAANS, Language.GERMAN, Language.YIDDISH }, { "dos", Language.YIDDISH }, { "e", Language.NORWEGIAN }, { "'e", Language.FRISIAN }, { "een", Language.DUTCH }, { "eene", Language.DUTCH }, { "egy", Language.HUNGARIAN }, { "ei", Language.NORWEGIAN }, { "ein", Language.GERMAN, Language.NORWEGIAN, Language.WALLOON }, { "eine", Language.GERMAN }, { "einem", Language.GERMAN }, { "einen", Language.GERMAN }, { "einer", Language.GERMAN }, { "eines", Language.GERMAN }, { "eit", Language.NORWEGIAN }, { "el", Language.CATALAN, Language.SPANISH }, { "el-", Language.ARABIC }, { "els", Language.CATALAN }, { "en", Language.CATALAN, Language.DANISH, Language.NORWEGIAN, Language.SWEDISH }, { "enne", Language.WALLOON }, { "et", Language.DANISH, Language.NORWEGIAN }, { "ett", Language.SWEDISH }, { "eyn", Language.YIDDISH }, { "eyne", Language.YIDDISH }, { "gl'", Language.ITALIAN }, { "gli", Language.PROVENCAL }, { "ha-", Language.HEBREW }, { "hai", Language.CLASSICAL_GREEK, Language.GREEK }, { "he", Language.HAWAIIAN }, { "h\u0113", Language.CLASSICAL_GREEK, Language.GREEK }, { "he-", Language.HEBREW }, { "heis", Language.GREEK }, { "hen", Language.GREEK }, { "hena", Language.GREEK }, { "henas", Language.GREEK }, { "het", Language.DUTCH }, { "hin", Language.ICELANDIC }, { "hina", Language.ICELANDIC }, { "hinar", Language.ICELANDIC }, { "hinir", Language.ICELANDIC }, { "hinn", Language.ICELANDIC }, { "hinna", Language.ICELANDIC }, { "hinnar", Language.ICELANDIC }, { "hinni", Language.ICELANDIC }, { "hins", Language.ICELANDIC }, { "hinu", Language.ICELANDIC }, { "hinum", Language.ICELANDIC }, { "hi\u01d2", Language.ICELANDIC }, { "ho", Language.CLASSICAL_GREEK, Language.GREEK }, { "hoi", Language.CLASSICAL_GREEK, Language.GREEK }, { "i", Language.ITALIAN }, { "ih'", Language.PROVENCAL }, { "il", Language.ITALIAN, Language.PROVENCAL_OCCITAN }, { "il-", Language.MALTESE }, { "in", Language.FRISIAN }, { "it", Language.FRISIAN }, { "ka", Language.HAWAIIAN }, { "ke", Language.HAWAIIAN }, { "l'", Language.CATALAN, Language.FRENCH, Language.ITALIAN, Language.PROVENCAL_OCCITAN, Language.WALLOON }, { "l-", Language.MALTESE }, { "la", Language.CATALAN, Language.ESPERANTO, Language.FRENCH, Language.ITALIAN, Language.PROVENCAL_OCCITAN, Language.SPANISH }, { "las", Language.PROVENCAL_OCCITAN, Language.SPANISH }, { "le", Language.FRENCH, Language.ITALIAN, Language.PROVENCAL_OCCITAN }, { "les", Language.CATALAN, Language.FRENCH, Language.PROVENCAL_OCCITAN, Language.WALLOON }, { "lh", Language.PROVENCAL_OCCITAN }, { "lhi", Language.PROVENCAL_OCCITAN }, { "li", Language.PROVENCAL_OCCITAN }, { "lis", Language.PROVENCAL_OCCITAN }, { "lo", Language.ITALIAN, Language.PROVENCAL_OCCITAN, Language.SPANISH }, { "los", Language.PROVENCAL_OCCITAN, Language.SPANISH }, { "lou", Language.PROVENCAL_OCCITAN }, { "lu", Language.PROVENCAL_OCCITAN }, { "mga", Language.TAGALOG }, { "m\u0303ga", Language.TAGALOG }, { "mia", Language.GREEK }, { "'n", Language.AFRIKAANS, Language.DUTCH, Language.FRISIAN }, { "na", Language.HAWAIIAN, Language.IRISH, Language.SCOTTISH_GAELIC }, { "na h-", Language.IRISH, Language.SCOTTISH_GAELIC }, { "nje", Language.ALBANIAN }, { "ny", Language.MALAGASY }, { "'o", Language.NEAPOLITAN_ITALIAN }, { "o", Language.GALICIAN, Language.HAWAIIAN, Language.PORTUGUESE, Language.ROMANIAN }, { "os", Language.PORTUGUESE }, { "'r", Language.ICELANDIC }, { "'s", Language.GERMAN }, { "sa", Language.TAGALOG }, { "sa mga", Language.TAGALOG }, { "si", Language.TAGALOG }, { "sin\u00e1", Language.TAGALOG }, { "'t", Language.DUTCH, Language.FRISIAN }, { "ta", Language.CLASSICAL_GREEK, Language.GREEK }, { "tais", Language.CLASSICAL_GREEK }, { "tas", Language.CLASSICAL_GREEK }, { "t\u0113", Language.CLASSICAL_GREEK }, { "t\u0113n", Language.CLASSICAL_GREEK, Language.GREEK }, { "t\u0113s", Language.CLASSICAL_GREEK, Language.GREEK }, { "the", Language.ENGLISH }, { "t\u014d", Language.CLASSICAL_GREEK, Language.GREEK }, { "tois", Language.CLASSICAL_GREEK }, { "t\u014dn", Language.CLASSICAL_GREEK, Language.GREEK }, { "tou", Language.CLASSICAL_GREEK, Language.GREEK }, { "um", Language.PORTUGUESE }, { "uma", Language.PORTUGUESE }, { "un", Language.CATALAN, Language.FRENCH, Language.ITALIAN, Language.PROVENCAL_OCCITAN, Language.ROMANIAN, Language.SPANISH }, { "un'", Language.ITALIAN }, { "una", Language.CATALAN, Language.ITALIAN, Language.PROVENCAL_OCCITAN, Language.SPANISH }, { "une", Language.FRENCH }, { "unei", Language.ROMANIAN }, { "unha", Language.GALICIAN }, { "uno", Language.ITALIAN, Language.PROVENCAL_OCCITAN }, { "uns", Language.PROVENCAL_OCCITAN }, { "unui", Language.ROMANIAN }, { "us", Language.PROVENCAL_OCCITAN }, { "y", Language.WELSH }, { "ye", Language.ENGLISH }, { "yr", Language.WELSH } };
        ianaArticleMap = new HashMap();
        int wordIdx = 0;
        int langIdx = 0;
        Map langWordMap = new HashMap();
        for (wordIdx = 0; wordIdx < articleWordArray.length; wordIdx++) {
            for (langIdx = 1; langIdx < articleWordArray[wordIdx].length; langIdx++) {
                Language lang = (Language) articleWordArray[wordIdx][langIdx];
                if (lang != null && lang.IANA.length() > 0) {
                    List words = (List) langWordMap.get(lang);
                    if (words == null) {
                        words = new ArrayList();
                        langWordMap.put(lang, words);
                    }
                    if (!words.contains(articleWordArray[wordIdx][0]))
                        words.add(articleWordArray[wordIdx][0]);
                }
            }
        }
        Iterator langIter = langWordMap.keySet().iterator();
        while (langIter.hasNext()) {
            Language lang = (Language) langIter.next();
            List wordList = (List) langWordMap.get(lang);
            String[] words = new String[wordList.size()];
            for (int idx = 0; idx < wordList.size(); idx++) words[idx] = (String) wordList.get(idx);
            Arrays.sort(words, new MARC21InitialArticleWord.InverseLengthComparator());
            ianaArticleMap.put(lang.IANA, new MARC21InitialArticleWord.ArticlesForLang(lang, words));
        }
    }

    private static class ArticlesForLang {

        final Language lang;

        final String[] words;

        ArticlesForLang(Language lang, String[] words) {
            this.lang = lang;
            this.words = words;
        }
    }

    private static class InverseLengthComparator implements Comparator {

        public int compare(Object arg0, Object arg1) {
            return ((String) arg1).length() - ((String) arg0).length();
        }
    }
}
