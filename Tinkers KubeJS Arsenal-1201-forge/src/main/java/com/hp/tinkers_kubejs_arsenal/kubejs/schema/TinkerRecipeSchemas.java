package com.hp.tinkers_kubejs_arsenal.kubejs.schema;

import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerComponents;
import com.google.gson.JsonElement;
import slimeknights.tconstruct.common.TinkerTags;
import com.google.gson.JsonNull;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.AlloyRecipeJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.CastingRecipeJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.MeltingRecipeJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.ModifierRecipeJS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.TinkerRecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.ToolCastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;

public final class TinkerRecipeSchemas {
    private static final Map<RecipeSchema, List<String>> CHAINED = new IdentityHashMap<>();
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
    public static final RecipeSchema MATERIAL = material();
    public static final RecipeSchema PART_BUILDER = partBuilder();
    public static final RecipeSchema ITEM_PART_BUILDER = itemPartBuilder();
    public static final RecipeSchema TOOL_BUILDING = toolBuilding();
    public static final RecipeSchema PART_BUILDER_RECYCLING = partBuilderRecycling();
    public static final RecipeSchema PART_BUILDER_TOOL_RECYCLING = partBuilderToolRecycling();
    public static final RecipeSchema MODIFIER = modifier();
    public static final RecipeSchema INCREMENTAL_MODIFIER = incrementalModifier();
    public static final RecipeSchema SWAPPABLE_MODIFIER = swappableModifier();
    public static final RecipeSchema MULTILEVEL_MODIFIER = multilevelModifier();
    public static final RecipeSchema MULTILEVEL_INCREMENTAL_MODIFIER = multilevelIncrementalModifier();
    public static final RecipeSchema OVERSLIME_MODIFIER = overslimeModifier();
    public static final RecipeSchema CRAFTING_OVERSLIME_MODIFIER = overslimeModifier();
    public static final RecipeSchema MODIFIER_SALVAGE = modifierSalvage();
    public static final RecipeSchema MODIFIER_REPAIR = modifierRepair();
    public static final RecipeSchema CRAFTING_MODIFIER_REPAIR = modifierRepair();
    public static final RecipeSchema REMOVE_MODIFIER = modifierRemoval();
    public static final RecipeSchema EXTRACT_MODIFIER = modifierRemoval();
    public static final RecipeSchema MODIFIER_SORTING = schema(TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs"));
    public static final RecipeSchema MODIFIER_SET_WORKTABLE = modifierSetWorktable();
    public static final RecipeSchema ENCHANTMENT_CONVERTING = enchantmentConverting();
    public static final RecipeSchema TOGGLE_INTERACTION = toggleInteraction();
    public static final RecipeSchema TINKER_STATION_DAMAGING = schema(TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"), TinkerComponents.POSITIVE_INT.key("damage_amount"));
    public static final RecipeSchema TINKER_STATION_PART_SWAPPING = materialSwapping();
    public static final RecipeSchema TOOL_MATERIAL_SWAPPING = materialSwapping();
    public static final RecipeSchema FIXED_MATERIAL_SWAPPING = fixedMaterialSwapping();
    public static final RecipeSchema PART_SWAPPING_OVERRIDE = partSwappingOverride();
    public static final RecipeSchema TIPPED_TOOL_TRANSFORM = tippedToolTransform();
    public static final RecipeSchema SEVERING = severing();
    public static final RecipeSchema AGEABLE_SEVERING = ageableSevering();
    public static final RecipeSchema PLAYER_BEHEADING = fixedSevering();
    public static final RecipeSchema SNOW_GOLEM_BEHEADING = fixedSevering();
    public static final RecipeSchema MOOSHROOM_DEMUSHROOMING = fixedSevering();
    public static final RecipeSchema SHEEP_SHEARING = fixedSevering();

    private TinkerRecipeSchemas() {
    }

    private static RecipeSchema schema(RecipeKey<?>... keys) {
        return new RecipeSchema(TinkerRecipeJS.class, TinkerRecipeJS::new, keys);
    }

    private static RecipeSchema chain(Class<? extends TinkerRecipeJS> type, Supplier<? extends TinkerRecipeJS> factory, RecipeKey<?> main,
                                      RecipeKey<?>[] later, RecipeKey<?>... optional) {
        List<RecipeKey<?>> keys = new ArrayList<>();
        if (main != null) {
            keys.add(main);
        }
        for (RecipeKey<?> key : later) {
            keys.add(key.defaultOptional());
        }
        keys.addAll(Arrays.asList(optional));
        RecipeSchema schema = new RecipeSchema(type, factory, keys.toArray(new RecipeKey<?>[0]));
        if (main == null) {
            schema.constructor();
        } else {
            schema.constructor(main);
        }
        CHAINED.put(schema, Arrays.stream(later).map(key -> key.name).toList());
        return schema;
    }

    private static RecipeKey<?>[] later(RecipeKey<?>... keys) {
        return keys;
    }

    public static List<String> chainedKeys(RecipeSchema schema) {
        return CHAINED.getOrDefault(schema, List.of());
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
        return chain(CastingRecipeJS.class, CastingRecipeJS::new,
            TinkerComponents.ITEM_OUTPUT_REQUIRED_ITEM.key("result"),
            later(TinkerComponents.FLUID_INGREDIENT.key("fluid").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("cooling_time").defaultOptional(),
            emptyIngredient("cast"),
            bool("cast_consumed"),
            bool("switch_slots"),
            group()
        );
    }

    private static RecipeSchema filling() {
        return chain(TinkerRecipeJS.class, TinkerRecipeJS::new,
            TinkerComponents.ITEM.key("container"),
            later(TinkerComponents.POSITIVE_INT.key("fluid_amount")),
            group()
        );
    }

    private static RecipeSchema duplication() {
        return chain(CastingRecipeJS.class, CastingRecipeJS::new,
            TinkerComponents.INGREDIENT_REQUIRED.key("cast"),
            later(TinkerComponents.FLUID_INGREDIENT.key("fluid").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("cooling_time").defaultOptional(),
            group()
        );
    }

    private static RecipeSchema potion() {
        return chain(CastingRecipeJS.class, CastingRecipeJS::new,
            TinkerComponents.ITEM_RESULT.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("bottle"), TinkerComponents.FLUID_INGREDIENT.key("fluid").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("cooling_time").optional(5),
            group()
        );
    }

    private static RecipeSchema tipping() {
        return chain(CastingRecipeJS.class, CastingRecipeJS::new,
            TinkerComponents.MODIFIER.key("modifier"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.FLUID_INGREDIENT.key("fluid").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("cooling_time").optional(5),
            group()
        );
    }

    private static RecipeSchema materialCasting() {
        return chain(TinkerRecipeJS.class, TinkerRecipeJS::new,
            TinkerComponents.MATERIAL_ITEM.key("result"),
            later(TinkerComponents.POSITIVE_INT.key("item_cost")),
            emptyIngredient("cast"),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            bool("cast_consumed"),
            bool("switch_slots"),
            group()
        );
    }

    private static RecipeSchema compositeCasting() {
        return chain(TinkerRecipeJS.class, TinkerRecipeJS::new,
            TinkerComponents.MATERIAL_ITEM.key("result"),
            later(TinkerComponents.POSITIVE_INT.key("item_cost")),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            TinkerComponents.MATERIAL_STATS.key("casting_stat_conflict").defaultOptional(),
            group()
        );
    }

    private static RecipeSchema toolCasting() {
        RecipeComponent<MaterialVariantId[]> materials = TinkerComponents.MATERIAL_VARIANT.asArray();
        return chain(TinkerRecipeJS.class, TinkerRecipeJS::new,
            TinkerComponents.MODIFIABLE_ITEM.key("result"),
            later(TinkerComponents.POSITIVE_INT.key("item_cost")),
            emptyIngredient("cast"),
            TinkerComponents.CAST_PURPOSE.key("cast_purpose").optional(ToolCastingRecipe.CastPurpose.MAYBE_MATERIAL),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            materials.key("extra_materials").optional(new MaterialVariantId[0]),
            group()
        );
    }

    private static RecipeSchema partSwappingCasting() {
        return chain(TinkerRecipeJS.class, TinkerRecipeJS::new,
            TinkerComponents.INGREDIENT_OPTIONAL.key("tools").allowEmpty(),
            later(TinkerComponents.POSITIVE_INT.key("item_cost"), TinkerComponents.NON_NEGATIVE_INT.key("index")),
            TinkerComponents.MATERIAL_PREDICATE.key("allowed_materials").optional(MaterialPredicate.ANY),
            group()
        );
    }

    private static RecipeSchema materialFluid() {
        return chain(CastingRecipeJS.class, CastingRecipeJS::new,
            TinkerComponents.MATERIAL_VARIANT.key("output"),
            later(TinkerComponents.FLUID_INGREDIENT.key("fluid").noBuilders()),
            TinkerComponents.MATERIAL_VARIANT.key("input").defaultOptional(),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            bool("hide_in_book")
        );
    }

    private static RecipeSchema molding() {
        return chain(TinkerRecipeJS.class, TinkerRecipeJS::new,
            TinkerComponents.ITEM_OUTPUT_REQUIRED_STACK.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("material")),
            emptyIngredient("pattern"),
            bool("pattern_consumed")
        );
    }

    private static RecipeSchema melting() {
        return chain(MeltingRecipeJS.class, MeltingRecipeJS::new,
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            later(TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result").noBuilders()),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            TinkerComponents.POSITIVE_INT.key("time").defaultOptional(),
            fluidOutputs("byproducts").noBuilders(),
            group()
        );
    }

    private static RecipeSchema oreMelting() {
        RecipeComponent<TinkerComponents.OreByproduct[]> byproducts = TinkerComponents.ORE_BYPRODUCT.asArray();
        return chain(MeltingRecipeJS.class, MeltingRecipeJS::new,
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            later(TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result").noBuilders(), TinkerComponents.ORE_RATE.key("rate").noBuilders()),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            TinkerComponents.POSITIVE_INT.key("time").defaultOptional(),
            byproducts.key("byproducts").optional(new TinkerComponents.OreByproduct[0]).noBuilders(),
            group()
        );
    }

    private static RecipeSchema damagableMelting() {
        RecipeComponent<TinkerComponents.FluidOutputUnit[]> byproducts = TinkerComponents.FLUID_OUTPUT_UNIT.asArray();
        return chain(MeltingRecipeJS.class, MeltingRecipeJS::new,
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            later(TinkerComponents.FLUID_OUTPUT_UNIT.key("result").noBuilders()),
            TinkerComponents.NON_NEGATIVE_INT.key("temperature").defaultOptional(),
            TinkerComponents.POSITIVE_INT.key("time").defaultOptional(),
            byproducts.key("byproducts").optional(new TinkerComponents.FluidOutputUnit[0]).noBuilders(),
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

    private static RecipeSchema material() {
        return schema(
            TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"),
            TinkerComponents.MATERIAL_VARIANT.key("material"),
            TinkerComponents.POSITIVE_INT.key("value").optional(1),
            TinkerComponents.POSITIVE_INT.key("needed").optional(1),
            TinkerComponents.ITEM_OUTPUT_OPTIONAL_STACK.key("leftover").optional(ItemOutput.EMPTY),
            group()
        );
    }

    private static RecipeSchema partBuilder() {
        return schema(TinkerComponents.PATTERN.key("pattern"), TinkerComponents.POSITIVE_INT.key("cost"), TinkerComponents.MATERIAL_ITEM_RESULT.key("result"),
            TinkerComponents.INGREDIENT_REQUIRED.key("pattern_item").optional(defaultPatternItem()), bool("allow_uncraftable"), group());
    }

    private static RecipeSchema itemPartBuilder() {
        return schema(TinkerComponents.PATTERN.key("pattern"), TinkerComponents.ITEM_OUTPUT_REQUIRED_STACK.key("result"),
            TinkerComponents.MATERIAL_VARIANT.key("material").optional(MaterialId.UNKNOWN), TinkerComponents.INGREDIENT_REQUIRED.key("pattern_item").optional(defaultPatternItem()),
            TinkerComponents.NON_NEGATIVE_INT.key("cost").optional(0));
    }

    private static RecipeSchema toolBuilding() {
        RecipeComponent<Ingredient[]> extra = TinkerComponents.INGREDIENT_REQUIRED.asArray();
        RecipeComponent<IToolPart[]> parts = TinkerComponents.TOOL_PART.asArray();
        RecipeComponent<MaterialVariantId[]> materials = TinkerComponents.MATERIAL_VARIANT.asArray();
        return schema(TinkerComponents.MODIFIABLE_ITEM.key("result"), TinkerComponents.POSITIVE_INT.key("result_count").optional(1),
            TinkerComponents.RESOURCE_LOCATION.key("slot_layout").defaultOptional(), extra.key("extra_requirements").optional(new Ingredient[0]),
            parts.key("parts_override").defaultOptional(), materials.key("extra_materials").optional(new MaterialVariantId[0]), group());
    }

    private static RecipeSchema partBuilderRecycling() {
        RecipeComponent<?> results = TinkerComponents.ITEM_OUTPUT_REQUIRED_STACK.asMap(TinkerComponents.PATTERN);
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("tool"), TinkerComponents.INGREDIENT_REQUIRED.key("pattern"), results.key("results"));
    }

    private static RecipeSchema partBuilderToolRecycling() {
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("pattern"), TinkerComponents.SIZED_INGREDIENT.key("tools").optional(defaultMultipartTool()),
            TinkerComponents.MATERIAL_ITEM.asArray().key("parts").optional(new IMaterialItem[0]));
    }

    private static Ingredient defaultPatternItem() {
        return Ingredient.of(TinkerTags.Items.DEFAULT_PATTERNS);
    }

    private static slimeknights.mantle.recipe.ingredient.SizedIngredient defaultMultipartTool() {
        return slimeknights.mantle.recipe.ingredient.SizedIngredient.of(Ingredient.of(TinkerTags.Items.MULTIPART_TOOL));
    }

    private static RecipeSchema modifier() {
        return chain(ModifierRecipeJS.class, ModifierRecipeJS::new,
            TinkerComponents.MODIFIER.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("max_tool_size").optional(16),
            TinkerComponents.LEVEL_RANGE.key("level").optional(new slimeknights.tconstruct.library.json.IntRange(1, 32767)).noBuilders(),
            TinkerComponents.SLOT_COUNT.key("slots").defaultOptional().noBuilders(),
            bool("allow_crystal").optional(true),
            bool("check_trait_level")
        );
    }

    private static RecipeSchema incrementalModifier() {
        return chain(ModifierRecipeJS.class, ModifierRecipeJS::new,
            TinkerComponents.MODIFIER.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.INGREDIENT_REQUIRED.key("input").noBuilders(), TinkerComponents.POSITIVE_INT.key("needed_per_level")),
            TinkerComponents.POSITIVE_INT.key("amount_per_item").optional(1),
            TinkerComponents.POSITIVE_INT.key("max_tool_size").optional(16),
            TinkerComponents.LEVEL_RANGE.key("level").optional(new slimeknights.tconstruct.library.json.IntRange(1, 32767)).noBuilders(),
            TinkerComponents.SLOT_COUNT.key("slots").defaultOptional().noBuilders(),
            TinkerComponents.ITEM_OUTPUT_OPTIONAL_STACK.key("leftover").optional(ItemOutput.EMPTY),
            bool("allow_crystal").optional(true),
            bool("check_trait_level")
        );
    }

    private static RecipeSchema swappableModifier() {
        return chain(ModifierRecipeJS.class, ModifierRecipeJS::new,
            TinkerComponents.SWAPPABLE_RESULT.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("max_tool_size").optional(16),
            TinkerComponents.VARIANT_FORMATTER.key("variant_formatter").defaultOptional(),
            TinkerComponents.SLOT_COUNT.key("slots").defaultOptional().noBuilders(),
            bool("allow_crystal").optional(true)
        );
    }

    private static RecipeSchema multilevelModifier() {
        return chain(ModifierRecipeJS.class, ModifierRecipeJS::new,
            TinkerComponents.MODIFIER.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.LEVEL_ENTRY.asArray().key("levels").noBuilders()),
            TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs").optional(new slimeknights.mantle.recipe.ingredient.SizedIngredient[0]).noBuilders(),
            TinkerComponents.POSITIVE_INT.key("max_tool_size").optional(16),
            bool("allow_crystal").optional(true),
            bool("check_trait_level")
        );
    }

    private static RecipeSchema multilevelIncrementalModifier() {
        return chain(ModifierRecipeJS.class, ModifierRecipeJS::new,
            TinkerComponents.MODIFIER.key("result"),
            later(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.INGREDIENT_REQUIRED.key("input").noBuilders(), TinkerComponents.POSITIVE_INT.key("needed_per_level"),
                TinkerComponents.LEVEL_ENTRY.asArray().key("levels").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("amount_per_item").optional(1),
            TinkerComponents.POSITIVE_INT.key("max_tool_size").optional(16),
            TinkerComponents.ITEM_OUTPUT_OPTIONAL_STACK.key("leftover").optional(ItemOutput.EMPTY),
            bool("allow_crystal").optional(true),
            bool("check_trait_level")
        );
    }

    private static RecipeSchema overslimeModifier() {
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"), TinkerComponents.POSITIVE_INT.key("restore_amount"), TinkerComponents.INGREDIENT_REQUIRED.key("tools").optional(defaultDurability()));
    }

    private static RecipeSchema modifierSalvage() {
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.MODIFIER.key("modifier"), TinkerComponents.SLOT_COUNT.key("slots"),
            TinkerComponents.POSITIVE_INT.key("max_tool_size").optional(16), TinkerComponents.LEVEL_RANGE.key("level").optional(new slimeknights.tconstruct.library.json.IntRange(1, 32767)));
    }

    private static RecipeSchema modifierRepair() {
        return schema(TinkerComponents.MODIFIER.key("modifier"), TinkerComponents.INGREDIENT_REQUIRED.key("ingredient"), TinkerComponents.POSITIVE_INT.key("repair_amount"));
    }

    private static RecipeSchema modifierRemoval() {
        return schema(TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs"), TinkerComponents.GROUP.key("name").optional("modifiers"),
            TinkerComponents.SIZED_INGREDIENT.key("tools").optional(defaultWorktableTool()),
            TinkerComponents.ITEM_STACK_OUTPUT.asArray().key("leftovers").optional(new ItemStack[0]), TinkerComponents.MODIFIER_PREDICATE.key("modifier_predicate").defaultOptional());
    }

    private static RecipeSchema modifierSetWorktable() {
        return schema(TinkerComponents.RESOURCE_LOCATION.key("data_key"), TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs"),
            TinkerComponents.BOOLEAN.key("add_to_set"), TinkerComponents.INGREDIENT_REQUIRED.key("tools").optional(defaultTool()), TinkerComponents.MODIFIER_PREDICATE.key("modifier_predicate").defaultOptional(),
            bool("allow_traits"));
    }

    private static RecipeSchema enchantmentConverting() {
        return schema(TinkerComponents.GROUP.key("name"), TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs"), TinkerComponents.BOOLEAN.key("match_book"),
            TinkerComponents.BOOLEAN.key("return_unenchanted"), TinkerComponents.MODIFIER_PREDICATE.key("modifier_predicate").defaultOptional());
    }

    private static RecipeSchema toggleInteraction() {
        return schema(TinkerComponents.SIZED_INGREDIENT.asArray().key("inputs"), TinkerComponents.INGREDIENT_REQUIRED.key("tools").optional(defaultTool()));
    }

    private static RecipeSchema materialSwapping() {
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.POSITIVE_INT.key("max_stack_size").optional(16),
            TinkerComponents.SIZED_INGREDIENT.asArray().key("extra_requirements").optional(new slimeknights.mantle.recipe.ingredient.SizedIngredient[0]));
    }

    private static RecipeSchema fixedMaterialSwapping() {
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.SIZED_INGREDIENT.key("ingredient"), TinkerComponents.MATERIAL_VARIANT.key("material"), TinkerComponents.INDEX_ARRAY.key("index"),
            TinkerComponents.POSITIVE_INT.key("max_stack_size").optional(16),
            TinkerComponents.NON_NEGATIVE_INT.key("repair_value").optional(0), TinkerComponents.SIZED_INGREDIENT.asArray().key("extra_requirements").optional(new slimeknights.mantle.recipe.ingredient.SizedIngredient[0]));
    }

    private static RecipeSchema partSwappingOverride() {
        return schema(TinkerComponents.INGREDIENT_REQUIRED.key("tools"), TinkerComponents.TOOL_PART.key("part"), TinkerComponents.INDEX_ARRAY.key("index"),
            TinkerComponents.POSITIVE_INT.key("max_stack_size").optional(16),
            TinkerComponents.SIZED_INGREDIENT.asArray().key("extra_requirements").optional(new slimeknights.mantle.recipe.ingredient.SizedIngredient[0]));
    }

    private static RecipeSchema tippedToolTransform() {
        return schema(TinkerComponents.MODIFIABLE_ITEM.key("result"), TinkerComponents.INGREDIENT_REQUIRED.key("input"), TinkerComponents.MODIFIER.key("modifier"),
            TinkerComponents.RESOURCE_LOCATION.key("slot_layout").defaultOptional(), TinkerComponents.MATERIAL_VARIANT.asArray().key("materials").optional(new MaterialVariantId[0]), group());
    }

    private static RecipeSchema severing() {
        return schema(TinkerComponents.ENTITY_INGREDIENT.key("entity"), TinkerComponents.ITEM_OUTPUT_REQUIRED_STACK.key("result"),
            TinkerComponents.PERCENT.key("per_level_chance").optional(0.05F), TinkerComponents.PERCENT.key("looting_bonus").optional(0.01F));
    }

    private static RecipeSchema ageableSevering() {
        return schema(TinkerComponents.ENTITY_INGREDIENT.key("entity"), TinkerComponents.ITEM_OUTPUT_REQUIRED_STACK.key("adult_result"),
            TinkerComponents.ITEM_OUTPUT_OPTIONAL_STACK.key("child_result").optional(ItemOutput.EMPTY), TinkerComponents.PERCENT.key("per_level_chance").optional(0.05F),
            TinkerComponents.PERCENT.key("looting_bonus").optional(0.01F));
    }

    private static RecipeSchema fixedSevering() {
        return schema(TinkerComponents.PERCENT.key("per_level_chance").optional(0.05F), TinkerComponents.PERCENT.key("looting_bonus").optional(0.01F));
    }

    private static Ingredient defaultTool() {
        return Ingredient.of(TinkerTags.Items.MODIFIABLE);
    }

    private static Ingredient defaultDurability() {
        return Ingredient.of(TinkerTags.Items.DURABILITY);
    }

    private static slimeknights.mantle.recipe.ingredient.SizedIngredient defaultWorktableTool() {
        return slimeknights.mantle.recipe.ingredient.SizedIngredient.of(defaultTool());
    }

    private static RecipeSchema alloy() {
        return chain(AlloyRecipeJS.class, AlloyRecipeJS::new,
            null,
            later(TinkerComponents.ALLOY_INGREDIENT.asArray().key("inputs").noBuilders(), TinkerComponents.FLUID_OUTPUT_REQUIRED.key("result").noBuilders()),
            TinkerComponents.POSITIVE_INT.key("temperature").defaultOptional()
        );
    }
}


