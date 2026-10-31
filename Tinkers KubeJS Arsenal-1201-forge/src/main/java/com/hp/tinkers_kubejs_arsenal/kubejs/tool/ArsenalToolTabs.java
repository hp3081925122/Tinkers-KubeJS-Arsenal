package com.hp.tinkers_kubejs_arsenal.kubejs.tool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Iterator;
import java.util.Map;

public final class ArsenalToolTabs {
    private static final ResourceLocation KUBEJS_TAB = new ResourceLocation("kubejs", "tab");

    private ArsenalToolTabs() {}

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (ArsenalToolBuilder.TOOLS.isEmpty()) {
            return;
        }
        ResourceLocation tab = event.getTabKey().location();
        if (tab.equals(TinkerTools.tabTools.getId())) {
            for (ArsenalToolBuilder tool : ArsenalToolBuilder.TOOLS.values()) {
                if (tool.get() instanceof IModifiable modifiable) {
                    ToolBuildHandler.addVariants(event::accept, modifiable, "");
                }
            }
        } else if (tab.equals(KUBEJS_TAB)) {
            Iterator<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>> entries = event.getEntries().iterator();
            while (entries.hasNext()) {
                if (ArsenalToolBuilder.TOOLS.containsKey(ForgeRegistries.ITEMS.getKey(entries.next().getKey().getItem()))) {
                    entries.remove();
                }
            }
        }
    }
}
