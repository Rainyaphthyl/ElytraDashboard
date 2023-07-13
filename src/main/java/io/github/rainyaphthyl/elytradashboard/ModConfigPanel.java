package io.github.rainyaphthyl.elytradashboard;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class ModConfigPanel extends AbstractConfigPanel {
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
        addLabel(1, 0, 0, 200, 32, 0xFFFF55,
                I18n.format("elytraDashboard.config.name.keyboardElytra"));
        ModSettings settings = LiteModElytraDashboard.getSettings();
        GuiCheckbox checkbox = addControl(new GuiCheckbox(0, 0, 32,
                I18n.format("elytraDashboard.config.option.enabled")), control -> {
            control.checked = !control.checked;
            settings.keyboardElytra = control.checked;
            LiteLoader.getInstance().writeConfig(settings);
        });
        checkbox.checked = settings.keyboardElytra;
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
    }
}
