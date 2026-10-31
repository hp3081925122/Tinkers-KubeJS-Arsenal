package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.List;

public record ScriptMaterialStats(MaterialStatType<ScriptMaterialStats> type, List<Entry> entries, float[] values) implements IMaterialStats {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (StatTypeEventJS.StatTypeDefinition definition : StatTypeEventJS.TYPES.values()) {
                List<Entry> entries = new ArrayList<>();
                for (StatTypeEventJS.Field field : definition.fields()) {
                    IToolStat<?> stat = ToolStats.getToolStat(new ToolStatId(field.toolStat()));
                    if (!(stat instanceof INumericToolStat<?> numeric)) {
                        LOGGER.error("Stat type {} field '{}' uses {}, which is not a numeric Tinkers tool stat; field ignored", definition.id(), field.name(), field.toolStat());
                        continue;
                    }
                    entries.add(new Entry(field, numeric));
                }
                MaterialRegistry.getInstance().registerStatType(createType(definition.id(), List.copyOf(entries)));
                LOGGER.info("Registered material stat type {} with {} fields", definition.id(), entries.size());
            }
        });
    }

    private static MaterialStatType<ScriptMaterialStats> createType(MaterialStatsId id, List<Entry> entries) {
        MaterialStatType<ScriptMaterialStats>[] holder = new MaterialStatType[1];
        RecordLoadable<ScriptMaterialStats> loadable = new RecordLoadable<>() {
            @Override
            public ScriptMaterialStats deserialize(JsonObject json, TypedMap context) {
                float[] values = new float[entries.size()];
                for (int i = 0; i < values.length; i++) {
                    Entry entry = entries.get(i);
                    values[i] = GsonHelper.getAsFloat(json, entry.field().name(), entry.field().defaultValue());
                }
                return new ScriptMaterialStats(holder[0], entries, values);
            }

            @Override
            public void serialize(ScriptMaterialStats stats, JsonObject json) {
                for (int i = 0; i < entries.size(); i++) {
                    json.addProperty(entries.get(i).field().name(), stats.values[i]);
                }
            }

            @Override
            public ScriptMaterialStats decode(FriendlyByteBuf buffer, TypedMap context) {
                float[] values = new float[entries.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = buffer.readFloat();
                }
                return new ScriptMaterialStats(holder[0], entries, values);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, ScriptMaterialStats stats) {
                for (float value : stats.values) {
                    buffer.writeFloat(value);
                }
            }
        };
        float[] defaults = new float[entries.size()];
        for (int i = 0; i < defaults.length; i++) {
            defaults[i] = entries.get(i).field().defaultValue();
        }
        java.util.function.Function<MaterialStatType<ScriptMaterialStats>, ScriptMaterialStats> factory = type -> new ScriptMaterialStats(type, entries, defaults);
        holder[0] = new MaterialStatType<>(id, factory, loadable);
        return holder[0];
    }

    @Override
    public MaterialStatType<?> getType() {
        return this.type;
    }

    @Override
    public List<Component> getLocalizedInfo() {
        return this.getLocalizedInfo(1);
    }

    @Override
    public List<Component> getLocalizedInfo(float scale) {
        List<Component> info = new ArrayList<>();
        for (int i = 0; i < this.entries.size(); i++) {
            Entry entry = this.entries.get(i);
            float value = this.values[i] * scale;
            String key = entry.stat().getTranslationKey();
            info.add(switch (entry.field().operation()) {
                case "add" -> IToolStat.formatColoredBonus(key, value);
                case "percent" -> IToolStat.formatColoredPercentBoost(key, value);
                case "multiply" -> IToolStat.formatColoredMultiplier(key, value);
                default -> entry.stat().formatValue(value);
            });
        }
        return info;
    }

    @Override
    public List<Component> getLocalizedDescriptions() {
        List<Component> descriptions = new ArrayList<>();
        for (Entry entry : this.entries) {
            descriptions.add(entry.stat().getDescription());
        }
        return descriptions;
    }

    @Override
    public void apply(ModifierStatsBuilder builder, float scale) {
        for (int i = 0; i < this.entries.size(); i++) {
            INumericToolStat<?> stat = this.entries.get(i).stat();
            float value = this.values[i] * scale;
            switch (this.entries.get(i).field().operation()) {
                case "add" -> stat.add(builder, value);
                case "percent" -> stat.percent(builder, value);
                case "multiply" -> stat.multiply(builder, value);
                default -> ((INumericToolStat) stat).update(builder, Float.valueOf(value));
            }
        }
    }

    public record Entry(StatTypeEventJS.Field field, INumericToolStat<?> stat) {}
}
