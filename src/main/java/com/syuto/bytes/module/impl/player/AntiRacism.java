package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.ChatEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;

public class AntiRacism extends Module {
    public AntiRacism() {
        super("AntiRacism", "FUCK RACISM", Category.PLAYER);
    }

    static String[] racismList = new String[] {
            "nigger", "niggur", "nigga",
            "niga", "monkey", "blackie",
            "kike", "chink", "beaner",
            "chimp", "chigga"
    };

    @EventHandler
    public void onChat(ChatEvent event) {
        String message = event.getMessage().toLowerCase();
        for (String word : racismList) {
            if (message.contains(word) && !event.isCanceled()) {
                String replacement = "*".repeat(word.length());
                ChatUtils.print("No no word!");
                event.setCanceled(true);
                event.setMessage(message.replaceAll(word, replacement));
            }
        }
    }
}

