package io.github.rainyaphthyl.elytradashboard.config;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.EnumMap;
import java.util.Map;

public class ModConfigPanel extends AbstractConfigPanel {
    private static final int GENERAL_HEIGHT = 16;
    private static final int CHECKBOX_HEIGHT = 12;
    private static final int OPTION_INTERVAL = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int COLOR_NORMAL = 0xE0E0E0;
    private static final int COLOR_DISABLED = 0xA0A0A0;
    private static final int COLOR_HOVERED = 0xFFFFA0;
    private static final int COLOR_LABEL = 0x00E0E0;
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
        int rangeWidth = host.getWidth();
        mainSettings = ModSettings.INSTANCE;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        tempSettings.syncFrom(mainSettings);
        int posY = 0;
        int id = 0;
        GuiCheckbox checkboxKeyElytra = addControl(new GuiCheckbox(id, 0, posY, I18n.format("elytraDashboard.config.name.keyboardElytra")),
                control -> {
                    control.checked = !control.checked;
                    tempSettings.keyboardElytraEnabled = control.checked;
                });
        checkboxKeyElytra.checked = tempSettings.keyboardElytraEnabled;
        if (checkboxKeyElytra.getButtonWidth() > rangeWidth) {
            checkboxKeyElytra.setWidth(rangeWidth);
        }
        posY += GENERAL_HEIGHT;
        GuiCheckbox checkboxDashboard = addControl(new GuiCheckbox(++id, 0, posY, I18n.format("elytraDashboard.config.name.dashboard")),
                control -> {
                    control.checked = !control.checked;
                    tempSettings.dashboardEnabled = control.checked;
                });
        checkboxDashboard.checked = tempSettings.dashboardEnabled;
        posY += GENERAL_HEIGHT;
        EnumPosX[] optionSimpleKeys = EnumPosX.values();
        String text = I18n.format("elytraDashboard.config.name.dashboardPosX");
        int posX = 0;
        int width = fontRenderer.getStringWidth(text);
        addLabel(++id, posX, posY + 1, width, CHECKBOX_HEIGHT, COLOR_LABEL, text);
        posX += width;
        Map<EnumPosX, GuiCheckbox> checkboxMap = new EnumMap<>(EnumPosX.class);
        for (EnumPosX optionKey : optionSimpleKeys) {
            posX += OPTION_INTERVAL;
            String optionName = optionKey.getDisplayName();
            GuiCheckbox checkbox = addControl(new GuiCheckbox(++id, posX, posY, optionName), control -> {
                if (!control.checked) {
                    control.checked = true;
                    for (Map.Entry<EnumPosX, GuiCheckbox> entry : checkboxMap.entrySet()) {
                        EnumPosX restKey = entry.getKey();
                        if (!optionKey.equals(restKey)) {
                            GuiCheckbox value = entry.getValue();
                            value.checked = false;
                        }
                    }
                    tempSettings.dashboardPosX = optionKey;
                }
            });
            if (checkbox.x + checkbox.getButtonWidth() > rangeWidth) {
                posX = OPTION_INTERVAL;
                posY += CHECKBOX_HEIGHT;
                checkbox.x = posX;
                checkbox.y = posY;
            }
            posX += checkbox.getButtonWidth();
            checkboxMap.put(optionKey, checkbox);
            checkbox.checked = tempSettings.dashboardPosX == optionKey;
        }
        posY += GENERAL_HEIGHT;
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
