# 变量规范与主题系统

## CSS 变量（:root）

定义在 `src/styles/index.scss` 的 `:root` 中，运行时动态生效。

### 色彩变量

| 变量名 | 默认值 | 用途 |
|--------|--------|------|
| `--rf-primary` | `#1677FF` | 主色，按钮、链接、高亮 |
| `--rf-primary-light` | `#E6F4FF` | 主色浅底，hover 背景、tag 背景 |
| `--rf-success` | `#52C41A` | 成功状态 |
| `--rf-warning` | `#FAAD14` | 警告状态 |
| `--rf-danger` | `#F5222D` | 危险/错误状态 |
| `--rf-info` | `#722ED1` | 信息提示 |
| `--rf-text-main` | `#262626` | 主标题文字 |
| `--rf-text-secondary` | `#595959` | 正文文字 |
| `--rf-text-muted` | `#8C8C8C` | 辅助文字、placeholder |
| `--rf-border-color` | `#D9D9D9` | 边框、分割线 |
| `--rf-bg-page` | `#F0F2F5` | 页面背景 |
| `--rf-bg-card` | `#FFFFFF` | 卡片背景 |

### 布局变量

| 变量名 | 默认值 | 用途 |
|--------|--------|------|
| `--rf-sidebar-width` | `220px` | 侧边栏展开宽度 |
| `--rf-sidebar-collapsed-width` | `64px` | 侧边栏折叠宽度 |
| `--rf-header-height` | `56px` | 顶部导航高度 |

### 使用示例

```scss
.my-component {
  color: var(--rf-text-main);
  border: 1px solid var(--rf-border-color);
  background: var(--rf-primary-light);
}
```

## Element Plus 主题变量

Element Plus 使用 `--el-*` 前缀的 CSS 变量。常用覆盖点：

```scss
:root {
  --el-color-primary: var(--rf-primary);
  --el-color-success: var(--rf-success);
  --el-color-warning: var(--rf-warning);
  --el-color-danger: var(--rf-danger);
  --el-border-radius-base: 6px;
  --el-font-size-base: 14px;
}
```

如需覆盖，在 `src/styles/element-override.scss` 中定义，并在 `main.js` 引入：

```js
import './styles/element-override.scss'
```

## 命名规范

- RiverFlow 自定义变量：`--rf-{语义}`，如 `--rf-primary`
- 扩展新变量时遵循现有分类：color、text、bg、border、shadow、spacing、layout
- 禁止在组件中硬编码色值，一律通过变量引用
