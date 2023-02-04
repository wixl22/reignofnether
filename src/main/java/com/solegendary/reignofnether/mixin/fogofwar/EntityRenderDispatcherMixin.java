package com.solegendary.reignofnether.mixin.fogofwar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "renderFlame",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onRenderFlame(PoseStack pMatrixStack, MultiBufferSource pBuffer, Entity pEntity, CallbackInfo ci) {
        if (!FogOfWarClientEvents.isInBrightChunk(pEntity.getOnPos()))
            ci.cancel();
    }
}
