@REM Maven Wrapper script for Windows

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar

if exist "%MAVEN_WRAPPER_JAR%" goto runMaven

echo Downloading Maven Wrapper...
mkdir "%MAVEN_PROJECTBASEDIR%.mvn\wrapper" 2>NUL
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%MAVEN_WRAPPER_JAR%')"

:runMaven
set JAVA_CMD=java
if defined JAVA_HOME set JAVA_CMD=%JAVA_HOME%\bin\java

%JAVA_CMD% %MAVEN_OPTS% -classpath "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
