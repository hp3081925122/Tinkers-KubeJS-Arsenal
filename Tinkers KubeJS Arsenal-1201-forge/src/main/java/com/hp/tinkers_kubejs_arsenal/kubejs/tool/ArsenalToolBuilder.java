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
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.item.armor.DummyArmorMaterial;
import slimeknights.tconstruct.library.tools.item.armor.MultilayerArmorItem;
import slimeknights.tconstruct.tools.item.ModifiableSwordItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArsenalToolBuilder extends ItemBuilder {
    public static final String TOOL_TYPE = "tinkers_kubejs_arsenal:tool";
    public static final String SWORD_TYPE = "tinkers_kubejs_arsenal:sword";
    public static final String BOW_TYPE = "tinkers_kubejs_arsenal:bow";
    public static final String CROSSBOW_TYPE = "tinkers_kubejs_arsenal:crossbow";
    public static final String ARMOR_TYPE = "tinkers_kubejs_arsenal:armor";
    public static final Map<ResourceLocation, ArsenalToolBuilder> TOOLS = new LinkedHashMap<>();
    private static final List<String> BOOK_PAGES = List.of("materials_and_you/%s/tools", "mighty_smelting/%s/tools", "encyclopedia/%s/tools/small", "encyclopedia/%s/tools/broad", "encyclopedia/%s/tools/ranged", "encyclopedia/%s/tools/ancient", "encyclopedia/%s/tools/staff", "tinkers_gadgetry/%s/staffs", "tinkers_gadgetry/%s/ancient");

    public enum Kind { TOOL, SWORD, BOW, CROSSBOW, ARMOR }

    private final Kind kind;
    private final List<ToolPart> parts = new ArrayList<>();
    private final Set<String> brokenParts = new LinkedHashSet<>();
    private final Set<String> pullFrames = new LinkedHashSet<>();
    private final Map<String, String> names = new LinkedHashMap<>();
    private final Map<String, List<String>> bookTexts = new LinkedHashMap<>();
    private final Map<String, List<String>> bookProperties = new LinkedHashMap<>();
    private final Set<String> armorBooks = new LinkedHashSet<>();
    private String armorSlot;
    private ResourceLocation armorSet;
    private ResourceLocation equipSound = new ResourceLocation("minecraft:item.armor.equip_iron");

    public ArsenalToolBuilder(ResourceLocation id, boolean sword) {
        this(id, sword ? Kind.SWORD : Kind.TOOL);
    }

    public ArsenalToolBuilder(ResourceLocation id, Kind kind) {
        super(id);
        this.kind = kind;
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

    public ArsenalToolBuilder pullFrames(String... names) {
        for (String name : names) {
            this.require(name);
            this.pullFrames.add(name);
        }
        return this;
    }

    public ArsenalToolBuilder slot(String armorSlot) {
        if (this.kind != Kind.ARMOR) {
            throw this.fail(".slot(type) is only available on armor items");
        }
        if (!Set.of("helmet", "chestplate", "leggings", "boots").contains(armorSlot)) {
            throw this.fail("Unknown armor slot '" + armorSlot + "'");
        }
        this.armorSlot = armorSlot;
        return this;
    }

    public ArsenalToolBuilder armorSet(String set) {
        if (this.kind != Kind.ARMOR) {
            throw this.fail(".armorSet(id) is only available on armor items");
        }
        this.armorSet = new ResourceLocation(set);
        return this;
    }

    public ArsenalToolBuilder equipSound(String sound) {
        if (this.kind != Kind.ARMOR) {
            throw this.fail(".equipSound(sound) is only available on armor items");
        }
        this.equipSound = new ResourceLocation(sound);
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

    public ArsenalToolBuilder bookArmor(String book) {
        if (this.kind != Kind.ARMOR) {
            throw this.fail(".bookArmor(book) is only available on armor items");
        }
        this.armorBooks.add(book);
        return this;
    }

    public List<ToolPart> parts() {
        return List.copyOf(this.parts);
    }

    public List<ToolPart> logicalParts() {
        if (this.kind != Kind.BOW) {
            return List.copyOf(this.parts);
        }
        List<ToolPart> result = new ArrayList<>();
        for (ToolPart part : this.parts) {
            if (part.name().equals("limb")) {
                result.add(part);
                result.add(part);
            } else {
                result.add(part);
            }
        }
        return List.copyOf(result);
    }

    public Kind kind() {
        return this.kind;
    }

    public Set<String> pullFrames() {
        return Set.copyOf(this.pullFrames);
    }

    public String armorSlot() {
        return this.armorSlot;
    }

    public ResourceLocation armorSet() {
        return this.armorSet;
    }

    public ResourceLocation equipSound() {
        return this.equipSound;
    }

    public Set<String> armorBooks() {
        return Set.copyOf(this.armorBooks);
    }

    public boolean isBroken(String name) {
        return this.brokenParts.contains(name);
    }

    @Override
    public Item createObject() {
        if (this.parts.isEmpty()) {
            throw this.fail("Tool has no parts; add them with .part(name, partItem)");
        }
        if (this.kind == Kind.ARMOR && (this.armorSlot == null || this.armorSet == null)) {
            throw this.fail("Armor items require .slot(type) and .armorSet(id)");
        }
        ToolDefinition definition = ToolDefinition.create(this.id);
        return switch (this.kind) {
            case SWORD -> new ModifiableSwordItem(this.createItemProperties(), definition);
            case BOW -> new ModifiableBowItem(this.createItemProperties(), definition);
            case CROSSBOW -> new ModifiableCrossbowItem(this.createItemProperties(), definition);
            case ARMOR -> {
                ArmorItem.Type type = switch (this.armorSlot) {
                    case "helmet" -> ArmorItem.Type.HELMET;
                    case "chestplate" -> ArmorItem.Type.CHESTPLATE;
                    case "leggings" -> ArmorItem.Type.LEGGINGS;
                    case "boots" -> ArmorItem.Type.BOOTS;
                    default -> throw this.fail("Unknown armor slot '" + this.armorSlot + "'");
                };
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(this.equipSound);
                yield new MultilayerArmorItem(new DummyArmorMaterial(this.armorSet, sound == null ? SoundEvents.ARMOR_EQUIP_IRON : sound), type, this.createItemProperties(), definition, this.armorSet);
            }
            default -> new ModifiableItem(this.createItemProperties(), definition);
        };
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (!this.parts.isEmpty()) {
            this.modelJson = this.toolModel(false);
            if (!this.brokenParts.isEmpty()) {
                generator.json(new ResourceLocation(this.id.getNamespace(), "models/" + this.brokenModel().getPath()), this.toolModel(true));
            }
            if (this.kind == Kind.BOW || this.kind == Kind.CROSSBOW) {
                for (int frame = 1; frame <= 3; frame++) {
                    generator.json(new ResourceLocation(this.id.getNamespace(), "models/" + this.frameModel(frame).getPath()), this.rangedFrameModel(frame));
                }
                generator.json(new ResourceLocation(this.id.getNamespace(), "models/" + this.blockingModel().getPath()), this.blockingModelJson(0));
                for (int frame = 1; frame <= 3; frame++) {
                    generator.json(new ResourceLocation(this.id.getNamespace(), "models/" + this.blockingModel(frame).getPath()), this.blockingModelJson(frame));
                }
            }
        }
        super.generateAssetJsons(generator);
        if (this.kind == Kind.ARMOR && this.armorSet != null) {
            ArsenalArmorSetBuilder set = ArsenalArmorSetBuilder.SETS.get(this.armorSet);
            if (set != null) {
                generator.json(new ResourceLocation(this.armorSet.getNamespace(), "tinkering/armor_models/" + this.armorSet.getPath()), set.modelJson());
            }
        }
        Set<String> langs = new LinkedHashSet<>(this.bookTexts.keySet());
        langs.addAll(this.bookProperties.keySet());
        String file = this.id.getNamespace() + "_" + this.id.getPath().replace('/', '_');
        for (String lang : langs) {
            JsonObject page = this.bookPage(lang);
            for (String folder : BOOK_PAGES) {
                generator.json(new ResourceLocation("tconstruct", "book/" + folder.formatted(lang) + "/" + file), page);
            }
        }
        if (this.kind == Kind.ARMOR) {
            for (String lang : langs) {
                JsonObject page = this.bookPage(lang);
                Set<String> books = new LinkedHashSet<>(this.armorBooks);
                books.add("encyclopedia");
                for (String book : books) {
                    String folder = "encyclopedia".equals(book) ? "encyclopedia" : book;
                    generator.json(new ResourceLocation("tconstruct", "book/" + folder + "/" + lang + "/armor/" + file), page);
                }
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
        String defaultParent = switch (this.kind) {
            case SWORD -> "forge:item/default-tool";
            case BOW -> "tconstruct:item/base/bow";
            case CROSSBOW -> "tconstruct:item/base/crossbow";
            case ARMOR -> "forge:item/default";
            default -> "tconstruct:item/base/axe";
        };
        json.addProperty("parent", this.parentModel.isEmpty() ? defaultParent : this.parentModel);
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
        if (this.kind == Kind.BOW) {
            JsonObject bowTextures = json.getAsJsonObject("textures");
            ToolPart limb = this.require("limb");
            bowTextures.remove("limb");
            bowTextures.addProperty("limb_bottom", limb.texture().toString());
            bowTextures.addProperty("limb_top", limb.texture().toString());
            bowTextures.addProperty("grip", this.require("grip").texture().toString());
            bowTextures.addProperty("bowstring", this.require("bowstring").texture().toString());
            JsonArray bowParts = new JsonArray();
            JsonObject bottom = new JsonObject();
            bottom.addProperty("name", "limb_bottom");
            bottom.addProperty("index", 0);
            bowParts.add(bottom);
            JsonObject top = new JsonObject();
            top.addProperty("name", "limb_top");
            top.addProperty("index", 1);
            bowParts.add(top);
            JsonObject string = new JsonObject();
            string.addProperty("name", "bowstring");
            string.addProperty("index", 3);
            bowParts.add(string);
            JsonObject grip = new JsonObject();
            grip.addProperty("name", "grip");
            grip.addProperty("index", 2);
            bowParts.add(grip);
            partList = bowParts;
            json.addProperty("large", false);
            JsonObject ammo = new JsonObject();
            ammo.addProperty("key", "tconstruct:drawback_ammo");
            ammo.addProperty("flip", true);
            ammo.addProperty("left", true);
            JsonArray offset = new JsonArray();
            offset.add(-3.0);
            offset.add(-4.0);
            ammo.add("offset", offset.deepCopy());
            ammo.add("small_offset", offset);
            JsonArray largeOffset = new JsonArray();
            largeOffset.add(-2.0);
            largeOffset.add(-2.0);
            ammo.add("large_offset", largeOffset);
            json.add("ammo", ammo);
            JsonArray maps = new JsonArray();
            maps.add("tconstruct:longbow");
            json.add("modifier_maps", maps);
            JsonArray bowRoots = new JsonArray();
            bowRoots.add(root);
            json.add("modifier_roots", bowRoots);
        } else if (this.kind == Kind.CROSSBOW) {
            JsonArray maps = new JsonArray();
            maps.add("tconstruct:crossbow");
            json.add("modifier_maps", maps);
        } else if (this.kind == Kind.ARMOR) {
            JsonArray maps = new JsonArray();
            maps.add("tconstruct:armor/" + this.armorSlot);
            json.add("modifier_maps", maps);
        }
        json.add("parts", partList);
        if (!broken) {
            JsonArray overrides = new JsonArray();
            if (this.kind == Kind.BOW || this.kind == Kind.CROSSBOW) {
                for (int frame = 1; frame <= 3; frame++) {
                    JsonObject predicate = new JsonObject();
                    predicate.addProperty("tconstruct:charging", 1);
                    predicate.addProperty("tconstruct:charge", this.chargeValue(frame));
                    JsonObject override = new JsonObject();
                    override.add("predicate", predicate);
                    override.addProperty("model", this.frameModel(frame).toString());
                    overrides.add(override);
                }
                JsonObject blocking = new JsonObject();
                JsonObject blockingPredicate = new JsonObject();
                blockingPredicate.addProperty("tconstruct:charging", 2);
                blocking.add("predicate", blockingPredicate);
                blocking.addProperty("model", this.blockingModel().toString());
                overrides.add(blocking);
                for (int frame = 1; frame <= 3; frame++) {
                    JsonObject predicate = new JsonObject();
                    predicate.addProperty("tconstruct:charging", 2);
                    predicate.addProperty("tconstruct:charge", this.chargeValue(frame));
                    JsonObject override = new JsonObject();
                    override.add("predicate", predicate);
                    override.addProperty("model", this.blockingModel(frame).toString());
                    overrides.add(override);
                }
                if (this.kind == Kind.CROSSBOW) {
                    JsonObject ammo = new JsonObject();
                    JsonObject ammoPredicate = new JsonObject();
                    ammoPredicate.addProperty("tconstruct:ammo", 1);
                    ammo.add("predicate", ammoPredicate);
                    ammo.addProperty("model", this.frameModel(3).toString());
                    overrides.add(ammo);
                    JsonObject ammoBlocking = new JsonObject();
                    JsonObject ammoBlockingPredicate = new JsonObject();
                    ammoBlockingPredicate.addProperty("tconstruct:ammo", 1);
                    ammoBlockingPredicate.addProperty("tconstruct:charging", 2);
                    ammoBlocking.add("predicate", ammoBlockingPredicate);
                    ammoBlocking.addProperty("model", this.blockingModel(3).toString());
                    overrides.add(ammoBlocking);
                }
            }
            if (!this.brokenParts.isEmpty()) {
                JsonObject predicate = new JsonObject();
                predicate.addProperty("tconstruct:broken", 1);
                JsonObject override = new JsonObject();
                override.add("predicate", predicate);
                override.addProperty("model", this.brokenModel().toString());
                overrides.add(override);
            }
            if (!overrides.isEmpty()) {
                json.add("overrides", overrides);
            }
        }
        return json;
    }

    private JsonObject rangedFrameModel(int frame) {
        return this.toolModelFrame(frame, false);
    }

    private JsonObject toolModelFrame(int frame, boolean broken) {
        JsonObject json = this.toolModel(broken);
        json.remove("overrides");
        JsonObject textures = json.getAsJsonObject("textures");
        if (this.kind == Kind.BOW) {
            ResourceLocation limb = this.require("limb").texture();
            ResourceLocation string = this.require("bowstring").texture();
            textures.addProperty("limb_bottom", limb.withSuffix("_" + frame).toString());
            textures.addProperty("limb_top", limb.withSuffix("_" + frame).toString());
            textures.addProperty("bowstring", string.withSuffix("_" + frame).toString());
        }
        for (ToolPart part : this.parts) {
            if (this.kind != Kind.BOW && this.pullFrames.contains(part.name()) && !broken) {
                textures.addProperty(part.name(), part.texture().withSuffix("_" + frame).toString());
            }
        }
        JsonArray roots = new JsonArray();
        String root = this.id.getNamespace() + ":item/tool/" + this.id.getPath() + "/modifiers/";
        if (this.kind == Kind.BOW) {
            JsonArray bowRoots = new JsonArray();
            bowRoots.add(root + frame + "/");
            bowRoots.add(root);
            json.add("modifier_roots", bowRoots);
            JsonArray maps = new JsonArray();
            maps.add("tconstruct:longbow/" + frame);
            maps.add("tconstruct:longbow");
            json.add("modifier_maps", maps);
        } else {
            roots.add(root + frame + "/");
            roots.add(root);
            json.add("modifier_roots", roots);
            JsonArray maps = new JsonArray();
            maps.add("tconstruct:crossbow/" + frame);
            maps.add("tconstruct:crossbow");
            json.add("modifier_maps", maps);
        }
        return json;
    }

    private JsonObject blockingModelJson(int frame) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", frame == 0 ? this.id.getNamespace() + ":item/" + this.id.getPath() : this.frameModel(frame).toString());
        JsonObject display = new JsonObject();
        JsonObject right = new JsonObject();
        JsonArray rightRotation = new JsonArray();
        rightRotation.add(0);
        rightRotation.add(180);
        rightRotation.add(-5);
        right.add("rotation", rightRotation);
        JsonArray rightTranslation = new JsonArray();
        rightTranslation.add(-5);
        rightTranslation.add(-2);
        rightTranslation.add(-0.2);
        right.add("translation", rightTranslation);
        JsonArray scale = new JsonArray();
        scale.add(1);
        scale.add(1);
        scale.add(1);
        right.add("scale", scale);
        display.add("firstperson_righthand", right);
        JsonObject left = new JsonObject();
        JsonArray leftRotation = new JsonArray();
        leftRotation.add(0);
        leftRotation.add(0);
        leftRotation.add(5);
        left.add("rotation", leftRotation);
        left.add("translation", rightTranslation.deepCopy());
        left.add("scale", scale.deepCopy());
        display.add("firstperson_lefthand", left);
        json.add("display", display);
        return json;
    }

    private String baseRangedModel() {
        return this.kind == Kind.BOW ? "tconstruct:item/longbow" : "tconstruct:item/crossbow";
    }

    private double chargeValue(int frame) {
        return frame == 1 ? 0.05 : frame == 2 ? (this.kind == Kind.CROSSBOW ? 0.58 : 0.6) : 1.0;
    }

    private ResourceLocation frameModel(int frame) {
        return new ResourceLocation(this.id.getNamespace(), "item/tool/" + this.id.getPath() + "/pulling_" + frame);
    }

    private ResourceLocation blockingModel() {
        return this.blockingModel(0);
    }

    private ResourceLocation blockingModel(int frame) {
        return new ResourceLocation(this.id.getNamespace(), "item/tool/" + this.id.getPath() + "/blocking" + (frame == 0 ? "" : "_" + frame));
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
