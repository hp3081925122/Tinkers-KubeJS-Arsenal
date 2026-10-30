# 匠魂 3.12.0.220 配方序列化器字段参考

> 来源：反编译 Tinkers' Construct 3.12.0.220（CurseForge 文件 8809959）与 Mantle 1.11.117（文件 8809819）。
> 本文件是实施文档 `recipe-compat-plan.md` 的附录。序列化器数量、浇铸字段、熔化/冷却公式、`level` 与 `allowed_materials` 默认值已由主对话复核。
> 反编译源码临时目录：`D:/AI临时文件/tkja-src/`（tconstruct、mantle、kubejs、probejs）。若目录被清理，用 Vineflower 重新反编译 Gradle 缓存中的对应 jar。

### 0. 总览与结论
- 注册入口：`TinkerModule` 声明 `RECIPE_SERIALIZERS`（namespace `tconstruct`，`common/TinkerModule.java:72`）。实际注册分布在三个类：`TinkerSmeltery.java` 32 个（行 343–436）、`TinkerTables.java` 16 个（行 123–169）、`TinkerModifiers.java` 28 个（行 589–674）。合计 **76 个序列化器**。
- 注意：`TinkerTools`、`TinkerCommons`、`TinkerMaterials` 中的 `RECIPE_SERIALIZER` 事件处理只注册 Ingredient / Condition / Predicate / Stat，不是配方序列化器，已排除。
- 另外 `mantle:` 命名空间的配方（crafting_shaped_retextured、fluid_container、tag_filled 等）属于 Mantle，不计入本次 76 个。
- JSON 中出现但不属于配方序列化器的 `tconstruct:` 类型：`config`、`instrument`、`material_value`、`no_container`、`slot_type`、`stat_type`、`terracube`、`tool_hook`、`has_part`、`sky_slime`、`ender_slime`、`block_tag`、`material`（嵌套时为 Ingredient）。
- 基础路径约定：`TC = D:/AI临时文件/tkja-src/tconstruct/slimeknights/tconstruct/`，示例路径相对于 `D:/AI临时文件/tkja-src/tconstruct/data/tconstruct/recipes/`。
- 通用说明：`type` 由注册表注入（TYPED_SERIALIZER，非业务字段）；`id` 来自文件名（ContextKey.ID，非 JSON 字段）；`group` 为可选字符串，缺省 `""`（`LoadableRecipeSerializer.RECIPE_GROUP`）。

### 1. 按注册类分组的表格

#### 1.1 TinkerSmeltery（smeltery/TinkerSmeltery.java，32 个）
| ID | 配方类 | 源文件路径 |
|---|---|---|
| tconstruct:casting_basin | ItemCastingRecipe | TC/library/recipe/casting/ItemCastingRecipe.java |
| tconstruct:casting_table | ItemCastingRecipe | 同上 |
| tconstruct:basin_filling | ContainerFillingRecipe | TC/library/recipe/casting/container/ContainerFillingRecipe.java |
| tconstruct:table_filling | ContainerFillingRecipe | 同上 |
| tconstruct:basin_duplication | CastDuplicationRecipe | TC/library/recipe/casting/CastDuplicationRecipe.java |
| tconstruct:table_duplication | CastDuplicationRecipe | 同上 |
| tconstruct:casting_basin_potion | PotionCastingRecipe | TC/library/recipe/casting/PotionCastingRecipe.java |
| tconstruct:casting_table_potion | PotionCastingRecipe | 同上 |
| tconstruct:casting_basin_tipping | TippingCastingRecipe | TC/library/recipe/casting/TippingCastingRecipe.java |
| tconstruct:casting_table_tipping | TippingCastingRecipe | 同上 |
| tconstruct:casting_basin_tipped_clearing | TipClearingCastingRecipe | TC/library/recipe/casting/TipClearingCastingRecipe.java |
| tconstruct:casting_table_tipped_clearing | TipClearingCastingRecipe | 同上 |
| tconstruct:retextured_casting_basin | RetexturedCastingRecipe | TC/library/recipe/casting/RetexturedCastingRecipe.java |
| tconstruct:retextured_casting_table | RetexturedCastingRecipe | 同上 |
| tconstruct:basin_casting_material | MaterialCastingRecipe | TC/library/recipe/casting/material/MaterialCastingRecipe.java |
| tconstruct:table_casting_material | MaterialCastingRecipe | 同上 |
| tconstruct:basin_casting_composite | CompositeCastingRecipe | TC/library/recipe/casting/material/CompositeCastingRecipe.java |
| tconstruct:table_casting_composite | CompositeCastingRecipe | 同上 |
| tconstruct:basin_tool_casting | ToolCastingRecipe | TC/library/recipe/casting/material/ToolCastingRecipe.java |
| tconstruct:table_tool_casting | ToolCastingRecipe | 同上 |
| tconstruct:material_fluid | MaterialFluidRecipe | TC/library/recipe/casting/material/MaterialFluidRecipe.java |
| tconstruct:basin_casting_part_swapping | PartSwapCastingRecipe | TC/library/recipe/casting/material/PartSwapCastingRecipe.java |
| tconstruct:table_casting_part_swapping | PartSwapCastingRecipe | 同上 |
| tconstruct:molding_basin | MoldingRecipe | TC/library/recipe/molding/MoldingRecipe.java |
| tconstruct:molding_table | MoldingRecipe | 同上 |
| tconstruct:melting | MeltingRecipe | TC/library/recipe/melting/MeltingRecipe.java |
| tconstruct:ore_melting | OreMeltingRecipe | TC/library/recipe/melting/OreMeltingRecipe.java |
| tconstruct:damagable_melting | DamageableMeltingRecipe | TC/library/recipe/melting/DamageableMeltingRecipe.java |
| tconstruct:material_melting | MaterialMeltingRecipe | TC/library/recipe/melting/MaterialMeltingRecipe.java |
| tconstruct:melting_fuel | MeltingFuel | TC/library/recipe/fuel/MeltingFuel.java |
| tconstruct:entity_melting | EntityMeltingRecipe | TC/library/recipe/entitymelting/EntityMeltingRecipe.java |
| tconstruct:alloy | AlloyRecipe | TC/library/recipe/alloying/AlloyRecipe.java |

#### 1.2 TinkerTables（tables/TinkerTables.java，16 个）
| ID | 配方类 | 源文件路径 |
|---|---|---|
| tconstruct:material | MaterialRecipe | TC/library/recipe/material/MaterialRecipe.java |
| tconstruct:tool_building | ToolBuildingRecipe | TC/library/recipe/tinkerstation/building/ToolBuildingRecipe.java |
| tconstruct:tinker_station_part_swapping | TinkerStationPartSwapping | TC/tables/recipe/TinkerStationPartSwapping.java |
| tconstruct:tinker_station_damaging | TinkerStationDamagingRecipe | TC/tables/recipe/TinkerStationDamagingRecipe.java |
| tconstruct:fixed_material_swapping | FixedMaterialSwappingRecipe | TC/library/recipe/tinkerstation/building/FixedMaterialSwappingRecipe.java |
| tconstruct:part_swapping_override | PartSwappingOverrideRecipe | TC/library/recipe/tinkerstation/building/PartSwappingOverrideRecipe.java |
| tconstruct:tool_material_swapping | ToolMaterialSwappingRecipe | TC/library/recipe/tinkerstation/building/ToolMaterialSwappingRecipe.java |
| tconstruct:crafting_shaped_material | ShapedMaterialRecipe（vanilla Shaped 子类 + 自定义 Serializer） | TC/library/recipe/material/ShapedMaterialRecipe.java |
| tconstruct:crafting_shaped_materials | ShapedMaterialsRecipe | TC/library/recipe/material/ShapedMaterialsRecipe.java |
| tconstruct:crafting_shapeless_materials | ShapelessMaterialsRecipe | TC/library/recipe/material/ShapelessMaterialsRecipe.java |
| tconstruct:part_builder | PartRecipe | TC/library/recipe/partbuilder/PartRecipe.java |
| tconstruct:item_part_builder | ItemPartRecipe | TC/library/recipe/partbuilder/ItemPartRecipe.java |
| tconstruct:part_builder_tool_recycling | PartBuilderToolRecycle | TC/tables/recipe/PartBuilderToolRecycle.java |
| tconstruct:part_builder_recycling | PartBuilderRecycle | TC/library/recipe/partbuilder/recycle/PartBuilderRecycle.java |
| tconstruct:tinker_station_repair | TinkerStationRepairRecipe（SimpleRecipeSerializer） | TC/tables/recipe/TinkerStationRepairRecipe.java |
| tconstruct:crafting_table_repair | CraftingTableRepairKitRecipe（SimpleRecipeSerializer） | TC/tables/recipe/CraftingTableRepairKitRecipe.java |

#### 1.3 TinkerModifiers（tools/TinkerModifiers.java，28 个）
| ID | 配方类 | 源文件路径 |
|---|---|---|
| tconstruct:modifier | ModifierRecipe | TC/library/recipe/modifiers/adding/ModifierRecipe.java |
| tconstruct:incremental_modifier | IncrementalModifierRecipe | TC/library/recipe/modifiers/adding/IncrementalModifierRecipe.java |
| tconstruct:swappable_modifier | SwappableModifierRecipe | TC/library/recipe/modifiers/adding/SwappableModifierRecipe.java |
| tconstruct:multilevel_modifier | MultilevelModifierRecipe | TC/library/recipe/modifiers/adding/MultilevelModifierRecipe.java |
| tconstruct:multilevel_incremental_modifier | MultilevelIncrementalModifierRecipe | TC/library/recipe/modifiers/adding/MultilevelIncrementalModifierRecipe.java |
| tconstruct:overslime_modifier | OverslimeModifierRecipe | TC/library/recipe/modifiers/adding/OverslimeModifierRecipe.java |
| tconstruct:crafting_overslime_modifier | OverslimeCraftingTableRecipe | TC/library/recipe/modifiers/adding/OverslimeCraftingTableRecipe.java |
| tconstruct:modifier_salvage | ModifierSalvage | TC/library/recipe/modifiers/ModifierSalvage.java |
| tconstruct:armor_dyeing_modifier | ArmorDyeingRecipe（SimpleRecipeSerializer） | TC/tools/recipe/ArmorDyeingRecipe.java |
| tconstruct:banner_modifier | BannerModifierRecipe（SimpleRecipeSerializer） | TC/tools/recipe/BannerModifierRecipe.java |
| tconstruct:armor_trim_modifier | ArmorTrimRecipe（SimpleRecipeSerializer） | TC/tools/recipe/ArmorTrimRecipe.java |
| tconstruct:tipped_tool_transform | TippedToolTransformRecipe | TC/tools/recipe/TippedToolTransformRecipe.java |
| tconstruct:modifier_repair | ModifierRepairTinkerStationRecipe | TC/library/recipe/tinkerstation/repairing/ModifierRepairTinkerStationRecipe.java |
| tconstruct:crafting_modifier_repair | ModifierRepairCraftingRecipe | TC/library/recipe/tinkerstation/repairing/ModifierRepairCraftingRecipe.java |
| tconstruct:modifier_material_repair（已 deprecated） | ModifierMaterialRepairRecipe | TC/library/recipe/tinkerstation/repairing/ModifierMaterialRepairRecipe.java |
| tconstruct:crafting_modifier_material_repair（已 deprecated） | ModifierMaterialRepairKitRecipe | TC/library/recipe/tinkerstation/repairing/ModifierMaterialRepairKitRecipe.java |
| tconstruct:remove_modifier | ModifierRemovalRecipe | TC/tools/recipe/ModifierRemovalRecipe.java |
| tconstruct:extract_modifier | ExtractModifierRecipe | TC/tools/recipe/ExtractModifierRecipe.java |
| tconstruct:modifier_sorting | ModifierSortingRecipe | TC/tools/recipe/ModifierSortingRecipe.java |
| tconstruct:modifier_set_worktable | ModifierSetWorktableRecipe | TC/library/recipe/worktable/ModifierSetWorktableRecipe.java |
| tconstruct:enchantment_converting | EnchantmentConvertingRecipe | TC/tools/recipe/EnchantmentConvertingRecipe.java |
| tconstruct:toggle_interaction | ToggleInteractionWorktableRecipe | TC/tools/recipe/ToggleInteractionWorktableRecipe.java |
| tconstruct:severing | SeveringRecipe | TC/library/recipe/modifiers/severing/SeveringRecipe.java |
| tconstruct:ageable_severing | AgeableSeveringRecipe | TC/library/recipe/modifiers/severing/AgeableSeveringRecipe.java |
| tconstruct:player_beheading | PlayerBeheadingRecipe | TC/tools/recipe/severing/PlayerBeheadingRecipe.java |
| tconstruct:snow_golem_beheading | SnowGolemBeheadingRecipe | TC/tools/recipe/severing/SnowGolemBeheadingRecipe.java |
| tconstruct:mooshroom_demushrooming | MooshroomDemushroomingRecipe | TC/tools/recipe/severing/MooshroomDemushroomingRecipe.java |
| tconstruct:sheep_shearing | SheepShearingRecipe | TC/tools/recipe/severing/SheepShearingRecipe.java |

---

### 2. 每个序列化器的字段表与示例

约定：「必填」= requiredField；「默认」= defaultField；「可选」= nullableField / emptyField。Ingredient 指 Minecraft 原版 Ingredient JSON（`item` / `tag` / 数组 / 自定义 `type`）；DISALLOW 不允许空，ALLOW 允许空。

**[1] tconstruct:casting_basin / tconstruct:casting_table**（ItemCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| cast | 否 | EMPTY | Ingredient（ALLOW_EMPTY）|
| fluid | 是 | - | FluidIngredient |
| result | 是 | - | ItemOutput（REQUIRED_ITEM，不读 count）|
| cooling_time | 是 | - | int ≥1 |
| cast_consumed | 否 | false | boolean |
| switch_slots | 否 | false | boolean |

示例（casting_basin）：`smeltery/casting/...` 对应 `./compat/ceramics/casting/golden_bricks.json`
```json
{
  "type": "tconstruct:casting_basin",
  "cast": {
    "item": "ceramics:porcelain_bricks"
  },
  "cast_consumed": true,
  "conditions": [
    {
      "type": "forge:mod_loaded",
      "modid": "ceramics"
    }
  ],
  "cooling_time": 19,
  "fluid": {
    "amount": 10,
    "tag": "forge:molten_gold"
  },
  "result": "ceramics:golden_bricks"
}
```
示例（casting_table）：`./common/encyclopedia.json`
```json
{
  "type": "tconstruct:casting_table",
  "cast": {
    "item": "minecraft:book"
  },
  "cast_consumed": true,
  "cooling_time": 57,
  "fluid": {
    "amount": 90,
    "tag": "forge:molten_gold"
  },
  "result": "tconstruct:encyclopedia"
}
```

**[2] tconstruct:basin_filling / tconstruct:table_filling**（ContainerFillingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| fluid_amount | 是 | - | int ≥1 |
| container | 是 | - | Item 物品 ID（Loadables.ITEM）|

示例（basin_filling）：`./smeltery/casting/filling/scorched_fuel_gauge.json`
```json
{
  "type": "tconstruct:basin_filling",
  "container": "tconstruct:scorched_fuel_gauge",
  "fluid_amount": 250
}
```
示例（table_filling）：`./compat/ceramics/filling_clay_bucket.json`
```json
{
  "type": "tconstruct:table_filling",
  "conditions": [
    {
      "type": "forge:mod_loaded",
      "modid": "ceramics"
    }
  ],
  "container": "ceramics:empty_clay_bucket",
  "fluid_amount": 1000
}
```

**[3] tconstruct:basin_duplication / tconstruct:table_duplication**（CastDuplicationRecipe extends ItemCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| cast | 是 | - | Ingredient（DISALLOW_EMPTY）|
| fluid | 是 | - | FluidIngredient |
| cooling_time | 是 | - | int ≥1 |

- basin_duplication：无示例（JSON 中 0 个文件）。
- table_duplication 示例：`./smeltery/casting/diamond/smithing_template.json`
```json
{
  "type": "tconstruct:table_duplication",
  "cast": [
    {
      "item": "minecraft:netherite_upgrade_smithing_template"
    },
    {
      "tag": "minecraft:trim_templates"
    }
  ],
  "cooling_time": 186,
  "fluid": {
    "amount": 500,
    "tag": "tconstruct:molten_diamond"
  }
}
```

**[4] tconstruct:casting_basin_potion / tconstruct:casting_table_potion**（PotionCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| bottle | 是 | - | Ingredient（DISALLOW_EMPTY）|
| fluid | 是 | - | FluidIngredient |
| result | 是 | - | Item 物品 ID |
| cooling_time | 否 | 5 | int ≥1 |

- casting_basin_potion：无示例。
- casting_table_potion 示例：`./smeltery/casting/filling/bottle.json`
```json
{
  "type": "tconstruct:casting_table_potion",
  "bottle": {
    "item": "minecraft:glass_bottle"
  },
  "fluid": {
    "amount": 250,
    "tag": "forge:potion"
  },
  "result": "minecraft:potion"
}
```

**[5] tconstruct:casting_basin_tipping / tconstruct:casting_table_tipping**（TippingCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| tools | 是 | - | Ingredient（DISALLOW_EMPTY）|
| fluid | 是 | - | FluidIngredient |
| cooling_time | 否 | 5 | int ≥1 |
| modifier | 是 | - | ModifierId |

- casting_basin_tipping：无示例。
- casting_table_tipping 示例：`./tools/modifiers/slotless/ammo_tipping.json`
```json
{
  "type": "tconstruct:casting_table_tipping",
  "cooling_time": 20,
  "fluid": {
    "amount": 50,
    "tag": "forge:potion"
  },
  "modifier": "tconstruct:tipped",
  "tools": {
    "tag": "tconstruct:modifiable/ammo"
  }
}
```

**[6] tconstruct:casting_basin_tipped_clearing / tconstruct:casting_table_tipped_clearing**（TipClearingCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| tools | 是 | - | Ingredient（DISALLOW_EMPTY）|
| fluid | 是 | - | FluidIngredient |
| cooling_time | 否 | 5 | int ≥1 |
| modifier | 是 | - | ModifierId |

- casting_basin_tipped_clearing：无示例。
- casting_table_tipped_clearing 示例：`./tools/modifiers/slotless/ammo_tip_clearing.json`
```json
{
  "type": "tconstruct:casting_table_tipped_clearing",
  "cooling_time": 20,
  "fluid": {
    "amount": 50,
    "tag": "mantle:water"
  },
  "modifier": "tconstruct:tipped",
  "tools": {
    "tag": "tconstruct:modifiable/ammo"
  }
}
```

**[7] tconstruct:retextured_casting_basin / tconstruct:retextured_casting_table**（RetexturedCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| cast | 否 | EMPTY | Ingredient（ALLOW_EMPTY）|
| fluid | 是 | - | FluidIngredient |
| result | 是 | - | ItemOutput（REQUIRED_ITEM）|
| cooling_time | 是 | - | int ≥1 |
| cast_consumed | 否 | false | boolean |
| switch_slots | 否 | false | boolean |

- retextured_casting_table：无示例。
- retextured_casting_basin 示例：`./smeltery/casting/scorched/foundry_controller.json`
```json
{
  "type": "tconstruct:retextured_casting_basin",
  "cast": {
    "tag": "tconstruct:foundry_bricks"
  },
  "cast_consumed": true,
  "cooling_time": 221,
  "fluid": {
    "amount": 1000,
    "tag": "tconstruct:molten_obsidian"
  },
  "result": "tconstruct:foundry_controller"
}
```

**[8] tconstruct:basin_casting_material / tconstruct:table_casting_material**（MaterialCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| cast | 否 | EMPTY | Ingredient（ALLOW_EMPTY）|
| item_cost | 是 | - | int ≥1 |
| result | 是 | - | IMaterialItem 物品 ID（TinkerLoadables.MATERIAL_ITEM）|
| allowed_materials | 否 | `MaterialPredicate.ANY`（任意材料） | MaterialPredicate |
| cast_consumed | 否 | false | boolean |
| switch_slots | 否 | false | boolean |

- basin_casting_material 示例：`./tools/parts/fake_storage_block_casting.json`
```json
{
  "type": "tconstruct:basin_casting_material",
  "item_cost": 9,
  "result": "tconstruct:fake_storage_block"
}
```
- table_casting_material 示例：`./tools/parts/casting/adze_head_gold_cast.json`
```json
{
  "type": "tconstruct:table_casting_material",
  "cast": {
    "tag": "tconstruct:casts/multi_use/adze_head"
  },
  "item_cost": 2,
  "result": "tconstruct:adze_head"
}
```

**[9] tconstruct:basin_casting_composite / tconstruct:table_casting_composite**（CompositeCastingRecipe extends MaterialCastingRecipe；LOADER 不含 cast/cast_consumed/switch_slots）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| item_cost | 是 | - | int ≥1 |
| result | 是 | - | IMaterialItem 物品 ID |
| allowed_materials | 否 | ANY | MaterialPredicate |
| casting_stat_conflict | 否（nullable）| null | MaterialStatsId |

- basin_casting_composite 示例：`./tools/parts/fake_storage_block_composite.json`
```json
{
  "type": "tconstruct:basin_casting_composite",
  "item_cost": 9,
  "result": "tconstruct:fake_storage_block"
}
```
- table_casting_composite 示例：`./tools/parts/casting/adze_head_composite.json`
```json
{
  "type": "tconstruct:table_casting_composite",
  "item_cost": 2,
  "result": "tconstruct:adze_head"
}
```

**[10] tconstruct:basin_tool_casting / tconstruct:table_tool_casting**（ToolCastingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| cast | 否 | EMPTY | Ingredient（ALLOW_EMPTY）|
| item_cost | 是 | - | int ≥1 |
| cast_purpose | 否 | MAYBE_MATERIAL | enum：MAYBE_MATERIAL（已 deprecated）/ CATALYST / CONSUMED / CONSUMED_OFFSET / FIRST_MATERIAL / SECOND_MATERIAL |
| result | 是 | - | IModifiable 物品 ID（MODIFIABLE_ITEM）|
| allowed_materials | 否 | ANY | MaterialPredicate |
| extra_materials | 否 | [] | MaterialVariantId 数组 |

- basin_tool_casting 示例：`./tools/armor/slimelytra.json`
```json
{
  "type": "tconstruct:basin_tool_casting",
  "cast": {
    "item": "minecraft:elytra"
  },
  "cast_purpose": "consumed",
  "item_cost": 8,
  "result": "tconstruct:slime_wings"
}
```
- table_tool_casting 示例：`./tools/armor/plate/plate_shield.json`
```json
{
  "type": "tconstruct:table_tool_casting",
  "cast": {
    "type": "tconstruct:material",
    "item": "tconstruct:shield_core"
  },
  "cast_purpose": "first_material",
  "item_cost": 3,
  "result": "tconstruct:plate_shield"
}
```

**[10b] tconstruct:basin_casting_part_swapping / tconstruct:table_casting_part_swapping**（PartSwapCastingRecipe，主对话补充）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| tools | 是 | - | Ingredient（ALLOW_EMPTY，作为浇铸位上的工具）|
| item_cost | 是 | - | int ≥1 |
| index | 是 | - | int ≥0，替换的部件序号 |
| allowed_materials | 否 | ANY | MaterialPredicate |

示例：`./tools/armor/plate/boots_swapping.json`
```json
{
  "type": "tconstruct:table_casting_part_swapping",
  "index": 0,
  "item_cost": 2,
  "tools": {
    "item": "tconstruct:plate_boots"
  }
}
```

**[11] tconstruct:material_fluid**（MaterialFluidRecipe，RecipeSerializer；无 group 字段）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| fluid | 是 | - | FluidIngredient |
| temperature | 是 | - | int ≥0 |
| input | 否（nullable）| null | MaterialVariantId |
| output | 否（nullable）| null | MaterialVariantId |
| hide_in_book | 否 | false | boolean |

示例：`./tools/materials/casting/aluminum.json`
```json
{
  "type": "tconstruct:material_fluid",
  "conditions": [
    {
      "type": "mantle:tag_filled",
      "tag": "forge:ingots/aluminum"
    }
  ],
  "fluid": {
    "amount": 90,
    "tag": "forge:molten_aluminum"
  },
  "output": "tconstruct:aluminum",
  "temperature": 425
}
```

**[12] tconstruct:molding_basin / tconstruct:molding_table**（MoldingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| material | 是 | - | Ingredient（DISALLOW_EMPTY）|
| pattern | 否 | EMPTY | Ingredient（ALLOW_EMPTY）|
| pattern_consumed | 否 | false | boolean |
| result | 是 | - | ItemOutput（REQUIRED_ITEM）|

- molding_basin：无示例。
- molding_table 示例：`./smeltery/casts/red_sand/molding/adze_head.json`
```json
{
  "type": "tconstruct:molding_table",
  "material": {
    "tag": "tconstruct:casts/red_sand"
  },
  "pattern": {
    "type": "tconstruct:material",
    "item": "tconstruct:adze_head"
  },
  "result": "tconstruct:adze_head_red_sand_cast"
}
```

**[13] tconstruct:melting**（MeltingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| ingredient | 是 | - | Ingredient（DISALLOW_EMPTY）|
| result | 是 | - | FluidOutput（Loadable.REQUIRED，对象形式）|
| temperature | 是 | - | int ≥0 |
| time | 是 | - | int ≥1 |
| byproducts | 否 | [] | FluidOutput 数组 |

示例：`./compat/ceramics/clay/block.json`（带 forge:mod_loaded 条件，已截取前 30 行）
```json
{
  "type": "tconstruct:melting",
  "conditions": [
    {
      "type": "forge:mod_loaded",
      "modid": "ceramics"
    }
  ],
  "ingredient": [
    {
      "item": "ceramics:dark_bricks"
    },
    {
      "item": "ceramics:dark_bricks_stairs"
    },
    {
      "item": "ceramics:dark_bricks_wall"
    },
    {
      "item": "ceramics:dragon_bricks"
    },
    {
      "item": "ceramics:dragon_bricks_stairs"
    },
    {
      "item": "ceramics:dragon_bricks_wall"
    }
  ],
  "result": {
    "amount": 1000,
```

**[14] tconstruct:ore_melting**（OreMeltingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| ingredient | 是 | - | Ingredient |
| result | 是 | - | FluidOutput |
| temperature | 是 | - | int ≥0 |
| time | 是 | - | int ≥1 |
| byproducts | 否 | [] | FluidOutput 数组；每个元素可写 rate，缺省 default（MergingListField 合并）|
| rate | 是 | - | enum：none / metal / gem / default（TinkerLoadables.ORE_RATE_TYPE）|

示例：`./smeltery/melting/amethyst/bud_large.json`
```json
{
  "type": "tconstruct:ore_melting",
  "byproducts": [
    {
      "amount": 300,
      "fluid": "tconstruct:molten_quartz"
    }
  ],
  "ingredient": {
    "item": "minecraft:large_amethyst_bud"
  },
  "rate": "gem",
  "result": {
    "amount": 300,
    "fluid": "tconstruct:molten_amethyst"
  },
  "temperature": 950,
  "time": 194
}
```

**[15] tconstruct:damagable_melting**（DamageableMeltingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| ingredient | 是 | - | Ingredient |
| result | 是 | - | FluidOutput；可额外写 unit_size（MergingField，默认 1）|
| temperature | 是 | - | int ≥0 |
| time | 是 | - | int ≥1 |
| byproducts | 否 | [] | FluidOutput 数组；元素可写 unit_size（默认 1）|

示例：`./compat/ceramics/clay/clay_boots.json`
```json
{
  "type": "tconstruct:damagable_melting",
  "conditions": [
    {
      "type": "forge:mod_loaded",
      "modid": "ceramics"
    }
  ],
  "ingredient": {
    "item": "ceramics:clay_boots"
  },
  "result": {
    "amount": 1000,
    "fluid": "tconstruct:molten_clay",
    "unit_size": 50
  },
  "temperature": 450,
  "time": 96
}
```

**[16] tconstruct:material_melting**（MaterialMeltingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| input | 是 | - | MaterialVariantId |
| temperature | 是 | - | int ≥1 |
| result | 是 | - | FluidOutput（REQUIRED）|
| byproducts | 否 | [] | FluidOutput 数组 |

示例：`./tools/materials/melting/aluminum.json`
```json
{
  "type": "tconstruct:material_melting",
  "conditions": [
    {
      "type": "mantle:tag_filled",
      "tag": "forge:ingots/aluminum"
    }
  ],
  "input": "tconstruct:aluminum",
  "result": {
    "amount": 90,
    "tag": "forge:molten_aluminum"
  },
  "temperature": 425
}
```

**[17] tconstruct:melting_fuel**（MeltingFuel）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| fluid | 否 | EMPTY | FluidIngredient |
| duration | 否 | 0 | int ≥1（若 fluid 非空则必须 >0，validate 校验）|
| temperature | 是 | - | int ≥1 |
| rate | 是 | - | int ≥1 |

示例：`./smeltery/melting/fuel/blaze.json`
```json
{
  "type": "tconstruct:melting_fuel",
  "duration": 150,
  "fluid": {
    "amount": 50,
    "fluid": "tconstruct:blazing_blood"
  },
  "rate": 15,
  "temperature": 1500
}
```

**[18] tconstruct:entity_melting**（EntityMeltingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| entity | 是 | - | EntityIngredient（`type` / `tag` / `types` 数组，或数组复合）|
| result | 是 | - | FluidOutput（REQUIRED）|
| damage | 否 | 2 | int ≥1 |

示例：`./smeltery/entity_melting/bee.json`
```json
{
  "type": "tconstruct:entity_melting",
  "damage": 2,
  "entity": {
    "tag": "forge:bees"
  },
  "result": {
    "amount": 25,
    "tag": "forge:honey"
  }
}
```

**[19] tconstruct:alloy**（AlloyRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| inputs | 是 | - | AlloyIngredient 数组，至少 2 个（list(2)，不允许单对象）|
| result | 是 | - | FluidOutput（REQUIRED）|
| temperature | 是 | - | int ≥1 |

AlloyIngredient 元素：直接写 FluidIngredient 字段（amount + fluid/tag），或 `{"match": FluidIngredient, "catalyst": bool}`。`catalyst` 默认 false。

示例：`./compat/ceramics/alloy_porcelain.json`
```json
{
  "type": "tconstruct:alloy",
  "conditions": [
    {
      "type": "forge:mod_loaded",
      "modid": "ceramics"
    }
  ],
  "inputs": [
    {
      "amount": 750,
      "tag": "tconstruct:molten_clay"
    },
    {
      "amount": 100,
      "tag": "tconstruct:molten_quartz"
    }
  ],
  "result": {
    "amount": 1000,
    "fluid": "tconstruct:molten_porcelain"
  },
  "temperature": 700
}
```

**[20] tconstruct:material**（MaterialRecipe，TinkerTables 注册）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| ingredient | 是 | - | Ingredient（DISALLOW_EMPTY）|
| value | 否 | 1 | int ≥1（IMaterialValue.VALUE_FIELD）|
| needed | 否 | 1 | int ≥1（IMaterialValue.NEEDED_FIELD）|
| material | 是 | - | MaterialVariantId |
| leftover | 否 | EMPTY | ItemOutput（OPTIONAL_STACK，支持 count）|

示例：`./tools/materials/aluminum/block.json`（注意：此处 `tconstruct:material` 为顶层配方类型；嵌套的 `"type": "tconstruct:material"` 是 Ingredient 类型，不是配方）
```json
{
  "type": "tconstruct:material",
  "conditions": [
    {
      "type": "mantle:tag_filled",
      "tag": "forge:storage_blocks/aluminum"
    }
  ],
  "ingredient": {
    "tag": "forge:storage_blocks/aluminum"
  },
  "leftover": {
    "count": 1,
    "tag": "forge:ingots/aluminum"
  },
  "material": "tconstruct:aluminum",
  "needed": 1,
  "value": 9
}
```

**[21] tconstruct:tool_building**（ToolBuildingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| result | 是 | - | IModifiable 物品 ID |
| result_count | 否 | 1 | int ≥1 |
| slot_layout | 否（nullable）| null | ResourceLocation |
| extra_requirements | 否 | [] | Ingredient 数组（DISALLOW_EMPTY）|
| parts_override | 否（nullable）| null | IToolPart 物品数组 |
| extra_materials | 否 | [] | MaterialVariantId 数组 |

示例：`./tools/armor/plate/plate_boots.json`
```json
{
  "type": "tconstruct:tool_building",
  "result": "tconstruct:plate_boots",
  "result_count": 1,
  "slot_layout": "tconstruct:plate_armor"
}
```

**[22] tconstruct:tinker_station_part_swapping**（TinkerStationPartSwapping extends MaterialSwappingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 是 | - | Ingredient（DISALLOW_EMPTY）|
| max_stack_size | 否 | 16 | int ≥1 |
| extra_requirements | 否 | [] | SizedIngredient 数组 |

示例：`./tables/ammo_part_swapping.json`
```json
{
  "type": "tconstruct:tinker_station_part_swapping",
  "max_stack_size": 4,
  "tools": [
    {
      "item": "tconstruct:arrow"
    },
    {
      "item": "tconstruct:shuriken"
    }
  ]
}
```

**[23] tconstruct:tool_material_swapping**（ToolMaterialSwappingRecipe extends MaterialSwappingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 是 | - | Ingredient |
| max_stack_size | 否 | 16 | int ≥1 |
| extra_requirements | 否 | [] | SizedIngredient 数组 |

示例：`./tables/tool_material_swapping.json`
```json
{
  "type": "tconstruct:tool_material_swapping",
  "max_stack_size": 16,
  "tools": {
    "type": "forge:difference",
    "base": {
      "tag": "tconstruct:modifiable/multipart"
    },
    "subtracted": {
      "tag": "tconstruct:modifiable/unswappable/tools"
    }
  }
}
```

**[24] tconstruct:tinker_station_damaging**（TinkerStationDamagingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| ingredient | 是 | - | Ingredient（DISALLOW_EMPTY）|
| damage_amount | 是 | - | int ≥1 |

示例：`./tables/tinker_station_damaging/blazing_bucket.json`
```json
{
  "type": "tconstruct:tinker_station_damaging",
  "damage_amount": 2500,
  "ingredient": {
    "item": "tconstruct:blazing_blood_bucket"
  }
}
```

**[25] tconstruct:fixed_material_swapping**（FixedMaterialSwappingRecipe extends MaterialSwappingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 是 | - | Ingredient |
| max_stack_size | 否 | 16 | int ≥1 |
| extra_requirements | 否 | [] | SizedIngredient 数组 |
| ingredient | 是 | - | SizedIngredient |
| material | 是 | - | MaterialVariantId |
| index | 是 | - | int 数组（IntArrayLoadable，元素 ≥0，1–10 个，允许单个整数简写）|
| repair_value | 否 | 0 | int ≥0 |

示例：`./tools/armor/slime_skull/swapping/blaze.json`
```json
{
  "type": "tconstruct:fixed_material_swapping",
  "index": 0,
  "ingredient": {
    "item": "tconstruct:blaze_head"
  },
  "material": "tconstruct:blaze",
  "max_stack_size": 16,
  "repair_value": 6,
  "tools": {
    "tag": "tconstruct:modifiable/skulls/swappable"
  }
}
```

**[26] tconstruct:part_swapping_override**（PartSwappingOverrideRecipe extends MaterialSwappingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 是 | - | Ingredient |
| max_stack_size | 否 | 16 | int ≥1 |
| extra_requirements | 否 | [] | SizedIngredient 数组 |
| part | 是 | - | IToolPart 物品 ID（TOOL_PART_ITEM）|
| index | 是 | - | int 数组（同上）|

示例：无示例。

**[27] tconstruct:crafting_shaped_material**（ShapedMaterialRecipe，自定义 Serializer，非 LoadableRecipeSerializer）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| 原版 Shaped 字段 | 是 | - | group / category / key / pattern / result（vanilla 解析）|
| extra_materials | 否 | [] | MaterialVariantId 数组 |

约束：解析后 `getMaterial()` 为空则抛错「Invalid material ingredients」，即 key 中必须至少有一个材料 Ingredient。
示例：无示例。

**[28] tconstruct:crafting_shaped_materials**（ShapedMaterialsRecipe，自定义 Serializer）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| category | 否 | misc | CraftingBookCategory |
| key | 是 | - | 原版 key 映射 |
| pattern | 是 | - | 原版 pattern 字符串数组 |
| result | 是 | - | ItemStack |
| show_notification | 否 | true | boolean |
| parts | 是 | - | string，每个字符对应 key 中的一个 Ingredient，决定部件顺序 |
| extra_materials | 否 | [] | MaterialVariantId 数组 |

示例：`./tables/scorched_anvil_material.json`
```json
{
  "type": "tconstruct:crafting_shaped_materials",
  "category": "misc",
  "key": {
    "m": {
      "type": "tconstruct:material",
      "item": "tconstruct:fake_storage_block",
      "material": {
        "type": "mantle:tag",
        "tag": "tconstruct:compatibility_metals/alloys"
      }
    },
    "s": {
      "tag": "tconstruct:scorched_blocks"
    }
  },
  "parts": "m",
  "pattern": [
    "mmm",
    " s ",
    "sss"
  ],
  "result": {
    "item": "tconstruct:scorched_anvil"
```

**[29] tconstruct:crafting_shapeless_materials**（ShapelessMaterialsRecipe，自定义 Serializer）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| 原版 Shapeless 字段 | 是 | - | group / category / ingredients / result |
| parts | 是 | - | int，范围 1 到 ingredients 数量 |
| extra_materials | 否 | [] | MaterialVariantId 数组 |

示例：`./tools/parts/fake_block_to_ingots.json`
```json
{
  "type": "tconstruct:crafting_shapeless_materials",
  "category": "misc",
  "ingredients": [
    {
      "type": "tconstruct:material",
      "item": "tconstruct:fake_storage_block",
      "material": {
        "type": "tconstruct:has_part",
        "part": "tconstruct:fake_ingot"
      }
    }
  ],
  "parts": 1,
  "result": {
    "count": 9,
    "item": "tconstruct:fake_ingot"
  }
}
```

**[30] tconstruct:part_builder**（PartRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| pattern | 是 | - | Pattern（ID）|
| pattern_item | 否 | DEFAULT_PATTERNS（tag tconstruct:patterns/default）| Ingredient（DISALLOW_EMPTY）|
| cost | 是 | - | int ≥1 |
| allow_uncraftable | 否 | false | boolean |
| result.item | 是 | - | IMaterialItem 物品 ID（MergingField，result 对象内）|
| result.count | 否 | 1 | int ≥1 |

示例：`./tools/parts/builder/adze_head.json`
```json
{
  "type": "tconstruct:part_builder",
  "cost": 2,
  "pattern": "tconstruct:adze_head",
  "pattern_item": [
    {
      "tag": "tconstruct:patterns/default"
    },
    {
      "item": "tconstruct:adze_head_cast"
    }
  ],
  "result": {
    "item": "tconstruct:adze_head"
  }
}
```

**[31] tconstruct:item_part_builder**（ItemPartRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| material | 否 | MaterialId.UNKNOWN | MaterialVariantId |
| pattern | 是 | - | Pattern（ID）|
| pattern_item | 否 | DEFAULT_PATTERNS | Ingredient（DISALLOW_EMPTY）|
| cost | 否 | 0 | int ≥0（validate：material 非空时 cost 必须 >0）|
| result | 是 | - | ItemStack（REQUIRED_STACK，支持 count）|

示例：`./smeltery/casts/red_sand/builder_block/adze_head.json`
```json
{
  "type": "tconstruct:item_part_builder",
  "pattern": "tconstruct:adze_head",
  "pattern_item": {
    "tag": "forge:sand/red"
  },
  "result": {
    "count": 4,
    "item": "tconstruct:adze_head_red_sand_cast"
  }
}
```

**[32] tconstruct:part_builder_tool_recycling**（PartBuilderToolRecycle）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 否 | SizedIngredient.fromTag(tconstruct:multipart_tool) | SizedIngredient |
| pattern | 是 | - | Ingredient（DISALLOW_EMPTY）|
| parts | 否 | [] | IMaterialItem 数组 |

示例：`./tools/recycling/battlesign.json`
```json
{
  "type": "tconstruct:part_builder_tool_recycling",
  "parts": [
    "tconstruct:large_plate",
    "tconstruct:repair_kit"
  ],
  "pattern": {
    "item": "tconstruct:venom_bottle"
  },
  "tools": {
    "item": "tconstruct:battlesign"
  }
}
```

**[33] tconstruct:part_builder_recycling**（PartBuilderRecycle）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tool | 是 | - | Ingredient（DISALLOW_EMPTY）|
| pattern | 是 | - | Ingredient（DISALLOW_EMPTY）|
| results | 是 | - | Map<物品 ID 字符串, ItemStack（REQUIRED_STACK，支持 count）>|

示例：`./tables/recycling/bow.json`
```json
{
  "type": "tconstruct:part_builder_recycling",
  "pattern": {
    "item": "tconstruct:venom_bottle"
  },
  "results": {
    "tconstruct:bowstring": {
      "count": 3,
      "item": "minecraft:string"
    },
    "tconstruct:rod": {
      "count": 3,
      "item": "minecraft:stick"
    }
  },
  "tool": {
    "item": "minecraft:bow"
  }
}
```

**[34] tconstruct:tinker_station_repair**（SimpleRecipeSerializer）
无额外字段，仅 `type`。
示例：`./tables/tinker_station_repair.json`
```json
{
  "type": "tconstruct:tinker_station_repair"
}
```

**[35] tconstruct:crafting_table_repair**（SimpleRecipeSerializer）
无额外字段，仅 `type`。
示例：`./tables/crafting_table_repair.json`
```json
{
  "type": "tconstruct:crafting_table_repair"
}
```

**[36] tconstruct:modifier**（ModifierRecipe extends AbstractModifierRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| inputs | 是 | - | SizedIngredient 数组，至少 1 个，不允许单对象简写 |
| tools | 是 | - | Ingredient（DISALLOW_EMPTY）|
| max_tool_size | 否 | 16 | int ≥1 |
| result | 是 | - | ModifierId |
| level | 否 | 1–32767 全范围（`IntRange.defaultField` 以自身为默认值）| IntRange：整数或 `{"min":..,"max":..}` |
| slots | 否（nullable）| null | SlotType.SlotCount |
| allow_crystal | 否 | true | boolean |
| check_trait_level | 否 | false | boolean |

示例：`./tools/modifiers/ability/ambidextrous.json`
```json
{
  "type": "tconstruct:modifier",
  "allow_crystal": true,
  "check_trait_level": true,
  "inputs": [
    {
      "item": "minecraft:leather"
    },
    {
      "tag": "forge:gems/diamond"
    },
    {
      "item": "minecraft:leather"
    },
    {
      "tag": "forge:string"
    },
    {
      "tag": "forge:string"
    }
  ],
  "level": 1,
  "result": "tconstruct:ambidextrous",
  "slots": {
```

**[37] tconstruct:incremental_modifier**（IncrementalModifierRecipe）
在 AbstractModifierRecipe 字段基础上：
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| input | 是 | - | Ingredient（DISALLOW_EMPTY）|
| amount_per_item | 否 | 1 | int ≥1 |
| needed_per_level | 是 | - | int ≥1 |
| leftover | 否 | EMPTY | ItemOutput（OPTIONAL_STACK）|
| tools / max_tool_size / result / level / slots / allow_crystal / check_trait_level | 同 modifier | - | 同上 |

示例：`./tools/modifiers/ability/strength.json`
```json
{
  "type": "tconstruct:incremental_modifier",
  "allow_crystal": true,
  "amount_per_item": 1,
  "input": {
    "item": "tconstruct:ichor_slime_crystal"
  },
  "level": {
    "max": 2
  },
  "needed_per_level": 72,
  "result": "tconstruct:strength",
  "slots": {
    "abilities": 1
  },
  "tools": {
    "tag": "tconstruct:modifiable/armor/chestplate"
  }
}
```

**[38] tconstruct:swappable_modifier**（SwappableModifierRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| inputs | 是 | - | SizedIngredient 数组（至少 1）|
| tools | 是 | - | Ingredient |
| max_tool_size | 否 | 16 | int ≥1 |
| result.name | 是 | - | ModifierId（MergingField）|
| result.value | 是 | - | string |
| variant_formatter | 否 | DEFAULT | VariantFormatter |
| slots | 否 | null | SlotCount |
| allow_crystal | 否 | true | boolean |

示例：`./tools/modifiers/ability/crystalshot_amethyst.json`
```json
{
  "type": "tconstruct:swappable_modifier",
  "allow_crystal": false,
  "inputs": [
    {
      "item": "minecraft:amethyst_cluster"
    },
    {
      "item": "minecraft:blaze_rod"
    },
    {
      "item": "minecraft:amethyst_cluster"
    },
    {
      "tag": "forge:ingots/manyullyn"
    },
    {
      "tag": "forge:ingots/manyullyn"
    }
  ],
  "result": {
    "name": "tconstruct:crystalshot",
    "value": "amethyst"
  },
```

**[39] tconstruct:multilevel_modifier**（MultilevelModifierRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| inputs | 否 | [] | SizedIngredient 数组（允许单对象简写）|
| tools | 是 | - | Ingredient |
| max_tool_size | 否 | 16 | int ≥1 |
| result | 是 | - | ModifierId |
| allow_crystal | 否 | true | boolean |
| levels | 是 | - | LevelEntry 数组，至少 1 个；每个 `{level: 1–32767 必填, slots: SlotCount 可选}` |
| check_trait_level | 否 | false | boolean |

约束：inputs 为空且 allow_crystal=false 时报错。
示例：`./tools/modifiers/ability/reflecting.json`
```json
{
  "type": "tconstruct:multilevel_modifier",
  "allow_crystal": true,
  "inputs": [
    {
      "amount_needed": 4,
      "item": "tconstruct:sky_congealed_slime"
    },
    {
      "amount_needed": 4,
      "item": "tconstruct:ichor_congealed_slime"
    },
    {
      "amount_needed": 4,
      "item": "tconstruct:sky_congealed_slime"
    },
    {
      "amount_needed": 4,
      "item": "tconstruct:earth_congealed_slime"
    },
    {
      "amount_needed": 4,
      "item": "tconstruct:earth_congealed_slime"
    }
```

**[40] tconstruct:multilevel_incremental_modifier**（MultilevelIncrementalModifierRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| input | 是 | - | Ingredient（DISALLOW_EMPTY）|
| amount_per_item | 否 | 1 | int ≥1 |
| needed_per_level | 是 | - | int ≥1 |
| tools | 是 | - | Ingredient |
| max_tool_size | 否 | 16 | int ≥1 |
| result | 是 | - | ModifierId |
| leftover | 否 | EMPTY | ItemOutput |
| allow_crystal | 否 | true | boolean |
| levels | 是 | - | LevelEntry 数组（同 multilevel_modifier）|
| check_trait_level | 否 | false | boolean |

示例：`./tools/modifiers/upgrade/feather_fall.json`
```json
{
  "type": "tconstruct:multilevel_incremental_modifier",
  "allow_crystal": true,
  "amount_per_item": 1,
  "check_trait_level": true,
  "input": {
    "item": "minecraft:feather"
  },
  "levels": [
    {
      "level": 1,
      "slots": {
        "upgrades": 1
      }
    },
    {
      "level": 2
    },
    {
      "level": 3,
      "slots": {
        "upgrades": 1
      }
    },
```

**[41] tconstruct:overslime_modifier**（OverslimeModifierRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 否 | Ingredient.of(TinkerTags.Items.DURABILITY) | Ingredient（DISALLOW_EMPTY）|
| ingredient | 是 | - | Ingredient（DISALLOW_EMPTY）|
| restore_amount | 是 | - | int ≥1 |

示例：`./tools/modifiers/slotless/overslime/earth_ball.json`
```json
{
  "type": "tconstruct:overslime_modifier",
  "ingredient": {
    "item": "minecraft:slime_ball"
  },
  "restore_amount": 20,
  "tools": {
    "tag": "tconstruct:modifiable/durability"
  }
}
```

**[42] tconstruct:crafting_overslime_modifier**（OverslimeCraftingTableRecipe）
字段与 overslime_modifier 相同（tools 默认同上，ingredient 必填，restore_amount 必填）。
示例：`./tools/modifiers/slotless/overslime/earth_bottle_crafting_table.json`
```json
{
  "type": "tconstruct:crafting_overslime_modifier",
  "ingredient": {
    "item": "tconstruct:earth_slime_bottle"
  },
  "restore_amount": 20,
  "tools": {
    "tag": "tconstruct:modifiable/durability"
  }
}
```

**[43] tconstruct:modifier_salvage**（ModifierSalvage）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 是 | - | Ingredient（DISALLOW_EMPTY）|
| max_tool_size | 否 | 16 | int ≥1 |
| modifier | 是 | - | ModifierId |
| level | 否 | 1–32767 全范围 | IntRange |
| slots | 是 | - | SlotCount |

示例：`./tools/modifiers/salvage/ability/ambidextrous.json`
```json
{
  "type": "tconstruct:modifier_salvage",
  "modifier": "tconstruct:ambidextrous",
  "slots": {
    "abilities": 1
  },
  "tools": {
    "tag": "tconstruct:modifiable/melee/unarmed"
  }
}
```

**[44] tconstruct:armor_dyeing_modifier / tconstruct:banner_modifier / tconstruct:armor_trim_modifier**（SimpleRecipeSerializer，仅 type）
- armor_dyeing_modifier 示例：`./tools/modifiers/slotless/dyeing.json`
```json
{
  "type": "tconstruct:armor_dyeing_modifier"
}
```
- banner_modifier 示例：`./tools/modifiers/slotless/banner.json`
```json
{
  "type": "tconstruct:banner_modifier"
}
```
- armor_trim_modifier 示例：`./tools/modifiers/slotless/trim.json`
```json
{
  "type": "tconstruct:armor_trim_modifier"
}
```

**[45] tconstruct:tipped_tool_transform**（TippedToolTransformRecipe extends ToolBuildingRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| group | 否 | "" | string |
| result | 是 | - | IModifiable 物品 ID |
| slot_layout | 否 | null | ResourceLocation |
| input | 是 | - | Ingredient（DISALLOW_EMPTY）|
| materials | 否 | [] | MaterialVariantId 数组 |
| modifier | 是 | - | ModifierId |

示例：`./tools/building/arrow_from_tipped.json`
```json
{
  "type": "tconstruct:tipped_tool_transform",
  "input": {
    "type": "mantle:potion_display",
    "item": "minecraft:tipped_arrow"
  },
  "materials": [
    "tconstruct:flint",
    "tconstruct:wood",
    "tconstruct:feather"
  ],
  "modifier": "tconstruct:tipped",
  "result": "tconstruct:arrow",
  "slot_layout": "tconstruct:tinker_station"
}
```

**[46] tconstruct:modifier_repair**（ModifierRepairTinkerStationRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| modifier | 是 | - | ModifierId |
| ingredient | 是 | - | Ingredient（DISALLOW_EMPTY）|
| repair_amount | 是 | - | int ≥1 |

示例：`./tools/modifiers/tasty.json`
```json
{
  "type": "tconstruct:modifier_repair",
  "ingredient": {
    "item": "tconstruct:bacon"
  },
  "modifier": "tconstruct:tasty",
  "repair_amount": 25
}
```

**[47] tconstruct:crafting_modifier_repair**（ModifierRepairCraftingRecipe）
字段同 modifier_repair（modifier / ingredient / repair_amount 均必填）。示例：无示例。

**[48] tconstruct:modifier_material_repair（deprecated，提示改用 tconstruct:material_repair 模块）**（ModifierMaterialRepairRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| modifier | 是 | - | ModifierId |
| repair_material | 是 | - | MaterialId |
| stat_type | 是 | - | MaterialStatsId |

示例：无示例。

**[49] tconstruct:crafting_modifier_material_repair（deprecated，同上）**（ModifierMaterialRepairKitRecipe）
字段同 [48]。示例：无示例。

**[50] tconstruct:remove_modifier**（ModifierRemovalRecipe extends AbstractWorktableRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| name | 否 | "modifiers" | string |
| tools | 否 | SizedIngredient.of(tconstruct:modifiable) | SizedIngredient |
| inputs | 是 | - | SizedIngredient 数组（至少 1）|
| leftovers | 否 | [] | ItemStack 数组（REQUIRED_STACK_NBT）|
| modifier_predicate | 否 | false（PredicateRegistry 布尔常量）| ModifierPredicate |

示例：`./tools/modifiers/worktable/remove_modifier_sponge.json`
```json
{
  "type": "tconstruct:remove_modifier",
  "inputs": [
    {
      "item": "minecraft:wet_sponge"
    }
  ],
  "leftovers": [
    "minecraft:sponge"
  ],
  "modifier_predicate": {
    "type": "mantle:inverted",
    "inverted_type": "tconstruct:tag",
    "tag": "tconstruct:remove_blacklist"
  },
  "name": "modifiers",
  "tools": {
    "tag": "tconstruct:modifiable"
  }
}
```

**[51] tconstruct:extract_modifier**（ExtractModifierRecipe extends ModifierRemovalRecipe）
字段与 remove_modifier 完全相同。
示例：`./tools/modifiers/worktable/extract/ability.json`
```json
{
  "type": "tconstruct:extract_modifier",
  "inputs": [
    {
      "item": "tconstruct:ichor_slime_crystal"
    },
    {
      "item": "minecraft:wet_sponge"
    }
  ],
  "leftovers": [
    "minecraft:sponge"
  ],
  "modifier_predicate": {
    "type": "mantle:and",
    "predicates": [
      {
        "type": "mantle:inverted",
        "inverted_type": "tconstruct:tag",
        "tag": "tconstruct:extract_blacklist/tools"
      },
      {
        "type": "tconstruct:slot_type",
        "slot": "abilities"
```

**[52] tconstruct:modifier_sorting**（ModifierSortingRecipe extends AbstractWorktableRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| inputs | 是 | - | SizedIngredient，可单对象或数组（min=-1，至少 1 个）；多于 1 个会打 deprecated 警告 |

示例：`./tools/modifiers/worktable/modifier_sorting.json`
```json
{
  "type": "tconstruct:modifier_sorting",
  "inputs": {
    "item": "minecraft:compass"
  }
}
```

**[53] tconstruct:modifier_set_worktable**（ModifierSetWorktableRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| data_key | 是 | - | ResourceLocation |
| inputs | 是 | - | SizedIngredient 数组（至少 1）|
| tools | 否 | DEFAULT_TOOLS（tconstruct:modifiable）| Ingredient（写入 JSON）|
| modifier_predicate | 否 | false | ModifierPredicate |
| add_to_set | 是 | - | boolean |
| allow_traits | 否 | false | boolean |

示例：`./tools/modifiers/worktable/invisible_ink_adding.json`
```json
{
  "type": "tconstruct:modifier_set_worktable",
  "add_to_set": true,
  "allow_traits": false,
  "data_key": "tconstruct:invisible_modifiers",
  "inputs": [
    {
      "type": "mantle:fluid_container",
      "amount": 250,
      "display": {
        "item": "tconstruct:sky_slime_bottle"
      },
      "tag": "tconstruct:sky_slime"
    }
  ],
  "modifier_predicate": {
    "type": "mantle:inverted",
    "inverted_type": "tconstruct:tag",
    "tag": "tconstruct:invisible_ink_blacklist"
  },
  "tools": {
    "tag": "tconstruct:modifiable"
  }
}
```

**[54] tconstruct:enchantment_converting**（EnchantmentConvertingRecipe extends AbstractWorktableRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| name | 是 | - | string |
| inputs | 是 | - | SizedIngredient 数组（至少 1）|
| match_book | 是 | - | boolean |
| return_unenchanted | 是 | - | boolean |
| modifier_predicate | 否 | false | ModifierPredicate |

示例：`./tools/modifiers/worktable/enchantment_converting/ability_book.json`
```json
{
  "type": "tconstruct:enchantment_converting",
  "inputs": [
    {
      "item": "tconstruct:ichor_slime_crystal"
    },
    {
      "tag": "forge:gems/diamond"
    }
  ],
  "match_book": true,
  "modifier_predicate": {
    "type": "tconstruct:slot_type",
    "slot": "abilities"
  },
  "name": "abilities",
  "return_unenchanted": false
}
```

**[55] tconstruct:toggle_interaction**（ToggleInteractionWorktableRecipe extends AbstractWorktableRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| tools | 否 | DEFAULT_TOOLS | Ingredient |
| inputs | 是 | - | SizedIngredient 数组（至少 1）|

示例：`./tools/modifiers/worktable/toggle_interaction_modifier.json`
```json
{
  "type": "tconstruct:toggle_interaction",
  "inputs": [
    {
      "item": "minecraft:lever"
    }
  ],
  "tools": {
    "tag": "tconstruct:modifiable/interactable/dual"
  }
}
```

**[56] tconstruct:severing**（SeveringRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| entity | 是 | - | EntityIngredient |
| result | 是 | - | ItemStack（REQUIRED_STACK，支持 count）|
| per_level_chance | 否 | 0.05 | float，0–1 |
| looting_bonus | 否 | 0.01 | float，0–1 |

示例：`./tools/severing/blaze_head.json`
```json
{
  "type": "tconstruct:severing",
  "entity": {
    "type": "minecraft:blaze"
  },
  "looting_bonus": 0.01,
  "per_level_chance": 0.05,
  "result": "tconstruct:blaze_head"
}
```

**[57] tconstruct:ageable_severing**（AgeableSeveringRecipe extends SeveringRecipe）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| entity | 是 | - | EntityIngredient |
| adult_result | 是 | - | ItemStack（REQUIRED_STACK）|
| child_result | 否 | EMPTY | ItemStack（OPTIONAL_STACK）|
| per_level_chance | 否 | 0.05 | float 0–1 |
| looting_bonus | 否 | 0.01 | float 0–1 |

示例：`./tools/severing/chicken_feather.json`
```json
{
  "type": "tconstruct:ageable_severing",
  "adult_result": {
    "count": 2,
    "item": "minecraft:feather"
  },
  "entity": {
    "type": "minecraft:chicken"
  },
  "looting_bonus": 0.01,
  "per_level_chance": 0.05
}
```

**[58] tconstruct:player_beheading**（PlayerBeheadingRecipe extends SeveringRecipe；LOADER 无 entity / result）
| 字段 | 必填 | 默认 | 类型 |
|---|---|---|---|
| per_level_chance | 否 | 0.05 | float 0–1 |
| looting_bonus | 否 | 0.01 | float 0–1 |

示例：`./tools/severing/player_head.json`
```json
{
  "type": "tconstruct:player_beheading",
  "looting_bonus": 0.02,
  "per_level_chance": 0.1
}
```

**[59] tconstruct:snow_golem_beheading**（SnowGolemBeheadingRecipe；字段同 [58]）
示例：`./tools/severing/snow_golem_head.json`
```json
{
  "type": "tconstruct:snow_golem_beheading",
  "looting_bonus": 0.01,
  "per_level_chance": 0.05
}
```

**[60] tconstruct:mooshroom_demushrooming**（MooshroomDemushroomingRecipe；字段同 [58]）
示例：`./tools/severing/mooshroom_shroom.json`
```json
{
  "type": "tconstruct:mooshroom_demushrooming",
  "looting_bonus": 0.01,
  "per_level_chance": 0.05
}
```

**[61] tconstruct:sheep_shearing**（SheepShearingRecipe；字段同 [58]）
示例：`./tools/severing/sheep_wool.json`
```json
{
  "type": "tconstruct:sheep_shearing",
  "looting_bonus": 0.01,
  "per_level_chance": 0.05
}
```

#### 2.x 补充：本次 JSON 中没有示例的序列化器（12 个）
- tconstruct:basin_duplication、tconstruct:casting_basin_potion、tconstruct:casting_basin_tipping、tconstruct:casting_basin_tipped_clearing、tconstruct:retextured_casting_table、tconstruct:basin_casting_part_swapping、tconstruct:molding_basin、tconstruct:part_swapping_override、tconstruct:crafting_shaped_material、tconstruct:crafting_modifier_repair、tconstruct:modifier_material_repair、tconstruct:crafting_modifier_material_repair。
- 以上 12 个的字段仅来自源码，已在对应小节列出。
- 注：上面 [1]–[61] 中的编号为 61 节，合并了同类（如 basin/table 对）；其中 [1]–[61] 覆盖全部 76 个 ID（合并节内每个 ID 均已列出）。

---

### 3. 第 4 点：Mantle 流体与物品 JSON 格式

**FluidIngredient**（`D:/AI临时文件/tkja-src/mantle/slimeknights/mantle/recipe/ingredient/FluidIngredient.java`）
- 对象形式，三种键互斥：
  - `fluid` + `amount`：具体流体（FLUID_MATCH，`fluid` 为 Fluid 注册 ID）。
  - `tag` + `amount`：流体标签（TAG_MATCH）。
  - `name` + `amount`：旧式别名，与 `fluid` 同义（LegacyField）。
- `amount` 为 int ≥1，必填（IntLoadable.FROM_ONE）。
- 支持数组：`LOADABLE = loadableBuilder().array(COMPOUND)`，数组元素为 COMPOUND，源码 `COMPOUND` 使用 `list(2)`，即复合流体至少 2 个元素。
- 示例（TC 中实际用法）：`{"amount": 10, "tag": "forge:molten_gold"}`、`{"amount": 50, "fluid": "tconstruct:blazing_blood"}`。

**FluidOutput**（`D:/AI临时文件/tkja-src/mantle/slimeknights/mantle/recipe/helper/FluidOutput.java`）
- 只接受对象形式（不接受字符串简写）。
- `fluid` + `amount` (+ 可选 `nbt`)：具体流体（OfStack / REQUIRED_STACK_NBT）。
- `tag` + `amount` (+ 可选 `nbt`)：标签偏好输出（OfTagPreference）。
- Loadable 枚举：`OPTIONAL`（允许空）与 `REQUIRED`（必须非空）。
- 示例：`{"amount": 90, "tag": "forge:molten_aluminum"}`、`{"amount": 1000, "fluid": "tconstruct:molten_porcelain"}`。

**ItemOutput**（`D:/AI临时文件/tkja-src/mantle/slimeknights/mantle/recipe/helper/ItemOutput.java`）
- 字符串简写：`"ns:item"`（count 为 1）。
- 对象 `item` 形式：`{"item": "ns:item"}`；`count` 仅在 `*_STACK` 变体读取（REQUIRED_STACK / OPTIONAL_STACK），`*_ITEM` 变体不读 count。
- 对象 `tag` 形式：`{"tag": "ns:tag", "count": N}`，`count` 同样仅 *_STACK 读取，可选 `nbt`。
- 变体：OPTIONAL_ITEM、OPTIONAL_STACK、REQUIRED_ITEM、REQUIRED_STACK。
- 示例：`"result": "tconstruct:encyclopedia"`、`{"count": 4, "item": "tconstruct:adze_head_red_sand_cast"}`。

**SizedIngredient**（`D:/AI临时文件/tkja-src/mantle/slimeknights/mantle/recipe/ingredient/SizedIngredient.java`）：Ingredient 字段 + 可选 `amount_needed`（int ≥1，默认 1）。

**IngredientLoadable**（`D:/AI临时文件/tkja-src/mantle/slimeknights/mantle/data/loadable/common/IngredientLoadable.java`）：`ALLOW_EMPTY` / `DISALLOW_EMPTY`，实际解析为 `Ingredient.fromJson`。

---

### 4. 第 5 点：熔化/冷却时间与 FluidValues

**IMeltingRecipe**（`D:/AI临时文件/tkja-src/tconstruct/slimeknights/tconstruct/library/recipe/melting/IMeltingRecipe.java`，行 33–55）
```java
   static int getTemperature(Fluid fluid) {
      return fluid.getFluidType().getTemperature() - 300;
   }

   static int getTemperature(FluidStack fluid) {
      return fluid.getFluid().getFluidType().getTemperature(fluid) - 300;
   }

   static int getTemperature(FluidObject<?> fluid) {
      return fluid.getType().getTemperature() - 300;
   }

   static int calcTime(int temperature, float factor) {
      return (int)Math.round(Math.pow(temperature + 300, 0.585F) * factor);
   }

   static float calcTimeFactor(int amount) {
      return (float)Math.sqrt(amount / 90.0F);
   }

   static int calcTimeForAmount(int temperature, int amount) {
      return calcTime(temperature, calcTimeFactor(amount));
   }
```
计算规律：`time = round((temperature + 300)^0.585 * sqrt(amount / 90))`。注意 90 即 INGOT 的 mB 数。

**ICastingRecipe**（`D:/AI临时文件/tkja-src/tconstruct/slimeknights/tconstruct/library/recipe/casting/ICastingRecipe.java`，行 27–33）
```java
   static int calcCoolingTime(int temperature, int amount) {
      return IMeltingRecipe.calcTimeForAmount(temperature, amount);
   }

   static int calcCoolingTime(FluidStack fluid) {
      return calcCoolingTime(IMeltingRecipe.getTemperature(fluid), fluid.getAmount());
   }
```
冷却时间与熔化时间使用同一公式（复用 IMeltingRecipe.calcTimeForAmount）。

**FluidValues**（`D:/AI临时文件/tkja-src/tconstruct/slimeknights/tconstruct/library/recipe/FluidValues.java`）常量（单位 mB）：
| 常量 | 数值 |
|---|---|
| INGOT | 90 |
| NUGGET | 10 |
| METAL_BLOCK | 810 |
| GEM | 100 |
| GEM_SHARD | 25 |
| LARGE_GEM_BLOCK | 900 |
| SMALL_GEM_BLOCK | 400 |
| BRICK | 250 |
| BRICK_BLOCK | 1000 |
| GLASS_BLOCK | 1000 |
| GLASS_PANE | 250 |
| SLIMEBALL | 250 |
| SLIME_CONGEALED | 1000 |
| SLIME_BLOCK | 2250 |
| SLIME_DROP | 50 |
| ICHOR_BYPRODUCT | 200 |
| ICHOR_BLAZING_BLOOD | 10 |
| BOWL | 250 |
| BOTTLE | 250 |
| SIP | 50 |
| LANTERN_CAPACITY | 50 |

---

### 5. 结论与未核实项
- 序列化器总数：**76**（TinkerSmeltery 32 + TinkerTables 16 + TinkerModifiers 28）。有 JSON 示例 64 个，无示例 12 个。
- 主对话脚本复核（2026年10月30日）：molding、modifier、modifier_repair 的 LOADER 不含 `group`，已从表中删除；部件替换浇铸小节为补充内容。
- 两参 `defaultField(key, getter)`：`level` 默认 1–32767，`allowed_materials` 默认 ANY（已复核）。
- deprecated：modifier_material_repair、crafting_modifier_material_repair；cast_purpose 的 MAYBE_MATERIAL 已标注 deprecated。
