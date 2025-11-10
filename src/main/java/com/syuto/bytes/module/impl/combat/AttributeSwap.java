package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent.Mode;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

/**
 * Swaps to mace on attack, then swaps back on next tick.
 */
public class AttributeSwap extends Module {

    private int previousSlot = -1;

    public AttributeSwap() {
        super("AttributeSwap", "Swap to mace during attacks", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (event.getMode() == Mode.Pre) {
            int current = mc.player.getInventory().selectedSlot;
            previousSlot = current;
            if (PlayerUtil.isHoldingWeapon()) {
                int maceSlot = findMaceInHotbar();
                if (maceSlot != -1) {
                    mc.player.getInventory().selectedSlot = maceSlot;
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
                    }
                    ChatUtils.print("Swapped to mace " + maceSlot + " from " + current);
                }
            }
        }

        if (event.getMode() == Mode.Post) {
            if (previousSlot >= 0) {
                mc.player.getInventory().selectedSlot = previousSlot;
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
                }
                ChatUtils.print("Swapped back " + previousSlot);
                previousSlot = -1;
            }
        }
    }

    private int findMaceInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() instanceof MaceItem) {
                return i;
            }
        }
        return -1;
    }
}
