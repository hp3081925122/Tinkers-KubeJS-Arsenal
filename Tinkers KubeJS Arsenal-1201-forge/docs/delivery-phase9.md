# 第九期交付：KubeJS 自定义弓弩与护甲

## 已完成

- 新增 `tinkers_kubejs_arsenal:bow`、`crossbow`、`armor` 三种物品类型。
- 弓弩支持部件定义、拉弓帧、格挡帧、损坏模型、远程标签和材质帧贴图生成。
- 护甲支持头盔、胸甲、护腿、靴子、护甲套启动事件、穿戴模型、共用工作台布局、部位标签、染色/纹饰标签和护甲手册标签。
- 护甲组装配方自动引用 `<护甲套>_armor` 布局，多个部位的第一部件会合并为同一个过滤器。
- ProbeJS 参数类型补充了 `armorSlot` 和 `armorSet`，并加入护甲套构建器。

## 写法

```js
ArsenalToolEvents.armorSets(event => {
  event.create('kubejs:bronze')
    .materialLayer('plating', 0)
    .materialLayer('maille', 1)
    .trim()
})

StartupEvents.registry('item', event => {
  event.create('kubejs:short_bow', 'tinkers_kubejs_arsenal:bow')
    .part('limb', 'tconstruct:bow_limb')
    .part('grip', 'tconstruct:bow_grip')
    .part('bowstring', 'tconstruct:bowstring')
    .pullFrames('limb', 'bowstring')
    .brokenPart('limb')

  event.create('kubejs:bronze_chestplate', 'tinkers_kubejs_arsenal:armor')
    .slot('chestplate')
    .armorSet('kubejs:bronze')
    .part('plating', 'tconstruct:chestplate_plating')
    .part('maille', 'tconstruct:maille')
    .bookArmor('puny_smelting')
})
```

护甲套可以使用 `.materialLayer`、`.fixedLayer`、`.dyedLayer` 和 `.trim`。声明染色或纹饰图层后会自动加入对应匠魂标签。

## 核对结果

- `DummyArmorMaterial` 的防御、耐久、韧性和击退抗性均为 0，不会叠加原版护甲值。
- 匠魂格挡模型使用基础 `blocking` 和 `blocking_1..3`，结构已按 1.20.1 匠魂资源生成。
- 护甲布局使用 `pattern` 图标和数组过滤器，格式与 `tconstruct:plate_armor` 一致。
- 所有护甲手册章节的页面路径均为 `armor`；百科全书使用 `book_armor`，其他手册使用 `book_armor/<手册名>`。
- 弓弩没有 `_broken_1..3` 贴图，损坏模型直接使用普通损坏贴图。

## 验证

- `./gradlew.bat compileJava --offline`：通过。
- 临时 KubeJS 脚本注册弓、弩和四件护甲：启动脚本 0 错误，客户端资源重载完成，匠魂加载到新增护甲模型。
- 测试脚本位于 `D:\AI临时文件\tkja_phase9_startup.js` 和 `D:\AI临时文件\tkja_phase9_server.js`，已在测试后从 `run/kubejs` 删除。

游戏内弓弩射击、护甲组装数值、穿戴正背面和手册页面仍需按第九期测试清单补充截图证据。
