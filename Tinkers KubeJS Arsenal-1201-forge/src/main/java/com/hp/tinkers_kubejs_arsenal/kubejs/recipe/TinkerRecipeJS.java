package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.google.gson.JsonElement;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import java.lang.reflect.Array;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;

public class TinkerRecipeJS extends RecipeJS {
    @Override
    public void afterLoaded() {
        super.afterLoaded();
        switch (this.getType().getPath()) {
            case "melting", "ore_melting", "damagable_melting" -> this.applyMeltingDefaults();
            case "casting_basin", "casting_table", "basin_duplication", "table_duplication", "retextured_casting_basin", "retextured_casting_table" ->
                this.applyCastingDefaults();
            case "material_fluid" -> this.applyTemperatureDefault("fluid");
            case "material_melting" -> this.applyTemperatureDefault("result");
            case "alloy" -> {
                Object inputs = this.value("inputs");
                if (inputs == null || Array.getLength(inputs) < 2) {
                    throw new RecipeExceptionJS("Alloy recipe " + this.id + " requires at least 2 inputs");
                }
                this.applyTemperatureDefault("result");
            }
            case "melting_fuel" -> {
                FluidIngredient fluid = this.value("fluid");
                Integer duration = this.value("duration");
                if (fluid != null && fluid != FluidIngredient.EMPTY && (duration == null || duration == 0)) {
                    throw new RecipeExceptionJS("Melting fuel " + this.id + " has a fluid, so 'duration' must be greater than 0");
                }
            }
            default -> {
            }
        }
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
    private <T> T value(String name) {
        RecipeComponentValue<?> holder = this.getAllValueMap().get(name);
        return holder == null ? null : (T) holder.value;
    }

    private JsonElement written(String name) {
        RecipeComponentValue<?> holder = this.getAllValueMap().get(name);
        return holder == null || holder.value == null ? null : this.serializeValue(holder);
    }

    private <T> JsonElement serializeValue(RecipeComponentValue<T> holder) {
        return holder.key.component.write(this, holder.value);
    }
}
