package project_biu.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormatterTest {

    @Test
    void dropsTrailingZerosSoWholeNumbersStayClean() {
        assertEquals("3", NumberFormatter.format(3.0, "x"));
        assertEquals("3.5", NumberFormatter.format(3.5000, "x"));
        assertEquals("0", NumberFormatter.format(0.0, "x"));
        assertEquals("-2.25", NumberFormatter.format(-2.25, "x"));
    }

    @Test
    void roundsToFourDecimalsHalfUp() {
        assertEquals("1.2346", NumberFormatter.format(1.23455, "x"));
        assertEquals("0.0001", NumberFormatter.format(0.00005, "x"));
    }

    @Test
    void fallsBackWhenValueIsNotFinite() {
        assertEquals("fb", NumberFormatter.format(Double.NaN, "fb"));
        assertEquals("fb", NumberFormatter.format(Double.POSITIVE_INFINITY, "fb"));
        assertEquals("fb", NumberFormatter.format(Double.NEGATIVE_INFINITY, "fb"));
    }
}
