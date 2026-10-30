# 第四期材料接口交付说明（4D）

## 完成内容

4D 把材料外观并入服务端材料定义。服务端脚本可以在同一个 `ArsenalMaterialEvents.data` 材料对象上声明属性、特性、颜色、后备贴图、精细贴图生成器和语言；玩家进入服务器或服务器执行 `/reload` 后，外观资源会通过 Forge 网络包同步到客户端。

客户端接收资源后挂载一个内存资源包，资源包位于资源包列表顶部，然后触发原版资源重载。因此匠魂的 `MaterialRenderInfoLoader`、Mantle 颜色加载和原版语言加载继续走原生路径，不需要修改匠魂的材料缓存。

## 文件变更

- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/material/MaterialDataEventJS.java`
  - 增加 `color`、`fallbacks`、`luminosity`、`texture`、`generator`、`generatorPalette`；
  - 增加 `textColor`、`name`、`flavor`、`encyclopedia`；
  - 保留与 `MaterialAssetEventJS` 一致的渲染 JSON、语言键和默认调色板规则。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/network/TinkerMaterialNetwork.java`
  - 注册 Forge `SimpleChannel`；
  - 在 `OnDatapackSyncEvent` 中向登录玩家和重载后的玩家发送客户端资源；
  - 把材料渲染 JSON、`mantle/colors.json` 和各语言 JSON 合并为一个网络包。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/client/SyncedMaterialClient.java`
  - 保存服务器资源并注册动态内存资源包；
  - 资源包更新后触发 `Minecraft.reloadResourcePacks()`。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/TinkersKubejsArsenal.java`
  - 在模组初始化阶段提前加载网络通道，避免 Forge 注册阶段结束后再注册通道。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/mixin/ServerScriptManagerMixin.java`
  - 在服务端材料事件收集完成后刷新待同步的客户端资源快照。
- `run/kubejs/server_scripts/tkja_material_test.js`
  - 基础材料同时声明服务端属性和客户端外观。
- `run/kubejs/client_scripts/tkja_material_test.js`
  - 只保留客户端专属的 `#polished` 变体测试。

## 使用方式

```js
// 服务端定义，同时同步客户端外观和语言
ArsenalMaterialEvents.data(event => {
  event.create('kubejs:mythril')
    .tier(3)
    .head({ durability: 600, mining_speed: 7.5, mining_tier: 'minecraft:diamond', melee_attack: 3 })
    .color('6FD0E8')
    .fallbacks('metal')
    .name('Mythril')
    .name('zh_cn', '秘银')
    .textColor('7FE0F8')
})
```

服务端材料 ID 仍然是基础 `ResourceLocation`。4D 不把 `base#variant` 变成新的服务端材料，因为 TC 的服务端材料管理器和属性表按基础材料 ID 工作；变体外观仍可用 `ArsenalMaterialEvents.assets` 在客户端声明。

## 网络资源格式

服务端快照会生成以下客户端资源：

- `assets/<namespace>/tinkering/materials/<path>.json`；
- `assets/<namespace>/mantle/colors.json`，同一命名空间的颜色合并到一个对象；
- `assets/<namespace>/lang/<language>.json`，名称、flavor 和 encyclopedia 合并到对应语言文件。

网络包只传输本次服务端脚本定义的资源。材料从脚本中删除并执行 `/reload` 后，客户端收到空资源或不再包含该材料的快照，下一次资源重载会移除旧资源。

## 验证

- `gradlew.bat compileJava --no-daemon`：通过。
- `gradlew.bat runServer --no-daemon`：服务端成功启动，材料脚本无脚本错误，加载 92 个材料、20 个材料标签、668 个材料属性；未再出现网络通道注册阶段错误。
- `gradlew.bat runClient --no-daemon`：客户端成功启动，现有 4C SpriteSource 继续生成 93 张部件贴图。
- 服务端注册阶段已验证网络通道提前注册；客户端资源包和 SpriteSource 均使用内存资源，不写入工作区临时文件。

## 限制

- 当前同步的是客户端资源 JSON 和语言文本，不同步服务端脚本本身；客户端必须安装本模组才能解析网络包。
- 服务端材料变体仍是客户端专属能力，基础材料和变体要分别声明时仍需使用 `data` 与 `assets` 两个事件。
- 只有外观资源和上次不同才会重载客户端资源；外观资源改动后执行 `/reload` 仍会整体重载一次，材料数量很多时耗时会增加。

## 后续修复（2026年10月31日）

| 问题 | 原因 | 修复 |
|---|---|---|
| 每次进入世界都会整体重载一次客户端资源，没有定义材料时也一样 | 每次数据包同步都会发送资源，客户端收到后无条件调用 `reloadResourcePacks()`，空包也会触发 | 客户端把收到的资源存到 `local/tinkers_kubejs_arsenal/synced_materials.json`；启动时通过 `AddPackFindersEvent` 注册资源包（设为必选包，自动启用），并载入这份缓存；收到的内容与当前相同时跳过重载。单人模式只有外观改动后的第一次进入世界或 `/reload` 才会重载 |
| 单个语言文件超过 32767 字符时，进入世界会报错断开 | 每个资源文件用 `writeUtf/readUtf` 传输，单个字符串有 32767 字符上限 | 所有资源打包压缩成一个字节数组发送，网络协议版本升到 `2`；压缩后超过 1,000,000 字节时只在服务端记一条错误、不发送，玩家不会被断开 |

资源包 `TKJA server materials` 现在是必选包，会出现在资源包界面里，不能手动移除。
