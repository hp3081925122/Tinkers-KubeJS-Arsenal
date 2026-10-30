package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;

public class MeltingRecipeJS extends TinkerRecipeJS {
    private static final String ORE_MELTING = "ore_melting";
    private static final String DAMAGABLE_MELTING = "damagable_melting";

    public MeltingRecipeJS result(String fluid, int amount) {
        this.read("result", TagJson.fluidJson(fluid, amount));
        return this;
    }

    public MeltingRecipeJS result(String fluid, int amount, String rate) {
        this.require(ORE_MELTING, "result(fluid, amount, rate)");
        this.read("result", TagJson.fluidJson(fluid, amount));
        this.read("rate", rate);
        return this;
    }

    public MeltingRecipeJS result(String fluid, int amount, int unitSize) {
        this.require(DAMAGABLE_MELTING, "result(fluid, amount, unitSize)");
        JsonObject json = TagJson.fluidJson(fluid, amount);
        json.addProperty("unit_size", unitSize);
        this.read("result", json);
        return this;
    }

    public MeltingRecipeJS byproduct(String fluid, int amount) {
        return this.appendByproduct(TagJson.fluidJson(fluid, amount));
    }

    public MeltingRecipeJS byproduct(String fluid, int amount, String rate) {
        this.require(ORE_MELTING, "byproduct(fluid, amount, rate)");
        JsonObject json = TagJson.fluidJson(fluid, amount);
        json.addProperty("rate", rate);
        return this.appendByproduct(json);
    }

    public MeltingRecipeJS byproduct(String fluid, int amount, int unitSize) {
        this.require(DAMAGABLE_MELTING, "byproduct(fluid, amount, unitSize)");
        JsonObject json = TagJson.fluidJson(fluid, amount);
        json.addProperty("unit_size", unitSize);
        return this.appendByproduct(json);
    }

    private MeltingRecipeJS appendByproduct(JsonObject json) {
        JsonArray array = new JsonArray();
        JsonElement existing = this.written("byproducts");
        if (existing != null && existing.isJsonArray()) {
            array.addAll(existing.getAsJsonArray());
        }
        array.add(json);
        this.read("byproducts", array);
        return this;
    }

    private void require(String path, String method) {
        if (!path.equals(this.getType().getPath())) {
            throw new RecipeExceptionJS("." + method + " is only available on tconstruct:" + path + ", not " + this.getType());
        }
    }
}
