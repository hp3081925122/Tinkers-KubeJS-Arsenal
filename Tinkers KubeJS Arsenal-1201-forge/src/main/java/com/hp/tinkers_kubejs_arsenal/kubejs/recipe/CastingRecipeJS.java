package com.hp.tinkers_kubejs_arsenal.kubejs.recipe;

import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;

public class CastingRecipeJS extends TinkerRecipeJS {
    public CastingRecipeJS fluid(String fluid, int amount) {
        this.read("fluid", TagJson.fluidJson(fluid, amount));
        return this;
    }
}
