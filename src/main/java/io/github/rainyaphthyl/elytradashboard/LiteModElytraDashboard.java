package io.github.rainyaphthyl.elytradashboard;

import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import io.github.rainyaphthyl.elytradashboard.util.versions.ModVersion;

import java.io.File;

public class LiteModElytraDashboard implements LiteMod, Configurable {
    public static final String NAME = "Elytra Dashboard";
    public static final String VERSION = "0.1.1-alpha.0";
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

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void init(File configPath) {
        LiteLoader liteLoader = LiteLoader.getInstance();
        liteLoader.registerExposable(getSettings(), ModSettings.FILE_NAME);
        liteLoader.writeConfig(getSettings());
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<? extends ConfigPanel> getConfigPanelClass() {
        return ModConfigPanel.class;
    }
}
