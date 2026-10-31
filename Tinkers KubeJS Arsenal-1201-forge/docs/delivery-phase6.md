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
| `.parentModel(...)` | KubeJS 自带方法，改模型的手持姿态；默认普通工具用 `tconstruct:item/base/axe`，剑用 `forge:item/default-tool` |

KubeJS 物品构建器自带的方法（`.tag`、`.rarity` 等）照常可用。工具固定不可堆叠。

**开发者需要画的贴图：** 每个部件一张灰度底图，放在 `kubejs/assets/kubejs/textures/item/tool/war_pick/head.png` 这类路径；声明了 `.brokenPart` 的部件再画一张 `head_broken.png`。每种材料的部件贴图由 4C 的运行时生成按灰度底图自动着色，匠魂自带的 97 种材料中有 86 种带生成配置，第四期用 KubeJS 定义并写了 `generator` 的材料也会生成。

词条在工具上的外观贴图可选，放在 `kubejs/assets/kubejs/textures/item/tool/war_pick/modifiers/` 下，文件名和匠魂一致；没有就不显示词条外观。

### 服务端脚本：工具数值

```js
ArsenalToolEvents.data(event => {
  event.create('kubejs:war_pick')
    .baseStats({ attack_damage: 2, attack_speed: 1.0 })
    .multiplyStats({ durability: 1.2, mining_speed: 0.8 })
    .slots({ upgrades: 3, abilities: 1 })
    .trait('tconstruct:pierce', 1)
    .toolActions('pickaxe_dig')
    .effective('minecraft:mineable/pickaxe')
    .boxAoe({ expansion_direction: 'tconstruct:pitch', expansions: [{ depth: 2 }, { height: 1 }] })
    .modifiable('melee/weapon', 'harvest/primary', 'harvest/stone', 'aoe', 'small', 'interactable/right')
    .materialName(0)
})
```

| 方法 | 生成的匠魂模块或数据 |
|---|---|
| `.baseStats({...})` | `tconstruct:base_stats`；键不带命名空间时补 `tconstruct:` |
| `.multiplyStats({...})` | `tconstruct:multiply_stats` |
| `.slots({...})` | `tconstruct:modifier_slots`，值为不小于 0 的整数 |
| `.trait(modifier)` / `.trait(modifier, level)` | `tconstruct:traits` |
| `.toolActions(...)` | `tconstruct:tool_actions` |
| `.effective(blockTag)` | `tconstruct:is_effective`，用 `mantle:tag` 判断方块标签，可带或不带 `#` |
| `.boxAoe` / `.circleAoe` / `.veinAoe` / `.treeAoe` | 对应 `tconstruct:*_aoe`，参数对象原样写入，字段和匠魂 JSON 一致 |
| `.sweepMelee` / `.circleMelee` | 对应 `tconstruct:sweep_melee`、`tconstruct:circle_melee` |
| `.module(type, options)` | 任意匠魂工具模块 |
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

## 输入检查

以下情况报英文错误并跳过这把工具：

- `ArsenalToolEvents.data` 里的 ID 没有在启动脚本用本模组的工具类型注册；
- 部件物品不是匠魂部件；
- 属性值不是数字、词条槽不是非负整数、词条等级小于 1；
- 部件序号超出范围；
- 部件超过 4 个却没写 `.layoutSlot`。

启动脚本里注册了但服务端没写 `ArsenalToolEvents.data` 的工具，会警告"没有属性、无法组装"。

## 文件

- `kubejs/tool/ArsenalToolBuilder.java`：两种物品类型的构建器、模型 JSON 生成、多语言名称。
- `kubejs/tool/ToolDataEventJS.java`：`ArsenalToolEvents.data` 事件和工具定义链式对象。
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
- 生成的部件贴图数量约为"材料数 × 部件数"，每把工具约数百张，会增加资源重载时间和内存。
- 匠魂创造模式物品栏、JEI 子类型和匠魂手册条目未接入，后续阶段实现。
