package com.voicecontrol.douyin;

public class KeywordMatcher {

    public enum Action {
        NEXT, PREV
    }

    /**
     * Match voice input to an action.
     * Returns null if no keyword found.
     */
    public static Action match(String text) {
        if (text == null) return null;
        // Remove filler characters for fuzzy matching
        String cleaned = text.replaceAll("[嗯啊呢吧哦呀了的嘞嘛诶]+", "").trim();
        String lower = cleaned.toLowerCase();

        // Next video keywords
        if (containsAny(lower, "下一个", "下一条", "下一个视频", "往下",
                "翻下去", "下面", "next", "下一个吧")) {
            return Action.NEXT;
        }
        // Previous video keywords
        if (containsAny(lower, "上一个", "上一条", "上一个视频", "往上",
                "翻上去", "上面", "previous", "上一个吧")) {
            return Action.PREV;
        }
        return null;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
