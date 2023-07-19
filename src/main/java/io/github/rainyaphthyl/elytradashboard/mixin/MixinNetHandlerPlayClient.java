package io.github.rainyaphthyl.elytradashboard.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
        if (entity instanceof AccessEntityFireworkRocket) {
            EntityLivingBase payload = ((AccessEntityFireworkRocket) entity).getBoostedEntity();
            if (payload != null) {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString("handleEntityMovement: " + entity.getName() + ": " + payload.getName()).setStyle(new Style().setColor(TextFormatting.DARK_GREEN)), ChatType.CHAT));
                }
            }
        }
    }

    @Inject(method = "handleEntityVelocity", at = @At(value = "HEAD"))
    private void recordEntityVelocity(SPacketEntityVelocity packetIn, CallbackInfo ci) {
        int id = Objects.requireNonNull(packetIn).getEntityID();
        Entity entity = world.getEntityByID(id);
        if (entity instanceof AccessEntityFireworkRocket) {
            EntityLivingBase payload = ((AccessEntityFireworkRocket) entity).getBoostedEntity();
            if (payload != null) {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString("handleEntityVelocity: " + entity.getName() + ": " + payload.getName()).setStyle(new Style().setColor(TextFormatting.GREEN)), ChatType.CHAT));
                }
            }
        }
    }
}
