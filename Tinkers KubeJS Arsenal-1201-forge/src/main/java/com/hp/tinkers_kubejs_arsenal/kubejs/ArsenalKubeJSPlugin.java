package com.hp.tinkers_kubejs_arsenal.kubejs;

import com.hp.tinkers_kubejs_arsenal.kubejs.schema.TinkerRecipeSchemas;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import java.util.LinkedHashMap;
import java.util.Map;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialAssetEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.ArsenalMaterialEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ArsenalModifierEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ArsenalToolEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalToolBuilder;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalPartBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class ArsenalKubeJSPlugin extends KubeJSPlugin {
    private MaterialAssetEventJS assets;

    @Override
    public void init() {
        RegistryInfo.ITEM.addType(ArsenalToolBuilder.TOOL_TYPE, ArsenalToolBuilder.class, id -> new ArsenalToolBuilder(id, false));
        RegistryInfo.ITEM.addType(ArsenalToolBuilder.SWORD_TYPE, ArsenalToolBuilder.class, id -> new ArsenalToolBuilder(id, true));
        RegistryInfo.ITEM.addType(ArsenalPartBuilder.TYPE, ArsenalPartBuilder.class, ArsenalPartBuilder::new);
    }

    @Override
    public void registerEvents() {
        ArsenalMaterialEvents.register();
        ArsenalModifierEvents.register();
        ArsenalToolEvents.register();
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace("tconstruct")
            .register("casting_basin", TinkerRecipeSchemas.CASTING_BASIN)
            .register("casting_table", TinkerRecipeSchemas.CASTING_TABLE)
            .register("basin_filling", TinkerRecipeSchemas.BASIN_FILLING)
            .register("table_filling", TinkerRecipeSchemas.TABLE_FILLING)
            .register("basin_duplication", TinkerRecipeSchemas.BASIN_DUPLICATION)
            .register("table_duplication", TinkerRecipeSchemas.TABLE_DUPLICATION)
            .register("casting_basin_potion", TinkerRecipeSchemas.CASTING_BASIN_POTION)
            .register("casting_table_potion", TinkerRecipeSchemas.CASTING_TABLE_POTION)
            .register("casting_basin_tipping", TinkerRecipeSchemas.CASTING_BASIN_TIPPING)
            .register("casting_table_tipping", TinkerRecipeSchemas.CASTING_TABLE_TIPPING)
            .register("casting_basin_tipped_clearing", TinkerRecipeSchemas.CASTING_BASIN_TIPPED_CLEARING)
            .register("casting_table_tipped_clearing", TinkerRecipeSchemas.CASTING_TABLE_TIPPED_CLEARING)
            .register("retextured_casting_basin", TinkerRecipeSchemas.RETEXTURED_CASTING_BASIN)
            .register("retextured_casting_table", TinkerRecipeSchemas.RETEXTURED_CASTING_TABLE)
            .register("basin_casting_material", TinkerRecipeSchemas.BASIN_CASTING_MATERIAL)
            .register("table_casting_material", TinkerRecipeSchemas.TABLE_CASTING_MATERIAL)
            .register("basin_casting_composite", TinkerRecipeSchemas.BASIN_CASTING_COMPOSITE)
            .register("table_casting_composite", TinkerRecipeSchemas.TABLE_CASTING_COMPOSITE)
            .register("basin_tool_casting", TinkerRecipeSchemas.BASIN_TOOL_CASTING)
            .register("table_tool_casting", TinkerRecipeSchemas.TABLE_TOOL_CASTING)
            .register("basin_casting_part_swapping", TinkerRecipeSchemas.BASIN_CASTING_PART_SWAPPING)
            .register("table_casting_part_swapping", TinkerRecipeSchemas.TABLE_CASTING_PART_SWAPPING)
            .register("material_fluid", TinkerRecipeSchemas.MATERIAL_FLUID)
            .register("molding_basin", TinkerRecipeSchemas.MOLDING_BASIN)
            .register("molding_table", TinkerRecipeSchemas.MOLDING_TABLE)
            .register("melting", TinkerRecipeSchemas.MELTING)
            .register("ore_melting", TinkerRecipeSchemas.ORE_MELTING)
            .register("damagable_melting", TinkerRecipeSchemas.DAMAGABLE_MELTING)
            .register("material_melting", TinkerRecipeSchemas.MATERIAL_MELTING)
            .register("melting_fuel", TinkerRecipeSchemas.MELTING_FUEL)
            .register("entity_melting", TinkerRecipeSchemas.ENTITY_MELTING)
            .register("alloy", TinkerRecipeSchemas.ALLOY)
            .register("material", TinkerRecipeSchemas.MATERIAL)
            .register("part_builder", TinkerRecipeSchemas.PART_BUILDER)
            .register("item_part_builder", TinkerRecipeSchemas.ITEM_PART_BUILDER)
            .register("tool_building", TinkerRecipeSchemas.TOOL_BUILDING)
            .register("part_builder_recycling", TinkerRecipeSchemas.PART_BUILDER_RECYCLING)
            .register("part_builder_tool_recycling", TinkerRecipeSchemas.PART_BUILDER_TOOL_RECYCLING)
            .register("modifier", TinkerRecipeSchemas.MODIFIER)
            .register("incremental_modifier", TinkerRecipeSchemas.INCREMENTAL_MODIFIER)
            .register("swappable_modifier", TinkerRecipeSchemas.SWAPPABLE_MODIFIER)
            .register("multilevel_modifier", TinkerRecipeSchemas.MULTILEVEL_MODIFIER)
            .register("multilevel_incremental_modifier", TinkerRecipeSchemas.MULTILEVEL_INCREMENTAL_MODIFIER)
            .register("overslime_modifier", TinkerRecipeSchemas.OVERSLIME_MODIFIER)
            .register("crafting_overslime_modifier", TinkerRecipeSchemas.CRAFTING_OVERSLIME_MODIFIER)
            .register("modifier_salvage", TinkerRecipeSchemas.MODIFIER_SALVAGE)
            .register("modifier_repair", TinkerRecipeSchemas.MODIFIER_REPAIR)
            .register("crafting_modifier_repair", TinkerRecipeSchemas.CRAFTING_MODIFIER_REPAIR)
            .register("remove_modifier", TinkerRecipeSchemas.REMOVE_MODIFIER)
            .register("extract_modifier", TinkerRecipeSchemas.EXTRACT_MODIFIER)
            .register("modifier_sorting", TinkerRecipeSchemas.MODIFIER_SORTING)
            .register("modifier_set_worktable", TinkerRecipeSchemas.MODIFIER_SET_WORKTABLE)
            .register("enchantment_converting", TinkerRecipeSchemas.ENCHANTMENT_CONVERTING)
            .register("toggle_interaction", TinkerRecipeSchemas.TOGGLE_INTERACTION)
            .register("tinker_station_damaging", TinkerRecipeSchemas.TINKER_STATION_DAMAGING)
            .register("tinker_station_part_swapping", TinkerRecipeSchemas.TINKER_STATION_PART_SWAPPING)
            .register("tool_material_swapping", TinkerRecipeSchemas.TOOL_MATERIAL_SWAPPING)
            .register("fixed_material_swapping", TinkerRecipeSchemas.FIXED_MATERIAL_SWAPPING)
            .register("part_swapping_override", TinkerRecipeSchemas.PART_SWAPPING_OVERRIDE)
            .register("tipped_tool_transform", TinkerRecipeSchemas.TIPPED_TOOL_TRANSFORM)
            .register("severing", TinkerRecipeSchemas.SEVERING)
            .register("ageable_severing", TinkerRecipeSchemas.AGEABLE_SEVERING)
            .register("player_beheading", TinkerRecipeSchemas.PLAYER_BEHEADING)
            .register("snow_golem_beheading", TinkerRecipeSchemas.SNOW_GOLEM_BEHEADING)
            .register("mooshroom_demushrooming", TinkerRecipeSchemas.MOOSHROOM_DEMUSHROOMING)
            .register("sheep_shearing", TinkerRecipeSchemas.SHEEP_SHEARING);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("ArsenalFluids", ArsenalFluids.class);
        event.add("ArsenalRecipes", ArsenalRecipes.class);
        event.add("ArsenalMaterialEvents", ArsenalMaterialEvents.wrapper(event.getType()));
        event.add("ArsenalModifierEvents", ArsenalModifierEvents.wrapper(event.getType()));
        event.add("ArsenalToolEvents", ArsenalToolEvents.wrapper(event.getType()));
        event.add("ArsenalTool", ArsenalTool.class);
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        this.assets = new MaterialAssetEventJS();
        ArsenalMaterialEvents.ASSETS.post(this.assets);
        Map<String, JsonObject> colors = new LinkedHashMap<>();
        for (MaterialAssetEventJS.MaterialAsset asset : this.assets.getMaterials().values()) {
            ResourceLocation id = asset.id();
            generator.json(new ResourceLocation(id.getNamespace(), "tinkering/materials/" + asset.outputPath()), asset.render());
            if (asset.textColor() != null) {
                colors.computeIfAbsent(id.getNamespace(), namespace -> new JsonObject()).addProperty(asset.translationKey(), asset.textColor());
            }
        }
        colors.forEach((namespace, json) -> generator.json(new ResourceLocation(namespace, "mantle/colors"), json));
    }

    @Override
    public void generateLang(LangEventJS event) {
        if (this.assets == null) {
            return;
        }
        for (MaterialAssetEventJS.MaterialAsset asset : this.assets.getMaterials().values()) {
            ResourceLocation id = asset.id();
            String base = asset.translationKey();
            asset.names().forEach((lang, text) -> event.map.put(new LangEventJS.Key(id.getNamespace(), lang, base), text));
            asset.flavors().forEach((lang, text) -> event.map.put(new LangEventJS.Key(id.getNamespace(), lang, base + ".flavor"), text));
            asset.encyclopedia().forEach((lang, text) -> event.map.put(new LangEventJS.Key(id.getNamespace(), lang, base + ".encyclopedia"), text));
        }
        this.assets = null;
    }
}


