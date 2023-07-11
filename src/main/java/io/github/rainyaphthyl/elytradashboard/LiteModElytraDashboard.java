package io.github.rainyaphthyl.elytradashboard;

import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import io.github.rainyaphthyl.elytradashboard.util.versions.ModVersion;

import java.io.File;

@SuppressWarnings("unused")
@ExposableOptions(strategy = ConfigStrategy.Unversioned, filename = "elytradashboard.json")
public class LiteModElytraDashboard implements LiteMod, Configurable {
    private static final String NAME = "Elytra Dashboard";
    private static final String VERSION = "0.1.1-alpha.0";
    private static ModVersion versionObj = null;

    public static ModVersion getVersionObj() {
        if (versionObj == null) {
            versionObj = ModVersion.getVersion(VERSION);
        }
        return versionObj;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void init(File configPath) {
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
        return null;
    }
}
