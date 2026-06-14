package project_biu.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats numeric values for display
 */
public final class NumberFormatter {
    private NumberFormatter() {
    }

    /**
     * Removing unnecessary zeros and rounding to 4 decimal places.
     *
     * @param value    the value to format
     * @param fallback the fallback value if the value is not finite or NaN
     * @return the formatted value
     */
    public static String format(double value, String fallback) {
        if (Double.isNaN(value) || !Double.isFinite(value)) return fallback;
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}