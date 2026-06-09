package project_biu.utils;

import java.util.Locale;

/**
 * Formats numeric topic values for display.
 */
public final class NumberFormatter {
    private NumberFormatter() {
    }

    public static String format(double value, String fallback) {
        if (Double.isNaN(value)) return fallback;
        return String.format(Locale.ROOT, "%.4f", value);
    }
}