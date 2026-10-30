# 第四期实施文档：用 KubeJS 定义匠魂材料

> 编写日期：2026年10月30日
> 前置阅读（按顺序）：
> 1. [phase2-3-plan.md](phase2-3-plan.md) 第 2 节：硬性规则 R1～R11 在本期**继续有效**（R1～R4 针对配方组件，本期只在涉及配方时适用）
> 2. [kubejs-api-style.md](kubejs-api-style.md)：链式写法规范 S1～S14，本期所有 JS 接口都必须遵守
> 3. 当前代码：`src/main/java/com/hp/tinkers_kubejs_arsenal/**`
>
> 仓库：https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal （`main` 分支）
> 匠魂版本：curse.maven `tinkers-construct-74072:8809959`（以 `gradle.properties` 为准）。Gradle 缓存里还有一个旧版 jar（6044827），**不要拿它当参考**，两者的材料数据不同。

## 1. 目标

让整合包作者只写 JS 就能新增一种完整可用的匠魂材料，并且：

- 服务端数据（材料定义、属性、特性）改完后执行 `/reload` 立即生效；
- 客户端外观（颜色、渲染信息、名称）改完后按 `F3+T` 立即生效；
- 写脚本时 ProbeJS 能补全材料属性的每个字段、属性 ID、词条 ID 和后备贴图名。

熔化、浇铸、材料物品这些配方已经能用现有的配方接口写，本期**不重复提供**（S1）。

## 2. 匠魂材料由哪些部分组成（已对照 8809959 jar 和源码核对）

| 部分 | 位置 | 加载者 | 客户端怎么拿到 |
|---|---|---|---|
| 材料定义 | `data/<ns>/tinkering/materials/definition/<path>.json` | `MaterialManager` | 网络包 `UpdateMaterialsPacket` |
| 材料属性 | `data/<ns>/tinkering/materials/stats/<path>.json` | `MaterialStatsManager`（合并型加载器） | `UpdateMaterialStatsPacket` |
| 材料特性 | `data/<ns>/tinkering/materials/traits/<path>.json` | `MaterialTraitsManager`（合并型加载器） | `UpdateMaterialTraitsPacket` |
| 渲染信息 | `assets/<ns>/tinkering/materials/<path>.json` | `MaterialRenderInfoLoader` | 纯客户端资源 |
| 文字颜色 | `assets/<ns>/mantle/colors.json`，键 `material.<ns>.<path>` | Mantle `ResourceColorManager` | 纯客户端资源 |
| 语言 | `material.<ns>.<path>`、`.flavor`、`.encyclopedia` | 原版语言系统 | 纯客户端资源 |

材料 ID 由文件路径决定，例如 `kubejs:mythril` 对应 `data/kubejs/tinkering/materials/definition/mythril.json`。

### 2.1 材料定义字段（`MaterialJson` + `MaterialManager.loadMaterial`）

| 字段 | 类型 | 缺省时 |
|---|---|---|
| `craftable` | boolean | **缺少就报错，材料被跳过**，所以本模组必须总是写出它 |
| `tier` | int | 0 |
| `sortOrder` | int | 100 |
| `hidden` | boolean | false |
| `rarity` | 稀有度枚举名 | 按阶级计算：≥5 史诗，4 稀有，3 罕见，其余普通 |
| `condition`、`redirect` | Forge 条件、重定向 | 本期不支持（见第 7 节） |

### 2.2 材料属性类型（`MaterialRegistry` 第 98～120 行，共 23 种）

| 属性 ID（`tconstruct:` 省略） | 字段（**加粗为必填**） |
|---|---|
| `head` | **`durability`**(int≥1)、`mining_speed`(float，默认 1)、`mining_tier`(挖掘等级 ID，默认 `minecraft:wood`)、`melee_attack`(float，默认 1) |
| `handle` | `durability`、`mining_speed`、`melee_speed`、`melee_damage`，都是 float 倍率，默认 0（0.1 表示 +10%） |
| `grip` | `durability`(倍率)、`accuracy`、`melee_damage`(≥0)，float，默认 0 |
| `limb` | **`durability`**(int≥1)、`draw_speed`、`velocity`、`accuracy`(float，默认 0) |
| `plating_helmet` / `plating_chestplate` / `plating_leggings` / `plating_boots` | **`durability`**、`armor`、`toughness`、`knockback_resistance`(float，默认 0) |
| `plating_shield` | **`durability`**、`toughness`、`knockback_resistance`（没有 `armor`） |
| `skull` | **`durability`**、`armor`(int≥0，默认 0) |
| `slime` | **`durability`**、**`overslime_capacity`**(int≥0) |
| `ribcage` / `shell` / `laces` | **`repair_amount`**(int≥1) |
| `binding`、`bowstring`、`maille`、`shield_core`、`repair_kit`、`cuirass`、`arrow_head`、`arrow_shaft`、`fletching` | 没有字段，写空对象 `{}` |

“写了这个属性类型”就等于“这个材料能做对应部件”。没写的属性类型，就做不了对应部件。

### 2.3 材料特性（`MaterialTraitsJson`）

```json
{ "default": [{ "name": "tconstruct:magnetic", "level": 1 }],
  "perStat": { "tconstruct:armor": [{ "name": "tconstruct:projectile_protection", "level": 1 }] } }
```

- 键名是驼峰的 `perStat`。
- **某个属性类型只要出现在 `perStat` 里，就完全替换 `default`，不会合并**（`MaterialTraits.getTraits` 用的是 `getOrDefault`）。
- `perStat` 的键还可以是属性分组，例如铁用到了 `tconstruct:armor`，它不在第 2.2 节的 23 种里。实施时要查清 TC 支持哪些分组 ID，并列进交付说明。

### 2.4 渲染与贴图（客户端）

- 渲染信息字段：`texture`（可选）、`fallbacks`（字符串数组）、`color`（ARGB 或 RGB 十六进制）、`luminosity`（0～15）。
- **TC 没有在运行时按调色板生成部件贴图的机制。** `/tconstruct generate part_textures` 是手动命令，它把贴图写进资源包目录。
- 但是 `MaterialRenderInfo.getSprite` 找不到材料专属贴图时，会依次尝试 `fallbacks` 指定的后备贴图（如 `head_metal`），再退回基础贴图，并用 `color` 着色。TC jar 里自带这些灰度后备贴图，后缀有 `metal`、`wood`、`rock`、`bone`、`crystal`、`slime_metal`、`slime_wood`、`primitive`、`stick` 等。所以**新材料只写颜色和后备贴图就能正常显示**，效果是整体着色，不如 TC 自带材料那样按调色板精细上色。精细上色放在第 4C 阶段。

## 3. 关键设计：热重载怎么接

### 3.1 服务端：`/reload`

已核对的 KubeJS 2001.6.5 时序（`ServerScriptManager.wrapResourceManager`），每次加载世界和每次 `/reload` 都会执行：

1. `new GeneratedServerResourcePack()`：这一步调用插件的 `generateDataJsons`，**这时服务端脚本还没重载**；
2. `this.reload(...)`：重新加载 server_scripts，同时清空所有事件监听；
3. 发布 `ServerEvents.lowPriorityData` 和 `ServerEvents.highPriorityData`，把内容写进两个 `VirtualKubeJSDataPack`；
4. 调用插件的 `onServerReload`；
5. 返回资源管理器，之后匠魂的三个管理器读取数据。

结论：不能用 `generateDataJsons`，因为它拿到的是上一次的数据；也不能在 `onServerReload` 里注册 Java 监听，因为下次重载第 2 步会把它清掉，而且时机已经晚于第 3 步。

**做法：** 写一个 Mixin，注入 `dev.latvian.mods.kubejs.server.ServerScriptManager#wrapResourceManager`，位置在第 3 步发布 `highPriorityData` 之后、`ConsoleJS.SERVER.info("Scripts loaded")` 之前（`remap = false`）。在注入点：

- 用 MixinExtras 的 `@Local` 拿到 high 优先级的那个 `VirtualKubeJSDataPack`（同一个方法里有两个同类型局部变量，必须用 `ordinal` 区分 high 和 low；开发环境里有 `mixinextras-forge 0.4.1`，实施前要先确认它在编译类路径上，不在就改用 `LocalCapture`）；
- 发布本模组的服务端事件 `TinkerMaterialEvents.data`，收集脚本里定义的材料；
- 对每种材料调用 `VirtualKubeJSDataPack.addData(ResourceLocation, String)` 写入三个 JSON。`addData` 的 id 参数**不带 `data/` 前缀**，例如 `kubejs:tinkering/materials/stats/mythril.json`。

写进 high 包的好处：优先级最高，可以覆盖 TC 或其他模组的同名材料。属性和特性是合并型加载器，所以“覆盖原版材料的部分属性”也能做到。这个能力写进交付说明，但不额外提供接口。

Mixin 写在已有的 `tinkers_kubejs_arsenal.mixins.json` 里。KubeJS 是本模组的必需依赖（已写入 `mods.toml`），所以不需要做条件加载。

### 3.2 客户端：`F3+T`

已核对的时序：

- `KeyboardHandlerMixin` 在按下 `F3+T`、调用 `reloadResourcePacks()` 之前，先执行 `KubeJSClient.reloadClientScripts()`；
- 随后 `GeneratedClientResourcePack` 被重新创建，其 `generate()` 依次调用插件的 `generateAssetJsons`、`generateLang`，再发布 `ClientEvents.highPriorityAssets`。

所以客户端**不需要 Mixin**：在插件的 `generateAssetJsons` 里发布本模组的客户端事件 `TinkerMaterialEvents.assets`，生成渲染信息和 `mantle/colors.json`；在 `generateLang` 里写入语言。

注意：

- `generateLang` 只给出 `en_us` 的 `LangEventJS`，但它的 `map` 是所有语言共用的 `Map<LangEventJS.Key, String>`，`Key` 里带有语言。所以其他语言直接 `map.put(new LangEventJS.Key(ns, "zh_cn", key), value)`，**不要**自己写 `lang/zh_cn.json` 文件，否则会和用户的 `ClientEvents.lang` 冲突。
- 客户端事件在 `generateAssetJsons` 里发布一次，结果缓存起来，供同一轮 `generateLang` 使用，不要发布两次。
- 首次进游戏时，客户端脚本是否已在第一次生成资源包之前加载，**实施时要验证**。如果没有，就要找一个更早的时机，不能只靠 `F3+T`。

### 3.3 为什么分成两个事件

服务端数据和客户端外观必须分开写：多人游戏里客户端不执行 server_scripts，服务器也不执行 client_scripts。单人游戏两边都会执行。

代价是同一个材料 ID 要在两个脚本里各写一次。合并成一处定义需要服务器把外观同步给客户端，这放在第 4D 阶段。

## 4. 分阶段

| 阶段 | 内容 | 是否本期必做 |
|---|---|---|
| **4A** | 第 5、6 节的服务端和客户端接口、`/reload` 和 `F3+T` 热重载、ProbeJS 补全、校验、测试脚本 | **必做** |
| 4B | 材料变体（`kubejs:mythril#polished`）、材料标签（`tinkering/tags/materials`）、写出 `generator` 字段，让作者能用 TC 的 `/tconstruct generate part_textures` 生成精细贴图 | 4A 验收后再做 |
| 4C | 运行时按调色板生成部件贴图（注册 SpriteSource，读取 TC 的 `generator_part_textures.json`）。工作量大，需要单独评估 | 待定 |
| 4D | 服务器把外观（颜色、名称、渲染信息）同步给客户端，一处定义两边生效 | 待定 |

## 5. 服务端接口（4A）

### 5.1 写法

```js
// server_scripts，改完执行 /reload
TinkerMaterialEvents.data(event => {
  event.create('kubejs:mythril')
    .tier(3)
    .craftable(false)            // 可省略，默认 false；TC 要求必须写出，本模组总是写出
    .sortOrder(5)                // 可省略，默认 100
    .hidden(false)               // 可省略
    .rarity('rare')              // 可省略，默认按 tier 计算
    .head({ durability: 600, mining_speed: 7.5, mining_tier: 'minecraft:diamond', melee_attack: 3 })
    .handle({ durability: 0.1, melee_speed: 0.05 })
    .binding()
    .limb({ durability: 600, draw_speed: -0.1, velocity: 0.1 })
    .grip({ durability: 0.1, accuracy: 0.05, melee_damage: 2 })
    .bowstring()
    .platingChestplate({ durability: 300, armor: 6, toughness: 1 })
    .maille()
    .trait('tconstruct:magnetic')                              // 默认特性，等级 1
    .trait('tconstruct:sharpness', 2)                          // 默认特性，等级 2
    .statTrait('tconstruct:armor', 'tconstruct:projectile_protection', 1)
})
```

### 5.2 方法表

| 方法 | 写入 | 说明 |
|---|---|---|
| `event.create(id)` | | S2：唯一的构造参数。同一次重载里重复创建同一个 ID 时抛错 |
| `.tier(int)` `.sortOrder(int)` `.craftable(boolean)` `.hidden(boolean)` | definition | |
| `.rarity(String)` | definition | 只接受 `common`/`uncommon`/`rare`/`epic`，其他值抛错 |
| 23 个属性方法（见 5.3） | stats | 有字段的属性类型收一个对象参数；无字段的属性类型不收参数 |
| `.stat(String statId, Map values)` | stats | **只用于第三方模组注册的属性类型**；传入 TC 自带的 23 种之一时抛错，提示改用专用方法（S1） |
| `.trait(String modifier)` / `.trait(String modifier, int level)` | traits.default | 追加（S5） |
| `.statTrait(String stat, String modifier, int level)` | traits.perStat | 追加；**交付说明和 JS 注释都要写明：写了 perStat 的属性类型不再继承 default** |

### 5.3 属性方法名

方法名是属性 ID 的驼峰形式：`head`、`handle`、`grip`、`limb`、`platingHelmet`、`platingChestplate`、`platingLeggings`、`platingBoots`、`platingShield`、`skull`、`slime`、`ribcage`、`shell`、`laces`（以上 14 个收对象参数），以及 `binding`、`bowstring`、`maille`、`shieldCore`、`repairKit`、`cuirass`、`arrowHead`、`arrowShaft`、`fletching`（以上 9 个无参数），共 23 个，**必须和 `MaterialRegistry` 的注册项一一对应**，交付说明里列出对照表。

对象参数的要求：

- 键名直接用 TC 的 JSON 字段名（蛇形，例如 `mining_speed`），不另起名字，避免和 TC 文档对不上；
- 写入前按 TC 的 `LOADABLE` 校验：未知字段名抛错（防止拼错字段名后被静默忽略），缺少必填字段抛错，数值超出范围（如 `durability < 1`）抛错；
- 校验逻辑**不要手写字段表**：直接用对应属性类型的 `MaterialStatType.getLoadable()`（或 TC 中等价的接口，实施前核对）对 JSON 做一次 `deserialize`，失败时把 TC 的异常信息包装成英文的 `IllegalArgumentException` 抛出。未知字段名 TC 不会报错，这一项需要单独检查：用该 Loadable 序列化默认值得到合法键集合，或用其他可靠办法得到合法键集合，实施时说明用了哪种。

### 5.4 生成规则

- 每次重载只写出本次脚本里定义的材料。删掉脚本里的定义后执行 `/reload`，材料就消失，这是预期行为。
- 没有调用任何特性方法时，不写 traits 文件。
- 没有调用任何属性方法时，照样写出 definition，同时在 `ConsoleJS.SERVER` 打一条英文警告：这样的材料做不了任何部件。
- 同一个 ID 被 TC 或其他模组的数据包定义过时，high 包会覆盖它。打一条 info 日志说明发生了覆盖（检测方法：在资源管理器里查同路径的已有资源）。

## 6. 客户端接口（4A）

```js
// client_scripts，改完按 F3+T
TinkerMaterialEvents.assets(event => {
  event.create('kubejs:mythril')
    .color('6FD0E8')                 // 渲染着色；也是默认的文字颜色
    .fallbacks('metal')              // 可变参数，按顺序尝试
    .luminosity(0)                   // 可省略
    .texture('kubejs:mythril')       // 可省略；有自己画的专属贴图时才写
    .textColor('7FE0F8')             // 可省略；提示框文字颜色，默认等于 color
    .name('Mythril')                 // en_us
    .name('zh_cn', '秘银')
    .flavor('zh_cn', '据说是精灵打造的金属。')
    .encyclopedia('zh_cn', '……')
})
```

| 方法 | 写入 |
|---|---|
| `.color(String)` | 渲染信息 `color`。接受 6 位或 8 位十六进制，可带 `#`，其他格式抛错 |
| `.fallbacks(String...)` | 渲染信息 `fallbacks` |
| `.luminosity(int)` | 0～15，超范围抛错 |
| `.texture(String)` | 渲染信息 `texture` |
| `.textColor(String)` | `assets/<ns>/mantle/colors.json` 里的 `material.<ns>.<path>`，格式 `#RRGGBB` |
| `.name(String text)` / `.name(String lang, String text)` | `material.<ns>.<path>`。参数个数不同，不违反 S14 |
| `.flavor(...)` / `.encyclopedia(...)` | `.flavor` / `.encyclopedia` 后缀，重载方式同 `.name` |

- 没写 `.name` 时不生成语言条目。TC 会显示翻译键，这样作者能发现自己漏写了名称。
- 没写 `.color` 时不写 `color` 字段（TC 默认白色），也不写文字颜色。
- `mantle/colors.json` 每个命名空间只有一个文件。同一命名空间下的所有材料要合并写进同一个 JSON；KubeJS 生成包里同一路径只能放一个文件。

## 7. 本期不做

- 材料定义的 `condition`、`redirect`：脚本本身就能用 `if` 判断模组是否加载，不需要条件。
- 材料变体、材料标签、精细贴图：放在 4B、4C。
- 材料物品、熔化、浇铸配方的快捷写法：已有配方接口，不重复提供（S1）。

## 8. ProbeJS 补全

沿用 `ArsenalProbeJSPlugin` 的做法：

- 对象参数按**参数名**补全类型（S11）。23 种属性方法的参数名分别是 `headStats`、`handleStats`……（具体名单在交付说明里列出），在 `PARAM_TYPES` 里登记对应的对象类型，例如 `headStats` 登记为 `{durability: number, mining_speed?: number, mining_tier?: Special.TinkerHarvestTier, melee_attack?: number}`。
- 新增特殊类型 `Special.TinkerHarvestTier`：挖掘等级 ID，从 Forge `TierSortingRegistry.getSortedTiers()` 取 ID，封闭列表。
- `statTrait` 的 `stat` 参数：用已有的 `Special.TinkerMaterialStat`，加上 `perStat` 支持的分组 ID（第 2.3 节）。
- `trait`、`statTrait` 的 `modifier` 参数：用已有的 `Special.TinkerModifier`。
- `.fallbacks` 的参数：后备贴图后缀的字面量联合类型，再加 `(string & {})`。后缀列表**从 TC jar 的贴图文件名里统计**，不要手写。统计方法写进交付说明。
- `.rarity` 的参数：四个稀有度字面量。
- `event.create` 的 `id` 参数：任意字符串，不补全。

参数名一律要求有意义：编译已开启 `-parameters`。

## 9. 测试脚本（交付时一起提供，放在 `run/kubejs/`）

1. `server_scripts/tkja_material_test.js`：
   - 定义一种用到全部 23 个属性方法的材料，一种只有 head 的材料，一种用 `statTrait` 的材料；
   - 用 try/catch 测四个错误用例：未知字段名、缺少必填字段、`.stat` 传入 TC 自带属性类型、非法稀有度；
   - 在 `ServerEvents.loaded` 里通过 `MaterialRegistry.getMaterial(...)`、`getMaterialStats(...)`、`getTraits(...)` 打印实际加载结果，日志前缀 `[TKJA_MAT]`。
2. `client_scripts/tkja_material_test.js`：给上面的材料写外观和中英文名称。
3. 不要用 `const` 在循环体里声明变量（Rhino 会报重复声明），循环体里用 `let`。

## 10. 交付说明格式

在 `docs/` 下新建 `delivery-phase4a.md`，内容包括：

1. 新增和修改的文件列表；
2. 第 5.3 节 23 个属性方法与 `MaterialRegistry` 注册项的对照表；
3. `perStat` 支持的分组 ID 列表，以及在源码里的出处；
4. Mixin 的注入点（目标方法、`@At`、局部变量序号），以及 KubeJS 源码对应的行号；
5. 第 3.2 节“首次进游戏时客户端脚本是否已加载”的验证结论；
6. 未知字段检查用的是哪种办法；
7. 和本文档不一致的地方，以及以源码为准的依据；
8. `gradlew build` 的结果。

## 11. 审核方式（交付后由主对话执行）

1. 用脚本对比 23 个属性方法和 `MaterialRegistry` 的注册清单，再对比每个属性类型的合法字段和 TC `LOADABLE` 的字段（全量机械比对，不抽查）。
2. 检查 Mixin：注入点是否在 `highPriorityData` 之后，`@Local` 拿到的是不是 high 包。
3. 服务端测试（交给子代理）：启动服务端，检查 `[TKJA_MAT]` 日志；修改测试脚本里的耐久，执行 `/reload` 后再检查一次日志，确认数值变了。
4. 客户端测试由用户决定是否进行。如果要测，建议的测试项：
   - 部件加工台和浇铸台上能看到新材料，显示中文名称和颜色；
   - 改了 `.color` 之后按 `F3+T`，颜色立即变化；
   - 改了服务端脚本里的耐久之后执行 `/reload`，工具提示里的数值立即变化。
