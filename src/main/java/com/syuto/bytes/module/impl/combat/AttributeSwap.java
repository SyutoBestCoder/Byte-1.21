package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;

public class AttributeSwap extends Module {

    private int lastSlot = -1;
    private boolean swapNext = false;
    private boolean swapped = false;

    public AttributeSwap() {
        super("AttributeSwap", "Swaps to mace every other attack with axe or always with sword", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.player.getInventory() == null) return;

        if (event.getMode() == AttackEntityEvent.Mode.Pre) {
            ItemStack heldItem = mc.player.getMainHandStack();

            if (heldItem.getItem() instanceof AxeItem) {
                if (swapNext) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.getInventory().getStack(i);
                        if (stack != null && stack.getItem() == Items.MACE) {
                            lastSlot = mc.player.getInventory().selectedSlot;
                            mc.player.getInventory().selectedSlot = i;
                            swapped = true;
                            break;
                        }
                    }
                }

            } else if (heldItem.getItem() instanceof SwordItem) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack != null && stack.getItem() == Items.MACE) {
                        lastSlot = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = i;
                        swapped = true;
                        break;
                    }
                }
            }
        }

        if (event.getMode() == AttackEntityEvent.Mode.Post) {
            if (swapped && lastSlot != -1) {
                mc.player.getInventory().selectedSlot = lastSlot;
                swapped = false;
                lastSlot = -1;
            }

            ItemStack heldItem = mc.player.getMainHandStack();
            if (heldItem.getItem() instanceof AxeItem) {
                swapNext = !swapNext;
            } else {
                swapNext = false;
            }
        }
    }
}
