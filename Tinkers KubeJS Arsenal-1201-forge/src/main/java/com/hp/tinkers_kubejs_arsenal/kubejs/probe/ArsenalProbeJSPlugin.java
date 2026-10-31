package com.hp.tinkers_kubejs_arsenal.kubejs.probe;

import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalToolBuilder;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalPartBuilder;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ToolDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalArmorSetBuilder;

import com.google.gson.JsonPrimitive;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerTypes;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialAssetEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialTagEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.AlloyRecipeJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.CastingRecipeJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.MeltingRecipeJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.ModifierRecipeJS;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.features.plugin.DocGenerationEventJS;
import com.probejs.features.plugin.ProbeJSEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import slimeknights.mantle.data.registry.IdAwareComponentRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.common.TierSortingRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import net.minecraftforge.registries.ForgeRegistries;
import com.hp.tinkers_kubejs_arsenal.kubejs.ArsenalTool;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ModifierDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ModifierHookEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ToolStatsEventJS;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;

public class ArsenalProbeJSPlugin extends KubeJSPlugin {
    private static final String STAT_GROUPS = "\"tconstruct:melee_harvest\" | \"tconstruct:ranged\" | \"tconstruct:armor\" | \"tconstruct:ammo\"";
    private static final String FALLBACKS = "\"bone\" | \"cloth\" | \"contrast\" | \"crystal\" | \"metal\" | \"metal_contrast\" | \"primitive\" | \"rock\" | \"slime_metal\" | \"slime_wood\" | \"stick\" | \"vine\" | \"wood\" | (string & {})";
    private static final String PROPERTIES = "{ hardness?: number, density?: number, toughness?: number }";
    private static final String ROUNDING = "\"none\" | \"nearest\" | \"floor\" | \"ceil\"";
    private static final Map<String, String> PARAM_TYPES = Map.ofEntries(
        Map.entry("fluid", "Special.Fluid | `#${Special.FluidTag}`"),
        Map.entry("rate", "\"none\" | \"metal\" | \"gem\" | \"default\""),
        Map.entry("rateOrUnitSize", "\"none\" | \"metal\" | \"gem\" | \"default\" | number"),
        Map.entry("slot", "Special." + TinkerTypes.SLOT_NAME),
        Map.entry("armorSlot", "\"helmet\" | \"chestplate\" | \"leggings\" | \"boots\""),
        Map.entry("armorSet", "string"),
        Map.entry("rarity", "\"common\" | \"uncommon\" | \"rare\" | \"epic\""),
        Map.entry("stat", "Special." + TinkerTypes.MATERIAL_STAT_NAME + " | " + STAT_GROUPS),
        Map.entry("modifier", "Special." + TinkerTypes.MODIFIER_NAME),
        Map.entry("fallbacks", "(" + FALLBACKS + ")[]"),
        Map.entry("lang", "\"en_us\" | \"zh_cn\" | (string & {})"),
        Map.entry("supportedStats", "(Special." + TinkerTypes.MATERIAL_STAT_NAME + " | (string & {}))[]"),
        Map.entry("generator", "Record<string, any>"),
        Map.entry("palette", "Record<string, string>"),
        Map.entry("material", "Special." + TinkerTypes.MATERIAL_NAME),
        Map.entry("toolStat", "Special." + TinkerTypes.TOOL_STAT_NAME),
        Map.entry("statOperation", "\"add\" | \"percent\" | \"multiply_base\" | \"multiply_conditional\" | \"multiply_all\""),
        Map.entry("attribute", "Special." + TinkerTypes.ATTRIBUTE_NAME),
        Map.entry("attributeOperation", "\"addition\" | \"multiply_base\" | \"multiply_total\""),
        Map.entry("levelDisplay", "\"default\" | \"single_level\" | \"no_levels\" | \"pluses\""),
        Map.entry("sprites", "(\"tconstruct:gui/modifiers/book\" | \"tconstruct:gui/modifiers/tier\" | `minecraft:item/${string}` | (string & {}))[]"),
        Map.entry("tooltipDisplay", "\"always\" | \"tinker_station\" | \"never\""),
        Map.entry("miningTier", "Special." + TinkerTypes.HARVEST_TIER_NAME),
        Map.entry("slotType", "Special." + TinkerTypes.SLOT_NAME),
        Map.entry("direction", "\"pitch\" | \"height\" | \"side_hit\""),
        Map.entry("partItem", "Special." + TinkerTypes.TOOL_PART_NAME),
        Map.entry("operation", "\"base\" | \"add\" | \"percent\" | \"multiply\""),
        Map.entry("statType", "\"head\" | \"handle\" | \"binding\" | \"limb\" | \"grip\" | \"bowstring\" | \"plating_helmet\" | \"plating_chestplate\" | \"plating_leggings\" | \"plating_boots\" | \"plating_shield\" | \"maille\" | \"skull\" | \"arrow_head\" | \"arrow_shaft\" | \"fletching\" | Special." + TinkerTypes.MATERIAL_STAT_NAME + " | (string & {})"),
        Map.entry("toolActions", "(\"pickaxe_dig\" | \"axe_dig\" | \"shovel_dig\" | \"hoe_dig\" | \"sword_dig\" | \"shears_dig\" | \"axe_strip\" | \"shovel_flatten\" | \"hoe_till\" | \"sword_sweep\" | \"shield_block\" | (string & {}))[]"),
        Map.entry("blockTag", "Special.BlockTag | `#${Special.BlockTag}`"),
        Map.entry("options", "Record<string, any>"),
        Map.entry("formulaStat", "\"head\" | \"handle\" | \"limb\" | \"grip\" | \"plating_helmet\" | \"plating_chestplate\" | \"plating_leggings\" | \"plating_boots\" | \"plating_shield\" | Special." + TinkerTypes.MATERIAL_STAT_NAME + " | (string & {})"),
        Map.entry("excludedStats", "(Special." + TinkerTypes.MATERIAL_STAT_NAME + " | (string & {}))[]"),
        Map.entry("properties", PROPERTIES + " & Record<string, number>"),
        Map.entry("coefficients", "{ base?: number, min?: number, max?: number, round?: " + ROUNDING + " } & " + PROPERTIES + " & Record<string, number | string>"),
        Map.entry("round", ROUNDING),
        Map.entry("field", "\"durability\" | \"melee_attack\" | \"melee_damage\" | \"melee_speed\" | \"mining_speed\" | \"mining_tier\" | \"accuracy\" | \"draw_speed\" | \"velocity\" | \"armor\" | \"toughness\" | \"knockback_resistance\" | (string & {})"),
        Map.entry("levels", "[number, any][]"),
        Map.entry("modifiable", "(\"aoe\" | \"bonus_slots\" | \"broad\" | \"durability\" | \"harvest\" | \"harvest/primary\" | \"harvest/stone\" | \"held\" | \"interactable\" | \"interactable/dual\" | \"interactable/left\" | \"interactable/right\" | \"melee\" | \"melee/parry\" | \"melee/primary\" | \"melee/sword\" | \"melee/weapon\" | \"multipart\" | \"small\" | \"special\" | \"unsalvageable\" | \"unswappable\" | (string & {}))[]")
    );

    @Override
    public void onServerReload() {
        ProbeJSEvents.DOC_GEN.listenJava(ScriptType.SERVER, null, event -> {
            DocGenerationEventJS doc = (DocGenerationEventJS) event;
            doc.specialType(TinkerTypes.MATERIAL_NAME, literals(MaterialRegistry.getMaterials().stream().map(material -> material.getIdentifier().toString()), true));
            doc.specialType(TinkerTypes.MATERIAL_STAT_NAME, literals(MaterialRegistry.getInstance().getStatTypeLoader() instanceof IdAwareComponentRegistry<?> registry
                ? registry.getKeys().stream().map(Object::toString) : Stream.empty(), false));
            doc.specialType(TinkerTypes.HARVEST_TIER_NAME, literals(TierSortingRegistry.getSortedTiers().stream().map(TierSortingRegistry::getName).filter(Objects::nonNull).map(Object::toString), false));
            doc.specialType(TinkerTypes.MODIFIER_NAME, literals(ModifierManager.INSTANCE.getAllLocations().map(Object::toString), true));
            doc.specialType(TinkerTypes.PATTERN_NAME, literals(patterns(), true));
            doc.specialType(TinkerTypes.SLOT_NAME, literals(SlotType.getAllSlotTypes().stream().map(SlotType::getName), false));
            doc.specialType(TinkerTypes.TOOL_PART_NAME, literals(items(item -> item instanceof IToolPart), false));
            doc.specialType(TinkerTypes.MATERIAL_ITEM_NAME, literals(items(item -> item instanceof IMaterialItem), false));
            doc.specialType(TinkerTypes.MODIFIABLE_NAME, literals(items(item -> item instanceof IModifiable), false));
            doc.specialType(TinkerTypes.TOOL_STAT_NAME, literals(ToolStats.getAllStats().stream().map(stat -> stat.getName().toString()), true));
            doc.specialType(TinkerTypes.ATTRIBUTE_NAME, literals(ForgeRegistries.ATTRIBUTES.getKeys().stream().map(Object::toString), true));
            for (Class<?> recipeClass : List.of(MeltingRecipeJS.class, CastingRecipeJS.class, AlloyRecipeJS.class, ModifierRecipeJS.class, MaterialDataEventJS.MaterialDefinition.class, MaterialDataEventJS.StatFormulaDefinition.class, MaterialDataEventJS.class, MaterialAssetEventJS.MaterialAsset.class, MaterialTagEventJS.MaterialTag.class, ModifierDataEventJS.ModifierDefinition.class, ToolStatsEventJS.class, ModifierHookEvents.AttributesEventJS.class, ArsenalTool.class, ArsenalToolBuilder.class, ArsenalArmorSetBuilder.class, ArsenalArmorSetBuilder.Event.class, ArsenalPartBuilder.class, com.hp.tinkers_kubejs_arsenal.kubejs.material.StatTypeEventJS.StatTypeDefinition.class, ToolDataEventJS.ToolDefinitionJS.class)) {
                doc.transformDocument(recipeClass, ArsenalProbeJSPlugin::describeParams);
            }
            return null;
        });
    }

    private static void describeParams(DocumentClass document) {
        List<DocumentMethod> methods = document.getMethods().stream()
            .filter(method -> method.getParams().stream().anyMatch(param -> PARAM_TYPES.containsKey(param.getName())))
            .toList();
        document.getMethods().removeAll(methods);
        for (DocumentMethod method : methods) {
            List<PropertyParam> params = method.getParams();
            for (int i = 0; i < params.size(); i++) {
                String type = PARAM_TYPES.get(params.get(i).getName());
                if (type != null) {
                    params.set(i, new PropertyParam(params.get(i).getName(), new PropertyType.Native(type), params.get(i).isVarArg()));
                }
            }
        }
        document.getMethods().addAll(methods);
    }

    private static Stream<String> patterns() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return Stream.empty();
        }
        return server.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.PART_BUILDER.get()).stream()
            .map(IPartBuilderRecipe::getPattern)
            .filter(Objects::nonNull)
            .map(Object::toString);
    }

    private static Stream<String> items(Predicate<Item> filter) {
        return BuiltInRegistries.ITEM.stream().filter(filter).map(item -> BuiltInRegistries.ITEM.getKey(item).toString());
    }

    private static List<Object> literals(Stream<String> ids, boolean open) {
        List<Object> list = new ArrayList<>(ids.distinct().sorted().map(id -> new JsonPrimitive(id).toString()).toList());
        if (open || list.isEmpty()) {
            list.add("(string & {})");
        }
        return list;
    }
}


