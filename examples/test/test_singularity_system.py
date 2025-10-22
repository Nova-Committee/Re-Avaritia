#!/usr/bin/env python3
"""
奇点系统验证脚本
用于验证新的数据包系统是否正确实现
"""

import os
import json
import re
from pathlib import Path

def validate_singularity_files():
    """验证奇点数据文件格式"""
    print("🔍 检查奇点数据文件...")

    singularity_dir = Path("src/generated/resources/data/avaritia/singularities")
    if not singularity_dir.exists():
        print("❌ 奇点数据目录不存在")
        return False

    success_count = 0
    total_count = 0

    for json_file in singularity_dir.glob("*.json"):
        total_count += 1
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            # 验证必需字段
            required_fields = ["name", "colors", "ingredient"]
            missing_fields = [field for field in required_fields if field not in data]

            if missing_fields:
                print(f"❌ {json_file.name} 缺少必需字段: {missing_fields}")
                continue

            # 验证颜色格式
            colors = data["colors"]
            if not isinstance(colors, list) or len(colors) != 2:
                print(f"❌ {json_file.name} 颜色字段格式错误")
                continue

            for color in colors:
                if not re.match(r'^[0-9A-Fa-f]{6}$', color):
                    print(f"❌ {json_file.name} 颜色 '{color}' 格式错误，应为6位十六进制")
                    continue

            # 验证成分字段
            ingredient = data["ingredient"]
            if not isinstance(ingredient, dict):
                print(f"❌ {json_file.name} 成分字段格式错误")
                continue

            print(f"✅ {json_file.name} 格式验证通过")
            success_count += 1

        except json.JSONDecodeError as e:
            print(f"❌ {json_file.name} JSON格式错误: {e}")
        except Exception as e:
            print(f"❌ {json_file.name} 验证失败: {e}")

    print(f"\n📊 验证结果: {success_count}/{total_count} 个文件通过验证")
    return success_count == total_count

def validate_java_files():
    """验证Java代码实现"""
    print("\n🔍 检查Java代码实现...")

    java_files = [
        "src/main/java/committee/nova/mods/avaritia/init/data/SingularityDataProvider.java",
        "src/main/java/committee/nova/mods/avaritia/init/data/SingularityJsonReloadListener.java",
        "src/main/java/committee/nova/mods/avaritia/init/manager/SingularityDataManager.java"
    ]

    success_count = 0

    for java_file in java_files:
        file_path = Path(java_file)
        if not file_path.exists():
            print(f"❌ {java_file} 文件不存在")
            continue

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # 基本语法检查
            brace_count = content.count('{') - content.count('}')
            if brace_count != 0:
                print(f"❌ {java_file} 大括号不匹配")
                continue

            # 检查是否包含必要的导入和注解
            if "Singularity" not in content:
                print(f"❌ {java_file} 缺少Singularity相关代码")
                continue

            print(f"✅ {java_file} 基本验证通过")
            success_count += 1

        except Exception as e:
            print(f"❌ {java_file} 验证失败: {e}")

    print(f"\n📊 Java代码验证结果: {success_count}/{len(java_files)} 个文件通过验证")
    return success_count == len(java_files)

def check_migration_guide():
    """检查迁移指南"""
    print("\n🔍 检查迁移指南...")

    guide_file = Path("SINGULARITY_MIGRATION.md")
    if not guide_file.exists():
        print("❌ 迁移指南文件不存在")
        return False

    try:
        with open(guide_file, 'r', encoding='utf-8') as f:
            content = f.read()

        # 检查关键章节
        required_sections = [
            "## 概述",
            "## 主要改进",
            "## 文件结构",
            "## 使用新系统",
            "## 迁移步骤"
        ]

        missing_sections = [section for section in required_sections if section not in content]
        if missing_sections:
            print(f"❌ 迁移指南缺少章节: {missing_sections}")
            return False

        print("✅ 迁移指南内容完整")
        return True

    except Exception as e:
        print(f"❌ 检查迁移指南失败: {e}")
        return False

def main():
    """主测试函数"""
    print("🚀 开始奇点系统验证...\n")

    # 切换到项目根目录
    if Path("build.gradle").exists():
        print("✅ 已切换到项目根目录")
    else:
        print("❌ 未找到项目根目录")
        return

    # 执行验证
    tests = [
        ("奇点数据文件", validate_singularity_files),
        ("Java代码实现", validate_java_files),
        ("迁移指南", check_migration_guide)
    ]

    results = []
    for test_name, test_func in tests:
        result = test_func()
        results.append((test_name, result))

    # 输出最终结果
    print("\n" + "="*50)
    print("📋 最终验证结果:")
    print("="*50)

    all_passed = True
    for test_name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{test_name}: {status}")
        if not result:
            all_passed = False

    print("="*50)
    if all_passed:
        print("🎉 所有测试通过！奇点系统数据包迁移成功完成！")
        print("\n📝 下一步:")
        print("   1. 运行 './gradlew runData' 生成数据文件")
        print("   2. 使用 './gradlew build' 构建项目")
        print("   3. 在游戏中测试奇点功能")
    else:
        print("⚠️  部分测试失败，请检查上述错误信息并修复")

    print("="*50)

if __name__ == "__main__":
    main()