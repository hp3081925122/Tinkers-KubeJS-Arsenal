package com.hp.tinkers_kubejs_arsenal.kubejs.tool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Iterator;
import java.util.Map;

public final class ArsenalToolTabs {
    private static final ResourceLocation KUBEJS_TAB = new ResourceLocation("kubejs", "tab");

    private ArsenalToolTabs() {}

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (ArsenalToolBuilder.TOOLS.isEmpty() && ArsenalPartBuilder.PARTS.isEmpty()) {
            return;
        }
        ResourceLocation tab = event.getTabKey().location();
        if (tab.equals(TinkerTools.tabTools.getId())) {
            for (ArsenalToolBuilder tool : ArsenalToolBuilder.TOOLS.values()) {
                if (tool.get() instanceof IModifiable modifiable) {
                    ToolBuildHandler.addVariants(event::accept, modifiable, "");
                }
            }
        } else if (tab.equals(TinkerToolParts.tabToolParts.getId())) {
            for (ArsenalPartBuilder part : ArsenalPartBuilder.PARTS.values()) {
                if (part.get() instanceof IMaterialItem materialItem) {
                    materialItem.addVariants(event::accept, "");
                }
            }
        } else if (tab.equals(TinkerSmeltery.tabSmeltery.getId())) {
            for (ArsenalPartBuilder part : ArsenalPartBuilder.PARTS.values()) {
                part.getCasts().forEach(cast -> event.accept(cast.get()));
            }
        } else if (tab.equals(KUBEJS_TAB)) {
            Iterator<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>> entries = event.getEntries().iterator();
            while (entries.hasNext()) {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(entries.next().getKey().getItem());
                if (ArsenalToolBuilder.TOOLS.containsKey(key) || ArsenalPartBuilder.PARTS.containsKey(key) || ArsenalPartBuilder.CASTS.contains(key)) {
                    entries.remove();
                }
            }
        }
    }
}
