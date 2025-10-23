package com.syuto.bytes.module.impl.misc;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketReceivedEvent;
import com.syuto.bytes.eventbus.impl.WorldJoinEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class AntiCheatDetector extends Module {

    private final List<Integer> transactions = new ArrayList<>();
    private boolean capturing;

    public AntiCheatDetector() {
        super("AntiCheatDetector", "Detects the server anti-cheat", Category.EXPLOIT);
    }

    @Override
    protected void onEnable() {
        transactions.clear();
        capturing = true;
        alert();
    }

    @Override
    protected void onDisable() {
        transactions.clear();
        capturing = false;
    }

    @EventHandler
    public void onWorldJoin(WorldJoinEvent e) {
        transactions.clear();
        capturing = true;
    }

    @EventHandler
    public void onPacket(PacketReceivedEvent e) {
        if (e.getPacket() instanceof GameJoinS2CPacket) {
            transactions.clear();
            capturing = true;
            return;
        }
        if (e.getPacket() instanceof DisconnectS2CPacket) {
            capturing = false;
            transactions.clear();
            return;
        }
        if (e.getPacket() instanceof CommonPingS2CPacket packet) {
            if (!capturing) return;
            int value = (short) packet.getParameter();
            transactions.add(value);
            if (transactions.size() == 5) {
                alert();
                capturing = false;
            }
        }
    }

    private void alert() {
        String ac = guessAntiCheat(null);
        if (ac != null) {
            ChatUtils.print("Detected AntiCheat: " + ac);
            if ("Unknown".equals(ac)) {
                ChatUtils.print("AC debug: " + transactions.toString());
            }
        }
    }

    private String guessAntiCheat(String address) {
        if (transactions.size() < 5) return null;

        List<Integer> diffs = new ArrayList<>(transactions.size() - 1);
        for (int i = 0; i < transactions.size() - 1; i++) {
            diffs.add(transactions.get(i + 1) - transactions.get(i));
        }

        int first = transactions.get(0);

        if (address != null && address.toLowerCase().endsWith("hypixel.net")) return "Watchdog";

        boolean allEqual = true;
        for (int i = 1; i < diffs.size(); i++) {
            if (!diffs.get(i).equals(diffs.get(0))) {
                allEqual = false;
                break;
            }
        }

        if (allEqual) {
            int d = diffs.get(0);
            if (d == 1) {
                if (inRange(first, -23772, -23762)) return "Vulcan";
                if (inRange(first, 95, 105) || inRange(first, -20005, -19995)) return "Matrix";
                if (inRange(first, -32773, -32762)) return "Grizzly";
                return "Verus";
            } else if (d == -1) {
                if (inRange(first, -8287, -8280)) return "Errata";
                if (first < -3000) return "Intave";
                if (inRange(first, -5, 0)) return "Grim";
                if (inRange(first, -3000, -2995)) return "Karhu";
                return "Polar";
            } else {
                return null;
            }
        }

        if (transactions.size() >= 2) {
            if (transactions.get(0).equals(transactions.get(1))) {
                boolean restInc = true;
                for (int i = 2; i < transactions.size() - 1; i++) {
                    if ((transactions.get(i + 1) - transactions.get(i)) != 1) {
                        restInc = false;
                        break;
                    }
                }
                if (restInc) return "Verus";
            }
        }

        if (diffs.size() >= 2) {
            boolean restMinusOne = true;
            for (int i = 2; i < diffs.size(); i++) {
                if (diffs.get(i) != -1) {
                    restMinusOne = false;
                    break;
                }
            }
            if (diffs.get(0) >= 100 && diffs.get(1) == -1 && restMinusOne) return "Polar";
        }

        if (first < -3000) {
            for (int v : transactions) if (v == 0) return "Intave";
        }

        if (transactions.size() >= 3) {
            if (transactions.get(0) == -30767 && transactions.get(1) == -30766 && transactions.get(2) == -25767) {
                boolean rest = true;
                for (int i = 3; i < transactions.size() - 1; i++) {
                    if ((transactions.get(i + 1) - transactions.get(i)) != 1) {
                        rest = false;
                        break;
                    }
                }
                if (rest) return "Old Vulcan";
            }
        }

        return "Unknown";
    }

    private boolean inRange(int v, int a, int b) {
        return v >= a && v <= b;
    }
}
