package com.hp.tinkers_kubejs_arsenal.kubejs.component;

import dev.latvian.mods.kubejs.typings.desc.PrimitiveDescJS;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;

public final class TinkerTypes {
    public static final String MATERIAL_NAME = "TinkerMaterial";
    public static final String MODIFIER_NAME = "TinkerModifier";
    public static final String MATERIAL_STAT_NAME = "TinkerMaterialStat";
    public static final String PATTERN_NAME = "TinkerPattern";
    public static final String SLOT_NAME = "TinkerSlot";
    public static final String TOOL_PART_NAME = "TinkerToolPart";
    public static final String MATERIAL_ITEM_NAME = "TinkerMaterialItem";
    public static final String MODIFIABLE_NAME = "TinkerModifiable";
    public static final String HARVEST_TIER_NAME = "TinkerHarvestTier";

    public static final TypeDescJS MATERIAL = special(MATERIAL_NAME);
    public static final TypeDescJS MODIFIER = special(MODIFIER_NAME);
    public static final TypeDescJS MATERIAL_STAT = special(MATERIAL_STAT_NAME);
    public static final TypeDescJS HARVEST_TIER = special(HARVEST_TIER_NAME);
    public static final TypeDescJS PATTERN = special(PATTERN_NAME);
    public static final TypeDescJS TOOL_PART = special(TOOL_PART_NAME);
    public static final TypeDescJS MATERIAL_ITEM = special(MATERIAL_ITEM_NAME);
    public static final TypeDescJS MODIFIABLE = special(MODIFIABLE_NAME);
    public static final TypeDescJS SLOT_COUNT = new PrimitiveDescJS("{[key in Special." + SLOT_NAME + "]?: number}");
    public static final TypeDescJS ITEM = special("Item");
    public static final TypeDescJS ITEM_TAG = special("ItemTag");
    public static final TypeDescJS ITEM_OR_TAG = ITEM.or(new PrimitiveDescJS("`#${Special.ItemTag}`"));
    public static final TypeDescJS FLUID = special("Fluid");
    public static final TypeDescJS FLUID_TAG = special("FluidTag");
    public static final TypeDescJS ENTITY = special("EntityType");
    public static final TypeDescJS ENTITY_TAG = special("EntityTypeTag");
    public static final TypeDescJS ENTITY_OR_TAG = ENTITY.or(new PrimitiveDescJS("`#${Special.EntityTypeTag}`"));
    public static final TypeDescJS ORE_RATE = new PrimitiveDescJS("\"none\" | \"metal\" | \"gem\" | \"default\"");
    public static final TypeDescJS VARIANT_FORMATTER = new PrimitiveDescJS("\"tconstruct:default\" | \"tconstruct:material\" | \"tconstruct:parameter\"");
    public static final TypeDescJS LEVEL_RANGE = TypeDescJS.any(TypeDescJS.NUMBER,
        TypeDescJS.object().add("min", TypeDescJS.NUMBER, true).add("max", TypeDescJS.NUMBER, true));

    private TinkerTypes() {
    }

    private static TypeDescJS special(String name) {
        return new PrimitiveDescJS("Special." + name);
    }
}
