package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoPearl extends Module {
    public AutoPearl() {
        super("AutoPearl", "Automatically throws a pearl and then a wind charge", Category.PLAYER);
    }

    private int throwStage = 0;
    private int previousSlot = -1;

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.options.pickItemKey.isPressed() && throwStage == 0) {
            previousSlot = mc.player.getInventory().selectedSlot;
            throwStage = 1;
        }

        switch (throwStage) {
            case 1 -> {
                boolean pearlThrown = useItem(Items.ENDER_PEARL);
                if (!pearlThrown) {
                    reset();
                    return;
                }
                throwStage = 2;
            }

            case 2 -> {
                throwStage = 3;
            }

            case 3 -> {
                useItem(Items.WIND_CHARGE);
                reset();
            }
        }
    }

    private boolean useItem(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == item) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }

    private void reset() {
        if (previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
        throwStage = 0;
    }
}
