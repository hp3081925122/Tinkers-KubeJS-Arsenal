package com.hp.tinkers_kubejs_arsenal.kubejs.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ToolDataEventJS extends EventJS {
    private static final int[][][] DEFAULT_LAYOUTS = {
        {{39, 35}},
        {{39, 35}, {21, 53}},
        {{53, 22}, {15, 60}, {33, 42}},
        {{25, 20}, {21, 52}, {50, 48}, {44, 29}}
    };
    private static final String ICON_NBT = "{Damage:0,tic_broken:0b,tic_display:1b,tic_materials:[\"tconstruct:ui_render#head\",\"tconstruct:ui_render#handle\",\"tconstruct:ui_render#extra\",\"tconstruct:ui_render#large\",\"tconstruct:ui_render#extra_large\"]}";

    private final Map<ResourceLocation, ToolDefinitionJS> tools = new LinkedHashMap<>();

    public ToolDefinitionJS create(String id) {
        ResourceLocation key = new ResourceLocation(id);
        if (this.tools.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate tool definition: " + id);
        }
        ToolDefinitionJS tool = new ToolDefinitionJS(key, ArsenalToolBuilder.TOOLS.get(key));
        this.tools.put(key, tool);
        tool.checkRegistered();
        return tool;
    }

    public Map<ResourceLocation, ToolDefinitionJS> getTools() {
        return this.tools;
    }

    public static class ToolDefinitionJS {
        private final ResourceLocation id;
        private final ArsenalToolBuilder builder;
        private final JsonObject baseStats = new JsonObject();
        private final JsonObject multipliers = new JsonObject();
        private final JsonObject slots = new JsonObject();
        private final JsonArray traits = new JsonArray();
        private final JsonArray toolActions = new JsonArray();
        private final List<JsonObject> modules = new ArrayList<>();
        private final Set<String> modifiable = new LinkedHashSet<>();
        private final Map<Integer, int[]> layoutSlots = new LinkedHashMap<>();
        private JsonObject box;
        private int primaryPart;
        private int materialName = -1;
        private int sortIndex = 100;
        private boolean invalid;

        private ToolDefinitionJS(ResourceLocation id, ArsenalToolBuilder builder) {
            this.id = id;
            this.builder = builder;
        }

        public ToolDefinitionJS baseStat(String toolStat, double value) {
            this.baseStats.addProperty(statId(toolStat), value);
            return this;
        }

        public ToolDefinitionJS multiplyStat(String toolStat, double value) {
            this.multipliers.addProperty(statId(toolStat), value);
            return this;
        }

        public ToolDefinitionJS slot(String slotType, int count) {
            if (count < 0) {
                throw this.fail("Slot count for '" + slotType + "' must be at least 0");
            }
            this.slots.addProperty(slotType, count);
            return this;
        }

        public ToolDefinitionJS trait(String modifier) {
            return this.trait(modifier, 1);
        }

        public ToolDefinitionJS trait(String modifier, int level) {
            if (level < 1) {
                throw this.fail("Trait level for " + modifier + " must be at least 1");
            }
            JsonObject json = new JsonObject();
            json.addProperty("level", level);
            json.addProperty("name", new ResourceLocation(modifier).toString());
            this.traits.add(json);
            return this;
        }

        public ToolDefinitionJS toolActions(String... toolActions) {
            for (String action : toolActions) {
                this.toolActions.add(action);
            }
            return this;
        }

        public ToolDefinitionJS effective(String blockTag) {
            JsonObject json = new JsonObject();
            json.addProperty("predicate_type", "mantle:tag");
            json.addProperty("tag", new ResourceLocation(blockTag.startsWith("#") ? blockTag.substring(1) : blockTag).toString());
            return this.add("tconstruct:is_effective", json);
        }

        public ToolDefinitionJS boxAoe(int width, int height, int depth) {
            this.box().add("bonus", boxSize(width, height, depth));
            return this;
        }

        public ToolDefinitionJS boxAoeExpansion(int width, int height, int depth) {
            this.box().getAsJsonArray("expansions").add(boxSize(width, height, depth));
            return this;
        }

        public ToolDefinitionJS boxAoeDirection(String direction) {
            this.box().addProperty("expansion_direction", new ResourceLocation(direction.contains(":") ? direction : "tconstruct:" + direction).toString());
            return this;
        }

        public ToolDefinitionJS circleAoe(int diameter, boolean threeDimensional) {
            JsonObject json = new JsonObject();
            json.addProperty("3D", threeDimensional);
            json.addProperty("diameter", diameter);
            return this.add("tconstruct:circle_aoe", json);
        }

        public ToolDefinitionJS veinAoe(int maxDistance) {
            JsonObject json = new JsonObject();
            json.addProperty("max_distance", maxDistance);
            return this.add("tconstruct:vein_aoe", json);
        }

        public ToolDefinitionJS treeAoe(int widthBonus, int depthBonus) {
            JsonObject json = new JsonObject();
            json.addProperty("width_bonus", widthBonus);
            json.addProperty("depth_bonus", depthBonus);
            return this.add("tconstruct:tree_aoe", json);
        }

        public ToolDefinitionJS sweepMelee(double range) {
            JsonObject json = new JsonObject();
            json.addProperty("range", range);
            return this.add("tconstruct:sweep_melee", json);
        }

        public ToolDefinitionJS circleMelee(double diameter) {
            JsonObject json = new JsonObject();
            json.addProperty("diameter", diameter);
            return this.add("tconstruct:circle_melee", json);
        }

        public ToolDefinitionJS module(String type, Map<String, Object> options) {
            JsonObject json = new JsonObject();
            options.forEach((key, value) -> {
                if (!"type".equals(key)) {
                    json.add(key, JsonIO.of(value));
                }
            });
            return this.add(new ResourceLocation(type).toString(), json);
        }

        public ToolDefinitionJS modifiable(String... modifiable) {
            for (String tag : modifiable) {
                String path = tag.startsWith("tconstruct:modifiable/") ? tag.substring("tconstruct:modifiable/".length()) : tag;
                if (path.contains(":")) {
                    throw this.fail("Modifiable tag '" + tag + "' must be a path under tconstruct:modifiable/, such as melee/primary");
                }
                this.modifiable.add(path);
            }
            return this;
        }

        public ToolDefinitionJS primaryPart(int index) {
            this.checkIndex(index);
            this.primaryPart = index;
            return this;
        }

        public ToolDefinitionJS materialName(int index) {
            this.checkIndex(index);
            this.materialName = index;
            return this;
        }

        public ToolDefinitionJS layoutSlot(int index, int x, int y) {
            this.checkIndex(index);
            this.layoutSlots.put(index, new int[]{x, y});
            return this;
        }

        public ToolDefinitionJS sortIndex(int sortIndex) {
            this.sortIndex = sortIndex;
            return this;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public boolean isInvalid() {
            return this.invalid;
        }

        public Set<String> modifiableTags() {
            Set<String> tags = new LinkedHashSet<>(List.of("multipart", "durability"));
            tags.addAll(this.modifiable);
            return tags;
        }

        public JsonObject definition() {
            JsonArray list = new JsonArray();
            JsonObject partStats = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonArray materials = new JsonArray();
            for (ArsenalToolBuilder.ToolPart part : this.builder.parts()) {
                parts.add(part.item().toString());
                JsonObject material = new JsonObject();
                material.addProperty("type", "tconstruct:random");
                material.addProperty("tier", 1);
                materials.add(material);
            }
            partStats.add("parts", parts);
            partStats.addProperty("primary_part", this.primaryPart);
            list.add(typed("tconstruct:part_stats", partStats));
            JsonObject defaults = new JsonObject();
            defaults.add("materials", materials);
            list.add(typed("tconstruct:default_materials", defaults));
            if (this.baseStats.size() > 0) {
                list.add(typed("tconstruct:base_stats", wrap("stats", this.baseStats)));
            }
            if (this.multipliers.size() > 0) {
                list.add(typed("tconstruct:multiply_stats", wrap("multipliers", this.multipliers)));
            }
            if (this.slots.size() > 0) {
                list.add(typed("tconstruct:modifier_slots", wrap("slots", this.slots)));
            }
            if (this.traits.size() > 0) {
                list.add(typed("tconstruct:traits", wrap("traits", this.traits)));
            }
            if (this.toolActions.size() > 0) {
                list.add(typed("tconstruct:tool_actions", wrap("tool_actions", this.toolActions)));
            }
            this.modules.forEach(list::add);
            if (this.materialName >= 0) {
                JsonObject name = new JsonObject();
                name.addProperty("index", this.materialName);
                list.add(typed("tconstruct:fixed_material_name", name));
            }
            return wrap("modules", list);
        }

        public JsonObject layout() {
            List<ArsenalToolBuilder.ToolPart> parts = this.builder.parts();
            int[][] defaults = parts.size() <= DEFAULT_LAYOUTS.length ? DEFAULT_LAYOUTS[parts.size() - 1] : new int[0][];
            JsonArray slots = new JsonArray();
            for (int i = 0; i < parts.size(); i++) {
                int[] position = this.layoutSlots.getOrDefault(i, i < defaults.length ? defaults[i] : null);
                if (position == null) {
                    throw new IllegalArgumentException("Tool " + this.id + ": part " + i + " has no station position; tools with more than " + DEFAULT_LAYOUTS.length + " parts need .layoutSlot(index, x, y) for each extra part");
                }
                ResourceLocation item = parts.get(i).item();
                JsonObject filter = new JsonObject();
                filter.addProperty("item", item.toString());
                JsonObject slot = new JsonObject();
                slot.add("filter", filter);
                slot.addProperty("icon", item.toString());
                slot.addProperty("translation_key", ForgeRegistries.ITEMS.getValue(item).getDescriptionId());
                slot.addProperty("x", position[0]);
                slot.addProperty("y", position[1]);
                slots.add(slot);
            }
            JsonObject icon = new JsonObject();
            icon.addProperty("item", this.id.toString());
            icon.addProperty("nbt", ICON_NBT);
            JsonObject json = new JsonObject();
            json.add("icon", icon);
            json.add("input_slots", slots);
            json.addProperty("sortIndex", this.sortIndex);
            json.addProperty("translation_key", ForgeRegistries.ITEMS.getValue(this.id).getDescriptionId());
            return json;
        }

        public JsonObject recipe() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "tconstruct:tool_building");
            json.addProperty("result", this.id.toString());
            json.addProperty("result_count", 1);
            return json;
        }

        private void checkRegistered() {
            if (this.builder == null) {
                throw this.fail("Not registered; create it in startup_scripts with StartupEvents.registry('item', event => event.create('" + this.id + "', '" + ArsenalToolBuilder.TOOL_TYPE + "'))");
            }
            for (ArsenalToolBuilder.ToolPart part : this.builder.parts()) {
                Item item = ForgeRegistries.ITEMS.getValue(part.item());
                if (!(item instanceof IToolPart)) {
                    throw this.fail("Part '" + part.name() + "' uses " + part.item() + ", which is not a Tinkers tool part");
                }
            }
        }

        private JsonObject box() {
            if (this.box == null) {
                JsonObject json = new JsonObject();
                json.addProperty("expansion_direction", "tconstruct:side_hit");
                json.add("expansions", new JsonArray());
                this.box = typed("tconstruct:box_aoe", json);
                this.modules.add(this.box);
            }
            return this.box;
        }

        private static JsonObject boxSize(int width, int height, int depth) {
            JsonObject json = new JsonObject();
            json.addProperty("width", width);
            json.addProperty("height", height);
            json.addProperty("depth", depth);
            return json;
        }

        private static String statId(String toolStat) {
            return toolStat.contains(":") ? new ResourceLocation(toolStat).toString() : "tconstruct:" + toolStat;
        }

        private ToolDefinitionJS add(String type, JsonObject json) {
            this.modules.add(typed(type, json));
            return this;
        }

        private void checkIndex(int index) {
            if (index < 0 || index >= this.builder.parts().size()) {
                throw this.fail("Part index " + index + " is out of range, the tool has " + this.builder.parts().size() + " parts");
            }
        }

        private IllegalArgumentException fail(String message) {
            this.invalid = true;
            return new IllegalArgumentException("Tool " + this.id + ": " + message);
        }

        private static JsonObject typed(String type, JsonObject json) {
            JsonObject result = new JsonObject();
            result.addProperty("type", type);
            json.entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return result;
        }

        private static JsonObject wrap(String key, JsonElement value) {
            JsonObject json = new JsonObject();
            json.add(key, value);
            return json;
        }
    }
}
