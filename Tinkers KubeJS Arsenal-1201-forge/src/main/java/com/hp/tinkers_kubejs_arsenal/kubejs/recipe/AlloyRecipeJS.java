package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;

public class AlloyRecipeJS extends TinkerRecipeJS {
    public AlloyRecipeJS input(String fluid, int amount) {
        return this.appendInput(TagJson.fluidJson(fluid, amount));
    }

    public AlloyRecipeJS catalyst(String fluid, int amount) {
        JsonObject json = new JsonObject();
        json.add("match", TagJson.fluidJson(fluid, amount));
        json.addProperty("catalyst", true);
        return this.appendInput(json);
    }

    public AlloyRecipeJS result(String fluid, int amount) {
        this.read("result", TagJson.fluidJson(fluid, amount));
        return this;
    }

    private AlloyRecipeJS appendInput(JsonObject json) {
        JsonArray array = new JsonArray();
        JsonElement existing = this.written("inputs");
        if (existing != null && existing.isJsonArray()) {
            array.addAll(existing.getAsJsonArray());
        }
        array.add(json);
        this.read("inputs", array);
        return this;
    }
}
