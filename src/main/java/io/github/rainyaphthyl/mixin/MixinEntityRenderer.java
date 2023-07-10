package io.github.rainyaphthyl.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "updateCameraAndRender(FJ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V",
                    shift = At.Shift.NONE)
    )
    private void rotateByKeyboard(float partialTicks, long nanoTime, CallbackInfo ci) {
        if (mc.player.isElytraFlying()) {
            mc.player.turn(-7, 0);
            System.out.println("MixinEntityRenderer -> updateCameraAndRender(FJ)V");
        }
    }
}
