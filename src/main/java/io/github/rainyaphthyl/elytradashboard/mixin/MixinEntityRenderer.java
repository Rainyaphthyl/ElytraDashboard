package io.github.rainyaphthyl.elytradashboard.mixin;

import com.mumfrey.liteloader.Tickable;
import io.github.rainyaphthyl.elytradashboard.input.KeyRotator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements Tickable {
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
    private void updateRotationKey(CallbackInfo ci) {
        if (mc.player.isElytraFlying()) {
            GameSettings gameSettings = mc.gameSettings;
            elytraDashboard$rotator.updateTickRotation(gameSettings);
            System.err.println("Delta(Rotation): " + elytraDashboard$rotator.getDeltaYaw() + " / " + elytraDashboard$rotator.getDeltaPitch());
        }
    }

    /**
     * Render every frame
     *
     * @param partialTicks Seems in {@code [0.0F, 1.0F)} ?
     */
    @Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V", shift = At.Shift.BEFORE)
    )
    private void rotateByKeyboard(float partialTicks, long nanoTime, CallbackInfo ci) {
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
}
