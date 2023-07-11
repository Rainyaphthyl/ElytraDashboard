package io.github.rainyaphthyl.mixin;

import com.mumfrey.liteloader.Tickable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import org.apache.logging.log4j.Logger;
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
    private static Logger LOGGER;
    @Shadow
    @Final
    private Minecraft mc;
    @Unique
    private float elytraDashboard$yawStrafing = 0.0F;
    @Unique
    private float elytraDashboard$pitchForward = 0.0F;
    @Unique
    private float elytraDashboard$prevSensitivity;
    @Unique
    private float elytraDashboard$prevMouseRate;

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
    }

    /**
     * Render every frame
     */
    @Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V", shift = At.Shift.NONE)
    )
    private void rotateByKeyboard(float partialTicks, long nanoTime, CallbackInfo ci) {
        if (mc.player.isElytraFlying()) {
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
                elytraDashboard$yawStrafing += (float) keyStrafing * rate;
                elytraDashboard$pitchForward += (float) keyForward * rate;
                int i = 1;
                if (gameSettings.invertMouse) {
                    i = -1;
                }
                mc.player.turn(elytraDashboard$yawStrafing, elytraDashboard$pitchForward * (float) i);
            }
            if (elytraDashboard$yawStrafing != 0.0F) {
                elytraDashboard$yawStrafing *= elytraDashboard$INERTIA;
                if (Math.abs(elytraDashboard$yawStrafing) < 0.003F) {
                    elytraDashboard$yawStrafing = 0.0F;
                }
            }
            if (elytraDashboard$pitchForward != 0.0F) {
                elytraDashboard$pitchForward *= elytraDashboard$INERTIA;
                if (Math.abs(elytraDashboard$pitchForward) < 0.003F) {
                    elytraDashboard$pitchForward = 0.0F;
                }
            }
            LOGGER.info("MixinEntityRenderer -> updateCameraAndRender(FJ)V {}", System.nanoTime());
        }
    }
}
