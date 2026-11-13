package com.example.urlshortener.util;

/**
 * Bijective base-62 encoding (0-9, a-z, A-Z). Maps database sequence IDs to short codes.
 * See docs/SYSTEM_DESIGN.md — "ID → short code".
 */
public final class Base62 {

    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private Base62() {}

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be non-negative");
        }
        if (value == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        long v = value;
        while (v > 0) {
            sb.append(ALPHABET[(int) (v % 62)]);
            v /= 62;
        }
        return sb.reverse().toString();
    }

    public static long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("encoded must not be empty");
        }
        long result = 0;
        for (int i = 0; i < encoded.length(); i++) {
            result = result * 62 + indexOf(encoded.charAt(i));
        }
        return result;
    }

    private static int indexOf(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'Z') {
            return c - 'A' + 36;
        }
        throw new IllegalArgumentException("invalid base62 character: " + c);
    }
}
