package io.github.rainyaphthyl.elytradashboard.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.*;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
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

    @Inject(method = "handleEntityMetadata", at = @At(value = "HEAD"))
    private void getEntityMetadata(SPacketEntityMetadata packetIn, CallbackInfo ci) {
        int id = Objects.requireNonNull(packetIn).getEntityId();
        Entity entity = world.getEntityByID(id);
        if (entity instanceof MixinEntityFireworkRocket) {
            EntityLivingBase payload = ((MixinEntityFireworkRocket) entity).getBoostedEntity();
            if (payload != null) {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString("handleEntityMetadata: " + entity), ChatType.CHAT));
                }
            }
        }
    }

    @Inject(method = "handleEntityMovement", at = @At(value = "HEAD"))
    private void recordEntityMovement(SPacketEntity packetIn, CallbackInfo ci) {
        Entity entity = Objects.requireNonNull(packetIn).getEntity(world);
        if (entity instanceof MixinEntityFireworkRocket) {
            EntityLivingBase payload = ((MixinEntityFireworkRocket) entity).getBoostedEntity();
            if (payload != null) {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString("handleEntityMovement: " + entity), ChatType.CHAT));
                }
            }
        }
    }

    @Inject(method = "handleEntityVelocity", at = @At(value = "HEAD"))
    private void recordEntityVelocity(SPacketEntityVelocity packetIn, CallbackInfo ci) {
        int id = Objects.requireNonNull(packetIn).getEntityID();
        Entity entity = world.getEntityByID(id);
        if (entity instanceof MixinEntityFireworkRocket) {
            EntityLivingBase payload = ((MixinEntityFireworkRocket) entity).getBoostedEntity();
            if (payload != null) {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString("handleEntityVelocity: " + entity), ChatType.CHAT));
                }
            }
        }
    }

    @Inject(method = "handleSpawnObject", at = @At(value = "HEAD"))
    private void recordSpawningEntity(SPacketSpawnObject packetIn, CallbackInfo ci) {
        int id = Objects.requireNonNull(packetIn).getEntityID();
        Entity entity = world.getEntityByID(id);
        if (entity instanceof MixinEntityFireworkRocket) {
            EntityLivingBase payload = ((MixinEntityFireworkRocket) entity).getBoostedEntity();
            if (payload != null) {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString("handleSpawnObject: " + entity), ChatType.CHAT));
                }
            }
        }
    }
}
