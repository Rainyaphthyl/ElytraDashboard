package io.github.rainyaphthyl.elytradashboard.core;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import javax.annotation.Nonnull;

public class KeyboardRotator {
    /**
     * This may not be {@code 8.0F} for keyboard operation
     */
    public static final float RATE_ALTERED = 16.0F;
    public static final float SCALE_FACTOR = 0.6F;
    public static final float SCALE_OFFSET = 0.2F;
    public static final float INERTIA = 0.6F;
    private float deltaYaw = 0.0F;
    private float deltaPitch = 0.0F;
    private float prevDeltaYaw = 0.0F;
    private float prevDeltaPitch = 0.0F;
    private float prevPartialTick = 0.0F;
    private float prevSensitivity;
    private float prevMouseRate;
    /**
     * If you have been holding a key before taking off, the keyboard rotator will not immediately start
     */
    private boolean keyHeldBeforeFlight = true;

    {
        prevSensitivity = 0.0F;
        float scale = prevSensitivity * KeyboardRotator.SCALE_FACTOR + KeyboardRotator.SCALE_OFFSET;
        prevMouseRate = scale * scale * scale * KeyboardRotator.RATE_ALTERED;
    }

    public void updateTickRotation(@Nonnull GameSettings gameSettings, boolean elytraFlying) {
        if (!elytraFlying) {
            keyHeldBeforeFlight = true;
        }
        KeyBinding keyBindLeft = gameSettings.keyBindLeft;
        KeyBinding keyBindRight = gameSettings.keyBindRight;
        KeyBinding keyBindForward = gameSettings.keyBindForward;
        KeyBinding keyBindBack = gameSettings.keyBindBack;
        if (keyHeldBeforeFlight) {
            keyHeldBeforeFlight = keyBindLeft.isKeyDown() || keyBindRight.isKeyDown() || keyBindForward.isKeyDown() || keyBindBack.isKeyDown();
        }
        boolean checking = elytraFlying && !keyHeldBeforeFlight && References.flightInstrument.getHeight() >= 20.0;
        if (checking) {
            prevDeltaYaw = deltaYaw;
            prevDeltaPitch = deltaPitch;
            float rate;
            float sensitivity = gameSettings.mouseSensitivity;
            if (prevSensitivity == sensitivity) {
                rate = prevMouseRate;
            } else {
                float scale = sensitivity * KeyboardRotator.SCALE_FACTOR + KeyboardRotator.SCALE_OFFSET;
                rate = scale * scale * scale * KeyboardRotator.RATE_ALTERED;
                prevMouseRate = rate;
                prevSensitivity = sensitivity;
            }
            int keyForward = 0;
            if (keyBindForward.isKeyDown()) {
                ++keyForward;
            }
            if (keyBindBack.isKeyDown()) {
                --keyForward;
            }
            int keyStrafing = 0;
            if (keyBindRight.isKeyDown()) {
                ++keyStrafing;
            }
            if (keyBindLeft.isKeyDown()) {
                --keyStrafing;
            }
            if (keyStrafing != 0 || keyForward != 0) {
                deltaYaw += (float) keyStrafing * rate;
                deltaPitch += (float) keyForward * rate;
            }
        }
        if (deltaYaw != 0.0F) {
            deltaYaw *= KeyboardRotator.INERTIA;
            if (Math.abs(deltaYaw) < 0.003F) {
                deltaYaw = 0.0F;
            }
        }
        if (deltaPitch != 0.0F) {
            deltaPitch *= KeyboardRotator.INERTIA;
            if (Math.abs(deltaPitch) < 0.003F) {
                deltaPitch = 0.0F;
            }
        }
    }

    /**
     * The array object will be returned if valid.
     *
     * @param result       The buffer to store the returned {@code deltaYaw} and {@code deltaPitch}
     * @param partialTicks The mixin argument
     * @return An array with 2 floats: {@code deltaYaw} and {@code deltaPitch}
     */
    public float[] updateFrameRotation(float[] result, float partialTicks) {
        if (result == null || result.length < 2) {
            result = new float[2];
        }
        float partialDYaw;
        float partialDPitch;
        if (prevPartialTick < partialTicks) {
            float currFrame = partialTicks - prevPartialTick;
            partialDYaw = deltaYaw * currFrame;
            partialDPitch = deltaPitch * currFrame;
        } else {
            float priorHalf = 1.0F - prevPartialTick;
            partialDYaw = prevDeltaYaw * priorHalf;
            partialDPitch = prevDeltaPitch * priorHalf;
            float postDYaw = deltaYaw * partialTicks;
            float postDPitch = deltaPitch * partialTicks;
            partialDYaw += postDYaw;
            partialDPitch += postDPitch;
        }
        result[0] = partialDYaw;
        result[1] = partialDPitch;
        prevPartialTick = partialTicks;
        return result;
    }
}
