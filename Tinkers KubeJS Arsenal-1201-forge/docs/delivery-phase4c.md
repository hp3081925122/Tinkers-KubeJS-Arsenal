# 第四期材料接口交付说明（4C）

## 完成内容

4C 为客户端补上了运行时部件贴图生成。资源重载时，模组会把一个自定义 `SpriteSource` 加到 Minecraft 的 `minecraft:blocks` 图集加载器中，然后复用匠魂 8809959 的部件贴图生成配置和原生调色板变换器。

本阶段只增加客户端资源处理，不改变服务端材料数据、材料属性、材料特性或材料标签。

## 实现文件

- `src/main/java/com/hp/tinkers_kubejs_arsenal/client/GeneratedMaterialSpriteSource.java`
  - 读取 `tconstruct:tinkering/generator_part_textures.json`；
  - 扫描所有命名空间的 `tinkering/materials/*.json`；
  - 解析材料渲染 JSON 中的 `generator`、`fallbacks`、`texture` 和变体字段；
  - 依据 `supported_stats`、`ignore_material_stats`、`skip_variants` 和部件的 `allow_animated` 决定是否生成；
  - 调用 TC 的 `ISpriteTransformer` 生成调色板贴图，并读取动画元数据；
  - 直接把内存中的 `NativeImage` 注册为 Sprite，不写入磁盘 PNG。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/mixin/SpriteResourceLoaderMixin.java`
  - 在 `SpriteResourceLoader.load` 返回后，把运行时生成源追加到方块图集的 SpriteSource 列表。
- `src/main/resources/tinkers_kubejs_arsenal.mixins.json`
  - 增加客户端 mixin 列表，不让服务端加载客户端贴图类。

## 生成规则

部件定义来自 TC 的 `generator_part_textures.json`。对于每个材料渲染 JSON：

1. 使用显式 `texture`，没有时根据材料资源路径推导纹理后缀；变体路径 `materials/<base>/<variant>.json` 会转换为 `<base>_<variant>`。
2. 按材料 `fallbacks` 顺序查找 `textures/<part>_<fallback>.png`，找不到时再查找基础部件贴图 `textures/<part>.png`。
3. 先检查输出贴图是否已经由资源包提供。已有显式 PNG 时跳过生成，保持资源包覆盖优先级。
4. 复用 TC 的 `MaterialGeneratorInfo` 和 `ISpriteTransformer`，因此灰度到调色板的映射、动画帧数和 `.png.mcmeta` 动画参数与 TC 数据生成逻辑一致。
5. 输出 ID 使用 TC 的命名规则：原版命名空间使用路径，其他命名空间使用 `namespace_path`，例如 `tconstruct:item/tool/pickaxe/head_kubejs_phase4_all_polished`。

生成结果只存在于本次图集加载的内存中。执行 `F3+T` 时，客户端资源包重新生成，SpriteSource 会再次读取材料资源并生成新的 Sprite。

## 与 4B 的关系

4B 的 `.generator(...)`、`.generator(Map)` 和 `.generatorPalette(Map)` 负责把生成配置写入材料渲染 JSON；4C 负责在客户端图集加载阶段实际消费这些配置。没有 `generator` 的材料不会被本阶段处理，仍使用普通颜色着色和后备贴图逻辑。

4C 同时支持 4B 的 `#variant` 资源路径、`skip_variants` 和 `supported_stats` 规则。材料标签、服务端材料属性和特性仍以基础材料 ID 为准。

## 验证

- `gradlew.bat compileJava --no-daemon`：通过。
- `gradlew.bat runClient --no-daemon`：客户端完成启动和方块图集加载；Mixin 日志确认 `SpriteResourceLoaderMixin` 已应用。
- `run/logs/debug.log`：出现 `Generated 93 material part sprites`，且未再出现动画变换器的 `Cannot get image for a sprite without reader` 异常。
- 客户端材料渲染信息日志包含 `kubejs:phase4_all`、`kubejs:phase4_all#polished` 和 `kubejs:phase4_head`，说明 4B 测试材料已进入本次资源重载。
- 4B 已完成的 `runServer` 验证仍适用；4C 只注册客户端 mixin，不向服务端类路径引入客户端贴图逻辑。

## 限制

- 当前只把生成源追加到 `minecraft:blocks` 图集，这是 TC 部件贴图所在的图集；其他图集不处理。
- 生成贴图不落盘，客户端重启或资源重载时会重新计算；大量材料会增加资源重载时间和内存占用。
- 服务端材料外观同步已在 4D 完成，详见 [delivery-phase4d.md](delivery-phase4d.md)。
