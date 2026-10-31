package com.hp.tinkers_kubejs_arsenal.kubejs.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.tools.item.ModifiableSwordItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArsenalToolBuilder extends ItemBuilder {
    public static final String TOOL_TYPE = "tinkers_kubejs_arsenal:tool";
    public static final String SWORD_TYPE = "tinkers_kubejs_arsenal:sword";
    public static final Map<ResourceLocation, ArsenalToolBuilder> TOOLS = new LinkedHashMap<>();
    private static final List<String> BOOK_PAGES = List.of("materials_and_you/%s/tools", "mighty_smelting/%s/tools", "encyclopedia/%s/tools/small", "encyclopedia/%s/tools/broad", "encyclopedia/%s/tools/ancient", "encyclopedia/%s/tools/staff");

    private final boolean sword;
    private final List<ToolPart> parts = new ArrayList<>();
    private final Set<String> brokenParts = new LinkedHashSet<>();
    private final Map<String, String> names = new LinkedHashMap<>();
    private final Map<String, List<String>> bookTexts = new LinkedHashMap<>();
    private final Map<String, List<String>> bookProperties = new LinkedHashMap<>();

    public ArsenalToolBuilder(ResourceLocation id, boolean sword) {
        super(id);
        this.sword = sword;
        this.maxStackSize = 1;
        TOOLS.put(id, this);
    }

    public ArsenalToolBuilder part(String name, String partItem) {
        if (this.find(name) != null) {
            throw this.fail("Duplicate part name '" + name + "'");
        }
        this.parts.add(new ToolPart(name, new ResourceLocation(partItem), new ResourceLocation(this.id.getNamespace(), "item/tool/" + this.id.getPath() + "/" + name)));
        return this;
    }

    public ArsenalToolBuilder partTexture(String name, String texture) {
        ToolPart part = this.require(name);
        this.parts.set(this.parts.indexOf(part), new ToolPart(part.name(), part.item(), new ResourceLocation(texture)));
        return this;
    }

    public ArsenalToolBuilder brokenPart(String name) {
        this.require(name);
        this.brokenParts.add(name);
        return this;
    }

    public ArsenalToolBuilder name(String lang, String text) {
        this.names.put(lang, text);
        return this;
    }

    public ArsenalToolBuilder bookText(String lang, String text) {
        this.bookTexts.computeIfAbsent(lang, key -> new ArrayList<>()).add(text);
        return this;
    }

    public ArsenalToolBuilder bookProperty(String lang, String text) {
        this.bookProperties.computeIfAbsent(lang, key -> new ArrayList<>()).add(text);
        return this;
    }

    public List<ToolPart> parts() {
        return List.copyOf(this.parts);
    }

    public boolean isBroken(String name) {
        return this.brokenParts.contains(name);
    }

    @Override
    public Item createObject() {
        if (this.parts.isEmpty()) {
            throw this.fail("Tool has no parts; add them with .part(name, partItem)");
        }
        ToolDefinition definition = ToolDefinition.create(this.id);
        return this.sword ? new ModifiableSwordItem(this.createItemProperties(), definition) : new ModifiableItem(this.createItemProperties(), definition);
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (this.modelJson == null && !this.parts.isEmpty()) {
            this.modelJson = this.toolModel(false);
            if (!this.brokenParts.isEmpty()) {
                generator.json(new ResourceLocation(this.id.getNamespace(), "models/" + this.brokenModel().getPath()), this.toolModel(true));
            }
        }
        super.generateAssetJsons(generator);
        Set<String> langs = new LinkedHashSet<>(this.bookTexts.keySet());
        langs.addAll(this.bookProperties.keySet());
        String file = this.id.getNamespace() + "_" + this.id.getPath().replace('/', '_');
        for (String lang : langs) {
            JsonObject page = this.bookPage(lang);
            for (String folder : BOOK_PAGES) {
                generator.json(new ResourceLocation("tconstruct", "book/" + folder.formatted(lang) + "/" + file), page);
            }
        }
    }

    private JsonObject bookPage(String lang) {
        JsonArray text = new JsonArray();
        for (String paragraph : this.bookTexts.getOrDefault(lang, List.of())) {
            if (!text.isEmpty()) {
                JsonObject separator = new JsonObject();
                separator.addProperty("text", "\n");
                text.add(separator);
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("text", paragraph);
            text.add(entry);
        }
        JsonArray properties = new JsonArray();
        this.bookProperties.getOrDefault(lang, List.of()).forEach(properties::add);
        JsonObject json = new JsonObject();
        json.addProperty("tool", this.id.toString());
        json.add("text", text);
        json.add("properties", properties);
        return json;
    }

    @Override
    public void generateLang(LangEventJS event) {
        super.generateLang(event);
        String key = this.getBuilderTranslationKey();
        this.names.forEach((lang, text) -> event.map.put(new LangEventJS.Key(this.id.getNamespace(), lang, key), text));
    }

    private JsonObject toolModel(boolean broken) {
        JsonObject json = new JsonObject();
        json.addProperty("loader", "tconstruct:tool");
        json.addProperty("parent", this.parentModel.isEmpty() ? (this.sword ? "forge:item/default-tool" : "tconstruct:item/base/axe") : this.parentModel);
        JsonObject textures = new JsonObject();
        JsonArray partList = new JsonArray();
        for (int i = 0; i < this.parts.size(); i++) {
            ToolPart part = this.parts.get(i);
            textures.addProperty(part.name(), (broken && this.isBroken(part.name()) ? part.texture().withSuffix("_broken") : part.texture()).toString());
            JsonObject entry = new JsonObject();
            entry.addProperty("name", part.name());
            entry.addProperty("index", i);
            partList.add(entry);
        }
        json.add("textures", textures);
        String root = this.id.getNamespace() + ":item/tool/" + this.id.getPath() + "/modifiers/";
        JsonArray roots = new JsonArray();
        if (broken) {
            roots.add(root + "broken/");
        }
        roots.add(root);
        json.add("modifier_roots", roots);
        json.add("parts", partList);
        if (!broken && !this.brokenParts.isEmpty()) {
            JsonObject predicate = new JsonObject();
            predicate.addProperty("tconstruct:broken", 1);
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", this.brokenModel().toString());
            JsonArray overrides = new JsonArray();
            overrides.add(override);
            json.add("overrides", overrides);
        }
        return json;
    }

    private ResourceLocation brokenModel() {
        return new ResourceLocation(this.id.getNamespace(), "item/tool/" + this.id.getPath() + "/broken");
    }

    private ToolPart find(String name) {
        return this.parts.stream().filter(part -> part.name().equals(name)).findFirst().orElse(null);
    }

    private ToolPart require(String name) {
        ToolPart part = this.find(name);
        if (part == null) {
            throw this.fail("Unknown part name '" + name + "', declare it with .part(name, partItem) first");
        }
        return part;
    }

    private IllegalArgumentException fail(String message) {
        return new IllegalArgumentException("Tool " + this.id + ": " + message);
    }

    public record ToolPart(String name, ResourceLocation item, ResourceLocation texture) {}
}
