@echo off
chcp 65001 >nul
cls

echo ======================================
echo   每日打卡 - APK 构建脚本 (Windows)
echo ======================================
echo.

REM 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Java，请先安装 JDK 17
    pause
    exit /b 1
)

echo [1/3] 清理旧构建...
call gradlew.bat clean --no-daemon 2>nul

echo.
echo [2/3] 构建调试版 APK...
call gradlew.bat assembleDebug --no-daemon

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ======================================
    echo   构建成功!
    echo   APK 位置: app\build\outputs\apk\debug\app-debug.apk
    echo ======================================
    
    REM 获取文件大小
    for %%I in ("app\build\outputs\apk\debug\app-debug.apk") do (
        echo   文件大小: %%~zI 字节
    )
) else (
    echo 错误: 构建失败
    pause
    exit /b 1
)

echo.
echo [3/3] 完成!
echo.
echo 安装到设备:
echo   adb install app\build\outputs\apk\debug\app-debug.apk
echo.

pause
