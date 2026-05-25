# 依赖版本确认报告

> 目标环境: NeoForge 26.1.2 (Minecraft 1.21.1)
> 评审日期: 2026-05-25

---

## 1. Just Enough Items (JEI)

| 项目 | 内容 |
|------|------|
| **Mod** | Just Enough Items (JEI) |
| **作者** | mezz |
| **许可证** | MIT |
| **目标 MC 版本** | 1.21.1 (NeoForge) |
| **最新版本** | `19.27.0.340` |
| **Artifact** | `mezz.jei:jei-1.21.1-neoforge:19.27.0.340` |
| **API Artifact** | `mezz.jei:jei-1.21.1-neoforge-api:19.27.0.340` |
| **Maven 仓库** | `https://maven.blamejared.com/`（主）或 `https://modmaven.dev/`（镜像） |
| **CurseForge 项目 ID** | 238222 |
| **Gradle 配置** | 见下 |

### Gradle 依赖配置

```gradle
repositories {
    maven {
        name = "Jared's maven"
        url = "https://maven.blamejared.com/"
    }
    maven {
        name = "ModMaven"
        url = "https://modmaven.dev"
    }
}

dependencies {
    compileOnly "mezz.jei:jei-1.21.1-neoforge-api:19.27.0.340"
    runtimeOnly "mezz.jei:jei-1.21.1-neoforge:19.27.0.340"
}
```

### 版本来源

- **Maven 元数据**: `mezz.jei:jei-1.21.1-neoforge:maven-metadata.xml` 中 `<release>` 标签为 `19.27.0.340`
- **Modrinth**: 最新版 `19.27.0.340`（2026-01-05）
- **CurseForge**: Release `19.27.0.336`（2025-12-29）、Beta `19.27.0.340`（2026-01-05）
- 建议使用 Maven 主仓库的最新版 `19.27.0.340`

---

## 2. Jade

| 项目 | 内容 |
|------|------|
| **Mod** | Jade 🔍 |
| **作者** | Snownee |
| **许可证** | Custom License |
| **目标 MC 版本** | 1.21.1 (NeoForge) |
| **最新版本** | `15.10.5+neoforge` |
| **Jar 文件名** | `Jade-1.21.1-NeoForge-15.10.5.jar` |
| **Modrinth Maven** | `maven.modrinth:jade:15.10.5+neoforge` |
| **Curse Maven** | `curse.maven:jade-324717:7545219` |
| **CurseForge 项目 ID** | 324717 |
| **Gradle 配置** | 见下 |

### Gradle 依赖配置

**方式 A — Modrinth Maven（推荐）:**

```gradle
repositories {
    maven {
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    implementation "maven.modrinth:jade:15.10.5+neoforge"
}
```

**方式 B — Curse Maven:**

```gradle
repositories {
    maven {
        url = "https://www.cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    implementation "curse.maven:jade-324717:7545219"
}
```

### 版本历史（1.21.1 NeoForge）

| 版本 | 日期 | 类型 |
|------|------|------|
| ~~15.10.5+neoforge~~ | **2026-01-29** | **Release ← 当前最新** |
| 15.10.4+neoforge | 2026-01-07 | Release |
| 15.10.2+neoforge | 2025-07-06 | Release |
| 15.10.1+neoforge | 2025-06-18 | Release |
| 15.10.0+neoforge | 2025-03-11 | Release |

---

## 3. Curios API

| 项目 | 内容 |
|------|------|
| **Mod** | Curios API (Forge/NeoForge) |
| **作者** | TheIllusiveC4 |
| **许可证** | LGPL-3.0 |
| **目标 MC 版本** | 1.21.1 (NeoForge) |
| **最新版本** | `9.5.1+1.21.1` |
| **Jar 文件名** | `curios-neoforge-9.5.1+1.21.1.jar` |
| **Artifact** | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` |
| **API Artifact** | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api` |
| **Maven 仓库** | `https://maven.theillusivec4.top/` |
| **CurseForge 项目 ID** | 309927 |
| **Curse Maven** | `curse.maven:curios-309927:6529130` |
| **Gradle 配置** | 见下 |

### Gradle 依赖配置

**方式 A — 官方 Maven（推荐）:**

```gradle
repositories {
    maven {
        name = "Illusive Soulworks maven"
        url = "https://maven.theillusivec4.top/"
    }
}

dependencies {
    compileOnly "top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api"
    runtimeOnly "top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1"
}
```

**方式 B — Curse Maven:**

```gradle
repositories {
    maven {
        url = "https://www.cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    implementation "curse.maven:curios-309927:6529130"
}
```

### 版本来源

- **Maven 元数据**: `top.theillusivec4.curios:curios-neoforge:maven-metadata.xml` — 最新为 `9.5.1+1.21.1`
- **CurseForge**: File ID `6529130`（2025-05-14）
- **Modrinth**: `9.5.1+1.21.1` 可用
- 注意: 1.21.11+ 版本号已跳至 `14.0.0+1.21.11`，`15.0.0-beta.2+26.1.2`（适配最新 NeoForge）

---

## 4. NeoForge ModDevGradle (MDG) 插件

| 项目 | 内容 |
|------|------|
| **插件 ID** | `net.neoforged.moddev` |
| **最新版本** | `2.0.141`（2026-03-22） |
| **推荐版本** | `2.0.96`（MDK-1.21.1 使用）或更高 |
| **最低兼容版本** | `2.0.72`（MDG 2 首个稳定版，2025-01-02） |
| **插件仓库** | Gradle Plugin Portal（默认） |

### Gradle 配置

```gradle
plugins {
    id 'net.neoforged.moddev' version '2.0.96'
}
```

### 版本说明

- **MDG 2.x** 是当前主力版本，仅支持 NeoForge 20.4+（即 MC 1.20.4+）
- MC 1.21.1 / NeoForge 26.1.2 要求 **MDG 2.0.x**
- 官方 MDK 仓库 `NeoForgeMDKs/MDK-1.21.1-ModDevGradle` 使用 `2.0.96`
- 建议使用 `>= 2.0.96` 以确保稳定性

---

## 5. 汇总表

| 依赖 | Maven 坐标 | 版本 | 仓库 URL |
|------|-----------|------|----------|
| **JEI** (运行时) | `mezz.jei:jei-1.21.1-neoforge:19.27.0.340` | `19.27.0.340` | `https://maven.blamejared.com/` |
| **JEI** (API) | `mezz.jei:jei-1.21.1-neoforge-api:19.27.0.340` | `19.27.0.340` | `https://maven.blamejared.com/` |
| **Jade** | `maven.modrinth:jade:15.10.5+neoforge` | `15.10.5+neoforge` | `https://api.modrinth.com/maven` |
| **Curios** (运行时) | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` | `9.5.1+1.21.1` | `https://maven.theillusivec4.top/` |
| **Curios** (API) | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api` | `9.5.1+1.21.1` | `https://maven.theillusivec4.top/` |
| **MDG 插件** | `net.neoforged.moddev` | `2.0.96+` | Gradle Plugin Portal |

---

## 6. 注意事项

1. **JEI** 的 CurseForge 标 `19.27.0.336` 为 Release，`19.27.0.340` 为 Beta，但 Maven 元数据中 `<release>` 指向 `19.27.0.340`。建议直接使用 Maven 仓库版本。
2. **Jade** 建议通过 Modrinth Maven 引入（官方文档推荐方式），比 Curse Maven 更稳定。
3. **Curios** 的官方 Maven 仓库 `maven.theillusivec4.top` 可能不稳定，可备选 Curse Maven。
4. 所有三个依赖均不需要 FG 的 `deobf()` 包装，直接使用标准 `implementation`/`runtimeOnly`/`compileOnly` 即可。
5. `gradle.properties` 建议统一管理版本变量，例如：
   ```properties
   jei_version=19.27.0.340
   jade_version=15.10.5+neoforge
   curios_version=9.5.1+1.21.1
   ```
