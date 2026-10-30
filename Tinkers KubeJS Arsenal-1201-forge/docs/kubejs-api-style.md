# KubeJS 写法规范（链式风格）

> 制定日期：2026年10月30日
> 适用范围：本模组注册的**所有**配方，包括今后新增的配方。所有实施文档都必须引用本规范，违反规范的实现要返工。

## 1. 规则

| 编号 | 规则 | 说明 |
|---|---|---|
| S1 | 每个配方只有**一种**写法 | 不提供多参数的位置写法。KubeJS 自带的 `event.custom(json)` 不算在内，它是原始 JSON 的后备写法 |
| S2 | 构造函数**最多 1 个参数**，并且必须是单值：一个 ID、一个物品或一个标签 | 用 `RecipeSchema.constructor(key)` 只登记这一种构造方式；不需要构造参数时，用 `constructor()` 登记无参构造 |
| S3 | 构造参数取“最能代表这个配方的东西” | 熔化类取原料；浇铸类取产物；词条类取词条 ID；合金不收参数。新配方要在实施文档里写明取了什么、为什么 |
| S4 | “ID + 数量”一律写成**两个参数**的链式方法 | 例如 `.fluid(流体, 用量)`、`.result(流体, 用量)`、`.slots(槽位, 数量)`。禁止要求用户写 `{fluid, amount}` 这样的对象 |
| S5 | 需要逐个累加的列表，用**单数名**方法，每调用一次追加一项 | 例如 `.byproduct(...)`、`.input(...)`、`.level(...)`。元素是单值、整体传入更自然的列表，可以用同名方法直接传数组 |
| S6 | 普通单值字段，用 KubeJS 自动生成的同名链式方法 | 方法名就是 JSON 键名（蛇形命名），例如 `.cooling_time(50)`、`.cast_consumed(true)` |
| S7 | 有自定义方法的字段，必须对该键调用 `.noBuilders()` | 避免同一个字段有两种设置方式 |
| S8 | 必填但改由链式方法设置的字段，放进 `chain()` 的 `later(...)` | 写出 JSON 时会统一检查；缺少时抛出英文异常，并提示该调用哪个方法 |
| S9 | 默认值计算和校验都放在 `TinkerRecipeJS.serialize()`，**不要放在 `afterLoaded()`** | KubeJS 执行完构造函数后、执行链式方法之前，就会调用 `afterLoaded()`，这时链式设置的字段还是空的 |
| S10 | 自定义链式方法放在**按配方家族划分**的 `RecipeJS` 子类里 | 现有：`MeltingRecipeJS`、`CastingRecipeJS`、`AlloyRecipeJS`、`ModifierRecipeJS`。不要放进通用的 `TinkerRecipeJS`，否则会覆盖其他配方自动生成的同名方法 |
| S11 | 自定义方法的参数名要有意义，并且沿用已有的名字 | 编译时开启了 `-parameters`。ProbeJS 插件按参数名补全类型：`fluid` 提示流体和流体标签，`rate` 提示矿石倍率，`rateOrUnitSize` 提示矿石倍率或数字，`slot` 提示槽位类型。新增同类参数要沿用这些名字；新增参数类型时，要同步更新 `ArsenalProbeJSPlugin.PARAM_TYPES` |
| S12 | 物品类参数直接声明为 KubeJS 的 `InputItem` / `OutputItem` | 这样能自动支持 `'4x minecraft:diamond'`、标签和 `Item.of(...)`，ProbeJS 也会自动补全 |
| S13 | 方法名用英文 | 方法名和 JSON 键名保持一致，或用常见的英文单词 |
| S14 | 同名方法、同样参数个数时，不要只靠 `String` 和数字类型区分重载 | Rhino 会把数字转成 `String` 并选错重载（实测：`.result(流体, 180, 90)` 被匹配到 `rate` 版本）。这种情况合并成一个 `Object` 参数，在方法里按类型分派 |

## 2. 已按规范完成的配方

### 熔化（`MeltingRecipeJS`）
```js
event.recipes.tconstruct.melting('#forge:ingots/gold')
  .result('tconstruct:molten_gold', 90)
  .temperature(700)                          // 可省略，自动计算
  .time(60)                                  // 可省略，自动计算
  .byproduct('tconstruct:molten_copper', 10)

event.recipes.tconstruct.ore_melting('#forge:ores/gold')
  .result('tconstruct:molten_gold', 180, 'metal')
  .byproduct('tconstruct:molten_copper', 90, 'metal')

event.recipes.tconstruct.damagable_melting('minecraft:golden_sword')
  .result('tconstruct:molten_gold', 180, 90) // 第 3 个参数是单位耐久
```

### 浇铸（`CastingRecipeJS` / `TinkerRecipeJS`）
| 配方 | 构造参数 | 链式必填 | 常用可选 |
|---|---|---|---|
| `casting_table` / `casting_basin` / `retextured_casting_*` | 产物 | `.fluid(流体, 用量)` | `.cast()` `.cast_consumed()` `.switch_slots()` `.cooling_time()` |
| `table_duplication` / `basin_duplication` | 铸模 | `.fluid()` | `.cooling_time()` |
| `casting_*_potion` | 产物 | `.bottle()` `.fluid()` | `.cooling_time()` |
| `casting_*_tipping` / `casting_*_tipped_clearing` | 词条 ID | `.tools()` `.fluid()` | `.cooling_time()` |
| `*_filling` | 容器 | `.fluid_amount()` | |
| `*_casting_material` / `*_casting_composite` / `*_tool_casting` | 产物 | `.item_cost()` | `.cast()` `.allowed_materials()` 等 |
| `*_casting_part_swapping` | 工具 | `.item_cost()` `.index()` | `.allowed_materials()` |
| `material_fluid` | 产出材料 | `.fluid()` | `.input()` `.temperature()` |
| `molding_*` | 产物 | `.material()` | `.pattern()` `.pattern_consumed()` |

```js
event.recipes.tconstruct.casting_table('minecraft:gold_ingot')
  .fluid('tconstruct:molten_gold', 90)
  .cast('#tconstruct:casts/multi_use/ingot')
  .cast_consumed(false)
  .cooling_time(50)                          // 可省略，自动计算
```

### 合金（`AlloyRecipeJS`）
```js
event.recipes.tconstruct.alloy()
  .input('tconstruct:molten_copper', 270)
  .input('tconstruct:molten_tin', 90)
  .catalyst('tconstruct:molten_quartz', 10)  // 只要求存在、不消耗
  .result('tconstruct:molten_bronze', 360)
  .temperature(700)                          // 可省略，按产物计算
```

### 词条（`ModifierRecipeJS`）
```js
event.recipes.tconstruct.modifier('tconstruct:diamond')
  .tools('#tconstruct:modifiable')
  .input('minecraft:diamond', 4)             // 也可以写 .input('4x minecraft:diamond')
  .input('minecraft:nether_star')
  .slots('upgrades', 1)
  .level(1, 3)                               // 等级范围；.level(3) 表示只能是 3 级
  .max_tool_size(1)

event.recipes.tconstruct.incremental_modifier('tconstruct:sharpness')
  .tools('#tconstruct:modifiable/melee')
  .input('minecraft:quartz')                 // 增量词条只有一个原料，不能带数量
  .needed_per_level(36)
  .amount_per_item(1)
  .slots('upgrades', 1)

event.recipes.tconstruct.swappable_modifier('tconstruct:embellishment')
  .variant('tconstruct:iron')
  .tools('#tconstruct:modifiable')
  .input('minecraft:iron_ingot', 4)

event.recipes.tconstruct.multilevel_modifier('tconstruct:luck')
  .tools('#tconstruct:modifiable')
  .input('minecraft:lapis_block')
  .level(1, 'upgrades', 1)                   // 每调用一次加一级，可以带槽位消耗
  .level(2)
  .level(3, 'abilities', 1)
```

## 3. 还没改造的配方（后续阶段必须按本规范改）

下面这些配方还保留着 KubeJS 默认的位置参数构造方式，后续实施文档要逐个改造，并在交付说明里列出每个配方的构造参数和链式方法：

- 熔化：`material_melting`、`melting_fuel`、`entity_melting`；
- 第二期：`material`、`part_builder`、`item_part_builder`、`tool_building`、`part_builder_recycling`、`part_builder_tool_recycling`；
- 第三期：
  - 黏液保护：`overslime_modifier`、`crafting_overslime_modifier`；
  - 回收与修复：`modifier_salvage`、`modifier_repair`、`crafting_modifier_repair`；
  - 工作台：`remove_modifier`、`extract_modifier`、`modifier_sorting`、`modifier_set_worktable`、`enchantment_converting`、`toggle_interaction`；
  - 工匠台：`tinker_station_damaging`、`tinker_station_part_swapping`、`tool_material_swapping`、`fixed_material_swapping`、`part_swapping_override`、`tipped_tool_transform`；
  - 斩首：`severing`、`ageable_severing` 和 4 个固定生物的斩首配方。

## 4. 实现方式（给实施者）

- **注册 Schema：** 用 `TinkerRecipeSchemas.chain(配方类, 构造器, 构造键或 null, later(链式必填键...), 可选键...)`。
- **必填检查：** `later(...)` 里的键会自动改成可选，写出时由 `TinkerRecipeJS.serialize()` 统一检查。报错提示的方法名来自 `TinkerRecipeJS.HINTS`，默认就是键名。
- **自定义方法里设置字段：**
  - JSON 或可被组件解析的值，用 `read(键, 值)`；
  - 已经是目标 Java 类型的值，用 `store(键, 值)`；
  - 追加列表时，用 `written(键)` 取出现有 JSON 数组，追加新元素后再 `read` 回去。
- **校验与默认值：** 新的校验和默认值计算一律写在 `serialize()` 的 `switch` 里。
