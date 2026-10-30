package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaterialAssetEventJS extends EventJS {
    private final Map<ResourceLocation, MaterialAsset> materials = new LinkedHashMap<>();

    public MaterialAsset create(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.materials.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate material asset: " + id);
        }
        MaterialAsset asset = new MaterialAsset(key);
        this.materials.put(key, asset);
        return asset;
    }

    public Map<ResourceLocation, MaterialAsset> getMaterials() {
        return this.materials;
    }

    public static class MaterialAsset {
        private final ResourceLocation id;
        private final JsonObject render = new JsonObject();
        private final Map<String, String> names = new LinkedHashMap<>();
        private final Map<String, String> flavors = new LinkedHashMap<>();
        private final Map<String, String> encyclopedia = new LinkedHashMap<>();
        private String textColor;
        private boolean customTextColor;

        private MaterialAsset(ResourceLocation id) {
            this.id = id;
        }

        public MaterialAsset color(String color) {
            String hex = hex(color);
            this.render.addProperty("color", hex);
            if (!this.customTextColor) {
                this.textColor = "#" + hex.substring(hex.length() - 6);
            }
            return this;
        }

        public MaterialAsset fallbacks(String... fallbacks) {
            JsonArray array = new JsonArray();
            for (String fallback : fallbacks) {
                array.add(fallback);
            }
            this.render.add("fallbacks", array);
            return this;
        }

        public MaterialAsset luminosity(int luminosity) {
            if (luminosity < 0 || luminosity > 15) {
                throw new IllegalArgumentException("Material " + this.id + ": luminosity must be between 0 and 15");
            }
            this.render.addProperty("luminosity", luminosity);
            return this;
        }

        public MaterialAsset texture(String texture) {
            this.render.addProperty("texture", new ResourceLocation(texture).toString());
            return this;
        }

        public MaterialAsset textColor(String color) {
            String hex = hex(color);
            if (hex.length() != 6) {
                throw new IllegalArgumentException("Material " + this.id + ": text color must be six hexadecimal digits");
            }
            this.textColor = "#" + hex;
            this.customTextColor = true;
            return this;
        }

        public MaterialAsset name(String text) {
            return this.name("en_us", text);
        }

        public MaterialAsset name(String lang, String text) {
            this.names.put(lang, text);
            return this;
        }

        public MaterialAsset flavor(String text) {
            return this.flavor("en_us", text);
        }

        public MaterialAsset flavor(String lang, String text) {
            this.flavors.put(lang, text);
            return this;
        }

        public MaterialAsset encyclopedia(String text) {
            return this.encyclopedia("en_us", text);
        }

        public MaterialAsset encyclopedia(String lang, String text) {
            this.encyclopedia.put(lang, text);
            return this;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public JsonObject render() {
            return this.render;
        }

        public Map<String, String> names() {
            return this.names;
        }

        public Map<String, String> flavors() {
            return this.flavors;
        }

        public Map<String, String> encyclopedia() {
            return this.encyclopedia;
        }

        public String textColor() {
            return this.textColor;
        }

        private String hex(String color) {
            String text = color.startsWith("#") ? color.substring(1) : color;
            if (!text.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
                throw new IllegalArgumentException("Material " + this.id + ": color must be six or eight hexadecimal digits, got '" + color + "'");
            }
            return text.toUpperCase(java.util.Locale.ROOT);
        }
    }
}
