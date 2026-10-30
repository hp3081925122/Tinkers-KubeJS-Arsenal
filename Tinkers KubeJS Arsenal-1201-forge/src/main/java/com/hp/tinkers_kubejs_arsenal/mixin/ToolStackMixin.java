package com.hp.tinkers_kubejs_arsenal.mixin;

import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ArsenalToolEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ToolStatsEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

@Mixin(value = ToolStack.class, remap = false)
public abstract class ToolStackMixin {
    @Redirect(method = "rebuildStats", at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/tools/stat/ModifierStatsBuilder;build()Lslimeknights/tconstruct/library/tools/nbt/StatsNBT;"))
    private StatsNBT tkja$stats(ModifierStatsBuilder builder) {
        if (ArsenalToolEvents.STATS.hasListeners()) {
            ArsenalToolEvents.STATS.post(ScriptType.SERVER, new ToolStatsEventJS((ToolStack) (Object) this, null, builder));
        }
        return builder.build();
    }
}
