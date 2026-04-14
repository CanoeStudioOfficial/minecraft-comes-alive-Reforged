package mca.core;

import mca.api.API;
import mca.blocks.TileEntityTombstone;
import mca.command.CommandAdminMCA;
import mca.command.CommandMCA;
import mca.core.forge.EventHooks;
import mca.core.forge.GuiHandler;
import mca.core.forge.NetMCA;
import mca.core.forge.ServerProxy;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import mca.mca.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, guiFactory = "mca.client.MCAGuiFactory")
public class MCA {
    public static final String MODID = Tags.MOD_ID;

    @SidedProxy(clientSide = "mca.core.forge.ClientProxy", serverSide = "mca.core.forge.ServerProxy")
    public static ServerProxy proxy;
    public static CreativeTabs creativeTab;
    @Mod.Instance
    private static MCA instance;
    private static Logger logger;
    private static Localizer localizer;
    private static Config config;


    public static Logger getLog() {
        if (logger == null) {
            logger = org.apache.logging.log4j.LogManager.getLogger(MODID);
        }
        return logger;
    }

    public static MCA getInstance() {
        return instance;
    }

    public static Localizer getLocalizer() {
        return localizer;
    }

    public static Config getConfig() {
        return config;
    }



    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        proxy.registerEntityRenderers();
        localizer = new Localizer();
        config = new Config(event);
        creativeTab = new CreativeTabs("MCA") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ItemsMCA.ENGAGEMENT_RING);
            }
        };
        MinecraftForge.EVENT_BUS.register(new EventHooks());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        NetMCA.registerMessages();


    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "EntityVillagerMCA"), EntityVillagerMCA.class, EntityVillagerMCA.class.getSimpleName(), 1120, this, 50, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "GrimReaperMCA"), EntityGrimReaper.class, EntityGrimReaper.class.getSimpleName(), 1121, this, 50, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "ZombieVillagerMCA"), EntityZombieVillagerMCA.class, EntityZombieVillagerMCA.class.getSimpleName(), 1122, this, 50, 2, true);

        // Register TileEntities
        GameRegistry.registerTileEntity(TileEntityTombstone.class, new ResourceLocation(MODID, "tombstone"));

        proxy.registerModelMeshers();
        ItemsMCA.assignCreativeTabs();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        API.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMCA());
        event.registerServerCommand(new CommandAdminMCA());
    }



    public String getRandomSupporter() {

            return API.getRandomName(EnumGender.getRandom());

    }

}
