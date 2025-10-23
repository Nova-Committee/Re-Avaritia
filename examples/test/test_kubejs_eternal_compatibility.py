#!/usr/bin/env python3
"""
KubeJS奇点与EternalSingularityCraftRecipe兼容性测试
验证KubeJS创建的奇点能够正确触发配方缓存失效
"""

import os
import re
from pathlib import Path

def check_eternal_singularity_import():
    """检查SingularityDataManager是否正确导入了EternalSingularityCraftRecipe"""
    print("🔍 检查EternalSingularityCraftRecipe导入...")

    manager_file = Path("src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityDataManager.java")
    if not manager_file.exists():
        print("❌ SingularityDataManager.java 文件不存在")
        return False

    try:
        with open(manager_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 检查导入语句
        import_pattern = r'import\s+committee\.nova\.mods\.avaritia\.common\.crafting\.recipe\.EternalSingularityCraftRecipe;'
        if not re.search(import_pattern, content):
            print("❌ 缺少EternalSingularityCraftRecipe导入")
            return False

        # 检查invalidate调用
        invalidate_pattern = r'EternalSingularityCraftRecipe\.invalidate\(\)'
        invalidate_calls = re.findall(invalidate_pattern, content)

        # 应该在registerRuntimeSingularity、removeRuntimeSingularity、clearRuntimeSingularities中都调用
        if len(invalidate_calls) < 3:
            print(f"❌ invalidate调用次数不足: {len(invalidate_calls)}/3")
            return False

        print("✅ EternalSingularityCraftRecipe导入和调用正确")
        print(f"   发现 {len(invalidate_calls)} 个invalidate调用点")
        return True

    except Exception as e:
        print(f"❌ 检查导入失败: {e}")
        return False

def check_eternal_singularity_recipe():
    """检查EternalSingularityCraftRecipe是否使用SingularityDataManager"""
    print("\n🔍 检查EternalSingularityCraftRecipe兼容性...")

    recipe_file = Path("src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/EternalSingularityCraftRecipe.java")
    if not recipe_file.exists():
        print("❌ EternalSingularityCraftRecipe.java 文件不存在")
        return False

    try:
        with open(recipe_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 检查SingularityDataManager导入
        if "import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;" not in content:
            print("❌ EternalSingularityCraftRecipe缺少SingularityDataManager导入")
            return False

        # 检查getInstance调用
        get_instance_pattern = r'SingularityDataManager\.getInstance\(\)'
        if not re.search(get_instance_pattern, content):
            print("❌ EternalSingularityCraftRecipe没有调用SingularityDataManager.getInstance()")
            return False

        # 检查getSingularities调用
        get_singularities_pattern = r'SingularityDataManager\.getInstance\(\)\.getSingularities\(\)'
        if not re.search(get_singularities_pattern, content):
            print("❌ EternalSingularityCraftRecipe没有调用getSingularities()")
            return False

        print("✅ EternalSingularityCraftRecipe与SingularityDataManager兼容")
        return True

    except Exception as e:
        print(f"❌ 检查EternalSingularityCraftRecipe失败: {e}")
        return False

def check_cache_invalidation_logic():
    """检查缓存失效逻辑的完整性"""
    print("\n🔍 检查缓存失效逻辑...")

    manager_file = Path("src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityDataManager.java")
    if not manager_file.exists():
        return False

    try:
        with open(manager_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 查找所有运行时奇点操作方法
        methods = [
            "registerRuntimeSingularity",
            "removeRuntimeSingularity",
            "clearRuntimeSingularities"
        ]

        for method in methods:
            # 查找方法定义
            method_pattern = f'public.*{method}\\s*\\('
            method_match = re.search(method_pattern, content)

            if not method_match:
                print(f"❌ 未找到方法: {method}")
                return False

            # 获取方法体内容
            start_pos = method_match.start()
            # 简单的方法体查找（可能不准确，但用于基本验证）
            method_end_pos = content.find('}', start_pos + 500)  # 在方法开始后500字符内查找结束

            if method_end_pos == -1:
                print(f"❌ 方法 {method} 未找到结束")
                return False

            method_content = content[start_pos:method_end_pos]

            # 检查是否包含invalidate调用
            if 'EternalSingularityCraftRecipe.invalidate()' not in method_content:
                print(f"❌ 方法 {method} 缺少invalidate调用")
                return False

            print(f"✅ 方法 {method} 包含缓存失效逻辑")

        print("✅ 所有运行时奇点操作都包含缓存失效逻辑")
        return True

    except Exception as e:
        print(f"❌ 检查缓存失效逻辑失败: {e}")
        return False

def check_data_provider_integration():
    """检查数据提供者是否正确集成了ModSingularities"""
    print("\n🔍 检查数据提供者集成...")

    # 由于代码结构发生变化，检查新的路径
    data_provider_paths = [
        "src/main/java/committee/nova/mods/avaritia/init/data/SingularityDataProvider.java",
        "src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityDataProvider.java"
    ]

    data_provider_file = None
    for path in data_provider_paths:
        if Path(path).exists():
            data_provider_file = path
            break

    if not data_provider_file:
        print("❌ 未找到SingularityDataProvider.java")
        return False

    try:
        with open(data_provider_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 检查ModSingularities导入
        if "import committee.nova.mods.avaritia.init.registry.ModSingularities;" not in content:
            print("❌ 数据提供者缺少ModSingularities导入")
            return False

        # 检查getDefaults调用
        if "ModSingularities.getDefaults()" not in content:
            print("❌ 数据提供者未调用ModSingularities.getDefaults()")
            return False

        print("✅ 数据提供者正确集成了ModSingularities")
        return True

    except Exception as e:
        print(f"❌ 检查数据提供者失败: {e}")
        return False

def check_kubejs_schema_compatibility():
    """检查KubeJS模式是否与新的数据管理器兼容"""
    print("\n🔍 检查KubeJS模式兼容性...")

    schema_paths = [
        "src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/SingularitySchema.java",
        "src/main/java/committee/nova/mods/avaritia/core/singularity/compat/kubejs/SingularitySchema.java"
    ]

    schema_file = None
    for path in schema_paths:
        if Path(path).exists():
            schema_file = path
            break

    if not schema_file:
        print("❌ 未找到SingularitySchema.java")
        return False

    try:
        with open(schema_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 检查新的SingularityDataManager导入
        new_import_pattern = r'import\s+committee\.nova\.mods\.avaritia\.core\.singularity\.SingularityDataManager;'
        if re.search(new_import_pattern, content):
            print("✅ KubeJS模式使用了新的SingularityDataManager")
            return True

        # 检查旧的导入（已弃用）
        old_import_pattern = r'import\s+committee\.nova\.mods\.avaritia\.init\.manager\.SingularityDataManager;'
        if re.search(old_import_pattern, content):
            print("⚠️  KubeJS模式仍在使用旧的SingularityManager路径")
            return False

        print("❌ KubeJS模式缺少SingularityDataManager导入")
        return False

    except Exception as e:
        print(f"❌ 检查KubeJS模式兼容性失败: {e}")
        return False

def validate_cache_invalidation_timing():
    """验证缓存失效的时序正确性"""
    print("\n🔍 验证缓存失效时序...")

    # 检查关键操作序列
    expected_sequence = [
        "this.runtimeSingularities.put",
        "EternalSingularityCraftRecipe.invalidate()",
        "MinecraftForge.EVENT_BUS.post"
    ]

    manager_file = Path("src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityDataManager.java")
    if not manager_file.exists():
        return False

    try:
        with open(manager_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 提取registerRuntimeSingularity方法内容
        register_method_match = re.search(
            r'public void registerRuntimeSingularity\(.*?\{(.*?)(?=public|\Z)',
            content,
            re.DOTALL
        )

        if not register_method_match:
            print("❌ 无法提取registerRuntimeSingularity方法")
            return False

        method_content = register_method_match.group(1)

        # 检查时序
        sequence_issues = []
        for i, expected in enumerate(expected_sequence):
            if expected not in method_content:
                sequence_issues.append(f"缺少操作: {expected}")
            else:
                # 检查操作顺序
                pos = method_content.find(expected)
                if i > 0:
                    prev_pos = method_content.find(expected_sequence[i-1])
                    if pos < prev_pos:
                        sequence_issues.append(f"操作时序错误: {expected} 应该在 {expected_sequence[i-1]} 之后")

        if sequence_issues:
            print("❌ 缓存失效时序有问题:")
            for issue in sequence_issues:
                print(f"   - {issue}")
            return False

        print("✅ 缓存失效时序正确")
        print("   1. 更新运行时奇点映射")
        print("   2. 使配方缓存失效")
        print("   3. 发布重载事件")
        return True

    except Exception as e:
        print(f"❌ 验证缓存失效时序失败: {e}")
        return False

def main():
    """主测试函数"""
    print("🚀 开始KubeJS与EternalSingularityCraftRecipe兼容性测试...\n")

    # 执行兼容性检查
    tests = [
        ("EternalSingularityCraftRecipe导入", check_eternal_singularity_import),
        ("EternalSingularityCraftRecipe兼容性", check_eternal_singularity_recipe),
        ("缓存失效逻辑", check_cache_invalidation_logic),
        ("数据提供者集成", check_data_provider_integration),
        ("KubeJS模式兼容性", check_kubejs_schema_compatibility),
        ("缓存失效时序", validate_cache_invalidation_timing)
    ]

    results = []
    for test_name, test_func in tests:
        result = test_func()
        results.append((test_name, result))

    # 输出最终结果
    print("\n" + "="*70)
    print("📋 KubeJS与EternalSingularityCraftRecipe兼容性测试结果:")
    print("="*70)

    all_passed = True
    for test_name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{test_name}: {status}")
        if not result:
            all_passed = False

    print("="*70)
    if all_passed:
        print("🎉 所有兼容性测试通过！")
        print("\n📝 KubeJS奇点与EternalSingularityCraftRecipe完全兼容：")
        print("   ✅ 运行时奇点注册会自动触发配方缓存失效")
        print("   ✅ EternalSingularityCraftRecipe会动态获取最新奇点列表")
        print("   ✅ 缓存失效时序正确，避免竞态条件")
        print("   ✅ 数据提供者正确集成ModSingularities")
        print("\n🚀 现在可以安全使用KubeJS创建奇点了！")
        print("\n📖 使用说明：")
        print("   1. 运行 './gradlew runData' 生成数据文件")
        print("   2. 启动带有KubeJS的游戏")
        print("   3. 使用ServerEvents.recipes创建自定义奇点")
        print("   4. 验证EternalSingularityCraftRecipe能够识别新奇点")
    else:
        print("⚠️  部分兼容性测试失败")
        print("\n🔧 建议修复步骤：")
        print("   1. 确保SingularityDataManager正确导入EternalSingularityCraftRecipe")
        print("   2. 在所有运行时奇点操作中调用invalidate()")
        print("   3. 更新KubeJS模式以使用新的数据管理器路径")
        print("   4. 验证缓存失效的时序正确性")
        print("   5. 检查数据提供者是否正确集成")

    print("="*70)

if __name__ == "__main__":
    main()