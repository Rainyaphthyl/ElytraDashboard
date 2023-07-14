package io.github.rainyaphthyl.elytradashboard;

import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import io.github.rainyaphthyl.elytradashboard.config.ModConfigPanel;
import io.github.rainyaphthyl.elytradashboard.config.ModSettings;
import io.github.rainyaphthyl.elytradashboard.util.FileHelper;
import io.github.rainyaphthyl.elytradashboard.util.version.ModVersion;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LiteModElytraDashboard implements LiteMod, Configurable {
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

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void init(File configPath) {
        LiteLoader liteLoader = LiteLoader.getInstance();
        boolean registered = false;
        if (configPath != null && configPath.isDirectory()) {
            File configFile = new File(configPath, ModSettings.FILE_NAME);
            File backupFile = new File(configPath, ModSettings.BACKUP_NAME);
            try {
                boolean backupRequiring = configFile.canRead();
                boolean protectRequiring;
                if (backupRequiring) {
                    protectRequiring = backupFile.canRead() && configFile.length() < backupFile.length() * 0.75;
                } else {
                    protectRequiring = backupFile.exists();
                }
                if (protectRequiring) {
                    // Protect the backup if the config is missing
                    Date dateObj = new Date();
                    //noinspection SpellCheckingInspection
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS.ZZZZ", Locale.CANADA_FRENCH);
                    String dateTxt = dateFormat.format(dateObj);
                    String archiveName = ModSettings.FILE_NAME + "." + dateTxt + ModSettings.BACKUP_POSTFIX;
                    FileHelper.copyFile(backupFile, new File(configPath, archiveName));
                }
                if (backupRequiring) {
                    if (protectRequiring) {
                        liteLoader.registerExposable(getSettings(), ModSettings.FILE_NAME);
                        liteLoader.writeConfig(getSettings());
                        registered = true;
                    }
                    FileHelper.copyFile(configFile, backupFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!registered) {
            liteLoader.registerExposable(getSettings(), ModSettings.FILE_NAME);
        }
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
