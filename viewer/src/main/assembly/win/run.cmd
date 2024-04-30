@echo off
set DIR=%~dp0%.
cd %DIR%

set JAVA_OPTS=-Xmx8g

SET COMMAND="dir /B /O-D journo-viewer-*.jar"
FOR /F "delims=" %%A IN ('%COMMAND%') DO (
    SET TEMPVAR=%%A
    GOTO :SetJar
)
:SetJar
set JAR=%TEMPVAR%
start javaw %JAVA_OPTS% -jar .\%JAR%