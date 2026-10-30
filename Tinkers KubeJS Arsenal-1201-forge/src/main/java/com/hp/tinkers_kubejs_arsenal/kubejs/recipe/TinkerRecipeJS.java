package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.google.gson.JsonElement;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerComponents;
import com.hp.tinkers_kubejs_arsenal.kubejs.schema.TinkerRecipeSchemas;
import java.lang.reflect.Array;
import java.util.Map;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

public class TinkerRecipeJS extends RecipeJS {
    private static final Map<String, String> HINTS = Map.of(
        "inputs", "input(...)",
        "levels", "level(...)",
        "rate", "result(fluid, amount, rate)"
    );

    @Override
    public void serialize() {
        for (String name : TinkerRecipeSchemas.chainedKeys(this.type.schemaType.schema)) {
            if (this.value(name) == null) {
                throw new RecipeExceptionJS("Recipe " + this.id + " (" + this.getType() + ") is missing '" + name + "'; set it with ." + HINTS.getOrDefault(name, name + "(...)"));
            }
        }
        switch (this.getType().getPath()) {
            case "alloy" -> {
                Object inputs = this.value("inputs");
                if (inputs == null || Array.getLength(inputs) < 2) {
                    throw new RecipeExceptionJS("Alloy recipe " + this.id + " requires at least 2 inputs");
                }
            }
            case "melting_fuel" -> {
                FluidIngredient fluid = this.value("fluid");
                Integer duration = this.value("duration");
                if (fluid != null && fluid != FluidIngredient.EMPTY && (duration == null || duration == 0)) {
                    throw new RecipeExceptionJS("Melting fuel " + this.id + " has a fluid, so 'duration' must be greater than 0");
                }
            }
            case "item_part_builder" -> {
                MaterialVariantId material = this.value("material");
                Integer cost = this.value("cost");
                if (material != null && !MaterialId.UNKNOWN.equals(material) && (cost == null || cost <= 0)) {
                    throw new RecipeExceptionJS("Item part builder " + this.id + " requires a positive cost when material is defined");
                }
            }
            case "modifier" -> requireArray("inputs", 1);
            case "multilevel_modifier" -> {
                requireArray("levels", 1);
                Object inputs = this.value("inputs");
                Boolean allowCrystal = this.value("allow_crystal");
                if ((inputs == null || Array.getLength(inputs) == 0) && !Boolean.TRUE.equals(allowCrystal)) {
                    throw new RecipeExceptionJS("Multilevel modifier " + this.id + " requires inputs when allow_crystal is false");
                }
            }
            case "multilevel_incremental_modifier" -> requireArray("levels", 1);
            case "modifier_sorting" -> requireArray("inputs", 1, 1);
            case "fixed_material_swapping", "part_swapping_override" -> {
                int[] indices = this.value("index");
                if (indices == null || indices.length < 1 || indices.length > 10) {
                    throw new RecipeExceptionJS("Recipe " + this.id + " requires between 1 and 10 indices");
                }
                for (int index : indices) {
                    if (index < 0) throw new RecipeExceptionJS("Recipe " + this.id + " has a negative index");
                }
            }
            case "swappable_modifier" -> {
                requireArray("inputs", 1);
                TinkerComponents.SwappableResult result = this.value("result");
                if (result == null || result.value() == null) {
                    throw new RecipeExceptionJS("Swappable modifier " + this.id + " requires .variant(value)");
                }
            }
            default -> {
            }
        }
        switch (this.getType().getPath()) {
            case "melting", "ore_melting", "damagable_melting" -> this.applyMeltingDefaults();
            case "casting_basin", "casting_table", "basin_duplication", "table_duplication", "retextured_casting_basin", "retextured_casting_table" ->
                this.applyCastingDefaults();
            case "material_fluid" -> this.applyTemperatureDefault("fluid");
            case "material_melting", "alloy" -> this.applyTemperatureDefault("result");
            default -> {
            }
        }
        super.serialize();
    }

    private void applyMeltingDefaults() {
        Integer temperature = this.value("temperature");
        boolean missingTime = this.value("time") == null;
        if (temperature != null && !missingTime) {
            return;
        }
        JsonElement result = this.written("result");
        if (temperature == null) {
            temperature = IMeltingRecipe.getTemperature(this.resolveFluid(result, "temperature"));
            this.set("temperature", temperature);
        }
        if (missingTime) {
            this.set("time", IMeltingRecipe.calcTimeForAmount(temperature, TagJson.firstAmount(result)));
        }
    }

    private void applyCastingDefaults() {
        if (this.value("cooling_time") != null) {
            return;
        }
        JsonElement fluid = this.written("fluid");
        int temperature = IMeltingRecipe.getTemperature(this.resolveFluid(fluid, "cooling_time"));
        this.set("cooling_time", ICastingRecipe.calcCoolingTime(temperature, TagJson.firstAmount(fluid)));
    }

    private void applyTemperatureDefault(String fluidKey) {
        if (this.value("temperature") != null) {
            return;
        }
        this.set("temperature", IMeltingRecipe.getTemperature(this.resolveFluid(this.written(fluidKey), "temperature")));
    }

    private Fluid resolveFluid(JsonElement json, String missingKey) {
        Fluid fluid = TagJson.firstFluid(json);
        if (fluid == null) {
            throw new RecipeExceptionJS("Recipe " + this.id + " (" + this.getType() + ") cannot resolve a fluid from " + TagJson.describe(json)
                + " to compute '" + missingKey + "'; the fluid tag is empty or unknown, so set '" + missingKey + "' explicitly");
        }
        return fluid;
    }

    @SuppressWarnings("unchecked")
    protected <T> T value(String name) {
        RecipeComponentValue<?> holder = this.getAllValueMap().get(name);
        return holder == null ? null : (T) holder.value;
    }

    private void requireArray(String name, int min) {
        requireArray(name, min, Integer.MAX_VALUE);
    }

    private void requireArray(String name, int min, int max) {
        Object value = this.value(name);
        int length = value == null ? 0 : Array.getLength(value);
        if (length < min || length > max) {
            throw new RecipeExceptionJS("Recipe " + this.id + " field '" + name + "' must contain between " + min + " and " + max + " entries");
        }
    }

    protected JsonElement written(String name) {
        RecipeComponentValue<?> holder = this.getAllValueMap().get(name);
        return holder == null || holder.value == null ? null : this.serializeValue(holder);
    }

    private <T> JsonElement serializeValue(RecipeComponentValue<T> holder) {
        return holder.key.component.write(this, holder.value);
    }

    protected boolean has(String name) {
        return this.getAllValueMap().containsKey(name);
    }

    protected void read(String name, Object from) {
        RecipeComponentValue<?> holder = this.getAllValueMap().get(name);
        if (holder == null) {
            throw new RecipeExceptionJS("Recipe type " + this.getType() + " has no '" + name + "' field");
        }
        this.readValue(holder, from);
    }

    @SuppressWarnings("unchecked")
    protected <T> void store(String name, T value) {
        RecipeComponentValue<T> holder = (RecipeComponentValue<T>) this.getAllValueMap().get(name);
        this.setValue(holder.key, value);
    }

    private <T> void readValue(RecipeComponentValue<T> holder, Object from) {
        this.setValue(holder.key, holder.key.component.read(this, from));
    }
}
