package com.hp.tinkers_kubejs_arsenal.kubejs.probe;

import com.google.gson.JsonPrimitive;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerTypes;
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
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;

public class ArsenalProbeJSPlugin extends KubeJSPlugin {
    private static final Map<String, String> PARAM_TYPES = Map.of(
        "fluid", "Special.Fluid | `#${Special.FluidTag}`",
        "rate", "\"none\" | \"metal\" | \"gem\" | \"default\"",
        "slot", "Special." + TinkerTypes.SLOT_NAME
    );

    @Override
    public void onServerReload() {
        ProbeJSEvents.DOC_GEN.listenJava(ScriptType.SERVER, null, event -> {
            DocGenerationEventJS doc = (DocGenerationEventJS) event;
            doc.specialType(TinkerTypes.MATERIAL_NAME, literals(MaterialRegistry.getMaterials().stream().map(material -> material.getIdentifier().toString()), true));
            doc.specialType(TinkerTypes.MATERIAL_STAT_NAME, literals(MaterialRegistry.getInstance().getStatTypeLoader() instanceof IdAwareComponentRegistry<?> registry
                ? registry.getKeys().stream().map(Object::toString) : Stream.empty(), false));
            doc.specialType(TinkerTypes.MODIFIER_NAME, literals(ModifierManager.INSTANCE.getAllLocations().map(Object::toString), true));
            doc.specialType(TinkerTypes.PATTERN_NAME, literals(patterns(), true));
            doc.specialType(TinkerTypes.SLOT_NAME, literals(SlotType.getAllSlotTypes().stream().map(SlotType::getName), false));
            doc.specialType(TinkerTypes.TOOL_PART_NAME, literals(items(item -> item instanceof IToolPart), false));
            doc.specialType(TinkerTypes.MATERIAL_ITEM_NAME, literals(items(item -> item instanceof IMaterialItem), false));
            doc.specialType(TinkerTypes.MODIFIABLE_NAME, literals(items(item -> item instanceof IModifiable), false));
            for (Class<?> recipeClass : List.of(MeltingRecipeJS.class, CastingRecipeJS.class, AlloyRecipeJS.class, ModifierRecipeJS.class)) {
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
                    params.set(i, new PropertyParam(params.get(i).getName(), new PropertyType.Native(type), false));
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
