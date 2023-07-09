package io.github.rainyaphthyl;

import com.mumfrey.liteloader.LiteMod;
import io.github.rainyaphthyl.util.versions.ModVersion;

import java.io.File;

@SuppressWarnings("unused")
public class LiteModElytraDashboard implements LiteMod {
    private static final String NAME = "Elytra Dashboard";
    private static final String VERSION = "0.1.0";
    private static ModVersion VERSION_OBJ = null;

    public static ModVersion get_version_obj() {
        if (VERSION_OBJ == null) {
            VERSION_OBJ = ModVersion.getVersion(VERSION);
        }
        return VERSION_OBJ;
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
}
