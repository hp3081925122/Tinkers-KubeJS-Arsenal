# 第九期交接文档：弓弩与护甲测试

## 交接结论

第九期代码已能编译，弩、护甲、染色、快速装填、JEI 和手册已经完成实际游戏验证。2026年10月31日继续测试后，长弓组装、拉弓三帧、工匠站纹饰和创造栏均已通过；测试中发现并修复了弓弩防御模型和弓弦未知材质贴图缺失两个问题。仍未完成：Render Preview 对比（工具路径不存在）、四件护甲图标与弩各帧的重新目检。

## 继续测试结果（2026年10月31日）

环境：主项目 `runClient -PskipAli -PskipMcp`（`build.gradle` 新增 `skipAli` 开关，不改文件即可去掉 ALI），Minecraft Self MCP 0.3.0，`run/mctest/autojoin.txt` 自动进入 `phase9_test_world`。截图在 `run/mctest/shots/test_p9c_*.png`，测试脚本在 `D:\AI临时文件	kja_phase9_test	est_p9c_*.py`。

| 项目 | 结果 | 证据 |
|---|---|---|
| 长弓与弩配方 | 通过，原问题不是配方缺陷 | 工匠站只有 1 个工具槽 + 3 个输入槽（`#0`-`#3`，`#4` 产出），长弓需要 4 个部件（`logicalParts()` 为 弓臂、弓臂、弓把、弓弦），在工匠站上无法组装；放 3 个部件只能匹配弩。匠魂 `ToolBuildingRecipe.matches` 只按输入槽顺序比对部件、`requiredInputs > maxInputs` 直接不匹配，不看左侧布局，匠魂自己的长弓同样只能在匠魂砧上做。实测：工匠站 3 部件产出 `kubejs:test_crossbow`；匠魂砧放 弓臂、弓臂、弓把、弓弦 预览“测试长弓”（耐久 360、拉弓速度 1），取出后背包有 `kubejs:test_bow`；匠魂砧放 3 部件产出弩。截图 `test_p9c_anvil_bow.png`、`test_p9c_station_crossbow.png` |
| 拉弓三帧 | 通过 | 按住使用键不松开，逐次读取 `tconstruct:charge` 和实际解析出的模型贴图：静止 `limb/bowstring`；第 4 tick charge=0.2 和第 10 tick charge=0.5 为 `limb_1/bowstring_1`；第 17 tick charge=0.85 为 `limb_2/bowstring_2`；第 27 tick charge=1.35 为 `limb_3/bowstring_3`；松开后回到静止贴图。修复后重启复测结果相同。截图 `test_p9c_pull_1.png`-`test_p9c_pull_4.png` |
| 护甲纹饰 | 通过 | 工匠站工具槽放测试胸甲（铜/皮革），输入槽放 `sentry_armor_trim_smithing_template` 和石英，产出胸甲；`tic_persistent` 为 `{"tconstruct:trim_material":"minecraft:quartz","tconstruct:trim_pattern":"minecraft:sentry"}`，`tic_upgrades` 有 `tconstruct:trim`，模板和石英已消耗。穿戴后正面胸口出现白色哨兵纹样，背面肩部白色；换上同材质无纹饰胸甲对比无白色图案。截图 `test_p9c_trim_front_zoom.png`、`test_p9c_trim_back_zoom.png`、`test_p9c_notrim_front_zoom.png` |
| 创造栏 | 通过 | 读取创造栏内容：`tconstruct:tools` 中 `test_bow` 36 个、`test_crossbow` 36 个、四件护甲各 41 个材质变体；`tconstruct:tool_parts` 与 `kubejs:tab` 中 0 个 |
| 资源加载 | 修复后通过 | 修复前 `latest.log` 有 2 条 `Missing textures in model kubejs:test_bow/test_crossbow#inventory`，缺 `bowstring_0` 和 `bowstring*_tconstruct_unknown`；修复后为 0 条 |

上一轮纹饰失败的原因：原版锻造台本来就不接受匠魂护甲（匠魂自己的护甲也只能在工匠站加纹饰）；工匠站那次未能确认原因，推测是选中了护甲布局，输入槽过滤只接受镶板和链甲。

### 本次修复

1. `ArsenalToolBuilder.blockingModelJson`：防御模型原来对第 0 帧也拼 `_0` 后缀，引用了不存在的 `bowstring_0`、`limb_0`。改为与匠魂 `longbow/blocking.json` 相同：只写 `parent`（第 0 帧指向主模型，第 1-3 帧指向对应拉弓帧）和 `display`。
2. `GeneratedMaterialSpriteSource`：匠魂 `unknown` 材质的生成器不支持 `bowstring` 属性，导致弓弦没有 `_tconstruct_unknown` 变体，而工具模型会请求这张图。匠魂自带的长弓、弩弓弦贴图都有 unknown 变体，因此改为工具贴图（不含部件物品贴图）在 unknown 材质下不受属性类型限制，一律生成。

### 仍未完成

- Render Preview：`E:/模组/发cf的模组/Render Preview` 路径不存在，未做对比。
- 四件护甲物品栏图标、弩装填/发射/损坏模型本次未重新目检（上一轮已有截图，本次只验证了长弓帧和胸甲纹饰渲染）。

## 项目与环境

- 项目目录：`E:\模组\发cf的模组\AAA-Tinkers\Tinkers KubeJS Arsenal\Tinkers KubeJS Arsenal-1201-forge`
- Minecraft：1.20.1 Forge 47.4.23
- 测试 MCP：Minecraft Self MCP 0.3.0（上一轮为 0.2.0），端口 9876
- MCP Python 客户端：`E:\模组\自用工具源码+产物\Minecraft Self MCP\python\mctest.py`
- 测试世界：`phase9_test_world`，创造模式，允许作弊
- 测试方块位置：玩家附近 `(-9, 90, -2)`，测试时放置过工匠站和锻造台
- 临时测试文件：`D:\AI临时文件\tkja_phase9_test`

测试结束后客户端已停止，ALI 依赖已恢复到 `build.gradle`。临时测试脚本和测试资源已经从 `run/kubejs` 删除。

## 已通过的实际测试

### 弩

- 使用工匠站实际放入两份木弓臂、木弓把、线弓弦，成功产出 `kubejs:test_crossbow`。
- 持弩装填后，状态中出现 `tconstruct:crossbow_ammo`。
- 右键发射后，目标盔甲架消失，确认命中并造成足够伤害。
- 在工匠站放入 5 个岩浆膏，输出预览和最终 NBT 均出现 `tconstruct:quick_charge`。
- 快速装填截图：`run/mctest/shots/quick_charge_applied.png`。
- 弩组装截图：`run/mctest/shots/bow_assembly.png`。该文件名历史上叫 bow assembly，但实际输出是测试弩。

### 护甲

- 胸甲镶板（青铜）+ 链甲基底（铁）实际组装成功。
- Tooltip 实测：6 护甲、1 击退抗性、2 盔甲韧性、耐久 448。
- 装备槽位通过 `/data get entity @s Inventory` 确认，胸甲位为槽位 102。
- 切换生存模式后使用 `minecraft:mob_attack` 造成伤害，护甲 `Damage` 从 0 变为 1，确认耐久下降。
- 护甲组装截图：`run/mctest/shots/armor_assembly.png`。
- 正背面截图：`run/mctest/shots/armor_front.png`、`run/mctest/shots/armor_back.png`。

### 染色、JEI、手册

- 工匠站放入测试胸甲和红色染料后，NBT 出现 `tconstruct:dyed`，并写入 `tconstruct:dyed` 持久化颜色。
- JEI 已完成加载，完整截图：`run/mctest/shots/creative_jei_full.png`。
- 《材料与你》可打开，截图：`run/mctest/shots/manual_book.png`。

### 资源与启动

- 临时测试启动脚本加载 0 错误。
- 客户端资源重载完成。
- 最终测试日志没有 `Failed to load model`、`Missing offset` 或测试弓弩 Missing Texture 警告。
- `./gradlew.bat compileJava --offline`：通过。

## 上一轮未通过或尚未确认的问题（1-4 已在上方继续测试中解决）

### 1. 长弓与弩的工匠站配方冲突

测试脚本中的长弓和弩都使用相同的三个部件类型：`bow_limb`、`bow_grip`、`bowstring`。长弓实际组装时工匠站输出了 `kubejs:test_crossbow`，没有输出 `kubejs:test_bow`。

相关代码：

- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/tool/ToolDataEventJS.java`
- `ToolDataEventJS.ToolDefinitionJS.recipe()` 当前只生成 `tconstruct:tool_building`，没有为相同部件组合提供区分条件。
- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/tool/ArsenalToolBuilder.java` 负责长弓/弩模型和逻辑部件。

建议优先确认匠魂 `ToolBuildingRecipe` 对相同部件组合的匹配顺序，再决定是：

1. 为长弓和弩提供不同的部件类型或不同过滤标签；
2. 为其中一个工具增加独有部件；
3. 设计不会被匠魂配方系统合并的 station layout/recipe 区分方式。

不能只通过修改模型或输出名称解决，因为当前问题发生在实际配方匹配阶段。

### 2. 长弓三帧拉弓证据不完整

直接给予 `kubejs:test_bow` 后可以拉弓并射击，目标盔甲架被命中；已生成：

- `run/mctest/shots/bow_frame_1.png`
- `run/mctest/shots/bow_frame_2.png`
- `run/mctest/shots/bow_frame_3.png`
- `run/mctest/shots/bow_shot_hit.png`

当前 MCP 的 `hold_key` 会在调用结束时释放按键，无法在同一个调用中读取明确的 1/2/3 级 `tconstruct:charge` 值。继续测试时应使用更新后的 MCP 做并发截图或增加按键按下/释放分离接口，并逐帧确认模型纹理确实不同。

### 3. 锻造纹饰未通过

- 原版锻造台中，测试护甲无法接受锻造模板和下界石英。
- 工匠站中放入测试胸甲、`minecraft:sentry_armor_trim_smithing_template` 和 `minecraft:quartz`，输出预览没有加入纹饰词条。

匠魂 jar 中存在：

- `data/tconstruct/recipes/tools/modifiers/slotless/trim.json`
- `slimeknights.tconstruct.tools.recipe.ArmorTrimRecipe`
- `tconstruct:armor/trim` 可修改标签

下一步应反编译/检查 `ArmorTrimRecipe` 的实际输入槽顺序、模板标签和材料标签，确认测试护甲是否正确带有 `.trim()` 生成的模块；不要直接假定原版 Smithing Table 配方适用。

### 4. 创造栏未确认

已打开创造模式物品栏，但当前页面仍是原版建筑方块，搜索框在该页面未成功筛出测试物品。需要继续检查 `ArsenalToolTabs` 的创造栏注册，以及测试物品是否被加入正确的 `CreativeModeTab`。

相关代码：

- `src/main/java/com/hp/tinkers_kubejs_arsenal/kubejs/tool/ArsenalToolTabs.java`
- `src/main/java/com/hp/tinkers_kubejs_arsenal/TinkersKubejsArsenal.java`

### 5. 正式资源的最终渲染确认仍需做

测试期间为了验证模型格式，曾在 `run/kubejs/assets` 临时加入测试弓弩基础贴图和 `_0_tconstruct_unknown` 等别名贴图；这些临时资源已删除。最终应使用真实 KubeJS 物品/材质定义重新启动，并通过 `E:/模组/发cf的模组/Render Preview` 检查：

- 基础未拉弓模型；
- 拉弓第 1、2、3 帧；
- 弩装填、发射和损坏模型；
- 四件护甲的物品栏图标、穿戴正面和背面；
- 染色和纹饰层。

## 重要的测试噪声

- `tkja_chain_test.js` 会故意生成若干配方错误，其中包含一条旧式 melting 配方的 ERROR。这是已有测试脚本噪声，不能把它当成第九期弓弩/护甲功能通过或失败的依据。
- 移除 ALI 后 JEI 能在约 6 秒完成注册；此前 ALI 的 JEI 插件会长时间阻塞资源/世界加载。测试完毕已恢复 ALI 依赖。

## 继续工作建议

1. 先修复长弓/弩相同部件的配方冲突，并用工匠站分别实际产出 `test_bow` 与 `test_crossbow`。
2. 检查 `ArmorTrimRecipe` 和 `.trim()` 生成的数据，完成纹饰实际应用与渲染验证。
3. 修正或确认创造栏注册。
4. 用更新后的 MCP 补做三帧拉弓的逐帧 charge 断言。
5. 重新启动客户端，完成 Render Preview，确认真实资源而非临时测试贴图。
6. 修复后再次运行 `compileJava --offline`，再执行完整客户端测试并停止客户端。

