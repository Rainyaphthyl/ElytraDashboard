package io.github.rainyaphthyl.elytradashboard.mixin;

import io.github.rainyaphthyl.elytradashboard.core.References;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;

@Mixin(ItemFirework.class)
public class MixinItemFirework extends Item {
    @Nonnull
    @Redirect(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;getHeldItem(Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack getItemStackTags(@Nonnull EntityPlayer playerIn, EnumHand enumHand) {
        ItemStack itemStack = playerIn.getHeldItem(enumHand);
        if (playerIn.isElytraFlying()) {
            boolean clientAccess = playerIn instanceof EntityPlayerSP;
            // Server-side query can fail in single-player, due to unknown effects from other mods
            // Do not check the server
            if (clientAccess) {
                NBTTagCompound compound = itemStack.getSubCompound("Fireworks");
                byte level = compound == null ? (byte) 0 : compound.getByte("Flight");
                References.flightInstrument.recordFirework(level);
            }
        }
        return itemStack;
    }
}
