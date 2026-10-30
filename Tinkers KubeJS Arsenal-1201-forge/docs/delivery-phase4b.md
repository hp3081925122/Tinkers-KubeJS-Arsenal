# 第四期材料接口交付说明（4B）

## 完成内容

4B 已在 4A 的材料接口上补齐三部分能力：

- 客户端材料变体：`kubejs:mythril#polished`；
- 服务端材料标签：`data/<ns>/tinkering/tags/materials/<path>.json`；
- 客户端 `generator` 字段，生成 TC `/tconstruct generate part_textures` 所需的渲染配置。

本阶段没有把变体伪装成服务端材料 ID。TC 的 `MaterialVariantId` 会把 `base#variant` 解析成基础材料和变体，材料属性、特性以及材料标签仍然使用基础材料 ID。

## 文件变更

- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/MaterialAssetEventJS.java`
  - `create` 支持 `base#variant`；
  - 变体资源写到 `materials/<base>/<variant>.json`；
  - 变体语言键为 `material.<namespace>.<base>.<variant>`；
  - 新增 `generator(Map)`、`generator(String...)`、`generatorPalette(Map)`。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/MaterialTagEventJS.java`
  - 新增材料标签事件和 `add`、`addOptional`、`addAll`、`replace` 链式 API。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/ArsenalMaterialEvents.java`
  - 注册 `ArsenalMaterialEvents.tags` 服务端事件。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/mixin/ServerScriptManagerMixin.java`
  - 在 high priority 数据包中写入 `tinkering/tags/materials`。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/ArsenalKubeJSPlugin.java`
  - 使用变体资源路径和变体语言键生成客户端资源。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/probe/ArsenalProbeJSPlugin.java`
  - 增加 `supportedStats`、`generator`、`palette`、`material` 参数类型。
- `run/kubejs/server_scripts/tkja_material_test.js`
  - 增加材料标签测试。
- `run/kubejs/client_scripts/tkja_material_test.js`
  - 增加 `phase4_all#polished` 变体和 generator 测试。

## JS 接口

### 材料变体

```js
ArsenalMaterialEvents.assets(event => {
  event.create('kubejs:phase4_all#polished')
    .color('80E0FF')
    .fallbacks('metal')
    .name('Polished Phase 4 Material')
})
```

生成路径为 `assets/kubejs/tinkering/materials/phase4_all/polished.json`。变体名称和 Mantle 颜色键使用 `material.kubejs.phase4_all.polished`。

### 材料标签

```js
ArsenalMaterialEvents.tags(event => {
  event.create('kubejs:test_materials')
    .add('kubejs:phase4_all')
    .addOptional('kubejs:phase4_head')
})
```

生成的 JSON 使用 TC 原生标签格式。`add` 写字符串值，`addOptional` 写 `{id, required:false}`，`replace(true)` 才会写出 `replace: true`。标签成员禁止使用 `#variant`，因为 TC 材料标签加载器匹配基础材料 ID。

由于 Rhino 会把单字符串调用和 Java 可变参数重载判定为歧义，批量添加方法命名为 `addAll(String...)`，例如 `.addAll('kubejs:a', 'kubejs:b')`。

### generator

```js
event.create('kubejs:phase4_all#polished')
  .color('80E0FF')
  .generator('tconstruct:head', 'tconstruct:handle')
```

字符串重载会生成 TC 的 `recolor_sprite` + `grey_to_color` 配置，并根据 `.color(...)` 生成默认调色板。需要精确控制时可传入原始对象，或在其后调用：

```js
event.create('kubejs:custom#polished')
  .generator({
    supported_stats: ['tconstruct:head'],
    transformer: {
      type: 'tconstruct:recolor_sprite',
      color_mapping: { type: 'tconstruct:grey_to_color', palette: {} }
    }
  })
  .generatorPalette({ '063': 'FF102030', '255': 'FFE0F0FF' })
```

`generator(Map)` 要求 `transformer` 和 `supported_stats` 两个字段；变体会自动补 `variant: true`。这与 8809959 版本 TC 的 `MaterialGeneratorInfo.LOADABLE` 字段一致。generator 只写出 TC 生成精细部件贴图需要的描述，不会在运行时自动生成 PNG；生成贴图仍需执行 TC 命令。

## 源码依据

已对照当前依赖 `tinkers-construct-74072:8809959`：

- `MaterialVariantId` 使用 `base#variant` 语法；
- `MaterialRenderInfoLoader` 从 `materials/<base>/<variant>.json` 读取变体；
- `MaterialGeneratorInfo.LOADABLE` 要求 `transformer`、`supported_stats`，并支持 `variant`；
- `MaterialManager` 从 `tinkering/tags/materials` 读取材料标签，标签值是材料 ID。

## 验证

- `gradlew.bat compileJava --no-daemon`：通过。
- `gradlew.bat runServer --no-daemon`：通过世界启动和脚本加载。
- 服务端日志显示 `Loaded 92 materials`、`Loaded 20 material tags for 75 materials`；自定义 `phase4_all` 的 23 个 stat 和 `phase4_head` 的 head stat 均已加载，未再出现标签事件异常。
- 4A 已有的四个错误用例仍输出预期英文校验错误。
- 客户端资源重载和变体生成已在 4C 验证；基础材料的外观同步已在 4D 完成，详见 [delivery-phase4d.md](delivery-phase4d.md)。

## 限制与后续

- `generator(String...)` 的默认调色板是便捷默认值，复杂材料应使用 `generator(Map)` 或 `generatorPalette(Map)`；
- 运行时部件贴图生成已在 4C 完成，详见 [delivery-phase4c.md](delivery-phase4c.md)；
- 服务端材料定义、属性和特性继续只接受基础材料 ID，客户端变体不会创建新的 `MaterialRegistry` 材料。
