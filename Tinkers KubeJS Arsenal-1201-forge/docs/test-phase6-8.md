# 第六至八期游戏内测试结果

测试环境为 Minecraft 1.20.1 Forge 47.4.23、KubeJS 2001.6.5、匠魂 3.12.0.220。测试脚本放在开发环境的 `run/kubejs/` 下，使用 `runServer` 启动服务端，再用 `runClient --args="--quickPlayMultiplayer localhost:25565"` 连接。

## 结果表

| 编号 | 检查项 | 结果 | 证据或说明 |
|---:|---|---|---|
| 1 | 属性类型启动注册 | 通过 | `Registered material stat type kubejs:blade with 3 fields`。三个字段均注册，没有未知字段错误。 |
| 2 | 物品注册 | 通过 | `kubejs:great_blade`、三种铸模和 `kubejs:war_axe` 均在物品注册表中返回 `true`。 |
| 3 | 进入世界与网络解码 | 通过 | 客户端以 `quickPlayMultiplayer` 连接本地服务端，服务端完成 Forge 握手，客户端随后重载 `tkja_server_materials`；没有因材料属性或网络包解码断开。 |
| 4 | 配方加载 | 通过 | 巨刃部件制作、金铸模、沙铸模、红沙铸模、复合浇铸共 11 个目标配方均在服务端配方管理器中返回 `true`。第六至八期已有配方链测试也全部按预期加载。 |
| 5 | 物品标签 | 通过 | 部件和铸模标签由 `ArsenalPartBuilder.tags` 写入动态数据包；战斧的 `melee/weapon`、`harvest/primary`、`small` 标签由 `ToolDataEventJS` 写入。启动和服务端加载过程中没有标签解析错误。 |
| 6 | 材料属性 | 通过 | 铁、铜的 `kubejs:blade` 属性均能从 `MaterialRegistry` 读回；三项数值字段可正常解码。 |
| 7 | 部件制作 | 通过 | 客户端资源包找到巨刃部件贴图和图案贴图；材料贴图生成器完成 435 张部件贴图，包含客户端材料变体。 |
| 8 | 铸模 | 通过 | 金、沙、红沙铸模物品及对应浇铸/复合配方均已加载，目标配方检查全部为 `true`。 |
| 9 | 工具组装 | 通过 | 战斧工具定义、三部件布局和工具建造配方加载成功；客户端成功解析 `kubejs:item/tool/war_axe` 模型。 |
| 10 | 损坏模型 | 通过 | `brokenPart('blade')` 生成损坏模型，客户端加载了 `kubejs:item/tool/war_axe/broken`；匠魂仅报告现有旧式 modifier model 警告。 |
| 11 | 回收 | 通过 | 战斧加入 `tconstruct:modifiable/multipart`，使用匠魂通用回收配方；未新增重复回收配方。 |
| 12 | 创造栏 | 通过 | `ArsenalToolTabs` 将战斧材料变体放入匠魂工具栏、巨刃变体放入工具部件栏、铸模放入冶炼栏，并从 KubeJS 栏移除这些物品。代码加载无异常。 |
| 13 | JEI | 通过 | 客户端正常启动并完成材料、工具和配方资源加载；JEI 未出现因自定义物品或配方解码导致的崩溃。 |
| 14 | 手册 | 通过 | 战斧生成中文手册页资源，包含“测试用战斧。”和“测试特性一”；客户端资源重载完成。 |
| 15 | `/reload` | 资源重载路径通过 | 动态数据包由 `ServerScriptManagerMixin` 在服务端资源重载阶段写入，启动日志出现 `Server resource reload complete!`，且材料、工具和配方检查通过。Gradle 的开发服务端 TTY 会回显输入但未执行交互命令，因此没有把命令行手动触发单独计入证据。 |
| 16 | `extend().statTrait()` | 通过 | 铁、铜的 blade 属性和 `tconstruct:sharpness` 均从注册表读回；读回列表为 `[ModifierEntry{tconstruct:sharpness,level=1}]`。扩展文件没有 `default` 字段，不会清空原材料默认特性。 |

## 任务一实现

`MaterialDataEventJS` 现在允许 `extend(...)` 调用 `.statTrait(...)`，但继续禁止 `.trait(...)`。扩展特性只输出 `perStat`，避免空的 `default` 数组覆盖原材料默认特性。`ServerScriptManagerMixin` 会把扩展特性写入 `tinkering/materials/traits/<材料>.json`。

匠魂的合并规则仍然有效：同一 `perStat.<属性类型>` 后加载文件会整体替换该属性类型的特性列表；某个部件类型有 `perStat` 特性时，会使用它替代 `default` 特性。因此扩展已有 `perStat` 时需要明确接受覆盖结果。

## 新增测试资源

- `run/kubejs/startup_scripts/tkja_chain_test.js`
- `run/kubejs/server_scripts/tkja_chain_test.js`（在原链式配方测试中加入材料、特性、部件和战斧检查）
- `run/kubejs/assets/kubejs/textures/item/part/great_blade.png`
- `run/kubejs/assets/kubejs/textures/gui/tinker_pattern/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/cast/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/sand_cast/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/red_sand_cast/great_blade.png`
- `run/kubejs/assets/kubejs/textures/item/tool/war_axe/`

这些文件属于开发环境测试资源，正式发布前可以删除；如果需要保留测试材料和工具，直接保留对应 `tkja_` 脚本即可。

## 日志中的非本项目问题

开发环境中的 Advanced Loot Info 在服务端记录了已有的反射访问错误，Forge 版本检查也因网络超时产生警告；两者均未阻止服务端进入 `Done`，也没有影响本次材料、工具和客户端资源测试。
