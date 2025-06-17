@echo off
REM 一键启动前后端脚本（Windows）

REM 启动后端
cd /d "%~dp0..\backend"
echo [INFO] 后端正在编译...
call mvn clean package -DskipTests
if not exist target\processengine-backend-0.0.1-SNAPSHOT.jar (
    echo [ERROR] 后端 jar 包未生成，启动失败
    exit /b 1
)
start "后端" cmd /c "java -jar target\processengine-backend-0.0.1-SNAPSHOT.jar > backend.log 2>&1"
cd /d "%~dp0"

REM 启动前端
cd /d "%~dp0..\frontend"
call npm install
start "前端" cmd /c "npm run dev > frontend.log 2>&1"
cd /d "%~dp0"

echo ----------------------------------------
echo 前端访问: http://localhost:5173/
echo 后端API:  http://localhost:8080/api/process-definitions
echo ---------------------------------------- 