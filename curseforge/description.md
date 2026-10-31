# Tinkers KubeJS Arsenal

A Tinkers' Construct addon for Minecraft 1.20.1 Forge that lets you extend Tinkers' Construct with KubeJS scripts

## Features

*   **Recipes:** all 66 Tinkers' Construct recipe types (melting, casting, alloying, part building, Tinker Station, modifiers, severing and more) in one consistent chained style, plus one-call batch metal recipes.
*   **Materials:** new materials with stats, traits, colors and generated part textures; add stats or traits to existing materials; material tags and client-side variants.
*   **Material properties:** give materials hardness, density and toughness, and let formulas generate their stats and traits (see below).
*   **Stat types and parts:** register new material stat types and new part shapes. Casts, recipes, tags, models and per-material textures are generated for you.
*   **Tools:** register new modular tools, bows, crossbows, armor and armor sets, with stats, modifier slots, traits, tool actions and area mining.
*   **Modifiers:** define new modifiers and listen to melee damage, hits, projectile hits, block breaking, incoming damage, tool stats and attributes per modifier.
*   **Utilities:** `ArsenalTool` to query modifiers and read or write tool data, and `ArsenalFluids` for standard fluid amounts.

Most changes apply with `/reload`; changes to event logic only need `/kubejs reload server_scripts`.

## Material Properties

In Tinkers' Construct, every material needs hand-written stats for every stat type. Add a new stat type, and every material needs a new set of stats. With material properties, you write one formula instead:

*   **Properties:** give a material hardness, density and toughness, plus any custom values you need.
*   **Stat formulas:** write one linear formula for a stat type, and every material with properties gets its stats and traits generated. 100 materials need just 1 formula, and new materials only need their properties.
*   **Part formulas:** give a specific part extra tool stats and traits based on its material's properties. Tinkers' own parts are supported.
*   **Overrides:** hand-written stats always win, stat types can be excluded per material, and replace mode uses only the formula results.
*   **Tooltips:** part tooltips show the bonus. Hold Shift for stats, and use F3+H to see the original value and the bonus.

```js
ArsenalMaterialEvents.data(event => {
  event.create('kubejs:mythril').properties(4, 5, 9)

  event.statFormula('head')
    .field('melee_attack', { hardness: 1 })
    .field('durability', { base: 100, toughness: 50 }, 'nearest')
    .tiers({ hardness: 1 }, [[0, 'minecraft:wood'], [6, 'minecraft:iron'], [9, 'minecraft:diamond']])
    .trait('tconstruct:reinforced', { toughness: 0.25 }, 5)

  event.partFormula('tconstruct:pick_head')
    .stat('attack_damage', { hardness: 0.5 })
})
```

The same data can also be written in data packs. The results are normal Tinkers material data, so the book, JEI and multiplayer sync all work as usual. See [Material Properties](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki/Material-Properties) for details.

## ProbeJS Support

With ProbeJS installed, run `/probejs dump` after `/reload` to get autocompletion for:

*   **Recipes:** every `event.recipes.tconstruct.*` recipe type and its arguments.
*   **IDs from your current game:** materials, material stat types, mining tiers, modifiers, part builder patterns, modifier slot types, tool parts, material items, modifiable tools, tool stats and attributes. Script-defined content is included, and custom IDs are still accepted.
*   **Builder arguments:** fluids and fluid tags, block tags, ore rates, rarities, armor slots, part stat types, tool actions, modifiable tags, texture fallbacks, stat and attribute operations, modifier level and tooltip display, area-mining direction and language codes.
*   **Covered APIs:** melting, casting, alloying and modifier recipes; material definitions, appearance and tags; stat types; modifier definitions; `toolStats` and `attributes` events; `ArsenalTool`; tool, part and armor set builders; tool data definitions.

## Requirements

*   Minecraft Forge 1.20.1
*   Tinkers' Construct (with Mantle)
*   KubeJS 2001.6.x (with Rhino and Architectury)
*   HPTlib 1.1.0 or newer
*   ProbeJS (optional, for script autocompletion)

Install on both client and server.

## Documentation

Full guide with examples in English and Chinese: [GitHub Wiki](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki)

Source code and issues: [GitHub](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal)

## 中文介绍

匠魂 KubeJS 军械库是匠魂（Tinkers' Construct）的附属模组

*   **配方：** 支持全部 66 种匠魂配方（熔化、浇铸、合金、部件、工匠站、词条、斩首等），统一链式写法，并提供一行生成整套金属配方的工具。
*   **材料：** 定义新材料的属性、特性、颜色和自动生成的部件贴图；给已有材料追加属性或特性；材料标签与客户端材料变体。
*   **材料三围：** 给材料填写硬度、密度、韧性，由公式自动生成属性和特性（见下文）。
*   **属性类型与部件：** 注册新的材料属性类型和新部件形状，自动生成铸模、配方、标签、模型和各材料贴图。
*   **工具：** 注册新的模块化工具、弓、弩、护甲和护甲套，设置数值、词条槽、特性、工具动作和范围挖掘。
*   **词条：** 定义新词条，并按词条监听近战伤害、命中、弹射物命中、挖掘、受伤、工具属性和属性修饰等事件。
*   **工具类：** `ArsenalTool` 查询词条、读写工具数据，`ArsenalFluids` 提供标准流体用量。

大部分修改执行 `/reload` 即可生效；只改事件逻辑时用 `/kubejs reload server_scripts` 更快。

材料三围：原版匠魂里，每个材料都要为每种属性类型手写数值；新增一种属性类型，就得给所有材料各补一份。三围体系改为只写一份公式：

*   **三围：** 给材料填写硬度、密度、韧性，也可以加自定义数值。
*   **属性公式：** 给属性类型写一份线性公式，所有填了三围的材料自动算出该类型的数值和特性。100 个材料只要 1 份公式，新材料也只需填三围。
*   **部件公式：** 按部件所用材料的三围，为某个具体部件额外加成工具属性和特性，匠魂原版部件也能写。
*   **覆盖规则：** 手写的数值始终优先，可以按材料排除属性类型，替换模式下完全只用公式结果。
*   **提示框：** 部件提示框直接显示加成，按住 Shift 查看属性，F3+H 查看原值与加成。

同样的内容也可以用数据包编写。生成的都是普通的匠魂材料数据，手册、JEI 和联机同步照常工作。写法见 Wiki 的[材料三围与属性公式](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki/Material-Properties-zh)。

ProbeJS 支持：安装 ProbeJS 后，在 `/reload` 之后执行 `/probejs dump`，即可获得以下补全：

*   **配方：** 全部 `event.recipes.tconstruct.*` 配方类型及其参数。
*   **按当前游戏生成的 ID 列表：** 材料、材料属性类型、挖掘等级、词条、部件加工台图案、词条槽类型、工具部件、材料物品、可改造工具、工具属性和属性修饰。脚本定义的内容也会列出，同时仍可填写自定义 ID。
*   **构建器参数：** 流体与流体标签、方块标签、矿石倍率、稀有度、护甲部位、部件属性类型、工具动作、可改造标签、贴图回退、属性运算方式、词条等级与提示显示方式、范围挖掘方向、语言代码。
*   **覆盖的 API：** 熔化、浇铸、合金、词条配方；材料定义、外观与标签；材料属性类型；词条定义；`toolStats` 与 `attributes` 事件；`ArsenalTool`；工具、部件、护甲套构建器；工具数据定义。

前置：Minecraft Forge 1.20.1、匠魂（含 Mantle）、KubeJS 2001.6.x（含 Rhino、Architectury）、HPTlib 1.1.0 及以上；ProbeJS 可选。客户端和服务端都需要安装。

完整中英文教程见 [GitHub Wiki](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki)。
