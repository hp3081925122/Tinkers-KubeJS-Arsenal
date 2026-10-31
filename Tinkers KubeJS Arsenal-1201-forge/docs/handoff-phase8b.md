# 交接文档：第八期补充（extend 加特性）与第六至八期游戏内测试

## 0. 背景与约束

- 项目：Tinkers KubeJS Arsenal，Minecraft 1.20.1 Forge 47.4.23，KubeJS 2001.6.5，匠魂 jar 版本 8809959（已解压在 `D:/AI临时文件/tc_jar_8809959/`，可用 `javap -p -c -cp .` 查字节码）。
- 仓库：`E:\模组\发cf的模组\AAA-Tinkers\Tinkers KubeJS Arsenal`，Gradle 工程在 `Tinkers KubeJS Arsenal-1201-forge`，分支 main，远程 https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal 。
- 最新提交：5206700（第八期）。本文档对应的两项工作都**还没开始改代码**。
- 用户规则（必须遵守）：
  - 中文回复；文档、注释只用中文，只用 UTF-8。
  - Java 代码不写注释。
  - KubeJS 脚本里禁止写只调用一次的 function。
  - 调试日志输出用英文。
  - git 提交信息不带任何 Claude / AI 署名（不加 Co-Authored-By，不加 Generated with）。
  - 临时文件、脚本放 `D:\AI临时文件`。
  - 使用任何类或方法前，先用源码或 javap 确认。
  - 修改后至少跑 `./gradlew compileJava` 验证。
  - 本次用户明确要求做游戏内测试。
- 写法规范见 `docs/kubejs-api-style.md`：JS 全局对象以 `Arsenal` 开头（S15）；服务端定义 API 用链式和位置参数，不用对象字面量（S16）。

## 1. 任务一：`extend` 放开 `.statTrait`

### 1.1 目标

第八期的 `event.extend('tconstruct:iron')` 目前只能追加属性（`.customStat`、`.head` 等）。需要让它也能追加按部件类型区分的特性：

```js
ArsenalMaterialEvents.data(event => {
  event.extend('tconstruct:iron')
    .customStat('kubejs:blade', 2, 0, 250)
    .statTrait('kubejs:blade', 'tconstruct:sharpness', 1)
})
```

### 1.2 已确认的匠魂合并规则（不用再查）

特性文件 `data/<ns>/tinkering/materials/traits/<材料>.json` 由 `MaterialTraitsManager`（继承 `MergingJsonDataLoader`）加载，多个数据包里的同一材料文件依次合并到 `MaterialTraits$Builder`：

| JSON 字段 | 合并方式（来自字节码） |
|---|---|
| `default` | `setDefaultTraits(list)`：后加载的文件**只要有这个字段就整体替换**；字段为 null 才保留原值 |
| `perStat.<属性类型>` | `setTraits(statId, list)`：按属性类型逐个 `put`，**同一属性类型整体替换**，其他属性类型不受影响；值为 null 时删除该属性类型 |

另外，匠魂的规则是：某类部件如果有 `perStat` 特性，就用它代替 `default`。所以给铁加 `perStat."kubejs:blade"` 后，铁做的 blade 部件只有这里写的特性，没有铁默认的"磁性"。这是匠魂本身的行为，写进文档说明即可。

### 1.3 结论与要做的改动

只放开 `.statTrait`，`.trait`（写 `default`）在 extend 上继续禁止，否则会把铁原有的"磁性"等默认特性整个替换掉。

文件：`src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/MaterialDataEventJS.java`

1. `statTrait(...)`（约 250 行）开头有 `this.requireFull("statTrait");`，删掉这一行。
2. `trait(...)` 两个重载保留 `requireFull`，但把报错说明改准确。可以在 `requireFull` 里对 `trait` 单独给出说明，例如：`.trait(...) is not available on extend(...) because it would replace the material's existing default traits; use .statTrait(...) instead`。
3. `traits()`（约 402 行）现在固定输出 `"default": this.defaultTraits`。extend 时**不能输出 `default` 字段**，哪怕是空数组也不行：空数组非 null，会把原材料默认特性清空。改成 extension 时只输出 `perStat`。
4. extend 给已有的 perStat 键（比如铁的 `tconstruct:armor`）写特性会替换原列表。至少要在 `docs/delivery-phase8.md` 里写明。可选：在 `ServerScriptManagerMixin` 写文件时用 `MaterialRegistry.getInstance().getTraits(materialId, statId)` 检查原材料是否已有该属性类型的特性，有就 `ConsoleJS.SERVER.warn(...)` 用英文提示会被替换。用这个方法前先 javap 确认签名；IMaterialRegistry 里有 `getTraits(MaterialId, MaterialStatsId)` 和 `hasUniqueTraits(...)`。注意：这时匠魂数据可能还是上一次加载的结果，首次进世界时可能为空，只能当提示用。

文件：`src/main/java/com/hp/tinkers_kubejs_arsenal/mixin/ServerScriptManagerMixin.java`

5. 第八期加的 extension 循环（搜 `data.getExtensions()`）目前只写 stats。补上：`if (extension.hasTraits()) add(pack, extension.id(), "traits", extension.traits());`，并设 `added = true`。`add(...)` 会写到 `<ns>/tinkering/materials/traits/<path>`。

文档：`docs/delivery-phase8.md`

6. `extend` 说明表加上 `.statTrait`；写明默认特性不能改的原因，以及 perStat 会替代默认特性、同属性类型会整体替换。
7. 删除"限制"里的"`extend` 不能给已有材料加特性词条，后续阶段实现。"

完成后 `./gradlew compileJava`，然后提交、推送（`git push` 直接可用）。

## 2. 任务二：游戏内测试第六至八期

### 2.1 为什么要测

第四到八期都只跑过编译。生成的配方、模型、工具定义的格式都是照抄匠魂 jar 里的数据，但从没在游戏里加载过。第七、八期又建立在第六期之上，所以要在开始弓弩、护甲之前，把整条链验证一遍。

### 2.2 测试环境

- 开发环境 `Tinkers KubeJS Arsenal-1201-forge/run/`，用户同意直接在本目录测。启动用 `./gradlew runClient`（后台运行，耗时长）。
- 已知干扰：开发依赖 Advanced Loot Info（`ali:jei_plugin`）在 `/reload` 时会卡 4 分钟以上，这不是本模组的问题。需要时可以临时从 `run/mods` 或依赖里去掉它，测完恢复。
- 可以用游戏交互 MCP（skill `minecraft-mcp`，jar 在 `E:\模组\我的世界mcp的jar`）。**注意**：这些 jar 是按正式环境编译的，放进 `run/mods` 会在进主界面时崩溃。所以开发环境里主要靠日志、命令和 KubeJS 脚本输出验证，用户也可以手动配合看画面。
- `run/options.txt` 里设置 `pauseOnLostFocus:false`。
- `run/kubejs/server_scripts/tkja_material_test.js` 是早期的材料测试脚本，可以保留。新建的测试脚本统一用 `tkja_` 前缀，测完告诉用户哪些文件是测试用的。

### 2.3 最小测试脚本

`run/kubejs/startup_scripts/tkja_chain_test.js`：

```js
ArsenalMaterialEvents.statTypes(event => {
  event.create('kubejs:blade')
    .field('sharpness', 'attack_damage', 'base', 0)
    .field('balance', 'attack_speed', 'percent', 0)
    .field('durability', 'durability', 'base', 1)
    .textureLike('head')
    .name('zh_cn', '刃身')
})

StartupEvents.registry('item', event => {
  event.create('kubejs:great_blade', 'tinkers_kubejs_arsenal:part')
    .statType('kubejs:blade')
    .cost(4)
    .casts()
    .displayName('Great Blade')
    .name('zh_cn', '巨刃')

  event.create('kubejs:war_axe', 'tinkers_kubejs_arsenal:tool')
    .part('blade', 'kubejs:great_blade')
    .part('handle', 'tconstruct:tool_handle')
    .part('binding', 'tconstruct:tool_binding')
    .brokenPart('blade')
    .displayName('War Axe')
    .name('zh_cn', '战斧')
    .bookText('zh_cn', '测试用战斧。')
    .bookProperty('zh_cn', '测试特性一')
})
```

`run/kubejs/server_scripts/tkja_chain_test.js`：

```js
ArsenalMaterialEvents.data(event => {
  event.extend('tconstruct:iron')
    .customStat('kubejs:blade', 3, 0.1, 300)
  event.extend('tconstruct:copper')
    .customStat('kubejs:blade', 2, 0.2, 200)
})

ArsenalToolEvents.data(event => {
  event.create('kubejs:war_axe')
    .baseStat('attack_damage', 3)
    .baseStat('attack_speed', 1.0)
    .slot('upgrades', 3)
    .slot('abilities', 1)
    .toolActions('axe_dig')
    .effective('minecraft:mineable/axe')
    .modifiable('melee/weapon', 'harvest/primary', 'small')
})
```

如果任务一已经完成，再在 extend 里加 `.statTrait('kubejs:blade', 'tconstruct:sharpness', 1)` 一起测。modifier ID 先查匠魂 `data/tconstruct/tinkering/modifiers/` 确认存在。

测试贴图：从 `D:/AI临时文件/tc_jar_8809959/assets/tconstruct/textures/` 复制现成的匠魂贴图到 `run/kubejs/assets/kubejs/textures/` 下对应路径即可，不需要画新图：

| 目标路径（`run/kubejs/assets/kubejs/textures/` 下） | 来源（匠魂 textures 下） |
|---|---|
| `item/part/great_blade.png` | `item/tool/broad_axe/blade.png` |
| `gui/tinker_pattern/great_blade.png` | `gui/tinker_pattern/broad_axe_head.png` |
| `item/cast/great_blade.png`、`item/sand_cast/great_blade.png`、`item/red_sand_cast/great_blade.png` | `item/cast/broad_axe_head.png`、`item/sand_cast/broad_axe_head.png`、`item/red_sand_cast/broad_axe_head.png` |
| `item/tool/war_axe/blade.png`、`blade_broken.png`、`handle.png`、`binding.png` | `item/tool/broad_axe/` 下的同名文件（均已确认存在） |

### 2.4 检查清单

先看 `run/logs/latest.log` 和 `run/logs/kubejs/startup.log`、`server.log`、`client.log`，搜 `ERROR`、`Exception`、`tinkers_kubejs_arsenal`、`kubejs:blade`、`kubejs:great_blade`、`kubejs:war_axe`。

| # | 检查项 | 预期 | 涉及代码 |
|---|---|---|---|
| 1 | 启动日志 | 有 `Registered material stat type kubejs:blade with 3 fields`，没有字段被忽略的 error | `ScriptMaterialStats.register` |
| 2 | 物品注册 | `kubejs:great_blade`、`great_blade_cast`、`great_blade_sand_cast`、`great_blade_red_sand_cast`、`kubejs:war_axe` 都存在（`/give` 能给出） | `ArsenalPartBuilder`、`ArsenalToolBuilder` |
| 3 | 进入世界 | 服务端日志没有 "unknown stat type"、材料属性解析错误、配方解析错误；客户端没有因网络解码失败断开 | `ScriptMaterialStats` 的 loadable 编解码 |
| 4 | 配方加载 | `/kubejs dump_registry` 不合适时，可在服务端脚本里用 `ServerEvents.recipes` 输出 `event.countRecipes({output: 'kubejs:great_blade'})`，或进 JEI 查看。预期：部件制作台、金/沙铸模浇铸、复合浇铸、7 个铸模配方都在 | `ArsenalPartBuilder.recipes()` |
| 5 | 标签 | `/kubejs hand` 或脚本查询：`kubejs:great_blade` 在 `tconstruct:parts`；铸模在 `tconstruct:casts/gold` 等；`kubejs:war_axe` 在 `tconstruct:modifiable/multipart`、`small` | `ServerScriptManagerMixin` itemTags |
| 6 | 材料属性 | 部件制作台里 great_blade 只能用铁、铜（以及写了 blade 属性的材料）制作；物品提示显示攻击伤害、攻速百分比、耐久三行 | `ScriptMaterialStats.getLocalizedInfo`、`extend` 写入 |
| 7 | 部件制作 | 部件制作台出现 great_blade 图案（图标、名称"巨刃"）；用 4 个铁锭做出铁制巨刃，模型按材料着色 | 图案贴图、`pattern.kubejs.great_blade` 语言键、`tconstruct:material` 模型 |
| 8 | 铸模 | 冶炼炉浇铸台：铁制巨刃 + 熔融金 → 金铸模；金铸模 + 熔融铁 → 铁制巨刃 | 铸模配方 |
| 9 | 工具组装 | 工匠台出现战斧按钮，3 个部件槽；组装出的战斧属性合理，名称、模型正常 | 第六期工具定义、布局、模型 |
| 10 | 损坏模型 | 用 `/data` 或命令把战斧耐久打到 0，模型切换为 broken 刃 | `brokenPart` |
| 11 | 回收 | 部件制作台放毒液瓶 + 战斧，能拆回部件 | 匠魂通用回收配方 |
| 12 | 创造栏 | 匠魂"工具部件"栏有巨刃的各材料变体；"冶炼"栏有三种铸模；"工具"栏有战斧变体；KubeJS 栏里没有这些无材料物品 | `ArsenalToolTabs` |
| 13 | JEI | 巨刃显示多个材料变体；配方页能看到部件制作和浇铸 | 匠魂 JEI 插件 |
| 14 | 手册 | 《材料与你》工具章能找到战斧，显示中文介绍和特性；铁的材料页"可制作部件"里有巨刃 | 第六期 bookText |
| 15 | /reload | 改服务端脚本里的数值后 `/reload`，属性变化生效，没有报错（注意 ALI 卡顿） | mixin 数据包 |
| 16 | 任务一 | 如果已完成：铁制巨刃有锋利特性；铁镐等原有工具仍有"磁性" | 任务一改动 |

### 2.5 发现问题时

- 先看日志和源码定位原因再改。调试时加英文日志输出，问题解决后删除。
- 改完跑 `compileJava`，再复测对应项。
- 修复按问题分别提交，提交信息写清楚修了哪一项。

### 2.6 交付

- 把测试结果表（每项通过、失败、修复情况）写进 `docs/test-phase6-8.md`。
- 列出新增的测试脚本、贴图文件，问用户要不要保留。
- 提交并推送。

## 3. 相关代码速查

| 功能 | 文件（`src/main/java/com/hp/tinkers_kubejs_arsenal/` 下） |
|---|---|
| 工具物品类型、模型、手册页 | `kubejs/tool/ArsenalToolBuilder.java` |
| 工具数值事件 | `kubejs/tool/ToolDataEventJS.java` |
| 部件、铸模、配方、标签 | `kubejs/tool/ArsenalPartBuilder.java` |
| 创造栏 | `kubejs/tool/ArsenalToolTabs.java` |
| 属性类型启动事件 | `kubejs/material/StatTypeEventJS.java` |
| 属性类型实现、注册 | `kubejs/material/ScriptMaterialStats.java` |
| 材料数据、customStat、extend | `kubejs/material/MaterialDataEventJS.java` |
| 服务端动态数据包写入 | `mixin/ServerScriptManagerMixin.java` |
| 运行时材料贴图生成 | `client/GeneratedMaterialSpriteSource.java` |
| 插件入口（物品类型、startup 事件、语言） | `kubejs/ArsenalKubeJSPlugin.java` |
| 交付文档 | `docs/delivery-phase6.md`、`delivery-phase7.md`、`delivery-phase8.md` |
