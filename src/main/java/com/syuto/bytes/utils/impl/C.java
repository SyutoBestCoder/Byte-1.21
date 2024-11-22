package com.syuto.bytes.utils.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

public class C {
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    public static ClientPlayerEntity p() {
        return mc.player;
    }

    public static ClientWorld w() {
        return mc.world;
    }

    public static boolean isInGame() {
        return p() != null && w() != null;
    }
}