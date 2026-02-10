@echo off
setlocal EnableExtensions
set "SCRIPT_DIR=%~dp0"
set "LOG_DIR=%SCRIPT_DIR%logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-3 delims=/- " %%a in ("%date%") do (
  set "DATE_STAMP=%%c%%a%%b"
)
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (
  set "TIME_STAMP=%%a%%b%%c"
)
set "LOG_FILE=%LOG_DIR%\run_%DATE_STAMP%_%TIME_STAMP%.log"
echo Starting... > "%LOG_FILE%"
echo Log: %LOG_FILE%
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "& { python '%SCRIPT_DIR%start.py' 2>&1 | Tee-Object -FilePath '%LOG_FILE%' }"
endlocal
