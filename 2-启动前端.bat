@echo off
title 仓库库存管理系统 - 前端服务
cd /d "%~dp0frontend"
echo ============================================
echo   正在启动前端服务，请稍候
echo   看到 Local: http://localhost:5173/ 即启动成功
echo   启动成功后请不要关闭本窗口
echo ============================================
echo.
if not exist "node_modules" goto install
goto run
:install
echo 首次运行，正在安装前端依赖，需要几分钟...
call npm install
:run
call npm run dev
echo.
echo 前端服务已停止。
pause
exit /b 0
