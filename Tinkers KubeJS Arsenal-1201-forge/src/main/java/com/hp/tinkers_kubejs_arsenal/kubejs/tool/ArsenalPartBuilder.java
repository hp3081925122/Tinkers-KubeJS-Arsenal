package com.hp.tinkers_kubejs_arsenal.kubejs.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.PartCastItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ArsenalPartBuilder extends ItemBuilder {
    public static final String TYPE = "tinkers_kubejs_arsenal:part";
    public static final Map<ResourceLocation, ArsenalPartBuilder> PARTS = new LinkedHashMap<>();
    public static final Set<ResourceLocation> CASTS = new HashSet<>();

    private MaterialStatsId statType;
    private int cost = 1;
    private boolean casts;
    private ResourceLocation partTexture;
    private final Map<String, String> names = new LinkedHashMap<>();
    private final List<CastBuilder> castBuilders = new ArrayList<>();

    public ArsenalPartBuilder(ResourceLocation id) {
        super(id);
        this.partTexture = new ResourceLocation(id.getNamespace(), "item/part/" + id.getPath());
        PARTS.put(id, this);
    }

    public ArsenalPartBuilder statType(String statType) {
        this.statType = new MaterialStatsId(statType.contains(":") ? statType : "tconstruct:" + statType);
        return this;
    }

    public ArsenalPartBuilder cost(int cost) {
        if (cost < 1) {
            throw this.fail("Cost must be at least 1, got " + cost);
        }
        this.cost = cost;
        return this;
    }

    public ArsenalPartBuilder casts() {
        this.casts = true;
        return this;
    }

    public ArsenalPartBuilder partTexture(String texture) {
        this.partTexture = new ResourceLocation(texture);
        return this;
    }

    public ArsenalPartBuilder name(String lang, String text) {
        this.names.put(lang, text);
        return this;
    }

    public MaterialStatsId getStatType() {
        return this.statType;
    }

    public int getCost() {
        return this.cost;
    }

    public ResourceLocation getPartTexture() {
        return this.partTexture;
    }

    public List<CastBuilder> getCasts() {
        return List.copyOf(this.castBuilders);
    }

    public Map<String, JsonObject> recipes() {
        String path = this.id.getPath();
        String part = this.id.toString();
        Map<String, JsonObject> recipes = new LinkedHashMap<>();
        JsonArray patternItems = new JsonArray();
        patternItems.add(tag("tconstruct:patterns/default"));
        this.castBuilders.stream().filter(cast -> cast.type == CastType.GOLD).forEach(cast -> patternItems.add(item(cast.id.toString())));
        JsonObject builder = recipe("tconstruct:part_builder");
        builder.addProperty("cost", this.cost);
        builder.addProperty("pattern", part);
        builder.add("pattern_item", patternItems);
        builder.add("result", item(part));
        recipes.put("tools/parts/builder/" + path, builder);
        JsonObject composite = recipe("tconstruct:table_casting_composite");
        composite.addProperty("item_cost", this.cost);
        composite.addProperty("result", part);
        recipes.put("tools/parts/casting/" + path + "_composite", composite);
        for (CastBuilder cast : this.castBuilders) {
            String castId = cast.id.toString();
            if (cast.type == CastType.GOLD) {
                JsonObject casting = recipe("tconstruct:table_casting_material");
                casting.add("cast", tag(this.castTag(true)));
                casting.addProperty("item_cost", this.cost);
                casting.addProperty("result", part);
                recipes.put("tools/parts/casting/" + path + "_gold_cast", casting);
                JsonObject fluid = new JsonObject();
                fluid.addProperty("amount", 90);
                fluid.addProperty("tag", "forge:molten_gold");
                JsonObject create = recipe("tconstruct:casting_table");
                create.add("cast", materialItem(part));
                create.addProperty("cast_consumed", true);
                create.addProperty("cooling_time", 57);
                create.add("fluid", fluid);
                create.addProperty("result", castId);
                create.addProperty("switch_slots", true);
                recipes.put("smeltery/casts/gold/" + path, create);
                continue;
            }
            String sand = cast.type == CastType.SAND ? "sand" : "red_sand";
            JsonObject blockResult = item(castId);
            blockResult.addProperty("count", 4);
            JsonObject builderBlock = recipe("tconstruct:item_part_builder");
            builderBlock.addProperty("pattern", part);
            builderBlock.add("pattern_item", tag(cast.type == CastType.SAND ? "forge:sand/colorless" : "forge:sand/red"));
            builderBlock.add("result", blockResult);
            recipes.put("smeltery/casts/" + sand + "/builder_block/" + path, builderBlock);
            JsonObject builderCast = recipe("tconstruct:item_part_builder");
            builderCast.addProperty("pattern", part);
            builderCast.add("pattern_item", tag("tconstruct:casts/" + sand));
            builderCast.addProperty("result", castId);
            recipes.put("smeltery/casts/" + sand + "/builder_cast/" + path, builderCast);
            JsonObject molding = recipe("tconstruct:molding_table");
            molding.add("material", tag("tconstruct:casts/" + sand));
            molding.add("pattern", materialItem(part));
            molding.addProperty("result", castId);
            recipes.put("smeltery/casts/" + sand + "/molding/" + path, molding);
            if (cast.type == CastType.SAND) {
                JsonObject casting = recipe("tconstruct:table_casting_material");
                casting.add("cast", tag(this.castTag(false)));
                casting.addProperty("cast_consumed", true);
                casting.addProperty("item_cost", this.cost);
                casting.addProperty("result", part);
                recipes.put("tools/parts/casting/" + path + "_sand_cast", casting);
            }
        }
        return recipes;
    }

    public void tags(BiConsumer<String, String> tags) {
        tags.accept("tconstruct:parts", this.id.toString());
        for (CastBuilder cast : this.castBuilders) {
            boolean gold = cast.type == CastType.GOLD;
            tags.accept(gold ? "tconstruct:casts/gold" : cast.type == CastType.SAND ? "tconstruct:casts/sand" : "tconstruct:casts/red_sand", cast.id.toString());
            tags.accept(this.castTag(gold), cast.id.toString());
        }
        if (!this.castBuilders.isEmpty()) {
            tags.accept("tconstruct:casts/multi_use", "#" + this.castTag(true));
            tags.accept("tconstruct:casts/single_use", "#" + this.castTag(false));
        }
    }

    private String castTag(boolean multiUse) {
        return this.id.getNamespace() + ":casts/" + (multiUse ? "multi_use/" : "single_use/") + this.id.getPath();
    }

    private static JsonObject recipe(String type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        return json;
    }

    private static JsonObject tag(String tag) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", tag);
        return json;
    }

    private static JsonObject item(String item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        return json;
    }

    private static JsonObject materialItem(String item) {
        JsonObject json = item(item);
        json.addProperty("type", "tconstruct:material");
        return json;
    }

    @Override
    public Item createObject() {
        if (this.statType == null) {
            throw this.fail("Part has no stat type; set it with .statType(...), for example 'head' or 'handle'");
        }
        return new ToolPartItem(this.createItemProperties(), this.statType);
    }

    @Override
    public void createAdditionalObjects() {
        if (!this.casts) {
            return;
        }
        for (CastType type : CastType.values()) {
            CastBuilder cast = new CastBuilder(new ResourceLocation(this.id.getNamespace(), this.id.getPath() + type.suffix), this, type);
            this.castBuilders.add(cast);
            CASTS.add(cast.id);
            RegistryInfo.ITEM.addBuilder(cast);
        }
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (this.modelJson == null) {
            JsonObject textures = new JsonObject();
            textures.addProperty("texture", this.partTexture.toString());
            JsonObject json = new JsonObject();
            json.addProperty("parent", "forge:item/default");
            json.addProperty("loader", "tconstruct:material");
            json.add("textures", textures);
            this.modelJson = json;
        }
        super.generateAssetJsons(generator);
    }

    @Override
    public void generateLang(LangEventJS event) {
        super.generateLang(event);
        String namespace = this.id.getNamespace();
        Map<String, String> allNames = new LinkedHashMap<>();
        allNames.put("en_us", this.displayName != null ? this.displayName.getString() : UtilsJS.snakeCaseToTitleCase(this.id.getPath()));
        allNames.putAll(this.names);
        String key = this.getBuilderTranslationKey();
        String pattern = "pattern." + namespace + "." + this.id.getPath().replace('/', '.');
        allNames.forEach((lang, text) -> {
            event.map.put(new LangEventJS.Key(namespace, lang, key), text);
            event.map.put(new LangEventJS.Key(namespace, lang, pattern), text);
            for (CastBuilder cast : this.castBuilders) {
                String suffix = lang.equals("zh_cn") ? cast.type.chinese : cast.type.english;
                event.map.put(new LangEventJS.Key(namespace, lang, cast.getBuilderTranslationKey()), text + suffix);
            }
        });
    }

    private IllegalArgumentException fail(String message) {
        return new IllegalArgumentException("Part " + this.id + ": " + message);
    }

    public enum CastType {
        GOLD("_cast", "cast", " Gold Cast", "金质铸模"),
        SAND("_sand_cast", "sand_cast", " Sand Cast", "沙子铸模"),
        RED_SAND("_red_sand_cast", "red_sand_cast", " Red Sand Cast", "红沙铸模");

        private final String suffix;
        private final String folder;
        private final String english;
        private final String chinese;

        CastType(String suffix, String folder, String english, String chinese) {
            this.suffix = suffix;
            this.folder = folder;
            this.english = english;
            this.chinese = chinese;
        }
    }

    public static class CastBuilder extends ItemBuilder {
        private final ArsenalPartBuilder part;
        private final CastType type;

        public CastBuilder(ResourceLocation id, ArsenalPartBuilder part, CastType type) {
            super(id);
            this.part = part;
            this.type = type;
            this.texture(part.id.getNamespace() + ":item/" + type.folder + "/" + part.id.getPath());
        }

        public CastType type() {
            return this.type;
        }

        @Override
        public Item createObject() {
            return new PartCastItem(this.createItemProperties(), () -> (IMaterialItem) this.part.get());
        }

        @Override
        public void generateLang(LangEventJS event) {
        }
    }
}
