---
name: frontend-style-optimize
description: RiverFlow 前端样式优化与规范化。Use when: (1) 修改或新增 Vue 组件的样式代码, (2) 调整 Element Plus 主题或自定义主题变量, (3) 扩展全局 SCSS 工具类或样式规范, (4) 优化页面布局、间距、色彩等视觉表现, (5) 处理响应式适配或滚动条/动画等交互样式, (6) 审查现有组件的样式质量并提出改进建议。
---

# RiverFlow 前端样式优化

## 技术栈

- Vue 3 + Vite
- Element Plus 2.x
- Sass (SCSS)
- CSS Variables 主题系统

## 核心原则

1. **变量优先**：所有颜色、间距、尺寸优先使用 CSS 变量或 SCSS 变量，禁止硬编码
2. **工具类复用**：优先复用现有工具类（`.rf-page`、`.rf-card`、`.rf-page-title`），避免重复声明相同样式
3. **组件隔离**：组件内部样式使用 `<style scoped>`，全局覆盖放在 `styles/` 目录
4. **Element Plus 定制**：通过 CSS 变量覆盖主题，而非直接修改组件内部选择器

## 样式文件组织

```
src/styles/
├── index.scss          # 全局入口（已存在）：变量、滚动条、工具类
├── element-override.scss  # Element Plus 组件覆盖（按需创建）
├── mixins.scss         # SCSS mixin（按需创建）
└── animation.scss      # 全局动画（按需创建）
```

## 工作流

### 1. 新增/修改组件样式

- 检查 `references/variables.md` 确认可用变量
- 组件模板优先使用现有工具类
- `<style scoped lang="scss">` 中通过变量引用颜色/尺寸
- 如需新变量，先在 `index.scss` :root 中定义，再使用

### 2. 主题/变量调整

- 查看 `references/variables.md` 了解现有变量语义和命名规则
- 修改 `src/styles/index.scss` 中的 `:root` 变量
- 同步检查 Element Plus 变量是否需要覆盖

### 3. 工具类扩展

- 查看 `references/utilities.md` 了解现有工具类
- 遵循命名规范：`.rf-{类别}-{修饰}` 前缀
- 新工具类添加到 `src/styles/index.scss`

### 4. Element Plus 定制

- 优先使用 CSS 变量覆盖（`--el-*`）
- 复杂覆盖创建 `src/styles/element-override.scss` 并在 `main.js` 引入
- 参考 `references/component-patterns.md` 中的覆盖模式

## References

- **变量规范与主题系统**：`references/variables.md`
- **工具类清单与扩展规范**：`references/utilities.md`
- **组件样式模式与 Element Plus 覆盖**：`references/component-patterns.md`
