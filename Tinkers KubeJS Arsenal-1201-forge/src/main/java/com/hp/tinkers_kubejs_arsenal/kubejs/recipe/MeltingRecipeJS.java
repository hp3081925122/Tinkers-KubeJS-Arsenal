package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.rhino.Wrapper;

public class MeltingRecipeJS extends TinkerRecipeJS {
    private static final String ORE_MELTING = "ore_melting";
    private static final String DAMAGABLE_MELTING = "damagable_melting";

    public MeltingRecipeJS result(String fluid, int amount) {
        this.read("result", TagJson.fluidJson(fluid, amount));
        return this;
    }

    public MeltingRecipeJS result(String fluid, int amount, Object rateOrUnitSize) {
        JsonObject json = TagJson.fluidJson(fluid, amount);
        if (this.extra(json, rateOrUnitSize, "result") instanceof String rate) {
            this.read("rate", rate);
        }
        this.read("result", json);
        return this;
    }

    public MeltingRecipeJS byproduct(String fluid, int amount) {
        return this.appendByproduct(TagJson.fluidJson(fluid, amount));
    }

    public MeltingRecipeJS byproduct(String fluid, int amount, Object rateOrUnitSize) {
        JsonObject json = TagJson.fluidJson(fluid, amount);
        if (this.extra(json, rateOrUnitSize, "byproduct") instanceof String rate) {
            json.addProperty("rate", rate);
        }
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

    private Object extra(JsonObject json, Object rateOrUnitSize, String method) {
        Object value = Wrapper.unwrapped(rateOrUnitSize);
        if (value instanceof Number unitSize) {
            this.require(DAMAGABLE_MELTING, method + "(fluid, amount, unitSize)");
            json.addProperty("unit_size", unitSize.intValue());
            return null;
        }
        if (value instanceof CharSequence rate) {
            this.require(ORE_MELTING, method + "(fluid, amount, rate)");
            return rate.toString();
        }
        throw new RecipeExceptionJS("." + method + "(fluid, amount, x) expects a rate string on tconstruct:" + ORE_MELTING + " or a unit size number on tconstruct:" + DAMAGABLE_MELTING);
    }

    private void require(String path, String method) {
        if (!path.equals(this.getType().getPath())) {
            throw new RecipeExceptionJS("." + method + " is only available on tconstruct:" + path + ", not " + this.getType());
        }
    }
}
