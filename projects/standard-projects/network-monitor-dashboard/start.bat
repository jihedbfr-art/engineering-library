@echo off
title LAN Monitor
cd /d "%~dp0"
echo Demarrage de LAN Monitor...
echo Interface : http://127.0.0.1:8787
echo.
python app.py
pause
