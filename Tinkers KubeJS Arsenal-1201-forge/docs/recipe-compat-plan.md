# 匠魂配方 KubeJS 兼容实施文档

> 编写日期：2026年10月30日
> 用途：交给实施模型按本文档编码；完成后由主对话审核。
> 字段参考附录：[tconstruct-recipe-reference.md](tconstruct-recipe-reference.md)（76 个序列化器的字段、默认值与真实 JSON 示例）。

## 1. 环境基线（已核对）

| 项目 | 版本 | 来源 |
|---|---|---|
| Minecraft / Forge | 1.20.1 / 47.4.23 | `gradle.properties` |
| Tinkers' Construct | 3.12.0.220（CF 文件 8809959） | jar 内 `mods.toml` |
| Mantle | 1.11.117（CF 文件 8809819） | `gradle.properties` 注释 |
| KubeJS | 2001.6.5-build.26（CF 文件 8020595） | jar 内 `mods.toml` |
| ProbeJS | 6.0.1（CF 文件 5227399，1.20.1 Forge 的最新版） | CurseForge 文件列表 + jar 内 `mods.toml` |

- 反编译源码在 `D:/AI临时文件/tkja-src/{tconstruct,mantle,kubejs,probejs}`。所有类和方法的用法以这里为准，不要参考网上教程，也不要参考本机其他项目（尤其是 `tetra_data_kubejs`）的写法。
- 匠魂本体**没有**任何 KubeJS 集成代码（已 grep 确认），所有配方都要由本附属提供。
- ProbeJS 已作为 `compileOnly` + `runtimeOnly` 开发依赖加入 `build.gradle`，**不写入** `mods.toml`。

## 2. 实施硬性规则

1. 不允许简化实现。字段、默认值和校验以附录和源码为准，不能漏字段，也不能把可选字段写死。
2. Java 代码**不写注释**。
3. 使用任何类或方法之前，先在反编译源码里确认签名和语义。
4. 文件一律使用 UTF-8。
5. `mods.toml` 中把 `tconstruct`、`kubejs` 声明为必需依赖（`mandatory=true`）。按用户规则，`versionRange="[0.0.1,)"`，不设最高版本。ProbeJS 是可选依赖，不写入。
6. ProbeJS 的类只能出现在独立的类里，并且只在确认 ProbeJS 已加载时才去触碰这些类（见第 8 节）。没装 ProbeJS 时，绝不能因为类加载失败而崩溃。
7. 每个阶段完成后运行 `gradlew build` 验证（如遇证书错误，加参数 `-Dnet.minecraftforge.gradle.check.certs=false`）。是否做客户端或服务端测试，由用户决定。
8. 不改本文档范围以外的文件。
9. 附录最初由子代理盘点，之后经过两轮脚本比对（`D:/AI临时文件/verify_ref.py`、`verify_ref2.py`：把源码 `LOADER` 和文档逐字段比对）和人工复核，修正了 1 处漏节和 4 处多列的 `group`。实施时如果发现附录和源码不一致，**以源码为准**，并在交付说明里列出。
10. 如果实施时又把搜索、盘点之类的工作交给子代理，子代理给出的"已全部覆盖"这类结论不能直接采用，必须用脚本或逐项对照来验证。

## 3. 总体架构

### 3.1 插件入口

KubeJS 6 从 jar 根目录的 `kubejs.plugins.txt` 读取插件（源码 `KubeJSPlugins.loadFromFile`）。每行格式为 `类名 [依赖模组ID...] [client]`，列出的模组没有全部加载时，就跳过这个插件。

```
src/main/resources/kubejs.plugins.txt
com.hp.tinkers_kubejs_arsenal.kubejs.ArsenalKubeJSPlugin tconstruct
```

`ArsenalKubeJSPlugin extends KubeJSPlugin`，需要重写：
- `registerRecipeSchemas(RegisterRecipeSchemasEvent event)`：用 `event.namespace("tconstruct").register(路径, schema)` 注册全部配方。注册后，脚本里可以用 `event.recipes.tconstruct.melting(...)` 调用。
- `registerBindings(BindingsEvent event)`：暴露辅助类（第 6 节）。

主类 `TinkersKubejsArsenal` 不直接引用任何 KubeJS 类。

### 3.2 建议的包结构

```
com.hp.tinkers_kubejs_arsenal.kubejs
├── ArsenalKubeJSPlugin
├── component      匠魂专用 RecipeComponent
├── schema         每个配方组一个接口，内含 RecipeKey 与 RecipeSchema 常量
├── recipe         需要计算默认值的 RecipeJS 子类
├── binding        暴露给脚本的辅助类
└── probejs        ProbeJS 相关代码（第 8 节，后期）
```

写法参考 KubeJS 自带的 `dev.latvian.mods.kubejs.recipe.schema.minecraft.CookingRecipeSchema`：用接口常量定义 `RecipeKey`，再用 `new RecipeSchema(...)` 组合起来。

### 3.3 自定义组件（component 包）

KubeJS 自带的 `FluidComponents.INPUT/OUTPUT` 底层是 `FluidStackJS`，**不支持流体标签**，而且写出的 JSON 会带上多余的 `nbt` 字段。所以必须写匠魂专用的组件。每个组件都必须实现：

| 方法 | 要求 |
|---|---|
| `componentClass()` | 返回真实值类型 |
| `read` / `write` | 和附录里的 JSON 格式严格一致，往返读写后结果不变 |
| `constructorDescription(DescriptionContext)` | 返回准确的类型描述，ProbeJS 补全依赖这个方法（第 8 节） |
| `role()` | 按输入或输出返回 `ComponentRole.INPUT/OUTPUT` |
| `isInput` / `isOutput` | 支持 `event.remove({input/output: Fluid.of(...)})`：KubeJS 用 `FluidLike` 来匹配，见 `FluidComponents.isOutput` |
| `replaceInput` / `replaceOutput` | 支持 `event.replaceInput/replaceOutput`，用于流体替换 |
| `checkEmpty` | 必填值为空时返回明确的英文报错 |

需要实现的组件：

| 组件 | 对应格式（详见附录第 3 节） |
|---|---|
| `TinkerFluidIngredient` | Mantle `FluidIngredient`：`{fluid, amount}`、`{tag, amount}`，或至少 2 个元素的数组。脚本侧接受 `Fluid.of('x', 90)`、`'#forge:molten_iron'` 加数量、对象写法 |
| `TinkerFluidOutput` | Mantle `FluidOutput`：只能是对象，`{fluid, amount}` 或 `{tag, amount}`，可选 `nbt` |
| `TinkerItemOutput` | Mantle `ItemOutput`：分 `*_ITEM` 和 `*_STACK` 两种变体，前者不写 `count`，后者支持 `count` 和 `tag` 形式 |
| `SizedIngredient` | Ingredient 加 `amount_needed`（默认 1） |
| `EntityIngredient` | `{type}`、`{tag}`、`{types:[...]}`，或数组 |
| `AlloyInput` | FluidIngredient 平铺，或 `{match, catalyst}` |
| `ModifierId`、`MaterialVariantId`、`MaterialStatsId`、`Pattern` | 字符串 ID，读取时校验格式；材料变体允许 `mat#variant` |
| `IntRange` | 整数或 `{min, max}`，默认 1–32767 |
| `SlotCount` | `{"槽位类型": 数量}`，只允许一个键 |
| `MaterialPredicate` / `ModifierPredicate` | 先原样透传 JSON（嵌套谓词结构很复杂），类型描述写成 `object`，在第 9 节列为后续增强 |
| 枚举 | `rate`（none/metal/gem/default）、`cast_purpose` 用 KubeJS 的 `EnumComponent` 或字符串组件，写出小写值 |

## 4. 配方范围与分期

ID 前缀都是 `tconstruct:`，字段一律以附录为准。

### 第一期：冶炼与浇铸

| 配方 | 说明 |
|---|---|
| `melting` | 默认值计算见第 5 节 |
| `ore_melting` | 必填 `rate`；副产物元素可带 `rate` |
| `damagable_melting` | 产物和副产物可带 `unit_size` |
| `alloy` | `inputs` 至少 2 个 |
| `melting_fuel` | 校验：`fluid` 非空时 `duration > 0` |
| `entity_melting` | `damage` 默认 2 |
| `casting_table` / `casting_basin` | 默认值计算见第 5 节；`result` 用 REQUIRED_ITEM，不写 `count` |
| `retextured_casting_table` / `retextured_casting_basin` | 字段同普通浇铸 |
| `table_duplication` / `basin_duplication` | `cast` 必填 |
| `table_filling` / `basin_filling` | `container` 必须是物品 ID |
| `molding_table` / `molding_basin` | 沙铸模的制作 |

第一期还要完成：第 3 节的全部流体组件、第 6 节的 `TinkerFluids` 常量和 `metal` 一键套装。

### 第二期：材料与部件

`material`、`material_fluid`、`material_melting`、`table_casting_material` / `basin_casting_material`、`table_casting_composite` / `basin_casting_composite`、`table_tool_casting` / `basin_tool_casting`、`table_casting_part_swapping` / `basin_casting_part_swapping`、`part_builder`、`item_part_builder`、`tool_building`、`part_builder_recycling`、`part_builder_tool_recycling`。

注意：
- `material_fluid` 没有 `group` 字段。`input` 和 `output` 都可以为空，但至少要有一个；实施前查看源码中的 validate 逻辑，确认这条约束是否成立。
- `part_builder` 的 `result` 是对象 `{item, count}`（MergingField），不是字符串。
- `cast_purpose` 的 `MAYBE_MATERIAL` 已经弃用，不要作为默认值写出 JSON，省略这个字段即可。

### 第三期：工匠台、词条与斩首

`modifier`、`incremental_modifier`、`swappable_modifier`、`multilevel_modifier`、`multilevel_incremental_modifier`、`overslime_modifier`、`crafting_overslime_modifier`、`modifier_salvage`、`modifier_repair`、`crafting_modifier_repair`、`remove_modifier`、`extract_modifier`、`modifier_sorting`、`modifier_set_worktable`、`enchantment_converting`、`toggle_interaction`、`tinker_station_damaging`、`tinker_station_part_swapping`、`tool_material_swapping`、`fixed_material_swapping`、`part_swapping_override`、`tipped_tool_transform`、`casting_table_tipping` / `casting_basin_tipping`、`casting_table_tipped_clearing` / `casting_basin_tipped_clearing`、`casting_table_potion` / `casting_basin_potion`、`severing`、`ageable_severing`、`player_beheading`、`snow_golem_beheading`、`mooshroom_demushrooming`、`sheep_shearing`。

注意：
- `modifier` 的 `inputs` 不允许写成单个对象；`multilevel_modifier` 的 `inputs` 允许单个对象。
- `multilevel_modifier`：`inputs` 为空且 `allow_crystal=false` 时要报错。
- `modifier_sorting` 的 `inputs` 超过 1 个时，匠魂会打出弃用警告，Schema 里只接受 1 个。

### 不做

| 配方 | 原因 |
|---|---|
| `tinker_station_repair`、`crafting_table_repair`、`armor_dyeing_modifier`、`banner_modifier`、`armor_trim_modifier` | SimpleRecipeSerializer，只有 `type` 一个字段，没有可配置内容 |
| `modifier_material_repair`、`crafting_modifier_material_repair` | 源码已标记弃用 |
| `crafting_shaped_material`、`crafting_shaped_materials`、`crafting_shapeless_materials` | 依赖材料 Ingredient（`tconstruct:material` 类型的 Ingredient）。等第 9 节的材料 Ingredient 包装做完再评估 |

## 5. 默认值计算（recipe 包）

匠魂的数据生成用以下公式（源码 `IMeltingRecipe`、`ICastingRecipe`、`MeltingRecipeBuilder`、`ItemCastingRecipeBuilder`）：

```
温度 temperature = 流体 FluidType.getTemperature() - 300
时间 time        = round((temperature + 300) ^ 0.585 * sqrt(amount / 90))
冷却 cooling_time = 同一公式（ICastingRecipe.calcCoolingTime → IMeltingRecipe.calcTimeForAmount）
```

实施要求：
- 直接调用 `IMeltingRecipe.getTemperature`、`IMeltingRecipe.calcTimeForAmount`、`ICastingRecipe.calcCoolingTime`，不要自己重写公式。
- 写一个 `RecipeJS` 子类，通过 `new RecipeSchema(子类.class, 子类::new, keys...)` 绑定。在 `afterLoaded()` 里先调用 `super.afterLoaded()`，再补缺失的值。`RecipeOptional` 拿不到配方里其他字段的值，所以不能用它来计算默认值。
- `melting`、`ore_melting`、`damagable_melting`：
  - 省略 `temperature` 时，按产物流体计算；
  - 省略 `time` 时，按温度和产物数量计算。
- 浇铸类（`casting_*`、`retextured_*`、`*_duplication`）：省略 `cooling_time` 时，按输入流体的温度和数量计算。
- `material_melting`、`material_fluid`、`alloy`：省略 `temperature` 时，按流体计算。
- 流体是**具体 ID** 时直接计算。
- 流体是**标签**时：先核实 KubeJS 6 在配方事件期间能否取到流体标签的内容（查 KubeJS 源码里配方事件的 TagContext 实现）。
  - 能取到：用标签里的第一个流体计算。
  - 取不到，或标签为空：抛出英文报错，要求脚本显式填写温度或时间。

  无论哪种情况，都不允许静默填一个固定值。
- 显式填写的值一律优先，不能覆盖。

## 6. 辅助绑定（binding 包）

通过 `registerBindings` 暴露，名字固定：

1. `TinkerFluids`：照搬 `FluidValues` 的常量，包括 INGOT 90、NUGGET 10、METAL_BLOCK 810、GEM 100、GEM_SHARD 25 等，完整列表见附录第 4 节。直接引用 `FluidValues` 的字段，不手抄数值。
2. `TinkerRecipes.metal(event, 选项对象)`：完全按匠魂 `SmelteryRecipeBuilder.metal()` 生成一种金属的整套配方（已实现）。
   - 选项：
     - `name`：必填，金属名；
     - `fluid`：必填，流体 ID 或 `#流体标签`；
     - `temperature`：可选，省略时按流体计算，流体标签解析不到时报错；
     - `ores`：可选，默认 true，控制是否生成矿石类配方；
     - `byproducts`：可选，数组，元素为 `{fluid, amount=90, rate='metal', requires?}`。按顺序取第一个可用的副产物：没写 `requires`，或 `requires` 指定的物品标签非空，就算可用。这和匠魂"只取第一个存在的副产物"的规则一致。
   - 生成内容（基础单位 90 mB，矿石倍率固定为 `metal`）：

     | 配方 | 规模 | 时间系数 | 其他 |
     |---|---|---|---|
     | 块熔化 | 9 | 3 | |
     | 块浇铸 | 810 mB | | 盆，不需要铸模 |
     | 锭 | 1 | 1 | 熔化，加浇铸：`casts/multi_use/ingot` 的金铸模和 `casts/single_use/ingot` 的消耗型沙铸模 |
     | 粒 | 1/9 | 1/3 | 同锭 |
     | 粗矿 | 1 | 1.5 | |
     | 粗矿块 | 9 | 6 | |
     | 稀疏矿 | 1 | 1.5 | `ores/<名>` ∩ `forge:ore_rates/sparse` |
     | 单矿 | 2 | 2.5 | `ores/<名>` − `tconstruct:non_singular_ore_rates` |
     | 密集矿 | 6 | 4.5 | `ores/<名>` ∩ `forge:ore_rates/dense` |

     副产物只加在矿石类配方上，用量为 `(int)(amount × 规模)`。
   - 每个条目都先用 KubeJS `TagContext` 检查物品标签（或标签的交集、差集）是否非空，不用 `conditions`。
   - 配方 ID 为 `tinkers_kubejs_arsenal:metal/<名>/<melting|casting>/<变体>`。如果 `tconstruct:smeltery/<melting|casting>/metal/<名>/<变体>` 已经存在，就跳过这一条并打一条英文日志。
   - 和最初设计的偏差：去掉了 `cast` 选项。匠魂的铸模是通用的，不按金属区分，锭、粒、块的浇铸总是生成。

## 7. 删除与替换

- 所有流体字段的组件都必须实现 `isInput/isOutput`，确保 `event.remove({ output: Fluid.of('tconstruct:molten_iron') })` 能匹配到匠魂的熔化配方。
- 这只对**通过本附属 Schema 解析的配方**生效。匠魂自带的 JSON 配方会在加载时被 KubeJS 用已注册的 Schema 解析，实施时要验证这一点：查 KubeJS 加载数据包配方时怎么选择 Schema（`RecipesEventJS` 中按 `type` 查找命名空间的逻辑）。

## 8. ProbeJS 补全

已核实：ProbeJS 6.0.1 的 `FormatterRecipeKey` 调用 `key.component.constructorDescription(...)` 生成参数类型，用 `key.preferred` 生成 Builder 方法名，并把 `optional` 的默认值写进注释。由此得出：

- 第一到三期**不需要写任何 ProbeJS 代码**。只要每个组件都正确实现 `constructorDescription`，每个可选键都用 `.optional(...)` 声明，补全就能自动生成。
- 键名用 JSON 原名（snake_case），再用 `.preferred("camelCase")` 提供驼峰别名，和 KubeJS 自带 Schema 的风格一致。
- 后期增强：匠魂的材料 ID 和词条 ID 是从数据包加载的，ProbeJS 默认不会列出。实现时要先研究 ProbeJS 6 的扩展点（`com.probejs.features.plugin.ProbeJSEvents`、`specials` 包）。相关代码放进 `kubejs.probejs` 包，并只在 `ModList.get().isLoaded("probejs")` 为真时才触碰，否则未安装 ProbeJS 时会出现类加载崩溃。

## 9. 后续增强（不在一到三期内）

1. 材料 Ingredient 包装：脚本能写出 `{"type":"tconstruct:material", ...}`，然后开放 `crafting_shaped_materials` 等配方。
2. `MaterialPredicate` / `ModifierPredicate` 的结构化构造器。
3. ProbeJS 的材料 ID 和词条 ID 补全。

## 10. 交付与审核清单

每期交付时附一份简短说明：新增了哪些文件、哪些配方、哪些行为和本文档不同、原因是什么。审核时会逐项检查：

- [ ] 本期范围内每个配方的每个字段都和附录一致（必填、可选、默认值、写出格式）。审核时用脚本把 Schema 的键和匠魂 `LOADER` 字段逐一比对，不只靠抽查。
- [ ] 每个配方都能对应附录里的一个真实 JSON 示例：用 Schema 读入后再写出，结果等价。
- [ ] 默认值计算调用的是匠魂的静态方法，标签流体的处理符合第 5 节。
- [ ] 所有组件都实现了 `constructorDescription`，没有直接返回 `any` 的情况（第 3.3 节标明透传的谓词除外）。
- [ ] 流体的删除和替换能生效。
- [ ] `mods.toml` 没有写入 ProbeJS；没有 ProbeJS 时，不会加载任何 ProbeJS 类。
- [ ] Java 代码里没有注释；全部文件是 UTF-8。
- [ ] `gradlew build` 通过。
