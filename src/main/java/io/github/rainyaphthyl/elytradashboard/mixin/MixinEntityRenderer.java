package io.github.rainyaphthyl.elytradashboard.mixin;

import io.github.rainyaphthyl.elytradashboard.config.ModSettings;
import io.github.rainyaphthyl.elytradashboard.display.RegFrameUpdaters;
import io.github.rainyaphthyl.elytradashboard.input.KeyRotator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Unique
    private final KeyRotator elytraDashboard$rotator = new KeyRotator();
    @Unique
    private final float[] elytraDashboard$bufferDeltas = new float[2];
    @Shadow
    @Final
    private Minecraft mc;

    /**
     * Render every tick
     */
    @Inject(method = "updateRenderer()V", at = @At("HEAD"))
    private void onTickRenderer(CallbackInfo ci) {
        Profiler profiler = mc.profiler;
        profiler.startSection("elytraKeyInput");
        if (ModSettings.INSTANCE.keyboardElytra && mc.player.isElytraFlying()) {
            GameSettings gameSettings = mc.gameSettings;
            elytraDashboard$rotator.updateTickRotation(gameSettings);
        }
        profiler.endStartSection("tickDashboard");
        Entity renderViewEntity = mc.getRenderViewEntity();
        boolean inGame = renderViewEntity != null && renderViewEntity.world != null;
        RegFrameUpdaters.updateAllOnTick(inGame);
        profiler.endSection();
    }

    /**
     * Render every frame
     *
     * @param partialTicks Seems in {@code [0.0F, 1.0F)} ?
     */
    @Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V", shift = At.Shift.BEFORE)
    )
    private void onPlayerTurn(float partialTicks, long nanoTime, CallbackInfo ci) {
        Profiler profiler = mc.profiler;
        profiler.startSection("elytraKeyRotation");
        if (ModSettings.INSTANCE.keyboardElytra) {
            EntityPlayerSP player = mc.player;
            if (player.isElytraFlying()) {
                int i = 1;
                if (mc.gameSettings.invertMouse) {
                    i = -1;
                }
                float[] partialDeltas = elytraDashboard$rotator.updateFrameRotation(elytraDashboard$bufferDeltas, partialTicks);
                float partialDYaw = partialDeltas[0];
                float partialDPitch = partialDeltas[1];
                player.turn(partialDYaw, partialDPitch * (float) i);
            }
        }
        profiler.endSection();
    }

    @Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    private void onRenderGUI(float partialTicks, long nanoTime, CallbackInfo ci) {
        Profiler profiler = mc.profiler;
        profiler.startSection("renderDashboard");
        Entity renderViewEntity = mc.getRenderViewEntity();
        boolean inGame = renderViewEntity != null && renderViewEntity.world != null;
        RegFrameUpdaters.updateAllOnFrame(partialTicks, inGame);
        profiler.endSection();
    }
}
