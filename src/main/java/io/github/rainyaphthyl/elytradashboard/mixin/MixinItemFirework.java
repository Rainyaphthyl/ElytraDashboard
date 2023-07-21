package io.github.rainyaphthyl.elytradashboard.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;
import java.util.UUID;

@Mixin(ItemFirework.class)
public class MixinItemFirework extends Item {
    @Nonnull
    @Redirect(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;getHeldItem(Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack getItemStackTags(@Nonnull EntityPlayer playerIn, EnumHand enumHand) throws IllegalArgumentException {
        ItemStack itemStack = playerIn.getHeldItem(enumHand);
        Minecraft client = Minecraft.getMinecraft();
        EntityPlayerSP playerHost = client.player;
        boolean accessible;
        if (client.isSingleplayer()) {
            if (playerIn instanceof EntityPlayerMP && playerHost != null) {
                UUID uuidHost = playerHost.getUniqueID();
                UUID uuidServer = playerIn.getUniqueID();
                accessible = uuidHost.equals(uuidServer);
            } else {
                accessible = false;
            }
        } else {
            accessible = playerIn instanceof EntityPlayerSP;
        }
        if (accessible) {
            NBTTagCompound compound = itemStack.getSubCompound("Fireworks");
            byte level = compound == null ? (byte) 0 : compound.getByte("Flight");
            client.addScheduledTask(() -> {
                NetHandlerPlayClient connection = client.getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(itemStack.getTextComponent()));
                    connection.handleChat(new SPacketChat(new TextComponentString("Level = " + level)));
                }
            });
        }
        return itemStack;
    }
}
