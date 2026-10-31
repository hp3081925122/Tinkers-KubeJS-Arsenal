# Tinkers KubeJS Arsenal

A Tinkers' Construct addon for Minecraft 1.20.1 Forge that lets you extend Tinkers' Construct with KubeJS scripts, without writing any Java.

## Features

- **Recipes:** all 66 Tinkers' Construct recipe types (melting, casting, alloying, part building, Tinker Station, modifiers, severing and more) in one consistent chained style, plus one-call batch metal recipes.
- **Materials:** new materials with stats, traits, colors and generated part textures; add stats or traits to existing materials; material tags and client-side variants.
- **Stat types and parts:** register new material stat types and new part shapes. Casts, recipes, tags, models and per-material textures are generated for you.
- **Tools:** register new modular tools, bows, crossbows, armor and armor sets, with stats, modifier slots, traits, tool actions and area mining.
- **Modifiers:** define new modifiers and listen to melee damage, hits, projectile hits, block breaking, incoming damage, tool stats and attributes per modifier.
- **Utilities:** `ArsenalTool` to query modifiers and read or write tool data, and `ArsenalFluids` for standard fluid amounts.

Most changes apply with `/reload`; changes to event logic only need `/kubejs reload server_scripts`.

## ProbeJS Support

With ProbeJS installed, run `/probejs dump` after `/reload` to get autocompletion for:

- **Recipes:** every `event.recipes.tconstruct.*` recipe type and its arguments.
- **IDs from your current game:** materials, material stat types, mining tiers, modifiers, part builder patterns, modifier slot types, tool parts, material items, modifiable tools, tool stats and attributes. Script-defined content is included, and custom IDs are still accepted.
- **Builder arguments:** fluids and fluid tags, block tags, ore rates, rarities, armor slots, part stat types, tool actions, modifiable tags, texture fallbacks, stat and attribute operations, modifier level and tooltip display, area-mining direction and language codes.
- **Covered APIs:** melting, casting, alloying and modifier recipes; material definitions, appearance and tags; stat types; modifier definitions; `toolStats` and `attributes` events; `ArsenalTool`; tool, part and armor set builders; tool data definitions.

## Requirements

- Minecraft Forge 1.20.1
- Tinkers' Construct (with Mantle)
- KubeJS 2001.6.x (with Rhino and Architectury)
- ProbeJS (optional, for script autocompletion)

Install on both client and server.

## Documentation

Full guide with examples in English and Chinese: [GitHub Wiki](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki)

Source code and issues: [GitHub](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal)

## 中文介绍

匠魂 KubeJS 军械库是匠魂（Tinkers' Construct）的附属模组，适用于 Minecraft 1.20.1 Forge。不写 Java，只用 KubeJS 脚本扩展匠魂。

- **配方：** 支持全部 66 种匠魂配方（熔化、浇铸、合金、部件、工匠站、词条、斩首等），统一链式写法，并提供一行生成整套金属配方的工具。
- **材料：** 定义新材料的属性、特性、颜色和自动生成的部件贴图；给已有材料追加属性或特性；材料标签与客户端材料变体。
- **属性类型与部件：** 注册新的材料属性类型和新部件形状，自动生成铸模、配方、标签、模型和各材料贴图。
- **工具：** 注册新的模块化工具、弓、弩、护甲和护甲套，设置数值、词条槽、特性、工具动作和范围挖掘。
- **词条：** 定义新词条，并按词条监听近战伤害、命中、弹射物命中、挖掘、受伤、工具属性和属性修饰等事件。
- **工具类：** `ArsenalTool` 查询词条、读写工具数据，`ArsenalFluids` 提供标准流体用量。

大部分修改执行 `/reload` 即可生效；只改事件逻辑时用 `/kubejs reload server_scripts` 更快。

ProbeJS 支持：安装 ProbeJS 后，在 `/reload` 之后执行 `/probejs dump`，即可获得以下补全：

- **配方：** 全部 `event.recipes.tconstruct.*` 配方类型及其参数。
- **按当前游戏生成的 ID 列表：** 材料、材料属性类型、挖掘等级、词条、部件加工台图案、词条槽类型、工具部件、材料物品、可改造工具、工具属性和属性修饰。脚本定义的内容也会列出，同时仍可填写自定义 ID。
- **构建器参数：** 流体与流体标签、方块标签、矿石倍率、稀有度、护甲部位、部件属性类型、工具动作、可改造标签、贴图回退、属性运算方式、词条等级与提示显示方式、范围挖掘方向、语言代码。
- **覆盖的 API：** 熔化、浇铸、合金、词条配方；材料定义、外观与标签；材料属性类型；词条定义；`toolStats` 与 `attributes` 事件；`ArsenalTool`；工具、部件、护甲套构建器；工具数据定义。

前置：Minecraft Forge 1.20.1、匠魂（含 Mantle）、KubeJS 2001.6.x（含 Rhino、Architectury）；ProbeJS 可选。客户端和服务端都需要安装。

完整中英文教程见 [GitHub Wiki](https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki)。
