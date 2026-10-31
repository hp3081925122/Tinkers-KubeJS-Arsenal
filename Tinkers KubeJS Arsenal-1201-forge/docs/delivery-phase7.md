# 第七期交付说明：KubeJS 注册新部件

## 完成内容

在启动脚本里注册新的匠魂部件形状。部件的物品、铸模、配方、标签、模型和材料贴图都由本模组自动生成，改动需要重启游戏。

## 写法

```js
StartupEvents.registry('item', event => {
  event.create('kubejs:great_blade', 'tinkers_kubejs_arsenal:part')
    .statType('head')
    .cost(4)
    .casts()
    .displayName('Great Blade')
    .name('zh_cn', '巨刃')

  event.create('kubejs:war_axe', 'tinkers_kubejs_arsenal:tool')
    .part('blade', 'kubejs:great_blade')
    .part('handle', 'tconstruct:tough_handle')
})
```

| 方法 | 说明 |
|---|---|
| 类型 `tinkers_kubejs_arsenal:part` | 匠魂 `ToolPartItem` |
| `.statType(statType)` | 必填。用哪类材料属性，决定哪些材料能做、部件提供哪些数值；不带命名空间时补 `tconstruct:`。可用 `head`、`handle`、`binding`、`limb`、`grip`、`bowstring`、`plating_*`、`maille`、`skull`、`arrow_head`、`arrow_shaft`、`fletching` 等已有类型，或第八期注册的新类型（写完整 ID） |
| `.cost(n)` | 材料消耗，单位和匠魂相同（1 = 一锭），默认 1，不能小于 1 |
| `.casts()` | 同时注册金、沙、红沙三种铸模；不写就只能在部件制作台用材料直接做 |
| `.partTexture(texture)` | 改部件灰度底图路径，默认 `<命名空间>:item/part/<路径>` |
| `.name(lang, text)` | 其他语言的名称；英文名照常用 `.displayName(...)`。部件制作台的图案名和铸模名跟着生成 |

部件注册后可以直接用在第六期的自定义工具里，`.part(name, partItem)` 填这个部件的 ID 即可。

## 自动生成的内容

| 类别 | 内容 |
|---|---|
| 物品 | 部件；写了 `.casts()` 时再加 `<路径>_cast`、`<路径>_sand_cast`、`<路径>_red_sand_cast` 三个铸模（`PartCastItem`） |
| 模型 | 部件用 `tconstruct:material` 加载器，按材料着色；铸模用普通物品模型 |
| 部件配方 | 部件制作台 `part_builder`（空白图案；有金铸模时也可用金铸模当图案）；复合浇铸 `table_casting_composite`；金铸模浇铸和沙铸模浇铸 `table_casting_material` |
| 铸模配方 | 部件上浇 90mB 熔融金得到金铸模；沙铸模、红沙铸模各 3 种做法：部件制作台用沙子做 4 个、用空白沙铸模做、在铸模台上用部件压出来 |
| 标签 | `tconstruct:parts`；`tconstruct:casts/gold`、`casts/sand`、`casts/red_sand`；`<命名空间>:casts/multi_use/<路径>`、`<命名空间>:casts/single_use/<路径>`，并挂到 `tconstruct:casts/multi_use`、`casts/single_use` 下 |
| 语言 | 部件名、图案名 `pattern.<命名空间>.<路径>`、三种铸模名（英文加 " Gold Cast" 这类后缀，中文加"金质铸模"这类后缀，和匠魂一致） |
| 材料贴图 | 部件灰度底图加入运行时贴图生成，每种带 `generator` 的材料自动着色 |

配方和标签写进服务端动态数据包，格式照抄匠魂镐头的数据。

## 开发者需要画的贴图

以 `kubejs:great_blade` 为例，放在 `kubejs/assets/kubejs/textures/` 下：

| 路径 | 用途 |
|---|---|
| `item/part/great_blade.png` | 部件灰度底图，各材料颜色由它生成 |
| `gui/tinker_pattern/great_blade.png` | 部件制作台里的图案图标，匠魂会自动收进方块图集 |
| `item/cast/great_blade.png` | 金铸模（写了 `.casts()` 才需要） |
| `item/sand_cast/great_blade.png` | 沙铸模 |
| `item/red_sand_cast/great_blade.png` | 红沙铸模 |

缺图只显示紫黑方块，不报错。

## 匠魂创造栏与 JEI

- 部件按材料变体放进匠魂"工具部件"创造栏，铸模放进"冶炼"创造栏；KubeJS 创造栏里的无材料部件和铸模移除。
- JEI：匠魂 JEI 插件按 `tconstruct:parts` 标签给部件注册材料子类型，部件已自动加入该标签；部件制作台、浇铸、铸模台配方从数据包读取，自动显示。

## 输入检查

- 没写 `.statType(...)`：启动时报错 `Part <id>: Part has no stat type ...`。
- `.cost` 小于 1：启动时报错。
- 属性类型不存在（比如拼错）：服务端加载时警告 `Part <id> uses unknown stat type ...`，这种部件没有材料能做。

## 文件

- `kubejs/tool/ArsenalPartBuilder.java`：部件类型构建器、铸模附加物品、模型、语言、配方和标签生成。
- `kubejs/ArsenalKubeJSPlugin.java`：注册物品类型 `tinkers_kubejs_arsenal:part`。
- `mixin/ServerScriptManagerMixin.java`：写入部件配方和标签；物品标签统一按完整标签 ID 汇总。
- `client/GeneratedMaterialSpriteSource.java`：部件底图加入运行时贴图生成。
- `kubejs/tool/ArsenalToolTabs.java`：部件、铸模的创造栏处理。
- `kubejs/probe/ArsenalProbeJSPlugin.java`：补全 `statType`。

## 验证

- `gradlew compileJava`：通过。
- 未做游戏内测试，按计划等本项目功能做完后统一测试。

## 限制

- 新增材料属性类型、工具回收、手册部件的情况见第八期 `delivery-phase8.md`。
