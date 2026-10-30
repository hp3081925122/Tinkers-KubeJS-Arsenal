package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaterialTagEventJS extends EventJS {
    private final Map<ResourceLocation, MaterialTag> tags = new LinkedHashMap<>();

    public MaterialTag create(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.tags.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate material tag: " + id);
        }
        MaterialTag tag = new MaterialTag();
        this.tags.put(key, tag);
        return tag;
    }

    public Map<ResourceLocation, MaterialTag> getTags() {
        return this.tags;
    }

    public static class MaterialTag {
        private final JsonArray values = new JsonArray();
        private boolean replace;

        private MaterialTag() {}

        public MaterialTag add(String material) {
            return this.add(material, true);
        }

        public MaterialTag add(String material, boolean required) {
            if (material.indexOf('#') >= 0) {
                throw new IllegalArgumentException("Material tags require base material IDs without #variant: " + material);
            }
            new ResourceLocation(material);
            if (required) {
                this.values.add(material);
            } else {
                JsonObject optional = new JsonObject();
                optional.addProperty("id", material);
                optional.addProperty("required", false);
                this.values.add(optional);
            }
            return this;
        }

        public MaterialTag addAll(String... materials) {
            for (String material : materials) {
                this.add(material);
            }
            return this;
        }

        public MaterialTag addOptional(String material) {
            return this.add(material, false);
        }

        public MaterialTag replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public JsonObject json() {
            JsonObject json = new JsonObject();
            if (this.replace) {
                json.addProperty("replace", true);
            }
            json.add("values", this.values);
            return json;
        }
    }
}
