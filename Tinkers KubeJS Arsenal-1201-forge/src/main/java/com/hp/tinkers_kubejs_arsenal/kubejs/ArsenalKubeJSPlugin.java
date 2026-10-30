package com.hp.tinkers_kubejs_arsenal.kubejs;

import com.hp.tinkers_kubejs_arsenal.kubejs.schema.TinkerRecipeSchemas;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class ArsenalKubeJSPlugin extends KubeJSPlugin {
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
            .register("alloy", TinkerRecipeSchemas.ALLOY);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("TinkerFluids", TinkerFluids.class);
        event.add("TinkerRecipes", TinkerRecipes.class);
    }
}
