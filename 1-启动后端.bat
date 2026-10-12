@echo off
title 仓库库存管理系统 - 后端服务
cd /d "%~dp0backend"
echo ============================================
echo   正在启动后端服务，请稍候（约 8 秒）
echo   启动成功后请不要关闭本窗口
echo ============================================
echo.
if not exist "target\warehouse-system-1.0.0.jar" goto nojar
java -jar target\warehouse-system-1.0.0.jar
echo.
echo 后端服务已停止。
pause
exit /b 0
:nojar
echo [错误] 没有找到 target\warehouse-system-1.0.0.jar
echo 请先执行一次打包：mvn clean package -DskipTests
pause
exit /b 1
