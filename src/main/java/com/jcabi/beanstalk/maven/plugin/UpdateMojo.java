/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import javax.validation.constraints.NotNull;

/**
 * Update WAR artifact in AWS Elastic Beanstalk to a new version.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7.1
 * @goal update
 * @phase deploy
 */
@Loggable(Loggable.INFO)
public final class UpdateMojo extends AbstractBeanstalkMojo {

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(@NotNull final Application app,
        @NotNull final Version version, @NotNull final String template) {
        Environment primary;
        if (app.hasPrimary()) {
            primary = app.primary();
            primary.update(version);
        } else {
            app.clean(false);
            primary = app.candidate(version, template);
        }
        if (!this.isGreen(primary)) {
            this.postMortem(primary);
            throw new DeploymentException(
                String.format(
                    "failed to deploy since %s never got GREEN", primary
                )
            );
        }
        Logger.info(
            this,
            "Environment '%s' successfully updated to '%s'",
            primary, version
        );
    }

}
