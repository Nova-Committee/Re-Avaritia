/**
 * 完整的KubeJS奇点创建示例
 *
 * 这个文件展示了如何使用新的奇点系统创建各种类型的奇点，
 * 包括基础奇点、条件奇点、动态奇点和批量奇点创建。
 *
 * 兼容性：完全兼容EternalSingularityCraftRecipe
 * 特性：自动触发配方缓存失效，支持实时配方更新
 */

console.log('开始加载KubeJS奇点示例...')

// ============ 基础奇点示例 ============

ServerEvents.recipes(event => {
    console.log('创建基础奇点...')

    // 简单的铁奇点
    event.custom({
        type: 'avaritia:singularity',
        id: 'iron',
        name: 'singularity.basic.iron',
        colors: ['E1E1E1', '6C6C6C'],  // 亮灰色
        ingredient: 'minecraft:iron_ingot',
        materialCount: 1000,
        timeRequired: 200,
        enabled: true,
        recipeDisabled: false
    })

    // 使用标签的铜奇点
    event.custom({
        type: 'avaritia:singularity',
        id: 'copper',
        name: 'singularity.basic.copper',
        colors: ['FA977C', 'BC5430'],  // 铜色
        tag: 'forge:ingots/copper',  // 所有铜锭
        materialCount: 800,  // 铜比较常见，减少需求
        timeRequired: 150,
        enabled: true,
        recipeDisabled: false
    })

    console.log('基础奇点创建完成')
})

// ============ 条件奇点示例 ============

ServerEvents.recipes(event => {
    console.log('创建条件奇点...')

    // 仅在ProjectE加载时创建的奇点
    event.custom({
        type: 'avaritia:singularity',
        id: 'dark_matter',
        name: 'singularity.conditonal.projecte',
        colors: ['8A2BE2', '4A0E4E'],  // 项目E色彩
        ingredient: 'projecte:dark_matter',
        materialCount: 10000,  // 高级材料需要更多
        timeRequired: 600,
        enabled: true,
        recipeDisabled: false,
        conditions: [
            {
                type: 'forge:mod_loaded',
                modid: 'projecte'
            }
        ]
    })

    // 多条件组合 - 科技模组且特定标签存在
    event.custom({
        type: 'avaritia:singularity',
        id: 'thermal_composite',
        name: 'singularity.conditional.thermal_composite',
        colors: ['FFA500', 'E65100'],  // 橙色系
        tag: 'forge:gears/signalum',  // Signalum齿轮
        materialCount: 500,
        timeRequired: 350,
        enabled: true,
        recipeDisabled: false,
        conditions: [
            {
                type: 'forge:and',
                value: [
                    {
                        type: 'forge:mod_loaded',
                        modid: 'thermal'
                    },
                    {
                        type: 'forge:not',
                        value: {
                            type: 'forge:tag_empty',
                            tag: 'forge:gears/signalum'
                        }
                    }
                ]
            }
        ]
    })

    console.log('条件奇点创建完成')
})

// ============ 动态批量奇点创建 ============

ServerEvents.recipes(event => {
    console.log('创建动态批量奇点...')

    // 批量创建所有原版金属锭奇点
    const vanillaMetals = [
        { item: 'minecraft:iron_ingot', name: 'iron', colors: ['E1E1E1', '6C6C6C'], multiplier: 1.0 },
        { item: 'minecraft:gold_ingot', name: 'gold', colors: ['FFD700', 'D98E04'], multiplier: 1.2 },
        { item: 'minecraft:copper_ingot', name: 'copper', colors: ['FA977C', 'BC5430'], multiplier: 0.8 },
        { item: 'minecraft:diamond', name: 'diamond', colors: ['A6FCE9', '1AACA8'], multiplier: 2.0 },
        { item: 'minecraft:emerald', name: 'emerald', colors: ['7DF8AC', '008E1A'], multiplier: 1.5 },
        { item: 'minecraft:netherite_ingot', name: 'netherite', colors: ['443A3B', '1A1616'], multiplier: 3.0 }
    ]

    vanillaMetals.forEach((metal, index) => {
        const baseCount = 1000
        const baseTime = 200

        event.custom({
            type: 'avaritia:singularity',
            id: `${metal.name}`,
            name: `singularity.vanilla.${metal.name}`,
            colors: metal.colors,
            ingredient: metal.item,
            materialCount: Math.floor(baseCount * metal.multiplier),
            timeRequired: Math.floor(baseTime * metal.multiplier),
            enabled: true,
            recipeDisabled: false
        })
    })

    console.log(`批量创建了 ${vanillaMetals.length} 个原版金属奇点`)
})

// ============ 基于配置的奇点 ============

ServerEvents.recipes(event => {
    console.log('创建基于配置的奇点...')

    // 模拟配置读取
    const difficulty = global.config?.avaritia?.difficulty || 'normal'
    const enableExperimental = global.config?.avaritia?.experimentalSingularities || false

    // 根据难度调整基础奇点
    let difficultyMultiplier, difficultyName
    switch(difficulty) {
        case 'easy':
            difficultyMultiplier = 0.5
            difficultyName = 'Simple'
            break
        case 'normal':
            difficultyMultiplier = 1.0
            difficultyName = 'Normal'
            break
        case 'hard':
            difficultyMultiplier = 2.0
            difficultyName = 'Hard'
            break
        case 'hardcore':
            difficultyMultiplier = 5.0
            difficultyName = 'Hardcore'
            break
        default:
            difficultyMultiplier = 1.0
            difficultyName = 'Normal'
    }

    // 难度调整的铁奇点
    event.custom({
        type: 'avaritia:singularity',
        id: 'adaptive_iron',
        name: 'singularity.difficulty.adaptive_iron',
        colors: ['C0C0C0', '808080'],  // 绿色系，表示适应性
        ingredient: 'minecraft:iron_ingot',
        materialCount: Math.floor(1000 * difficultyMultiplier),
        timeRequired: Math.floor(200 * difficultyMultiplier),
        enabled: true,
        recipeDisabled: false
    })

    console.log(`创建了难度调整奇点 (${difficultyName}: ${difficultyMultiplier}x)`)
})

// ============ 实验性奇点 ============

ServerEvents.recipes(event => {
    console.log('创建实验性奇点...')

    // 只有在启用实验性功能时才创建
    if (global.config?.avaritia?.experimentalSingularities) {
        // 红石能量奇点
        event.custom({
            type: 'avaritia:singularity',
            id: 'redstone_energy',
            name: 'singularity.experimental.redstone_energy',
            colors: ['FF0000', '8B0000'],  // 深红色
            ingredient: 'minecraft:redstone_block',
            materialCount: 2000,
            timeRequired: 400,
            enabled: true,
            recipeDisabled: false
        })

        // 粘液奇点
        event.custom({
            type: 'avaritia:singularity',
            id: 'slime_flux',
            name: 'singularity.experimental.slime_flux',
            colors: ['00FF00', '006400'],  // 亮绿色
            ingredient: 'minecraft:slime_ball',
            materialCount: 1500,
            timeRequired: 300,
            enabled: true,
            recipeDisabled: false
        })

        // 观察者奇点 - 神秘物品
        event.custom({
            type: 'avaritia:singularity',
            id: 'essence_of_observation',
            name: 'singularity.experimental.essence_of_observation',
            colors: ['7A7A7A', '2F4F4F'],  // 深紫色
            ingredient: 'minecraft:shulker_shell',
            materialCount: 500,  // 神秘物品数量需求较少
            timeRequired: 800,  // 但需要更长时间
            enabled: true,
            recipeDisabled: false
        })

        console.log('实验性奇点创建完成')
    } else {
        console.log('实验性奇点已禁用')
    }
})

// ============ 奇点管理示例 ============

// 这个事件监听器会在奇点系统重载时触发
ServerEvents.loaded(event => {
    console.log('奇点系统已重载，可以执行额外的逻辑...')

    // 这里可以添加额外的奇点管理逻辑
    // 例如：自动分类、统计分析、特殊效果等

    // 获取当前所有奇点统计
    const manager = Java.type('committee.nova.mods.avaritia.core.singularity.SingularityDataManager').getInstance()
    if (manager && manager.isInitialized()) {
        const singularities = manager.getSingularities()
        console.log(`当前已注册奇点数量: ${singularities.size()}`)

        // 按类型分类统计（简化版本）
        let vanillaCount = 0, moddedCount = 0, customCount = 0
        singularities.forEach(singularity => {
            const id = singularity.getId()
            if (id.getPath().startsWith('vanilla.')) {
                vanillaCount++
            } else if (id.getPath().startsWith('basic.') || id.getPath().startsWith('conditional.') || id.getPath().startsWith('experimental.')) {
                customCount++
            } else {
                moddedCount++
            }
        })

        console.log(`奇点分类统计: 原版(${vanillaCount}) | 模组(${moddedCount}) | 自定义(${customCount})`)
    }
})

// ============ 错误处理和验证 ============

// 验证函数 - 确保奇点数据有效
function validateSingularity(singularityData) {
    try {
        // 检查必需字段
        if (!singularityData.name || !singularityData.colors || (!singularityData.ingredient && !singularityData.tag)) {
            throw new Error('奇点缺少必需字段: name, colors, ingredient/tag')
        }

        // 检查颜色格式
        if (!Array.isArray(singularityData.colors) || singularityData.colors.length < 2) {
            throw new Error('奇点颜色数组格式错误，需要至少2个颜色')
        }

        singularityData.colors.forEach((color, index) => {
            if (!/^[0-9A-Fa-f]{6}$/.test(color)) {
                throw new Error(`颜色${index}格式错误: ${color}，应为6位十六进制`)
            }
        })

        // 检查材料定义
        if (singularityData.ingredient && singularityData.tag) {
            throw new Error('奇点不能同时指定ingredient和tag')
        }

        return true
    } catch (error) {
        console.error(`奇点验证失败: ${error.message}`)
        return false
    }
}

// 使用验证的奇点创建
ServerEvents.recipes(event => {
    console.log('创建验证过的奇点...')

    const validatedSingularity = {
        type: 'avaritia:singularity',
        id: 'safe_example',
        name: 'singularity.validated.safe_example',
        colors: ['FFFFFF', '000000'],  // 黑白对比
        ingredient: 'minecraft:diamond',  // 钻石 - 安全选择
        materialCount: 1000,
        timeRequired: 200,
        enabled: true,
        recipeDisabled: false
    }

    // 验证后创建
    if (validateSingularity(validatedSingularity)) {
        event.custom(validatedSingularity)
        console.log('验证过的奇点创建成功')
    }
})

console.log('KubeJS奇点示例加载完成！')
console.log('检查日志以确认所有奇点是否正确创建和加载。')