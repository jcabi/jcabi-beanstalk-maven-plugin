package com.jcabi.beanstalk.maven.plugin;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the AbstractBeanstalkMojo class.
 * @author Dmitri Pisarenko (dp@altruix.co)
 * @version $Id$
 * @since 1.0
 */
public final class AbstractBeanstalkMojoTest {
    private class BeanstalkMojoForTesting extends AbstractBeanstalkMojo
    {
        @Override
        protected void exec(final Application app, final Version version,
            final String tmpl) {
        }
    }

    /**
     * Verifies that execute calls checkEbextensionsValidity method.
     */
    @Test
    public void executeCallscheckEbextensionsValidity()
        throws MojoFailureException {
        final AbstractBeanstalkMojo mojo = Mockito.spy(
            new BeanstalkMojoForTesting()
        );
        mojo.setSkip(false);
        Mockito.doNothing().when(mojo).checkEbextensionsValidity();
        mojo.execute();
        Mockito.verify(mojo).checkEbextensionsValidity();
    }
}
