package com.hp.tinkers_kubejs_arsenal.kubejs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

public final class ArsenalTool {
    private static final List<EquipmentSlot> ARMOR = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private ArsenalTool() {}

    public static boolean isTool(ItemStack item) {
        return !item.isEmpty() && item.is(TinkerTags.Items.MODIFIABLE);
    }

    public static int modifierLevel(ItemStack item, String modifier) {
        return ModifierUtil.getModifierLevel(item, new ModifierId(modifier));
    }

    public static boolean hasModifier(ItemStack item, String modifier) {
        return modifierLevel(item, modifier) > 0;
    }

    public static int armorModifierLevel(LivingEntity entity, String modifier) {
        ModifierId id = new ModifierId(modifier);
        int level = 0;
        for (EquipmentSlot slot : ARMOR) {
            level += ModifierUtil.getModifierLevel(entity.getItemBySlot(slot), id);
        }
        return level;
    }

    public static int projectileModifierLevel(Entity projectile, String modifier) {
        return EntityModifierCapability.getOrEmpty(projectile).getLevel(new ModifierId(modifier));
    }

    public static int getInt(ItemStack item, String key) {
        return isTool(item) ? ToolStack.from(item).getPersistentData().getInt(new ResourceLocation(key)) : 0;
    }

    public static void setInt(ItemStack item, String key, int value) {
        if (isTool(item)) {
            ToolStack.from(item).getPersistentData().putInt(new ResourceLocation(key), value);
        }
    }

    public static float getFloat(ItemStack item, String key) {
        return isTool(item) ? ToolStack.from(item).getPersistentData().getFloat(new ResourceLocation(key)) : 0;
    }

    public static void setFloat(ItemStack item, String key, float value) {
        if (isTool(item)) {
            ToolStack.from(item).getPersistentData().putFloat(new ResourceLocation(key), value);
        }
    }

    public static void rebuild(ItemStack item) {
        if (isTool(item)) {
            ToolStack.from(item).rebuildStats();
        }
    }
}
