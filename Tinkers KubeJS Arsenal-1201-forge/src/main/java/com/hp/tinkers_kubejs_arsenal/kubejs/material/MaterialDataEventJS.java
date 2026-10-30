package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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

    public MaterialDefinition create(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.materials.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate material: " + id);
        }
        MaterialDefinition material = new MaterialDefinition(key);
        this.materials.put(key, material);
        return material;
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
        private boolean traitsUsed;
        private boolean invalid;

        private MaterialDefinition(ResourceLocation id) {
            this.id = id;
            this.definition.addProperty("craftable", false);
            this.definition.addProperty("tier", 0);
            this.definition.addProperty("sortOrder", 100);
            this.definition.addProperty("hidden", false);
        }

        public MaterialDefinition tier(int tier) {
            this.definition.addProperty("tier", tier);
            return this;
        }

        public MaterialDefinition sortOrder(int sortOrder) {
            this.definition.addProperty("sortOrder", sortOrder);
            return this;
        }

        public MaterialDefinition craftable(boolean craftable) {
            this.definition.addProperty("craftable", craftable);
            return this;
        }

        public MaterialDefinition hidden(boolean hidden) {
            this.definition.addProperty("hidden", hidden);
            return this;
        }

        public MaterialDefinition rarity(String rarity) {
            String value = rarity.toLowerCase(Locale.ROOT);
            if (!RARITIES.contains(value)) {
                throw this.fail("Invalid material rarity '" + rarity + "', expected common, uncommon, rare or epic");
            }
            this.definition.addProperty("rarity", value.toUpperCase(Locale.ROOT));
            return this;
        }

        public MaterialDefinition head(Map<String, Object> headStats) {
            return this.stat("head", headStats, Map.of("durability", INT_MIN_ONE, "mining_speed", FLOAT, "mining_tier", TIER, "melee_attack", FLOAT), "durability");
        }

        public MaterialDefinition handle(Map<String, Object> handleStats) {
            return this.stat("handle", handleStats, Map.of("durability", FLOAT, "mining_speed", FLOAT, "melee_speed", FLOAT, "melee_damage", FLOAT));
        }

        public MaterialDefinition grip(Map<String, Object> gripStats) {
            return this.stat("grip", gripStats, Map.of("durability", FLOAT, "accuracy", FLOAT, "melee_damage", FLOAT_MIN_ZERO));
        }

        public MaterialDefinition limb(Map<String, Object> limbStats) {
            return this.stat("limb", limbStats, Map.of("durability", INT_MIN_ONE, "draw_speed", FLOAT, "velocity", FLOAT, "accuracy", FLOAT), "durability");
        }

        public MaterialDefinition platingHelmet(Map<String, Object> platingStats) {
            return this.plating("plating_helmet", platingStats);
        }

        public MaterialDefinition platingChestplate(Map<String, Object> platingStats) {
            return this.plating("plating_chestplate", platingStats);
        }

        public MaterialDefinition platingLeggings(Map<String, Object> platingStats) {
            return this.plating("plating_leggings", platingStats);
        }

        public MaterialDefinition platingBoots(Map<String, Object> platingStats) {
            return this.plating("plating_boots", platingStats);
        }

        public MaterialDefinition platingShield(Map<String, Object> shieldStats) {
            return this.stat("plating_shield", shieldStats, Map.of("durability", INT_MIN_ONE, "toughness", FLOAT, "knockback_resistance", FLOAT), "durability");
        }

        public MaterialDefinition skull(Map<String, Object> skullStats) {
            return this.stat("skull", skullStats, Map.of("durability", INT_MIN_ONE, "armor", INT_MIN_ZERO), "durability");
        }

        public MaterialDefinition slime(Map<String, Object> slimeStats) {
            return this.stat("slime", slimeStats, Map.of("durability", INT_MIN_ONE, "overslime_capacity", INT_MIN_ZERO), "durability", "overslime_capacity");
        }

        public MaterialDefinition ribcage(Map<String, Object> repairStats) {
            return this.repair("ribcage", repairStats);
        }

        public MaterialDefinition shell(Map<String, Object> repairStats) {
            return this.repair("shell", repairStats);
        }

        public MaterialDefinition laces(Map<String, Object> repairStats) {
            return this.repair("laces", repairStats);
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

        public MaterialDefinition trait(String modifier) {
            return this.trait(modifier, 1);
        }

        public MaterialDefinition trait(String modifier, int level) {
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
            traits.add("default", this.defaultTraits);
            traits.add("perStat", this.perStat);
            return traits;
        }

        public boolean hasTraits() {
            return this.traitsUsed;
        }

        public boolean isInvalid() {
            return this.invalid;
        }

        private MaterialDefinition plating(String name, Map<String, Object> values) {
            return this.stat(name, values, Map.of("durability", INT_MIN_ONE, "armor", FLOAT, "toughness", FLOAT, "knockback_resistance", FLOAT), "durability");
        }

        private MaterialDefinition repair(String name, Map<String, Object> values) {
            return this.stat(name, values, Map.of("repair_amount", INT_MIN_ONE), "repair_amount");
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

        private IllegalArgumentException fail(String message) {
            this.invalid = true;
            return new IllegalArgumentException("Material " + this.id + ": " + message);
        }

        private static JsonObject traitJson(String modifier, int level) {
            JsonObject json = new JsonObject();
            json.addProperty("name", modifier);
            json.addProperty("level", level);
            return json;
        }
    }
}
