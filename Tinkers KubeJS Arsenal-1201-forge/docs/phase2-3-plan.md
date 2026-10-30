# 第二、三期实施文档：材料部件、工匠台与词条配方

> 编写日期：2026年10月30日
> 前置阅读（按顺序）：
> 1. [recipe-compat-plan.md](recipe-compat-plan.md)：总体规则，第 2 节的硬性规则仍然全部有效
> 2. [review-2026-10-30.md](review-2026-10-30.md)：第一轮审核出的问题，**本期不得重犯**
> 3. [tconstruct-recipe-reference.md](tconstruct-recipe-reference.md)：字段附录，已经过两轮复核
> 4. 当前代码：`src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/**`
>
> 仓库：https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal （`main` 分支）

## 1. 现状

已注册 31 个配方 ID，见 `ArsenalKubeJSPlugin`：

- 冶炼：`melting`、`ore_melting`、`damagable_melting`、`material_melting`、`melting_fuel`、`entity_melting`、`alloy`；
- 浇铸：`casting_*`、`retextured_casting_*`、`*_duplication`、`*_filling`、`casting_*_potion`、`casting_*_tipping`、`casting_*_tipped_clearing`、`molding_*`；
- 材料浇铸：`*_casting_material`、`*_casting_composite`、`*_tool_casting`、`*_casting_part_swapping`、`material_fluid`。

本期要补齐剩下的配方（第 3 节）。

## 2. 硬性规则：第一轮的教训

下面每一条都对应第一轮真实出过的错误。审核时逐条检查，违反任何一条都要返工。

| 编号 | 规则 | 依据 |
|---|---|---|
| R1 | 重写 `replaceInput` / `replaceOutput` 时，**必须先调用 `isInput` / `isOutput`**，不匹配就返回原值 | KubeJS 的 `RecipeJS.replaceInput` 会对每个输入值都调用这个方法，自己不判断匹配；第一轮因此把所有流体都替换了（P0-1） |
| R2 | 替换时保留原来的数量或用量：物品保留 count，`SizedIngredient` 保留 `amount_needed` | KubeJS 自带的 `InputItem` / `OutputItem` 替换就是保留原数量的（P0-2） |
| R3 | 只要是输入或输出性质的字段，组件都必须实现匹配；物品类组件**必须**支持 `event.remove({input/output})` | 不注册 Schema 时 KubeJS 本来就能匹配，注册后不支持就成了功能回归（P0-3） |
| R4 | 配方事件期间涉及标签的判断，一律用 `component/TagJson`（底层是 KubeJS `TagContext`）。**禁止**用 `FluidIngredient.getFluids()`、`FluidOutput.get()`、`ItemOutput.get()`、`Fluid.is(tag)`、`Item.builtInRegistryHolder().is(tag)` 来做判断 | 配方加载时注册表里的标签还没绑定（P0-4）。物品 `Ingredient` 不在此列：KubeJS 用 `IngredientTagValueMixin` 修补过它，可以直接调用 `ItemMatch.contains(ingredient)` 和 `getItems()` |
| R5 | 不允许静默填值：拿不到数据就抛出 `RecipeExceptionJS`，报错信息用英文 | P0-4、P0-6 |
| R6 | `constructorDescription` 不允许返回 `TypeDescJS.ANY`；结构太复杂的谓词可以写成 `STRING.or(ANY_MAP)` | P1-1 |
| R7 | 实现每个配方之前，**先打开它在匠魂源码里的 `LOADER` 逐字段核对**，附录只作参考 | 附录在第二轮复核中又修正了 5 处，包括 `modifier_predicate` 的默认值、回收配方 `results` 的键类型、斩首类的结果类型 |
| R8 | 优先复用第 4 节列出的现有组件，不要另写一套功能相同的组件 | |
| R9 | 交付时必须附交付说明（第 8 节） | 第一轮没有附 |
| R10 | 遵守用户规则：Java 不写注释；文件用 UTF-8；可选依赖不写入 `mods.toml`；每个阶段完成后运行 `gradlew build`（证书报错时加 `-Dnet.minecraftforge.gradle.check.certs=false`） | |

## 3. 本期范围

### 第二期：材料与部件（6 个）

| ID | 配方类 | 要点 |
|---|---|---|
| `material` | `MaterialRecipe` | `ingredient` 为输入；`leftover` 用 `ITEM_OUTPUT_OPTIONAL_STACK`，也是输出；`value`、`needed` 默认都是 1 |
| `part_builder` | `PartRecipe` | `result` 是合并结构 `{item: IMaterialItem, count?=1}`，见 5.1；`pattern_item` 默认 `#tconstruct:patterns/default` |
| `item_part_builder` | `ItemPartRecipe` | `result` 用 `ITEM_OUTPUT_REQUIRED_STACK`；`material` 默认 `MaterialId.UNKNOWN`；校验：`material` 不是 UNKNOWN 时 `cost` 必须大于 0，见源码 validate |
| `tool_building` | `ToolBuildingRecipe` | `result_count` 默认 1；`slot_layout` 可为空（ResourceLocation）；`extra_requirements` 是 Ingredient 列表；`parts_override` 是部件物品列表，可为空 |
| `part_builder_recycling` | `PartBuilderRecycle` | `results` 是 **Pattern ID → ItemOutput(REQUIRED_STACK)** 的映射，见 5.3 |
| `part_builder_tool_recycling` | `PartBuilderToolRecycle` | `tools` 是 SizedIngredient，默认 `#tconstruct:multipart_tool`；`parts` 是 IMaterialItem 列表 |

### 第三期：工匠台、词条与斩首（28 个）

| 分组 | ID |
|---|---|
| 添加词条 | `modifier`、`incremental_modifier`、`swappable_modifier`、`multilevel_modifier`、`multilevel_incremental_modifier` |
| 黏液保护 | `overslime_modifier`、`crafting_overslime_modifier` |
| 词条回收与修复 | `modifier_salvage`、`modifier_repair`、`crafting_modifier_repair` |
| 工作台 | `remove_modifier`、`extract_modifier`、`modifier_sorting`、`modifier_set_worktable`、`enchantment_converting`、`toggle_interaction` |
| 工匠台其他 | `tinker_station_damaging`、`tinker_station_part_swapping`、`tool_material_swapping`、`fixed_material_swapping`、`part_swapping_override`、`tipped_tool_transform` |
| 斩首 | `severing`、`ageable_severing`、`player_beheading`、`snow_golem_beheading`、`mooshroom_demushrooming`、`sheep_shearing` |

后 4 个斩首配方只有 `per_level_chance` 和 `looting_bonus` 两个字段，生物是写死在代码里的。也要注册，否则 KubeJS 没法修改它们的掉落概率。

### 仍然不做

和总文档第 4 节相同：5 个 SimpleRecipeSerializer、2 个已弃用的材料修复配方，以及 3 个 `crafting_*_material(s)` 配方。

## 4. 可直接复用的现有组件（`TinkerComponents`）

| 组件 | 用途 |
|---|---|
| `INGREDIENT_REQUIRED` / `INGREDIENT_OPTIONAL` | 原版 Ingredient，已支持匹配和替换 |
| `ITEM_OUTPUT_REQUIRED_ITEM` / `_REQUIRED_STACK` / `_OPTIONAL_STACK` | Mantle `ItemOutput`，已支持匹配、替换和标签 |
| `ENTITY_INGREDIENT` | 实体原料 |
| `MATERIAL_VARIANT`、`MATERIAL_STATS`、`MODIFIER`、`PATTERN` | 字符串 ID |
| `MATERIAL_PREDICATE` | 材料谓词，原样透传 |
| `ITEM`、`ITEM_RESULT`、`MATERIAL_ITEM`、`MODIFIABLE_ITEM` | 物品 ID。后三个已支持按产物匹配 |
| `POSITIVE_INT`、`NON_NEGATIVE_INT`、`BOOLEAN`、`GROUP` | 基础类型 |
| `LoadableComponent` | 包装任意 Mantle `Loadable`；需要按产物匹配时，传入 `outputStack` 函数 |
| `TagJson` | 标签和流体的解析与匹配工具 |

`TinkerRecipeJS.afterLoaded` 按配方类型分支做校验。本期新增的校验也加在这里，用 `switch` 分支，不要另建子类。

## 5. 需要新增的组件

每个组件都要满足 R1 到 R6。下表的"包装对象"都已经在源码里确认过。

| 组件 | 包装对象 | 角色 | 匹配与替换要求 | 类型描述 |
|---|---|---|---|---|
| `SIZED_INGREDIENT` | `SizedIngredient.LOADABLE`（Mantle） | INPUT | `isInput`：遍历 `getMatchingStacks()`，只要有一个满足 `ItemMatch.contains` 就算匹配。替换：用新 Ingredient 和原 `getAmountNeeded()` 重建。读取：支持 KubeJS 的 `InputItem`，`'4x minecraft:leather'` 转成 `SizedIngredient.of(ingredient, 4)`；也支持 JSON `{item/tag, amount_needed}` | `javaType(InputItem)` 或 `{item?, tag?, amount_needed?}` |
| `ITEM_STACK_OUTPUT` | `ItemStackLoadable.REQUIRED_STACK_NBT`（Mantle），用于 `leftovers` | OUTPUT | `isOutput`：`ItemMatch.contains(stack)`。替换时保留数量和 NBT 以外的属性，参照 `ItemOutputComponent.replaceOutput` | `javaType(OutputItem)` |
| `LEVEL_RANGE` | `ModifierEntry.VALID_LEVEL`（IntRange，1–32767） | OTHER | 无 | `NUMBER` 或 `{min?, max?}` |
| `SLOT_COUNT` | `SlotType.SlotCount.LOADABLE` | OTHER | 无 | `{[slot: string]: number}`，只允许一个键 |
| `LEVEL_ENTRY` | `MultilevelModifierRecipe.LevelEntry.LOADABLE` | OTHER | 无 | `{level: number, slots?: {...}}` |
| `MODIFIER_PREDICATE` | `ModifierPredicate.LOADER`，**默认值 `ModifierPredicate.ANY`** | OTHER | 无 | `STRING.or(ANY_MAP)` |
| `TOOL_PART` | `TinkerLoadables.TOOL_PART_ITEM` | OTHER | 无 | `STRING` |
| `INDEX_ARRAY` | `new IntArrayLoadable(IntLoadable.FROM_ZERO, -1, 10)`，参数照源码 | OTHER | 无。允许写单个整数 | `NUMBER` 或 `number[]` |
| `PERCENT` | `FloatLoadable.PERCENT`，范围 0–1 | OTHER | 无 | `NUMBER` |
| `VARIANT_FORMATTER` | `SwappableModifierRecipe.VariantFormatter.LOADER` | OTHER | 无 | `STRING` |
| `RESOURCE_LOCATION` | `Loadables.RESOURCE_LOCATION`，用于 `slot_layout`、`data_key` | OTHER | 无 | `STRING` |

### 5.1 合并结构字段（MergingField）

匠魂用 `MergingField` 把多个字段写进同一个 JSON 对象，Schema 要用**一个**组件读写整个对象：

| 配方 | JSON 写法 | 组件 |
|---|---|---|
| `part_builder` | `"result": {"item": "<IMaterialItem>", "count": 1}`，`count` 默认 1，等于 1 时不写出 | 新建 `MATERIAL_ITEM_RESULT`，值为 `record(IMaterialItem item, int count)`，角色 OUTPUT，支持按产物匹配 |
| `swappable_modifier` | `"result": {"name": "<ModifierId>", "value": "<string>"}`，两个字段都必填 | 新建 `SWAPPABLE_RESULT`，值为 `record(ModifierId name, String value)`，角色 OTHER |
| `damagable_melting` | 已实现（`FLUID_OUTPUT_UNIT`） | 参考它的写法 |

### 5.2 列表字段

- 用 `组件.asArray()`。数量下限照源码：`modifier` 的 `inputs` 至少 1 个，并且**不允许**写成单个对象；`multilevel_modifier` 的 `inputs` 允许为空，也允许写成单个对象；`levels` 至少 1 个。数量下限在 `TinkerRecipeJS.afterLoaded` 里校验。
- 允许写成单个对象的字段，读取时用 `asArrayOrSelf()`；写出格式和匠魂一致，一律写成数组。

### 5.3 `part_builder_recycling.results`

源码是 `Pattern.PARSER.mapWithValues(ItemOutput.Loadable.REQUIRED_STACK)`，JSON 是一个对象，键为 Pattern ID，值为物品输出。

- 实现前，先确认 KubeJS 2001.6.5 的 `RecipeComponent.asMap(keyComponent)`（`MapRecipeComponent`）写出的 JSON 是不是普通对象。是的话就用 `ITEM_OUTPUT_REQUIRED_STACK.asMap(PATTERN)`，否则自己写组件。
- 每个值都要支持按产物匹配和替换。

## 6. 每个配方额外要做的校验（加在 `TinkerRecipeJS.afterLoaded`）

| 配方 | 校验 | 源码依据 |
|---|---|---|
| `modifier` / `swappable_modifier` | `inputs` 不能为空 | `list(1)` |
| `multilevel_modifier` | `inputs` 为空且 `allow_crystal=false` 时报错；`levels` 至少 1 个 | 类里的 validate |
| `multilevel_incremental_modifier` | `levels` 至少 1 个 | `list(1)` |
| `item_part_builder` | `material` 不是 UNKNOWN 时，`cost` 必须大于 0 | validate |
| `modifier_sorting` | `inputs` 只允许 1 个：匠魂对多个输入会打弃用警告，我们直接报错 | 弃用警告 |
| `fixed_material_swapping` / `part_swapping_override` | `index` 有 1 到 10 个元素，每个都 ≥ 0 | `IntArrayLoadable(..., -1, 10)` |
| `crafting_shapeless_materials` | 不在本期范围 | |

实施时要在每个配方类里搜索 `validate(`。如果还有本表没列出的校验，一并实现，并写进交付说明。

## 7. 删除与替换：哪些字段算输入、哪些算输出

| 字段 | 角色 |
|---|---|
| `inputs`、`input`、`ingredient`、`tools`、`tool`、`pattern`（回收配方中是 Ingredient）、`pattern_item`、`extra_requirements` | INPUT |
| `result`（物品类）、`leftover`、`leftovers`、`results`、`adult_result`、`child_result`、`parts`（回收配方中是产物） | OUTPUT |
| 词条 ID、材料 ID、Pattern ID、槽位、等级、谓词 | OTHER |

注意：
- `tools` 虽然是输入，但 `remove({input: 某个工具})` 删掉"可用于这个工具的词条配方"是合理的。按 R3 实现匹配即可，不做特殊处理。
- `part_builder` 的 `pattern` 是 **Pattern ID**，角色 OTHER；回收配方的 `pattern` 是 **Ingredient**，角色 INPUT。两者不要混用组件。

## 8. 交付说明格式

交付时在 `docs/` 下新建 `delivery-phase2-3.md`，内容包括：

1. 新增和修改的文件列表；
2. 新注册的配方 ID 列表，必须是第 3 节的全集；有遗漏要写明原因；
3. 新增组件列表，每个写明包装的 Loadable、角色、是否支持匹配和替换；
4. 和本文档或附录不一致的地方，以及以源码为准的依据（类名和行号）；
5. `gradlew build` 的结果。

## 9. 审核方式（交付后由主对话执行）

1. 运行 `D:/AI临时文件/verify_schema.py`，比对所有 Schema 的键和 `LOADER` 字段。已知的合理差异如下，其余差异都要解释：
   - `cooling_time`、`temperature` 改为可选，由代码计算；
   - 解析器误报：`melting` 系列的常量名、`rate` 重名、`unit_size`。
2. 逐个打开组件，检查 R1 到 R6。
3. 抽查 MergingField 和列表字段：读入附录里的示例 JSON 再写出，看结果是否等价（只做静态推演）。
4. 游戏内测试由用户决定是否进行。如果要测，建议的测试项：
   - `event.remove({output: 'tconstruct:pick_head'})` 能删掉部件制作台和浇铸配方；
   - `event.replaceInput({type: 'tconstruct:modifier'}, 'minecraft:diamond', 'minecraft:emerald')` 只替换钻石，数量不变；
   - `event.recipes.tconstruct.modifier(...)` 添加的词条能在工匠台上使用。
