package com.espmod.client.mixin;

import com.espmod.client.EntityESPConfig;
import com.espmod.client.EntityESPEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void forceGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!EntityESPConfig.enabled || !EntityESPConfig.showOutline) return;

        Entity self = (Entity) (Object) this;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || self == client.player || self.level() != client.level) return;

        EntityESPEntry entry = EntityESPConfig.getEntityLookup().get(self.getType());
        if (entry != null && entry.enabled) cir.setReturnValue(true);
    }

    @Inject(method = "isInvisible", at = @At("RETURN"), cancellable = true)
    private void espOverrideInvisible(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!EntityESPConfig.enabled || !EntityESPConfig.showOutline) return;

        Entity self = (Entity) (Object) this;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || self == client.player || self.level() != client.level) return;

        EntityESPEntry entry = EntityESPConfig.getEntityLookup().get(self.getType());
        if (entry != null && entry.enabled) cir.setReturnValue(false);
    }
}
