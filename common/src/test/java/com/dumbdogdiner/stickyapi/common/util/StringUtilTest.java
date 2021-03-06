/*
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilTest {
    @Test
    public void testCreateProgressBarReducedArgs() {
        // Create the progress bar
        String bar = StringUtil.createProgressBar(10, 50);

        // Check size
        // Reduced arg adds brackets, so length should be 12.
        assertEquals(bar.length(), 12);

        // Check occurences of bar (should be 5)
        Pattern pattern = Pattern.compile("[^\u258D]*\u258D");
        Matcher matcher = pattern.matcher(bar);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        assertEquals(count, 5);
    }

    @Test
    public void testCreateProgressBar() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 100, true, true, true);

        // Check size - brackets are added so 12.
        assertEquals(bar.length(), 17, bar);
        // 10x '|'
        // 1x '['
        // 1x ']'
        // 1x ' '
        // 1x '%'
        // 3x percentage | '100'

        // Check occurences of bar (should be 10)
        Pattern pattern = Pattern.compile("[^\\|]*\\|");
        Matcher matcher = pattern.matcher(bar);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        assertEquals(count, 10);
    }

    @Test
    public void testCreateProgressBarNoBrackets() {
        String bar = StringUtil.createProgressBar(10, 50, false, false, false);
        assertEquals(bar.length(), 10);
    }

    @Test
    public void testCreateProgressBarStrictAssertFull() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 100, true, true, true);
        assertEquals(bar, "[||||||||||] 100%");
    }

    @Test
    public void testCreateProgressBarStrictAssertHalf() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 50, true, true, true);
        assertEquals(bar, "[|||||     ] 50%");
    }

    @Test
    public void testCreateProgressBarStrictAssertThird() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 33, true, true, true);
        assertEquals(bar, "[||||      ] 33%");
    }

    @Test
    public void testCreateProgressBarNoBracketsStrictAssertFull() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 100, false, false, false);
        assertEquals(bar, "\u258D\u258D\u258D\u258D\u258D\u258D\u258D\u258D\u258D\u258D");
    }

    @Test
    public void testCreateProgressBarNoBracketsStrictAssertHalf() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 50, false, false, false);
        assertEquals(bar, "\u258D\u258D\u258D\u258D\u258D     ");
    }

    @Test
    public void testCreateProgressBarNoBracketsStrictAssertThird() {
        // Brackets, Monospace, Percentage
        String bar = StringUtil.createProgressBar(10, 33, false, false, false);
        assertEquals(bar, "\u258D\u258D\u258D\u258D      ");
    }

    // capitaliseSentence<keepUpperCase>

    @Test
    public void testCapitaliseSentence() {
        assertEquals(StringUtil.capitaliseSentence("hello world"), "Hello World");
        assertEquals(StringUtil.capitaliseSentence("Hello World"), "Hello World");
        assertEquals(StringUtil.capitaliseSentence("Hello wOrld"), "Hello World");
    }

    @Test
    public void testCapitaliseSentenceKeepUpperCase() {
        assertEquals(StringUtil.capitaliseSentenceKeepUpperCase("hello world"), "Hello World");
        assertEquals(StringUtil.capitaliseSentenceKeepUpperCase("Hello World"), "Hello World");
        assertEquals(StringUtil.capitaliseSentenceKeepUpperCase("Hello wOrld"), "Hello WOrld");
    }

    // censorWord

    @Test
    public void testCensorWord() {
        assertEquals(StringUtil.censorWord("hi"), "**");
    }

    @Test
    public void testCensorWordRegex() {
        assertEquals(StringUtil.censorWord("hi 123", "[A-z]"), "hi****");
    }

    // replaceLeet

    @Test
    public void testReplaceLeetEmptyMsg() {
        assertEquals(StringUtil.replaceLeet(""), "");
    }

    @Test
    public void testReplaceLeet() {
        // From javadoc example.
        assertEquals(StringUtil.replaceLeet("50m3 1337 5p34k h3r3 0w0"), "some leet speak here owo");
    }

    // compareMany

    @Test
    public void testCompareMany() {
        assertTrue(StringUtil.compareMany("hello there", new String[] { "hello there" }));
        assertTrue(StringUtil.compareMany("hello there", new String[] { "Hello there" }));
        assertTrue(StringUtil.compareMany("hello there", new String[] { "hello there", "foxes" }));

        assertFalse(StringUtil.compareMany("hello there", new String[] { "hello ", "there" }));
        assertFalse(StringUtil.compareMany("hello there", new String[] { "goodbye" }));
    }

    // startsWithIgnoreCase

    @Test
    public void testStartsWithIgnoreCasePrefixLargerThanString() {
        // Test that value is false when prefix length is larger than string length.
        assertFalse(StringUtil.startsWithIgnoreCase("123", "12345"));
    }

    @Test
    public void testStartsWithIgnoreCaseValidTrue() {
        // Test functionality: true (string starts with prefix)
        assertTrue(StringUtil.startsWithIgnoreCase("hello world", "hello"));
    }

    @Test
    public void testStartsWithIgnoreCaseValidFalse() {
        // Test functionality: false (string does not start with prefix)
        assertFalse(StringUtil.startsWithIgnoreCase("hello world", "goodbye"));
    }

    @Test
    public void testStartsWithIgnoreCaseNullPointerExceptionPrefix() {
        assertThrows(NullPointerException.class, () -> {
            StringUtil.startsWithIgnoreCase("hello world", null);
        });
    }

    @Test
    public void testStartsWithIgnoreCaseNullPointerExceptionString() {
        assertThrows(NullPointerException.class, () -> {
            StringUtil.startsWithIgnoreCase(null, "hello");
        });
    }

    // hyphenateUUID

    @Test
    public void testHyphenateUUIDValid() {
        assertEquals("de8c89e1-2f25-424d-8078-c6ff58db7d6e",
                StringUtil.hyphenateUUID("de8c89e12f25424d8078c6ff58db7d6e").toString());
    }

    @Test
    public void testHyphenateUUIDValidLengthNot32() {
        assertEquals("de8c89e1-2f25-424d-8078-c6ff58db7d6e", StringUtil.hyphenateUUID("de8c89e1-2f25-424d-8078-c6ff58db7d6e").toString());
    }

    @Test
    public void testHyphenateUUIDInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtil.hyphenateUUID("invalid");
        });
    }

    @Test
    public void testHyphenateUUIDNull() {
        assertThrows(NullPointerException.class, () -> {
            StringUtil.hyphenateUUID(null);
        });
    }

    @RepeatedTest(100)
    public void testRandomObfuscatedString() {
        String rand = StringUtil.randomObfuscatedString(3,3, 0);
        assertTrue(StringUtil.startsWith(rand, ChatColor.MAGIC.toString()));
        System.out.println(rand);
        assertEquals(3, ChatColor.stripColor(rand).length());

        rand = StringUtil.randomObfuscatedString(1, 100, 0);
        assertTrue(ChatColor.stripColor(rand).length() >= 1 && ChatColor.stripColor(rand).length() <= 100);
        assertEquals(ChatColor.stripColor(rand).length(), rand.length() - 4);
        System.out.println(rand);
    }
}
