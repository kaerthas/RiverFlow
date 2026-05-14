# 工具类清单与扩展规范

## 现有工具类

定义在 `src/styles/index.scss` 中。

### 页面布局

| 类名 | 样式 | 用途 |
|------|------|------|
| `.rf-page` | padding: 20px; min-height: calc(100vh - 56px); background: #F0F2F5 | 页面内容区包裹 |
| `.rf-card` | background: #fff; border-radius: 8px; padding: 20px; box-shadow | 卡片容器 |
| `.rf-page-title` | font-size: 20px; font-weight: 600; margin-bottom: 20px | 页面标题 |

## 扩展规范

新增工具类需遵循命名规则：`.rf-{类别}-{修饰}`

### 建议扩展类别

```scss
// 间距
.rf-mb-{n}    // margin-bottom: n px
.rf-mt-{n}    // margin-top
.rf-p-{n}     // padding

// Flex 布局
.rf-flex        // display: flex
.rf-flex-between // justify-content: space-between; align-items: center
.rf-flex-center  // justify-content: center; align-items: center

// 文字
.rf-text-ellipsis  // 单行省略
.rf-text-main      // color: var(--rf-text-main)
.rf-text-secondary // color: var(--rf-text-secondary)
.rf-text-muted     // color: var(--rf-text-muted)

// 状态色
.rf-text-primary   // color: var(--rf-primary)
.rf-text-success   // color: var(--rf-success)
.rf-text-warning   // color: var(--rf-warning)
.rf-text-danger    // color: var(--rf-danger)
```

### 添加新工具类流程

1. 确认是否已有同类工具类可复用
2. 遵循命名规范，使用 CSS 变量而非硬编码
3. 添加到 `src/styles/index.scss`
4. 在组件模板中优先使用类名，减少 scoped style 代码量
