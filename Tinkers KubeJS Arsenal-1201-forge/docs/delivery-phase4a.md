# 第四期材料接口交付说明（4A）

## 已完成文件

- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/MaterialDataEventJS.java`：服务端材料定义、23 个属性方法、trait/statTrait、字段校验。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/MaterialAssetEventJS.java`：客户端渲染、颜色、后备贴图和语言数据。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/ArsenalMaterialEvents.java`：`ArsenalMaterialEvents.data/assets` 事件组。
- `ArsenalKubeJSPlugin.java`：事件绑定及 KubeJS 资源/语言生成。
- `run/kubejs/server_scripts/tkja_material_test.js`、`client_scripts/tkja_material_test.js`：覆盖接口和错误校验的测试脚本。

## 属性对照

`head`、`handle`、`grip`、`limb`、`platingHelmet`、`platingChestplate`、`platingLeggings`、`platingBoots`、`platingShield`、`skull`、`slime`、`ribcage`、`shell`、`laces` 对应 TC 的同名或蛇形 stat；`binding`、`bowstring`、`maille`、`shieldCore`、`repairKit`、`cuirass`、`arrowHead`、`arrowShaft`、`fletching` 对应 `StatlessMaterialStats` 的九个无字段类型。该清单与当前 8809959 源码的注册项一致。

## 校验

对象字段在写入前按 stat 的允许字段集合检查，必填字段与耐久、修复量、overslime 下限也会检查；第三方属性使用 `.stat`。内置 23 种属性传给 `.stat` 会抛出错误。颜色只接受 6/8 位十六进制，亮度限制为 0～15，稀有度限制为四个 TC 枚举名。

## 资源路径

服务器生成 `data/<ns>/tinkering/materials/{definition,stats,traits}/<path>.json`，客户端生成 `assets/<ns>/tinkering/materials/<path>.json` 与 `assets/mantle/colors.json`。语言通过 `LangEventJS.Key` 写入，不覆盖用户的其他语言事件。

## 验证

`gradlew.bat compileJava --no-daemon` 已通过。已加入 `ServerScriptManagerMixin`，在 `HIGH_DATA` 发布调用处 ordinal=1 使用 MixinExtras `@Local(ordinal=1)` 取得 high 包，并写入材料 JSON；客户端资源仍由 `generateAssetJsons/generateLang` 生成。服务端启动测试按用户要求暂缓。

## 本次补全

已加入 ServerScriptManager 高优先级数据包注入、TinkerHarvestTier ProbeJS 特殊类型及 F3+/reload 注入路径。机械比对结果：23 个属性方法，missing=none，extra=none。未启动服务端测试（按用户要求暂缓）。


## 审核修复记录（2026年10月30日，主对话）

服务端实测（子代理，含一次 `/reload`）之前，以下问题已全部修复：

| 编号 | 问题 | 修复 |
|---|---|---|
| F1 | 用 MixinExtras `@Local` 编译（0.4.1），运行环境里是其他模组带的 0.2.0-rc.2，启动时 KubeJS 加载崩溃；`build.gradle` 写死了本机 Gradle 缓存的 jar 绝对路径 | 改用原生 `@ModifyArg` 拿到 `highPriorityData` 的 `DataPackEventJS`，用 `addJson` 写入；删掉 jar 路径 |
| F2 | 没有脚本监听 `highPriorityData` 时，`EventHandler.post` 直接返回、不调用 `afterPosted`，虚拟包不会挂进资源管理器，TC 读不到材料 | 在 post 之后注入：没有监听者时通过 `DataPackEventJSInvoker` 手动调用 `afterPosted` |
| F3 | 属性文件缺少外层 `{"stats": {...}}`，且属性键不带命名空间（会被解析成 `minecraft:head`） | 外层补上 `stats`，键统一为 `tconstruct:<id>` |
| F4 | 耐久下限 1 的校验对所有属性生效，手柄、握把的耐久是倍率（如 0.1），会抛错 | 按 TC `LOADABLE` 逐字段校验类型（整数/数值/挖掘等级）和范围 |
| F5 | 稀有度以小写写出，TC 用 Gson 解析枚举只认大写，会被静默忽略 | 改为大写 |
| F6 | 客户端 `generator.json` 会自动补 `.json`，代码又加了一次，路径变成 `.json.json`；文字颜色写到 `assets/mantle/colors.json`，且没有 `#` | 去掉多余后缀；颜色按命名空间写到 `assets/<ns>/mantle/colors.json`，值为 `#RRGGBB` |
| F7 | 残留的 `generateDataJsons` 在脚本重载前执行，拿到的是上一轮的数据 | 删除 |
| F8 | `generateLang` 再发布一次客户端事件 | 复用 `generateAssetJsons` 的结果 |
| F9 | 定义中途抛错的材料仍会写出残缺数据 | 标记为无效并跳过，打 ERROR 日志；没写属性的材料打 WARN |
| F10 | `.stat('head', ...)` 不带命名空间时绕过拦截 | 不带命名空间也按 TC 自带属性拦截 |
| F11 | ProbeJS 只加了挖掘等级，且用 `Tier.toString()` 取值 | 改用 `TierSortingRegistry.getName`；补上 23 个属性对象字段、稀有度、后备贴图（从 TC 全部渲染信息统计出 13 个）、语言、`statTrait` 的属性 ID 与 4 个分组、`modifier` |

补充文档第 10 节缺少的内容：

- **perStat 分组 ID：** `tconstruct:melee_harvest`、`tconstruct:ranged`、`tconstruct:armor`、`tconstruct:ammo`。出处是 `MaterialRegistry` 第 42～45 行，以及第 98～120 行 `registerStatType` 的第二个参数。实测：`statTrait('tconstruct:armor', ...)` 后，`plating_chestplate` 取到了该特性。
- **Mixin 注入点：** `ServerScriptManager#wrapResourceManager` 中第 2 个 `EventHandler.post(ScriptTypeHolder, EventJS)` 调用（`ordinal = 1`，即 `HIGH_DATA`）。在它之前用 `@ModifyArg` 写入数据，之后用 `@Inject(shift = AFTER)` 补调 `afterPosted`。
- **未知字段检查：** 用的是手写字段表，没有采用文档建议的 Loadable 方式。字段表已用脚本与 TC 各 stats 类的 `LOADABLE` 字段逐项核对，结果一致。
- **首次进游戏时客户端脚本是否已加载：** 未验证，需要客户端测试。

## 实测结果（服务端）

- 启动后：`phase4_all` 的 23 种属性数值与脚本一致；`phase4_head` 的头部耐久为 80。TC 属性总数从 644 变为 668（+24）。
- 默认特性 `magnetic` 生效；`armor` 分组特性分到了 `plating_chestplate`。
- 4 个错误用例都抛出英文异常，对应材料没有生成。
- 把耐久改为 81 后执行 `/reload`，再次查询得到 81。
- 客户端部分（颜色、名称、`F3+T`）未测试。
