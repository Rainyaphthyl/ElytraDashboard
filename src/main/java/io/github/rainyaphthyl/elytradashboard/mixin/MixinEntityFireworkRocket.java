package io.github.rainyaphthyl.elytradashboard.mixin;

import net.minecraft.entity.item.EntityFireworkRocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFireworkRocket.class)
public class MixinEntityFireworkRocket {
    @Inject(method = "setVelocity", at = @At(value = "RETURN"))
    private void onSetVelocity(double x, double y, double z, CallbackInfo ci) {
    }
}
