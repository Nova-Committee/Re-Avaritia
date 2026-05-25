# Wave 0 预审阶段学习笔记

## 证据文件发现
- 审计文件分布在 `.sisyphus/evidence/` 目录，共 14 个文件
- 任务编号和实际文件编号不对应（如 0.1, 0.1b, 0.2a, 0.2b 等）
- mixin 审计在 `avaritia-mixin-audit.md` 而非 `task-0.8-mixin-audit.txt`
- AT 审计文件 (`task-0.9-at-audit.txt`) **不存在**，需要在后续补审

## 关键结论
- **Parchment**: 26.1.2 无对应版本，建议 Mojang 映射；但另有 1.21.11 Parchment 可选
- **Lombok**: 仅 5% 文件使用，无复杂注解，STRIP 时机成熟
- **Caps→Attachments**: 最高复杂度迁移项 (HIGH)
- **Mixins**: 11 个中 2 个 BROKEN (RecipeManager, ReloadableServerResources)，4 个需注意
- **依赖版本**: 坐标和旧版本完全不同，需全部更新

## 决策记录
- 优先顺序: Build系统 → Mixin修复 → Lombok剥离 → API迁移
- Parchment 建议用 Mojang 映射而非 1.21.11 版本（避免签名不匹配风险）
