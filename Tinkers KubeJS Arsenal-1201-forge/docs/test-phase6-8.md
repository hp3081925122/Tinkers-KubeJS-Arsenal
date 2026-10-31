# 第六至八期游戏内测试结果

测试环境为 Minecraft 1.20.1 Forge 47.4.23、KubeJS 2001.6.5、匠魂 3.12.0.220。测试脚本放在开发环境的 `run/kubejs/` 下，使用 `runServer` 启动服务端，再用 `runClient --args="--quickPlayMultiplayer localhost:25565"` 连接。

## 结果表

| 编号 | 检查项 | 结果 | 证据或说明 |
|---:|---|---|---|
| 1 | 属性类型启动注册 | 通过 | `Registered material stat type kubejs:blade with 3 fields`。三个字段均注册，没有未知字段错误。 |
| 2 | 物品注册 | 通过 | `kubejs:great_blade`、三种铸模和 `kubejs:war_axe` 均在物品注册表中返回 `true`。 |
| 3 | 进入世界与网络解码 | 通过 | 客户端以 `quickPlayMultiplayer` 连接本地服务端，服务端完成 Forge 握手，客户端随后重载 `tkja_server_materials`；没有因材料属性或网络包解码断开。 |
| 4 | 配方加载 | 通过 | 巨刃部件制作、金铸模、沙铸模、红沙铸模、复合浇铸共 11 个目标配方均在服务端配方管理器中返回 `true`。第六至八期已有配方链测试也全部按预期加载。 |
| 5 | 物品标签 | 通过 | 补测脚本逐项调用 `Item.of(...).hasTag(...)`；巨刃、三种铸模和战斧的目标标签全部打印 `PASS actual=true`。 |
| 6 | 材料属性 | 通过 | 铁、铜的 `kubejs:blade` 属性均能从 `MaterialRegistry` 读回；三项数值字段可正常解码。 |
| 7 | 部件制作 | 通过 | `PartRecipe` 实测 `cost=4`、`pattern=kubejs:great_blade`；铁材料输出为 `kubejs:great_blade`。 |
| 8 | 铸模 | 通过 | 实测金铸模配方的输入匹配为 `castMatch=true`，流体为 `tconstruct:molten_gold`、数量 `90`，输出为 `great_blade_cast`。 |
| 9 | 工具组装 | 通过 | 铁/铜/木三材料战斧均成功组装；铁攻击 `6.75`、铜攻击 `5`、差值 `1.75`，攻速 `1.1000000238/1.2000000477`，耐久 `300/200`。`part_stats` 运行时缩放读回为 `1,1,1`。 |
| 10 | 损坏模型 | 服务端通过，GUI 未测 | `ToolDamageUtil.breakTool` 后 `isBroken=true`。正式客户端/MCP 未准备好，因此没有把损坏模型截图计为通过。 |
| 11 | 回收 | 通过 | 匠魂通用回收显示配方共 `61` 条，结果中实际出现 `kubejs:great_blade`；显示配方材料占位为 `tconstruct:unknown`，这是通用回收展示层的占位值。 |
| 12 | 创造栏 | GUI 未测 | 代码注册和标签加载正常；当前没有正式客户端/MCP 会话，未声称创造栏界面通过。 |
| 13 | JEI | GUI 未测 | 开发客户端曾完成启动资源加载，但没有正式 JEI 页面检索截图，本次按要求标记未测。 |
| 14 | 手册 | GUI 未测 | 手册资源和中文文本已生成；当前没有正式客户端/MCP 会话，未声称手册界面通过。 |
| 15 | `/reload` | 通过 | RCON 实测：基线 `attack=6.75`；临时把铁 `customStat` 从 `3` 改为 `5` 后执行 `/reload`，读回 `attack=8.75`；恢复为 `3` 并再次 `/reload` 后回到 `6.75`。RCON 已关闭。 |
| 16 | `extend().statTrait()` | 通过 | 铁、铜的 blade 属性和 `tconstruct:sharpness` 均从注册表读回；本次补测另实测铁默认与 head 特性均保留 `tconstruct:magnetic`。扩展文件没有 `default` 字段，不会清空原材料默认特性。 |

## 4D/补测证据

本次补测使用 `run/kubejs/server_scripts/tkja_verify_behavior.js`，通过 RCON 执行 `/kubejs custom_command tkja_verify` 和 `/kubejs custom_command tkja_verify_tool`。逐项断言输出统一使用 `[TKJA-VERIFY]` 前缀，完整日志在 `run/logs/latest.log` 与 `run/logs/kubejs/server.log`。

- 5、7、7b、8、9、10、11、16b 均有 `PASS` 输出；9 的三部件缩放为 `1,1,1`，实际铁/铜攻击为 `6.75/5`。
- 15 的 RCON 证据为 `6.75 → 8.75 → 6.75`，中间只临时修改铁 `customStat`，完成后已恢复脚本。
- 10B、12B、13B、14B 需要正式客户端/MCP 截图。本环境只有开发客户端，且未准备可用的正式 MCP 客户端实例，因此这些 GUI 项目保持“未测”。
- RCON 测试客户端为临时文件 `D:\AI临时文件\tkja_rcon.py`；服务端测试结束后已从 `run/server.properties` 恢复备份并关闭 RCON。

## 任务一实现

`MaterialDataEventJS` 现在允许 `extend(...)` 调用 `.statTrait(...)`，但继续禁止 `.trait(...)`。扩展特性只输出 `perStat`，避免空的 `default` 数组覆盖原材料默认特性。`ServerScriptManagerMixin` 会把扩展特性写入 `tinkering/materials/traits/<材料>.json`。

匠魂的合并规则仍然有效：同一 `perStat.<属性类型>` 后加载文件会整体替换该属性类型的特性列表；某个部件类型有 `perStat` 特性时，会使用它替代 `default` 特性。因此扩展已有 `perStat` 时需要明确接受覆盖结果。

## 新增测试资源

- `run/kubejs/startup_scripts/tkja_chain_test.js`
- `run/kubejs/server_scripts/tkja_chain_test.js`（在原链式配方测试中加入材料、特性、部件和战斧检查）
- `run/kubejs/server_scripts/tkja_verify_behavior.js`（4D 行为级补测断言）
- `D:\AI临时文件\tkja_rcon.py`（临时 RCON 测试客户端，不属于仓库）
- `run/kubejs/assets/kubejs/textures/item/part/great_blade.png`
- `run/kubejs/assets/kubejs/textures/gui/tinker_pattern/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/cast/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/sand_cast/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/red_sand_cast/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/tool/war_axe/`

这些文件属于开发环境测试资源，正式发布前可以删除；如果需要保留测试材料和工具，直接保留对应 `tkja_` 脚本即可。

## 日志中的非本项目问题

开发环境中的 Advanced Loot Info 在服务端记录了已有的反射访问错误，Forge 版本检查也因网络超时产生警告；两者均未阻止服务端进入 `Done`，也没有影响本次材料、工具和客户端资源测试。
