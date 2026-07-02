package mca.compat;

import mca.core.MCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class RaidsBackportCompat {
    private static final String MODID = "raids";

    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    public static void postInit() {
        if (!isLoaded()) {
            return;
        }

        try {
            RaidsBackportCompatCalls.postInit();
            MinecraftForge.EVENT_BUS.register(new RaidsBackportCompat());
            MCA.getLog().info("Enabled raids-backport integration.");
        } catch (Throwable t) {
            MCA.getLog().error("Failed to initialize raids-backport integration.", t);
        }
    }

    public static boolean isRaidEnemy(EntityLivingBase entity) {
        if (!isLoaded()) {
            return false;
        }

        try {
            return RaidsBackportCompatCalls.isRaider(entity);
        } catch (Throwable t) {
            return false;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }

        RaidsBackportCompatCalls.convertRaiderToBandit(event.getEntityLiving());
    }
}
