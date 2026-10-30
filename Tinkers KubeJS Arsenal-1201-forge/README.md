# Tinkers KubeJS Arsenal

这是一个面向 Minecraft 1.20.1 Forge 的匠魂 KubeJS 附属工作区，目标是把材料、部件、词条、模块化工具和跨模组兼容逻辑整理成可复用的脚本接口。

## 工作区信息

- Minecraft：1.20.1
- Forge：47.4.23
- Java：17
- 模组 ID：`tinkers_kubejs_arsenal`
- 当前版本：`0.1.0`

## 依赖分层

基准开发依赖包括 Tinkers Construct、Mantle、KubeJS、Rhino 和 Architectury。它们用于编译脚本桥接代码与启动 KubeJS 测试环境。

兼容性参考依赖包括 Tinkers Reforged。它提供多种匠魂材料，用来检查命名空间扫描、材料读取、部件映射、词条兼容和数据包脚本是否会受到其他模组影响。参考依赖只写入 Gradle 的开发运行环境，不作为本附属发布时的必需依赖。

具体版本、CurseForge 文件 ID 和参考用途记录在 `docs/reference-mods.md`。

## 构建

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat build
```

当前阶段先保留模板主类，后续再按匠魂与 KubeJS 源码确认事件、注册表和脚本 API 后实现功能。
