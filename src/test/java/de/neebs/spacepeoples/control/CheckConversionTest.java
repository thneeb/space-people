package de.neebs.spacepeoples.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckConversionTest {
    @Test
    void testSalutationConvert() {
        Assertions.assertEquals("Herr Dr.", "Mr. Dr.".replaceAll("Mr.", "Herr"));
    }

    @Test
    void textSubstring() {
        String s = "X1-DF55-20250Z";
        String result = s.substring(0, s.lastIndexOf("-"));
        Assertions.assertEquals("X1-DF55", result);
    }
}
