package com.hp.tinkers_kubejs_arsenal.kubejs.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ingredient.TagContext;
import dev.latvian.mods.kubejs.recipe.ItemMatch;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class TagJson {
    private TagJson() {
    }

    public static List<JsonObject> entries(JsonElement json) {
        List<JsonObject> list = new ArrayList<>();
        if (json == null || json.isJsonNull()) {
            return list;
        }
        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                list.addAll(entries(element));
            }
        } else if (json.isJsonObject()) {
            list.add(json.getAsJsonObject());
        }
        return list;
    }

    public static JsonObject fluidJson(String fluid, int amount) {
        JsonObject json = new JsonObject();
        if (fluid.startsWith("#")) {
            json.addProperty("tag", fluid.substring(1));
        } else {
            json.addProperty("fluid", fluid);
        }
        json.addProperty("amount", amount);
        return json;
    }

    public static TagContext context() {
        return TagContext.INSTANCE.getValue();
    }

    public static TagKey<Fluid> fluidTag(String id) {
        return TagKey.create(Registries.FLUID, new ResourceLocation(id));
    }

    public static TagKey<Item> itemTag(String id) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(id));
    }

    public static Fluid fluidById(String id) {
        Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(id));
        return fluid == Fluids.EMPTY ? null : fluid;
    }

    public static String fluidId(JsonObject entry) {
        if (entry.has("fluid")) {
            return entry.get("fluid").getAsString();
        }
        if (entry.has("name")) {
            return entry.get("name").getAsString();
        }
        return null;
    }

    public static boolean fluidMatches(JsonElement json, Fluid fluid) {
        for (JsonObject entry : entries(json)) {
            if (entryMatches(entry, fluid)) {
                return true;
            }
        }
        return false;
    }

    public static boolean entryMatches(JsonObject entry, Fluid fluid) {
        String id = fluidId(entry);
        if (id != null) {
            return BuiltInRegistries.FLUID.getKey(fluid).equals(new ResourceLocation(id));
        }
        if (entry.has("tag")) {
            return context().contains(fluidTag(entry.get("tag").getAsString()), fluid);
        }
        return false;
    }

    public static Fluid firstFluid(JsonElement json) {
        for (JsonObject entry : entries(json)) {
            String id = fluidId(entry);
            if (id != null) {
                Fluid fluid = fluidById(id);
                if (fluid != null) {
                    return fluid;
                }
            } else if (entry.has("tag")) {
                for (Holder<Fluid> holder : context().getTag(fluidTag(entry.get("tag").getAsString()))) {
                    if (holder.value() != Fluids.EMPTY) {
                        return holder.value();
                    }
                }
            }
        }
        return null;
    }

    public static int firstAmount(JsonElement json) {
        for (JsonObject entry : entries(json)) {
            if (entry.has("amount")) {
                return entry.get("amount").getAsInt();
            }
        }
        return 0;
    }

    public static String describe(JsonElement json) {
        return json == null ? "null" : json.toString();
    }

    public static JsonElement replaceFluid(JsonElement json, Fluid match, Fluid replacement) {
        if (json.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement element : json.getAsJsonArray()) {
                array.add(replaceFluid(element, match, replacement));
            }
            return array;
        }
        JsonObject entry = json.getAsJsonObject();
        if (!entryMatches(entry, match)) {
            return entry;
        }
        JsonObject replaced = new JsonObject();
        replaced.addProperty("fluid", BuiltInRegistries.FLUID.getKey(replacement).toString());
        replaced.add("amount", entry.get("amount"));
        return replaced;
    }

    public static boolean itemTagFilled(String id) {
        return !context().isEmpty(itemTag(id));
    }

    public static List<Item> itemTagValues(String id) {
        List<Item> items = new ArrayList<>();
        for (Holder<Item> holder : context().getTag(itemTag(id))) {
            items.add(holder.value());
        }
        return items;
    }

    public static boolean itemOutputMatches(JsonElement json, ItemMatch match) {
        if (json == null || json.isJsonNull()) {
            return false;
        }
        if (json.isJsonPrimitive()) {
            return itemMatches(json.getAsString(), match);
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("item")) {
            return itemMatches(object.get("item").getAsString(), match);
        }
        if (object.has("tag")) {
            for (Item item : itemTagValues(object.get("tag").getAsString())) {
                if (match.contains(new ItemStack(item))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean itemMatches(String id, ItemMatch match) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
        return item != Items.AIR && match.contains(new ItemStack(item));
    }
}
