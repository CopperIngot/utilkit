@echo off
setlocal EnableExtensions

REM Builds UtilKit with the JDK + Gradle staged in tools\.
REM Double-click or run from any terminal.

REM --- 1. Locate the project ------------------------------------------------
set "ROOT=C:\Users\bente\OneDrive\Documents\deepseek-harness\default-workspace"
if not exist "%ROOT%\tools" set "ROOT=%~dp0"
if not exist "%ROOT%\tools" (
    echo [x] Could not locate the project folder.
    echo     Looked in:
    echo       C:\Users\bente\OneDrive\Documents\deepseek-harness\default-workspace
    echo       %~dp0
    echo.
    pause
    exit /b 1
)
echo Project: %ROOT%

REM --- 2. Locate the JDK ----------------------------------------------------
set "JAVA_HOME="
for /d %%D in ("%ROOT%\tools\jdk-21\jdk-21*") do set "JAVA_HOME=%%D"
if not defined JAVA_HOME for /d %%D in ("%ROOT%\tools\jdk-21*") do set "JAVA_HOME=%%D"
if not defined JAVA_HOME (
    echo [x] No JDK found under "%ROOT%\tools". Contents:
    dir /b "%ROOT%\tools"
    echo.
    pause
    exit /b 1
)
echo JDK:     %JAVA_HOME%

REM --- 3. Locate Gradle -----------------------------------------------------
set "GRADLE_BIN="
for /d %%D in ("%ROOT%\tools\gradle-*") do if exist "%%D\bin\gradle.bat" set "GRADLE_BIN=%%D\bin"
if not defined GRADLE_BIN (
    echo [x] No Gradle found under "%ROOT%\tools".
    pause
    exit /b 1
)
echo Gradle:  %GRADLE_BIN%

set "PATH=%JAVA_HOME%\bin;%GRADLE_BIN%;%PATH%"

REM Gradle merges the Minecraft jars inside GRADLE_USER_HOME. OneDrive marks files
REM there as non-writable and Java's zip filesystem refuses to write, so keep the
REM cache on the local disk instead.
set "GRADLE_USER_HOME=%LOCALAPPDATA%\utilkit-gradle"
if not exist "%GRADLE_USER_HOME%" mkdir "%GRADLE_USER_HOME%"
if exist "%ROOT%\tools\gradle-home\caches" if not exist "%GRADLE_USER_HOME%\caches" (
    echo Copying existing Gradle cache out of OneDrive...
    robocopy "%ROOT%\tools\gradle-home" "%GRADLE_USER_HOME%" /E /NFL /NDL /NJH /NJS /NP >nul
)
if not exist "%GRADLE_USER_HOME%" (
    echo [!] Cannot create "%GRADLE_USER_HOME%", falling back to the workspace copy.
    set "GRADLE_USER_HOME=%ROOT%\tools\gradle-home"
)
echo Gradle home: %GRADLE_USER_HOME%

REM --- 4. Build -------------------------------------------------------------
cd /d "%ROOT%\utilkit" || (echo [x] Missing "%ROOT%\utilkit" & pause & exit /b 1)

echo.
echo Building... (first run downloads ~400 MB, this takes a few minutes)
echo.
call gradle.bat --no-daemon --console=plain -Dorg.gradle.native=false --stacktrace build
set "RESULT=%ERRORLEVEL%"

echo.
if "%RESULT%"=="0" (
    echo Build succeeded:
    dir /b build\libs\*.jar
) else (
    echo [x] Build failed with exit code %RESULT%.
)
pause
exit /b %RESULT%
