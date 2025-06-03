# NOTION-CONTROLLER-RECORDER

*Maven-based plugin to deploy Spring Controller Documentation to Notion*

![Last Commit](https://img.shields.io/github/last-commit/pingmyheart/Notion-Controller-Recorder)
![Repo Size](https://img.shields.io/github/repo-size/pingmyheart/Notion-Controller-Recorder)
![Issues](https://img.shields.io/github/issues/pingmyheart/Notion-Controller-Recorder)
![Pull Requests](https://img.shields.io/github/issues-pr/pingmyheart/Notion-Controller-Recorder)
![License](https://img.shields.io/github/license/pingmyheart/Notion-Controller-Recorder)
![Top Language](https://img.shields.io/github/languages/top/pingmyheart/Notion-Controller-Recorder)
![Language Count](https://img.shields.io/github/languages/count/pingmyheart/Notion-Controller-Recorder)

## Why Notion-Controller-Recorder?

This project provides a Maven-based plugin that simplifies the process of documenting Spring Controllers in Notion. Key
features include:

- 📜 **Automatic Documentation**: Generates comprehensive documentation for Spring Controllers, including endpoints,
  parameters, and responses.
- 🔗 **Notion Integration**: Seamlessly integrates with Notion, allowing you to publish documentation directly to your
  Notion workspace.
- 🚀 **Productivity Boost**: Saves time by automating the documentation process, enabling developers to focus on coding
  rather than writing documentation.
- 🤝 **Open Source Collaboration**: Built under the MIT License, promoting innovation and community contributions.

# Getting started

## Installation

In order to use the Notion-Controller-Recorder plugin, you need to add it to your Maven project. Add the following
dependency to your `pom.xml`:

```xml

<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>

  <artifactId>your-project-artifact-id</artifactId>
  <version>your-project-version</version>
  ...
  <pluginRepositories>
    <pluginRepository>
      <id>github</id>
      <url>https://maven.pkg.github.com/pingmyheart/notion-controller-recorder</url>
    </pluginRepository>
  </pluginRepositories>
  ...
  <build>
    ...
    <plugins>
      ...
      <plugin>
        <groupId>io.github.pingmyheart</groupId>
        <artifactId>notion-controller-recorder-maven-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <configuration>
          <notionToken>${env.NOTION_TOKEN}</notionToken>
          <notionPageId>${env.NOTION_PAGE_ID}</notionPageId>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>generate</goal>
              <goal>deploy</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
  </build>
</project>
```

The other importa part is to set the environment variables but moreover set server inside `.m2/settings.xml` file:

```xml

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>${env.GITHUB_USERNAME}</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
```

## Usage

To use the Notion-Controller-Recorder it's enough to create a page inside notion and set the `NOTION_PAGE_ID`
environment
variable to the ID of that page. The most importa part is to give permission to the Notion API to write on that page.
The plugin will automatically generate a page with the project name and a inner page with the version of the project.