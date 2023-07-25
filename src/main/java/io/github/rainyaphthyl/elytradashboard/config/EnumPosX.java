package io.github.rainyaphthyl.elytradashboard.config;

import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public enum EnumPosX {
    LEFT("left"),
    MID("mid"),
    RIGHT("right");
    public static final String PARENT_KEY = "elytraDashboard.config.option.";
    public final String fullKey;
    public final String simpleKey;

    EnumPosX(String simpleKey) {
        this.simpleKey = simpleKey;
        fullKey = PARENT_KEY + simpleKey;
    }

    @Override
    @Nonnull
    public String toString() {
        return getDisplayName();
    }

    @Nonnull
    public String getDisplayName() {
        return I18n.format(fullKey);
    }
}
