# 第九期开发文档：KubeJS 自定义弓弩与护甲

## 0. 背景与约束

- 第六期的自定义工具只支持两种物品类型：`tinkers_kubejs_arsenal:tool`（`ModifiableItem`）和 `tinkers_kubejs_arsenal:sword`（`ModifiableSwordItem`）。本期新增弓、弩、护甲。
- 本期是在第六至八期基础上做的。开工前确认第六至八期的补测（`docs/handoff-test-supplement.md`）已完成并且没有未修的失败项。
- 项目环境、用户规则、写法规范同 `docs/handoff-phase8b.md` 第 0 节：
  - 回复和文档用中文；Java 不写注释；调试日志用英文。
  - 提交信息不带 AI 署名。
  - KubeJS 禁止写只调用一次的 function。
  - 服务端定义 API 用链式和位置参数（S16）。
  - 使用 API 前先确认。
  - 修改后跑 `compileJava`。
- 匠魂 jar 已解压在 `D:/AI临时文件/tc_jar_8809959/`。本文里写"已确认"的内容都在这份 jar 里核对过（javap 字节码或数据文件）。
- 游戏内测试按 skill `minecraft-mod-testing`：开发环境 `runClient` 已带游戏交互 MCP，每个"通过"都要有截图、日志或数值作证据。

## 1. 范围

| 做 | 不做（后续阶段实现） |
|---|---|
| 弓（`ModifiableBowItem`，对应匠魂长弓） | 盾牌、法杖、鞘翅类、钓竿、投掷武器、弹药（箭） |
| 弩（`ModifiableCrossbowItem`） | 长弓的大号手持模型（`large`） |
| 头盔、胸甲、护腿、靴子（`MultilayerArmorItem`） | 按材料生成的护甲穿戴贴图（见 4.4） |

## 2. 匠魂实现（已确认）

### 2.1 弓弩

**物品类**（`slimeknights.tconstruct.library.tools.item.ranged`，都继承 `ModifiableLauncherItem`）：

| 类 | 构造器 |
|---|---|
| `ModifiableBowItem` | `(Item.Properties, ToolDefinition)` |
| `ModifiableCrossbowItem` | `(Item.Properties, ToolDefinition)`；另有带 `Predicate<ItemStack>` 弹药判断的重载，本期用默认 |

拉弓、射箭、弹药消耗都由物品类自己处理，工具定义里不需要额外模块。

**工具定义：**
- 长弓：`part_stats`（`bow_limb`、`bow_limb`、`bow_grip`、`bowstring`）、`default_materials`、`base_stats`（attack_damage 0、attack_speed 1、durability 120）、`multiply_stats`（durability 1.5）、`modifier_slots`（abilities 1、upgrades 2）。
- 弩：类似，部件为 `bow_limb`、`bow_grip`、`bowstring`，另有 `fixed_material_name`。

第六期 `ToolDataEventJS` 现有的链式方法已经能表达这些模块。

**属性类型：** `limb`、`grip`、`bowstring`，提供拉弓速度、箭速、精准等，第四期材料 API 已支持。

**物品模型**（`assets/tconstruct/models/item/longbow.json`）：
- `loader: tconstruct:tool`，`parent: tconstruct:item/base/bow`（弩是 `tconstruct:item/base/crossbow`）。
- `overrides` 用谓词 `tconstruct:charging` 和 `tconstruct:charge`：
  - `charging: 1` 是拉弓，`charge` 取 0.05、0.6、1.0 时分别对应 `pulling_1`、`pulling_2`、`pulling_3`。
  - `charging: 2` 是格挡，对应 `blocking`、`blocking_1`、`blocking_2`、`blocking_3`。
  - 另有 `tconstruct:broken` 对应 `broken`。
- 拉弓模型（如 `models/item/tool/longbow/pulling_1.json`）和主模型结构相同，只是会动的图层换成带帧号的贴图（`limb_bottom_1`、`bowstring_1`），不动的图层（`grip`）不变；`modifier_roots` 前面多一个 `modifiers/1/`。

**标签**（`data/tconstruct/tags/items/modifiable/`）：

| 标签 | 长弓 | 弩 | 作用 |
|---|---|---|---|
| `ranged/longbows` | ✓ | | 归入 `ranged/bows`、`ranged/power`、`ranged/bounce` |
| `ranged/crossbows` | | ✓ | 归入 `ranged/bows`、`ranged/quick_charge` |
| `ranged/bows` → `ranged/launcher` → `ranged` | 间接 | 间接 | 远程词条 |
| `ranged/broad` / `ranged/small` | 长弓在 broad | 弩在 small | 手册收录（broad 进《强力熔炼》） |
| `interactable/left` | ✓ | ✓ | 左手交互 |
| `multipart`、`durability`、`bonus_slots` | ✓ | ✓ | 第六期已自动加前两个 |

**工作台布局：** `tinkering/station_layouts/longbow.json`、`crossbow.json`，格式同第六期工具。

### 2.2 护甲

**物品类**（`slimeknights.tconstruct.library.tools.item.armor`）：
- `MultilayerArmorItem(ArmorMaterial, ArmorItem.Type, Item.Properties, ToolDefinition, ResourceLocation 穿戴模型名)`：本期用这个构造器。
- `ArmorMaterial` 可以用 `DummyArmorMaterial(ResourceLocation, SoundEvent)`。
- 匠魂自己用 `ModifiableArmorMaterial.create(id, sound, types...)`，它生成的工具定义 ID 是 `<id>_<type>`（如 `tconstruct:plate_chestplate`）。本模组每件护甲是一个 KubeJS 物品，直接用 `ToolDefinition.create(物品ID)`，和第六期一致。

**工具定义**（`plate_helmet.json`）：
- 包括 `part_stats`（`helmet_plating`、`maille`）、`default_materials`、`modifier_slots`（defense 3、upgrades 2）、`fixed_material_name`。
- **没有 `base_stats`**，护甲值、韧性、击退抗性都来自 `plating_*` 材料属性。
- 胸甲多一个 `multiply_stats`（attack_damage 0.5）。

**属性类型：** `plating_helmet`、`plating_chestplate`、`plating_leggings`、`plating_boots`、`maille`，第四期材料 API 已支持。

**物品模型**（`plate_chestplate.json`）：`loader: tconstruct:tool`，`parent: forge:item/default`，`modifier_maps: ["tconstruct:plate/chestplate", "tconstruct:armor/chestplate"]`，`tconstruct:broken` 对应 `_broken`。

**穿戴模型**（客户端资源 `assets/<ns>/tinkering/armor_models/<名字>.json`）：
- 由 `ArmorModelManager` 按 `MultilayerArmorItem` 构造器里的名字加载。
- 板甲的写法：
  ```json
  {"layers":[
    {"type":"tconstruct:material","index":0,"prefix":"tconstruct:plate/plating_"},
    {"type":"tconstruct:first_present","options":[...]},
    "tconstruct:trim"
  ]}
  ```
- 图层类型有：`material`（按第 index 个部件的材料）、`fixed`（固定贴图）、`dyed`（可染色）、`first_present`（取第一个存在的）、`material_has_fallback`、`trim`（锻造纹饰）。
- 穿戴贴图路径：`textures/tinker_armor/<prefix 路径>armor.png`（头盔、胸甲、靴子）和 `...leggings.png`（护腿），按材料的版本是 `..._<材料>.png`。

**穿戴贴图找不到材料版本时的处理**（`MaterialArmorTextureSupplier`）：依次找材料贴图、`fallbacks`，最后用底图按材料的 `vertexColor` 着色（`TintedArmorTexture`）。所以只画底图也能显示，各材料只是颜色不同、没有专门纹理。

**工作台布局**（`plate_armor.json`）：一套护甲共用**一个**布局：
- 第一格的 `filter` 是四种 plating 的列表，第二格是 maille，`icon` 用 `{"pattern": ...}`。
- 组装配方（`recipes/tools/armor/plate/plate_chestplate.json`）是 `{"type":"tconstruct:tool_building","result":"tconstruct:plate_chestplate","result_count":1,"slot_layout":"tconstruct:plate_armor"}`。
- 匠魂靠放入的部件匹配到具体哪件护甲。

**标签：**

| 标签 | 说明 |
|---|---|
| `armor/helmets`、`armor/chestplate`、`armor/leggings`、`armor/boots` | 按部位，**注意头盔是复数 `helmets`，胸甲是单数 `chestplate`** |
| `armor/worn` → `armor` | 由上面四个标签间接包含 |
| `dyeable` | 穿戴模型有 `dyed` 图层时才加 |
| `armor/trim` | 穿戴模型有 `trim` 图层时才加 |
| `multipart`、`durability`、`bonus_slots` | 同工具 |
| `book_armor/<手册>` | 手册收录（见 4.6） |

**创造栏：** 匠魂把板甲放在工具栏（`TinkerTools.addTabItems` 里有 `plateArmor`），自定义护甲也放工具栏，沿用第六期 `ArsenalToolTabs` 的做法。

## 3. 写法设计

### 3.1 启动脚本

```js
StartupEvents.registry('item', event => {
  // 弓：类型决定物品类
  event.create('kubejs:short_bow', 'tinkers_kubejs_arsenal:bow')
    .part('limb', 'tconstruct:bow_limb')
    .part('grip', 'tconstruct:bow_grip')
    .part('bowstring', 'tconstruct:bowstring')
    .pullFrames('limb', 'bowstring')
    .brokenPart('limb')
    .name('zh_cn', '短弓')

  // 弩
  event.create('kubejs:heavy_crossbow', 'tinkers_kubejs_arsenal:crossbow')
    .part('limb', 'tconstruct:bow_limb')
    .part('body', 'tconstruct:bow_grip')
    .part('bowstring', 'tconstruct:bowstring')
    .pullFrames('limb', 'bowstring')

  // 护甲：每件一个物品，用 armorSet 归到同一套
  event.create('kubejs:bronze_chestplate', 'tinkers_kubejs_arsenal:armor')
    .slot('chestplate')
    .armorSet('kubejs:bronze')
    .part('plating', 'tconstruct:chestplate_plating')
    .part('maille', 'tconstruct:maille')
    .equipSound('minecraft:item.armor.equip_iron')
    .name('zh_cn', '青铜胸甲')
})

// 护甲套的穿戴模型（启动事件，一套写一次）
ArsenalToolEvents.armorSets(event => {
  event.create('kubejs:bronze')
    .materialLayer('plating', 0)
    .materialLayer('maille', 1)
    .trim()
})
```

| 方法 | 适用 | 说明 |
|---|---|---|
| `.pullFrames(...parts)` | 弓、弩 | 这些部件有拉弓帧贴图 `<底图>_1/_2/_3`，拉弓模型里换成对应帧；没列出的部件不变 |
| `.slot(type)` | 护甲 | `helmet`、`chestplate`、`leggings`、`boots`，必填 |
| `.armorSet(id)` | 护甲 | 所属护甲套，决定穿戴模型和共用工作台布局，必填 |
| `.equipSound(sound)` | 护甲 | 穿上时的声音，默认铁甲声 |
| `event.create(setId)`（armorSets） | 护甲套 | 生成 `assets/<ns>/tinkering/armor_models/<路径>.json` |
| `.materialLayer(name, partIndex)` | 护甲套 | `{"type":"tconstruct:material","index":i,"prefix":"<ns>:<路径>/<name>_"}` |
| `.fixedLayer(name)` / `.dyedLayer(name)` | 护甲套 | `fixed` / `dyed` 图层；有 `dyed` 时自动加 `dyeable` 标签 |
| `.trim()` | 护甲套 | 加 `"tconstruct:trim"` 图层，并自动加 `armor/trim` 标签 |

`armorSets` 是新的启动事件，放在 `ArsenalToolEvents` 组里，在 `initStartup()` 发布。做法同第八期 `ArsenalMaterialEvents.statTypes`。

### 3.2 服务端脚本

沿用 `ArsenalToolEvents.data`，不新增方法：

```js
ArsenalToolEvents.data(event => {
  event.create('kubejs:short_bow')
    .baseStat('durability', 100)
    .multiplyStat('durability', 1.2)
    .slot('upgrades', 2)
    .slot('abilities', 1)
    .modifiable('ranged/longbows', 'ranged/broad', 'interactable/left')

  event.create('kubejs:bronze_chestplate')
    .slot('defense', 3)
    .slot('upgrades', 2)
    .materialName(0)
})
```

按物品类型自动生成的部分：

| 类型 | 自动标签 | 布局与配方 |
|---|---|---|
| 弓 | `multipart`、`durability`、`ranged/longbows`、`interactable/left` | 每把一个布局，同第六期 |
| 弩 | `multipart`、`durability`、`ranged/crossbows`、`interactable/left` | 同上 |
| 护甲 | `multipart`、`durability`、`armor/<部位>`（注意 helmets 复数），加上 3.1 里按图层自动加的 | 一套共用布局 `<套ns>:<套路径>_armor`。第一格 filter 是这套所有件的第一个部件，其余格同理；组装配方带 `slot_layout` |

护甲没有 `baseStat` 也不报警告；工具和弓弩缺少 `baseStat` 时照旧。

## 4. 实现要点

### 4.1 物品类型注册

`ArsenalKubeJSPlugin.init()` 加三种类型：`BOW_TYPE`、`CROSSBOW_TYPE`、`ARMOR_TYPE`。

两种做法：
- 在 `ArsenalToolBuilder` 里把 `boolean sword` 换成枚举 `Kind { TOOL, SWORD, BOW, CROSSBOW, ARMOR }`。
- 护甲另写 `ArsenalArmorBuilder`，因为差异大（slot、set、ArmorItem 的构造）。

建议护甲单独写；弓弩并入 `ArsenalToolBuilder`，模型生成共用。

`createObject()`：
- 弓：`new ModifiableBowItem(props, ToolDefinition.create(id))`。
- 弩：`new ModifiableCrossbowItem(props, ToolDefinition.create(id))`。
- 护甲：`new MultilayerArmorItem(new DummyArmorMaterial(setId, sound), type, props, ToolDefinition.create(id), setId)`。
  - `ArmorItem.Type` 由 `.slot` 换算。
  - 先 javap 确认 `DummyArmorMaterial` 对耐久、防御相关方法返回什么，避免原版护甲值叠加到匠魂数值上。

### 4.2 物品模型

弓弩在第六期 `toolModel()` 基础上做这些改动：
- `parent` 默认值：弓 `tconstruct:item/base/bow`，弩 `tconstruct:item/base/crossbow`，护甲 `forge:item/default`。
- 弓弩的 `overrides`：生成 `pulling_1..3` 三个子模型，写到 `models/item/tool/<路径>/pulling_N.json`。
  - 子模型里 `.pullFrames` 列出的部件贴图加 `_N` 后缀。
  - `modifier_roots` 前面加 `.../modifiers/N/`。
  - 谓词值照抄匠魂（0.05、0.6、1.0）。
- 格挡模型（`charging: 2`）用于"格挡"类词条。本期照样生成，贴图复用拉弓帧，即 `blocking_N` 用 `pulling_N` 的贴图；先打开匠魂的 `blocking_1.json` 确认它的结构。
- 损坏模型的 override 排在最后，和匠魂顺序一致。
- 护甲：`modifier_maps` 写 `tconstruct:armor/<部位>`，可以复用匠魂护甲词条的外观。

### 4.3 材料贴图生成

`GeneratedMaterialSpriteSource.loadParts()` 现在会加入每个部件底图和 `_broken`。还要补：
- 弓弩 `.pullFrames` 部件的 `_1`、`_2`、`_3`。如果同时声明了损坏，也要确认匠魂有没有损坏帧，按匠魂的贴图文件列表决定。

### 4.4 护甲穿戴贴图

- 开发者画底图：`textures/tinker_armor/<套路径>/<图层名>_armor.png` 和 `..._leggings.png`。
- 各材料用底图按 `vertexColor` 着色显示，这是匠魂本身的回退逻辑，不需要我们处理。
- 按材料生成专门的穿戴贴图不在本期范围。原因：这些贴图不在纹理图集里，不能沿用 `GeneratedMaterialSpriteSource`，需要另做一个动态资源包。第四期 KubeJS 材料穿在匠魂板甲上也是同样的着色效果。

### 4.5 服务端数据

`ToolDataEventJS` / `ServerScriptManagerMixin`：
- `recipe()` 支持可选的 `slot_layout`，护甲填所属套的布局 ID。
- 护甲套布局：同一 `armorSet` 下的所有件**合并成一个**布局文件 `tinkering/station_layouts/<套路径>_armor.json`。
  - 第 i 格的 `filter` 是这套所有件第 i 个部件的列表。
  - `icon` 先照抄匠魂用 `{"pattern": ...}`，图案贴图需要开发者提供；或者改用物品图标，查 `StationSlotLayout` 的 icon 格式后决定。
  - 只要有一件护甲写了服务端定义，就生成这个布局；没写定义的件跳过并警告。
- 自动标签按 3.2 的表。

### 4.6 手册

- 弓弩：第六期 `bookText` 的写入路径已经包含 `mighty_smelting/<lang>/tools`、`encyclopedia/<lang>/tools/broad` 等。只要加了 `ranged/broad` 标签就会被收录，**不用改**。
- 护甲：《百科全书》用 `tconstruct:modifiable/book_armor` 标签，页面路径是 `armor`。其他手册用 `book_armor/<手册名>` 子标签，各自的路径要在对应手册的 `sections/*.json` 里查 `load_tools`。
- 护甲的 `bookText` 写入路径要加这些目录，并提供 `.bookArmor('puny_smelting')` 这类方法加子标签。具体设计等查清各手册的路径后再定。

### 4.7 ProbeJS

- 新增参数类型：
  - `slot`（护甲部位）：`"helmet" | "chestplate" | "leggings" | "boots"`。注意已有 `slot` 参数名用于词条槽，要换个名字，比如 `armorSlot`。
  - `armorSet`。
- 把新的构建器类和套定义类加进 transform 列表。

## 5. 分期建议

| 步骤 | 内容 | 验收 |
|---|---|---|
| 9A | 弓、弩：物品类型、模型（拉弓、格挡、损坏）、贴图生成、自动标签 | 游戏内能组装、拉弓动画正常、射出箭、远程词条能加 |
| 9B | 护甲：物品类型、护甲套启动事件、穿戴模型、共用布局、标签 | 能组装 4 件、穿上显示正常、护甲值来自材料、可染色/纹饰（如果声明了） |
| 9C | 护甲手册收录、文档 `delivery-phase9.md`、写法规范 | — |

每步完成后跑 `compileJava`，并按 `minecraft-mod-testing` 做游戏内验证，然后提交、推送。

## 6. 测试要点

- **弓：** 用铁、木材料组装；手持拉弓截图三帧；射箭命中；加"力量"词条（`ranged/power`）成功，加"快速装填"失败（不在 `quick_charge` 标签）。
- **弩：** 装填、发射；加"快速装填"成功。
- **护甲：**
  - 组装四件。
  - 物品提示里护甲值等于材料 plating 属性计算值。
  - 穿上后截图正面、背面。
  - 染色（如果声明了 dyed 图层）、锻造台纹饰（如果声明了 trim）。
  - 受伤时耐久下降。
- **数值：** 用脚本按指定材料造出物品，读 `ToolStack` 属性和预期比对。方法见 `docs/handoff-test-supplement.md` 第 1 节 A。

## 7. 待确认问题（开工时先查清）

1. `DummyArmorMaterial` 的防御、耐久返回值，会不会让原版护甲属性叠加到匠魂数值上。
2. 格挡模型 `blocking_N` 的结构。
3. 布局 `icon` 能不能用物品而不用图案，护甲套布局里怎么写最省事。
4. 各手册护甲章节的 `load_tools` 路径。
5. 弓弩的 `_broken` 是否也有拉弓帧。
