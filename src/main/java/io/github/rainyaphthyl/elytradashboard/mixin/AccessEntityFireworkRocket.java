package io.github.rainyaphthyl.elytradashboard.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityFireworkRocket.class)
public interface AccessEntityFireworkRocket {
    @Accessor(value = "boostedEntity")
    EntityLivingBase getBoostedEntity();

    @Accessor(value = "lifetime")
    int getLifetime();

    @Accessor(value = "fireworkAge")
    int getFireworkAge();
}
