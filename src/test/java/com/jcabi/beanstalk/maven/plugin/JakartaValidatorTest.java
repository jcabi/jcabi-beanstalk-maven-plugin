/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Executable validation must survive the switch from javax to Jakarta.
 *
 * @since 1.0
 */
final class JakartaValidatorTest {

    @Test
    void rejectsNullConstructorArgument() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new Application(null, "app"),
            "constructor arguments must be validated before the body"
        );
    }

    @Test
    void rejectsNullMethodArgumentBeforeCallingAws() {
        final AWSElasticBeanstalk client =
            Mockito.mock(AWSElasticBeanstalk.class);
        final Application app = new Application(client, "application");
        Assertions.assertThrows(
            ConstraintViolationException.class, () -> app.swap(null),
            "method arguments must be validated before AWS requests"
        );
        Mockito.verifyNoInteractions(client);
    }

    @Test
    void validatesReturnValue() {
        Assertions.assertThrows(
            ConstraintViolationException.class, () -> new JakartaValidatorTest.Response().value(),
            "annotated return values must be validated"
        );
    }

    @Test
    void interpolatesExpressionLanguage() {
        Assertions.assertEquals(
            "minimum 2", JakartaValidatorTest.invalidText()
                .getConstraintViolations().iterator().next().getMessage(),
            "messages must be interpolated by the Jakarta EL implementation"
        );
    }

    @Test
    void acceptsValidArgument() {
        Assertions.assertEquals(
            "valid", new JakartaValidatorTest.Response().accept("valid"),
            "valid method calls must still succeed"
        );
    }

    private static ConstraintViolationException invalidText() {
        return Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JakartaValidatorTest.Response().accept(""),
            "invalid arguments must produce Jakarta violations"
        );
    }

    /**
     * Constrained fixture woven with the same aspect as production classes.
     *
     * @since 1.0
     */
    static final class Response {

        /**
         * Invalid result.
         *
         * @return Null
         */
        @NotNull
        String value() {
            return null;
        }

        /**
         * Validated argument with an EL message.
         *
         * @param text Text to return
         * @return Text
         */
        String accept(@Size(min = 2, message = "minimum ${min + 0}")
            final String text) {
            return text;
        }
    }
}
