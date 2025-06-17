@echo off
REM 一键重启前后端脚本（Windows）
call "%~dp0stop.bat"
timeout /t 2
call "%~dp0start.bat" 