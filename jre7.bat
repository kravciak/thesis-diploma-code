@echo off
echo Setting JAVA_HOME
set JAVA_HOME=c:\Program Files\Java\jre7
echo setting PATH
set PATH=%JAVA_HOME%\bin;%PATH%
echo Display java version
java -version
echo Done.