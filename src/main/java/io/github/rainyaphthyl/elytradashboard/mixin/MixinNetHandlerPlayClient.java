package io.github.rainyaphthyl.elytradashboard.mixin;

import io.github.rainyaphthyl.elytradashboard.core.References;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Shadow
    private WorldClient world;
    @Shadow
    private Minecraft client;

    @Inject(method = "handleEntityMovement", at = @At(value = "HEAD"))
    private void recordEntityMovement(SPacketEntity packetIn, CallbackInfo ci) {
        Entity entity = Objects.requireNonNull(packetIn).getEntity(world);
        References.flightInstrument.checkMarkFirework(client, entity);
    }

    @Inject(method = "handleEntityVelocity", at = @At(value = "HEAD"))
    private void recordEntityVelocity(SPacketEntityVelocity packetIn, CallbackInfo ci) {
        int id = Objects.requireNonNull(packetIn).getEntityID();
        Entity entity = world.getEntityByID(id);
        References.flightInstrument.checkMarkFirework(client, entity);
    }
}
