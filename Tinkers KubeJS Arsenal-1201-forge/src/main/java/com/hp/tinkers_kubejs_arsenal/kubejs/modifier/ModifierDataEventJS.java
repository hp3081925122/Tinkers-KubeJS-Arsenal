package com.hp.tinkers_kubejs_arsenal.kubejs.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ModifierDataEventJS extends EventJS {
    private static final Set<String> LEVEL_DISPLAYS = Set.of("default", "single_level", "no_levels", "pluses");
    private static final Set<String> TOOLTIP_DISPLAYS = Set.of("always", "tinker_station", "never");
    private static final Set<String> STAT_OPERATIONS = Set.of("add", "percent", "multiply_base", "multiply_conditional", "multiply_all");

    private final Map<ResourceLocation, ModifierDefinition> modifiers = new LinkedHashMap<>();

    public ModifierDefinition create(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.modifiers.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate modifier: " + id);
        }
        ModifierDefinition modifier = new ModifierDefinition(key);
        this.modifiers.put(key, modifier);
        return modifier;
    }

    public Map<ResourceLocation, ModifierDefinition> getModifiers() {
        return this.modifiers;
    }

    public static class ModifierDefinition {
        private final ResourceLocation id;
        private final JsonObject json = new JsonObject();
        private final JsonArray modules = new JsonArray();
        private final Map<String, String> names = new LinkedHashMap<>();
        private final Map<String, String> flavors = new LinkedHashMap<>();
        private final Map<String, String> descriptions = new LinkedHashMap<>();
        private final List<String> icons = new ArrayList<>();
        private String textColor;
        private boolean invalid;

        private ModifierDefinition(ResourceLocation id) {
            this.id = id;
            this.json.addProperty("level_display", "tconstruct:default");
            this.json.addProperty("tooltip_display", "always");
        }

        public ModifierDefinition levelDisplay(String levelDisplay) {
            String value = levelDisplay.toLowerCase(Locale.ROOT);
            if (value.startsWith("tconstruct:")) {
                value = value.substring("tconstruct:".length());
            }
            if (!LEVEL_DISPLAYS.contains(value)) {
                throw this.fail("Invalid level display '" + levelDisplay + "', expected default, single_level, no_levels or pluses");
            }
            this.json.addProperty("level_display", "tconstruct:" + value);
            return this;
        }

        public ModifierDefinition tooltipDisplay(String tooltipDisplay) {
            String value = tooltipDisplay.toLowerCase(Locale.ROOT);
            if (!TOOLTIP_DISPLAYS.contains(value)) {
                throw this.fail("Invalid tooltip display '" + tooltipDisplay + "', expected always, tinker_station or never");
            }
            this.json.addProperty("tooltip_display", value);
            return this;
        }

        public ModifierDefinition priority(int priority) {
            this.json.addProperty("priority", priority);
            return this;
        }

        public ModifierDefinition module(String moduleType, Map<String, Object> values) {
            if (ResourceLocation.tryParse(moduleType) == null) {
                throw this.fail("Invalid modifier module type '" + moduleType + "'");
            }
            JsonObject module = new JsonObject();
            module.addProperty("type", moduleType);
            values.forEach((key, value) -> {
                if (!"type".equals(key)) {
                    module.add(key, JsonIO.of(value));
                }
            });
            this.modules.add(module);
            return this;
        }

        public ModifierDefinition statBoost(String toolStat, String statOperation, double eachLevel) {
            String value = statOperation.toLowerCase(Locale.ROOT);
            if (!STAT_OPERATIONS.contains(value)) {
                throw this.fail("Invalid stat operation '" + statOperation + "', expected add, percent, multiply_base, multiply_conditional or multiply_all");
            }
            JsonObject module = new JsonObject();
            module.addProperty("type", "tconstruct:stat_boost");
            module.addProperty("stat", new ResourceLocation(toolStat).toString());
            module.addProperty("operation", value);
            module.addProperty("each_level", eachLevel);
            this.modules.add(module);
            return this;
        }

        public ModifierDefinition color(String color) {
            String hex = color.startsWith("#") ? color.substring(1) : color;
            if (!hex.matches("[0-9A-Fa-f]{6}")) {
                throw this.fail("Invalid modifier color '" + color + "', expected 6 hex digits such as FF5500");
            }
            this.textColor = "#" + hex.toUpperCase(Locale.ROOT);
            return this;
        }

        public ModifierDefinition icon(String... sprites) {
            if (sprites.length == 0) {
                throw this.fail("Modifier icon requires at least one sprite such as minecraft:item/redstone");
            }
            this.icons.clear();
            for (String sprite : sprites) {
                ResourceLocation id = ResourceLocation.tryParse(sprite);
                if (id == null) {
                    throw this.fail("Invalid modifier icon sprite '" + sprite + "'");
                }
                this.icons.add(id.toString());
            }
            return this;
        }

        public ModifierDefinition name(String text) {
            return this.name("en_us", text);
        }

        public ModifierDefinition name(String lang, String text) {
            this.names.put(lang, text);
            return this;
        }

        public ModifierDefinition flavor(String text) {
            return this.flavor("en_us", text);
        }

        public ModifierDefinition flavor(String lang, String text) {
            this.flavors.put(lang, text);
            return this;
        }

        public ModifierDefinition description(String text) {
            return this.description("en_us", text);
        }

        public ModifierDefinition description(String lang, String text) {
            this.descriptions.put(lang, text);
            return this;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public boolean isInvalid() {
            return this.invalid;
        }

        public JsonObject json() {
            JsonObject json = this.json.deepCopy();
            JsonArray modules = this.modules.deepCopy();
            JsonObject events = new JsonObject();
            events.addProperty("type", ArsenalEventsModule.ID.toString());
            modules.add(events);
            JsonObject damage = new JsonObject();
            damage.addProperty("type", ArsenalEventsModule.DAMAGE_ID.toString());
            modules.add(damage);
            json.add("modules", modules);
            return json;
        }

        public String translationKey() {
            return "modifier." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.');
        }

        public Map<String, String> names() {
            return this.names;
        }

        public Map<String, String> flavors() {
            return this.flavors;
        }

        public Map<String, String> descriptions() {
            return this.descriptions;
        }

        public List<String> icons() {
            return this.icons;
        }

        @Nullable
        public String textColor() {
            return this.textColor;
        }

        private IllegalArgumentException fail(String message) {
            this.invalid = true;
            return new IllegalArgumentException("Modifier " + this.id + ": " + message);
        }
    }
}
