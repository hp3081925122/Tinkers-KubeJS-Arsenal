# 第二、三期实施审核（2026年10月30日）

审核对象：[delivery-phase2-3.md](delivery-phase2-3.md) 所述改动（`TinkerComponents`、`LoadableComponent`、`TinkerRecipeSchemas`、`ArsenalKubeJSPlugin`、`TinkerRecipeJS`）。

审核方式：
- 用 `D:/AI临时文件/verify_schema2.py` 把 34 个新 Schema 的键与匠魂 `LOADER` 逐字段比对；
- 对每一处差异回到源码复核；
- 逐个阅读新组件，按 R1–R6 检查。

未做：构建以外的游戏内验证（交付方已跑过一次服务端加载）。

## 通过项

- 注册了第二期 6 个、第三期 28 个，共 34 个。第三期是 28 个，原计划写 30 是我的计数错误，交付方指出得对，已修正计划文档。
- 34 个 Schema 中，26 个和 `LOADER` 完全一致。另有 3 处差异是解析器误报或合并字段，属于合理差异：
  - `part_builder` 的 `item`/`count` 合并成 `result`；
  - `swappable_modifier` 的 `name`/`value` 合并成 `result`；
  - `modifier_salvage` 的 `level` 实际是 `defaultField`。
- 新组件 `SIZED_INGREDIENT`、`ITEM_STACK_OUTPUT`、`MATERIAL_ITEM_RESULT` 的替换都先判断是否匹配，并保留原数量或用量（R1、R2 通过）。
- 没有 `TypeDescJS.ANY`（R6 通过）；词条谓词按要求写成 `STRING.or(ANY_MAP)`。
- 默认值核对一致：`max_tool_size`、`max_stack_size` 为 16，`allow_crystal` 为 true，worktable 的 `tools` 默认 `#tconstruct:modifiable`，黏液保护的 `tools` 默认 `#tconstruct:durability`。
- `index` 校验与 `IntArrayLoadable(FROM_ZERO, -1, 10)` 一致；`multilevel_modifier` 的“无输入且不允许水晶”校验与源码 validate 一致。

## 必须修复

| 编号 | 位置 | 问题 | 源码依据 |
|---|---|---|---|
| P0-1 | `LoadableComponent.replaceOutput` | `MATERIAL_ITEM`、`MODIFIABLE_ITEM` 的 `outputValue` 在替换物类型不符时返回 `null`，导致 `replaceOutput` 返回 `null`。<br>后果：`event.replaceOutput({}, 'tconstruct:pick_head', 'minecraft:stick')` 会把配方的 `result` 清空，写出时出错。<br>应改为：转换结果为 null 时抛出英文异常，或返回原值。 | — |
| P0-2 | `multilevel_incremental_modifier` | 多出 `level`、`slots` 两个键，源码没有。等级和槽位来自 `levels` 列表。 | `MultilevelIncrementalModifierRecipe.java:24-38` |
| P0-3 | `remove_modifier`、`extract_modifier` | `inputs` 被设成可选，默认空数组。源码是必填，且至少 1 个。 | `AbstractWorktableRecipe.java:25-27`，`list(1).requiredField` |
| P0-4 | `modifier_set_worktable.add_to_set`<br>`enchantment_converting.match_book`<br>`enchantment_converting.return_unenchanted` | 这三个字段用了 `bool()`，没写时会静默变成 `false`，违反 R5。源码都是 `requiredField`。 | `ModifierSetWorktableRecipe.java:49`，`EnchantmentConvertingRecipe.java:52-53` |
| P0-5 | `tipped_tool_transform` | 缺 `group`。 | `TippedToolTransformRecipe.java:31`，`RECIPE_GROUP` |
| P0-6 | `SIZED_INGREDIENT.read` | 计划要求支持 `'4x minecraft:leather'` 这种写法。现在字符串走 JSON 路径，会变成 `{"item":"4x minecraft:leather"}`，读取直接失败。只有传入 `Item.of(...)` 这类已包装的对象时，数量才生效。 | 计划第 5 节 |

## 应当修复

- **P1-1：** `tool_building.extra_requirements` 用了 `INGREDIENT_OPTIONAL`，源码是 `IngredientLoadable.DISALLOW_EMPTY`（`ToolBuildingRecipe.java:61`），应改为 `INGREDIENT_REQUIRED`。
- **P1-2：** `MATERIAL_ITEM_RESULT.read` 只接受对象，传字符串 `'tconstruct:pick_head'` 会报出不清楚的 Gson 异常。应把字符串当作 `{item}` 处理，类型描述也加上 `STRING`。
- **P1-3：** `SWAPPABLE_RESULT.read` 缺 `value` 时会抛 NPE，应改为明确的英文报错。
- **P1-4：** 交付说明和代码不一致。说明里写了“黏液保护输入、修复配方字段”的校验，代码里没有。这两项其实由 Schema 的必填字段保证，不需要额外校验，但说明应该如实写。

## 修复后复审方式

1. 再跑 `verify_schema2.py`。除通过项里列出的 3 处合理差异外，应全部 OK。
2. 运行 `gradlew build`。
3. 游戏内测试项（由用户决定是否进行）：
   - `event.replaceOutput({}, 'tconstruct:pick_head', 'minecraft:stick')` 不会损坏部件制作台配方；
   - `event.recipes.tconstruct.modifier(['4x minecraft:diamond'], '#tconstruct:modifiable', 'tconstruct:diamond')` 的数量能被正确读取；
   - `event.remove({output: 'tconstruct:pick_head'})` 能删掉部件制作台配方。

## 结论

和上一轮的“原始 JSON 透传”相比，这一版质量明显提高：结构正确，组件的匹配和替换基本符合规则。但还有 6 处 P0 要修，暂不能合入。这些都是小改动，可以直接修。

## 修复记录（2026年10月30日，主对话直接修复）

| 编号 | 状态 | 修复方式 |
|---|---|---|
| P0-1 | 已修复 | `LoadableComponent.replaceOutput` 转换结果为 null 时抛出 `RecipeExceptionJS`，提示替换物类型不符 |
| P0-2 | 已修复 | 删除 `multilevel_incremental_modifier` 的 `level` 和 `slots` |
| P0-3 | 已修复 | `remove_modifier`、`extract_modifier` 的 `inputs` 改为必填，并移到可选键之前 |
| P0-4 | 已修复 | 这三个字段改为必填的 `BOOLEAN` |
| P0-5 | 已修复 | `tipped_tool_transform` 补上 `group` |
| P0-6 | 已修复 | 字符串输入改用 KubeJS 的 `InputItem.of` 解析，`'4x id'`、`'#tag'` 都会转成 `SizedIngredient` |
| P1-1 | 已修复 | `extra_requirements` 改为 `INGREDIENT_REQUIRED` |
| P1-2 | 已修复 | 字符串会被当作 `{item}`；类型描述加上 `STRING` |
| P1-3 | 已修复 | 缺 `name` 或 `value` 时抛出英文异常 |
| P1-4 | 已记录 | 只是交付说明写错，代码不需要改 |
| 新发现 P0-7 | 已修复 | 手写的两个默认标签路径错了：`tconstruct:multipart_tool` 应为 `tconstruct:modifiable/multipart`，`tconstruct:durability` 应为 `tconstruct:modifiable/durability`。不写 `tools` 时，`part_builder_tool_recycling` 和 `overslime_modifier` 的默认工具标签是空的。现在 4 个默认值都改用 `TinkerTags.Items` 常量，和源码一致 |

复审结果：
- `verify_schema2.py` 只剩通过项列出的 3 处合理差异；
- `gradlew build` 通过；
- 游戏内测试还没做，上面列出的测试项仍然有效。
