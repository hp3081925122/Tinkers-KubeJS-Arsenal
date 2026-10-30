package com.hp.tinkers_kubejs_arsenal.kubejs.probe;

import com.google.gson.JsonPrimitive;
import com.hp.tinkers_kubejs_arsenal.kubejs.component.TinkerTypes;
import com.hp.tinkers_kubejs_arsenal.kubejs.recipe.MeltingRecipeJS;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.features.plugin.DocGenerationEventJS;
import com.probejs.features.plugin.ProbeJSEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import java.util.ArrayList;
import java.util.List;
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
    @Override
    public void onServerReload() {
        ProbeJSEvents.DOC_GEN.listenJava(ScriptType.SERVER, null, event -> {
            DocGenerationEventJS doc = (DocGenerationEventJS) event;
            doc.specialType(TinkerTypes.MATERIAL_NAME, literals(MaterialRegistry.getMaterials().stream().map(material -> material.getIdentifier().toString()), true));
            doc.specialType(TinkerTypes.MODIFIER_NAME, literals(ModifierManager.INSTANCE.getAllLocations().map(Object::toString), true));
            doc.specialType(TinkerTypes.PATTERN_NAME, literals(patterns(), true));
            doc.specialType(TinkerTypes.SLOT_NAME, literals(SlotType.getAllSlotTypes().stream().map(SlotType::getName), false));
            doc.specialType(TinkerTypes.TOOL_PART_NAME, literals(items(item -> item instanceof IToolPart), false));
            doc.specialType(TinkerTypes.MATERIAL_ITEM_NAME, literals(items(item -> item instanceof IMaterialItem), false));
            doc.specialType(TinkerTypes.MODIFIABLE_NAME, literals(items(item -> item instanceof IModifiable), false));
            doc.transformDocument(MeltingRecipeJS.class, document -> {
                List<DocumentMethod> methods = document.getMethods().stream()
                    .filter(method -> method.getName().equals("result") || method.getName().equals("byproduct"))
                    .toList();
                document.getMethods().removeAll(methods);
                methods.forEach(ArsenalProbeJSPlugin::describeFluidMethod);
                document.getMethods().addAll(methods);
            });
            return null;
        });
    }

    private static void describeFluidMethod(DocumentMethod method) {
        List<PropertyParam> params = method.getParams();
        params.set(0, new PropertyParam("fluid", new PropertyType.Native("Special.Fluid | `#${Special.FluidTag}`"), false));
        params.set(1, new PropertyParam("amount", params.get(1).getType(), false));
        if (params.size() == 3) {
            boolean rate = params.get(2).getType() instanceof PropertyType.Clazz clazz && clazz.getClassName().equals(String.class.getName());
            params.set(2, rate
                ? new PropertyParam("rate", new PropertyType.Native("\"none\" | \"metal\" | \"gem\" | \"default\""), false)
                : new PropertyParam("unitSize", params.get(2).getType(), false));
        }
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
