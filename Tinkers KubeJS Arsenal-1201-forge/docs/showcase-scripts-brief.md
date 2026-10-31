# 任务书：用 KubeJS 编写 Tinkers KubeJS Arsenal 全功能展示魔改包

> 编写日期：2026年10月31日
> 接收方：负责写脚本的 AI
> 目的：写一套大量使用本模组**全部**功能的 KubeJS 脚本（新材料、新属性类型、新部件、新模块化工具、弓弩、护甲、新词条、各种配方），由用户进游戏逐项查看效果。

---

## 0. 你要交付什么

1. `startup_scripts`、`server_scripts`、`client_scripts` 三类脚本，放在下面第 2 节指定的 `showcase` 子目录。
2. 全部需要的贴图 PNG（用 Python + Pillow 生成，见第 6 节）。
3. 一份游戏内检查清单 `run/kubejs/showcase-checklist.md`：按"进游戏后的操作顺序"列出每项内容要去哪里看、应该看到什么（第 9 节有格式要求）。
4. 一个发放测试物品的自定义命令（第 8 节），让用户一条命令拿到工作台、材料、部件和成品工具。

**你不能启动游戏**。写完后由用户进游戏查看，所以每个 API 都要照第 4 节的清单写，不要猜方法名；拿不准的写法去读第 3 节列出的源码。

---

## 1. 环境

| 项目 | 值 |
|---|---|
| Minecraft / 加载器 | 1.20.1 Forge 47.4.23 |
| KubeJS | 2001.6.5（Rhino 引擎，JS 语法不是标准 ES6，见第 7 节） |
| 匠魂 | Tinkers' Construct 3.x（1.20.1，CurseForge 文件 8809959） |
| 本模组 | Tinkers KubeJS Arsenal，模组 ID `tinkers_kubejs_arsenal` |
| 项目目录 | `E:\模组\发cf的模组\AAA-Tinkers\Tinkers KubeJS Arsenal\Tinkers KubeJS Arsenal-1201-forge` |
| 游戏目录（开发环境） | 项目目录下的 `run\`，KubeJS 目录为 `run\kubejs\` |
| 匠魂 jar 解压副本 | `D:\AI临时文件\tc_jar_8809959\`（贴图、数据、类文件都在这里，可参考和复制） |

用户启动游戏：在项目目录执行 `gradlew runClient`。开发环境已移除 Advanced Loot Info，避免它的 JEI 插件阻塞资源重载。

---

## 2. 目录与命名规则

### 2.1 放哪里

```
run/kubejs/startup_scripts/showcase/*.js
run/kubejs/server_scripts/showcase/*.js
run/kubejs/client_scripts/showcase/*.js
run/kubejs/assets/kubejs/textures/...      ← 贴图
run/kubejs/showcase-checklist.md
```

- KubeJS 会递归加载子目录。按主题拆文件，例如 `showcase/materials.js`、`showcase/tools.js`、`showcase/recipes.js`、`showcase/modifiers.js`。
- `run/kubejs` 里已经有别人的测试脚本（`tkja_*.js`、`test_phase9_*.js`、`example.js`），**不要修改、不要删除**。其中 `tkja_chain_test.js` 会故意报几条配方错误，那不是你的问题。用户看日志时，以带 `demo_` 的 ID 为准。

### 2.2 ID 规则

- 命名空间统一用 `kubejs`，所有新 ID 以 `demo_` 开头，例如 `kubejs:demo_mythril`、`kubejs:demo_great_blade`、`kubejs:demo_war_scythe`，避免和已有测试脚本撞 ID。
- 新属性类型同样用 `kubejs:demo_xxx`，不能用 `tconstruct:` 命名空间（会报错）。

### 2.3 用户的硬性规则（必须遵守）

- 文件一律 UTF-8 编码。
- 注释只用中文，每个小代码块都要有一行中文注释说明用途。
- **禁止写只调用一次的 function**。只用一次的逻辑直接内联；确实要复用（调用 2 次以上）才抽函数。
- 日志输出（`console.info`）用英文，加统一前缀 `[DEMO]`。
- 每个物品、材料、词条都要同时写英文名和中文名（`zh_cn`）。

---

## 3. 必读资料（先读再写）

全部在项目 `docs/` 下：

| 文件 | 内容 |
|---|---|
| `kubejs-api-style.md` | 链式写法规范，以及熔化、浇铸、合金、词条配方的写法示例 |
| `delivery-phase2-3.md` | 第二、三期配方类型清单 |
| `tconstruct-recipe-reference.md` | 每种匠魂配方的字段表与 JSON 示例。写非链式配方时查这里 |
| `delivery-phase4a.md` ~ `delivery-phase4d.md` | 材料：属性、特性、外观、变体、标签、贴图生成器、外观同步 |
| `delivery-phase5.md` | 自定义词条、词条事件、全局工具属性事件、`ArsenalTool` 查询 |
| `delivery-phase6.md` | 自定义工具（启动注册 + 服务端数值）、手册页 |
| `delivery-phase7.md` | 自定义部件（铸模、配方、贴图） |
| `delivery-phase8.md` | 新材料属性类型、`extend` 给已有材料加属性 |
| `delivery-phase9.md`、`handoff-phase9.md` | 弓、弩、护甲、护甲套 |

源码（遇到文档没写清的细节时以源码为准）：`src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/` 下的
- `schema/TinkerRecipeSchemas.java`：66 种配方的键名、必填与可选；
- `material/*`、`modifier/*`、`tool/*`。

---

## 4. 本模组全部脚本接口（已对照源码逐项核对）

### 4.1 全局绑定

| 名字 | 内容 |
|---|---|
| `ArsenalFluids` | 流体用量常量：`INGOT` `NUGGET` `METAL_BLOCK` `GEM` `GEM_SHARD` `LARGE_GEM_BLOCK` `SMALL_GEM_BLOCK` `BRICK` `BRICK_BLOCK` `GLASS_BLOCK` `GLASS_PANE` `SLIMEBALL` `SLIME_CONGEALED` `SLIME_BLOCK` `SLIME_DROP` `ICHOR_BYPRODUCT` `ICHOR_BLAZING_BLOOD` `BOWL` `BOTTLE` `SIP` `LANTERN_CAPACITY` |
| `ArsenalRecipes` | `ArsenalRecipes.metal(event, {name, fluid, temperature?, byproducts?, ores?})`，一次生成整套金属的熔化和浇铸配方（块、锭、粒、粗矿、粗矿块、三种矿石倍率）。`byproducts` 每项为 `{fluid, amount?=90, rate?='metal', requires?}`；`ores: false` 时不生成矿石相关配方 |
| `ArsenalTool` | 静态查询：`isTool(item)`、`modifierLevel(item, id)`、`hasModifier(item, id)`、`armorModifierLevel(entity, id)`（四件护甲等级之和）、`projectileModifierLevel(projectile, id)`、`getInt/setInt/getFloat/setFloat(item, key[, value])`（工具持久数据）、`rebuild(item)` |

### 4.2 事件

| 事件 | 脚本类型 | 生效方式 |
|---|---|---|
| `ArsenalMaterialEvents.statTypes` | startup | 重启游戏 |
| `ArsenalMaterialEvents.data` | server | `/reload` |
| `ArsenalMaterialEvents.tags` | server | `/reload` |
| `ArsenalMaterialEvents.assets` | client（客户端资源生成阶段） | `F3+T` |
| `ArsenalModifierEvents.data` | server | `/reload` |
| `ArsenalModifierEvents.meleeDamage / afterMeleeHit / projectileHit / blockBreak / hurt / damage / toolStats / attributes`（第一个参数是词条 ID） | server | `/kubejs reload server_scripts` |
| `ArsenalToolEvents.stats` | server | 工具下次重建时生效 |
| `ArsenalToolEvents.data` | server | `/reload` |
| `ArsenalToolEvents.armorSets` | startup | 重启游戏 |
| `StartupEvents.registry('item', ...)` 配合下面 6 种物品类型 | startup | 重启游戏 |

物品类型：`tinkers_kubejs_arsenal:tool`、`tinkers_kubejs_arsenal:sword`、`tinkers_kubejs_arsenal:bow`、`tinkers_kubejs_arsenal:crossbow`、`tinkers_kubejs_arsenal:armor`、`tinkers_kubejs_arsenal:part`。

### 4.3 材料（`ArsenalMaterialEvents.data`）

`event.create(id)` 新建材料；`event.extend(id)` 给已有材料（匠魂或其他模组的）**追加**属性。

| 分类 | 方法 |
|---|---|
| 基本 | `.tier(n)` `.sortOrder(n)` `.craftable(bool)` `.hidden(bool)` `.rarity('common'\|'uncommon'\|'rare'\|'epic')` |
| 近战/采掘 | `.head(耐久, 挖掘速度, 挖掘等级, 攻击)` `.handle(耐久%, 挖掘速度%, 攻速%, 攻击%)` `.binding()` |
| 远程 | `.limb(耐久, 拉弓速度, 箭速, 精准)` `.grip(耐久%, 精准, 攻击)` `.bowstring()` |
| 护甲 | `.platingHelmet/.platingChestplate/.platingLeggings/.platingBoots(耐久, 护甲, 韧性, 击退抗性)` `.platingShield(耐久, 韧性, 击退抗性)` `.maille()` `.shieldCore()` `.cuirass()` |
| 弹药 | `.arrowHead()` `.arrowShaft()` `.fletching()` |
| 史莱姆套 | `.skull(耐久, 护甲)` `.slime(耐久, 过载容量)` `.ribcage(修复量)` `.shell(修复量)` `.laces(修复量)` `.repairKit()` |
| 自定义 | `.customStat(属性类型ID, ...数值)`（按 `.field` 顺序）；`.stat(第三方属性ID, {字段: 值})`（只给其他模组的属性类型用，匠魂自带的 23 种会被拒绝） |
| 特性 | `.trait(词条[, 等级])`（默认特性）；`.statTrait(属性类型或分组, 词条, 等级)`（只在某类部件上生效；分组可写 `tconstruct:melee_harvest`、`tconstruct:ranged`、`tconstruct:armor`、`tconstruct:ammo`） |
| 外观 | `.color('RRGGBB')` `.fallbacks(...)`（取值：`bone` `cloth` `contrast` `crystal` `metal` `metal_contrast` `primitive` `rock` `slime_metal` `slime_wood` `stick` `vine` `wood`） `.luminosity(0~15)` `.texture(...)` `.generator('tconstruct:head', ...)` 或 `.generator({...})` `.generatorPalette({'063':'FF102030', ...})` `.textColor('RRGGBB')` |
| 文本 | `.name([lang,] text)` `.flavor([lang,] text)` `.encyclopedia([lang,] text)` |

- `extend` 里只能写属性方法（`.head`、`.customStat` 等）和 `.statTrait`。外观、文本、`tier`、`trait` 等方法在 `extend` 里都会报错，因为它们会整体替换原材料的数据。
- 要让新材料有按部件精细生成的贴图，必须调用 `.generator(...)`，参数里列出它支持的属性类型。不写的话，只会用后备贴图按 `.color` 着色。
- 材料要能用物品制作，还需要配方：`tconstruct:material`（物品 → 材料值）；能熔化浇铸则还需要 `material_fluid` 和 `material_melting`。

**材料变体**（客户端，`ArsenalMaterialEvents.assets`）：`event.create('kubejs:demo_mythril#polished')`，可链 `.color` `.fallbacks` `.luminosity` `.texture` `.generator` `.generatorPalette` `.textColor` `.name` `.flavor` `.encyclopedia`。

**材料标签**（`ArsenalMaterialEvents.tags`）：`event.create('kubejs:demo_xxx').add(id)`，另有 `.add(id, required)` `.addAll(a, b, ...)` `.addOptional(id)` `.replace(true)`。标签成员不能带 `#变体`。

### 4.4 新属性类型（`ArsenalMaterialEvents.statTypes`，startup）

```js
event.create('kubejs:demo_blade')
  .field('sharpness', 'attack_damage', 'base', 0)    // 字段名, 匠魂工具属性, 运算, 默认值
  .field('balance', 'attack_speed', 'percent', 0)
  .textureLike('head')                               // 贴图生成时当作哪类已有属性
  .name('zh_cn', '刃身')
```

运算有 4 种：`base`、`add`、`percent`、`multiply`。部件用 `.statType('kubejs:demo_blade')` 绑定这种属性类型后，只有写了 `.customStat('kubejs:demo_blade', ...)` 的材料才能做这个部件。

**手册**：每种新属性类型会在匠魂手册里自动生成一个材料章节，标题就是属性类型名，章节内每种带这类属性的材料各占一页。章节按书分配材料：
- 《材料与你》收 0–1 级材料；
- 《小熔炼》收 2 级；
- 《大熔炼》收 3 级；
- 《百科全书》收全部级别。

这是刚加入、还没进游戏验证的功能，检查清单里要单列。

### 4.5 部件（物品类型 `tinkers_kubejs_arsenal:part`）

`.statType(类型)`（必填） `.cost(n)` `.casts()`（同时注册金、沙、红沙铸模及其配方） `.partTexture(贴图)` `.displayName(英文)` `.name('zh_cn', 中文)`

### 4.6 工具、剑、弓、弩、护甲（启动脚本注册物品）

| 方法 | 适用 |
|---|---|
| `.part(图层名, 部件物品ID)` | 全部；顺序就是部件序号 |
| `.partTexture(图层名, 贴图)` | 全部；可以直接指向匠魂已有灰度底图，例如 `tconstruct:item/tool/broad_axe/head` |
| `.brokenPart(图层名)` | 工具、剑、弓、弩 |
| `.pullFrames(图层名...)` | 弓、弩 |
| `.slot('helmet'\|'chestplate'\|'leggings'\|'boots')` `.armorSet(套ID)` `.equipSound(音效ID)` `.bookArmor(手册名)` | 护甲 |
| `.name(lang, text)` `.bookText(lang, text)` `.bookProperty(lang, text)` | 全部 |
| KubeJS 自带：`.displayName` `.tag` `.rarity` `.parentModel` 等 | 全部 |

- 弓的 `limb` 图层会占两个部件位，`limb`、`grip`、`bowstring` 三个图层实际是 4 个部件，**只能在匠魂砧上组装**。超过 3 个部件的工具都要用匠魂砧，工匠站做不了。
- 弩是 3 个部件，工匠站可以组装。

**护甲套**（`ArsenalToolEvents.armorSets`，startup）：`event.create('kubejs:demo_bronze')`，可链 `.materialLayer(图层名, 部件序号)` `.fixedLayer(图层名)` `.dyedLayer(图层名)` `.trim()`。

### 4.7 工具数值（`ArsenalToolEvents.data`，server）

`event.create(工具ID)` 后可链：

- 属性与词条：`.baseStat(属性, 值)`（护甲不生效） `.multiplyStat(属性, 值)` `.slot('upgrades'|'abilities'|'defense'|..., 数量)` `.trait(词条[, 等级])`
- 交互与判定：`.toolActions(...)` `.effective(方块标签)`
- 范围挖掘：`.boxAoe(宽, 高, 深)` `.boxAoeExpansion(宽, 高, 深)` `.boxAoeDirection('pitch'|'height'|'side_hit')` `.circleAoe(直径, 是否三维)` `.veinAoe(最大距离)` `.treeAoe(宽加成, 深加成)`
- 范围近战：`.sweepMelee(范围)` `.circleMelee(直径)`
- 兜底：`.module(类型, {原始JSON})`
- 标签与工作台：`.modifiable(标签路径...)` `.primaryPart(序号)` `.materialName(序号)` `.layoutSlot(序号, x, y)` `.sortIndex(n)`

自动生成、不用写的内容：
- 公共：部件属性、默认材料、工作台布局、组装配方，以及 `multipart` 和 `durability` 标签。
- 弓：自动加 `ranged/longbows`、`interactable/left` 标签。
- 弩：自动加 `ranged/crossbows`、`interactable/left` 标签。
- 护甲：自动加部位标签和手册标签；护甲套声明了染色或纹饰，再加 `dyeable`、`armor/trim`、`armor/worn` 标签。
- 护甲套布局：同一套的各件合并成一个布局文件。

工具是否进手册由 `.modifiable` 决定：`small` 进《材料与你》和《百科全书》的小工具章，`broad` 进《强力熔炼》和《百科全书》的大工具章。两个都不加，手册里就没有这把工具。

### 4.8 词条（`ArsenalModifierEvents.data`，server）

`event.create(id)` 后可链：
- `.color('RRGGBB')` `.name/.flavor/.description([lang,] text)`
- `.icon(贴图...)`：贴图必须在方块图集里，例如 `minecraft:item/redstone`、`kubejs:item/demo_icon`
- `.levelDisplay('default'|'single_level'|'no_levels'|'pluses')` `.tooltipDisplay('always'|'tinker_station'|'never')` `.priority(n)`
- `.statBoost(工具属性, 'add'|'percent'|'multiply_base'|'multiply_conditional'|'multiply_all', 每级数值)`
- `.module(匠魂模块类型, {原始JSON})`

词条事件（只对本模组定义的词条触发，只在服务端触发）：

| 事件 | 时机 | 可读/可写 |
|---|---|---|
| `meleeDamage(id, e)` | 近战伤害计算 | 可改 `e.damage` |
| `afterMeleeHit(id, e)` | 命中后 | `e.damage` 为实际伤害 |
| `projectileHit(id, e)` | 弹射物命中 | `e.projectile` `e.hitEntity` `e.attacker` `e.target` `e.blocked` |
| `blockBreak(id, e)` | 挖掉方块后 | `e.pos` `e.state` `e.player` `e.world` `e.isAOE()` `e.canHarvest()` `e.isEffective()` |
| `hurt(id, e)` | 护甲计算前受击，每件护甲各触发一次 | 可改 `e.damage`；`e.source` `e.entity` `e.slot` `e.directDamage` |
| `damage(id, e)` | 护甲计算后受伤 | 同上 |
| `toolStats(id, e)` | 工具重建时 | `e.add/percent/multiply/multiplyAll(属性, 值)` `e.get(属性)` `e.hasTag` `e.modifierLevel` |
| `attributes(id, e)` | 工具重建时 | `e.add(实体属性, 数值, 'addition'|'multiply_base'|'multiply_total')` |

- 攻击类事件公共字段：`attacker` `player` `target` `livingTarget` `hand` `slot` `projectile` `critical` `fullyCharged` `extraAttack` `baseDamage` `damage` `tool` `level` `modifier`。
- 全局工具属性：`ArsenalToolEvents.stats(e => ...)`，对所有匠魂工具生效，不要求带词条。可用 `e.itemId` `e.hasTag(tag)` `e.modifierLevel(id)` `e.get(stat)` 判断。

### 4.9 配方（66 种，全部在 `event.recipes.tconstruct.*`）

**链式写法**（写法见 `kubejs-api-style.md`）：

| 家族 | 配方 |
|---|---|
| 熔化 | `melting` `ore_melting` `damagable_melting` |
| 浇铸 | `casting_table` `casting_basin` `retextured_casting_*` `*_duplication` `casting_*_potion` `casting_*_tipping` `casting_*_tipped_clearing` `*_filling` `*_casting_material` `*_casting_composite` `*_tool_casting` `*_casting_part_swapping` `material_fluid` `molding_*` |
| 合金 | `alloy` |
| 词条 | `modifier` `incremental_modifier` `swappable_modifier` `multilevel_modifier` `multilevel_incremental_modifier` |

**非链式写法**：KubeJS 默认的位置参数。参数顺序就是 `TinkerRecipeSchemas.java` 里必填键的顺序，可选键用同名链式方法补。

| 家族 | 配方 |
|---|---|
| 熔化 | `material_melting` `melting_fuel` `entity_melting` |
| 材料与部件 | `material` `part_builder` `item_part_builder` `tool_building` `part_builder_recycling` `part_builder_tool_recycling` |
| 过载 | `overslime_modifier` `crafting_overslime_modifier` |
| 回收与修复 | `modifier_salvage` `modifier_repair` `crafting_modifier_repair` |
| 工作台 | `remove_modifier` `extract_modifier` `modifier_sorting` `modifier_set_worktable` `enchantment_converting` `toggle_interaction` |
| 工匠站 | `tinker_station_damaging` `tinker_station_part_swapping` `tool_material_swapping` `fixed_material_swapping` `part_swapping_override` `tipped_tool_transform` |
| 斩首 | `severing` `ageable_severing` `player_beheading` `snow_golem_beheading` `mooshroom_demushrooming` `sheep_shearing` |

写之前**逐个**查 `tconstruct-recipe-reference.md` 的字段表和 `TinkerRecipeSchemas.java` 的键定义，不要凭印象写字段名。

---

## 5. 内容要求（最低数量，越丰富越好）

目标是一套"小型魔改包"：主题自定（例如"秘银与龙骨"），但内容要能串起来，让用户从挖矿、冶炼一路做到成品工具、护甲和词条。

| 模块 | 最低要求 |
|---|---|
| 新材料 | ≥ 8 种，合计用到第 4.3 节**全部** 23 种匠魂属性方法。至少：1 种 `hidden(true)`；4 种稀有度各一次；1 种 `luminosity` 发光；2 种带 `.generator` + `.generatorPalette`；有材料覆盖 `fallbacks` 的全部 13 种取值中的至少 6 种；每种材料都有中英文 `name`、`flavor`、`encyclopedia`，部分带 `textColor` |
| 特性 | 每种新材料有 `.trait`；至少 4 次 `.statTrait`，覆盖 4 个分组 ID，另加 2 次针对具体属性类型 |
| `extend` | 给 ≥ 3 种匠魂已有材料追加属性（包括新属性类型的 `customStat`）和 `statTrait` |
| 材料变体 | ≥ 2 个 `#变体`（`assets` 事件） |
| 材料标签 | ≥ 2 个，用到 `add` `addAll` `addOptional` `replace` |
| 新属性类型 | ≥ 3 种，合计用到 `base` `add` `percent` `multiply` 四种运算，`textureLike` 至少用 2 种不同取值 |
| 新部件 | ≥ 6 种：至少 3 种用新属性类型、至少 2 种用匠魂已有属性类型；至少 3 种 `.casts()`、至少 1 种不带铸模；`cost` 取值不同 |
| 新工具（`tool`） | ≥ 5 把，覆盖 `boxAoe`（含 `boxAoeExpansion` 和 3 种 `boxAoeDirection`）、`circleAoe`、`veinAoe`、`treeAoe`、`effective`、`toolActions`、`module`；至少 1 把 5 个部件（必须写 `.layoutSlot`）；至少 1 把用 `.partTexture` 复用匠魂底图 |
| 新剑（`sword`） | ≥ 2 把，用到 `sweepMelee`、`circleMelee` |
| 弓 / 弩 | 弓 ≥ 1、弩 ≥ 1，都写 `.pullFrames` 和 `.brokenPart` |
| 护甲 | ≥ 2 个护甲套，每套 4 件；合计用到 `materialLayer` `fixedLayer` `dyedLayer` `trim`；至少 1 套用 `.bookArmor(...)` 和 `.equipSound(...)` |
| 工具数值 | 所有工具、弓弩、护甲都写 `ArsenalToolEvents.data`；合计用到第 4.7 节全部方法；`.modifiable` 覆盖 `small` 和 `broad`，让工具进手册；每把工具写中英文 `bookText` 和 `bookProperty` |
| 新词条 | ≥ 8 个，合计用到第 4.8 节全部方法和 8 种词条事件；每个都要有获得方式（词条配方）。`ArsenalTool.getInt/setInt` 至少用一次做"击杀计数"之类的持久数据，`ArsenalTool.rebuild` 至少用一次 |
| 全局工具属性 | `ArsenalToolEvents.stats` 至少 2 条规则（按标签、按物品 ID 各一） |
| `ArsenalTool` 查询 | 在 KubeJS 自带事件（如 `EntityEvents.hurt`、`ItemEvents.rightClicked`）里用到 `isTool` `hasModifier` `modifierLevel` `armorModifierLevel` `projectileModifierLevel` |
| 配方 | 66 种配方**每种至少 1 个**；`ArsenalRecipes.metal` 至少 1 次；有用到 `ArsenalFluids` 常量。新材料要有完整的获得链：物品 → 材料（`material`）、熔化（`material_melting`）、浇铸（`material_fluid`），金属材料还要有合金或 `metal` 批量配方 |
| 新物品 | 用 KubeJS 自带注册新增材料用的锭、宝石等物品（以及需要的流体），让新材料有原料可用 |

如果某个配方类型确实无法与新内容关联（例如斩首类），可以写针对原版生物或匠魂物品的示例，但必须写出来。

---

## 6. 贴图（必须由你生成）

缺贴图只会显示紫黑方块，不会报错，所以每张都要生成。用 Python + Pillow（已安装 9.4.0）写一个生成脚本，放在 `D:\AI临时文件\showcase_textures\`，输出到 `run/kubejs/assets/kubejs/textures/`。

### 6.1 规格

- 尺寸 16×16（护甲穿戴贴图除外，见下表），PNG，透明背景，像素画风格（不要平滑缩放）。
- **部件和工具底图必须是灰度**：匠魂按灰度值映射到材料调色板，彩色底图会着色错误。
- 推荐做法：从匠魂 jar 副本复制同类灰度底图，再改形状。这只是本地测试包，可以直接改用匠魂素材。素材在 `D:\AI临时文件\tc_jar_8809959\assets\tconstruct\textures\` 下：
  - 部件和工具图层底图：`item\tool\<工具>\<图层>.png`，例如镐头部件用的就是 `item\tool\pickaxe\head.png`。只取不带材料后缀的那张，带 `_tconstruct_xxx` 的是已着色版本。
  - 图案图标：`gui\tinker_pattern\`。
  - 铸模：`item\cast\`、`item\sand_cast\`、`item\red_sand_cast\`。
  - 护甲穿戴贴图：`tinker_armor\plate\`，尺寸 64×32。

### 6.2 需要的文件（以 `kubejs:demo_xxx` 为例，路径相对 `assets/kubejs/textures/`）

| 用途 | 路径 |
|---|---|
| 部件底图（灰度） | `item/part/demo_xxx.png` |
| 部件制作台图案图标 | `gui/tinker_pattern/demo_xxx.png` |
| 铸模（写了 `.casts()` 才需要） | `item/cast/demo_xxx.png`、`item/sand_cast/demo_xxx.png`、`item/red_sand_cast/demo_xxx.png` |
| 工具每个图层底图（灰度） | `item/tool/demo_<工具>/<图层名>.png` |
| 损坏图层 | `item/tool/demo_<工具>/<图层名>_broken.png` |
| 弓弩拉弓帧 | `item/tool/demo_<弓>/<图层名>_1.png`、`_2.png`、`_3.png` |
| 护甲物品图层 | `item/tool/demo_<护甲>/<图层名>.png` |
| 护甲穿戴贴图 | `tinker_armor/demo_<套>/<图层名>_armor.png`（64×32）、`<图层名>_leggings.png`（64×32） |
| 护甲套工作台图标 | `gui/tinker_pattern/demo_<套>.png` |
| 词条图标（可选） | `item/demo_<名字>.png`，写成 `kubejs:item/demo_<名字>` |
| 新增原料物品 | `item/demo_<物品>.png`（彩色即可） |

用了 `.partTexture` 指向匠魂底图的图层，就不用再画这张。护甲穿戴贴图只画底图即可，各材料会按颜色着色，这是匠魂本身的回退逻辑。

---

## 7. Rhino 与 KubeJS 写法陷阱（实测踩过）

- 回调里块级作用域的变量用 `let`；在同一事件回调里重复声明 `const` 会报 "redeclaration of var"。
- 不要把 `global.xxx` 赋值为 `null`，会抛异常。
- Java 字符串的 `.split('.')` 是正则，先 `String(x)` 转成 JS 字符串。
- 读物品 NBT 用 `stack.nbt`，不要用 `getTag()` 或 `hasTag()`。
- 同名方法、参数个数相同的重载可能选错，必要时把参数显式转换类型。
- 生成工具成品可以用匠魂的 `ToolBuildHandler.buildItemFromMaterials`（已实测可用）：

```js
// 按材料列表生成一把成品工具
const ToolBuildHandler = Java.loadClass('slimeknights.tconstruct.library.tools.helper.ToolBuildHandler')
const MaterialNBT = Java.loadClass('slimeknights.tconstruct.library.tools.nbt.MaterialNBT')
const ListTag = Java.loadClass('net.minecraft.nbt.ListTag')
const StringTag = Java.loadClass('net.minecraft.nbt.StringTag')
const list = new ListTag()
list.add(StringTag.valueOf('tconstruct:iron'))
list.add(StringTag.valueOf('tconstruct:wood'))
const stack = ToolBuildHandler.buildItemFromMaterials(Item.of('kubejs:demo_xxx').getItem(), MaterialNBT.readFromNBT(list))
```

材料数量必须等于工具的部件数量（弓的 `limb` 算两个）。

---

## 8. 测试辅助命令

在 `server_scripts/showcase/` 里写 `ServerEvents.customCommand`，用户在游戏里执行 `/kubejs custom_command <名字>`：

| 命令 | 作用 |
|---|---|
| `demo_kit` | 清空背包，给匠魂工作台（部件制作台、工匠站、匠魂砧、冶炼炉核心组件或 `tconstruct:smeltery_controller` 等）、所有新原料物品、空白图案和铸模 |
| `demo_tools` | 每把新工具、剑、弓、弩、护甲各给一件成品，材料用新材料；背包放不下就分两条命令 |
| `demo_modifiers` | 给手持工具加上全部新词条，可以用 `/tconstruct modifiers @s add <id> <等级>` |

命令里循环同一段逻辑时，用数组加 `forEach`，不要写只调用一次的函数。

---

## 9. 检查清单格式（`run/kubejs/showcase-checklist.md`）

按用户进游戏后的操作顺序写，每一项一行：

| 序号 | 功能 | 操作 | 应该看到 | 对应脚本 |
|---|---|---|---|---|

必须包含的检查项：
- 创造栏和 JEI 里的新物品、部件、铸模、工具（匠魂"工具""工具部件""冶炼"创造栏）；
- 材料提示和颜色、发光材料、变体外观、新属性类型的数值显示；
- 部件制作台做新部件、浇铸新部件、做铸模；
- 工匠站和匠魂砧组装每把工具（写明哪些必须用匠魂砧）；
- 范围挖掘的形状、剑的横扫；
- 弓的拉弓三帧、弩装填和发射、损坏贴图；
- 护甲穿戴的正面和背面、染色、纹饰；
- 每个词条的获得方式和效果，每个词条事件怎么触发；
- 手册：工具页、护甲页、新属性类型的材料章节（《百科全书》目录末尾）；
- 每类配方在哪个方块或 JEI 页签里看。

每一项都写明如果没看到效果，可能是哪类原因（缺贴图、需要 `/reload`、需要重启游戏、需要用匠魂砧等）。

---

## 10. 已知限制（不要当成脚本 bug 去绕）

- 新材料穿在护甲上只是按颜色着色，没有专门的穿戴纹理。
- 词条事件只对本模组定义的词条触发；匠魂自带词条请配合 KubeJS 自带事件和 `ArsenalTool` 判断。
- `toolStats`、`attributes`、`ArsenalToolEvents.stats` 在工具重建时计算。改脚本后，已有工具要重新组装、加词条或调用 `ArsenalTool.rebuild` 才会更新。
- 原版锻造台不接受匠魂护甲，加纹饰要在工匠站。
- 新属性类型的数值不会出现在匠魂原有的近战、远程等材料页里，只出现在它自己的手册章节、物品提示和工匠站中。

---

## 11. 完成标准

1. 第 5 节每一行的数量和方法覆盖都满足。写完后用脚本统计实际用到的方法和配方类型，与第 4 节逐项比对，把比对结果附在检查清单末尾。不要只写"已全部覆盖"。
2. 第 6.2 节需要的每张贴图都存在：用脚本按脚本里注册的 ID 列出应有路径，检查文件存在并且尺寸正确，结果附在检查清单末尾。
3. 所有文件都是 UTF-8 编码，注释用中文，没有只调用一次的 function。
4. 你不能运行游戏，不要声称"已测试通过"，只写"待用户进游戏检查"。
