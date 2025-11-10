package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;

public class ChestStealer extends Module {
    public NumberSetting stealDelay = new NumberSetting("Delay",this,200,0,500, 25);

    public ChestStealer() {
        super("Stealer", "Steals from chests", Category.PLAYER);
    }

    private boolean shouldSteal;
    private long delay, lastTime;

    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {
        if (mc.currentScreen instanceof GenericContainerScreen e) {
            String title = e.getTitle().getString();
            GenericContainerScreenHandler handler = e.getScreenHandler();
            if (title.contains("Large Chest") || title.contains("Chest") || title.contains("Barrel")) {
                steal(handler.getInventory().size(), handler);
            }
        } else if (mc.currentScreen instanceof ShulkerBoxScreen a) {
            String title = a.getTitle().getString();
            ShulkerBoxScreenHandler handle = a.getScreenHandler();
            if (title.contains("Shulker Box")) {
                steal(27, handle);
            }
        }
    }


    private void steal(int size, ScreenHandler handler) {
        for (int i = 0; i < size; i++) {
            Slot slot = handler.slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                if (System.currentTimeMillis() - this.lastTime >= delay) {
                    this.lastTime = System.currentTimeMillis();
                    mc.interactionManager.clickSlot(handler.syncId, slot.id, 1, SlotActionType.QUICK_MOVE, mc.player);
                    updateDelay();
                }
            }
        }
    }

    private void updateDelay() {
        delay = stealDelay.value.longValue();
    }
}
