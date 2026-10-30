package com.hp.tinkers_kubejs_arsenal.kubejs.probe;

import com.google.gson.JsonPrimitive;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerTypes;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialAssetEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialDataEventJS;
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
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;

public class ArsenalProbeJSPlugin extends KubeJSPlugin {
    private static final String STAT_GROUPS = "\"tconstruct:melee_harvest\" | \"tconstruct:ranged\" | \"tconstruct:armor\" | \"tconstruct:ammo\"";
    private static final String FALLBACKS = "\"bone\" | \"cloth\" | \"contrast\" | \"crystal\" | \"metal\" | \"metal_contrast\" | \"primitive\" | \"rock\" | \"slime_metal\" | \"slime_wood\" | \"stick\" | \"vine\" | \"wood\" | (string & {})";
    private static final Map<String, String> PARAM_TYPES = Map.ofEntries(
        Map.entry("fluid", "Special.Fluid | `#${Special.FluidTag}`"),
        Map.entry("rate", "\"none\" | \"metal\" | \"gem\" | \"default\""),
        Map.entry("rateOrUnitSize", "\"none\" | \"metal\" | \"gem\" | \"default\" | number"),
        Map.entry("slot", "Special." + TinkerTypes.SLOT_NAME),
        Map.entry("headStats", "{durability: number, mining_speed?: number, mining_tier?: Special." + TinkerTypes.HARVEST_TIER_NAME + ", melee_attack?: number}"),
        Map.entry("handleStats", "{durability?: number, mining_speed?: number, melee_speed?: number, melee_damage?: number}"),
        Map.entry("gripStats", "{durability?: number, accuracy?: number, melee_damage?: number}"),
        Map.entry("limbStats", "{durability: number, draw_speed?: number, velocity?: number, accuracy?: number}"),
        Map.entry("platingStats", "{durability: number, armor?: number, toughness?: number, knockback_resistance?: number}"),
        Map.entry("shieldStats", "{durability: number, toughness?: number, knockback_resistance?: number}"),
        Map.entry("skullStats", "{durability: number, armor?: number}"),
        Map.entry("slimeStats", "{durability: number, overslime_capacity: number}"),
        Map.entry("repairStats", "{repair_amount: number}"),
        Map.entry("rarity", "\"common\" | \"uncommon\" | \"rare\" | \"epic\""),
        Map.entry("stat", "Special." + TinkerTypes.MATERIAL_STAT_NAME + " | " + STAT_GROUPS),
        Map.entry("modifier", "Special." + TinkerTypes.MODIFIER_NAME),
        Map.entry("fallbacks", "(" + FALLBACKS + ")[]"),
        Map.entry("lang", "\"en_us\" | \"zh_cn\" | (string & {})")
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
            for (Class<?> recipeClass : List.of(MeltingRecipeJS.class, CastingRecipeJS.class, AlloyRecipeJS.class, ModifierRecipeJS.class, MaterialDataEventJS.MaterialDefinition.class, MaterialAssetEventJS.MaterialAsset.class)) {
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


