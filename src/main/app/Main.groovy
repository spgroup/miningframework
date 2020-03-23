package main.app

import com.google.inject.*

import main.arguments.ArgsParser
import main.arguments.Arguments
import main.arguments.InputParser

import main.exception.InvalidArgsException
import main.project.Project

import main.util.FileManager

class Main {
        static main(args) {
        ArgsParser argsParser = new ArgsParser()
        try {
            Arguments appArguments = argsParser.parse(args)
            
            if (appArguments.isHelp()) {
                argsParser.printHelp()
            } else {
                Class injectorClass = appArguments.getInjector()
                Injector injector = Guice.createInjector(injectorClass.newInstance())
                MiningFramework framework = injector.getInstance(MiningFramework.class)

                framework.setArguments(appArguments)

                FileManager.createOutputFiles(appArguments.getOutputPath(), appArguments.isPushCommandActive())
            
                String inputPath = appArguments.getInputPath()
                ArrayList<Project> projectList = InputParser.getProjectList(inputPath)

                framework.setProjectList(projectList)
                framework.start()
            }
        } catch (InvalidArgsException e) {
            println e.message
            println 'Run the miningframework with --help to see the possible arguments'
        }
    }
}