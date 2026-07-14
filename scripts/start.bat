@echo off
setlocal

cd /d "%~dp0.."

if /I "%~1"=="full" goto full
if /I "%~1"=="local" goto local
goto usage

:full
docker compose up --build -d
exit /b %errorlevel%

:local
docker compose stop processing-app repository-app >nul 2>&1
docker compose up -d --wait --wait-timeout 120 kafka
if errorlevel 1 exit /b 1
docker compose run --rm --no-deps kafka-topic-setup
exit /b %errorlevel%

:usage
echo Usage: scripts\start.bat ^<full^|local^>
echo   full  - runs Kafka and both applications in Docker
echo   local - runs Kafka in Docker; start both applications from the IDE
exit /b 1
