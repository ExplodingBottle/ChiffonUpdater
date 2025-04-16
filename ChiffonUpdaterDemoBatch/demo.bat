@echo off

echo This is the demo program, registering myself, please wait...
java -Dcuwrapper.command=regbin -Dcuwrapper.binaryFile="%~f0" -Dcuwrapper.logOnConsole=false -jar external.jar
echo Successfully registered myself!

pause
