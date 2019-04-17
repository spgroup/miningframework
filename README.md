# Mining Framework
Framework for mining git projects.

## Getting Started
* This project uses [Apache Groovy](http://groovy-lang.org/). Install it to execute the program
* It also uses a [Python](https://www.python.org/) script to convert the output to a SOOT compatible format. Install the version 3.7.x or newer to run with the SOOT output. 

* If you want to run the tests, you must use the command to clone the repository:
 ``` git clone --recursive https://github.com/spgroup/miningframework ```

## Dependency Injection
This framework uses [Google Guice](https://github.com/google/guice) to deal with dependency injection.

It's necessary to extend three abstract classes:
* **Commit Filter** defines conditions (filter) to analyze a commit.
* **Statistics Collector** retrieves commits' metrics for statistical analysis.
* **Data Collector** retrieves the data one wants to study from the commits.

The [services/](https://github.com/spgroup/miningframework/tree/master/src/services/) directory contains models for these dependencies. Also, the [MiningModule](https://github.com/spgroup/miningframework/blob/master/src/services/MiningModule.groovy) class acts as the dependency injector.

## Projects List
Another input file is a `.csv` file, that must contain information about the projects to be analyzed. Its lines should have the following structure (similar to the [projects](https://github.com/spgroup/miningframework/blob/master/projects.csv) file):

**output name**,**path**[,**relative**]

Where:
* **output name** refers to the name that should appear in output files;
* **path** is a local path or it's an url of a git project (https://github.com/...);
* **relative** (`true|false`), optional, indicates if **path** is a directory containing multiple projects or it is a project directory. The default is `false`.

## Running
One can run the framework by including `src` in the classpath and executing `src/main/script/MiningFramework.groovy`.

This can be done by configuring an IDE or executing the following command in a terminal:
* Windows/Linux/Mac: `groovy -cp src src/main/script/MiningFramework.groovy [options] [input] [output]`

`[input]` is a mandatory argument and refers to the path of the projects list's file. It's useful to type `--help` in the `[options]` field to see more details, including information about parameterization of the input files.

To get the SOOT framework output format execute the following command:
* Windows/Linux/Mac `groovy -cp src src/main/script/MiningFramework.groovy --post-script "python scripts/parse_to_soot.py [output] " [options] [input] [output]`

## Testing
One can the framework tests by including `src` in the classpath and executing `src/test/TestSuite.groovy`

This can be done by configuring an IDE or executing the following command in a terminal:
* Windows/Linux/Mac: `groovy -cp src src/test/TestSuite.groovy`

To create new tests, you have to create a git repository with a merge scenario simulating, add it to the `test_repositories` directory and add it to `src/test/input.csv` like a project and then create the Test class.


