@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  miningframework startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and MININGFRAMEWORK_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\miningframework.jar;%APP_HOME%\lib\guice-4.2.2.jar;%APP_HOME%\lib\commons-io-2.6.jar;%APP_HOME%\lib\groovycsv-1.3.jar;%APP_HOME%\lib\groovy-cli-commons-3.0.0.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\guava-25.1-android.jar;%APP_HOME%\lib\opencsv-4.0.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\groovy-ant-3.0.0.jar;%APP_HOME%\lib\groovy-cli-picocli-3.0.0.jar;%APP_HOME%\lib\groovy-groovysh-3.0.0.jar;%APP_HOME%\lib\groovy-console-3.0.0.jar;%APP_HOME%\lib\groovy-datetime-3.0.0.jar;%APP_HOME%\lib\groovy-groovydoc-3.0.0.jar;%APP_HOME%\lib\groovy-docgenerator-3.0.0.jar;%APP_HOME%\lib\groovy-jmx-3.0.0.jar;%APP_HOME%\lib\groovy-json-3.0.0.jar;%APP_HOME%\lib\groovy-jsr223-3.0.0.jar;%APP_HOME%\lib\groovy-macro-3.0.0.jar;%APP_HOME%\lib\groovy-nio-3.0.0.jar;%APP_HOME%\lib\groovy-servlet-3.0.0.jar;%APP_HOME%\lib\groovy-sql-3.0.0.jar;%APP_HOME%\lib\groovy-swing-3.0.0.jar;%APP_HOME%\lib\groovy-templates-3.0.0.jar;%APP_HOME%\lib\groovy-test-3.0.0.jar;%APP_HOME%\lib\groovy-test-junit5-3.0.0.jar;%APP_HOME%\lib\groovy-testng-3.0.0.jar;%APP_HOME%\lib\groovy-xml-3.0.0.jar;%APP_HOME%\lib\groovy-3.0.0.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-compat-qual-2.0.0.jar;%APP_HOME%\lib\error_prone_annotations-2.1.3.jar;%APP_HOME%\lib\j2objc-annotations-1.1.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.14.jar;%APP_HOME%\lib\commons-text-1.1.jar;%APP_HOME%\lib\commons-lang3-3.6.jar;%APP_HOME%\lib\commons-beanutils-1.9.3.jar;%APP_HOME%\lib\ant-junit-1.10.7.jar;%APP_HOME%\lib\ant-1.10.7.jar;%APP_HOME%\lib\ant-launcher-1.10.7.jar;%APP_HOME%\lib\ant-antlr-1.10.7.jar;%APP_HOME%\lib\qdox-1.12.1.jar;%APP_HOME%\lib\javaparser-core-3.15.11.jar;%APP_HOME%\lib\jline-2.14.6.jar;%APP_HOME%\lib\junit-4.13.jar;%APP_HOME%\lib\junit-platform-launcher-1.6.0.jar;%APP_HOME%\lib\junit-jupiter-engine-5.6.0.jar;%APP_HOME%\lib\testng-6.14.3.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-collections-3.2.2.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\junit-platform-engine-1.6.0.jar;%APP_HOME%\lib\junit-jupiter-api-5.6.0.jar;%APP_HOME%\lib\junit-platform-commons-1.6.0.jar;%APP_HOME%\lib\apiguardian-api-1.1.0.jar;%APP_HOME%\lib\jcommander-1.72.jar;%APP_HOME%\lib\opentest4j-1.2.0.jar

@rem Execute miningframework
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %MININGFRAMEWORK_OPTS%  -classpath "%CLASSPATH%" app.Main %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable MININGFRAMEWORK_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%MININGFRAMEWORK_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
