package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaterialAssetEventJS extends EventJS {
    private final Map<String, MaterialAsset> materials = new LinkedHashMap<>();

    public MaterialAsset create(String id) {
        MaterialId parsed = MaterialId.parse(id);
        if (this.materials.containsKey(parsed.key())) {
            throw new IllegalArgumentException("Duplicate material asset: " + id);
        }
        MaterialAsset asset = new MaterialAsset(parsed.id(), parsed.variant());
        this.materials.put(parsed.key(), asset);
        return asset;
    }

    public Map<String, MaterialAsset> getMaterials() {
        return this.materials;
    }

    private record MaterialId(ResourceLocation id, String variant) {
        private static MaterialId parse(String text) {
            int split = text.indexOf('#');
            String base = split < 0 ? text : text.substring(0, split);
            String variant = split < 0 ? "" : text.substring(split + 1);
            ResourceLocation id = new ResourceLocation(base);
            if (!variant.isEmpty() && !ResourceLocation.isValidPath(variant)) {
                throw new IllegalArgumentException("Invalid material variant: " + text);
            }
            return new MaterialId(id, variant);
        }

        private String key() {
            return this.id + (this.variant.isEmpty() ? "" : "#" + this.variant);
        }
    }

    public static class MaterialAsset {
        private final ResourceLocation id;
        private final String variant;
        private final JsonObject render = new JsonObject();
        private final Map<String, String> names = new LinkedHashMap<>();
        private final Map<String, String> flavors = new LinkedHashMap<>();
        private final Map<String, String> encyclopedia = new LinkedHashMap<>();
        private String textColor;
        private boolean customTextColor;

        private MaterialAsset(ResourceLocation id, String variant) {
            this.id = id;
            this.variant = variant;
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

        public MaterialAsset generator(Map<String, Object> generator) {
            JsonElement value = JsonIO.of(generator);
            if (!value.isJsonObject()) {
                throw new IllegalArgumentException("Material " + this.fullId() + ": generator must be an object");
            }
            JsonObject object = value.getAsJsonObject();
            if (!object.has("transformer") || !object.has("supported_stats")) {
                throw new IllegalArgumentException("Material " + this.fullId() + ": generator requires transformer and supported_stats");
            }
            if (!this.variant.isEmpty() && !object.has("variant")) {
                object.addProperty("variant", true);
            }
            this.render.add("generator", value);
            return this;
        }

        public MaterialAsset generator(String... supportedStats) {
            JsonObject generator = new JsonObject();
            JsonArray stats = new JsonArray();
            for (String stat : supportedStats) {
                ResourceLocation id = new ResourceLocation(stat.contains(":") ? stat : "tconstruct:" + stat);
                stats.add(id.toString());
            }
            generator.add("supported_stats", stats);
            JsonObject transformer = new JsonObject();
            transformer.addProperty("type", "tconstruct:recolor_sprite");
            JsonObject colorMapping = new JsonObject();
            colorMapping.addProperty("type", "tconstruct:grey_to_color");
            colorMapping.add("palette", defaultPalette());
            transformer.add("color_mapping", colorMapping);
            generator.add("transformer", transformer);
            if (!this.variant.isEmpty()) {
                generator.addProperty("variant", true);
            }
            this.render.add("generator", generator);
            return this;
        }

        public MaterialAsset generatorPalette(Map<String, String> palette) {
            JsonElement generator = this.render.get("generator");
            if (generator == null || !generator.isJsonObject() || !generator.getAsJsonObject().has("transformer")) {
                throw new IllegalStateException("Material " + this.fullId() + ": call generator(...) before generatorPalette(...)");
            }
            JsonObject transformer = generator.getAsJsonObject().getAsJsonObject("transformer");
            JsonObject mapping = transformer == null ? null : transformer.getAsJsonObject("color_mapping");
            if (mapping == null) {
                throw new IllegalStateException("Material " + this.fullId() + ": generator transformer has no color_mapping");
            }
            JsonObject values = new JsonObject();
            palette.forEach(values::addProperty);
            mapping.add("palette", values);
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

        public String variant() {
            return this.variant;
        }

        public String outputPath() {
            return this.variant.isEmpty() ? this.id.getPath() : this.id.getPath() + "/" + this.variant;
        }

        public String translationKey() {
            return "material." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.') + (this.variant.isEmpty() ? "" : "." + this.variant);
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

        private String fullId() {
            return this.id + (this.variant.isEmpty() ? "" : "#" + this.variant);
        }

        private JsonObject defaultPalette() {
            String color = this.render.has("color") ? this.render.get("color").getAsString() : "FFFFFFFF";
            if (color.length() == 6) {
                color = "FF" + color;
            }
            int alpha = Integer.parseInt(color.substring(0, 2), 16);
            int red = Integer.parseInt(color.substring(2, 4), 16);
            int green = Integer.parseInt(color.substring(4, 6), 16);
            int blue = Integer.parseInt(color.substring(6, 8), 16);
            JsonObject palette = new JsonObject();
            int[] shades = {0, 22, 40, 55, 70, 85, 100};
            String[] keys = {"000", "063", "102", "140", "178", "216", "255"};
            for (int i = 0; i < shades.length; i++) {
                double factor = shades[i] / 100.0;
                int r = (int) Math.round(red * factor);
                int g = (int) Math.round(green * factor);
                int b = (int) Math.round(blue * factor);
                palette.addProperty(keys[i], String.format(java.util.Locale.ROOT, "%02X%02X%02X%02X", alpha, r, g, b));
            }
            palette.addProperty("000", "00000000");
            return palette;
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
