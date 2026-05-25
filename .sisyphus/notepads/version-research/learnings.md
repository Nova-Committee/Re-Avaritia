# 依赖版本研究 - 学到的模式

## 数据来源
- **JEI**: Maven 元数据 (`maven.blamejared.com`) 最权威，比 CurseForge 页面上的标签更及时
- **Jade**: 官方文档推荐 Modrinth Maven (`maven.modrinth:jade:version`)
- **Curios**: 官方 Maven (`maven.theillusivec4.top`) 包含完整版本历史
- **MDG**: Gradle Plugin Portal 和 NeoForge 官方博客确认版本信息

## Maven 坐标格式 (NeoForge 26.1.2 / MC 1.21.1)
- JEI: `mezz.jei:jei-${mc_version}-neoforge:${version}`（含 API 变体 `:api` classifier）
- Jade (Modrinth): `maven.modrinth:jade:${version}+neoforge`
- Curios: `top.theillusivec4.curios:curios-neoforge:${version}`（含 `:api` classifier）
- 均不需要 `fg.deobf()` 包装

## 版本对应关系
- JEI 版本号 `19.27.0.x` 对应 MC 1.21.1 NeoForge
- Curios 版本号 `9.x.x+1.21.1` 对应 MC 1.21.1
- Jade 版本号 `15.x.x+neoforge` 对应 MC 1.21.1
