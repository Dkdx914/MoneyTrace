# 🚀 最简单方式：云CI自动构建APK

这是**最简单的方案**：在云端自动构建APK，你直接下载安装。

---

## 📋 操作步骤（5分钟）

### 第一步：上传代码到GitHub

1. 打开 https://github.com/new
2. 仓库名填：`MoneyTrace`
3. 选择 **Public**（公开）或 **Private**（私有）
4. 点击 **Create repository**
5. 在新页面找到 "…or push an existing repository from the command line"，复制里面的命令
6. 打开Windows PowerShell，进入项目目录，粘贴执行

```powershell
# 进入项目目录
cd c:\Users\刘\WorkBuddy\20260406125040\MoneyTrace

# 上传到GitHub（替换YOUR_USERNAME为你的GitHub用户名）
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/MoneyTrace.git
git push -u origin main
```

---

### 第二步：触发自动构建

上传完成后：

1. 打开你的GitHub仓库页面
2. 点击顶部的 **Actions** 标签
3. 在左侧找到 **Build MoneyTrace APK**，点击它
4. 点击右上角的 **Run workflow** → **Run workflow**
5. 等待2-3分钟（构建中）

---

### 第三步：下载APK

1. 构建完成后，点击该次构建任务
2. 滚动到底部找到 **Artifacts** 部分
3. 点击 **MoneyTrace-APK** 下载
4. 解压下载的zip文件，里面就是 **app-release-unsigned.apk**

---

### 第四步：安装到手机

1. 把 **app-release-unsigned.apk** 发送到你的手机（微信、QQ、数据线都可以）
2. 在手机上找到这个文件，点击安装
3. 如果提示"需要允许安装未知来源应用"，在设置里开启

**完成！** 🎉

---

## 📱 你的手机适配情况

✅ **已适配：小米 HyperOS 3.0 / Android 16 (API 36)**

我已将目标SDK设置为API 36，完全兼容你的系统。

---

## 💡 为什么这么简单？

- **不用安装JDK** - 云端有
- **不用安装Android SDK** - 云端有  
- **不用配置环境变量** - 云端自动配置
- **不用Android Studio** - 一行命令就上传
- **自动构建** - 点一下就开始
- **直接下载** - APK自动生成

---

## 🔑 关于签名

第一次用这个方案会生成一个临时签名（debug签名），可以直接安装。

**正式发布时**：需要替换成正式签名证书，但先用这个没问题。

---

## ❓ 遇到问题？

### 问题1：git命令报错
**解决**：确保电脑安装了git。从 https://git-scm.com/downloads 下载安装。

### 问题2：push失败（认证错误）
**解决**：GitHub现在需要Personal Access Token。
1. 点击 GitHub头像 → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. 点击 **Generate new token (classic)**
3. 权限勾选 **repo**
4. 生成后复制token
5. 再次push时，用token代替密码：
   ```powershell
   git push -u origin main
   # 提示输入用户名时：输入GitHub用户名
   # 提示输入密码时：粘贴token
   ```

### 问题3：构建失败
**解决**：把构建失败的链接发给我，我帮你看。

---

**就这么简单！5分钟搞定！** ✅
