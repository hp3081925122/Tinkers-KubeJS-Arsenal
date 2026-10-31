# 第十期功能测试结果

测试日期：2026-10-31

测试对象：`Tinkers KubeJS Arsenal-1201-forge`、HPTlib 1.1.0、Tinkers Construct 3.12.0.220、Mantle 1.11.117。

## 测试环境修复

- HPTlib 已统一使用 TConstruct `8809959` 和 Mantle `8809819`，依赖解析通过。
- 本模组已移除 Advanced Loot Info，ALI-free 客户端 classpath 中没有 `advanced-loot-info` 或 `ali`。
- 服务端启动统一使用 `runServer --offline -PskipMcp`，避免仅客户端 MCP 模组加载到专用服务端。
- 临时数据包、KubeJS 脚本和 RCON 已在测试结束后清理，`online-mode=true`、`enable-rcon=false` 已恢复。

## 服务端 D 场景

证据日志：`D:\AI临时文件\tkja-phase10-full-server\latest-phase10-final-d8.log`，以及后续 D9/D10/D11、D14 启动日志的 `run/logs/latest.log` 副本。

| 编号 | 结果 | 实际证据 |
|---|---|---|
| D1 | 通过 | `Loaded material properties for 4 materials, 3 stat formulas and 1 part formulas`；`Applied stat formulas to 11 material stat entries`；服务端 `Done`。 |
| D2 | 通过 | mythril 三围 `4/5/9`；excluded 的排除集合为 `[tconstruct:handle]`；铁为 `6/7/5`。 |
| D3 | 通过 | mythril head/handle/binding 均存在；head 为 `durability=550, attack=4, miningSpeed=3, tier=STONE`；handle 为 `0.09/0.08/-0.05/0`。 |
| D4 | 通过 | partial head 的 attack 为 `10`；铁 head 为 `250/2/6/IRON`，手写属性优先。 |
| D5 | 通过 | excluded head 存在，excluded handle 不存在。 |
| D6 | 通过 | 铁 head 有 `magnetic 1`、`reinforced 1`；默认特性和 handle 特性均保留 `magnetic 1`。 |
| D7 | 通过 | `canUseMaterial`：head 对 mythril=true、excluded=true；handle 对 excluded=false。 |
| D8 | 通过 | 有部件公式的 mythril 镐：attack `7.5600004`、miningSpeed `2.5500002`、lustrous 2、reinforced 2；移除 `pick_head.json` 后：attack `5.4`、miningSpeed `2.85`、lustrous 0。攻击差值为 `2.16`，对应镐定义中 `pick_head` 的部件缩放；挖掘速度变化符合 `percent` 结果。 |
| D9 | 通过 | D8 的有/无公式两次构造分别得到 lustrous `2/0`，reinforced 均为 `2`。 |
| D10 | 通过（修正测试对象） | 使用 `broad_axe` 时 lustrous 为 2，因为 TConstruct 的 broad axe 定义确实包含 `pick_head`，缩放为 `0.25`。交接文档把 broad axe 误列为“不含 pick_head”；该预期不成立。 |
| D11 | 通过 | 木头镐在有部件公式时 attack=`1`、miningSpeed=`2`，没有获得 mythril 的部件加成。 |
| D12 | 通过 | mctest 实测镐头提示框：硬度 4、密度 5、韧性 9；部件加成为近战伤害 +2、挖掘速度 -10%、光泽 II。工具手柄只有三围行。截图见 `run/mctest/runs/d12_tooltips-20261031-204423/`。 |
| D13 | 部分通过 | 成品镐提示框实测耐久 599、加固 II、光泽 II、穿甲 I、攻击伤害 8.56。工匠站预览未测。截图见 `run/mctest/runs/d13_finished_correct-20261031-204506/`。 |
| D14 | 通过 | 同一世界内执行 `/reload` 后，客户端脚本读回 hardness=`8`，head 为 attack=`8`、miningSpeed=`5`、tier=`IRON`。mctest 运行记录见 `run/mctest/runs/d14_reload-20261031-204528/` 和 `d14_stats-20261031-204556/`。 |
| D15 | 通过（复测，非代码问题） | 首轮记为失败，是因为只执行了 `/reload` 就截图，漏了交接文档要求的“退出并重新进入世界”。匠魂 `ToolStack.verifyTag`（物品从 NBT 加载时调用）在材料注册表加载完成后会对有材料的工具执行 `rebuildStats()`，`/reload` 本身不改已有物品 NBT；HPTlib 的部件加成注入在 `MaterialStatsModule.addToolStats/addTraits`，属于 `rebuildStats` 流程。复测（新启动客户端，`phase10_gui` 存档，`/data get`）：hardness=4 时镐 A 为 attack `7.5600004`、mining_speed `2.5500002`、tier `stone`；改 hardness=8 并 `/reload` 后、重进前镐 A 不变（对照值）；同条件新造镐 C 为 attack `15.08`、mining_speed `4.25`、tier `iron`；退出重进后镐 A 变为 attack `15.08`、mining_speed `4.25`、tier `iron`，与镐 C 完全一致；词条 reinforced 2、lustrous 2（上限 2）、pierce 1 不变。运行记录：`D:\AI临时文件\tkja-phase10-d15\d15_rejoin-20261031-210957\`，脚本 `d15_rejoin.py`。 |

## K、S、H 场景

| 场景 | 结果 | 原因 |
|---|---|---|
| K1 | 通过 | KubeJS-only 专用服务端加载 3 materials、3 stat formulas、1 part formula，应用 8 条 stat entries；mythril_k 为 4/5/9，excluded_k 排除 handle，head 存在、handle 被排除，head 为 550/4/3/STONE。证据：`D:\AI临时文件\tkja-phase10-remaining-server\k-only.log`。 |
| K2 | 失败（已修复，`af6e2f6`） | 首测：`tkja_props_errors.js` 第一个用例（limb 的 `bogus`）抛错后，KubeJS 把异常当作 `exit(ERROR)`，同一次 `ArsenalMaterialEvents.data` 的后续 handler（含正常的 `tkja_props_test.js`）全部未执行，HPTlib 只加载 `0 materials, 0 stat formulas and 0 part formulas`。原因：KubeJS 2001.6.5 `EventHandlerContainer.handle` 仅在传入 `EventExceptionHandler` 且返回 `null` 时继续后续 handler，本模组 `ServerScriptManagerMixin` 调 `DATA.post(data)` 未传。修复后三个数据事件（材料、词条、工具）都传入继续执行的异常处理器。复测 5 条报错依次为：`unknown round mode 'bogus'`、`'accuracy' coefficient 'hardness' must be a number`、`tconstruct:harvest_tier is not a numeric Tinkers tool stat`、`Part formula tconstruct:tool_binding: Trait tconstruct:reinforced max level must be at least 1`、`Material kubejs:bad_props: Material property 'hardness' must be a number`。证据：`D:\AI临时文件\tkja-phase10-k23\latest-k23.log`（修复前）、`latest-k23-fixed.log`（修复后）。 |
| K3 | 通过（修复后） | 日志有 `Stat formula tconstruct:limb was not generated` 和 `Stat formula tconstruct:grip was not generated`，以及 bad_props、tool_handle、tool_binding 的未生成提示；断言 `K3 PASS limb=false grip=false head=true handle=true`；公式集合为 `[binding, handle, head]`，部件公式只有 `pick_head`；HPTlib 加载 `3 materials, 3 stat formulas and 1 part formulas`，应用 8 条，与 K1 一致。 |
| K4 | 未测 | 未取得本轮 ProbeJS dump 的完整 d.ts 证据。 |
| S1-S4 | 未测 | 没有在独立工作目录同时完成专用服务端和客户端同步测试。 |
| H1-H3 | 未测 | HPTlib 独立 `runServer` 在 `downloadMCMeta` 阶段发生 SSL 握手失败，未进入游戏。 |

## 客户端与 GUI

ALI 移除后的客户端可以启动，且 classpath 中确认没有 ALI。使用全新 `phase10_gui` 世界后，D12 和成品提示框测试取得 mctest 截图；之前旧存档的 `Failed to load data pack config` 不再作为本轮依据。工匠站预览和专用服务器同步仍未完成。

## 非本项目日志噪声

服务端仍有既有的 `kubejs:phase5_guard` 未注册错误，以及 Forge 网络版本检查超时。这些错误与本轮材料属性数据包加载、公式计算和工具构造无关；ALI 已从开发依赖中移除。

## 临时资源

- 数据包、断言脚本和服务端日志：`D:\AI临时文件\tkja-phase10-full-server\`；
- K2/K3 脚本（`tkja_props_errors.js`、`tkja_verify_k3.js`）和日志：`D:\AI临时文件\tkja-phase10-k23\`，测完已从 `run/kubejs/server_scripts/` 移除；
- D15 复测脚本、运行记录和客户端启动日志：`D:\AI临时文件\tkja-phase10-d15\`；`phase10_gui` 中的测试数据包和 `run/mctest/autojoin.txt` 测完已删除；
- 客户端测试临时目录：`D:\AI临时文件\tkja-phase10-full-client\`（本轮未形成 GUI 通过截图）；
- 当前仓库 `run` 中没有保留第十期临时数据包和脚本。
