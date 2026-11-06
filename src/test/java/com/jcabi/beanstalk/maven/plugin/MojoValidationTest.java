/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Both real mojo bodies must retain logging and inherited validation.
 *
 * @since 1.0
 */
final class MojoValidationTest {

    @ParameterizedTest
    @MethodSource("mojos")
    void executesWithInheritedConstraints(final AbstractBeanstalkMojo mojo) {
        final Application app = Mockito.mock(Application.class);
        final Environment env = Mockito.mock(Environment.class);
        final Version version = Mockito.mock(Version.class);
        final Log log = Mockito.mock(Log.class);
        Mockito.when(log.isInfoEnabled()).thenReturn(true);
        StaticLoggerBinder.getSingleton().setMavenLog(log);
        Mockito.when(app.hasPrimary()).thenReturn(true);
        Mockito.when(app.primary()).thenReturn(env);
        Mockito.when(app.candidate(version, "template")).thenReturn(env);
        Mockito.when(env.green()).thenReturn(true);
        Mockito.when(env.primary()).thenReturn(true);
        mojo.exec(app, version, "template");
        Mockito.verify(env).green();
        Mockito.verify(log).info(Mockito.contains("#exec("));
    }

    @ParameterizedTest
    @MethodSource("mojos")
    void rejectsNullApplication(final AbstractBeanstalkMojo mojo) {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> mojo.exec(null, Mockito.mock(Version.class), "template"),
            "inherited application constraint must precede the mojo body"
        );
    }

    @ParameterizedTest
    @MethodSource("mojos")
    void rejectsNullVersion(final AbstractBeanstalkMojo mojo) {
        final Application app = Mockito.mock(Application.class);
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> mojo.exec(app, null, "template"),
            "inherited version constraint must precede the mojo body"
        );
        Mockito.verifyNoInteractions(app);
    }

    @ParameterizedTest
    @MethodSource("mojos")
    void rejectsNullTemplate(final AbstractBeanstalkMojo mojo) {
        final Application app = Mockito.mock(Application.class);
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> mojo.exec(app, Mockito.mock(Version.class), null),
            "inherited template constraint must precede the mojo body"
        );
        Mockito.verifyNoInteractions(app);
    }

    private static Stream<AbstractBeanstalkMojo> mojos() {
        return Stream.of(new DeployMojo(), new UpdateMojo());
    }
}
