package mca.core;

import mca.api.API;
import mca.command.CommandAdminMCA;
import mca.command.CommandMCA;
import mca.core.forge.EventHooks;
import mca.core.forge.GuiHandler;
import mca.core.forge.NetMCA;
import mca.util.proxy.CommonProxy;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.core.minecraft.RoseGoldOreGenerator;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.mca.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, guiFactory = "mca.client.MCAGuiFactory")
public class MCA {
    public static final String MODID = Tags.MOD_ID;

    @SidedProxy(clientSide = "mca.util.proxy.ClientProxy", serverSide = "mca.util.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs creativeTab;
    @Mod.Instance
    private static MCA instance;
    private static Logger logger;
    private static Config config;

    public static Logger getLog() {
        return logger;
    }

    public static MCA getInstance() {
        return instance;
    }

    public static Localizer getLocalizer() {
        // 通过proxy获取Localizer，确保只在客户端初始化
        return proxy.getLocalizer();
    }

    public static Config getConfig() {
        return config;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        proxy.registerEntityRenderers(); // 这里会初始化客户端的Localizer
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
        GameRegistry.registerWorldGenerator(new RoseGoldOreGenerator(), MCA.getConfig().roseGoldSpawnWeight);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "EntityVillagerMCA"), EntityVillagerMCA.class, EntityVillagerMCA.class.getSimpleName(), 1120, this, 50, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "GrimReaperMCA"), EntityGrimReaper.class, EntityGrimReaper.class.getSimpleName(), 1121, this, 50, 2, true);
        ProfessionsMCA.registerCareers();

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