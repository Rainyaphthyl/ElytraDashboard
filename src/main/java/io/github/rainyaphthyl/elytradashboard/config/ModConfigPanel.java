package io.github.rainyaphthyl.elytradashboard.config;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import io.github.rainyaphthyl.elytradashboard.LiteModElytraDashboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class ModConfigPanel extends AbstractConfigPanel {
    private static final int CHECKBOX_HEIGHT = 12;
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
        int posY = 0;
        int id = 0;
        GuiCheckbox checkboxKeyElytra = addControl(new GuiCheckbox(id, 0, posY, I18n.format("elytraDashboard.config.name.keyboardElytra")),
                control -> {
                    control.checked = !control.checked;
                    tempSettings.keyboardElytraEnabled = control.checked;
                });
        checkboxKeyElytra.checked = tempSettings.keyboardElytraEnabled;
        posY += CHECKBOX_HEIGHT;
        GuiCheckbox checkboxDashboard = addControl(new GuiCheckbox(++id, 0, posY, I18n.format("elytraDashboard.config.name.dashboard")),
                control -> {
                    control.checked = !control.checked;
                    tempSettings.dashboardEnabled = control.checked;
                });
        checkboxDashboard.checked = tempSettings.dashboardEnabled;
        posY += CHECKBOX_HEIGHT;
        GuiCheckbox checkboxAlarm = addControl(new GuiCheckbox(++id, 0, posY, I18n.format("elytraDashboard.config.name.warning")),
                control -> {
                    control.checked = !control.checked;
                    tempSettings.warningEnabled = control.checked;
                });
        checkboxAlarm.checked = tempSettings.warningEnabled;
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
            mainSettings = null;
        }
    }
}
