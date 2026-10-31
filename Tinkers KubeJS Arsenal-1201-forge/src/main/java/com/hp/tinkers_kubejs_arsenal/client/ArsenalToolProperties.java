package com.hp.tinkers_kubejs_arsenal.client;

import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalToolBuilder;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;

public final class ArsenalToolProperties {
    private ArsenalToolProperties() {}

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ArsenalToolBuilder tool : ArsenalToolBuilder.TOOLS.values()) {
                Item item = tool.get();
                if (item instanceof ModifiableCrossbowItem) {
                    TinkerItemProperties.registerCrossbowProperties(item);
                } else {
                    TinkerItemProperties.registerToolProperties(item);
                }
            }
        });
    }
}
