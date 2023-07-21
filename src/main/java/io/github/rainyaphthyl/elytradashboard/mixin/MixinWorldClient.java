package io.github.rainyaphthyl.elytradashboard.mixin;

import io.github.rainyaphthyl.elytradashboard.core.References;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(WorldClient.class)
public class MixinWorldClient {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "removeEntityFromWorld", at = @At(value = "RETURN"))
    private void onRemoveEntity(int entityID, CallbackInfoReturnable<Entity> cir) {
        Entity entity = cir == null ? null : cir.getReturnValue();
        if (entity instanceof AccessEntityFireworkRocket) {
            EntityPlayerSP player = mc.player;
            if (player != null && player.isElytraFlying()) {
                EntityLivingBase payload = ((AccessEntityFireworkRocket) entity).getBoostedEntity();
                boolean flag = payload == player;
                if (!flag && payload instanceof EntityPlayer && payload.isElytraFlying()) {
                    UUID uuidHost = player.getUniqueID();
                    UUID uuidBoosted = payload.getUniqueID();
                    flag = uuidBoosted.equals(uuidHost);
                }
                if (flag) {
                    int lifetime = ((AccessEntityFireworkRocket) entity).getFireworkAge();
                    UUID uuidRocket = entity.getUniqueID();
                    References.flightInstrument.markFireworkLifetime(uuidRocket, lifetime);
                }
            }
        }
    }
}
