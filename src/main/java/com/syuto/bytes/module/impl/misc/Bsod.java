package com.syuto.bytes.module.impl.misc;

import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.WinUtil;

public class Bsod extends Module {
    public Bsod() {
        super("BSOD", "Cause a BSOD", Category.EXPLOIT);
    }

    @Override
    protected void onEnable() {
        WinUtil.bsod();
    }
}
