package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ColorSetting;
import com.syuto.bytes.setting.impl.ModeSetting;

import java.awt.*;

public class ThemeModule extends Module {

    public final ColorSetting accent = new ColorSetting("Accent", this, new Color(0, 160, 255));
    public final ModeSetting theme = new ModeSetting("Theme", this, "Dark", "Light");

    public ThemeModule() {
        super("Theme", "Customize the looks of the client.", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        this.setEnabled(false);
    }

}
