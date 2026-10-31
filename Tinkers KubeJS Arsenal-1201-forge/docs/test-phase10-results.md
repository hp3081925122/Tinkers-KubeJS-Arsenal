# 第十期测试结果：材料三围、属性公式、部件公式

测试日期：2026-10-31

测试对象：`Tinkers KubeJS Arsenal-1201-forge`

## 构建校验

通过。执行命令：

```powershell
$env:JAVA_HOME='C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat build --offline "-Dnet.minecraftforge.gradle.check.certs=false"
```

结果：`BUILD SUCCESSFUL`，18 个任务中 9 个执行、9 个为最新状态。

## 服务端测试阻塞

D/K 场景按交接文档准备了临时数据包和 KubeJS 脚本，但 `runServer` 未能启动。实际命令为：

```powershell
$env:JAVA_HOME='C:\Users\30819\.jdks\ms-17.0.18'
$env:GRADLE_OPTS='-Dnet.minecraftforge.gradle.check.certs=false'
.\gradlew.bat runServer --offline
```

第一次执行在 `:runtimeClasspathCopy` 阶段失败，实际错误包括：

- 根项目 Mantle `8809819_mapped_official_1.20.1` 与 HPTlib 传递的严格约束 `7563777_mapped_official_1.20.1` 冲突；
- 根项目 TConstruct `8809959_mapped_official_1.20.1` 与 HPTlib 传递的严格约束 `7449219_mapped_official_1.20.1` 冲突；
- 另有两个同类严格版本约束冲突。

使用临时强制版本的 init script 重试后，Forge DeobfuscatingRepo 在解析 Mantle POM 时出现 `java.lang.StackOverflowError`，随后报 `Could not initialize class net.minecraftforge.artifactural.api.artifact.Int...`，仍未进入游戏。

完整命令、错误和清理记录见：[phase10-server-blocked.md](D:/AI临时文件/tkja-phase10-server/phase10-server-blocked.md)。

## 编号结果

由于服务端没有启动，以下项目均为“未测”，没有伪造游戏内通过证据：

| 场景 | 编号 | 结果 | 原因 |
|---|---|---|---|
| D | D1-D15 | 未测 | `runServer` 在依赖解析阶段失败，未产生材料加载、公式计算、工具 NBT 或 `/reload` 证据。 |
| K | K1-K4 | 未测 | 未进入 KubeJS 服务端脚本加载阶段，无法取得等价性、报错隔离或 ProbeJS 输出。 |
| S | S1-S4 | 未测 | 专用服务端未启动，无法建立客户端连接或验证同步。 |
| H | H1-H3 | 未测 | 未在只装 HPTlib 的独立环境启动服务端，无法取得独立依赖和工具结果证据。 |

## 测试资源与清理

- 临时数据包、KubeJS 脚本和强制依赖 init script：`D:\AI临时文件\tkja-phase10-server\`；
- 临时资源曾复制到 `run/world/datapacks/hpt_props_test` 和 `run/kubejs/server_scripts/`，测试结束后已删除；
- `run/server.properties` 已恢复 `online-mode=true`；
- 当前仓库没有新增测试脚本、数据包或截图。

## 后续解阻条件

需要先统一本模组、HPTlib、Mantle 和 TConstruct 的 ForgeGradle 映射依赖版本，并确认 HPTlib 的严格版本约束与根项目依赖一致；完成后重新执行 D/K 服务端断言，再进行 S/H 和客户端截图项目。
