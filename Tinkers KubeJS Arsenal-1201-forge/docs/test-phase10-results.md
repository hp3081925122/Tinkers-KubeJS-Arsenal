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
| D12 | 未测 | 未取得部件提示框截图。 |
| D13 | 未测 | 未取得工匠站预览和成品提示框截图。 |
| D14 | 部分通过 | 将 hardness 改为 `8` 后重新加载世界，head 读回 attack=`8`、miningSpeed=`5`、tier=`IRON`。本轮使用重启加载验证，未取得同一世界内 `/reload` 后的独立日志。 |
| D15 | 未测 | 未完成已有工具退出重进后的重算对照。 |

## K、S、H 场景

| 场景 | 结果 | 原因 |
|---|---|---|
| K1-K3 | 未测 | 尚未完成 KubeJS 等价脚本、错误隔离和 KubeJS 材料读回的独立证据。 |
| K4 | 未测 | 未取得本轮 ProbeJS dump 的完整 d.ts 证据。 |
| S1-S4 | 未测 | 没有在独立工作目录同时完成专用服务端和客户端同步测试。 |
| H1-H3 | 未测 | HPTlib 独立 `runServer` 在 `downloadMCMeta` 阶段发生 SSL 握手失败，未进入游戏。 |

## 客户端与 GUI

ALI 移除后的客户端可以启动，且 classpath 中确认没有 ALI；但 GUI 测试没有形成可交付截图证据。之前使用旧存档时出现 `Failed to load data pack config`，该存档的 level.dat 数据包配置损坏，不能作为本轮通过依据。D12、D13 以及 S2、S3 因此保持“未测”。

## 非本项目日志噪声

服务端仍有既有的 `kubejs:phase5_guard` 未注册错误，以及 Forge 网络版本检查超时。这些错误与本轮材料属性数据包加载、公式计算和工具构造无关；ALI 已从开发依赖中移除。

## 临时资源

- 数据包、断言脚本和服务端日志：`D:\AI临时文件\tkja-phase10-full-server\`；
- 客户端测试临时目录：`D:\AI临时文件\tkja-phase10-full-client\`（本轮未形成 GUI 通过截图）；
- 当前仓库 `run` 中没有保留第十期临时数据包和脚本。
