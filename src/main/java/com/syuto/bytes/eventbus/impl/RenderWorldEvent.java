package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.AllArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

// maybe add partialticks idk
@AllArgsConstructor
public class RenderWorldEvent implements Event {

    public MatrixStack matrixStack;
}
