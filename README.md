<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />
 
[![Build Status](https://travis-ci.org/jcabi/jcabi-beanstalk-maven-plugin.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-beanstalk-maven-plugin)

More details are here:
[www.jcabi.com/jcabi-beanstalk-maven-plugin](http://www.jcabi.com/jcabi-beanstalk-maven-plugin/index.html)

The plugin automates deployment of Java WAR applications
to [AWS Elastic Beanstalk](http://aws.amazon.com/elasticbeanstalk/).
The plugin is designed with a minimalistic
approach, so that you don't need to provide a lot of configuration
options. Instead, there are a few conventions:

 * CNAME of a "primary" environment is always the same as the application name

 * Environments are configured only by
   [saved configuration templates](http://docs.amazonwebservices.com/elasticbeanstalk/latest/dg/using-features.managing.html#using-features.managing.saving)

 * Deployment is done either by
   [CNAME swap](http://docs.amazonwebservices.com/elasticbeanstalk/latest/dg/using-features.CNAMESwap.html)
   or version update

 * An application always contains only one "primary" environment.

Details are explained in [usage documentation](http://www.jcabi.com/jcabi-beanstalk-maven-plugin/index.html),
but in short it works like this:

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>com.jcabi</groupId>
        <artifactId>jcabi-beanstalk-maven-plugin</artifactId>
        <version>0.9</version>
        <configuration>
          <name>example</name>
          <bucket>webapps.example.com</bucket>
          <key>${project.artifactId}-${project.version}.war</key>
          <template>example</template>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>deploy</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-beanstalk-maven-plugin/issues/new).
If you want to discuss, please use our [Google Group](https://groups.google.com/forum/#!forum/jcabi).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
