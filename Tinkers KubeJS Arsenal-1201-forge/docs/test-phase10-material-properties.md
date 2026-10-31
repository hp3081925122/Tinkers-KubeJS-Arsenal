# 交接文档：第十期测试（材料三围、属性公式、部件公式）

## 0. 背景与约束

第十期新增功能分在两个项目里：

| 项目 | 路径 | 负责内容 | 提交 |
|---|---|---|---|
| HPTlib 1.1.0 | `E:/模组/发cf的模组/AAA-Tinkers/HPTlib/HPTlib-1201-forge` | 数据包格式、计算、注入匠魂、客户端同步、部件提示框 | 见该仓库 `git log` |
| 本模组 | `E:/模组/发cf的模组/AAA-Tinkers/Tinkers KubeJS Arsenal/Tinkers KubeJS Arsenal-1201-forge` | KubeJS 写法、JSON 生成、ProbeJS | 见本仓库 `git log` |

- **设计文档：** `docs/plan-phase10-material-properties.md`，第 8、9 节是实现记录。
- **数据格式：**
  - 中文说明：Wiki 页 `D:/AI临时文件/tka-wiki/Material-Properties-zh.md`；
  - 完整格式：HPTlib 的 `README.md`。
- **约束**（沿用 `docs/handoff-phase8b.md` 第 0 节）：
  - 回复和文档用中文；Java 不写注释；调试日志用英文；提交不带 AI 署名。
  - 临时文件放 `D:\AI临时文件`。
  - KubeJS 禁止写只调用一次的 function。
  - 使用 API 前先确认（用 javap 或查源码）。
- **匠魂源码：**
  - 3.12 反编译代码：`D:/AI临时文件/tcon-3.12-decomp/`（只反编译了 `library.materials` 和 `library.tools.definition.module.material` 两个包）。
  - 其余类直接对 jar 用 javap，jar 路径为 `C:/Users/30819/.gradle/caches/forge_gradle/deobf_dependencies/curse/maven/tinkers-construct-74072/8809959_mapped_official_1.20.1/`。
- **构建：**
  - 本模组用 Composite Build 关联本地 HPTlib，在本模组目录执行 `gradlew runClient` 或 `runServer` 时会自动带上 HPTlib 源码。
  - 网络证书报错时加参数：`--offline -Dnet.minecraftforge.gradle.check.certs=false`。
  - JDK 路径：`JAVA_HOME=C:\Users\30819\.jdks\ms-17.0.18`。
- **证据要求：** 每项都要拿到**实际结果**，例如日志行、打印出的数值、`/data get` 的输出、截图。"加载没报错""未崩溃"都不能作为通过依据。

## 1. 验证手段

### A. KubeJS 脚本断言（逻辑类，优先用）

在 `run/kubejs/server_scripts/` 下写 `tkja_verify_phase10.js`：
- **触发方式：** 用 `ServerEvents.customCommand('tkja_p10')` 触发，执行 `/kubejs custom_command tkja_p10`；
- **输出格式：** 统一为 `[TKJA-VERIFY] <编号> <PASS/FAIL> <实际值> <预期>`；
- **写法要求：** 断言直接内联，不要包成函数。

可用的 API。以下几个已经用 javap 确认过签名，其余的用之前先确认：

| 用途 | API |
|---|---|
| 材料三围 | `Java.loadClass('org.hp.hptlib.material.HptMaterialProperties').get(new MaterialId(id))`，返回 `MaterialProperties`，可调 `hardness()`、`density()`、`toughness()`、`exclude()`；没有三围时返回 `null` |
| 部件公式 | `HptMaterialProperties.getPartFormula(new ResourceLocation('tconstruct:pick_head'))` |
| 材料某属性类型的数值 | `MaterialRegistry.getInstance().getMaterialStats(MaterialId, MaterialStatsId)`，返回 `Optional`。头部属性的访问器为 `durability()`、`attack()`、`miningSpeed()`、`tier()`；手柄属性的访问器名要先 javap `HandleMaterialStats` 确认 |
| 材料特性 | `MaterialRegistry.getInstance().getTraits(MaterialId, MaterialStatsId)`、`getDefaultTraits(MaterialId)` |
| 按材料造工具 | `ToolBuildHandler.buildItemFromMaterials(IModifiable, MaterialNBT)`、`MaterialNBT.of(IMaterial...)`、`MaterialRegistry.getMaterial(MaterialId)` |
| 读工具属性和词条 | `ToolStack.from(stack).getStats().get(ToolStats.ATTACK_DAMAGE)`、`ToolStats.MINING_SPEED`、`ToolStack.getModifierLevel(ModifierId)` |

类名：
- `MaterialId`：`slimeknights.tconstruct.library.materials.definition.MaterialId`
- `MaterialStatsId`：`slimeknights.tconstruct.library.materials.stats.MaterialStatsId`
- `MaterialRegistry`：`slimeknights.tconstruct.library.materials.MaterialRegistry`

### B. 游戏交互 MCP（画面类）

按 `docs/handoff-test-supplement.md` 第 1 节 B 和 skill `minecraft-mod-testing` 操作：
- 先确认 `run/options.txt` 中 `pauseOnLostFocus:false`；
- 后台启动 `runClient`；
- 截图用 `screenshot_to_file`。

MCP 连不上或操作失败时，把这一项标为"未测"并写明原因，**不要标通过**。

### C. 命令读 NBT

```
/data get entity @p SelectedItem.tag.tic_stats
/data get entity @p SelectedItem.tag.tic_modifiers
```

匠魂的工具属性和词条存在这两个 NBT 里，在服务端读出的就是最终结果。

## 2. 测试素材

### 2.1 数据包（场景 D 使用）

在测试世界里建 `datapacks/hpt_props_test/`，文件如下。

`pack.mcmeta`
```json
{ "pack": { "pack_format": 15, "description": "HPTlib material properties test" } }
```

**材料定义**：`data/hpt_test/tinkering/materials/definition/` 下建 `mythril.json`、`partial.json`、`excluded.json` 三个文件，内容相同：
```json
{ "craftable": true, "hidden": false, "sortOrder": 100, "tier": 1 }
```

**三围**：`data/hpt_test/tinkering/material_properties/` 下建三个文件。
- `mythril.json`：`{ "hardness": 4, "density": 5, "toughness": 9 }`
- `partial.json`：`{ "hardness": 4, "density": 5, "toughness": 9 }`
- `excluded.json`：`{ "hardness": 4, "density": 5, "toughness": 9, "exclude": ["tconstruct:handle"] }`

**partial 手写的头部属性**：`data/hpt_test/tinkering/materials/stats/partial.json`
```json
{ "stats": { "tconstruct:head": { "melee_attack": 10 } } }
```

**给匠魂的铁加三围**：`data/tconstruct/tinkering/material_properties/iron.json`
```json
{ "hardness": 6, "density": 7, "toughness": 5 }
```

**属性公式**：`data/tconstruct/tinkering/stat_formulas/` 下建三个文件。

`head.json`
```json
{
  "fields": {
    "melee_attack": { "hardness": 1 },
    "durability": { "base": 100, "toughness": 50, "round": "nearest" },
    "mining_speed": { "base": 2, "hardness": 0.5, "density": -0.2, "min": 1 },
    "mining_tier": {
      "score": { "hardness": 1 },
      "levels": [[0, "minecraft:wood"], [3, "minecraft:stone"], [6, "minecraft:iron"], [9, "minecraft:diamond"]]
    }
  },
  "traits": [
    { "modifier": "tconstruct:reinforced", "level": { "toughness": 0.25 }, "max": 5 }
  ]
}
```

`handle.json`
```json
{
  "fields": {
    "durability": { "toughness": 0.01 },
    "melee_damage": { "hardness": 0.02 },
    "mining_speed": { "density": -0.01 },
    "melee_speed": { "base": 0 }
  }
}
```

`binding.json`
```json
{ "fields": {} }
```

**部件公式**：`data/tconstruct/tinkering/part_formulas/pick_head.json`
```json
{
  "stats": [
    { "stat": "tconstruct:attack_damage", "op": "add", "value": { "hardness": 0.5 } },
    { "stat": "tconstruct:mining_speed", "op": "percent", "value": { "density": -0.02, "min": -0.5 } }
  ],
  "traits": [
    { "modifier": "tconstruct:lustrous", "level": { "hardness": 0.5 }, "max": 2 }
  ]
}
```

### 2.2 KubeJS 脚本（场景 K 使用）

`run/kubejs/server_scripts/tkja_props_test.js`。效果与 2.1 的数据包等价，只是材料命名空间换成 `kubejs`：

```js
ArsenalMaterialEvents.data(event => {
  event.create('kubejs:mythril_k').properties(4, 5, 9).craftable(true).color('#7FD4FF')
  event.create('kubejs:excluded_k').properties({ hardness: 4, density: 5, toughness: 9 }).excludeFormula('handle').craftable(true)
  event.extend('tconstruct:iron').properties(6, 7, 5)

  event.statFormula('head')
    .field('melee_attack', { hardness: 1 })
    .field('durability', { base: 100, toughness: 50 }, 'nearest')
    .field('mining_speed', { base: 2, hardness: 0.5, density: -0.2, min: 1 })
    .tiers({ hardness: 1 }, [[0, 'minecraft:wood'], [3, 'minecraft:stone'], [6, 'minecraft:iron'], [9, 'minecraft:diamond']])
    .trait('tconstruct:reinforced', { toughness: 0.25 }, 5)
  event.statFormula('handle')
    .field('durability', { toughness: 0.01 })
    .field('melee_damage', { hardness: 0.02 })
    .field('mining_speed', { density: -0.01 })
    .field('melee_speed', { base: 0 })
  event.statFormula('binding')

  event.partFormula('tconstruct:pick_head')
    .stat('attack_damage', { hardness: 0.5 })
    .stat('mining_speed', 'percent', { density: -0.02, min: -0.5 })
    .trait('tconstruct:lustrous', { hardness: 0.5 }, 2)
})
```

报错用例另建一个文件 `run/kubejs/server_scripts/tkja_props_errors.js`。每个用例放在独立的 handler 里，这样一个用例报错不会影响其他用例：

```js
ArsenalMaterialEvents.data(event => {
  event.statFormula('limb').field('durability', { hardness: 1 }, 'bogus')
})
ArsenalMaterialEvents.data(event => {
  event.statFormula('grip').field('accuracy', { hardness: 'x' })
})
ArsenalMaterialEvents.data(event => {
  event.partFormula('tconstruct:tool_handle').stat('harvest_tier', { hardness: 1 })
})
ArsenalMaterialEvents.data(event => {
  event.partFormula('tconstruct:tool_binding').trait('tconstruct:reinforced', { hardness: 1 }, 0)
})
ArsenalMaterialEvents.data(event => {
  event.create('kubejs:bad_props').properties({ hardness: 'hard' })
})
```

## 3. 预期值（手算）

三围：mythril 为 4 / 5 / 9；铁为 6 / 7 / 5。

| 材料 | 属性类型 | 预期 |
|---|---|---|
| mythril | head | `melee_attack` 4；`durability` 550；`mining_speed` 3（2 + 2 − 1）；`mining_tier` `minecraft:stone`（分数 4 ≥ 3） |
| mythril | handle | `durability` 0.09；`melee_damage` 0.08；`mining_speed` −0.05；`melee_speed` 0 |
| mythril | binding | 存在（无字段） |
| mythril | head 特性 | 只有 `tconstruct:reinforced` 2 级（9 × 0.25 = 2.25，向下取整） |
| partial | head | `melee_attack` 为 **10**（手写的值保留），其余字段同 mythril |
| excluded | handle | **不存在**（`getMaterialStats` 返回空）；head 和 binding 存在 |
| 铁 | head | 与匠魂原值一致：`durability` 250、`melee_attack` 2、`mining_speed` 6、`mining_tier` `minecraft:iron`（铁的这些字段都已手写，公式不覆盖） |
| 铁 | head 特性 | `tconstruct:magnetic` 1 级 + `tconstruct:reinforced` 1 级（5 × 0.25 = 1.25） |
| 铁 | 默认特性 | 仍是 `tconstruct:magnetic` 1 级 |
| 铁 | handle 特性 | `tconstruct:magnetic` 1 级（handle 没有特性公式，回退到默认特性） |

部件公式（`tconstruct:pick_head`）：
- **mythril**：攻击 add +2，挖掘速度 percent −0.1，光泽（lustrous）2 级（4 × 0.5 = 2）。
- **铁**：攻击 add +3，挖掘速度 percent −0.14，光泽 2 级（6 × 0.5 = 3，被上限 2 截住）。

镐子的工具定义（`tconstruct:pickaxe`）：
- 部件是 `pick_head`、`tool_handle`、`tool_binding`，没写部件倍率，所以倍率为 1。
- 开工前要读完整的 `data/tconstruct/tinkering/tool_definitions/pickaxe.json`，确认有没有影响攻击的 `multiply_stats`。有的话，攻击差值要按它换算。

## 4. 测试项目

### 场景 D：只用数据包（本模组开发环境，不放 2.2 的脚本）

先把 `run/kubejs/server_scripts/` 里第十期的测试脚本改名为 `.bak`。然后新建世界，放入 2.1 的数据包，执行 `/reload`。

| 编号 | 项目 | 手段 | 怎么测 | 通过标准 |
|---|---|---|---|---|
| D1 | 加载日志 | 日志 | 在 `run/logs/latest.log` 中搜 `Loaded material properties for` 和 `Applied stat formulas` | 显示 4 个材料、3 个属性公式、1 个部件公式；`Applied ... entries` 的数量 ≥ 1；没有 `Failed to load` |
| D2 | 三围读取 | A | `HptMaterialProperties.get` 读 mythril、excluded、铁 | 数值与 2.1 一致；excluded 的 `exclude()` 包含 `tconstruct:handle` |
| D3 | 属性计算 | A | 读第 3 节表中各材料的 head、handle、binding 数值 | 全部与第 3 节一致 |
| D4 | 手写优先 | A | 读 partial 和铁的 head | partial 的 `melee_attack` = 10；铁的 head 与匠魂原值一致 |
| D5 | 排除 | A | 读 excluded 的 handle | 返回空 |
| D6 | 特性合并 | A | 按第 3 节读铁的 head 特性、默认特性、handle 特性，以及 mythril 的 head 特性 | 与第 3 节一致；铁的默认特性 magnetic 仍在 |
| D7 | 部件可制作 | A | `MaterialStatsId('tconstruct:head').canUseMaterial(...)` 对 mythril 和 excluded 测试；`tconstruct:handle` 对 excluded 测试（先 javap 确认该方法签名） | head：两个都为 true；handle 对 excluded 为 false |
| D8 | 部件加成：属性 | A + C | 用 `buildItemFromMaterials(pickaxe, MaterialNBT.of(mythril, mythril, mythril))` 造镐 A，读攻击和挖掘速度。然后把 `pick_head.json` 改名为 `.bak`，`/reload` 后再造镐 B 读同样的值。测完把文件改回原名 | 攻击：A − B = 2 × 部件倍率（按第 3 节换算）。挖掘速度：A 与 B 的关系符合匠魂 `percent` 的语义，即 −10%（报告里写出所查到的匠魂 `percent` 实现和推算过程）。用 C 读 `tic_stats` 作为第二份证据 |
| D9 | 部件加成：特性 | A + C | 读镐 A、镐 B 的 `getModifierLevel(tconstruct:lustrous)` 和 `getModifierLevel(tconstruct:reinforced)` | A：lustrous 为 2，reinforced 为 2；B：lustrous 为 0，reinforced 为 2 |
| D10 | 只对对应部件生效 | A | 用同样的 mythril 材料造一把宽斧（`tconstruct:broad_axe`）或手斧（`tconstruct:hand_axe`，部件不含 `pick_head`），读 lustrous 等级 | lustrous 为 0 |
| D11 | 没有三围的材料不加成 | A | 用木头（`tconstruct:wood`，没有三围）作为镐头材料造镐，在有和没有 `pick_head.json` 两种情况下读攻击 | 两次攻击相同 |
| D12 | 部件提示框 | B | 执行 `/give @p tconstruct:pick_head{Material:"hpt_test:mythril"}` 和 `/give @p tconstruct:tool_handle{Material:"hpt_test:mythril"}`，悬停截图（中文、英文各一张） | 镐头提示框有"部件加成："，下面是攻击 +2、挖掘速度 −10%、光泽 II；两个提示框都**不显示**三围；手柄提示框没有部件加成 |
| D13 | 工具提示框与工匠站预览 | B | 在工匠站用 mythril 部件组装镐子，截图预览结果；拿出成品后截图提示框 | 预览和成品的攻击、挖掘速度一致，都等于 D8 中镐 A 的值 |
| D14 | /reload 生效 | A | 把 `data/hpt_test/tinkering/material_properties/mythril.json` 的 hardness 改为 8，`/reload` 后重读 mythril 的 head | `melee_attack` 为 8；`mining_speed` 为 2 + 4 − 1 = 5；`mining_tier` 为 `minecraft:iron`。测完改回 4 |
| D15 | 已有工具重算 | C | D14 改完之后，把 D8 造出的镐 A 放进背包，退出并重新进入世界，再用 `/data get` 读它的攻击 | 重新进入后攻击按 hardness 8 重算（部件加成 +4，头部攻击 8）。报告中记录 `/reload` 之后、重进之前的值作为对照 |

### 场景 K：KubeJS 写法（本模组开发环境，新建世界，不放数据包）

把 2.2 的两个脚本放好，执行 `/reload`。

| 编号 | 项目 | 手段 | 怎么测 | 通过标准 |
|---|---|---|---|---|
| K1 | 等价性 | A | 对 `kubejs:mythril_k`、`kubejs:excluded_k`、铁，重复 D2 至 D9 的断言（材料 ID 换成 K 的） | 结果与场景 D 一致 |
| K2 | 报错用例 | 日志 | 在 `latest.log` 中搜 `tkja_props_errors.js` 的报错 | 5 个用例都有对应报错。报错原文分别包含：`unknown round mode 'bogus'`；`coefficient 'hardness' must be a number`；`harvest_tier is not a numeric Tinkers tool stat`；`max level must be at least 1`（前缀为 `Part formula tconstruct:tool_binding`）；`Material property 'hardness' must be a number` |
| K3 | 报错不污染 | A | 读 mythril_k 的 limb 和 grip | 日志中有 `Stat formula tconstruct:limb was not generated` 和 `Stat formula tconstruct:grip was not generated`；mythril_k 的 limb、grip 都不存在；head、handle 正常 |
| K4 | ProbeJS | 文件 | 执行 `/probejs dump`，在 `run/kubejs/probe/` 生成的 d.ts 中搜索 `partFormula`、`statFormula`、`excludeFormula` 及其参数类型 | `statFormula` 的参数有 `"head"` 等字面量；`properties` 有 `hardness?: number`；`.stat` 的第一个参数是工具属性特殊类型；`.field` 的取整参数是 `"none" \| "nearest" \| "floor" \| "ceil"` |

### 场景 S：专用服务端同步

在单人游戏里客户端和服务端是同一个 JVM，三围是共享的，所以同步只能在专用服务端上验证。

1. 在本模组目录后台运行 `gradlew runServer`。确认 `run/server.properties` 中 `online-mode=false`，并且服务端世界放入了 2.1 的数据包（服务端世界目录是 `run/world`）。
2. 另开一个进程运行 `gradlew runClient`，连接 `localhost`。两个进程共用 `run` 目录：如果有日志或锁文件冲突，把服务端的工作目录改到 `run-server`（在 `build.gradle` 的 `runs.server` 中加 `workingDirectory project.file('run-server')`，测完还原），并在报告里写明。

| 编号 | 项目 | 手段 | 怎么测 | 通过标准 |
|---|---|---|---|---|
| S1 | 同步包 | 日志 | 在客户端日志中搜 `Received synced material properties` | 显示 4 个材料、1 个部件公式 |
| S2 | 客户端提示框 | B | 客户端重复 D12 | 与 D12 一致 |
| S3 | 客户端重算一致 | B + C | 客户端拿着 mythril 镐，截图提示框中的攻击；服务端控制台执行 `data get entity <玩家名> SelectedItem.tag.tic_stats` | 客户端显示的攻击和服务端 NBT 中的攻击相同，并等于 D8 中镐 A 的值 |
| S4 | /reload 后同步 | 日志 + B | 服务端改 mythril 的 hardness 后执行 `reload` | 客户端日志再次出现 `Received synced`；部件提示框中的三围更新 |

### 场景 H：只装 HPTlib（不装 KubeJS 和本模组）

在 HPTlib 目录运行 `gradlew runClient`（只带匠魂、Mantle、JEI）。新建世界，放入 2.1 的数据包。

| 编号 | 项目 | 手段 | 怎么测 | 通过标准 |
|---|---|---|---|---|
| H1 | 不依赖 KubeJS | 日志 | 启动并进入世界 | 没有 `ClassNotFound` 或 KubeJS 相关报错；有 `Loaded material properties for` |
| H2 | 工具数值 | C | 执行 `/give @p tconstruct:pickaxe{tic_materials:["hpt_test:mythril","hpt_test:mythril","hpt_test:mythril"]}` 后读 `tic_stats` 和 `tic_modifiers` | 攻击与 D8 中镐 A 的值相同；`tic_modifiers` 中有 lustrous 2、reinforced 2 |
| H3 | 部件提示框 | B | 重复 D12 | 与 D12 一致 |

## 5. 发现问题时

- 先确认问题出在模组还是测试方法本身（API 用错、时机不对）。
- 如果是模组的问题：
  1. 看源码定位原因，加英文调试日志确认；
  2. 修复后对**被改的项目**执行 `gradlew build`；
  3. 复测这一项。
  - 每个修复单独提交，HPTlib 和本模组分开提交。
- 不要为了让测试通过去改第 3 节的预期值。如果预期确实写错了（比如匠魂的 `percent` 语义和这里写的不同），在报告里给出源码依据并说明理由。

## 6. 交付

- 新建 `docs/test-phase10-results.md`：每个编号写实际结果和证据（日志行、数值、截图文件名）。
  - 截图放 `docs/test-screenshots/phase10/`；体积大的放 `D:\AI临时文件`，并写明路径。
- 结果分三类：
  - 通过；
  - 失败（已修复，写提交号）；
  - 未测（写原因）。
- 列出新增的测试脚本、数据包和临时世界的路径。测试用的数据包和脚本不要提交进仓库。
- 测完还原所有临时改动：`build.gradle` 的工作目录、改名的 `.bak` 文件、改过的三围数值。
- 提交，但**不要推送**，由用户决定何时推送。
