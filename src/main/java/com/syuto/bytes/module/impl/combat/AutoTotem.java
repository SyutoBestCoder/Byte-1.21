package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("AutoTotem", "AutoTotemer", Category.COMBAT);
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        int foundInvIndex = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                foundInvIndex = i;
                break;
            }
        }

        if (foundInvIndex == -1) {
            for (int i = 9; i < mc.player.getInventory().size(); i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    foundInvIndex = i;
                    break;
                }
            }
        }

        if (foundInvIndex == -1) {
            //ChatUtils.print("No Totem in inventory");
            return;
        }

        int syncId = mc.player.currentScreenHandler.syncId;
        int offhandSlot = 45;

        int slotId = (foundInvIndex < 9) ? 36 + foundInvIndex : foundInvIndex;

        mc.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, offhandSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.PICKUP, mc.player);

        //ChatUtils.print("Moved Totem from slot " + foundInvIndex + " (window slot " + slotId + ") to offhand");
    }

}
