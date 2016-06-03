@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  scan.cli.wrapper startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal enabledelayedexpansion enableextensions

set TEMP_FILE=%TEMP%\SCAN_CLI.TMP

@rem Add default JVM options here. You can also use JAVA_OPTS and SCAN_CLI_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=-Xms128m -Xmx512m

set WMIC_NAME=wmic
set WMIC_EXE=
for %%P in (%PATHEXT%) do (
    for %%I in (%WMIC_NAME% %WMIC_NAME%%%P) do (
        if exist "%%~$PATH:I" (
            set WMIC_EXE=%%~$PATH:I
            goto :runwmic
        )
    )
)
:runwmic

if defined ProgramFiles(x86) (
    @rem Windows 64-bit OS

    if "%WMIC_EXE%"=="" goto :runsysteminfo

    if "%TRACE%"=="1" echo "WMIC_EXE=%WMIC_EXE%"
    for /f "skip=1" %%p in ('wmic os get freephysicalmemory') do (
        set AVAILABLE_RAM_KB=%%p
        goto :done1
    )
    goto :start

    :runsysteminfo
    systeminfo | find "Available Physical Memory" >%TEMP_FILE%
    for /f "tokens=4" %%p in ('type %TEMP_FILE%') do (
        set AVAILABLE_RAM=%%p
        goto :done2
    )
    del /Q /F %TEMP_FILE%
    goto :start
) else (
    @rem Windows 32-bit OS

    goto :start
)

:done1
if "%TRACE%"=="1" echo "using wmic"
set /a AVAILABLE_RAM=AVAILABLE_RAM_KB/1024
if "%TRACE%"=="1" echo "AVAILABLE_RAM=%AVAILABLE_RAM%"
goto :done

:done2
del /Q /F %TEMP_FILE%
if "%TRACE%"=="1" echo "using systeminfo"
set AVAILABLE_RAM=%AVAILABLE_RAM:,=%
if "%TRACE%"=="1" echo "AVAILABLE_RAM=%AVAILABLE_RAM%"

:done
set MAX_MX_VALUE=4096
set /a MX=AVAILABLE_RAM/2
set Nmbr2=%MX%
set DEFAULT_JVM_OPTS=-Xms256m -Xmx%MAX_MX_VALUE%m
if %Nmbr2% LSS %MAX_MX_VALUE% set DEFAULT_JVM_OPTS=-Xms256m -Xmx%Nmbr2%m
if "%TRACE%"=="1" echo "DEFAULT_JVM_OPTS=%DEFAULT_JVM_OPTS%"
goto :start

:start
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_NAME=java
for %%P in (%PATHEXT%) do (
  for %%I in (%JAVA_NAME% %JAVA_NAME%%%P) do (
    if exist "%%~$PATH:I" (
      echo %%~$PATH:I >%TEMP_FILE%
      goto GetJavaExe
    )
  )
)
echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:GetJavaExe
set /p JAVA_EXE= <%TEMP_FILE%
del /Q /F %TEMP_FILE%
for %%A in ("%JAVA_EXE%") do (
    set JAVA_BIN=%%~dpA
    set JAVA_NAME=%%~nxA
)
set JAVA_FAKE_PATH="%JAVA_BIN%..\%JAVA_NAME%"
call :dir_name_from_path JAVA_HOME !JAVA_FAKE_PATH!

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set SCAN_CLI_OPTS=-Done-jar.silent=true -Done-jar.jar.path="%APP_HOME%\lib\cache\scan.cli.impl-standalone.jar" %SCAN_CLI_OPTS%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\scan.cli-2.4.2-standalone.jar

@rem Execute scan.cli.wrapper
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SCAN_CLI_OPTS%  -jar "%CLASSPATH%"  %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:dir_name_from_path <resultVar> <pathVar>
(
    set "%~1=%~dp2"
    REM set "%~1=%~nx2"
    exit /b
)

:fail
rem Set variable SCAN_CLI_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%SCAN_CLI_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
