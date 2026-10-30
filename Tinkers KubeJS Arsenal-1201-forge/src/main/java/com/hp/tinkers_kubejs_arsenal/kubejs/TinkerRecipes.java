package com.hp.tinkers_kubejs_arsenal.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.TinkersKubejsArsenal;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TagJson;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.MapJS;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;

public final class TinkerRecipes {
    private static final int BASE_UNIT = 90;
    private static final String NON_SINGULAR_ORE_RATES = "tconstruct:non_singular_ore_rates";
    private static final String ORE_RATE_SPARSE = "forge:ore_rates/sparse";
    private static final String ORE_RATE_DENSE = "forge:ore_rates/dense";

    private TinkerRecipes() {
    }

    public static void metal(RecipesEventJS event, Map<?, ?> options) {
        Map<?, ?> map = MapJS.orEmpty(options);
        Object nameValue = map.get("name");
        Object fluidValue = map.get("fluid");
        if (nameValue == null || String.valueOf(nameValue).isBlank()) {
            throw new RecipeExceptionJS("TinkerRecipes.metal requires 'name'");
        }
        if (fluidValue == null || String.valueOf(fluidValue).isBlank()) {
            throw new RecipeExceptionJS("TinkerRecipes.metal requires 'fluid' (fluid id or '#fluid_tag')");
        }
        Metal metal = new Metal(event, String.valueOf(nameValue), String.valueOf(fluidValue), map.get("temperature"), byproducts(map.get("byproducts")));
        metal.melting("block", "storage_blocks/" + metal.name, 9.0F, 3.0F);
        metal.blockCasting();
        metal.meltingCasting("ingot", 1.0F, 1.0F);
        metal.meltingCasting("nugget", 0.11111111F, 0.33333334F);
        if (!(map.get("ores") instanceof Boolean ores) || ores) {
            metal.oreMelting("raw", "raw_materials/" + metal.name, null, null, 1.0F, 1.5F);
            metal.oreMelting("raw_block", "storage_blocks/raw_" + metal.name, null, null, 9.0F, 6.0F);
            metal.oreMelting("ore_sparse", "ores/" + metal.name, ORE_RATE_SPARSE, true, 1.0F, 1.5F);
            metal.oreMelting("ore_singular", "ores/" + metal.name, NON_SINGULAR_ORE_RATES, false, 2.0F, 2.5F);
            metal.oreMelting("ore_dense", "ores/" + metal.name, ORE_RATE_DENSE, true, 6.0F, 4.5F);
        }
    }

    private static List<Byproduct> byproducts(Object value) {
        List<Byproduct> list = new ArrayList<>();
        if (value == null) {
            return list;
        }
        for (Object entry : ListJS.orSelf(value)) {
            Map<?, ?> map = MapJS.of(entry);
            if (map == null || map.get("fluid") == null) {
                throw new RecipeExceptionJS("Each TinkerRecipes.metal byproduct needs a 'fluid'");
            }
            int amount = map.get("amount") instanceof Number number ? number.intValue() : BASE_UNIT;
            String rate = map.get("rate") == null ? "metal" : String.valueOf(map.get("rate"));
            String requires = map.get("requires") == null ? null : String.valueOf(map.get("requires"));
            list.add(new Byproduct(String.valueOf(map.get("fluid")), amount, rate, requires));
        }
        return list;
    }

    private static JsonObject fluidJson(String fluid, int amount) {
        JsonObject json = new JsonObject();
        if (fluid.startsWith("#")) {
            json.addProperty("tag", fluid.substring(1));
        } else {
            json.addProperty("fluid", fluid);
        }
        json.addProperty("amount", amount);
        return json;
    }

    private static JsonObject tagJson(String tag) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", tag);
        return json;
    }

    private record Byproduct(String fluid, int amount, String rate, String requires) {
    }

    private static final class Metal {
        private final RecipesEventJS event;
        private final String name;
        private final String fluid;
        private final int temperature;
        private final Byproduct byproduct;

        private Metal(RecipesEventJS event, String name, String fluid, Object temperature, List<Byproduct> byproducts) {
            this.event = event;
            this.name = name;
            this.fluid = fluid;
            this.temperature = temperature instanceof Number number ? number.intValue() : this.resolveTemperature();
            Byproduct selected = null;
            for (Byproduct candidate : byproducts) {
                if (candidate.requires() == null || TagJson.itemTagFilled(candidate.requires())) {
                    selected = candidate;
                    break;
                }
            }
            this.byproduct = selected;
        }

        private int resolveTemperature() {
            Fluid resolved = TagJson.firstFluid(fluidJson(this.fluid, BASE_UNIT));
            if (resolved == null) {
                throw new RecipeExceptionJS("TinkerRecipes.metal cannot resolve fluid '" + this.fluid + "' for metal '" + this.name
                    + "'; the fluid tag is empty or unknown, so set 'temperature' explicitly");
            }
            return IMeltingRecipe.getTemperature(resolved);
        }

        private void melting(String variant, String itemTag, float scale, float factor) {
            if (!TagJson.itemTagFilled("forge:" + itemTag)) {
                return;
            }
            JsonObject json = new JsonObject();
            json.addProperty("type", "tconstruct:melting");
            json.add("ingredient", tagJson("forge:" + itemTag));
            json.add("result", fluidJson(this.fluid, (int) (BASE_UNIT * scale)));
            json.addProperty("temperature", this.temperature);
            json.addProperty("time", IMeltingRecipe.calcTime(this.temperature, factor));
            this.add("melting", variant, json);
        }

        private void blockCasting() {
            String itemTag = "forge:storage_blocks/" + this.name;
            if (!TagJson.itemTagFilled(itemTag)) {
                return;
            }
            int amount = BASE_UNIT * 9;
            JsonObject json = new JsonObject();
            json.addProperty("type", "tconstruct:casting_basin");
            json.addProperty("cooling_time", ICastingRecipe.calcCoolingTime(this.temperature, amount));
            json.add("fluid", fluidJson(this.fluid, amount));
            json.add("result", tagJson(itemTag));
            this.add("casting", "block", json);
        }

        private void meltingCasting(String prefix, float scale, float factor) {
            String itemTag = "forge:" + prefix + "s/" + this.name;
            if (!TagJson.itemTagFilled(itemTag)) {
                return;
            }
            this.melting(prefix, prefix + "s/" + this.name, scale, factor);
            int amount = (int) (BASE_UNIT * scale);
            this.tableCasting(prefix + "_gold_cast", itemTag, amount, "tconstruct:casts/multi_use/" + prefix, false);
            this.tableCasting(prefix + "_sand_cast", itemTag, amount, "tconstruct:casts/single_use/" + prefix, true);
        }

        private void tableCasting(String variant, String itemTag, int amount, String castTag, boolean consumed) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "tconstruct:casting_table");
            json.add("cast", tagJson(castTag));
            if (consumed) {
                json.addProperty("cast_consumed", true);
            }
            json.addProperty("cooling_time", ICastingRecipe.calcCoolingTime(this.temperature, amount));
            json.add("fluid", fluidJson(this.fluid, amount));
            json.add("result", tagJson(itemTag));
            this.add("casting", variant, json);
        }

        private void oreMelting(String variant, String baseTag, String rateTag, Boolean intersect, float scale, float factor) {
            String itemTag = "forge:" + baseTag;
            JsonObject ingredient;
            boolean present;
            if (rateTag == null) {
                ingredient = tagJson(itemTag);
                present = TagJson.itemTagFilled(itemTag);
            } else if (intersect) {
                ingredient = new JsonObject();
                ingredient.addProperty("type", "forge:intersection");
                JsonArray children = new JsonArray();
                children.add(tagJson(itemTag));
                children.add(tagJson(rateTag));
                ingredient.add("children", children);
                present = this.anyItem(itemTag, rateTag, true);
            } else {
                ingredient = new JsonObject();
                ingredient.addProperty("type", "forge:difference");
                ingredient.add("base", tagJson(itemTag));
                ingredient.add("subtracted", tagJson(rateTag));
                present = this.anyItem(itemTag, rateTag, false);
            }
            if (!present) {
                return;
            }
            JsonObject json = new JsonObject();
            json.addProperty("type", "tconstruct:ore_melting");
            json.add("ingredient", ingredient);
            json.add("result", fluidJson(this.fluid, (int) (BASE_UNIT * scale)));
            json.addProperty("rate", "metal");
            json.addProperty("temperature", this.temperature);
            json.addProperty("time", IMeltingRecipe.calcTime(this.temperature, factor));
            if (this.byproduct != null) {
                JsonObject output = fluidJson(this.byproduct.fluid(), (int) (this.byproduct.amount() * scale));
                output.addProperty("rate", this.byproduct.rate());
                JsonArray byproducts = new JsonArray();
                byproducts.add(output);
                json.add("byproducts", byproducts);
            }
            this.add("melting", variant, json);
        }

        private boolean anyItem(String baseTag, String filterTag, boolean inFilter) {
            for (Item item : TagJson.itemTagValues(baseTag)) {
                if (TagJson.context().contains(TagJson.itemTag(filterTag), item) == inFilter) {
                    return true;
                }
            }
            return false;
        }

        private void add(String folder, String variant, JsonObject json) {
            ResourceLocation tinkersId = new ResourceLocation("tconstruct", "smeltery/" + folder + "/metal/" + this.name + "/" + variant);
            if (this.event.originalRecipes.containsKey(tinkersId)) {
                ConsoleJS.SERVER.info("TinkerRecipes.metal skipped " + folder + "/" + variant + " for '" + this.name + "': Tinkers' Construct already provides " + tinkersId);
                return;
            }
            ResourceLocation id = new ResourceLocation(TinkersKubejsArsenal.MOD_ID, "metal/" + this.name + "/" + folder + "/" + variant);
            this.event.custom(json).id(id);
        }
    }
}
