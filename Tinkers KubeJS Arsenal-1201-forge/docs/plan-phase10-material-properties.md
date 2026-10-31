# 第十期开发文档：材料三围与属性公式

## 0. 背景与约束

- 目标：给匠魂材料定义硬度（hardness）、密度（density）、韧性（toughness）三项数值（下称"三围"），再给材料属性类型写线性公式，`/reload` 时自动算出材料在该属性类型下的数值和特性（trait）。
  - 例：材料三围为 4、5、9，`tconstruct:head` 的公式写"每点硬度 +1 攻击"，算出该材料的 `melee_attack` 为 4。
- 分工：
  - **HPTlib**（`E:/模组/发cf的模组/AAA-Tinkers/HPTlib/HPTlib-1201-forge`）负责数据包格式、读取、计算以及注入匠魂。它不依赖 KubeJS，其他附属或整合包作者只用数据包就能接入。
  - **本模组**只提供 KubeJS 写法：脚本生成 HPTlib 格式的 JSON，并写入 `DataPackEventJS`，和现有的材料、属性写法相同。
  - 本模组改为**强制依赖** HPTlib（`mods.toml` 中 `mandatory=true`，CF 上设为 requiredDependency）。
- 已定下的设计：
  - 公式只写在属性类型上，不写在部件上。
  - 公式只支持线性系数。
- 项目环境、用户规则、写法规范同 `docs/handoff-phase8b.md` 第 0 节：Java 不写注释，调试日志用英文，修改后只跑 build 或 compileJava。
- HPTlib 现状：
  - 依赖 Tinkers 3.11.2.166、Mantle 1.11.104，版本 1.0.0，已发布到 CF（项目 ID 1699319）。
  - 被以下两个附属通过 Composite Build 依赖：Tinkers of Cataclysm、Tinkers of Legendary Monsters。
  - 目前没有 Mixin 配置，也不是 git 仓库。

## 1. 范围

| 做 | 不做（后续阶段实现） |
|---|---|
| HPTlib：三围数据包、公式数据包、计算并注入匠魂材料属性与特性 | 在材料提示框、手册中显示三围（第二阶段） |
| HPTlib：数值字段线性公式，非数值字段（挖掘等级）分段取值 | 非线性公式、JS 函数公式 |
| HPTlib：特性等级线性公式 | 部件级公式 |
| 本模组：KubeJS 写三围和公式，ProbeJS 补全 | 用数据包定义新属性类型（见 2.3） |
| 本模组：构建依赖、`mods.toml`、CF 依赖改为 HPTlib | 给匠魂原有材料预置三围或公式 |

## 2. 匠魂实现（已确认）

对照的是本模组开发依赖 Tinkers 3.12.0.220 的反编译代码（`D:/AI临时文件/tcon-3.12-decomp/`）。两个 `finishLoad` 的签名另在 3.11.2.166 的 jar 上用 javap 核对过，两版一致。

### 2.1 材料属性加载

`MaterialStatsManager` 的处理流程：
- 它继承 `MergingJsonDataLoader<Map<ResourceLocation, JsonObject>>`，读取目录 `tinkering/materials/stats`。
- `parse` 会把同一材料、同一属性类型的 JSON **按字段合并**：后加载的字段覆盖先加载的字段，值为 `null` 的属性类型会被删除。
- `finishLoad(Map<ResourceLocation, Map<ResourceLocation, JsonObject>> map, ResourceManager manager)` 的 `map` 结构是"材料 ID → 属性类型 ID → 已合并的 JSON"。随后逐项调用 `getStatType(id).getLoadable().deserialize(json, ...)`，遇到未注册的属性类型会报错并跳过。
- 反序列化之后，结果通过 `UpdateMaterialStatsPacket` 同步到客户端。

内置属性类型的字段名（取自 `tinkering/materials/stats/iron.json`）：

| 属性类型 | 字段 |
|---|---|
| `tconstruct:head` | `durability`、`melee_attack`、`mining_speed`、`mining_tier`（方块等级 ID，如 `minecraft:iron`） |
| `tconstruct:handle` | `durability`、`melee_damage`、`melee_speed`、`mining_speed`（倍率增量） |
| `tconstruct:limb` | `durability`、`accuracy`、`draw_speed`、`velocity` |
| `tconstruct:grip` | `durability`、`accuracy`、`melee_damage` |
| `tconstruct:plating_*` | `durability`、`armor`，部分类型另有 `toughness`、`knockback_resistance` |
| `binding`、`bowstring`、`maille` 等 | 无字段（`{}`），只用来标记"该材料可以做这个部件" |

本模组自定义的属性类型使用 `StatTypeEventJS.field(name, ...)` 定义的字段名。

### 2.2 材料特性加载

`MaterialTraitsManager` 的处理流程：
- 读取目录 `tinkering/materials/traits`，JSON 格式为 `{"default": [...], "perStat": {"tconstruct:head": [...]}}`。
- `finishLoad(Map<ResourceLocation, MaterialTraits.Builder> map, ResourceManager manager)` 调用 `Builder.build(statTypeFallbacks)`。

`MaterialTraits.getTraits(statId)` 的规则是 `traitsPerStats.getOrDefault(statId, defaultTraits)`。也就是说，**只要某个属性类型写了 perStat，就完全不用 default**。另外，`build` 时会按 `statTypeFallbacks` 补全回退关系，例如各 `plating_*` 回退到 `tconstruct:armor`。

`MaterialTraits.Builder` 只有 `setDefaultTraits`、`setTraits` 两个公开方法。字段 `defaultTraits` 和 `traitsPerStats` 是私有的，读取需要用 `@Accessor`。

### 2.3 属性类型不能用数据包定义

属性类型是代码注册表，原因如下：
- 注册方式是 `MaterialRegistry.registerStatType(MaterialStatType)` 写入 `IdAwareComponentRegistry`，匠魂自己在构造时注册，本模组在 `FMLCommonSetupEvent` 注册。
- 客户端解码 `UpdateMaterialStatsPacket` 时，也要从这个注册表里取 `MaterialStatType` 来调用 `decode`。客户端不加载服务端数据包，所以属性类型必须在两端启动时就注册好。
- 部件是物品，部件对应哪个属性类型在物品注册时就固定了，同样只能在启动阶段确定。

结论：
- 属性类型继续用匠魂原有类型，或本模组的 `ArsenalMaterialEvents.statTypes`（startup 脚本）来定义。
- **三围和公式可以完全用数据包写**，对任何已注册的属性类型都有效。
- 如果以后 HPTlib 要让非 KubeJS 用户也能新增属性类型，可以在启动时读取 `config/hptlib/stat_types/*.json`，两端各自加载。这一项不在本期范围内。

## 3. 数据包格式（HPTlib）

### 3.1 材料三围

路径：写在匠魂材料属性文件 `data/<材料命名空间>/tinkering/materials/stats/<材料路径>.json` 的 `properties` 字段（与 `stats` 并列，见第 10 节）。多个数据包按优先级逐键合并。

```json
{
  "properties": {
    "hardness": 4,
    "density": 5,
    "toughness": 9,
    "exclude": ["tconstruct:limb"]
  },
  "stats": { ... }
}
```

- 三围都可以省略，省略的按 0 计算。
- 允许写其他数字键，作为自定义三围，公式里可以直接引用。
- `exclude`（可选）：列出的属性类型不按公式生成。用于"材料有三围，但不想让它能做某种部件"。
- 材料 ID 不存在时，生成的数据会被匠魂忽略，不报错。

### 3.2 属性类型公式

路径：`data/<属性类型命名空间>/tinkering/stat_formulas/<属性类型路径>.json`，例如 `data/tconstruct/tinkering/stat_formulas/head.json`。同一路径整份覆盖。

```json
{
  "fields": {
    "melee_attack": { "hardness": 1 },
    "durability": { "base": 100, "toughness": 50, "round": "nearest", "min": 1 },
    "mining_speed": { "base": 2, "hardness": 0.5, "density": -0.2 },
    "mining_tier": {
      "score": { "hardness": 1 },
      "levels": [[0, "minecraft:wood"], [3, "minecraft:stone"], [6, "minecraft:iron"], [9, "minecraft:diamond"], [12, "minecraft:netherite"]]
    }
  },
  "traits": [
    { "modifier": "tconstruct:reinforced", "level": { "toughness": 0.25 }, "max": 5 },
    { "modifier": "tconstruct:dense", "level": { "base": -2, "density": 0.5 } }
  ]
}
```

**数值字段：**

结果 = `base + Σ(系数 × 三围)`。

- `base` 和每个三围系数都可以省略，省略的按 0 计算。
- `min` / `max`（可选）：限制结果范围。
- `round`（可选）：取整方式，可选 `none`（默认）、`nearest`、`floor`、`ceil`。整数字段（如 `durability`）应写 `nearest`。

**分段字段：**

`score` 按数值字段的规则算出一个分数，然后在 `levels` 中取**不超过该分数的最大一档**，把对应值原样写入。主要用于挖掘等级。

**特性：**

`level` 按数值字段的规则计算后**向下取整**；结果小于 1 不添加该特性。`max`（可选）用于限制等级上限。

### 3.3 优先级与合并规则

**属性：**
- 只对"有三围、有公式、且没被 `exclude` 排除"的材料和属性类型组合生成数据。
- 合并按字段进行：材料 stats JSON 里已经写了的字段**保持不变**，公式只补没写的字段。
- 材料原本没有这个属性类型时，生成一份新的。

**特性：**
- 先确定基础列表，依次取第一个存在的：
  1. 该属性类型的 perStat；
  2. 回退属性类型的 perStat；
  3. default。
- 再把公式算出的特性追加到基础列表，写为该属性类型的 perStat。这样能避免一写 perStat，default 特性就消失（见 2.2）。
- 基础列表里已经有同一个词条时，按"等级取较大值"合并，不重复添加。

**手写数据仍可完全覆盖：** 想对某个材料完全手写某个属性类型，在材料的 stats 里写全该属性类型的所有字段，再在 `exclude` 里排除它即可。

## 4. 实现要点

### 4.1 HPTlib

**新包：** `org.hp.hptlib.material`。

**数据加载：**
- `MaterialPropertiesLoader` 与 `StatFormulaLoader`：继承 `SimpleJsonResourceReloadListener`，在 `AddReloadListenerEvent` 中注册，只在服务端运行。
- **加载顺序有问题**：重载监听器的 prepare 阶段是并行执行的，不能保证在匠魂的 `finishLoad` 之前加载完成。
- 因此注入时**不依赖上述监听器的结果**。直接在 Mixin 里用 `finishLoad` 的 `ResourceManager` 参数调用 `manager.listResourceStacks("tinkering/materials/stats", ...)` 和 `manager.listResources("tinkering/stat_formulas", ...)` 自行读取。
- 两个文件夹的解析各只做一次：在 `MaterialStatsManager.finishLoad` 时读取，并缓存到一个静态快照中，供紧随其后的特性注入使用。
- 匠魂的这两个管理器注册顺序是 stats 在前、traits 在后。这一点开工时要确认；如果顺序不确定，就让两边各自读取一次。

**Mixin**（HPTlib 需新增 mixin 配置和 MixinGradle refmap，做法参照本模组 0.1.0 的修复）：
- `MaterialStatsManagerMixin`：`@Inject(method = "finishLoad", at = @At("HEAD"))`，按 3.3 的规则往 `map` 里补字段。
- `MaterialTraitsManagerMixin`：同样在 `finishLoad` 的 HEAD 注入，用 `@Shadow statTypeFallbacks` 取回退关系，按 3.3 的规则调用 `Builder.setTraits`。
- `MaterialTraitsBuilderAccessor`：`@Accessor` 读取 `defaultTraits`、`traitsPerStats`。

**计算与校验：**
- 计算和合并都在 JSON 层完成，生成的是普通 JSON。之后的反序列化、网络同步、提示框都由匠魂完成，客户端不需要任何改动。
- 公式引用的字段名不在该属性类型里时，记录一条英文 warn 日志（匠魂反序列化会忽略未知字段）。

**对外 API：**
- 提供 `HptMaterialProperties.get(MaterialId)`，返回只读的三围快照，供以后显示三围和本模组 ProbeJS 使用。

### 4.2 本模组（KubeJS）

**写法**（沿用 `ArsenalMaterialEvents.data`，链式加位置参数）：

```js
ArsenalMaterialEvents.data(event => {
  // 新材料带三围
  event.create('kubejs:mythril').properties(4, 5, 9)
  // 给已有材料补三围，并排除弓臂
  event.extend('tconstruct:iron').properties({ hardness: 6, density: 7, toughness: 5 }).excludeFormula('tconstruct:limb')

  // 属性类型公式
  event.statFormula('tconstruct:head')
    .field('melee_attack', { hardness: 1 })
    .field('durability', { base: 100, toughness: 50 }, 'nearest')
    .tiers({ hardness: 1 }, [[0, 'minecraft:wood'], [6, 'minecraft:iron'], [9, 'minecraft:diamond']])
    .trait('tconstruct:reinforced', { toughness: 0.25 }, 5)
})
```

**生成：** `ServerScriptManagerMixin` 已经负责向 `DataPackEventJS` 写入 JSON，本期增加写入：
- `tinkering/materials/stats/<材料>.json` 的 `properties` 字段
- `tinkering/stat_formulas/<属性类型>.json`

本模组不做任何计算，计算统一由 HPTlib 完成，保证 KubeJS 写法和数据包写法的结果一致。

**ProbeJS：**
- `statFormula` 的属性类型参数补全已注册的属性类型。
- `field` 的字段名补全按属性类型列出（内置类型用 2.1 的表；脚本类型从 `StatTypeEventJS.TYPES` 读取）。
- 三围键补全 `hardness`、`density`、`toughness`。

**依赖：**
- `settings.gradle` 加 `includeBuild('../../HPTlib/HPTlib-1201-forge')`。
- `build.gradle` 加 `implementation "org.hp.hptlib:hptlib:${hptlib_version}"`。
- `mods.toml` 加 `hptlib`：`mandatory=true`、`ordering="AFTER"`、`side="BOTH"`。
- 发布时 CF 依赖加 `hptlib`（1699319），类型为 requiredDependency。

### 4.3 文档

- 本模组 Wiki 新增"材料三围与公式"页（中英），并同步更新速查页。
- HPTlib README 补充数据包格式说明（即本文第 3 节），作为给其他作者的接入说明。

## 5. 分期建议

1. HPTlib：Mixin 配置和 refmap、数据包读取、计算、注入属性；`compileJava` 验证。
2. HPTlib：注入特性（含 perStat 和 default 合并）。
3. 本模组：接入 HPTlib 依赖、KubeJS 写法、JSON 生成、ProbeJS。
4. 文档：Wiki 和 HPTlib README。
5. 第二阶段（另开）：三围同步到客户端，在材料提示框和手册中显示。

## 6. 测试要点

（按用户规则，Claude Code 默认只做 build 验证；以下内容用于交给测试时参考。）

- 只写数据包、不装 KubeJS 时：
  - 有三围的材料能做出对应部件；
  - 部件提示框中的数值与公式手算结果一致。
- 材料 stats 已写 `melee_attack` 时，公式不覆盖该字段，其他字段由公式补全。
- `exclude` 生效：被排除的属性类型不生成数据。
- 材料原有 default 特性的情况下，公式添加 perStat 特性后，default 特性仍然保留。
- 护甲片：回退到 `tconstruct:armor` 的特性仍然保留。
- 对同一个材料，KubeJS 写法和等价的数据包写法生成的工具属性完全一致。
- `/reload` 后修改的数值立即生效，客户端提示框同步更新。
- 专用服务端：客户端不装数据包时，数值也能正常显示（检验通过匠魂同步包下发）。

## 7. 待确认问题（开工时先查清）

- **匠魂版本**：HPTlib 已与本模组统一使用 Tinkers 3.12.0.220 和 Mantle 1.11.117，避免 Composite Build 的严格依赖约束冲突。
- **重载顺序**：`MaterialStatsManager` 与 `MaterialTraitsManager` 在 `AddReloadListenerEvent` 中的先后顺序，决定能否共用 4.1 中的静态快照。
- **HPTlib 版本管理**：
  - HPTlib 目前不是 git 仓库，开工前建议先 `git init` 并提交现状。
  - 版本号为 1.1.0，本模组的 `hptlib_version_range` 只声明最低版本 `[0.0.1,)`。
- **HPTlib 的 `mods.toml`**：其中写了 JEI 可选依赖，与"可选依赖不写入 mods.toml"的规则冲突；是否顺便清理，需确认。

## 8. 实现记录（2026年10月31日）

- **待确认问题的结论：**
  - 重载顺序：匠魂在 `MaterialRegistry.addDataPackListeners` 中先注册 stats、后注册 traits，并且 Mantle 的 `MergingJsonDataLoader` 是同步执行的 `ResourceManagerReloadListener`，所以两边可以共用同一份快照（按同一个 `ResourceManager` 实例缓存）。
  - HPTlib 已 `git init` 并提交 1.0.0 的现状；版本升到 1.1.0；本模组的 `hptlib_version_range=[1.1.0,)`。
  - HPTlib 的匠魂依赖已升级到 3.12.0.220，Mantle 已升级到 1.11.117；JEI 可选依赖未清理。
- **HPTlib 新增：**
  - `org.hp.hptlib.material` 包：`HptMaterialProperties`、`MaterialProperties`、`StatFormula`、`FieldFormula`、`LinearFormula`。
  - `org.hp.hptlib.mixin` 包：`MaterialStatsManagerMixin`、`MaterialTraitsManagerMixin`、`MaterialTraitsBuilderAccessor`。
  - Mixin 都用了 `remap=false`，所以 refmap 为空，这是正常的。
  - 去掉了 `build finalizedBy reobfJar`：MixinGradle 已经把 reobfJar 挂到 assemble 上，保留它会形成循环依赖。
- **本模组新增：**
  - `MaterialDefinition` 增加 `properties(...)`、`excludeFormula(...)`。
  - `MaterialDataEventJS` 增加 `statFormula(...)` 和 `StatFormulaDefinition`，提供 `field`、`levels`、`tiers`、`trait` 方法。
  - `ServerScriptManagerMixin` 负责把这些内容写成两类数据包 JSON。
  - ProbeJS 新增以下参数的类型：`formulaStat`、`excludedStats`、`properties`、`coefficients`、`round`、`field`、`levels`。
- **验证：** 两个项目都执行了 `gradlew build`，均通过；按用户规则，未做游戏内测试。

## 9. 部件公式（追加）

部件公式是在属性类型公式之上，按具体部件额外加成。

**注入点：**
- 在 `MaterialStatsModule.addToolStats` 与 `addTraits` 的 TAIL 注入。
- 只处理实现了 `ToolPartsHook` 的模块（即 `PartStatsModule`）。
- 对每个下标 `i`，取出部件物品 `i` 和材料 `i`：
  - 部件物品有部件公式、且材料有三围时，才计算加成；
  - 数值乘以 `scales[i]`，再按 `base`/`add`/`percent`/`multiply` 写入 `ModifierStatsBuilder`；
  - 特性写入 `ModifierBuilder`。
- 3.11 与 3.12 中两个方法的签名一致，`scales` 字段都是包级可见的 `final float[]`。

**数据：**
- 路径为 `tinkering/part_formulas/<部件物品 ID>.json`，格式见 HPTlib README。
- 与三围在同一次 `load` 中读取（由 stats 管理器的 `finishLoad` 触发）。

**客户端同步：**
- 原因：`ToolStack.verifyTag` 在物品 NBT 加载时会调用 `rebuildStats`，客户端收到物品时也会重算，所以客户端必须有三围和部件公式。
- 做法：HPTlib 新增 `hptlib:material_properties` 通道，在 `OnDatapackSyncEvent` 时发送三围和部件公式的原始 JSON，客户端收到后重新解析。
- 副作用：已有工具不会在 `/reload` 后立刻更新，要等重新加载 NBT（重进世界、区块重载）时才会重算。

**本模组：**
- `event.partFormula(部件物品)`，可链式调用 `.stat(工具属性[, 运算方式], 系数)` 和 `.trait(词条, 系数[, 上限])`，生成部件公式 JSON。

**未做（后续阶段）：** 部件物品自身的提示框显示部件加成。

**验证：** 两个项目都执行了 `gradlew build`，均通过；未做游戏内测试。

## 10. 补充修改（2026年10月31日）

- **部件提示框：** HPTlib 新增 `MaterialPropertiesTooltip`，只在客户端注册，监听 `ItemTooltipEvent`。
  - 不显示三围数值（按用户要求已去掉三围行）。
  - 部件物品有部件公式时显示加成（按部件倍率 1）：特性并入匠魂词条区，同名词条等级相加；属性只在按住 Shift 时显示，同名属性未开 F3+H 显示合并后的最终值，开 F3+H 显示"原值 (加成)"；原本没有的属性未开 F3+H 直接显示数值，开 F3+H 用括号显示。不出现"部件加成"字样。
- **报错文案：** 部件公式里特性报错时，前缀改为 `Part formula <部件>`，之前误用了 `Stat formula`。
- **弱引用：** `HptMaterialProperties` 记录上次加载用的 `ResourceManager` 时改用 `WeakReference`，不再持有已关闭的资源管理器。
- **同步日志：** 客户端收到同步包时输出英文日志 `Received synced material properties ...`，供测试取证。
- **许可证：** 本模组的 `LICENSE.txt` 改为 MIT 文本，和 `mods.toml`、CF 声明一致。
- **HPTlib 依赖：** 删除 `mods.toml` 中的 JEI 可选依赖，依据是"可选依赖不写入 mods.toml"的规则。
- **示例修正：** 匠魂镐头部件的 ID 是 `tconstruct:pick_head`，Wiki 示例已改正。
- **测试文档：** `docs/test-phase10-material-properties.md`。
- **替换模式：** 三围可写 `"replace": true`，有公式且未排除的属性类型丢弃手写属性，只用公式结果；部件公式和属性类型的特性仍在原基础上叠加。目前只支持数据包写法，KubeJS `.properties()` 尚未支持。
- **三围并入属性文件：** 取消独立的 `tinkering/material_properties` 目录，三围改写在 `tinkering/materials/stats/<材料>.json` 的 `properties` 字段；匠魂用 Gson 解析时会忽略该字段。HPTlib 用 `listResourceStacks` 读取所有数据包的同名文件，`properties` 逐键合并。三围体系尚未发布，不保留旧写法的兼容。本模组 `ServerScriptManagerMixin` 生成材料和扩展时同样写入 `properties`。
