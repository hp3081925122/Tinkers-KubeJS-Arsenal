package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerComponents;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import java.util.Arrays;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;

public class ModifierRecipeJS extends TinkerRecipeJS {
    public ModifierRecipeJS input(InputItem ingredient) {
        return this.input(ingredient, ingredient.count);
    }

    public ModifierRecipeJS input(InputItem ingredient, int amount) {
        if (this.has("input")) {
            if (amount != 1) {
                throw new RecipeExceptionJS("Recipe " + this.id + " (" + this.getType() + ") takes a single input item; use .amount_per_item(...) and .needed_per_level(...) for amounts");
            }
            this.read("input", ingredient.ingredient);
            return this;
        }
        SizedIngredient[] existing = this.value("inputs");
        SizedIngredient[] inputs = existing == null ? new SizedIngredient[1] : Arrays.copyOf(existing, existing.length + 1);
        inputs[inputs.length - 1] = SizedIngredient.of(ingredient.ingredient, amount);
        this.store("inputs", inputs);
        return this;
    }

    public ModifierRecipeJS slots(String slot, int count) {
        JsonObject json = new JsonObject();
        json.addProperty(slot, count);
        this.read("slots", json);
        return this;
    }

    public ModifierRecipeJS level(int level) {
        if (this.has("levels")) {
            JsonObject entry = new JsonObject();
            entry.addProperty("level", level);
            return this.appendLevel(entry);
        }
        return this.level(level, level);
    }

    public ModifierRecipeJS level(int min, int max) {
        if (this.has("levels")) {
            throw new RecipeExceptionJS("Recipe " + this.id + " (" + this.getType() + ") uses .level(level) or .level(level, slot, count) for each level");
        }
        JsonObject json = new JsonObject();
        json.addProperty("min", min);
        json.addProperty("max", max);
        this.read("level", json);
        return this;
    }

    public ModifierRecipeJS level(int level, String slot, int count) {
        if (!this.has("levels")) {
            throw new RecipeExceptionJS("Recipe " + this.id + " (" + this.getType() + ") has no per-level slots; use .slots(slot, count)");
        }
        JsonObject slots = new JsonObject();
        slots.addProperty(slot, count);
        JsonObject entry = new JsonObject();
        entry.addProperty("level", level);
        entry.add("slots", slots);
        return this.appendLevel(entry);
    }

    public ModifierRecipeJS variant(String value) {
        Object current = this.value("result");
        if (!(current instanceof TinkerComponents.SwappableResult result)) {
            throw new RecipeExceptionJS("Recipe " + this.id + " (" + this.getType() + ") has no variant; .variant(...) is only for tconstruct:swappable_modifier");
        }
        this.read("result", new TinkerComponents.SwappableResult(result.name(), value));
        return this;
    }

    private ModifierRecipeJS appendLevel(JsonObject entry) {
        JsonArray array = new JsonArray();
        JsonElement existing = this.written("levels");
        if (existing != null && existing.isJsonArray()) {
            array.addAll(existing.getAsJsonArray());
        }
        array.add(entry);
        this.read("levels", array);
        return this;
    }
}
