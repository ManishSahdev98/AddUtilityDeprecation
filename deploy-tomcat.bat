@echo off
REM Tomcat Deployment Script for Java Deprecation Utility
REM This script helps deploy the application to a standalone Tomcat server

echo Tomcat Deployment Script for Java Deprecation Utility
echo ========================================================

REM Configuration
set WAR_FILE=target\deprecation-utility.war
set TOMCAT_WEBAPPS=%TOMCAT_HOME%\webapps
set APP_NAME=deprecation-utility

REM Check if TOMCAT_HOME is set
if "%TOMCAT_HOME%"=="" (
    echo TOMCAT_HOME environment variable is not set
    echo    Please set TOMCAT_HOME to your Tomcat installation directory
    echo    Example: set TOMCAT_HOME=C:\apache-tomcat-9.0.xx
    pause
    exit /b 1
)

REM Check if WAR file exists
if not exist "%WAR_FILE%" (
    echo WAR file not found: %WAR_FILE%
    echo    Please run 'mvn clean package' first
    pause
    exit /b 1
)

REM Check if Tomcat webapps directory exists
if not exist "%TOMCAT_WEBAPPS%" (
    echo Tomcat webapps directory not found: %TOMCAT_WEBAPPS%
    echo    Please check your TOMCAT_HOME setting
    pause
    exit /b 1
)

echo Deploying %APP_NAME% to Tomcat...
echo    Source: %WAR_FILE%
echo    Destination: %TOMCAT_WEBAPPS%\%APP_NAME%.war

REM Stop Tomcat if running
echo Stopping Tomcat...
cd /d "%TOMCAT_HOME%\bin"
call shutdown.bat
timeout /t 5 /nobreak >nul

REM Remove existing deployment
if exist "%TOMCAT_WEBAPPS%\%APP_NAME%" (
    echo Removing existing deployment...
    rmdir /s /q "%TOMCAT_WEBAPPS%\%APP_NAME%"
)

if exist "%TOMCAT_WEBAPPS%\%APP_NAME%.war" (
    echo Removing existing WAR file...
    del /q "%TOMCAT_WEBAPPS%\%APP_NAME%.war"
)

REM Copy new WAR file
echo Copying WAR file...
copy "%WAR_FILE%" "%TOMCAT_WEBAPPS%\"

REM Start Tomcat
echo Starting Tomcat...
cd /d "%TOMCAT_HOME%\bin"
call startup.bat

REM Wait for deployment
echo Waiting for deployment to complete...
timeout /t 10 /nobreak >nul

REM Check if application is accessible
echo Testing application...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/%APP_NAME%/' -UseBasicParsing; if ($response.StatusCode -eq 200) { Write-Host 'Deployment successful!' } else { Write-Host 'Deployment failed' } } catch { Write-Host 'Application not accessible' }"

echo.
echo Application should be available at: http://localhost:8080/%APP_NAME%/
echo Open your browser and navigate to the URL above
echo.
echo Useful commands:
echo    - View logs: tail -f %TOMCAT_HOME%\logs\catalina.out
echo    - Stop Tomcat: %TOMCAT_HOME%\bin\shutdown.bat
echo    - Start Tomcat: %TOMCAT_HOME%\bin\startup.bat
echo    - Access app: http://localhost:8080/%APP_NAME%/

pause

