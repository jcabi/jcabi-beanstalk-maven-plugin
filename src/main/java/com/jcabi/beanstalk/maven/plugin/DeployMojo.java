/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import javax.validation.constraints.NotNull;

/**
 * Deploys WAR artifact to AWS Elastic Beanstalk.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.3
 * @goal deploy
 * @phase deploy
 */
@Loggable(Loggable.INFO)
public final class DeployMojo extends AbstractBeanstalkMojo {

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(@NotNull final Application app,
        @NotNull final Version version, @NotNull final String template) {
        try {
            this.deploy(app, version, template);
        } catch (final DeploymentException ex) {
            app.clean(false);
            throw ex;
        }
    }

    /**
     * Deploy using this EBT client.
     * @param app The application to deploy to
     * @param version Version to deploy
     * @param template Template to use
     */
    private void deploy(final Application app, final Version version,
        final String template) {
        app.clean(false);
        final Environment candidate = app.candidate(version, template);
        if (this.isGreen(candidate)) {
            if (candidate.primary()) {
                Logger.info(
                    this,
                    "Candidate env '%s' is already primary, no need to swap",
                    candidate
                );
            } else {
                Logger.info(
                    this,
                    "Candidate env '%s' is not primary, let's swap",
                    candidate
                );
                app.swap(candidate);
            }
        } else {
            this.postMortem(candidate);
            throw new DeploymentException(
                String.format(
                    "failed to deploy, since %s never got GREEN", candidate
                )
            );
        }
    }

}
