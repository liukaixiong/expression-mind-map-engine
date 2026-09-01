# 五彩气泡碰撞动画背景 - 使用说明

## 效果预览
- 15个五彩3D气泡在屏幕内自由碰撞
- 气泡会向鼠标位置靠近（250px影响半径）
- 边界碰撞和气泡间碰撞检测
- 半透明表格和表单，可看到背景气泡

## 快速使用

### 1. 引入CSS和JS
在HTML的 `<head>` 中添加：
```html
<link href="/css/bubble-animation.css" rel="stylesheet">
<script src="/js/bubble-animation.js"></script>
```

### 2. 添加body类
给 `<body>` 标签添加 `bubble-bg` 类：
```html
<body class="bubble-bg">
```

### 3. 添加气泡容器
在 `<body>` 开始处添加：
```html
<div id="bubbleContainer"></div>
```

## 完整示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>页面标题</title>
    <!-- 引入气泡背景CSS -->
    <link href="/css/bubble-animation.css" rel="stylesheet">
    <!-- 引入气泡背景JS -->
    <script src="/js/bubble-animation.js"></script>
</head>
<!-- 添加 bubble-bg 类 -->
<body class="bubble-bg">
    <!-- 气泡容器（必需） -->
    <div id="bubbleContainer"></div>

    <!-- 你的页面内容 -->
    <div class="content">
        <h1>页面标题</h1>
        <p>内容会自动显示在气泡上方</p>
    </div>
</body>
</html>
```

## 特性说明

### 自动适配
- 自动检测DOM加载完成，可在任何位置引入JS
- 窗口大小改变时自动重新定位气泡
- 所有页面内容自动显示在气泡上方（z-index自动处理）

### Layui表格支持
CSS已包含Layui表格的半透明样式：
- 表格容器：`rgba(255, 255, 255, 0.4)`
- 表头：`rgba(255, 255, 255, 0.5)`
- 表格行：`rgba(255, 255, 255, 0.3)`
- 悬停：`rgba(255, 255, 255, 0.5)`
- 分页条：`rgba(255, 255, 255, 0.4)`

### 鼠标交互
- 气泡会向鼠标位置加速移动
- 影响半径：250px
- 最大速度限制：4

### 气泡配置
- 数量：15个
- 大小：50-100px（随机）
- 颜色：8种五彩颜色（红、绿、黄、紫、蓝、粉、青、橙）
- 透明度：1.0 / 0.95（完全不透明）
- 3D效果：多层阴影 + 双高光反射

## JavaScript API

可以通过全局对象 `window.bubbleAnimation` 控制气泡：

```javascript
// 添加一个气泡
window.bubbleAnimation.addBubble();

// 删除一个气泡
window.bubbleAnimation.removeBubble();

// 设置气泡数量（动态调整）
window.bubbleAnimation.setBubbleCount(20);

// 访问气泡数组
console.log(window.bubbleAnimation.bubbles);

// 访问颜色配置
console.log(window.bubbleAnimation.colors);
```

## CSS类说明

| 类名 | 说明 |
|------|------|
| `bubble-bg` | 添加到body启用气泡背景 |
| `#bubbleContainer` | 气泡容器ID（必需） |
| `.bubble` | 气泡元素类 |
| `.layui-form` | 半透明表单 |
| `.layui-table` | 半透明表格 |

## 注意事项

1. **必需元素**：必须包含 `<div id="bubbleContainer"></div>`
2. **body类**：必须给body添加 `bubble-bg` 类
3. **DOM加载**：JS会自动等待DOM加载完成，无需手动处理
4. **性能**：15个气泡，适合大多数场景
5. **兼容性**：需要支持CSS3的浏览器

## 文件位置

- CSS: `/static/css/bubble-animation.css`
- JS: `/static/js/bubble-animation.js`
- 模板: `/template/bubble-bg-template.html`

## 已应用页面

- ✅ login.html - 登录页
- ✅ executor-list.html - 执行器列表

## 更多样式

如需自定义气泡样式，可覆盖以下CSS变量：

```css
:root {
    --primary-color: #3b82f6;
    --formula-purple: #8b5cf6;
}
```