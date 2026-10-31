# 第五期交付说明：KubeJS 自定义词条、词条事件与工具属性事件

## 完成内容

1. `ArsenalModifierEvents.data`：在 `server_scripts` 里定义匠魂词条，生成 `data/<ns>/tinkering/modifiers/<path>.json`，`/reload` 生效；名称、描述、颜色随 4D 的网络同步发给客户端。
2. 词条事件：脚本定义的词条会自动带上本模组的事件模块，开发者按词条 ID 监听近战伤害、命中后、弹射物命中、挖掉方块、受击、工具属性、实体属性。
3. `ArsenalToolEvents.stats`：对所有匠魂工具生效的属性事件，不需要工具带词条（Mixin `ToolStack.rebuildStats`）。
4. `ArsenalTool`：在任意 KubeJS 事件里查询工具和护甲上的词条等级、读写工具持久数据、手动重建工具。
5. 第四期的全局名字改为 `Arsenal` 开头：`ArsenalMaterialEvents`、`ArsenalRecipes`、`ArsenalFluids`（写法规范 S15）。

## 文件

- `kubejs/ArsenalTool.java`：查询绑定。
- `kubejs/modifier/ArsenalModifierEvents.java`、`ArsenalToolEvents.java`：事件组。
- `kubejs/modifier/ModifierDataEventJS.java`：词条定义。
- `kubejs/modifier/ModifierHookEvents.java`、`ToolStatsEventJS.java`：事件对象。
- `kubejs/modifier/ArsenalEventsModule.java`：匠魂词条模块 `tinkers_kubejs_arsenal:kubejs_events`（近战伤害、命中、弹射物命中、挖方块、护甲前受击、工具属性、实体属性）和 `tinkers_kubejs_arsenal:kubejs_damage_events`（护甲后受伤）。两个模块在 `RegisterEvent`（配方序列化器阶段）注册，与匠魂自身注册模块的时机一致。
- `mixin/ToolStackMixin.java`：重定向 `rebuildStats()` 里唯一一次 `ModifierStatsBuilder.build()` 调用，在属性写入前发布 `ArsenalToolEvents.stats`。
- `mixin/ServerScriptManagerMixin.java`：材料之后发布 `ArsenalModifierEvents.data` 并写入词条 JSON。
- `network/TinkerMaterialNetwork.java`：词条名称、flavor、description、颜色并入同步资源。
- `kubejs/probe/ArsenalProbeJSPlugin.java`：补全工具属性 ID、属性操作、实体属性 ID、等级显示、提示显示。

## 用法

### 定义词条

```js
ArsenalModifierEvents.data(event => {
  event.create('kubejs:vampire')
    .color('B02030')
    .name('Vampire').name('zh_cn', '吸血')
    .flavor('zh_cn', '以血还血')
    .description('zh_cn', '近战伤害提高，命中后回复生命')
    .statBoost('tconstruct:attack_damage', 'add', 0.5)        // 匠魂原生 stat_boost 模块，每级 +0.5
    .module('tconstruct:enchantment', { /* 任意匠魂模块的原始 JSON */ })
    .icon('minecraft:item/redstone', 'tconstruct:gui/modifiers/book')  // 图标，多张贴图按顺序叠画
    .levelDisplay('default')        // default | single_level | no_levels | pluses
    .tooltipDisplay('always')       // always | tinker_station | never
})
```

语言键为 `modifier.<ns>.<path>`、`.flavor`、`.description`，颜色写入 `mantle/colors.json`，图标写入 `tinkering/modifier_icons.json`，三者都随外观同步发给客户端。

图标用于匠魂的书和 JEI。贴图必须在方块图集里：原版 `minecraft:item/*`、匠魂 `tconstruct:gui/modifiers/*` 可直接用；自己画的图放在 `kubejs/assets/kubejs/textures/item/<名字>.png`，写成 `kubejs:item/<名字>`。不设图标时，匠魂会画一本按词条颜色染色的默认书本。加到工具上仍用已有的词条配方，或用 `/tconstruct modifiers @s add <id> <等级>` 测试。

### 词条事件（只对带该词条的工具触发）

```js
ArsenalModifierEvents.meleeDamage('kubejs:vampire', event => {
  event.damage = event.damage * (1 + 0.1 * event.level)       // 可改伤害
})
ArsenalModifierEvents.afterMeleeHit('kubejs:vampire', event => {
  event.attacker.heal(event.damage * 0.05 * event.level)      // event.damage 为实际造成的伤害
})
ArsenalModifierEvents.projectileHit('kubejs:vampire', event => { /* event.projectile、event.hitEntity、event.attacker、event.target */ })
ArsenalModifierEvents.blockBreak('kubejs:vampire', event => { /* event.pos、event.state、event.player、event.isAOE() */ })
ArsenalModifierEvents.hurt('kubejs:guard', event => {         // 护甲计算前，每件带该词条的护甲各触发一次
  event.damage = Math.max(0, event.damage - event.level)
})
ArsenalModifierEvents.damage('kubejs:guard', event => { /* 护甲计算后 */ })
ArsenalModifierEvents.toolStats('kubejs:vampire', event => {
  event.multiply('tconstruct:mining_speed', 1 + 0.1 * event.level)   // add | percent | multiply | multiplyAll
})
ArsenalModifierEvents.attributes('kubejs:vampire', event => {
  event.add('minecraft:generic.movement_speed', 0.01 * event.level, 'addition')
})
```

攻击类事件可取：`attacker`、`player`、`target`、`livingTarget`、`hand`、`slot`、`projectile`、`critical`、`fullyCharged`、`extraAttack`、`baseDamage`、`damage`、`tool`、`level`、`modifier`。

### 全局工具属性

```js
ArsenalToolEvents.stats(event => {
  if (event.hasTag('tconstruct:modifiable/harvest')) event.percent('tconstruct:mining_speed', 0.1)
  // event.itemId、event.modifierLevel(id)、event.get(stat) 可用于判断
})
```

### 查询方法

```js
ArsenalTool.isTool(item)
ArsenalTool.modifierLevel(item, 'kubejs:vampire')
ArsenalTool.hasModifier(item, 'kubejs:vampire')
ArsenalTool.armorModifierLevel(entity, 'kubejs:guard')       // 四件护甲等级之和
ArsenalTool.projectileModifierLevel(arrow, 'kubejs:vampire')
ArsenalTool.getInt(item, 'kubejs:kills') / setInt(...) / getFloat(...) / setFloat(...)
ArsenalTool.rebuild(item)                                    // 立即按当前脚本重算属性
```

## 限制

- 词条事件只对本模组生成的词条触发；匠魂自带词条没有事件模块，判断它们请用 `ArsenalTool` 配合 KubeJS 自带事件。
- `toolStats`、`ArsenalToolEvents.stats`、`attributes` 在工具重建时计算并保存，只能依据工具自身数据；随时变化的条件请放到 `meleeDamage` 等事件里。改脚本后已有工具要等下一次重建（加词条、修理等）或调用 `ArsenalTool.rebuild(item)` 才更新。
- 工具站预览会在客户端线程重建工具，此时会在客户端线程执行服务端脚本里的属性事件；回调里不要依赖世界状态或做耗时操作。
- 战斗、受击、弹射物事件只在服务端触发。
- `attributes` 中同一事件对同一属性多次 `add` 会生成不同的修饰符，按调用顺序区分；调用顺序变化会使 UUID 变化。
- `ToolStackMixin` 使用 `@Redirect`，若其他模组也重定向同一调用会冲突；目前开发环境未发现。

## 验证

- `gradlew build`：通过。
- 运行测试按要求暂缓，待项目全部完成后统一测试；测试脚本为 `run/kubejs/server_scripts/tkja_modifier_test.js`（聊天输入 `tkja_mod` 打印查询结果）。
