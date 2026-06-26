package project_biu.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplaceUntrustedCharsTest {

    @Test
    void htmlEscapes_EveryDangerousEntity() {
        assertEquals("&amp;&lt;&gt;&quot;&#39;", ReplaceUntrustedChars.html("&<>\"'"));
    }

    @Test
    void htmlEscapes_AmpersandFirstToAvoidDoubleEscaping() {
        assertEquals("&lt;script&gt;", ReplaceUntrustedChars.html("<script>"));
    }

    @Test
    void jsEscapes_BackslashQuotesAndNewlines() {
        assertEquals("\\\\", ReplaceUntrustedChars.js("\\"));
        assertEquals("\\'", ReplaceUntrustedChars.js("'"));
        assertEquals("\\n", ReplaceUntrustedChars.js("\n"));
    }

    @Test
    void js_TurnsAngleBracketsIntoUnicodeSoTagsCannotBreakOut() {
        assertEquals("\\u003Cb\\u003E", ReplaceUntrustedChars.js("<b>"));
    }

    @Test
    void removeNulls_ReturnsEmptyStringForNulls() {
        assertEquals("", ReplaceUntrustedChars.html(null));
        assertEquals("", ReplaceUntrustedChars.js(null));
    }
}
