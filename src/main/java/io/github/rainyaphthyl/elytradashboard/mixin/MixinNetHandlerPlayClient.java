package io.github.rainyaphthyl.elytradashboard.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Inject(method = "handleEntityMovement", at = @At(value = "HEAD"))
    private void recordEntityMovement(SPacketEntity packetIn, CallbackInfo ci) {
    }

    @Inject(method = "handleEntityVelocity", at = @At(value = "HEAD"))
    private void recordEntityVelocity(SPacketEntityVelocity packetIn, CallbackInfo ci) {
    }
}
