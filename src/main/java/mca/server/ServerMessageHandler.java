package mca.server;

import mca.core.forge.NetMCA;
import mca.core.minecraft.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class ServerMessageHandler {

    public static void handleMessage(EntityPlayerMP player, NetMCA.ButtonAction message) {
        switch (message.getButtonId()) {
            case "gui.button.debug.startraid":
                startRaid(player);
                break;
            case "gui.button.debug.spawnguards":
                spawnGuards(player);
                break;
            case "gui.button.debug.rebuildvillage":
                rebuildVillage(player);
                break;
        }
    }

    private static void startRaid(EntityPlayerMP player) {
        player.sendMessage(new TextComponentTranslation("notify.debug.raid.start"));
        VillageHelper.forceRaid(player);
    }

    private static void spawnGuards(EntityPlayerMP player) {
        player.sendMessage(new TextComponentTranslation("notify.debug.guards.spawn"));
        VillageHelper.tick(player.world);
    }

    private static void rebuildVillage(EntityPlayerMP player) {
        player.sendMessage(new TextComponentTranslation("notify.debug.village.rebuild"));
    }
}
