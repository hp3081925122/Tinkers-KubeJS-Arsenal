package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatTypeEventJS extends EventJS {
    public static final Map<MaterialStatsId, StatTypeDefinition> TYPES = new LinkedHashMap<>();
    private static final Set<String> OPERATIONS = Set.of("base", "add", "percent", "multiply");

    public StatTypeDefinition create(String id) {
        MaterialStatsId key = new MaterialStatsId(id);
        if ("tconstruct".equals(key.getNamespace())) {
            throw new IllegalArgumentException("Stat type " + id + " uses the tconstruct namespace; use your own namespace such as kubejs:");
        }
        if (TYPES.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate stat type: " + id);
        }
        StatTypeDefinition type = new StatTypeDefinition(key);
        TYPES.put(key, type);
        return type;
    }

    public static class StatTypeDefinition {
        private final MaterialStatsId id;
        private final List<Field> fields = new ArrayList<>();
        private final Map<String, String> names = new LinkedHashMap<>();
        private MaterialStatsId textureStat = new MaterialStatsId("tconstruct", "head");

        private StatTypeDefinition(MaterialStatsId id) {
            this.id = id;
        }

        public StatTypeDefinition field(String name, String toolStat, String operation, double defaultValue) {
            if (!name.matches("[a-z0-9_]+")) {
                throw this.fail("Field name '" + name + "' may only contain a-z, 0-9 and _");
            }
            if (this.fields.stream().anyMatch(field -> field.name().equals(name))) {
                throw this.fail("Duplicate field '" + name + "'");
            }
            if (!OPERATIONS.contains(operation)) {
                throw this.fail("Unknown operation '" + operation + "' for field '" + name + "', allowed: " + OPERATIONS);
            }
            ResourceLocation stat = new ResourceLocation(toolStat.contains(":") ? toolStat : "tconstruct:" + toolStat);
            this.fields.add(new Field(name, stat, operation, (float) defaultValue));
            return this;
        }

        public StatTypeDefinition textureLike(String statType) {
            this.textureStat = new MaterialStatsId(statType.contains(":") ? statType : "tconstruct:" + statType);
            return this;
        }

        public StatTypeDefinition name(String lang, String text) {
            this.names.put(lang, text);
            return this;
        }

        public MaterialStatsId id() {
            return this.id;
        }

        public List<Field> fields() {
            return List.copyOf(this.fields);
        }

        public Map<String, String> names() {
            return this.names;
        }

        public MaterialStatsId textureStat() {
            return this.textureStat;
        }

        private IllegalArgumentException fail(String message) {
            return new IllegalArgumentException("Stat type " + this.id + ": " + message);
        }
    }

    public record Field(String name, ResourceLocation toolStat, String operation, float defaultValue) {}
}
