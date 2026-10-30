# 第二、三期交付说明

日期：2026年10月30日

本次按匠魂 1.20.1 Forge 本地源码的 `LOADER` 逐字段修正第二期和第三期 Schema。第三期实际是 28 个配方类型；原计划和旧交付说明写成 30 个属于计数错误，附录 `tconstruct-recipe-reference.md` 的清单为准。

## 修改文件

- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/component/LoadableComponent.java`
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/component/TinkerComponents.java`
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/schema/TinkerRecipeSchemas.java`
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/ArsenalKubeJSPlugin.java`
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/recipe/TinkerRecipeJS.java`
- `docs/phase2-3-plan.md`

## 注册内容

第二期 6 个：`material`、`part_builder`、`item_part_builder`、`tool_building`、`part_builder_recycling`、`part_builder_tool_recycling`。

第三期 28 个：`modifier`、`incremental_modifier`、`swappable_modifier`、`multilevel_modifier`、`multilevel_incremental_modifier`、`overslime_modifier`、`crafting_overslime_modifier`、`modifier_salvage`、`modifier_repair`、`crafting_modifier_repair`、`remove_modifier`、`extract_modifier`、`modifier_sorting`、`modifier_set_worktable`、`enchantment_converting`、`toggle_interaction`、`tinker_station_damaging`、`tinker_station_part_swapping`、`tool_material_swapping`、`fixed_material_swapping`、`part_swapping_override`、`tipped_tool_transform`、`severing`、`ageable_severing`、`player_beheading`、`snow_golem_beheading`、`mooshroom_demushrooming`、`sheep_shearing`。

## 新增组件

- `SIZED_INGREDIENT`：包装 Mantle `SizedIngredient.LOADABLE`，作为输入；支持 `InputItem`、数量读取、按物品匹配和替换，并保留原 `amount_needed`。
- `ITEM_STACK_OUTPUT`：包装 Mantle `ItemStackLoadable.REQUIRED_STACK_NBT`，作为物品输出；支持按物品匹配和替换，并保留原数量。
- `MATERIAL_ITEM_RESULT`：对应 `PartRecipe` 的 `MergingField`，写出 `{item,count}`，支持产物匹配和替换。
- `SWAPPABLE_RESULT`：对应 `SwappableModifierRecipe` 的 `result.name` 与 `result.value` 合并字段。
- `LEVEL_RANGE`、`SLOT_COUNT`、`LEVEL_ENTRY`、`MODIFIER_PREDICATE`、`TOOL_PART`、`INDEX_ARRAY`、`PERCENT`、`VARIANT_FORMATTER`、`RESOURCE_LOCATION`：分别包装匠魂和 Mantle 的对应 Loadable。

现有物品 ID 产物组件也补上了替换映射，替换时保留原产物数量。`part_builder_recycling.results` 使用 KubeJS 的 MapRecipeComponent，键是 Pattern ID，值是 ItemOutput。

## 运行时校验

`TinkerRecipeJS.afterLoaded` 新增了材料部件成本、词条输入和等级列表、黏液保护输入、修复配方字段、排序配方单输入、材料替换索引范围等校验；错误信息使用英文并在加载阶段抛出 `RecipeExceptionJS`。

## 与旧文档的差异

- `tool_building` 按源码使用 `slot_layout`、`parts_override`、`extra_materials`，没有虚构的 `parts` 结果字段。
- `modifier_repair` 和 `crafting_modifier_repair` 按 `IModifierRepairRecipe` 使用 `modifier`、`ingredient`、`repair_amount`。
- 四种固定斩首配方只有 `per_level_chance` 和 `looting_bonus`；生物和掉落物由类代码固定。
- `part_builder` 的 `result` 是合并对象，`part_builder_recycling.results` 是 Pattern ID 到 ItemOutput 的映射。

## 构建验证

已执行：

```powershell
$env:JAVA_HOME='C:\Users\30819\.jdks\ms-17.0.18'
$env:GRADLE_OPTS='-Dnet.minecraftforge.gradle.check.certs=false -Dhttps.protocols=TLSv1.2'
.\gradlew.bat compileJava --no-daemon
```

结果：`BUILD SUCCESSFUL`。

尚未完成真实游戏内的配方事件替换、删除、标签流体和金属批量生成测试；这些必须在服务端启动环境中继续验证，不能用编译结果替代。

## 服务端验证

由服务端测试代理启动 `gradlew runServer` 后，`run/logs/latest.log` 记录了 `phase3_schema_modifier=function`、`phase3_schema_severing=function`、`queued tinkers_kubejs_arsenal:test_melting`，并完成 `Server resource reload complete`。本次启动没有再次出现 Schema 必填键顺序异常。

日志仍有 2 条匠魂原有 `swappable_modifier` 配方解析警告，涉及 `slimewood_greenheart` 和 `slimewood_enderbark`；它们发生在原版配方重载阶段，不影响测试配方加载，后续需要在完整整合包依赖齐全时单独复核。

复测前根据 `ModifierSalvage.java:30` 的 `defaultField("level", ...)` 又将 `modifier_salvage.level` 改为可选默认范围；此前服务端日志中的 level 缺失警告属于该字段必填性误判，已纳入修正。


最终复测（20:56）中 `modifier_salvage` 的 level 缺失警告已消失；`TKJA_TEST` 仍全部成功，剩余 4 条仅为匠魂既有 `swappable_modifier` 解析警告，服务端随后已关闭。
