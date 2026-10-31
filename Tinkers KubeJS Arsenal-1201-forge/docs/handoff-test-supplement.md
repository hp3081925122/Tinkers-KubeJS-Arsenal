# 交接文档：第六至八期补测

## 0. 背景

上一轮测试结果在 `docs/test-phase6-8.md`，对应提交 d4c090b。复核后的结论：

- **加载层面已确认：** 注册、配方存在、网络同步、属性和特性读回都有实测证据，这部分不用重测。
- **行为层面没有验证：** 很多项标成"通过"，依据只是"加载没报错"或推理。本次补测**只做这些项**，每项都要拿到**实际结果**（打印出的数值或判断结果、截图、命令输出）。不允许再用"加载无异常""未崩溃""由某代码写入"当证据。

约束和环境沿用 `docs/handoff-phase8b.md` 第 0 节和 2.2 节：中文、Java 不写注释、调试日志用英文、提交不带 AI 署名、临时文件放 `D:\AI临时文件`、KubeJS 禁止写只调用一次的 function、用 API 前先确认。匠魂 jar 已解压在 `D:/AI临时文件/tc_jar_8809959/`，用 `javap -p -c -cp .` 查。

测试脚本、测试贴图沿用上一轮的 `run/kubejs/**/tkja_chain_test.js` 和 `run/kubejs/assets/kubejs/textures/` 下的文件。

## 1. 验证手段

有两条路，按项目选用。

### A. 开发环境 + KubeJS 脚本断言（逻辑类，优先用）

在 `run/kubejs/server_scripts/` 或 `client_scripts/` 下写 `tkja_verify_*.js`。用 `Java.loadClass` 调匠魂 API，把实际值打印到日志，格式统一为 `[TKJA-VERIFY] <编号> <PASS/FAIL> <实际值> <预期>`。KubeJS 禁止只调用一次的 function，所以断言直接内联写，不要包函数。

下面这些匠魂 API 已用 javap 确认存在：

| 用途 | API |
|---|---|
| 按指定材料造工具 | `ToolBuildHandler.buildItemFromMaterials(IModifiable, MaterialNBT)`、`MaterialNBT.of(IMaterial...)` |
| 读工具属性 | `ToolStack.from(stack).getStats().get(ToolStats.ATTACK_DAMAGE)` 等 |
| 损坏 | `ToolStack.setDamage(int)`、`ToolStack.isBroken()`、`ToolDamageUtil.breakTool(stack)`、`ToolDamageUtil.isBroken(stack)` |
| 取材料 | `MaterialRegistry.getMaterial(MaterialId)` |
| 材料默认特性 | `MaterialRegistry.getInstance().getDefaultTraits(MaterialId)` |

其他类和方法（部件配方、回收配方、创造栏）用之前先 javap 确认。

### B. 正式客户端 + 游戏交互 MCP（画面类）

开发环境的 `runClient` 不能装游戏交互 MCP 的 jar（它按正式环境编译，进主界面就崩溃）。画面类项目（创造栏、JEI、手册、模型显示）这样测：

1. `./gradlew build`，产物在 `build/libs/`。
2. 用游戏交互 MCP 的 `install_version`（或现有的正式实例）准备 1.20.1 Forge 47.4.23 客户端，装入：本模组 jar、匠魂 3.12.0.220 及 Mantle、KubeJS 2001.6.5 及依赖（Rhino、Architectury）、JEI、`E:\模组\我的世界mcp的jar\minecraft-mcp-1.20.1-forge-v0.4.20.jar`。依赖 jar 从 Gradle 缓存（`~/.gradle/caches/forge_gradle/deobf_dependencies` 是反混淆版，**不能用**）以外的正式版来源取；找不到正式版就停下来问用户。
3. 把 `run/kubejs/` 下的测试脚本和贴图复制到该实例的 `kubejs/` 目录。
4. `options.txt` 设 `pauseOnLostFocus:false`，启动后用 MCP 进单人世界，按下面的项目截图。

客户端实例放在 `D:\AI临时文件` 下，不要动用户已有的实例。如果 B 准备不起来（缺依赖、下载失败），在报告里写明，并把这些项标为"未测"，**不要标通过**。

## 2. 补测项目

编号沿用 `test-phase6-8.md`。

| 编号 | 项目 | 手段 | 怎么测 | 通过标准 |
|---|---|---|---|---|
| 5 | 标签成员 | A | 服务端脚本对 `Item.of('kubejs:great_blade')` 等调用 `.hasTag(...)` 或用 `Ingredient.of('#...')` 测试 | `kubejs:great_blade` ∈ `tconstruct:parts`；`great_blade_cast` ∈ `tconstruct:casts/gold` 和 `kubejs:casts/multi_use/great_blade`；两个沙铸模 ∈ `tconstruct:casts/single_use`；`kubejs:war_axe` ∈ `tconstruct:modifiable/multipart`、`durability`、`small`、`melee/weapon`、`harvest/primary` |
| 7 | 部件制作 | A | 从配方管理器取 `kubejs:tools/parts/builder/great_blade`，打印它的 cost、pattern；用匠魂部件配方的材料输出方法（javap `PartRecipe` 确认方法名）取铁制输出，检查输出物品和 NBT 里的材料 | cost=4，pattern=`kubejs:great_blade`，输出是 `kubejs:great_blade` 且材料为 `tconstruct:iron` |
| 7b | 不能用的材料 | A | 对没写 blade 属性的材料（例如 `tconstruct:wood`）调用 `new MaterialStatsId('kubejs:blade').canUseMaterial(...)`，铁、铜各测一次 | 木头 false，铁、铜 true |
| 8 | 浇铸 | A | 取 `kubejs:tools/parts/casting/great_blade_gold_cast`，打印 item_cost 和 cast 材料；`kubejs:smeltery/casts/gold/great_blade` 用铁制巨刃当铸模时的匹配结果（javap 确认浇铸配方的匹配方法） | 浇铸消耗为 4 锭对应的流体量；金铸模配方能匹配铁制巨刃 |
| 9 | 组装后的数值 | A | `buildItemFromMaterials(war_axe, MaterialNBT.of(iron, wood, wood))` 和 `(copper, wood, wood)`，分别打印攻击伤害、攻速、耐久 | 两把都不是 0 或默认值；铁比铜的攻击伤害**高**，差值等于 (3−2)×部件缩放（缩放来自工具定义 `part_stats`，默认 1，要先读出确认）；攻速体现 `percent` 加成。报告里写出实际数值和推算过程 |
| 10 | 损坏 | A + B | A：对造出的战斧 `ToolDamageUtil.breakTool`，检查 `isBroken` 为 true。B：给玩家一把耐久耗尽的战斧，截图手持模型 | A 为 true；B 截图中刃身是 `blade_broken` 贴图（和正常的对比截图） |
| 11 | 回收 | A | 取 `tconstruct:tools/recycling/general`，检查它的工具输入能接受战斧（javap `PartBuilderToolRecycle` 确认字段或方法）；能做到的话，用它对铁制战斧算出可回收的部件列表 | 能接受战斧；部件列表为巨刃、手柄、绑定结，材料对应 |
| 12 | 创造栏 | B（或 A 的客户端脚本） | 打开匠魂"工具部件""冶炼""工具"三个栏和 KubeJS 栏截图；或在客户端脚本里读对应 `CreativeModeTab` 的 display items | 部件栏有巨刃的多个材料变体（至少铁、铜）；冶炼栏有三种巨刃铸模；工具栏有战斧变体；KubeJS 栏里没有这 5 个物品 |
| 13 | JEI | B | 搜"巨刃"，截图物品列表和配方页（按 R 或 U） | 列表里有多个材料变体；配方页能看到部件制作台配方和浇铸配方 |
| 14 | 手册 | B | 打开《材料与你》，翻到工具章截图战斧页；打开铁的材料页截图 | 战斧页显示中文介绍"测试用战斧。"和特性"测试特性一"；铁的材料页能找到巨刃 |
| 15 | /reload | A | 开发服务端 `run/server.properties` 设 `enable-rcon=true`、`rcon.password=`（任意测试密码）、`rcon.port=25575`，用 Python 写一个 RCON 客户端发命令（放 `D:\AI临时文件`）。步骤：读出铁制战斧攻击伤害 → 把服务端脚本里铁的 `customStat('kubejs:blade', 3, ...)` 改成 `5` → RCON 发 `reload` → 再读一次 | `reload` 后攻击伤害按 (5−3)×缩放 增加；日志没有错误。测完把脚本改回 3、关掉 rcon。读数可以用 `ServerEvents.command` 或一个测试命令触发脚本打印；不能新增只调用一次的 function |
| 16b | 原有特性保留 | A | `getDefaultTraits(tconstruct:iron)` 打印；再取铁的 `tconstruct:head` 特性 | 默认特性里仍有 `tconstruct:magnetic`；铁的 blade 部件特性是 sharpness |

注意：Advanced Loot Info 在 `/reload` 时可能卡 4 分钟以上，等待时间要留够；如果超过 10 分钟，记录下来并临时移除它再测。

## 3. 发现问题时

- 先确认是本模组的问题，还是测试方法本身有问题（API 用错、时机不对）。
- 是本模组的问题：先看源码定位原因，加英文调试日志确认，修复后跑 `compileJava`，再复测这一项。每个修复单独提交。
- 不要为了让测试通过去改测试的预期值。预期确实写错了，在报告里说明理由。

## 4. 交付

- 更新 `docs/test-phase6-8.md`：补测的每项改为实际结果，并在证据列写日志行、数值或截图文件名。截图放 `docs/test-screenshots/`，体积大的话放 `D:\AI临时文件` 并写路径。
- 结果分三类：通过、失败（已修复，写提交号）、未测（写原因）。
- 列出新增的测试脚本和临时实例的路径。
- 提交并推送（`git push` 直接可用）。
