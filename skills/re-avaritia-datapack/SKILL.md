---
name: re-avaritia-datapack
description: Write and verify Re:Avaritia datapacks and supported KubeJS or CraftTweaker scripts for singularities, the Neutron Compressor, and tiered crafting tables. Use when a pack author asks to add, replace, or remove these definitions or recipes, or diagnose why a pack change does not take effect.
---

# Customize Re:Avaritia

1. Establish the target Minecraft version, loader, and Re:Avaritia release. Use this repository on the corresponding branch. Read the [branch reference](references/datapack.md) for the exact data format, the scripting integrations available on that branch, and links to their consumers.
2. Use a datapack for definitions and recipes that need no script engine. Start with the reference example closest to the request. Write it in the user's pack namespace. Use the original namespace and path only for an intentional override. Give a replacement recipe a distinct ID if you are suppressing the generated singularity recipe.
3. Use `kubejs/server_scripts/` or `scripts/` for scripting only when the target build provides the corresponding integration. Follow the linked scripting reference for method names and argument order. Inspect the locally installed script engine version before using examples from a different branch.
4. In an isolated world, install the pack under `datapacks/`, run `/datapack list enabled`, then `/reload` after editing. Check the log for failed resource IDs and the server's resolved recipe by ID. For recipes, run one craft or compression and confirm the input, tier, time, and output. For a singularity, check that the definition is present and that its derived recipe matches the requested behavior. Check after a second reload if a script adds or removes a definition. Report which engine and game versions you actually exercised.
5. Read the [gameplay and configuration reference](references/gameplay.md) when the task concerns the sword, shield, compressor automation, or ring. Recipe data can change inputs, outputs, counts, timings, and recipe availability. Those references show where runtime behavior is implemented and where the server offers a separate configuration setting.

Return the files with exact placement paths and the verification result. Keep disabled recipes and removed definitions distinct when reporting the outcome.
