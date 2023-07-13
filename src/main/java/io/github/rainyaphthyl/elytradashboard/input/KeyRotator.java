package io.github.rainyaphthyl.elytradashboard.input;

import net.minecraft.client.settings.GameSettings;

import javax.annotation.Nonnull;

public class KeyRotator {
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

    {
        prevSensitivity = 0.0F;
        float scale = prevSensitivity * KeyRotator.SCALE_FACTOR + KeyRotator.SCALE_OFFSET;
        prevMouseRate = scale * scale * scale * KeyRotator.RATE_ALTERED;
    }

    public void updateTickRotation(@Nonnull GameSettings gameSettings) {
        prevDeltaYaw = deltaYaw;
        prevDeltaPitch = deltaPitch;
        float rate;
        float sensitivity = gameSettings.mouseSensitivity;
        if (prevSensitivity == sensitivity) {
            rate = prevMouseRate;
        } else {
            float scale = sensitivity * KeyRotator.SCALE_FACTOR + KeyRotator.SCALE_OFFSET;
            rate = scale * scale * scale * KeyRotator.RATE_ALTERED;
            prevMouseRate = rate;
            prevSensitivity = sensitivity;
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
            deltaYaw += (float) keyStrafing * rate;
            deltaPitch += (float) keyForward * rate;
        }
        if (deltaYaw != 0.0F) {
            deltaYaw *= KeyRotator.INERTIA;
            if (Math.abs(deltaYaw) < 0.003F) {
                deltaYaw = 0.0F;
            }
        }
        if (deltaPitch != 0.0F) {
            deltaPitch *= KeyRotator.INERTIA;
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
            System.out.printf("Partial %.4f->%.4f: %+.6f/%+.6f\n", prevPartialTick, partialTicks,
                    partialDYaw, partialDPitch);
        } else {
            float priorHalf = 1.0F - prevPartialTick;
            partialDYaw = prevDeltaYaw * priorHalf;
            partialDPitch = prevDeltaPitch * priorHalf;
            System.out.printf("Tail %.4f->%.4f: %+.6f/%+.6f\n", prevPartialTick, 1.0F,
                    partialDYaw, partialDPitch);
            float postDYaw = deltaYaw * partialTicks;
            float postDPitch = deltaPitch * partialTicks;
            partialDYaw += postDYaw;
            partialDPitch += postDPitch;
            System.out.printf("Head %.4f->%.4f: %+.6f/%+.6f\n", 0.0F, partialTicks, postDYaw, postDPitch);
        }
        result[0] = partialDYaw;
        result[1] = partialDPitch;
        prevPartialTick = partialTicks;
        return result;
    }

    public float getDeltaYaw() {
        return deltaYaw;
    }

    public float getDeltaPitch() {
        return deltaPitch;
    }
}