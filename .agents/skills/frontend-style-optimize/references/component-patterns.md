# 组件样式模式与 Element Plus 覆盖

## Vue 单文件组件样式规范

```vue
<template>
  <!-- 优先使用工具类 -->
  <div class="rf-page">
    <div class="rf-card my-component">
      <!-- ... -->
    </div>
  </div>
</template>

<style scoped lang="scss">
.my-component {
  // 使用 CSS 变量
  border: 1px solid var(--rf-border-color);
  
  // 嵌套选择器保持简洁
  .header {
    margin-bottom: 16px;
  }
}
</style>
```

## Element Plus 覆盖模式

### 模式 1：CSS 变量覆盖（推荐）

适用于颜色、圆角、字号等主题属性。

```scss
// styles/element-override.scss
:root {
  --el-color-primary: var(--rf-primary);
  --el-button-primary-bg-color: var(--rf-primary);
  --el-menu-active-color: var(--rf-primary);
}
```

### 模式 2：全局选择器覆盖

适用于结构性样式调整。

```scss
// styles/element-override.scss
.el-table {
  border-radius: 8px;
  
  th.el-table__cell {
    background-color: #fafafa;
    font-weight: 600;
  }
}
```

### 模式 3：组件内深度覆盖

仅当某个组件需要特殊样式时使用 `:deep()`。

```vue
<style scoped lang="scss">
.my-custom-table {
  :deep(.el-table__header) {
    background: var(--rf-bg-page);
  }
}
</style>
```

## 常见组件优化建议

### 表格（ElTable）

- 添加圆角和阴影包裹
- 表头加粗、背景区分
- 操作列按钮紧凑排列

### 表单（ElForm）

- 统一标签宽度或右对齐
- 输入框聚焦色使用主色变量
- 表单项间距保持一致（16-20px）

### 对话框（ElDialog）

- 标题区域底部加分割线
- 内容区最小高度和最大高度限制
- 底部操作按钮右对齐

### 按钮（ElButton）

- 主操作使用 primary，次要操作用 default
- 危险操作用 danger 并确认
- 图标按钮保持统一尺寸

## 动画规范

如需添加过渡动画：

```scss
// 统一缓动函数
$ease-out: cubic-bezier(0.33, 1, 0.68, 1);
$ease-in-out: cubic-bezier(0.65, 0, 0.35, 1);

// 常用过渡
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s $ease-out;
}
```

建议集中放在 `src/styles/animation.scss`。

## 响应式断点

```scss
$breakpoint-sm: 768px;
$breakpoint-md: 992px;
$breakpoint-lg: 1200px;

@mixin respond-to($breakpoint) {
  @media (max-width: $breakpoint) {
    @content;
  }
}
```

如需响应式支持，创建 `src/styles/mixins.scss` 并引入到各组件。
