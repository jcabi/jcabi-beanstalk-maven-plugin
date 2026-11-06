/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;

/**
 * Legacy JSON punctuation accepted by the original AWS JSON parser.
 * JSON-java validates the normalized text; YAML always receives the original.
 *
 * @since 1.0
 */
final class LegacyJson {

    /**
     * Quoted strings, unquoted literals, arrows and individual characters.
     * Parentheses inside unquoted literals are ordinary characters, as before.
     */
    private static final Pattern TOKENS = Pattern.compile(
        String.join(
            "",
            "(\"(?:\\\\.|[^\"\\\\])*+\"|'(?:\\\\.|[^'\\\\])*+'",
            "|[^\\x00-\\x20,:\\]}/\\\\\"\\[{;=#(')]",
            "[^\\x00-\\x1f,:\\]}/\\\\\"\\[{;=#]*)|=>|."
        ),
        Pattern.DOTALL
    );

    /**
     * Original text.
     */
    private final String text;

    /**
     * New adapter.
     *
     * @param original Original configuration
     */
    LegacyJson(final String original) {
        this.text = original;
    }

    /**
     * Translate punctuation outside strings and unquoted literals.
     * Like the original parser, ignore text following a complete container.
     * Preserve its early ']' terminator after '(' or an array separator.
     *
     * @return Text accepted by JSON-java
     */
    String normalized() {
        final StringBuilder result = new StringBuilder(this.text.length());
        final Deque<Character> closing = new ArrayDeque<>();
        final Matcher tokens = LegacyJson.TOKENS.matcher(this.text);
        boolean value = false;
        while (tokens.find()) {
            final String token = tokens.group();
            if (LegacyJson.structural(token, tokens.group(1), value)) {
                result.append(LegacyJson.punctuation(token, closing, value));
                value = "}])".contains(token);
                if (value && closing.isEmpty()) {
                    break;
                }
            } else {
                result.append(token);
                value = value || tokens.group(1) != null || ")".equals(token);
            }
        }
        return result.toString();
    }

    private static boolean structural(final String token, final String literal,
        final boolean value) {
        return literal == null && token.charAt(0) > ' ' && (value || !")".equals(token));
    }

    private static String punctuation(final String token,
        final Deque<Character> closing, final boolean value) {
        String result = token;
        final int opening = "{[(".indexOf(token);
        if (opening >= 0) {
            closing.push("}])".charAt(opening));
            result = "{[[".substring(opening, opening + 1);
        } else if ("}])".contains(token)) {
            result = LegacyJson.close(token, closing, value);
        } else if ("=".equals(token) || "=>".equals(token)) {
            result = ":";
        } else if (";".equals(token) && value) {
            result = ",";
        }
        return result;
    }

    private static String close(final String token, final Deque<Character> closing,
        final boolean value) {
        if (closing.isEmpty()) {
            throw new JSONException("Unexpected legacy JSON closing bracket");
        }
        final char expected = closing.pop();
        final boolean early = expected == ')' && "]".equals(token) && !value;
        if (expected != token.charAt(0) && !early) {
            throw new JSONException("Mismatched legacy JSON container");
        }
        String result = token;
        if (")".equals(token)) {
            result = "]";
        }
        return result;
    }
}
