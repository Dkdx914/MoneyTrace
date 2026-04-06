@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   正在下载 GitHub CLI...
echo ========================================
echo.

echo 正在下载 gh.exe...
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/cli/cli/releases/latest/download/gh_2.52.0_windows_amd64.zip' -OutFile 'gh.zip'"

if not exist gh.zip (
    echo [错误] 下载失败
    pause
    exit /b 1
)

echo.
echo 正在解压...
powershell -Command "Expand-Archive -Path 'gh.zip' -DestinationPath '.' -Force"

echo.
echo 正在初始化 Git 仓库（简化版）...
cd /d c:\Users\刘\WorkBuddy\20260406125040\MoneyTrace
git init
git add .
git commit -m "Initial commit: MoneyTrace v1.2.0"

echo.
echo ========================================
echo   ✅ 准备完成！
echo ========================================
echo.
echo 现在需要你手动操作：
echo.
echo 1. 打开 https://github.com/new
echo 2. 创建一个名为 MoneyTrace 的新仓库
echo 3. 创建完成后，告诉我你的GitHub用户名
echo.
pause
