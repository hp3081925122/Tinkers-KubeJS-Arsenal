package com.hp.tinkers_kubejs_arsenal.kubejs.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.core.IngredientKJS;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.ItemMatch;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Wrapper;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.IntArrayLoadable;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.json.predicate.modifier.ModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.MultilevelModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.SwappableModifierRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.ToolCastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class TinkerComponents {
    public static final RecipeComponent<String> GROUP = StringComponent.ANY;
    public static final RecipeComponent<Ingredient> INGREDIENT_REQUIRED = new IngredientComponent(false);
    public static final RecipeComponent<Ingredient> INGREDIENT_OPTIONAL = new IngredientComponent(true);
    public static final RecipeComponent<SizedIngredient> SIZED_INGREDIENT = new SizedIngredientComponent();
    public static final RecipeComponent<ItemStack> ITEM_STACK_OUTPUT = new ItemStackOutputComponent();
    public static final RecipeComponent<FluidIngredient> FLUID_INGREDIENT = new FluidIngredientComponent();
    public static final RecipeComponent<FluidOutput> FLUID_OUTPUT_REQUIRED = new FluidOutputComponent(FluidOutput.Loadable.REQUIRED, "tinker_fluid_output");
    public static final RecipeComponent<FluidOutput> FLUID_OUTPUT_OPTIONAL = new FluidOutputComponent(FluidOutput.Loadable.OPTIONAL, "tinker_fluid_output_optional");
    public static final RecipeComponent<ItemOutput> ITEM_OUTPUT_REQUIRED_ITEM = new ItemOutputComponent(ItemOutput.Loadable.REQUIRED_ITEM, "tinker_item_output", false);
    public static final RecipeComponent<ItemOutput> ITEM_OUTPUT_REQUIRED_STACK = new ItemOutputComponent(ItemOutput.Loadable.REQUIRED_STACK, "tinker_item_stack_output", true);
    public static final RecipeComponent<ItemOutput> ITEM_OUTPUT_OPTIONAL_STACK = new ItemOutputComponent(ItemOutput.Loadable.OPTIONAL_STACK, "tinker_item_stack_output_optional", true);
    public static final RecipeComponent<EntityIngredient> ENTITY_INGREDIENT = new LoadableComponent<>("entity_ingredient", EntityIngredient.class,
        EntityIngredient.LOADABLE, ComponentRole.INPUT, TinkerComponents::entityDescription,
        json -> EntityIngredient.LOADABLE.convert(normalizeEntity(json), "entity"),
        ingredient -> ingredient == null ? "Entity ingredient can't be empty" : "", null);
    public static final RecipeComponent<AlloyRecipe.AlloyIngredient> ALLOY_INGREDIENT = new AlloyIngredientComponent();
    public static final RecipeComponent<MaterialVariantId> MATERIAL_VARIANT = typedId("material_variant", MaterialVariantId.class, MaterialVariantId.LOADABLE, TinkerTypes.MATERIAL);
    public static final RecipeComponent<MaterialStatsId> MATERIAL_STATS = typedId("material_stats", MaterialStatsId.class,
        new ParserLoadable<>(MaterialStatsId::tryParse, MaterialStatsId::toString), TinkerTypes.MATERIAL_STAT);
    public static final RecipeComponent<ModifierId> MODIFIER = typedId("modifier", ModifierId.class,
        new ParserLoadable<>(ModifierId::tryParse, ModifierId::toString), TinkerTypes.MODIFIER);
    public static final RecipeComponent<Pattern> PATTERN = typedId("pattern", Pattern.class,
        new ParserLoadable<>(Pattern.PARSER::tryParse, Pattern::toString), TinkerTypes.PATTERN);
    public static final RecipeComponent<IJsonPredicate<MaterialVariantId>> MATERIAL_PREDICATE = new LoadableComponent<>("material_predicate", IJsonPredicate.class,
        MaterialPredicate.LOADER, ComponentRole.OTHER, ctx -> TypeDescJS.STRING.or(TypeDescJS.ANY_MAP), TinkerComponents::readMaterialPredicate,
        value -> "", null);
    public static final RecipeComponent<Item> ITEM = itemId("item", Item.class, Loadables.ITEM, TinkerTypes.ITEM, ComponentRole.OTHER, null);
    public static final RecipeComponent<Item> ITEM_RESULT = itemId("item_result", Item.class, Loadables.ITEM, TinkerTypes.ITEM, ComponentRole.OUTPUT, ItemStack::new, stack -> stack.getItem());
    public static final RecipeComponent<IMaterialItem> MATERIAL_ITEM = itemId("material_item", IMaterialItem.class, TinkerLoadables.MATERIAL_ITEM, TinkerTypes.MATERIAL_ITEM,
        ComponentRole.OUTPUT, item -> new ItemStack(item.asItem()), stack -> stack.getItem() instanceof IMaterialItem item ? item : null);
    public static final RecipeComponent<IModifiable> MODIFIABLE_ITEM = itemId("modifiable_item", IModifiable.class, TinkerLoadables.MODIFIABLE_ITEM, TinkerTypes.MODIFIABLE,
        ComponentRole.OUTPUT, item -> new ItemStack(item.asItem()), stack -> stack.getItem() instanceof IModifiable item ? item : null);
    public static final RecipeComponent<Integer> POSITIVE_INT = NumberComponent.INT.min(1);
    public static final RecipeComponent<Integer> NON_NEGATIVE_INT = NumberComponent.INT.min(0);
    public static final RecipeComponent<Boolean> BOOLEAN = BooleanComponent.BOOLEAN;
    public static final RecipeComponent<IMeltingContainer.OreRateType> ORE_RATE = new LoadableComponent<>("ore_rate", IMeltingContainer.OreRateType.class,
        TinkerLoadables.ORE_RATE_TYPE, ComponentRole.OTHER, ctx -> TinkerTypes.ORE_RATE, json -> TinkerLoadables.ORE_RATE_TYPE.convert(json, "rate"),
        value -> value == null ? "Ore rate can't be empty" : "", null);
    public static final RecipeComponent<ToolCastingRecipe.CastPurpose> CAST_PURPOSE = new EnumComponent<>(ToolCastingRecipe.CastPurpose.class);
    public static final RecipeComponent<IntRange> LEVEL_RANGE = new LoadableComponent<>("level_range", IntRange.class,
        ModifierEntry.VALID_LEVEL, ComponentRole.OTHER, ctx -> TinkerTypes.LEVEL_RANGE,
        json -> ModifierEntry.VALID_LEVEL.convert(json, "level"), value -> value == null ? "Level range can't be empty" : "", null);
    public static final RecipeComponent<SlotType.SlotCount> SLOT_COUNT = new LoadableComponent<>("slot_count", SlotType.SlotCount.class,
        SlotType.SlotCount.LOADABLE, ComponentRole.OTHER, ctx -> TinkerTypes.SLOT_COUNT,
        json -> SlotType.SlotCount.LOADABLE.convert(json, "slots"), value -> value == null ? "Slot count can't be empty" : "", null);
    public static final RecipeComponent<MultilevelModifierRecipe.LevelEntry> LEVEL_ENTRY = new LoadableComponent<>("level_entry", MultilevelModifierRecipe.LevelEntry.class,
        MultilevelModifierRecipe.LevelEntry.LOADABLE, ComponentRole.OTHER,
        ctx -> TypeDescJS.object().add("level", TinkerTypes.LEVEL_RANGE).add("slots", TinkerTypes.SLOT_COUNT, true),
        json -> MultilevelModifierRecipe.LevelEntry.LOADABLE.convert(json, "level"), value -> value == null ? "Level entry can't be empty" : "", null);
    public static final RecipeComponent<IJsonPredicate<ModifierId>> MODIFIER_PREDICATE = new LoadableComponent<>("modifier_predicate", IJsonPredicate.class,
        ModifierPredicate.LOADER, ComponentRole.OTHER, ctx -> TypeDescJS.STRING.or(TypeDescJS.ANY_MAP),
        json -> ModifierPredicate.LOADER.convert(json, "modifier_predicate"), value -> value == null ? "Modifier predicate can't be empty" : "", null);
    public static final RecipeComponent<IToolPart> TOOL_PART = new LoadableComponent<>("tool_part", IToolPart.class,
        TinkerLoadables.TOOL_PART_ITEM, ComponentRole.OTHER, ctx -> TinkerTypes.TOOL_PART,
        json -> TinkerLoadables.TOOL_PART_ITEM.convert(json, "part"), value -> value == null ? "Tool part can't be empty" : "", null);
    public static final RecipeComponent<int[]> INDEX_ARRAY = new LoadableComponent<>("index_array", int[].class,
        new IntArrayLoadable(IntLoadable.FROM_ZERO, -1, 10), ComponentRole.OTHER, ctx -> TypeDescJS.any(TypeDescJS.NUMBER, TypeDescJS.NUMBER.asArray()),
        json -> new IntArrayLoadable(IntLoadable.FROM_ZERO, -1, 10).convert(json, "index"), value -> value == null ? "Index can't be empty" : "", null);
    public static final RecipeComponent<Float> PERCENT = new LoadableComponent<>("percent", Float.class,
        FloatLoadable.PERCENT, ComponentRole.OTHER, ctx -> TypeDescJS.NUMBER,
        json -> FloatLoadable.PERCENT.convert(json, "percent"), value -> value == null ? "Percent can't be empty" : "", null);
    public static final RecipeComponent<SwappableModifierRecipe.VariantFormatter> VARIANT_FORMATTER = new LoadableComponent<>("variant_formatter", SwappableModifierRecipe.VariantFormatter.class,
        SwappableModifierRecipe.VariantFormatter.LOADER, ComponentRole.OTHER, ctx -> TinkerTypes.VARIANT_FORMATTER,
        json -> SwappableModifierRecipe.VariantFormatter.LOADER.convert(json, "variant_formatter"), value -> value == null ? "Variant formatter can't be empty" : "", null);
    public static final RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new LoadableComponent<>("resource_location", ResourceLocation.class,
        Loadables.RESOURCE_LOCATION, ComponentRole.OTHER, ctx -> TypeDescJS.STRING,
        json -> Loadables.RESOURCE_LOCATION.convert(json, "resource_location"), value -> value == null ? "Resource location can't be empty" : "", null);

    private TinkerComponents() {
    }

    public record FluidOutputUnit(FluidOutput output, int unitSize) {
    }

    public record OreByproduct(FluidOutput output, IMeltingContainer.OreRateType rate) {
    }

    public static final RecipeComponent<FluidOutputUnit> FLUID_OUTPUT_UNIT = new FluidOutputWrapperComponent<>("fluid_output_unit", FluidOutputUnit.class,
        "unit_size", TypeDescJS.NUMBER) {
        @Override
        protected FluidOutput output(FluidOutputUnit value) {
            return value.output();
        }

        @Override
        protected FluidOutputUnit wrap(FluidOutput output, FluidOutputUnit original) {
            return new FluidOutputUnit(output, original.unitSize());
        }

        @Override
        protected FluidOutputUnit read(FluidOutput output, JsonObject json) {
            int unitSize = json.has("unit_size") ? json.get("unit_size").getAsInt() : 1;
            if (unitSize < 1) {
                throw new IllegalArgumentException("unit_size must be at least 1");
            }
            return new FluidOutputUnit(output, unitSize);
        }

        @Override
        protected void write(FluidOutputUnit value, JsonObject json) {
            if (value.unitSize() != 1) {
                json.addProperty("unit_size", value.unitSize());
            }
        }
    };

    public static final RecipeComponent<OreByproduct> ORE_BYPRODUCT = new FluidOutputWrapperComponent<>("ore_byproduct", OreByproduct.class,
        "rate", TinkerTypes.ORE_RATE) {
        @Override
        protected FluidOutput output(OreByproduct value) {
            return value.output();
        }

        @Override
        protected OreByproduct wrap(FluidOutput output, OreByproduct original) {
            return new OreByproduct(output, original.rate());
        }

        @Override
        protected OreByproduct read(FluidOutput output, JsonObject json) {
            IMeltingContainer.OreRateType rate = json.has("rate")
                ? TinkerLoadables.ORE_RATE_TYPE.convert(json.get("rate"), "rate")
                : IMeltingContainer.OreRateType.DEFAULT;
            return new OreByproduct(output, rate);
        }

        @Override
        protected void write(OreByproduct value, JsonObject json) {
            if (value.rate() != IMeltingContainer.OreRateType.DEFAULT) {
                json.add("rate", TinkerLoadables.ORE_RATE_TYPE.serialize(value.rate()));
            }
        }
    };

    private static <T> RecipeComponent<T> typedId(String name, Class<T> type, Loadable<T> loadable, TypeDescJS description) {
        return new LoadableComponent<>(name, type, loadable, ComponentRole.OTHER, ctx -> description,
            json -> loadable.convert(json, name), value -> value == null ? "Value can't be null" : "", null);
    }

    private static <T> RecipeComponent<T> itemId(String name, Class<T> type, Loadable<T> loadable, TypeDescJS description, ComponentRole role,
                                                  Function<T, ItemStack> outputStack) {
        return itemId(name, type, loadable, description, role, outputStack, null);
    }

    private static <T> RecipeComponent<T> itemId(String name, Class<T> type, Loadable<T> loadable, TypeDescJS description, ComponentRole role,
                                                  Function<T, ItemStack> outputStack, Function<ItemStack, T> outputValue) {
        return new LoadableComponent<>(name, type, loadable, role, ctx -> description,
            json -> loadable.convert(json, name), value -> value == null ? "Item can't be empty" : "", outputStack, outputValue);
    }

    static JsonElement toJson(Object value) {
        Object unwrapped = Wrapper.unwrapped(value);
        if (unwrapped instanceof JsonElement element) {
            return element;
        }
        JsonElement json = JsonIO.of(unwrapped);
        if (json == null) {
            throw new IllegalArgumentException("Expected a JSON value but got " + unwrapped);
        }
        return json;
    }

    private static TypeDescJS fluidDescription(DescriptionContext ctx) {
        return TypeDescJS.any(TypeDescJS.STRING, ctx.javaType(FluidStackJS.class),
            TypeDescJS.object().add("fluid", TinkerTypes.FLUID, true).add("tag", TinkerTypes.FLUID_TAG, true).add("amount", TypeDescJS.NUMBER));
    }

    private static TypeDescJS entityDescription(DescriptionContext ctx) {
        return TypeDescJS.any(TinkerTypes.ENTITY_OR_TAG,
            TypeDescJS.object().add("type", TinkerTypes.ENTITY, true).add("tag", TinkerTypes.ENTITY_TAG, true).add("types", TinkerTypes.ENTITY.asArray(), true));
    }

    private static JsonElement normalizeEntity(JsonElement json) {
        if (json.isJsonPrimitive()) {
            String text = json.getAsString();
            JsonObject object = new JsonObject();
            if (text.startsWith("#")) {
                object.addProperty("tag", text.substring(1));
            } else {
                object.addProperty("type", text);
            }
            return object;
        }
        if (json.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement element : json.getAsJsonArray()) {
                array.add(normalizeEntity(element));
            }
            return array;
        }
        return json;
    }

    private static JsonElement normalizeIngredient(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String text = json.getAsString();
            JsonObject object = new JsonObject();
            if (text.startsWith("#")) {
                object.addProperty("tag", text.substring(1));
            } else {
                object.addProperty("item", text);
            }
            return object;
        }
        if (json.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement element : json.getAsJsonArray()) {
                array.add(normalizeIngredient(element));
            }
            return array;
        }
        return json;
    }

    private static JsonElement normalizeFluid(Object value) {
        Object unwrapped = Wrapper.unwrapped(value);
        if (unwrapped instanceof FluidStackJS stack) {
            JsonObject json = new JsonObject();
            json.addProperty("fluid", stack.getId());
            json.addProperty("amount", stack.kjs$getAmount());
            return json;
        }
        if (unwrapped instanceof CharSequence text) {
            return fluidText(text.toString());
        }
        return toJson(unwrapped);
    }

    private static JsonElement fluidText(String text) {
        String[] parts = text.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Fluid '" + text + "' needs an explicit amount, e.g. '" + parts[0] + " 90' or Fluid.of('" + parts[0] + "', 90)");
        }
        JsonObject json = new JsonObject();
        if (parts[0].startsWith("#")) {
            json.addProperty("tag", parts[0].substring(1));
        } else {
            json.addProperty("fluid", parts[0]);
        }
        json.addProperty("amount", Integer.parseInt(parts[1]));
        return json;
    }

    private static IJsonPredicate<MaterialVariantId> readMaterialPredicate(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return MaterialPredicate.variant(MaterialVariantId.LOADABLE.convert(json, "material", TypedMap.EMPTY));
        }
        return MaterialPredicate.LOADER.convert(json, "allowed_materials", TypedMap.EMPTY);
    }

    private static Fluid matchedFluid(ReplacementMatch match) {
        return match instanceof FluidStackJS stack && !stack.kjs$isEmpty() ? stack.getFluid() : null;
    }

    private static FluidOutput replaceOutput(FluidOutput original, OutputReplacement with) {
        if (!(with instanceof FluidStackJS stack)) {
            return original;
        }
        return FluidOutput.fromStack(new FluidStack(stack.getFluid(), original.getAmount(), stack.getNbt()));
    }

    private static boolean outputMatches(FluidOutput output, ReplacementMatch match, FluidOutput.Loadable loadable) {
        Fluid fluid = matchedFluid(match);
        return fluid != null && output != null && !output.isEmpty() && TagJson.fluidMatches(loadable.serialize(output), fluid);
    }

    public record MaterialItemResult(IMaterialItem item, int count) {
    }

    public record SwappableResult(ModifierId name, String value) {
    }

    public static final RecipeComponent<MaterialItemResult> MATERIAL_ITEM_RESULT = new RecipeComponent<>() {
        @Override public String componentType() { return "material_item_result"; }
        @Override public ComponentRole role() { return ComponentRole.OUTPUT; }
        @Override public Class<?> componentClass() { return MaterialItemResult.class; }
        @Override public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.any(TinkerTypes.MATERIAL_ITEM, TypeDescJS.object().add("item", TinkerTypes.MATERIAL_ITEM).add("count", TypeDescJS.NUMBER, true));
        }
        @Override public JsonElement write(RecipeJS recipe, MaterialItemResult value) {
            JsonObject json = new JsonObject();
            json.add("item", TinkerLoadables.MATERIAL_ITEM.serialize(value.item()));
            if (value.count() != 1) json.addProperty("count", value.count());
            return json;
        }
        @Override public MaterialItemResult read(RecipeJS recipe, Object from) {
            JsonElement element = toJson(Wrapper.unwrapped(from));
            if (element.isJsonPrimitive()) {
                JsonObject wrapped = new JsonObject();
                wrapped.add("item", element);
                element = wrapped;
            }
            JsonObject json = element.getAsJsonObject();
            IMaterialItem item = TinkerLoadables.MATERIAL_ITEM.convert(json.get("item"), "item");
            int count = json.has("count") ? json.get("count").getAsInt() : 1;
            if (count < 1) throw new IllegalArgumentException("count must be at least 1");
            return new MaterialItemResult(item, count);
        }
        @Override public boolean isOutput(RecipeJS recipe, MaterialItemResult value, ReplacementMatch match) {
            return match instanceof ItemMatch itemMatch && itemMatch.contains(new ItemStack(value.item().asItem()));
        }
        @Override public MaterialItemResult replaceOutput(RecipeJS recipe, MaterialItemResult original, ReplacementMatch match, OutputReplacement with) {
            if (!this.isOutput(recipe, original, match) || !(with instanceof OutputItem output) || !(output.item.getItem() instanceof IMaterialItem item)) return original;
            return new MaterialItemResult(item, original.count());
        }
        @Override public String checkEmpty(RecipeKey<MaterialItemResult> key, MaterialItemResult value) {
            return value == null || value.item() == null ? "Material item result can't be empty" : "";
        }
    };

    public static final RecipeComponent<SwappableResult> SWAPPABLE_RESULT = new RecipeComponent<>() {
        @Override public String componentType() { return "swappable_result"; }
        @Override public ComponentRole role() { return ComponentRole.OTHER; }
        @Override public Class<?> componentClass() { return SwappableResult.class; }
        @Override public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.any(TinkerTypes.MODIFIER, TypeDescJS.object().add("name", TinkerTypes.MODIFIER).add("value", TypeDescJS.STRING));
        }
        @Override public JsonElement write(RecipeJS recipe, SwappableResult value) {
            JsonObject json = new JsonObject();
            json.add("name", ModifierId.PARSER.serialize(value.name()));
            json.addProperty("value", value.value());
            return json;
        }
        @Override public SwappableResult read(RecipeJS recipe, Object from) {
            JsonElement element = toJson(Wrapper.unwrapped(from));
            if (element.isJsonPrimitive()) {
                return new SwappableResult(ModifierId.PARSER.convert(element, "name"), null);
            }
            JsonObject json = element.getAsJsonObject();
            if (!json.has("name") || !json.has("value")) {
                throw new RecipeExceptionJS("Swappable modifier result requires both 'name' and 'value'");
            }
            return new SwappableResult(ModifierId.PARSER.convert(json.get("name"), "name"), json.get("value").getAsString());
        }
        @Override public String checkEmpty(RecipeKey<SwappableResult> key, SwappableResult value) {
            return value == null || value.name() == null ? "Swappable result can't be empty" : "";
        }
    };

    private static final class SizedIngredientComponent implements RecipeComponent<SizedIngredient> {
        @Override public String componentType() { return "tinker_sized_ingredient"; }
        @Override public ComponentRole role() { return ComponentRole.INPUT; }
        @Override public Class<?> componentClass() { return SizedIngredient.class; }
        @Override public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.any(ctx.javaType(InputItem.class), TypeDescJS.object().add("item", TinkerTypes.ITEM, true).add("tag", TinkerTypes.ITEM_TAG, true).add("amount_needed", TypeDescJS.NUMBER, true));
        }
        @Override public JsonElement write(RecipeJS recipe, SizedIngredient value) { return SizedIngredient.LOADABLE.serialize(value); }
        @Override public SizedIngredient read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof SizedIngredient sized) return sized;
            if (unwrapped instanceof InputItem item) return SizedIngredient.of(item.ingredient, item.count);
            if (unwrapped instanceof CharSequence) {
                InputItem item = InputItem.of(unwrapped.toString());
                return SizedIngredient.of(item.ingredient, item.count);
            }
            return SizedIngredient.LOADABLE.convert(normalizeIngredient(toJson(unwrapped)), "ingredient", TypedMap.EMPTY);
        }
        @Override public boolean hasPriority(RecipeJS recipe, Object from) { return Wrapper.unwrapped(from) instanceof SizedIngredient || Wrapper.unwrapped(from) instanceof InputItem; }
        @Override public boolean isInput(RecipeJS recipe, SizedIngredient value, ReplacementMatch match) {
            return match instanceof ItemMatch itemMatch && value != null && itemMatch.contains(value.getIngredient());
        }
        @Override public SizedIngredient replaceInput(RecipeJS recipe, SizedIngredient original, ReplacementMatch match, InputReplacement with) {
            if (!this.isInput(recipe, original, match) || !(with instanceof InputItem item)) return original;
            return SizedIngredient.of(item.ingredient, original.getAmountNeeded());
        }
        @Override public String checkEmpty(RecipeKey<SizedIngredient> key, SizedIngredient value) { return value == null || value.isEmpty() ? "Sized ingredient can't be empty" : ""; }
    }

    private static final class ItemStackOutputComponent implements RecipeComponent<ItemStack> {
        @Override public String componentType() { return "tinker_item_stack_output"; }
        @Override public ComponentRole role() { return ComponentRole.OUTPUT; }
        @Override public Class<?> componentClass() { return ItemStack.class; }
        @Override public TypeDescJS constructorDescription(DescriptionContext ctx) { return ctx.javaType(OutputItem.class); }
        @Override public JsonElement write(RecipeJS recipe, ItemStack value) { return ItemStackLoadable.REQUIRED_STACK_NBT.serialize(value); }
        @Override public ItemStack read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof ItemStack stack) return stack.copy();
            if (unwrapped instanceof OutputItem output) return output.item.copy();
            return ItemStackLoadable.REQUIRED_STACK_NBT.convert(toJson(unwrapped), "result");
        }
        @Override public boolean hasPriority(RecipeJS recipe, Object from) { Object unwrapped = Wrapper.unwrapped(from); return unwrapped instanceof ItemStack || unwrapped instanceof OutputItem; }
        @Override public boolean isOutput(RecipeJS recipe, ItemStack value, ReplacementMatch match) { return match instanceof ItemMatch itemMatch && value != null && !value.isEmpty() && itemMatch.contains(value); }
        @Override public ItemStack replaceOutput(RecipeJS recipe, ItemStack original, ReplacementMatch match, OutputReplacement with) {
            if (!this.isOutput(recipe, original, match) || !(with instanceof OutputItem output)) return original;
            ItemStack replacement = output.item.copy(); replacement.setCount(original.getCount()); return replacement;
        }
        @Override public String checkEmpty(RecipeKey<ItemStack> key, ItemStack value) { return value == null || value.isEmpty() ? "Item stack output can't be empty" : ""; }
    }

    private static final class IngredientComponent implements RecipeComponent<Ingredient> {
        private final boolean allowEmpty;
        private final IngredientLoadable loadable;

        private IngredientComponent(boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
            this.loadable = allowEmpty ? IngredientLoadable.ALLOW_EMPTY : IngredientLoadable.DISALLOW_EMPTY;
        }

        @Override
        public String componentType() {
            return this.allowEmpty ? "tinker_ingredient_optional" : "tinker_ingredient";
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.INPUT;
        }

        @Override
        public Class<?> componentClass() {
            return Ingredient.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return ctx.javaType(InputItem.class);
        }

        @Override
        public JsonElement write(RecipeJS recipe, Ingredient value) {
            return this.loadable.serialize(value);
        }

        @Override
        public Ingredient read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof Ingredient ingredient) {
                return ingredient;
            }
            if (unwrapped instanceof InputItem item) {
                return item.ingredient;
            }
            if (unwrapped instanceof ItemStack stack) {
                return Ingredient.of(stack);
            }
            return this.loadable.convert(normalizeIngredient(toJson(unwrapped)), "ingredient", TypedMap.EMPTY);
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            return unwrapped instanceof Ingredient || unwrapped instanceof InputItem || unwrapped instanceof ItemStack;
        }

        @Override
        public boolean isInput(RecipeJS recipe, Ingredient value, ReplacementMatch match) {
            return match instanceof ItemMatch itemMatch && value != null && !value.isEmpty() && ((IngredientKJS) (Object) value).kjs$canBeUsedForMatching() && itemMatch.contains(value);
        }

        @Override
        public Ingredient replaceInput(RecipeJS recipe, Ingredient original, ReplacementMatch match, InputReplacement with) {
            if (!this.isInput(recipe, original, match)) {
                return original;
            }
            if (with instanceof InputItem item) {
                return item.ingredient;
            }
            return original;
        }

        @Override
        public String checkEmpty(RecipeKey<Ingredient> key, Ingredient value) {
            return !this.allowEmpty && (value == null || value.isEmpty()) ? "Ingredient '" + key.name + "' can't be empty" : "";
        }
    }

    private static final class ItemOutputComponent implements RecipeComponent<ItemOutput> {
        private final ItemOutput.Loadable loadable;
        private final String type;
        private final boolean stack;

        private ItemOutputComponent(ItemOutput.Loadable loadable, String type, boolean stack) {
            this.loadable = loadable;
            this.type = type;
            this.stack = stack;
        }

        @Override
        public String componentType() {
            return this.type;
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.OUTPUT;
        }

        @Override
        public Class<?> componentClass() {
            return ItemOutput.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            TypeDescJS object = this.stack
                ? TypeDescJS.object().add("item", TinkerTypes.ITEM, true).add("tag", TinkerTypes.ITEM_TAG, true).add("count", TypeDescJS.NUMBER, true)
                : TypeDescJS.object().add("item", TinkerTypes.ITEM, true).add("tag", TinkerTypes.ITEM_TAG, true);
            return TypeDescJS.any(ctx.javaType(OutputItem.class), object);
        }

        @Override
        public JsonElement write(RecipeJS recipe, ItemOutput value) {
            return this.loadable.serialize(value);
        }

        @Override
        public ItemOutput read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof ItemOutput output) {
                return output;
            }
            if (unwrapped instanceof OutputItem item) {
                return ItemOutput.fromStack(item.item.copy());
            }
            if (unwrapped instanceof ItemStack stack) {
                return ItemOutput.fromStack(stack.copy());
            }
            JsonElement json = toJson(unwrapped);
            if (json.isJsonPrimitive() && json.getAsString().startsWith("#")) {
                JsonObject tag = new JsonObject();
                tag.addProperty("tag", json.getAsString().substring(1));
                json = tag;
            }
            return this.loadable.convert(json, "result", TypedMap.EMPTY);
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            return unwrapped instanceof ItemOutput || unwrapped instanceof OutputItem || unwrapped instanceof ItemStack;
        }

        @Override
        public boolean isOutput(RecipeJS recipe, ItemOutput value, ReplacementMatch match) {
            return match instanceof ItemMatch itemMatch && value != null && TagJson.itemOutputMatches(this.loadable.serialize(value), itemMatch);
        }

        @Override
        public ItemOutput replaceOutput(RecipeJS recipe, ItemOutput original, ReplacementMatch match, OutputReplacement with) {
            if (!this.isOutput(recipe, original, match) || !(with instanceof OutputItem item)) {
                return original;
            }
            ItemStack replacement = item.item.copy();
            replacement.setCount(Math.max(1, original.getCount()));
            return ItemOutput.fromStack(replacement);
        }

        @Override
        public String checkEmpty(RecipeKey<ItemOutput> key, ItemOutput value) {
            return this.loadable != ItemOutput.Loadable.OPTIONAL_STACK && (value == null || value.isEmpty())
                ? "Item output '" + key.name + "' can't be empty" : "";
        }
    }

    private static final class FluidIngredientComponent implements RecipeComponent<FluidIngredient> {
        @Override
        public String componentType() {
            return "tinker_fluid_ingredient";
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.INPUT;
        }

        @Override
        public Class<?> componentClass() {
            return FluidIngredient.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.any(fluidDescription(ctx), fluidDescription(ctx).asArray());
        }

        @Override
        public JsonElement write(RecipeJS recipe, FluidIngredient value) {
            return value.serialize();
        }

        @Override
        public FluidIngredient read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof FluidIngredient ingredient) {
                return ingredient;
            }
            if (unwrapped instanceof FluidStackJS stack) {
                return FluidIngredient.of(stack.getFluid(), (int) stack.kjs$getAmount());
            }
            if (unwrapped instanceof Iterable<?> iterable && !(unwrapped instanceof CharSequence)) {
                JsonArray array = new JsonArray();
                for (Object element : iterable) {
                    array.add(normalizeFluid(element));
                }
                return FluidIngredient.LOADABLE.convert(array, "fluid", TypedMap.EMPTY);
            }
            return FluidIngredient.LOADABLE.convert(normalizeFluid(unwrapped), "fluid", TypedMap.EMPTY);
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            return unwrapped instanceof FluidStackJS || unwrapped instanceof FluidIngredient;
        }

        @Override
        public boolean isInput(RecipeJS recipe, FluidIngredient value, ReplacementMatch match) {
            Fluid fluid = matchedFluid(match);
            return fluid != null && value != null && value != FluidIngredient.EMPTY && TagJson.fluidMatches(value.serialize(), fluid);
        }

        @Override
        public FluidIngredient replaceInput(RecipeJS recipe, FluidIngredient original, ReplacementMatch match, InputReplacement with) {
            if (!this.isInput(recipe, original, match) || !(with instanceof FluidStackJS stack)) {
                return original;
            }
            return FluidIngredient.LOADABLE.convert(TagJson.replaceFluid(original.serialize(), matchedFluid(match), stack.getFluid()), "fluid", TypedMap.EMPTY);
        }

        @Override
        public String checkEmpty(RecipeKey<FluidIngredient> key, FluidIngredient value) {
            return value == null ? "Fluid ingredient '" + key.name + "' can't be empty" : "";
        }
    }

    private static final class AlloyIngredientComponent implements RecipeComponent<AlloyRecipe.AlloyIngredient> {
        @Override
        public String componentType() {
            return "alloy_ingredient";
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.INPUT;
        }

        @Override
        public Class<?> componentClass() {
            return AlloyRecipe.AlloyIngredient.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.any(fluidDescription(ctx),
                TypeDescJS.object().add("match", fluidDescription(ctx)).add("catalyst", TypeDescJS.BOOLEAN, true));
        }

        @Override
        public JsonElement write(RecipeJS recipe, AlloyRecipe.AlloyIngredient value) {
            return AlloyRecipe.AlloyIngredient.LOADABLE.serialize(value);
        }

        @Override
        public AlloyRecipe.AlloyIngredient read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof AlloyRecipe.AlloyIngredient ingredient) {
                return ingredient;
            }
            if (unwrapped instanceof FluidStackJS || unwrapped instanceof CharSequence) {
                return new AlloyRecipe.AlloyIngredient(FLUID_INGREDIENT.read(recipe, unwrapped), false);
            }
            return AlloyRecipe.AlloyIngredient.LOADABLE.convert(toJson(unwrapped), "input");
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            return Wrapper.unwrapped(from) instanceof AlloyRecipe.AlloyIngredient;
        }

        @Override
        public boolean isInput(RecipeJS recipe, AlloyRecipe.AlloyIngredient value, ReplacementMatch match) {
            return value != null && FLUID_INGREDIENT.isInput(recipe, value.fluid(), match);
        }

        @Override
        public AlloyRecipe.AlloyIngredient replaceInput(RecipeJS recipe, AlloyRecipe.AlloyIngredient original, ReplacementMatch match, InputReplacement with) {
            if (!this.isInput(recipe, original, match)) {
                return original;
            }
            return new AlloyRecipe.AlloyIngredient(FLUID_INGREDIENT.replaceInput(recipe, original.fluid(), match, with), original.catalyst());
        }

        @Override
        public String checkEmpty(RecipeKey<AlloyRecipe.AlloyIngredient> key, AlloyRecipe.AlloyIngredient value) {
            return value == null || value.fluid().serialize().isJsonNull() ? "Alloy ingredient can't be empty" : "";
        }
    }

    private static final class FluidOutputComponent implements RecipeComponent<FluidOutput> {
        private final FluidOutput.Loadable loadable;
        private final String type;

        private FluidOutputComponent(FluidOutput.Loadable loadable, String type) {
            this.loadable = loadable;
            this.type = type;
        }

        @Override
        public String componentType() {
            return this.type;
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.OUTPUT;
        }

        @Override
        public Class<?> componentClass() {
            return FluidOutput.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return fluidDescription(ctx);
        }

        @Override
        public JsonElement write(RecipeJS recipe, FluidOutput value) {
            return this.loadable.serialize(value);
        }

        @Override
        public FluidOutput read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (unwrapped instanceof FluidOutput output) {
                return output;
            }
            return this.loadable.convert(normalizeFluid(unwrapped), "fluid", TypedMap.EMPTY);
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            return unwrapped instanceof FluidStackJS || unwrapped instanceof FluidOutput;
        }

        @Override
        public boolean isOutput(RecipeJS recipe, FluidOutput value, ReplacementMatch match) {
            return outputMatches(value, match, this.loadable);
        }

        @Override
        public FluidOutput replaceOutput(RecipeJS recipe, FluidOutput original, ReplacementMatch match, OutputReplacement with) {
            return this.isOutput(recipe, original, match) ? TinkerComponents.replaceOutput(original, with) : original;
        }

        @Override
        public String checkEmpty(RecipeKey<FluidOutput> key, FluidOutput value) {
            return this.loadable == FluidOutput.Loadable.REQUIRED && (value == null || value.isEmpty())
                ? "Fluid output '" + key.name + "' can't be empty" : "";
        }
    }

    private abstract static class FluidOutputWrapperComponent<T> implements RecipeComponent<T> {
        private final String type;
        private final Class<T> valueClass;
        private final String extraKey;
        private final TypeDescJS extraType;

        private FluidOutputWrapperComponent(String type, Class<T> valueClass, String extraKey, TypeDescJS extraType) {
            this.type = type;
            this.valueClass = valueClass;
            this.extraKey = extraKey;
            this.extraType = extraType;
        }

        protected abstract FluidOutput output(T value);

        protected abstract T wrap(FluidOutput output, T original);

        protected abstract T read(FluidOutput output, JsonObject json);

        protected abstract void write(T value, JsonObject json);

        @Override
        public String componentType() {
            return this.type;
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.OUTPUT;
        }

        @Override
        public Class<?> componentClass() {
            return this.valueClass;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.any(TypeDescJS.STRING, ctx.javaType(FluidStackJS.class),
                TypeDescJS.object().add("fluid", TinkerTypes.FLUID, true).add("tag", TinkerTypes.FLUID_TAG, true)
                    .add("amount", TypeDescJS.NUMBER).add(this.extraKey, this.extraType, true));
        }

        @Override
        public JsonElement write(RecipeJS recipe, T value) {
            JsonObject json = FluidOutput.Loadable.REQUIRED.serialize(this.output(value)).getAsJsonObject();
            this.write(value, json);
            return json;
        }

        @Override
        public T read(RecipeJS recipe, Object from) {
            Object unwrapped = Wrapper.unwrapped(from);
            if (this.valueClass.isInstance(unwrapped)) {
                return this.valueClass.cast(unwrapped);
            }
            JsonElement json = normalizeFluid(unwrapped);
            JsonObject object = json.getAsJsonObject().deepCopy();
            JsonElement extra = object.remove(this.extraKey);
            FluidOutput output = FluidOutput.Loadable.REQUIRED.convert(object, "result", TypedMap.EMPTY);
            if (extra != null) {
                object.add(this.extraKey, extra);
            }
            return this.read(output, object);
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            return this.valueClass.isInstance(Wrapper.unwrapped(from));
        }

        @Override
        public boolean isOutput(RecipeJS recipe, T value, ReplacementMatch match) {
            return value != null && outputMatches(this.output(value), match, FluidOutput.Loadable.REQUIRED);
        }

        @Override
        public T replaceOutput(RecipeJS recipe, T original, ReplacementMatch match, OutputReplacement with) {
            return this.isOutput(recipe, original, match) ? this.wrap(TinkerComponents.replaceOutput(this.output(original), with), original) : original;
        }

        @Override
        public String checkEmpty(RecipeKey<T> key, T value) {
            return value == null || this.output(value).isEmpty() ? "Fluid output '" + key.name + "' can't be empty" : "";
        }
    }

    private static final class ParserLoadable<T> implements Loadable<T> {
        private final Function<String, T> parser;
        private final Function<T, String> serializer;

        private ParserLoadable(Function<String, T> parser, Function<T, String> serializer) {
            this.parser = parser;
            this.serializer = serializer;
        }

        @Override
        public T convert(JsonElement element, String key, TypedMap context) {
            T value = this.parser.apply(element.getAsString());
            if (value == null) {
                throw new IllegalArgumentException("Invalid " + key + ": " + element);
            }
            return value;
        }

        @Override
        public JsonElement serialize(T value) {
            return new JsonPrimitive(this.serializer.apply(value));
        }

        @Override
        public T decode(net.minecraft.network.FriendlyByteBuf buffer, TypedMap context) {
            return this.parser.apply(buffer.readUtf());
        }

        @Override
        public void encode(net.minecraft.network.FriendlyByteBuf buffer, T value) {
            buffer.writeUtf(this.serializer.apply(value));
        }
    }
}
