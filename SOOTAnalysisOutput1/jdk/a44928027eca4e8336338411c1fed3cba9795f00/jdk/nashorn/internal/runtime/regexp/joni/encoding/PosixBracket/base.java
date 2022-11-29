package jdk.nashorn.internal.runtime.regexp.joni.encoding;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ErrorMessages;
import jdk.nashorn.internal.runtime.regexp.joni.exception.JOniException;
import java.util.HashMap;

public class PosixBracket {

    public static final char[][] PBSNamesLower = { "alnum".toCharArray(), "alpha".toCharArray(), "blank".toCharArray(), "cntrl".toCharArray(), "digit".toCharArray(), "graph".toCharArray(), "lower".toCharArray(), "print".toCharArray(), "punct".toCharArray(), "space".toCharArray(), "upper".toCharArray(), "xdigit".toCharArray(), "ascii".toCharArray(), "word".toCharArray() };

    public static final int[] PBSValues = { CharacterType.ALNUM, CharacterType.ALPHA, CharacterType.BLANK, CharacterType.CNTRL, CharacterType.DIGIT, CharacterType.GRAPH, CharacterType.LOWER, CharacterType.PRINT, CharacterType.PUNCT, CharacterType.SPACE, CharacterType.UPPER, CharacterType.XDIGIT, CharacterType.ASCII, CharacterType.WORD };

    public static int propertyNameToCType(String name) {
        name = name.toLowerCase();
        if (!PBSTableUpper.containsKey(name)) {
            throw new JOniException(ErrorMessages.ERR_INVALID_CHAR_PROPERTY_NAME.replaceAll("%n", name));
        }
        return PBSTableUpper.get(name);
    }

    private static final HashMap<String, Integer> PBSTableUpper = new HashMap<String, Integer>();

    static {
        for (int i = 0; i < PBSValues.length; i++) PBSTableUpper.put(new String(PBSNamesLower[i]), PBSValues[i]);
    }
}
