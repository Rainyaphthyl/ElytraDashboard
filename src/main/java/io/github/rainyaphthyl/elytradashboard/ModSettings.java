package io.github.rainyaphthyl.elytradashboard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.Exposable;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

@ExposableOptions(strategy = ConfigStrategy.Unversioned, filename = "elytradashboard.json")
public class ModSettings implements Exposable {
    public static final ModSettings INSTANCE = new ModSettings();
    @Expose
    @SerializedName("enable_keyboard_elytra")
    public boolean keyboardElytra = false;

    private ModSettings() {
    }
}
