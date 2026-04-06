@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   MoneyTrace - Git初始化助手
echo ========================================
echo.

set /p USERNAME="请输入你的GitHub用户名: "

echo.
echo 正在初始化Git仓库...
git init
if errorlevel 1 (
    echo [错误] Git未安装或不在PATH中
    echo 请从 https://git-scm.com/downloads 下载安装Git
    pause
    exit /b 1
)

echo.
echo 正在添加所有文件...
git add .

echo.
echo 正在创建提交...
git commit -m "Initial commit: MoneyTrace v1.2.0 with Cloud CI"

echo.
echo 正在配置远程仓库...
git remote add origin https://github.com/%USERNAME%/MoneyTrace.git
git branch -M main

echo.
echo ========================================
echo   ✅ Git初始化完成！
echo ========================================
echo.
echo 下一步操作：
echo.
echo 1. 在浏览器打开 https://github.com/new
echo 2. 创建一个名为 MoneyTrace 的新仓库
echo 3. 创建完成后，运行下面的命令上传代码：
echo.
echo    git push -u origin main
echo.
echo    （如果提示输入密码，输入GitHub Token，不是密码）
echo.
pause
