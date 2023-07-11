package io.github.rainyaphthyl.elytradashboard.mixin;

import com.mumfrey.liteloader.Tickable;
import net.minecraft.client.Minecraft;
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
    /**
     * This may not be {@code 8.0F} for keyboard operation
     */
    @Unique
    private static final float elytraDashboard$RATE_ALTERED = 8.0F;
    @Unique
    private static final float elytraDashboard$SCALE_FACTOR = 0.6F;
    @Unique
    private static final float elytraDashboard$SCALE_OFFSET = 0.2F;
    @Unique
    private static final float elytraDashboard$INERTIA = 0.91F;
    @Shadow
    @Final
    private Minecraft mc;
    @Unique
    private float elytraDashboard$deltaYaw = 0.0F;
    @Unique
    private float elytraDashboard$deltaPitch = 0.0F;
    @Unique
    private float elytraDashboard$prevDeltaYaw = 0.0F;
    @Unique
    private float elytraDashboard$prevDeltaPitch = 0.0F;
    @Unique
    private float elytraDashboard$prevSensitivity;
    @Unique
    private float elytraDashboard$prevMouseRate;
    @Unique
    private float elytraDashboard$prevPartialTick = 0.0F;

    {
        elytraDashboard$prevSensitivity = 0.0F;
        float scale = elytraDashboard$prevSensitivity * elytraDashboard$SCALE_FACTOR + elytraDashboard$SCALE_OFFSET;
        elytraDashboard$prevMouseRate = scale * scale * scale * elytraDashboard$RATE_ALTERED;
    }

    /**
     * Render every tick
     */
    @Inject(method = "updateRenderer()V", at = @At("HEAD"))
    private void updateRotationKey(CallbackInfo ci) {
        if (mc.player.isElytraFlying()) {
            elytraDashboard$prevDeltaYaw = elytraDashboard$deltaYaw;
            elytraDashboard$prevDeltaPitch = elytraDashboard$deltaPitch;
            GameSettings gameSettings = mc.gameSettings;
            float rate;
            float sensitivity = gameSettings.mouseSensitivity;
            if (elytraDashboard$prevSensitivity == sensitivity) {
                rate = elytraDashboard$prevMouseRate;
            } else {
                float scale = sensitivity * elytraDashboard$SCALE_FACTOR + elytraDashboard$SCALE_OFFSET;
                rate = scale * scale * scale * elytraDashboard$RATE_ALTERED;
                elytraDashboard$prevMouseRate = rate;
                elytraDashboard$prevSensitivity = sensitivity;
            }
            int keyForward = 0;
            if (gameSettings.keyBindForward.isKeyDown()) {
                ++keyForward;
            }
            if (gameSettings.keyBindBack.isKeyDown()) {
                --keyForward;
            }
            int keyStrafing = 0;
            if (gameSettings.keyBindRight.isKeyDown()) {
                ++keyStrafing;
            }
            if (gameSettings.keyBindLeft.isKeyDown()) {
                --keyStrafing;
            }
            if (keyStrafing != 0 || keyForward != 0) {
                elytraDashboard$deltaYaw += (float) keyStrafing * rate;
                elytraDashboard$deltaPitch += (float) keyForward * rate;
            }
            if (elytraDashboard$deltaYaw != 0.0F) {
                elytraDashboard$deltaYaw *= elytraDashboard$INERTIA;
                if (Math.abs(elytraDashboard$deltaYaw) < 0.003F) {
                    elytraDashboard$deltaYaw = 0.0F;
                }
            }
            if (elytraDashboard$deltaPitch != 0.0F) {
                elytraDashboard$deltaPitch *= elytraDashboard$INERTIA;
                if (Math.abs(elytraDashboard$deltaPitch) < 0.003F) {
                    elytraDashboard$deltaPitch = 0.0F;
                }
            }
            System.err.println("Delta(Rotation): " + elytraDashboard$deltaYaw + " / " + elytraDashboard$deltaPitch);
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
        if (mc.player.isElytraFlying()) {
            int i = 1;
            if (mc.gameSettings.invertMouse) {
                i = -1;
            }
            float partialDYaw;
            float partialDPitch;
            if (elytraDashboard$prevPartialTick < partialTicks) {
                float currFrame = partialTicks - elytraDashboard$prevPartialTick;
                partialDYaw = elytraDashboard$deltaYaw * currFrame;
                partialDPitch = elytraDashboard$deltaPitch * currFrame;
                System.out.printf("Partial %.4f->%.4f: %+.6f/%+.6f\n", elytraDashboard$prevPartialTick, partialTicks,
                        partialDYaw, partialDPitch);
            } else {
                float priorHalf = 1.0F - elytraDashboard$prevPartialTick;
                partialDYaw = elytraDashboard$prevDeltaYaw * priorHalf;
                partialDPitch = elytraDashboard$prevDeltaPitch * priorHalf;
                System.out.printf("Tail %.4f->%.4f: %+.6f/%+.6f\n", elytraDashboard$prevPartialTick, 1.0F,
                        partialDYaw, partialDPitch);
                float postDYaw = elytraDashboard$deltaYaw * partialTicks;
                float postDPitch = elytraDashboard$deltaPitch * partialTicks;
                partialDYaw += postDYaw;
                partialDPitch += postDPitch;
                System.out.printf("Head %.4f->%.4f: %+.6f/%+.6f\n", 0.0F, partialTicks, postDYaw, postDPitch);
            }
            elytraDashboard$prevPartialTick = partialTicks;
            mc.player.turn(partialDYaw, partialDPitch * (float) i);
        }
    }
}
