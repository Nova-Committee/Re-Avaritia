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

## 本次渲染 API 研究额外结论
- 在 MC 26.1.2 / NeoForge 26.1.2.15-beta 中，很多“老渲染类”其实**没有搬家**，仍然在原包名：`BakedQuad`、`ItemOverrides`、`ItemTransforms`、`ItemTransform`、`BlockElement`、`FaceBakery`、`ItemModelGenerator`、`BakedModel`、`LightTexture`、`RenderStateShard`、`GuiGraphics`、`PlayerRenderer`、`PlayerSkin`、`ShaderInstance`、`ItemRenderer`、`Zombie`。
- 真正需要替换的是 `ModelState`：新管线使用 `net.minecraft.client.renderer.block.dispatch.ModelState`，并由 NeoForge 的 `ModelStateExtension` 扩展。
- 旧的 NeoForge 几何 API 不是简单改包名，而是被重构：`IUnbakedGeometry`/`IGeometryLoader`/`IGeometryBakingContext` 这套旧接口被 `net.minecraft.client.resources.model.geometry.UnbakedGeometry`、`net.neoforged.neoforge.client.model.UnbakedModelLoader`、`UnbakedGeometryExtension`、`AbstractUnbakedModel` 等新结构替代。
- `AbstractUniform` 已迁到 Mojang 的 `com.mojang.blaze3d.shaders.AbstractUniform`。
- `SimpleModelState` / `UnbakedGeometryHelper` 这两个 NeoForge 旧类在 26.1.2 源码包里未找到，实际对应/替代实现是 `ComposedModelState` / `UnbakedElementsHelper`。
- 26.1 文档页与 primer 都明确提示 26.1 是一次较大的 vanilla + NeoForge 迁移，模型加载系统与相关接口属于“重构”而非纯粹的包名重命名。
