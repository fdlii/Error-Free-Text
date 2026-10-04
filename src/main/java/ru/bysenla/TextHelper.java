package ru.bysenla;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextHelper {
    private static final Pattern DIGITS = Pattern.compile("\\d");
    private static final Pattern URLS = Pattern.compile(
            "(?:https?://|ftp://|www\\.)\\S+"
                    + "|[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+"
                    + "|\\b[\\w-]+(?:\\.[\\w-]+)*\\.(?:ru|com|org|net|рф)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final int IGNORE_DIGITS = 2;
    private static final int IGNORE_URLS = 4;
    private static final int MAX_TEXT_LENGTH = 10000;

    public static int calculateOptions(String text) {
        int options = 0;
        if (DIGITS.matcher(text).find()) {
            options += IGNORE_DIGITS;
        }
        if (URLS.matcher(text).find()) {
            options += IGNORE_URLS;
        }
        return options;
    }

    public static String[] fragmentBigText(String text) {
        List<String> fragments = new ArrayList<>();
        int pos = 0;

        while (pos < text.length()) {
            int end = Math.min(pos + MAX_TEXT_LENGTH, text.length());
            if (end < text.length()) {
                int cut = end;
                while (cut > pos && !Character.isWhitespace(text.charAt(cut))) {
                    cut--;
                }
                if (cut > pos) {
                    end = cut;
                }
            }
            fragments.add(text.substring(pos, end));
            pos = end;
        }

        return fragments.toArray(new String[0]);
    }

    public static String replaceCorrectWords(String[] fragments, List<SpellError[]> errors) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fragments.length; i++) {
            int currentPos = 0;
            for (SpellError spellError : errors.get(i)) {
                if (spellError.pos() < currentPos) {
                    continue;
                }
                result.append(fragments[i], currentPos, spellError.pos());
                result.append(spellError.s().get(0));
                currentPos = spellError.pos() + spellError.word().length();
            }
            result.append(fragments[i], currentPos, fragments[i].length());
        }
        return result.toString();
    }
}
