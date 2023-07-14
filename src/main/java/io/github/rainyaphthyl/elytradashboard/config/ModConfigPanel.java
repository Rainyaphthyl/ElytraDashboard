package io.github.rainyaphthyl.elytradashboard.config;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import io.github.rainyaphthyl.elytradashboard.LiteModElytraDashboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

@SuppressWarnings("unused")
public class ModConfigPanel extends AbstractConfigPanel {
    private final ModSettings tempSettings = new ModSettings(ModSettings.INSTANCE);
    private ModSettings mainSettings = null;

    /**
     * Stub for implementors, this is similar to {@link GuiScreen#initGui} and
     * consumers should add all of their controls here
     *
     * @param host the panel host
     */
    @Override
    protected void addOptions(ConfigPanelHost host) {
        if (host == null) {
            return;
        }
        LiteModElytraDashboard mod = host.getMod();
        mainSettings = mod.getSettings();
        tempSettings.syncFrom(mainSettings);
        GuiCheckbox checkbox = addControl(new GuiCheckbox(0, 0, 32, I18n.format("elytraDashboard.config.name.keyboardElytra")),
                control -> {
                    control.checked = !control.checked;
                    tempSettings.keyboardElytra = control.checked;
                });
        checkbox.checked = tempSettings.keyboardElytra;
    }

    /**
     * Panels should return the text to display at the top of the config panel
     * window.
     */
    @Override
    public String getPanelTitle() {
        return I18n.format("elytraDashboard.config.title");
    }

    /**
     * Called when the panel is closed, panel should save settings
     */
    @Override
    public void onPanelHidden() {
        if (mainSettings != null && !mainSettings.equals(tempSettings)) {
            boolean updated = mainSettings.syncFrom(tempSettings);
            if (updated) {
                LiteLoader.getInstance().writeConfig(mainSettings);
            }
        }
    }
}