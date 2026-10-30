package com.hp.tinkers_kubejs_arsenal.kubejs.modifier;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class ToolStatsEventJS extends EventJS {
    private final IToolContext tool;
    private final ModifierEntry modifier;
    private final ModifierStatsBuilder builder;

    public ToolStatsEventJS(IToolContext tool, @Nullable ModifierEntry modifier, ModifierStatsBuilder builder) {
        this.tool = tool;
        this.modifier = modifier;
        this.builder = builder;
    }

    public IToolContext getTool() {
        return this.tool;
    }

    public ResourceLocation getItemId() {
        return BuiltInRegistries.ITEM.getKey(this.tool.getItem());
    }

    public boolean hasTag(String tag) {
        return this.tool.hasTag(TagKey.create(Registries.ITEM, new ResourceLocation(tag)));
    }

    public int modifierLevel(String modifier) {
        return this.tool.getModifierLevel(new ModifierId(modifier));
    }

    @Nullable
    public ModifierId getModifier() {
        return this.modifier == null ? null : this.modifier.getId();
    }

    public int getLevel() {
        return this.modifier == null ? 0 : this.modifier.getLevel();
    }

    public float getEffectiveLevel() {
        return this.modifier == null ? 0 : this.modifier.getEffectiveLevel();
    }

    public Object get(String toolStat) {
        return this.builder.getStat(stat(toolStat));
    }

    public void add(String toolStat, double value) {
        numeric(toolStat).add(this.builder, value);
    }

    public void percent(String toolStat, double factor) {
        numeric(toolStat).percent(this.builder, factor);
    }

    public void multiply(String toolStat, double factor) {
        numeric(toolStat).multiply(this.builder, factor);
    }

    public void multiplyAll(String toolStat, double factor) {
        numeric(toolStat).multiplyAll(this.builder, factor);
    }

    private static IToolStat<?> stat(String id) {
        IToolStat<?> stat = ToolStats.getToolStat(new ToolStatId(id));
        if (stat == null) {
            throw new IllegalArgumentException("Unknown tool stat " + id);
        }
        return stat;
    }

    private static INumericToolStat<?> numeric(String id) {
        if (stat(id) instanceof INumericToolStat<?> numeric) {
            return numeric;
        }
        throw new IllegalArgumentException("Tool stat " + id + " is not numeric");
    }
}
