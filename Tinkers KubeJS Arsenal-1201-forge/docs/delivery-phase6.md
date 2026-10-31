# 第六期交付说明：KubeJS 自定义匠魂工具

## 完成内容

自定义工具分成两处写：

| 写在哪 | 内容 | 生效方式 |
|---|---|---|
| `startup_scripts` | 注册工具物品：工具类型、部件组成、贴图、名称 | 重启游戏 |
| `server_scripts` | `ArsenalToolEvents.data`：基础属性、属性倍率、词条槽、自带词条、工具动作、挖掘判定、范围挖掘、可改造标签、工作台布局 | `/reload` |

部件组成写在启动脚本，是因为部件决定模型有几层贴图，改了本来就要重启；数值写在服务端脚本，方便热调。

## 写法

### 启动脚本：注册工具

```js
StartupEvents.registry('item', event => {
  event.create('kubejs:war_pick', 'tinkers_kubejs_arsenal:tool')
    .part('head', 'tconstruct:pick_head')
    .part('handle', 'tconstruct:tool_handle')
    .part('binding', 'tconstruct:tool_binding')
    .brokenPart('head')
    .displayName('War Pick')
    .name('zh_cn', '战镐')

  event.create('kubejs:rapier', 'tinkers_kubejs_arsenal:sword')
    .part('blade', 'tconstruct:small_blade')
    .part('guard', 'tconstruct:tool_binding')
    .part('handle', 'tconstruct:tool_handle')
})
```

| 方法 | 说明 |
|---|---|
| 类型 `tinkers_kubejs_arsenal:tool` | 匠魂 `ModifiableItem`，普通工具 |
| 类型 `tinkers_kubejs_arsenal:sword` | 匠魂 `ModifiableSwordItem`，带横扫，能快速破坏蜘蛛网 |
| `.part(name, partItem)` | 按顺序添加部件，顺序就是部件序号 0、1、2…；`name` 是模型里的图层名，同一工具内不能重复；`partItem` 必须是匠魂部件物品 |
| `.partTexture(name, texture)` | 改某个部件的灰度底图路径，默认 `<命名空间>:item/tool/<工具路径>/<name>` |
| `.brokenPart(name)` | 声明这个部件有损坏贴图 `<底图>_broken`，工具耐久耗尽时换成它；不声明就不生成损坏模型 |
| `.name(lang, text)` | 其他语言的名称；英文名照常用 KubeJS 的 `.displayName(...)` |
| `.bookText(lang, text)` | 匠魂手册工具页的介绍文字，每调用一次是一段，可多次调用 |
| `.bookProperty(lang, text)` | 匠魂手册工具页"特性"列表的一条，可多次调用 |
| `.parentModel(...)` | KubeJS 自带方法，改模型的手持姿态；默认普通工具用 `tconstruct:item/base/axe`，剑用 `forge:item/default-tool` |

KubeJS 物品构建器自带的方法（`.tag`、`.rarity` 等）照常可用。工具固定不可堆叠。

**开发者需要画的贴图：** 每个部件一张灰度底图，放在 `kubejs/assets/kubejs/textures/item/tool/war_pick/head.png` 这类路径；声明了 `.brokenPart` 的部件再画一张 `head_broken.png`。每种材料的部件贴图由 4C 的运行时生成按灰度底图自动着色，匠魂自带的 97 种材料中有 86 种带生成配置，第四期用 KubeJS 定义并写了 `generator` 的材料也会生成。

词条在工具上的外观贴图可选，放在 `kubejs/assets/kubejs/textures/item/tool/war_pick/modifiers/` 下，文件名和匠魂一致；没有就不显示词条外观。

### 服务端脚本：工具数值

```js
ArsenalToolEvents.data(event => {
  event.create('kubejs:war_pick')
    .baseStat('attack_damage', 2)
    .baseStat('attack_speed', 1.0)
    .multiplyStat('durability', 1.2)
    .multiplyStat('mining_speed', 0.8)
    .slot('upgrades', 3)
    .slot('abilities', 1)
    .trait('tconstruct:pierce', 1)
    .toolActions('pickaxe_dig')
    .effective('minecraft:mineable/pickaxe')
    .boxAoeDirection('pitch')
    .boxAoeExpansion(0, 0, 2)
    .boxAoeExpansion(0, 1, 0)
    .modifiable('melee/weapon', 'harvest/primary', 'harvest/stone', 'aoe', 'small', 'interactable/right')
    .materialName(0)
})
```

| 方法 | 生成的匠魂模块或数据 |
|---|---|
| `.baseStat(toolStat, value)` | `tconstruct:base_stats`，可多次调用；属性 ID 不带命名空间时补 `tconstruct:` |
| `.multiplyStat(toolStat, value)` | `tconstruct:multiply_stats`，可多次调用 |
| `.slot(slotType, count)` | `tconstruct:modifier_slots`，`count` 不小于 0 |
| `.trait(modifier)` / `.trait(modifier, level)` | `tconstruct:traits` |
| `.toolActions(...)` | `tconstruct:tool_actions` |
| `.effective(blockTag)` | `tconstruct:is_effective`，用 `mantle:tag` 判断方块标签，可带或不带 `#` |
| `.boxAoe(width, height, depth)` | `tconstruct:box_aoe` 的基础范围（`bonus`），在 1 格之外额外扩展的格数 |
| `.boxAoeExpansion(width, height, depth)` | `tconstruct:box_aoe` 的 `expansions`，每级"扩展"词条按顺序轮流使用一条，可多次调用 |
| `.boxAoeDirection(direction)` | `expansion_direction`：`pitch`、`height`、`side_hit`，默认 `side_hit` |
| `.circleAoe(diameter, threeDimensional)` | `tconstruct:circle_aoe` |
| `.veinAoe(maxDistance)` | `tconstruct:vein_aoe` |
| `.treeAoe(widthBonus, depthBonus)` | `tconstruct:tree_aoe` |
| `.sweepMelee(range)` | `tconstruct:sweep_melee` |
| `.circleMelee(diameter)` | `tconstruct:circle_melee` |
| `.module(type, options)` | 任意匠魂工具模块，`options` 原样写入；专用方法覆盖不到时使用 |
| `.primaryPart(index)` | `part_stats` 的主部件，默认 0 |
| `.materialName(index)` | `tconstruct:fixed_material_name`，工具名称跟随第几个部件的材料 |
| `.modifiable(...)` | 加入 `tconstruct:modifiable/<路径>` 物品标签，决定能加哪类词条 |
| `.layoutSlot(index, x, y)` | 改工作台里某个部件槽的位置 |
| `.sortIndex(n)` | 工作台工具按钮的排序，默认 100 |

自动生成、不用写的部分：

- `tconstruct:part_stats`：部件列表取自启动脚本；
- `tconstruct:default_materials`：每个部件随机 1 级材料，用于创造模式和 JEI 展示；
- `tconstruct:modifiable/multipart` 和 `tconstruct:modifiable/durability` 标签：所有部件工具都需要；
- 工作台布局 `tinkering/station_layouts/<路径>.json`：1 到 4 个部件按匠魂预设位置摆放（4 个部件照阔斧），超过 4 个部件必须给多出的部件写 `.layoutSlot`；超过 3 个部件时匠魂会要求用铁砧组装；
- 组装配方 `recipes/tools/building/<路径>.json`（`tconstruct:tool_building`）。

## 匠魂创造栏与 JEI

- 匠魂"工具"创造栏：每把自定义工具按材料列出全部变体，做法和匠魂自带工具一样（`ToolBuildHandler.addVariants`）。
- KubeJS 创造栏：KubeJS 默认会把每个 KubeJS 物品的无材料版本放进自己的创造栏，这种工具没有材料、模型显示不正常，所以从 KubeJS 创造栏里移除。
- JEI：不需要单独接入。匠魂的 JEI 插件会给 `tconstruct:modifiable/multipart` 标签里的物品注册工具子类型，自定义工具已自动加入该标签；JEI 的物品列表取自创造栏内容，因此各材料变体会显示出来；生成的 `tconstruct:tool_building` 配方会出现在匠魂的"工具组装"配方页里。

## 匠魂手册

工具会不会出现在手册里、出现在哪一章，由服务端脚本的 `.modifiable(...)` 决定，和匠魂自带工具一样按标签收录：

| 标签 | 收录到 |
|---|---|
| `small`（单手小工具：镐、鹤嘴锄、手斧、匕首、剑等） | 《材料与你》、《百科全书》小工具章 |
| `broad`（双手大工具：大锤、挖掘铲、宽斧、镰刀、劈刀等） | 《强力熔炼》、《百科全书》大工具章 |
| `special` / `ancient` | 《百科全书》法杖章 / 远古工具章 |

两个标签都不加，手册里就没有这把工具。

页面文字写在启动脚本：

```js
event.create('kubejs:war_pick', 'tinkers_kubejs_arsenal:tool')
  // ...
  .bookText('zh_cn', '战镐是一把能挖能打的重型镐。')
  .bookText('zh_cn', '第二段介绍。')
  .bookProperty('zh_cn', '挖掘石头和矿石')
  .bookProperty('zh_cn', '扩展词条向前延伸')
```

启动脚本时还不知道工具归哪个标签，所以同一份页面会写进上面所有章节的目录；手册只读取实际收录这把工具的那一章，其他目录的文件不会被用到。没写 `.bookText` / `.bookProperty` 的语言，手册照样收录这把工具，只显示名称和部件组成，没有介绍文字。

## 输入检查

以下情况报英文错误并跳过这把工具：

- `ArsenalToolEvents.data` 里的 ID 没有在启动脚本用本模组的工具类型注册；
- 部件物品不是匠魂部件；
- 词条槽数量小于 0、词条等级小于 1；
- 部件序号超出范围；
- 部件超过 4 个却没写 `.layoutSlot`。

启动脚本里注册了但服务端没写 `ArsenalToolEvents.data` 的工具，会警告"没有属性、无法组装"。

## 文件

- `kubejs/tool/ArsenalToolBuilder.java`：两种物品类型的构建器、模型 JSON 生成、多语言名称、手册页面生成。
- `kubejs/tool/ToolDataEventJS.java`：`ArsenalToolEvents.data` 事件和工具定义链式对象。
- `kubejs/tool/ArsenalToolTabs.java`：匠魂工具创造栏添加材料变体，KubeJS 创造栏移除无材料版本。
- `TinkersKubejsArsenal.java`：注册创造栏事件监听。
- `kubejs/modifier/ArsenalToolEvents.java`：事件组新增 `data`。
- `kubejs/ArsenalKubeJSPlugin.java`：`init()` 注册物品类型。
- `mixin/ServerScriptManagerMixin.java`：写入工具定义、工作台布局、组装配方和可改造标签。
- `client/GeneratedMaterialSpriteSource.java`：自定义工具的部件底图加入运行时贴图生成。
- `kubejs/probe/ArsenalProbeJSPlugin.java`：补全部件物品、工具属性、词条槽、工具动作、方块标签、可改造标签。

## 验证

- `gradlew compileJava`：通过。
- 未做游戏内测试，按计划等本项目功能做完后统一测试。

## 限制

- 只支持普通工具和剑；弓弩、护甲、盾牌、法杖等后续阶段实现。
- 只能复用已有的部件；新部件形状后续阶段实现。
- 部件贴图数量为"材料数 × 部件数"，和匠魂自带工具相同：匠魂的镐子在 jar 里预先放了 246 张部件贴图。区别只在于匠魂是打包时生成好的，本模组在每次资源重载时现场生成；单张贴图很小，生成开销和占用的图集空间都不大。
