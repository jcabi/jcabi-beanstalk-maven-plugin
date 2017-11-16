<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![Managed by Zerocracy](http://www.0crat.com/badge/C3RUBL5H9.svg)](http://www.0crat.com/p/C3RUBL5H9)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-beanstalk-maven-plugin)](http://www.rultor.com/p/jcabi/jcabi-beanstalk-maven-plugin)

[![Build Status](https://travis-ci.org/jcabi/jcabi-beanstalk-maven-plugin.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-beanstalk-maven-plugin)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-beanstalk-maven-plugin)](http://www.0pdd.com/p?name=jcabi/jcabi-beanstalk-maven-plugin)
[![Build status](https://ci.appveyor.com/api/projects/status/rudkdp50i862rhbh/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/jcabi-beanstalk-maven-plugin/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-beanstalk-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-beanstalk-maven-plugin)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-beanstalk-maven-plugin.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-beanstalk-maven-plugin)

More details are here:
[beanstalk.jcabi.com](http://beanstalk.jcabi.com/index.html)

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

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
