package com.espmod.client.mixin;

import com.espmod.client.BlockScanner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    // Unlike sendBlockUpdated, this runs for changed states regardless of the notification flags.
    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void espOnBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        BlockScanner.onBlockChanged(pos, oldState, newState);
    }
}
