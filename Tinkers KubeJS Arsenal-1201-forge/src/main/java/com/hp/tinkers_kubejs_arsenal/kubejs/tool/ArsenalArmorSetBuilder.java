package com.hp.tinkers_kubejs_arsenal.kubejs.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ArsenalArmorSetBuilder {
    public static final Map<ResourceLocation, ArsenalArmorSetBuilder> SETS = new LinkedHashMap<>();

    private final ResourceLocation id;
    private final JsonArray layers = new JsonArray();
    private boolean dyeable;
    private boolean trim;

    public ArsenalArmorSetBuilder(ResourceLocation id) {
        this.id = id;
        SETS.put(id, this);
    }

    public ArsenalArmorSetBuilder materialLayer(String name, int index) {
        JsonObject layer = new JsonObject();
        layer.addProperty("type", "tconstruct:material");
        layer.addProperty("index", index);
        layer.addProperty("prefix", this.id.getNamespace() + ":" + this.id.getPath() + "/" + name + "_");
        this.layers.add(layer);
        return this;
    }

    public ArsenalArmorSetBuilder fixedLayer(String name) {
        JsonObject layer = new JsonObject();
        layer.addProperty("type", "tconstruct:fixed");
        layer.addProperty("prefix", this.id.getNamespace() + ":" + this.id.getPath() + "/" + name + "_");
        this.layers.add(layer);
        return this;
    }

    public ArsenalArmorSetBuilder dyedLayer(String name) {
        JsonObject layer = new JsonObject();
        layer.addProperty("type", "tconstruct:dyed");
        layer.addProperty("prefix", this.id.getNamespace() + ":" + this.id.getPath() + "/" + name + "_");
        this.layers.add(layer);
        this.dyeable = true;
        return this;
    }

    public ArsenalArmorSetBuilder trim() {
        this.layers.add("tconstruct:trim");
        this.trim = true;
        return this;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public boolean dyeable() {
        return this.dyeable;
    }

    public boolean hasTrim() {
        return this.trim;
    }

    public JsonObject modelJson() {
        JsonObject json = new JsonObject();
        json.add("layers", this.layers.deepCopy());
        return json;
    }

    public static final class Event extends EventJS {
        public ArsenalArmorSetBuilder create(String id) {
            ResourceLocation key = new ResourceLocation(id);
            if (SETS.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate armor set: " + id);
            }
            return new ArsenalArmorSetBuilder(key);
        }
    }
}
