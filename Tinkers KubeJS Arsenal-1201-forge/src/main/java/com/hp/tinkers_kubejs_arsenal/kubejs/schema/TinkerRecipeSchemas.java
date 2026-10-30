package com.hp.tinkers_kubejs_arsenal.kubejs.schema;

import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerComponents;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.TinkerRecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.ToolCastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;

public final class TinkerRecipeSchemas {
    public static final RecipeSchema CASTING_BASIN = casting();
    public static final RecipeSchema CASTING_TABLE = casting();
    public static final RecipeSchema BASIN_FILLING = filling();
    public static final RecipeSchema TABLE_FILLING = filling();
    public static final RecipeSchema BASIN_DUPLICATION = duplication();
    public static final RecipeSchema TABLE_DUPLICATION = duplication();
    public static final RecipeSchema CASTING_BASIN_POTION = potion();
    public static final RecipeSchema CASTING_TABLE_POTION = potion();
    public static final RecipeSchema CASTING_BASIN_TIPPING = tipping();
    public static final RecipeSchema CASTING_TABLE_TIPPING = tipping();
    public static final RecipeSchema CASTING_BASIN_TIPPED_CLEARING = tipping();
    public static final RecipeSchema CASTING_TABLE_TIPPED_CLEARING = tipping();
    public static final RecipeSchema RETEXTURED_CASTING_BASIN = casting();
    public static final RecipeSchema RETEXTURED_CASTING_TABLE = casting();
    public static final RecipeSchema BASIN_CASTING_MATERIAL = materialCasting();
    public static final RecipeSchema TABLE_CASTING_MATERIAL = materialCasting();
    public static final RecipeSchema BASIN_CASTING_COMPOSITE = compositeCasting();
    public static final RecipeSchema TABLE_CASTING_COMPOSITE = compositeCasting();
    public static final RecipeSchema BASIN_TOOL_CASTING = toolCasting();
    public static final RecipeSchema TABLE_TOOL_CASTING = toolCasting();
    public static final RecipeSchema BASIN_CASTING_PART_SWAPPING = partSwappingCasting();
    public static final RecipeSchema TABLE_CASTING_PART_SWAPPING = partSwappingCasting();
    public static final RecipeSchema MATERIAL_FLUID = materialFluid();
    public static final RecipeSchema MOLDING_BASIN = molding();
    public static final RecipeSchema MOLDING_TABLE = molding();
    public static final RecipeSchema MELTING = melting();
    public static final RecipeSchema ORE_MELTING = oreMelting();
    public static final RecipeSchema DAMAGABLE_MELTING = damagableMelting();
    public static final RecipeSchema MATERIAL_MELTING = materialMelting();
    public static final RecipeSchema MELTING_FUEL = meltingFuel();
    public static final RecipeSchema ENTITY_MELTING = entityMelting();
    public static final RecipeSchema ALLOY = alloy();

    private TinkerRecipeSchemas() {
    }

    private static RecipeSchema schema(RecipeKey<?>... keys) {
        return new RecipeSchema(TinkerRecipeJS.class, TinkerRecipeJS::new, keys);
    }

    private static RecipeKey<String> group() {
        return TinkerComponents.GROUP.key("group").optional("");
    }

    private static RecipeKey<Boolean> bool(String name) {
        return TinkerComponents.BOOLEAN.key(name).optional(false);
    }

    private static RecipeKey<Ingredient> emptyIngredient(String name) {
        return TinkerComponents.INGREDIENT_OPTIONAL.key(name).optional(Ingredient.EMPTY).allowEmpty();
    }

    private static RecipeKey<FluidOutput[]> fluidOutputs(String name) {
        return TinkerComponents.FLUID_OUTPUT_OPTIONAL.asArray().key(name).optional(new FluidOutput[0]);
    }

    private static RecipeSchema casting() {
        return schema(
            TinkerComponents.FLUID_INGREDIENT.key("fluid"),
            TinkerComponents.ITEM_OUTPUT_REQUIRED_ITEM.key("result"),
            TinkerComponents.POSITIVE_INT.key("cooling_time").defaultOptional(),
            emptyIngredient("cast"),
            bool("cast_consumed"),
            bool("switch_slots"),
            group()
        );
    }

    private static RecipeSchema filling() {
        return schema(
            TinkerComponents.POSITIVE_INT.key("fluid_amount"),
            TinkerComponents.ITEM.key("container"),
            group()
        );
    }

    private static RecipeSchema duplication() {
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("cast"),
            TinkerComponents.FLUID_INGREDIENT.key("fluid"),
            TinkerComponents.POSITIVE_INT.key("cooling_time").defaultOptional(),
            group()
        );
    }

    private static RecipeSchema potion() {
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("bottle"),
            TinkerComponents.FLUID_INGREDIENT.key("fluid"),
            TinkerComponents.ITEM_RESULT.key("result"),
            TinkerComponents.POSITIVE_INT.key("cooling_time").optional(5),
            group()
        );
    }

    private static RecipeSchema tipping() {
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("tools"),
            TinkerComponents.FLUID_INGREDIENT.key("fluid"),
            TinkerComponents.MODIFIER.key("modifier"),
            TinkerComponents.POSITIVE_INT.key("cooling_time").optional(5),
            group()
        );
    }

    private static RecipeSchema materialCasting() {
        return schema(
            TinkerComponents.POSITIVE_INT.key("item_cost"),
            TinkerComponents.MATERIAL_ITEM.key("result"),
            emptyIngredient("cast"),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            bool("cast_consumed"),
            bool("switch_slots"),
            group()
        );
    }

    private static RecipeSchema compositeCasting() {
        return schema(
            TinkerComponents.POSITIVE_INT.key("item_cost"),
            TinkerComponents.MATERIAL_ITEM.key("result"),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            TinkerComponents.MATERIAL_STATS.key("casting_stat_conflict").defaultOptional(),
            group()
        );
    }

    private static RecipeSchema toolCasting() {
        RecipeComponent<MaterialVariantId[]> materials = TinkerComponents.MATERIAL_VARIANT.asArray();
        return schema(
            TinkerComponents.POSITIVE_INT.key("item_cost"),
            TinkerComponents.MODIFIABLE_ITEM.key("result"),
            emptyIngredient("cast"),
            TinkerComponents.CAST_PURPOSE.key("cast_purpose").optional(ToolCastingRecipe.CastPurpose.MAYBE_MATERIAL),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            materials.key("extra_materials").optional(new MaterialVariantId[0]),
            group()
        );
    }

    private static RecipeSchema partSwappingCasting() {
        return schema(
            TinkerComponents.INGREDIENT_OPTIONAL.key("tools").allowEmpty(),
            TinkerComponents.POSITIVE_INT.key("item_cost"),
            TinkerComponents.NON_NEGATIVE_INT.key("index"),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            group()
        );
    }

    private static RecipeSchema materialFluid() {
        return schema(
            TinkerComponents.FLUID_INGREDIENT.key("fluid"),
            TinkerComponents.MATERIAL_VARIANT.key("input").defaultOptional(),
            TinkerComponents.MATERIAL_VARIANT.key("output").defaultOptional(),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            bool("hide_in_book")
        );
    }

    private static RecipeSchema molding() {
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("material"),
            TinkerComponents.ITEM_OUTPUT_REQUIRED_STACK.key("result"),
            emptyIngredient("pattern"),
            bool("pattern_consumed")
        );
    }

    private static RecipeSchema melting() {
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result"),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            TinkerComponents.POSITIVE_INT.key("time").defaultOptional(),
            fluidOutputs("byproducts"),
            group()
        );
    }

    private static RecipeSchema oreMelting() {
        RecipeComponent<TinkerComponents.OreByproduct[]> byproducts = TinkerComponents.ORE_BYPRODUCT.asArray();
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result"),
            TinkerComponents.ORE_RATE.key("rate"),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            TinkerComponents.POSITIVE_INT.key("time").defaultOptional(),
            byproducts.key("byproducts").optional(new TinkerComponents.OreByproduct[0]),
            group()
        );
    }

    private static RecipeSchema damagableMelting() {
        RecipeComponent<TinkerComponents.FluidOutputUnit[]> byproducts = TinkerComponents.FLUID_OUTPUT_UNIT.asArray();
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            TinkerComponents.FLUID_OUTPUT_UNIT.key("result"),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            TinkerComponents.POSITIVE_INT.key("time").defaultOptional(),
            byproducts.key("byproducts").optional(new TinkerComponents.FluidOutputUnit[0]),
            group()
        );
    }

    private static RecipeSchema materialMelting() {
        return schema(
            TinkerComponents.MATERIAL_VARIANT.key("input"),
            TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result"),
            TinkerComponents.POSITIVE_INT.key("temperature").defaultOptional(),
            fluidOutputs("byproducts")
        );
    }

    private static RecipeSchema meltingFuel() {
        return schema(
            TinkerComponents.POSITIVE_INT.key("temperature"),
            TinkerComponents.POSITIVE_INT.key("rate"),
            TinkerComponents.FLUID_INGREDIENT.key("fluid").optional(FluidIngredient.EMPTY).allowEmpty(),
            TinkerComponents.NON_NEGATIVE_INT.key("duration").optional(0)
        );
    }

    private static RecipeSchema entityMelting() {
        return schema(
            TinkerComponents.ENTITY_INGREDIENT.key("entity"),
            TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result"),
            TinkerComponents.POSITIVE_INT.key("damage").optional(2)
        );
    }

    private static RecipeSchema alloy() {
        RecipeComponent<AlloyRecipe.AlloyIngredient[]> inputs = TinkerComponents.ALLOY_INGREDIENT.asArray();
        return schema(
            inputs.key("inputs"),
            TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result"),
            TinkerComponents.POSITIVE_INT.key("temperature").defaultOptional()
        );
    }
}
