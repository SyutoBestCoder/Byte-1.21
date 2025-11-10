package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;

public class ElytraSwap extends Module {
    public ElytraSwap() {
        super("ElytraSwap", "Swaps", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (event.getMode() == AttackEntityEvent.Mode.Pre) {
            ItemStack currentItem = mc.player.getMainHandStack();
            int current = mc.player.getInventory().selectedSlot;
            if (currentItem.getItem() instanceof MaceItem) {
                for (ItemStack a : mc.player.getInventory().armor) {
                    if (a != null && a.getItem() == Items.ELYTRA) {
                        for (int i = 0; i < 9; i++) {
                            ItemStack stack = mc.player.getInventory().getStack(i);
                            if (stack != null && (stack.getItem() == Items.NETHERITE_CHESTPLATE || stack.getItem() == Items.DIAMOND_CHESTPLATE)) {
                                mc.player.getInventory().selectedSlot = i;
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            }
                        }
                    }
                }
            }
            if (mc.player.getInventory().selectedSlot != current) {
                mc.player.getInventory().selectedSlot = current;
            }
        }
    }


    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
    }
}
