/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Jakarta executable validation alongside jcabi-aspects logging and caching.
 *
 * @since 1.0
 */
@Aspect
public final class JakartaValidator {

    /**
     * Validator shared for the lifetime of the plugin realm.
     */
    private final Validator validator = Validation
        .buildDefaultValidatorFactory().getValidator();

    /**
     * Validate constructor arguments before any constructor body runs.
     *
     * @param point Constructor invocation
     */
    @Before("preinitialization(com.jcabi.beanstalk.maven.plugin..*.new(.., @(jakarta.validation.* || jakarta.validation.constraints.*) (*), ..))")
    public void constructor(final JoinPoint point) {
        final Constructor<?> ctor = ((ConstructorSignature)
            point.getSignature()).getConstructor();
        JakartaValidator.check(
            this.validator.forExecutables().validateConstructorParameters(
                ctor, point.getArgs()
            )
        );
    }

    /**
     * Validate method arguments before execution.
     *
     * @param point Method invocation
     */
    @Before("execution(* com.jcabi.beanstalk.maven.plugin..*.*(.., @(jakarta.validation.* || jakarta.validation.constraints.*) (*), ..))")
    public void method(final JoinPoint point) {
        JakartaValidator.check(
            this.validator.forExecutables().validateParameters(
                point.getThis(),
                ((MethodSignature) point.getSignature()).getMethod(),
                point.getArgs()
            )
        );
    }

    /**
     * Validate annotated method return values.
     *
     * @param point Method invocation
     * @param result Returned value
     */
    @AfterReturning(
        pointcut = "execution(@(jakarta.validation.* || jakarta.validation.constraints.*) * com.jcabi.beanstalk.maven.plugin..*.*(..))",
        returning = "result"
    )
    public void returned(final JoinPoint point, final Object result) {
        JakartaValidator.check(
            this.validator.forExecutables().validateReturnValue(
                point.getThis(),
                ((MethodSignature) point.getSignature()).getMethod(), result
            )
        );
    }

    private static <T> void check(final Set<ConstraintViolation<T>> violations) {
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
