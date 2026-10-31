package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import java.util.Set;

public class MaterialDataEventJS extends EventJS {
    private static final String INT_MIN_ONE = "int>=1";
    private static final String INT_MIN_ZERO = "int>=0";
    private static final String FLOAT = "number";
    private static final String FLOAT_MIN_ZERO = "number>=0";
    private static final String TIER = "tier";
    private static final Set<String> BUILT_IN_STATS = Set.of("head", "handle", "grip", "limb", "plating_helmet", "plating_chestplate", "plating_leggings", "plating_boots", "plating_shield",
        "skull", "slime", "ribcage", "shell", "laces", "binding", "bowstring", "maille", "shield_core", "repair_kit", "cuirass", "arrow_head", "arrow_shaft", "fletching");
    private static final Set<String> RARITIES = Set.of("common", "uncommon", "rare", "epic");

    private final Map<ResourceLocation, MaterialDefinition> materials = new LinkedHashMap<>();
    private final Map<ResourceLocation, MaterialDefinition> extensions = new LinkedHashMap<>();
    private final Map<MaterialStatsId, StatFormulaDefinition> formulas = new LinkedHashMap<>();
    private final Map<ResourceLocation, PartFormulaDefinition> partFormulas = new LinkedHashMap<>();

    public MaterialDefinition create(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.materials.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate material: " + id);
        }
        MaterialDefinition material = new MaterialDefinition(key, false);
        this.materials.put(key, material);
        return material;
    }

    public MaterialDefinition extend(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.extensions.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate material extension: " + id);
        }
        MaterialDefinition material = new MaterialDefinition(key, true);
        this.extensions.put(key, material);
        return material;
    }

    public StatFormulaDefinition statFormula(String formulaStat) {
        MaterialStatsId key = new MaterialStatsId(formulaStat.contains(":") ? formulaStat : "tconstruct:" + formulaStat);
        if (this.formulas.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate stat formula: " + key);
        }
        StatFormulaDefinition formula = new StatFormulaDefinition(key);
        this.formulas.put(key, formula);
        return formula;
    }

    public Map<MaterialStatsId, StatFormulaDefinition> getFormulas() {
        return this.formulas;
    }

    public PartFormulaDefinition partFormula(String partItem) {
        ResourceLocation key = new ResourceLocation(partItem);
        if (this.partFormulas.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate part formula: " + key);
        }
        PartFormulaDefinition formula = new PartFormulaDefinition(key);
        this.partFormulas.put(key, formula);
        return formula;
    }

    public Map<ResourceLocation, PartFormulaDefinition> getPartFormulas() {
        return this.partFormulas;
    }

    public Map<ResourceLocation, MaterialDefinition> getExtensions() {
        return this.extensions;
    }

    public Map<ResourceLocation, MaterialDefinition> getMaterials() {
        return this.materials;
    }

    public static class MaterialDefinition {
        private final ResourceLocation id;
        private final JsonObject definition = new JsonObject();
        private final JsonObject stats = new JsonObject();
        private final JsonArray defaultTraits = new JsonArray();
        private final JsonObject perStat = new JsonObject();
        private final JsonObject render = new JsonObject();
        private JsonObject properties;
        private final JsonArray excludedFormulas = new JsonArray();
        private final Map<String, String> names = new LinkedHashMap<>();
        private final Map<String, String> flavors = new LinkedHashMap<>();
        private final Map<String, String> encyclopedia = new LinkedHashMap<>();
        private String textColor;
        private boolean customTextColor;
        private boolean traitsUsed;
        private boolean invalid;
        private final boolean extension;

        private MaterialDefinition(ResourceLocation id, boolean extension) {
            this.id = id;
            this.extension = extension;
            this.definition.addProperty("craftable", false);
            this.definition.addProperty("tier", 0);
            this.definition.addProperty("sortOrder", 100);
            this.definition.addProperty("hidden", false);
        }

        public MaterialDefinition tier(int tier) {
            this.requireFull("tier");
            this.definition.addProperty("tier", tier);
            return this;
        }

        public MaterialDefinition sortOrder(int sortOrder) {
            this.requireFull("sortOrder");
            this.definition.addProperty("sortOrder", sortOrder);
            return this;
        }

        public MaterialDefinition craftable(boolean craftable) {
            this.requireFull("craftable");
            this.definition.addProperty("craftable", craftable);
            return this;
        }

        public MaterialDefinition hidden(boolean hidden) {
            this.requireFull("hidden");
            this.definition.addProperty("hidden", hidden);
            return this;
        }

        public MaterialDefinition rarity(String rarity) {
            this.requireFull("rarity");
            String value = rarity.toLowerCase(Locale.ROOT);
            if (!RARITIES.contains(value)) {
                throw this.fail("Invalid material rarity '" + rarity + "', expected common, uncommon, rare or epic");
            }
            this.definition.addProperty("rarity", value.toUpperCase(Locale.ROOT));
            return this;
        }

        public MaterialDefinition head(double durability, double miningSpeed, String miningTier, double meleeAttack) {
            return this.stat("head", values("durability", durability, "mining_speed", miningSpeed, "mining_tier", miningTier, "melee_attack", meleeAttack), Map.of("durability", INT_MIN_ONE, "mining_speed", FLOAT, "mining_tier", TIER, "melee_attack", FLOAT), "durability");
        }

        public MaterialDefinition handle(double durability, double miningSpeed, double meleeSpeed, double meleeDamage) {
            return this.stat("handle", values("durability", durability, "mining_speed", miningSpeed, "melee_speed", meleeSpeed, "melee_damage", meleeDamage), Map.of("durability", FLOAT, "mining_speed", FLOAT, "melee_speed", FLOAT, "melee_damage", FLOAT));
        }

        public MaterialDefinition grip(double durability, double accuracy, double meleeDamage) {
            return this.stat("grip", values("durability", durability, "accuracy", accuracy, "melee_damage", meleeDamage), Map.of("durability", FLOAT, "accuracy", FLOAT, "melee_damage", FLOAT_MIN_ZERO));
        }

        public MaterialDefinition limb(double durability, double drawSpeed, double velocity, double accuracy) {
            return this.stat("limb", values("durability", durability, "draw_speed", drawSpeed, "velocity", velocity, "accuracy", accuracy), Map.of("durability", INT_MIN_ONE, "draw_speed", FLOAT, "velocity", FLOAT, "accuracy", FLOAT), "durability");
        }

        public MaterialDefinition platingHelmet(double durability, double armor, double toughness, double knockbackResistance) {
            return this.plating("plating_helmet", durability, armor, toughness, knockbackResistance);
        }

        public MaterialDefinition platingChestplate(double durability, double armor, double toughness, double knockbackResistance) {
            return this.plating("plating_chestplate", durability, armor, toughness, knockbackResistance);
        }

        public MaterialDefinition platingLeggings(double durability, double armor, double toughness, double knockbackResistance) {
            return this.plating("plating_leggings", durability, armor, toughness, knockbackResistance);
        }

        public MaterialDefinition platingBoots(double durability, double armor, double toughness, double knockbackResistance) {
            return this.plating("plating_boots", durability, armor, toughness, knockbackResistance);
        }

        public MaterialDefinition platingShield(double durability, double toughness, double knockbackResistance) {
            return this.stat("plating_shield", values("durability", durability, "toughness", toughness, "knockback_resistance", knockbackResistance), Map.of("durability", INT_MIN_ONE, "toughness", FLOAT, "knockback_resistance", FLOAT), "durability");
        }

        public MaterialDefinition skull(double durability, double armor) {
            return this.stat("skull", values("durability", durability, "armor", armor), Map.of("durability", INT_MIN_ONE, "armor", INT_MIN_ZERO), "durability");
        }

        public MaterialDefinition slime(double durability, double overslimeCapacity) {
            return this.stat("slime", values("durability", durability, "overslime_capacity", overslimeCapacity), Map.of("durability", INT_MIN_ONE, "overslime_capacity", INT_MIN_ZERO), "durability", "overslime_capacity");
        }

        public MaterialDefinition ribcage(double repairAmount) {
            return this.repair("ribcage", repairAmount);
        }

        public MaterialDefinition shell(double repairAmount) {
            return this.repair("shell", repairAmount);
        }

        public MaterialDefinition laces(double repairAmount) {
            return this.repair("laces", repairAmount);
        }

        public MaterialDefinition binding() {
            return this.emptyStat("binding");
        }

        public MaterialDefinition bowstring() {
            return this.emptyStat("bowstring");
        }

        public MaterialDefinition maille() {
            return this.emptyStat("maille");
        }

        public MaterialDefinition shieldCore() {
            return this.emptyStat("shield_core");
        }

        public MaterialDefinition repairKit() {
            return this.emptyStat("repair_kit");
        }

        public MaterialDefinition cuirass() {
            return this.emptyStat("cuirass");
        }

        public MaterialDefinition arrowHead() {
            return this.emptyStat("arrow_head");
        }

        public MaterialDefinition arrowShaft() {
            return this.emptyStat("arrow_shaft");
        }

        public MaterialDefinition fletching() {
            return this.emptyStat("fletching");
        }

        public MaterialDefinition customStat(String statType, double... values) {
            MaterialStatsId id = new MaterialStatsId(statType);
            StatTypeEventJS.StatTypeDefinition type = StatTypeEventJS.TYPES.get(id);
            if (type == null) {
                throw this.fail("Stat type " + id + " is not registered in ArsenalMaterialEvents.statTypes, registered: " + StatTypeEventJS.TYPES.keySet());
            }
            List<StatTypeEventJS.Field> fields = type.fields();
            if (values.length != fields.size()) {
                throw this.fail("Stat type " + id + " needs " + fields.size() + " values in order " + fields.stream().map(StatTypeEventJS.Field::name).toList() + ", got " + values.length);
            }
            JsonObject json = new JsonObject();
            for (int i = 0; i < values.length; i++) {
                json.addProperty(fields.get(i).name(), values[i]);
            }
            this.stats.add(id.toString(), json);
            return this;
        }

        public MaterialDefinition stat(String statId, Map<String, Object> values) {
            ResourceLocation stat = new ResourceLocation(statId);
            if ((!statId.contains(":") || "tconstruct".equals(stat.getNamespace())) && BUILT_IN_STATS.contains(stat.getPath())) {
                throw this.fail("Material stat " + stat + " is built in; use its dedicated method instead of .stat(...)");
            }
            JsonObject json = new JsonObject();
            values.forEach((key, value) -> json.add(key, dev.latvian.mods.kubejs.util.JsonIO.of(value)));
            this.stats.add(stat.toString(), json);
            return this;
        }

        public MaterialDefinition properties(double hardness, double density, double toughness) {
            return this.properties(values("hardness", hardness, "density", density, "toughness", toughness));
        }

        public MaterialDefinition properties(Map<String, Object> properties) {
            JsonObject json = this.properties == null ? new JsonObject() : this.properties;
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if ("exclude".equals(entry.getKey())) {
                    throw this.fail("'exclude' is reserved; use .excludeFormula(...) instead");
                }
                if (!(entry.getValue() instanceof Number number)) {
                    throw this.fail("Material property '" + entry.getKey() + "' must be a number");
                }
                json.addProperty(entry.getKey(), number.doubleValue());
            }
            this.properties = json;
            return this;
        }

        public MaterialDefinition excludeFormula(String... excludedStats) {
            for (String stat : excludedStats) {
                this.excludedFormulas.add(new ResourceLocation(stat.contains(":") ? stat : "tconstruct:" + stat).toString());
            }
            return this;
        }

        public boolean hasProperties() {
            return this.properties != null;
        }

        public JsonObject propertiesJson() {
            JsonObject json = this.properties.deepCopy();
            if (!this.excludedFormulas.isEmpty()) {
                json.add("exclude", this.excludedFormulas);
            }
            return json;
        }

        public MaterialDefinition trait(String modifier) {
            this.requireFull("trait");
            return this.trait(modifier, 1);
        }

        public MaterialDefinition trait(String modifier, int level) {
            this.requireFull("trait");
            this.defaultTraits.add(traitJson(modifier, level));
            this.traitsUsed = true;
            return this;
        }

        public MaterialDefinition statTrait(String stat, String modifier, int level) {
            String key = new ResourceLocation(stat).toString();
            JsonArray values = this.perStat.has(key) ? this.perStat.getAsJsonArray(key) : new JsonArray();
            values.add(traitJson(modifier, level));
            this.perStat.add(key, values);
            this.traitsUsed = true;
            return this;
        }

        public MaterialDefinition color(String color) {
            this.requireFull("color");
            String hex = this.hex(color);
            this.render.addProperty("color", hex);
            if (!this.customTextColor) {
                this.textColor = "#" + hex.substring(hex.length() - 6);
            }
            return this;
        }

        public MaterialDefinition fallbacks(String... fallbacks) {
            this.requireFull("fallbacks");
            JsonArray values = new JsonArray();
            for (String fallback : fallbacks) {
                values.add(fallback);
            }
            this.render.add("fallbacks", values);
            return this;
        }

        public MaterialDefinition luminosity(int luminosity) {
            this.requireFull("luminosity");
            if (luminosity < 0 || luminosity > 15) {
                throw this.fail("Material luminosity must be between 0 and 15");
            }
            this.render.addProperty("luminosity", luminosity);
            return this;
        }

        public MaterialDefinition texture(String texture) {
            this.requireFull("texture");
            this.render.addProperty("texture", new ResourceLocation(texture).toString());
            return this;
        }

        public MaterialDefinition generator(Map<String, Object> generator) {
            this.requireFull("generator");
            JsonElement value = JsonIO.of(generator);
            if (!value.isJsonObject()) {
                throw this.fail("generator must be an object");
            }
            JsonObject object = value.getAsJsonObject();
            if (!object.has("transformer") || !object.has("supported_stats")) {
                throw this.fail("generator requires transformer and supported_stats");
            }
            this.render.add("generator", value);
            return this;
        }

        public MaterialDefinition generator(String... supportedStats) {
            this.requireFull("generator");
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
            colorMapping.add("palette", this.defaultPalette());
            transformer.add("color_mapping", colorMapping);
            generator.add("transformer", transformer);
            this.render.add("generator", generator);
            return this;
        }

        public MaterialDefinition generatorPalette(Map<String, String> palette) {
            this.requireFull("generatorPalette");
            JsonElement generator = this.render.get("generator");
            if (generator == null || !generator.isJsonObject() || !generator.getAsJsonObject().has("transformer")) {
                throw new IllegalStateException("Material " + this.id + ": call generator(...) before generatorPalette(...)");
            }
            JsonObject transformer = generator.getAsJsonObject().getAsJsonObject("transformer");
            JsonObject mapping = transformer == null ? null : transformer.getAsJsonObject("color_mapping");
            if (mapping == null) {
                throw new IllegalStateException("Material " + this.id + ": generator transformer has no color_mapping");
            }
            JsonObject values = new JsonObject();
            palette.forEach(values::addProperty);
            mapping.add("palette", values);
            return this;
        }

        public MaterialDefinition textColor(String color) {
            this.requireFull("textColor");
            String hex = this.hex(color);
            if (hex.length() != 6) {
                throw this.fail("text color must be six hexadecimal digits");
            }
            this.textColor = "#" + hex;
            this.customTextColor = true;
            return this;
        }

        public MaterialDefinition name(String text) {
            this.requireFull("name");
            return this.name("en_us", text);
        }

        public MaterialDefinition name(String lang, String text) {
            this.requireFull("name");
            this.names.put(lang, text);
            return this;
        }

        public MaterialDefinition flavor(String text) {
            this.requireFull("flavor");
            return this.flavor("en_us", text);
        }

        public MaterialDefinition flavor(String lang, String text) {
            this.requireFull("flavor");
            this.flavors.put(lang, text);
            return this;
        }

        public MaterialDefinition encyclopedia(String text) {
            this.requireFull("encyclopedia");
            return this.encyclopedia("en_us", text);
        }

        public MaterialDefinition encyclopedia(String lang, String text) {
            this.requireFull("encyclopedia");
            this.encyclopedia.put(lang, text);
            return this;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public JsonObject definition() {
            return this.definition;
        }

        public JsonObject stats() {
            return this.stats;
        }

        public JsonObject traits() {
            JsonObject traits = new JsonObject();
            if (!this.extension) {
                traits.add("default", this.defaultTraits);
            }
            traits.add("perStat", this.perStat);
            return traits;
        }

        public boolean hasTraits() {
            return this.traitsUsed;
        }

        public boolean isInvalid() {
            return this.invalid;
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

        public String outputPath() {
            return this.id.getPath();
        }

        public String translationKey() {
            return "material." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.');
        }

        public boolean hasAssets() {
            return !this.render.entrySet().isEmpty() || this.textColor != null || !this.names.isEmpty() || !this.flavors.isEmpty() || !this.encyclopedia.isEmpty();
        }

        private MaterialDefinition plating(String name, double durability, double armor, double toughness, double knockbackResistance) {
            return this.stat(name, values("durability", durability, "armor", armor, "toughness", toughness, "knockback_resistance", knockbackResistance), Map.of("durability", INT_MIN_ONE, "armor", FLOAT, "toughness", FLOAT, "knockback_resistance", FLOAT), "durability");
        }

        private MaterialDefinition repair(String name, double repairAmount) {
            return this.stat(name, values("repair_amount", repairAmount), Map.of("repair_amount", INT_MIN_ONE), "repair_amount");
        }

        private static Map<String, Object> values(Object... entries) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < entries.length; i += 2) {
                map.put((String) entries[i], entries[i + 1]);
            }
            return map;
        }

        private MaterialDefinition emptyStat(String name) {
            this.stats.add("tconstruct:" + name, new JsonObject());
            return this;
        }

        private MaterialDefinition stat(String name, Map<String, Object> values, Map<String, String> fields, String... required) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String type = fields.get(entry.getKey());
                if (type == null) {
                    throw this.fail("Unknown field '" + entry.getKey() + "' for material stat tconstruct:" + name + ", allowed: " + fields.keySet());
                }
                this.writeField(json, name, entry.getKey(), type, entry.getValue());
            }
            for (String key : required) {
                if (!json.has(key)) {
                    throw this.fail("Material stat tconstruct:" + name + " requires field '" + key + "'");
                }
            }
            this.stats.add("tconstruct:" + name, json);
            return this;
        }

        private void writeField(JsonObject json, String stat, String key, String type, Object value) {
            if (TIER.equals(type)) {
                if (!(value instanceof CharSequence text) || ResourceLocation.tryParse(text.toString()) == null) {
                    throw this.fail("Field '" + key + "' of tconstruct:" + stat + " must be a harvest tier id such as minecraft:iron");
                }
                json.addProperty(key, text.toString());
                return;
            }
            if (!(value instanceof Number number)) {
                throw this.fail("Field '" + key + "' of tconstruct:" + stat + " must be a number");
            }
            double amount = number.doubleValue();
            if (type.startsWith("int")) {
                if (amount != Math.rint(amount)) {
                    throw this.fail("Field '" + key + "' of tconstruct:" + stat + " must be a whole number");
                }
                int min = INT_MIN_ONE.equals(type) ? 1 : 0;
                if (amount < min) {
                    throw this.fail("Field '" + key + "' of tconstruct:" + stat + " must be at least " + min);
                }
                json.addProperty(key, (int) amount);
                return;
            }
            if (FLOAT_MIN_ZERO.equals(type) && amount < 0) {
                throw this.fail("Field '" + key + "' of tconstruct:" + stat + " must be at least 0");
            }
            json.addProperty(key, amount);
        }

        private void requireFull(String method) {
            if (this.extension) {
                if ("trait".equals(method)) {
                    throw this.fail(".trait(...) is not available on extend(...) because it would replace the material's existing default traits; use .statTrait(...) instead");
                }
                throw this.fail("." + method + "(...) is not available on extend(...); extensions can only add stats to an existing material");
            }
        }

        private IllegalArgumentException fail(String message) {
            this.invalid = true;
            return new IllegalArgumentException("Material " + this.id + ": " + message);
        }

        private String hex(String color) {
            String text = color.startsWith("#") ? color.substring(1) : color;
            if (!text.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
                throw this.fail("color must be six or eight hexadecimal digits, got '" + color + "'");
            }
            return text.toUpperCase(Locale.ROOT);
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
                palette.addProperty(keys[i], String.format(Locale.ROOT, "%02X%02X%02X%02X", alpha, r, g, b));
            }
            palette.addProperty("000", "00000000");
            return palette;
        }

        private static JsonObject traitJson(String modifier, int level) {
            JsonObject json = new JsonObject();
            json.addProperty("name", modifier);
            json.addProperty("level", level);
            return json;
        }
    }

    public static class StatFormulaDefinition {
        private static final Set<String> ROUNDING = Set.of("none", "nearest", "floor", "ceil");
        private final MaterialStatsId id;
        private final JsonObject fields = new JsonObject();
        private final JsonArray traits = new JsonArray();
        private boolean invalid;

        private StatFormulaDefinition(MaterialStatsId id) {
            this.id = id;
        }

        public StatFormulaDefinition field(String field, Map<String, Object> coefficients) {
            this.fields.add(field, this.linear(field, coefficients));
            return this;
        }

        public StatFormulaDefinition field(String field, Map<String, Object> coefficients, String round) {
            JsonObject json = this.linear(field, coefficients);
            if (!ROUNDING.contains(round)) {
                throw this.fail("Field '" + field + "' uses unknown round mode '" + round + "', allowed: " + ROUNDING);
            }
            json.addProperty("round", round);
            this.fields.add(field, json);
            return this;
        }

        public StatFormulaDefinition levels(String field, Map<String, Object> coefficients, List<List<Object>> levels) {
            JsonObject json = new JsonObject();
            json.add("score", this.linear(field, coefficients));
            JsonArray array = new JsonArray();
            for (List<Object> level : levels) {
                if (level.size() != 2 || !(level.get(0) instanceof Number threshold)) {
                    throw this.fail("Field '" + field + "' levels must be [threshold, value] pairs");
                }
                JsonArray pair = new JsonArray();
                pair.add(threshold.doubleValue());
                pair.add(JsonIO.of(level.get(1)));
                array.add(pair);
            }
            if (array.isEmpty()) {
                throw this.fail("Field '" + field + "' needs at least one level");
            }
            json.add("levels", array);
            this.fields.add(field, json);
            return this;
        }

        public StatFormulaDefinition tiers(Map<String, Object> coefficients, List<List<Object>> levels) {
            for (List<Object> level : levels) {
                if (level.size() == 2 && (!(level.get(1) instanceof CharSequence text) || ResourceLocation.tryParse(text.toString()) == null)) {
                    throw this.fail("Mining tier levels must use harvest tier ids such as minecraft:iron");
                }
            }
            return this.levels("mining_tier", coefficients, levels);
        }

        public StatFormulaDefinition trait(String modifier, Map<String, Object> coefficients) {
            return this.trait(modifier, coefficients, Integer.MAX_VALUE);
        }

        public StatFormulaDefinition trait(String modifier, Map<String, Object> coefficients, int max) {
            if (max < 1) {
                throw this.fail("Trait " + modifier + " max level must be at least 1");
            }
            JsonObject json = new JsonObject();
            json.addProperty("modifier", new ResourceLocation(modifier).toString());
            json.add("level", this.linear("trait " + modifier, coefficients));
            if (max != Integer.MAX_VALUE) {
                json.addProperty("max", max);
            }
            this.traits.add(json);
            return this;
        }

        public MaterialStatsId id() {
            return this.id;
        }

        public boolean isInvalid() {
            return this.invalid;
        }

        public JsonObject json() {
            JsonObject json = new JsonObject();
            json.add("fields", this.fields);
            if (!this.traits.isEmpty()) {
                json.add("traits", this.traits);
            }
            return json;
        }

        JsonObject linear(String field, Map<String, Object> coefficients) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, Object> entry : coefficients.entrySet()) {
                if ("round".equals(entry.getKey())) {
                    if (!(entry.getValue() instanceof CharSequence text) || !ROUNDING.contains(text.toString())) {
                        throw this.fail("'" + field + "' uses unknown round mode '" + entry.getValue() + "', allowed: " + ROUNDING);
                    }
                    json.addProperty("round", text.toString());
                    continue;
                }
                if (!(entry.getValue() instanceof Number number)) {
                    throw this.fail("'" + field + "' coefficient '" + entry.getKey() + "' must be a number");
                }
                json.addProperty(entry.getKey(), number.doubleValue());
            }
            return json;
        }

        private IllegalArgumentException fail(String message) {
            this.invalid = true;
            return new IllegalArgumentException("Stat formula " + this.id + ": " + message);
        }
    }

    public static class PartFormulaDefinition {
        private static final Set<String> OPERATIONS = Set.of("base", "add", "percent", "multiply");
        private final ResourceLocation id;
        private final JsonArray stats = new JsonArray();
        private final StatFormulaDefinition traits;
        private boolean invalid;

        private PartFormulaDefinition(ResourceLocation id) {
            this.id = id;
            this.traits = new StatFormulaDefinition(new MaterialStatsId(id));
        }

        public PartFormulaDefinition stat(String toolStat, Map<String, Object> coefficients) {
            return this.stat(toolStat, "add", coefficients);
        }

        public PartFormulaDefinition stat(String toolStat, String operation, Map<String, Object> coefficients) {
            ResourceLocation stat = new ResourceLocation(toolStat.contains(":") ? toolStat : "tconstruct:" + toolStat);
            if (!(ToolStats.getToolStat(new ToolStatId(stat)) instanceof INumericToolStat<?>)) {
                throw this.fail(stat + " is not a numeric Tinkers tool stat");
            }
            if (!OPERATIONS.contains(operation)) {
                throw this.fail("Unknown operation '" + operation + "' for " + stat + ", allowed: " + OPERATIONS);
            }
            JsonObject json = new JsonObject();
            json.addProperty("stat", stat.toString());
            json.addProperty("op", operation);
            try {
                json.add("value", this.traits.linear(stat.toString(), coefficients));
            } catch (IllegalArgumentException e) {
                this.invalid = true;
                throw e;
            }
            this.stats.add(json);
            return this;
        }

        public PartFormulaDefinition trait(String modifier, Map<String, Object> coefficients) {
            return this.trait(modifier, coefficients, Integer.MAX_VALUE);
        }

        public PartFormulaDefinition trait(String modifier, Map<String, Object> coefficients, int max) {
            try {
                this.traits.trait(modifier, coefficients, max);
            } catch (IllegalArgumentException e) {
                this.invalid = true;
                throw e;
            }
            return this;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public boolean isInvalid() {
            return this.invalid || this.traits.isInvalid();
        }

        public JsonObject json() {
            JsonObject json = new JsonObject();
            json.add("stats", this.stats);
            JsonObject traitJson = this.traits.json();
            if (traitJson.has("traits")) {
                json.add("traits", traitJson.get("traits"));
            }
            return json;
        }

        private IllegalArgumentException fail(String message) {
            this.invalid = true;
            return new IllegalArgumentException("Part formula " + this.id + ": " + message);
        }
    }
}
