package com.espmod.client.mixin;

import com.espmod.client.ESPConfig;
import com.espmod.client.EntitySettings;
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
        if (!ESPConfig.enabled || !ESPConfig.showOutline) return;

        Entity self = (Entity) (Object) this;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || self == client.player) return;

        EntitySettings ov = ESPConfig.getOverride(self);
        if (ov != null && ov.enabled) {
            cir.setReturnValue(true);
        }
    }
}
