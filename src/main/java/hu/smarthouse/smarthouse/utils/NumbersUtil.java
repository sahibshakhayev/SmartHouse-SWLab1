package hu.smarthouse.smarthouse.utils;

public final class NumbersUtil {
//    private Numbers() {}

    public static int parse(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Expected a number, got: " + s); }
    }

    public static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}