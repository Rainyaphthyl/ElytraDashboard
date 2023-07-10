package io.github.rainyaphthyl.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Inject(method = "updateCameraAndRender(FJ)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;smoothCamera:Z", opcode = Opcodes.GETFIELD, shift = At.Shift.BEFORE)
    )
    private void rotateByKeyboard(float partialTicks, long nanoTime, CallbackInfo ci) {
        System.out.println("MixinEntityRenderer -> updateCameraAndRender(FJ)V");
    }
}
