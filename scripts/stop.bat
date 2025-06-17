@echo off
REM 一键关闭前后端脚本（Windows）

REM 关闭后端
for /f "tokens=2" %%a in ('wmic process where "CommandLine like '%%processengine-backend-0.0.1-SNAPSHOT.jar%%'" get ProcessId /value ^| find "ProcessId="') do (
    set BACKEND_PID=%%a
    taskkill /PID %%a /F
    echo [OK] 后端已关闭，PID: %%a
    goto :AFTER_BACKEND
)
echo [INFO] 未检测到后端进程
:AFTER_BACKEND

REM 关闭前端
for /f "tokens=2" %%a in ('wmic process where "CommandLine like '%%npm%%run%%dev%%'" get ProcessId /value ^| find "ProcessId="') do (
    set FRONTEND_PID=%%a
    taskkill /PID %%a /F
    echo [OK] 前端已关闭，PID: %%a
    goto :AFTER_FRONTEND
)
echo [INFO] 未检测到前端进程
:AFTER_FRONTEND 