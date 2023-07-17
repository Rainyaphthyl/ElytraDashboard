package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;

public class ElytraPacket {
    /**
     * The raw value of {@link DamageSource#FLY_INTO_WALL}
     */
    private float completeCollisionDamage = 0.0F;
    /**
     * The value of {@link DamageSource#FLY_INTO_WALL} reduced <b>only</b> by armors (with enchantments). Requires {@link ElytraPacket#applyReducedDamages} invocation.
     */
    private float reducedCollisionDamage = 0.0F;
    /**
     * The raw value of {@link DamageSource#FALL}
     */
    private float completeFallingDamage = 0.0F;
    /**
     * The value of {@link DamageSource#FALL} reduced <b>only</b> by armors (with enchantments). Requires {@link ElytraPacket#applyReducedDamages} invocation.
     */
    private float reducedFallingDamage = 0.0F;

    public ElytraPacket() {
    }

    public static float getReducedDamage(float damage, int modifier) {
        if (damage <= 0.0F) {
            return 0.0F;
        } else {
            if (modifier > 0) {
                damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) modifier);
            }
            return damage;
        }
    }

    public void applyReducedDamages(Iterable<ItemStack> armorList) {
        int modifierCollision = 0;
        int modifierFalling = 0;
        if (armorList != null) {
            for (ItemStack stack : armorList) {
                if (!stack.isEmpty()) {
                    NBTTagList tagList = stack.getEnchantmentTagList();
                    int count = tagList.tagCount();
                    for (int i = 0; i < count; ++i) {
                        NBTTagCompound compound = tagList.getCompoundTagAt(i);
                        int id = compound.getShort("id");
                        Enchantment enchantment = Enchantment.getEnchantmentByID(id);
                        int level = compound.getShort("lvl");
                        if (enchantment != null) {
                            modifierCollision += enchantment.calcModifierDamage(level, DamageSource.FLY_INTO_WALL);
                            modifierFalling += enchantment.calcModifierDamage(level, DamageSource.FALL);
                        }
                    }
                }
            }
        }
        reducedCollisionDamage = getReducedDamage(completeCollisionDamage, modifierCollision);
        reducedFallingDamage = getReducedDamage(completeFallingDamage, modifierFalling);
    }

    /**
     * The raw value of {@link DamageSource#FLY_INTO_WALL}
     */
    public float getCompleteCollisionDamage() {
        return completeCollisionDamage;
    }

    public void setCompleteCollisionDamage(float completeCollisionDamage) {
        this.completeCollisionDamage = Math.max(completeCollisionDamage, 0.0F);
    }

    /**
     * The value of {@link DamageSource#FLY_INTO_WALL} reduced <b>only</b> by armors (with enchantments). Requires {@link ElytraPacket#applyReducedDamages} invocation.
     */
    public float getReducedCollisionDamage() {
        return reducedCollisionDamage;
    }

    /**
     * The raw value of {@link DamageSource#FALL}
     */
    public float getCompleteFallingDamage() {
        return completeFallingDamage;
    }

    public void setCompleteFallingDamage(float completeFallingDamage) {
        this.completeFallingDamage = Math.max(completeFallingDamage, 0.0F);
    }

    /**
     * The value of {@link DamageSource#FALL} reduced <b>only</b> by armors (with enchantments). Requires {@link ElytraPacket#applyReducedDamages} invocation.
     */
    public float getReducedFallingDamage() {
        return reducedFallingDamage;
    }
}
