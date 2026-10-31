# 第八期交付说明：新材料属性类型、工具回收、手册部件

## 完成内容

| 项目 | 结果 |
|---|---|
| 新材料属性类型 | 已实现：启动脚本注册属性类型，服务端脚本给新材料或匠魂已有材料写这类属性 |
| 工具回收（拆回部件） | 不需要开发：匠魂自带的通用回收配方已覆盖自定义工具 |
| 手册部件页 | 不需要开发：匠魂手册本身没有单独的部件页，部件出现的两处都已自动接入 |

## 新材料属性类型

### 启动脚本：注册属性类型（改了要重启）

```js
ArsenalMaterialEvents.statTypes(event => {
  event.create('kubejs:blade')
    .field('sharpness', 'attack_damage', 'base', 0)
    .field('balance', 'attack_speed', 'percent', 0)
    .field('durability', 'durability', 'base', 1)
    .textureLike('head')
    .name('zh_cn', '刃身')
})
```

| 方法 | 说明 |
|---|---|
| `event.create(id)` | 新属性类型的 ID，必须用自己的命名空间，不能用 `tconstruct:` |
| `.field(name, toolStat, operation, defaultValue)` | 按顺序添加字段。`name` 是材料数据里的字段名（小写字母、数字、下划线）；`toolStat` 是作用到的匠魂工具属性，不带命名空间时补 `tconstruct:`，必须是数值属性；`defaultValue` 是材料没写这个字段时的值 |
| `.textureLike(statType)` | 生成材料贴图时把这类部件当成哪种已有属性类型，默认 `head`。材料的贴图生成配置支持这种类型，就会给这类部件生成贴图 |
| `.name(lang, text)` | 属性类型名称（语言键 `stat.<命名空间>.<路径>`）；英文默认取 ID 路径 |

`operation` 的四种取值，和匠魂自带属性类型的做法一一对应：

| 取值 | 匠魂方法 | 含义 | 匠魂里的例子 |
|---|---|---|---|
| `base` | `update` | 部件提供的基础值，多个同类部件取平均 | 镐头的耐久、攻击 |
| `add` | `add` | 在基础值上加 | 握把的精准 |
| `percent` | `percent` | 按百分比加成，0.1 表示 +10% | 手柄的耐久、攻速 |
| `multiply` | `multiply` | 乘以倍数 | — |

部件在工具上的权重（`part_stats` 的倍率）照常生效：所有字段都会乘上这个缩放值，和匠魂自带属性类型一致。

材料提示里每个字段显示为对应工具属性的名称和数值；`add`、`percent`、`multiply` 用匠魂的彩色加成格式。

### 服务端脚本：给材料写属性（`/reload` 生效）

```js
ArsenalMaterialEvents.data(event => {
  // 新材料：和其他属性一样链式写
  event.create('kubejs:mithril')
    .head(800, 9, 'minecraft:diamond', 3)
    .customStat('kubejs:blade', 4, 0.1, 600)

  // 匠魂已有的材料：只追加属性，不改原有数据
  event.extend('tconstruct:iron')
    .customStat('kubejs:blade', 2, 0, 250)
    .statTrait('kubejs:blade', 'tconstruct:sharpness', 1)
})
```

| 方法 | 说明 |
|---|---|
| `.customStat(statType, ...values)` | 按启动脚本里 `.field` 的顺序填数值，个数必须一致；属性类型没注册或个数不对都会报错 |
| `event.extend(materialId)` | 给已有材料（匠魂或其他模组的）追加属性或按部件类型特性。匠魂会把同一材料的多份属性文件逐字段合并，所以原有属性不受影响。`extend` 可写属性：`.head`、`.customStat`、`.stat` 等，也可用 `.statTrait(stat, modifier, level)` 写 `perStat` 特性；`.trait(...)` 仍禁止，因为它会整体替换原材料的默认特性。 |

`statTrait` 写入的是 `perStat.<属性类型>`。匠魂对某一部件类型如果存在 `perStat` 特性，就直接使用这份列表，不再叠加 `default` 特性。因此铁的 blade 只写 `.statTrait('kubejs:blade', ...)` 后，该 blade 不会继承铁的默认“磁性”。如果扩展给已有的 `perStat` 属性类型再次写特性，后加载文件会整体替换该属性类型原来的特性列表；这是匠魂数据合并规则，脚本应避免无意覆盖。

新属性类型配合第七期的部件：

```js
// 启动脚本
event.create('kubejs:great_blade', 'tinkers_kubejs_arsenal:part')
  .statType('kubejs:blade')
  .cost(4)
  .casts()
```

只有写了 `kubejs:blade` 属性的材料才能做这个部件。

### 实现方式

- 启动脚本的定义在 Forge 通用初始化阶段注册到匠魂（`MaterialRegistry.registerStatType`），此时匠魂肯定已经初始化完。
- 每个属性类型生成一个 `MaterialStatType`，数据读写（JSON 和网络）按字段顺序处理；客户端也执行同一份启动脚本，所以能解码服务端同步过来的属性。
- `extend` 写入 `<命名空间>/tinkering/materials/stats/<路径>.json`，只包含追加的属性。

## 工具回收

匠魂自带配方 `tconstruct:tools/recycling/general`：在部件制作台放入"毒液瓶"作为图案，可以把 `tconstruct:modifiable/multipart` 标签里的任何工具拆回部件，部件列表从工具定义读取。自定义工具已自动加入这个标签，所以不需要另写配方。

不想让某把工具被拆，在服务端工具定义里加 `.modifiable('unrecyclable')`。

## 手册

匠魂手册没有单独的部件页，部件出现在两处，都已自动处理：

- 工具页的部件列表：从工具定义读取（第六期）；
- 材料页的"可制作部件"：从 `tconstruct:parts` 标签读取，第七期的部件已加入该标签。

## 文件

- `kubejs/material/StatTypeEventJS.java`：启动事件 `ArsenalMaterialEvents.statTypes` 和属性类型链式定义。
- `kubejs/material/ScriptMaterialStats.java`：脚本属性类型的材料属性实现、数据读写、注册到匠魂。
- `kubejs/material/MaterialDataEventJS.java`：新增 `.customStat`、`event.extend`。
- `kubejs/material/MaterialDataEventJS.java`：`extend` 支持 `.statTrait`，并保留 `.trait` 的默认特性替换保护。
- `kubejs/material/ArsenalMaterialEvents.java`：事件组新增 `statTypes`。
- `kubejs/ArsenalKubeJSPlugin.java`：启动脚本加载后发布 `statTypes`；生成属性类型名称语言。
- `TinkersKubejsArsenal.java`：通用初始化时注册属性类型。
- `mixin/ServerScriptManagerMixin.java`：写入 `extend` 的属性文件。
- `client/GeneratedMaterialSpriteSource.java`：新属性类型的部件按 `.textureLike` 判断是否生成贴图。
- `kubejs/probe/ArsenalProbeJSPlugin.java`：补全 `operation`，加入属性类型定义类。
- `docs/test-phase6-8.md`：记录第六至八期服务端、客户端和联机资源同步测试结果。

## 验证

- `gradlew compileJava`：通过。
- 游戏内测试结果见 [test-phase6-8.md](test-phase6-8.md)。

## 限制

- 手册材料页按匠魂写死的属性类型分页（近战/采掘、远程、护甲、弹药），新属性类型的数值不会出现在材料页里，只在物品提示和工匠台中显示。
