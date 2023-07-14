package io.github.rainyaphthyl.elytradashboard;

import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import io.github.rainyaphthyl.elytradashboard.config.ModConfigPanel;
import io.github.rainyaphthyl.elytradashboard.config.ModSettings;
import io.github.rainyaphthyl.elytradashboard.display.RegFrameUpdaters;
import io.github.rainyaphthyl.elytradashboard.util.version.ModVersion;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.io.File;

public class LiteModElytraDashboard implements Tickable, Configurable {
    public static final String NAME = "Elytra Dashboard";
    public static final String VERSION = "0.1.1-alpha.2";
    private static ModVersion versionObj = null;

    @SuppressWarnings("unused")
    public static ModVersion getVersionObj() {
        if (versionObj == null) {
            versionObj = ModVersion.getVersion(VERSION);
        }
        return versionObj;
    }

    @SuppressWarnings("SameReturnValue")
    public ModSettings getSettings() {
        return ModSettings.INSTANCE;
    }

    /**
     * Get the mod version string
     *
     * @return the mod version as a string
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * Do startup stuff here, minecraft is not fully initialised when this
     * function is called so mods <b>must not</b> interact with minecraft in any
     * way here.
     *
     * @param configPath Configuration path to use
     */
    @Override
    public void init(File configPath) {
        ModSettings.initConfig(configPath);
        RegFrameUpdaters.registerAll();
    }

    /**
     * Called when the loader detects that a version change has happened since
     * this mod was last loaded.
     *
     * @param version       new version
     * @param configPath    Path for the new version-specific config
     * @param oldConfigPath Path for the old version-specific config
     */
    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    /**
     * Get the display name
     *
     * @return display name
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Get the class of the configuration panel to use, the returned class must
     * have a default (no-arg) constructor
     *
     * @return configuration panel class
     */
    @Override
    public Class<? extends ConfigPanel> getConfigPanelClass() {
        return ModConfigPanel.class;
    }

    /**
     * Called every frame
     *
     * @param minecraft    Minecraft instance
     * @param partialTicks Partial tick value
     * @param inGame       True if in-game, false if in the menu
     * @param clock        True if this is a new tick, otherwise false if it's a
     *                     regular frame
     */
    @Override
    public void onTick(@Nonnull Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (minecraft.world != null) {
            if (clock) {
                RegFrameUpdaters.updateAllOnTick(minecraft);
            }
            RegFrameUpdaters.updateAllOnFrame(minecraft, partialTicks);
        }
    }
}
