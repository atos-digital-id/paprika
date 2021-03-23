
# Contributing

Hello,

So, you are considering to contribute to the project. That's very great. Welcome. You can found here some tips and advices you should know.

When contributing to this repository, please first discuss the change you wish to make via [issue](https://github.com/atos-digital-id/paprika/issues) with the owners of this repository before making a change.

Please note we have a code of conduct, follow it in all your interactions with the project.

## Building environment

To build this project, you need at least Java 11 and Maven 3.6.3.

## Coding style

The coding style is checked during the build of the source, via the `net.revelc.code.formatter:formatter-maven-plugin` plugin. You can use `mvn formatter:format` to format your code before compiling it.

## Docs

Please modify the documentation in line with the changes you make to the code. To generate the complete site, use the script `generate_site.sh`. You need (in addition of a building environment):

* [Make](http://www.gnu.org/software/make/)
* [Asymptote](https://asymptote.sourceforge.io/)

## Creating a pull request

To push your modification, please follow [the classical pull request workflow](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request).

