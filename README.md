# Mining Framework
Framework for mining git projects.

## Getting Started
This project uses [Apache Groovy](http://groovy-lang.org/). Install it to execute the program. This is the only requisite.

## Dependency Injection
This framework uses [Google Guice](https://github.com/spgroup/miningframework/blob/master/MiningModule.groovy) to deal with dependency injection.

It's necessary to extend three abstract classes:
* **Commit Filter** defines conditions (filter) to analyze a commit.
* **Statistics Collector** retrieves commits' metrics for statistical analysis.
* **Data Collector** retrieves the data one wants to study from the commits.

The [services/](https://github.com/spgroup/miningframework/tree/master/services) directory contains models for these dependencies. Also, the [MiningModule](https://github.com/spgroup/miningframework/blob/master/MiningModule.groovy) class acts as the dependency injector.

## Projects List
Another input file is a `.csv` file, that must contain information about the projects to be analyzed. Its lines should have the following structure (similar to the [projects](https://github.com/spgroup/miningframework/blob/master/projects.csv) file):

**output name**,**path**[,**relative**]

Where:
* **output name** refers to the name that should appear in output files;
* **path** is a local path or it's an url of a git project (https://github.com/...);
* **relative** (`true|false`), optional, indicates if **path** is a directory containing multiple projects or it is a project directory. The default is `false`.

## Running
One can run the framework by including `services/`, `src/main` and `MiningModule.groovy` in the classpath and executing `src/main/MiningFramework.groovy`.

This can be done by configuring an IDE or executing the following command in a terminal:
* Windows: `groovy -cp "services;src/main;MiningModule" src/main/MiningFramework.groovy [options] [input] [output]`
* Linux/Mac: `groovy -cp "services:src/main:MiningModule" src/main/MiningFramework.groovy [options] [input] [output]` 

`[input]` is a mandatory argument and refers to the path of the projects list's file. It's useful to type `--help` in the `[options]` field to see more details, including information about parameterization of the input files.


