# 音乐浏览器 - TCL安卓电视应用

专门为TCL安卓电视设计的网址收藏应用，用于播放在线音乐。

## 功能特性

- 🎵 专为电视遥控器优化的操作体验
- 📺 全屏无边框显示
- 🔖 保存和管理多个音乐网站
- 📱 支持32位和64位ARM架构
- 🌙 深色主题，护眼模式

## 预设网址

- WinLyrics 视频: `https://www.coren.xin/winlyrics/video`
- TuneFlow 音乐: `https://upyun.coren.xin/web/tuneflow.html`

## 安装方法

### 方法1: 下载预构建APK

1. 前往 [GitHub Actions](../../actions) 页面
2. 点击最新的构建
3. 下载 `app-debug` 或 `app-release-unsigned` 工件
4. 将APK传输到电视并安装

### 方法2: 本地构建（需要Android SDK）

```bash
# 构建Debug版本
./gradlew assembleDebug

# APK位置
app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

### 遥控器操作

| 按键 | 功能 |
|------|------|
| 方向键 | 在网址列表中导航 |
| 确认键 | 打开选中的网址 |
| 返回键 | 网页后退/退出应用 |
| 菜单键 | 快速添加新网址 |

### 管理网址

- **添加**: 点击右下角 `+` 按钮
- **编辑/删除**: 长按网址卡片
- **快速删除**: 点击卡片上的删除图标

## 技术规格

- 最低Android版本: 5.0 (API 21)
- 目标Android版本: 14 (API 34)
- 支持架构: armeabi-v7a, arm64-v8a, x86, x86_64

## 开发

项目使用Kotlin开发，采用Material Design组件。

```
app/
├── src/main/
│   ├── java/com/coren/tvbrowser/
│   │   ├── MainActivity.kt      # 主界面
│   │   ├── WebViewActivity.kt   # 网页浏览
│   │   └── UrlManager.kt        # 数据管理
│   └── res/                     # 资源文件
```

## 许可证

MIT License
