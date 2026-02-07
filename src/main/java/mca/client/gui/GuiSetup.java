package mca.client.gui;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDestinyChoice;
import mca.enums.EnumGender;
import mca.util.SchematicLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiSetup extends GuiScreen {
    private static final ResourceLocation setupLogo = new ResourceLocation("mca:textures/setup.png");
    
    private final EntityPlayer player;
    private final PlayerSaveData data;
    private EnumDestinyChoice destinyChoice;
    private GuiTextField nameTextField;

    private int page;

    public GuiSetup(EntityPlayer player) {
        super();
        this.player = player;
        this.data = PlayerSaveData.get(player);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        page = 1;
        drawControls();
        player.sendMessage(new net.minecraft.util.text.TextComponentString("Welcome to MCA! Please answer the following questions to begin."));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (page == 3 && nameTextField != null) {
            nameTextField.updateCursorCounter();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (page == 6) {
            drawBackground(0);
        } else {
            drawDefaultBackground();
        }

        GlStateManager.pushMatrix();
        {
            GlStateManager.scale(0.55D, 0.25D, 1.0D);
            mc.getTextureManager().bindTexture(setupLogo);
            drawTexturedModalRect((int)((width / 2 + 62) / 0.55D), (int)((height / 2 - 120) / 0.25D), 0, 0, 256, 256);
        }
        GlStateManager.popMatrix();

        if (page == 1) {
            drawCenteredString(fontRenderer, "Are you a male, or a female?", width / 2, 120, 0xffffff);
        } else if (page == 2) {
            drawCenteredString(fontRenderer, "Which do you prefer?", width / 2, 120, 0xffffff);
        } else if (page == 3 && nameTextField != null) {
            drawCenteredString(fontRenderer, "What is your name?", width / 2, 100, 0xffffff);
            nameTextField.drawTextBox();
        } else if (page == 4) {
            drawCenteredString(fontRenderer, "Choose your destiny...", width / 2, 70, 0xffffff);
        } else if (page == 5) {
            drawCenteredString(fontRenderer, "WARNING: This destiny can potentially be destructive to your world.", width / 2, 70, 0xffffff);
            drawCenteredString(fontRenderer, "This option works best on flat land with no other structures nearby. Continue?", width / 2, 85, 0xffffff);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void cleanUpOnClose() {
        if (MCA.destinyCenterPoint != null) {
            // Simplified cleanup - in the new version we might want to handle this differently
            // but for now let's try to replicate the logic of clearing the test structure
            SchematicLoader.clearStructure("assets/mca/schematic/destiny-test.schematic", MCA.destinyCenterPoint, player.world);
        }

        if (player instanceof EntityPlayerSP) {
            EntityPlayerSP playerSP = (EntityPlayerSP) player;
            MCA.playPortalAnimation = true;
            playerSP.timeInPortal = 6.0F;
            playerSP.prevTimeInPortal = 0.0F;
        }

        MCA.destinySpawnFlag = false;
        player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (page == 3 && nameTextField != null) {
            nameTextField.textboxKeyTyped(typedChar, keyCode);
            // Update "Continue" button enabled state
            for (GuiButton button : buttonList) {
                if (button.id == 6) {
                    button.enabled = !nameTextField.getText().trim().isEmpty();
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (page == 3 && nameTextField != null) {
            nameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Page switching logic
        int oldPage = page;
        switch (button.id) {
            case 0: page = page == 6 ? 1 : page - 1; break;
            case 1: case 2: page = 2; break;
            case 3: case 4: case 5: page = 3; break;
            case 6: 
                if (MCA.getConfig().enableStructureSpawning) {
                    page = 4;
                } else {
                    setDestinyComplete();
                    cleanUpOnClose();
                    mc.displayGuiScreen(null);
                    NetMCA.INSTANCE.sendToServer(new NetMCA.DestinyChoice(EnumDestinyChoice.NONE.getId()));
                }
                break;
            default:
                // No page change for other buttons yet
        }

        // Button actions
        switch (button.id) {
            case 1: data.setGender(EnumGender.MALE); break;
            case 2: data.setGender(EnumGender.FEMALE); break;
            case 3: data.setGenderPreference(EnumGender.MALE); break;
            case 4: data.setGenderPreference(EnumGender.UNASSIGNED); break;
            case 5: data.setGenderPreference(EnumGender.FEMALE); break;
            case 6: 
                data.setMcaName(nameTextField.getText());
                // Logic already handled in page switching for 'Done' case
                break;
            case 7: destinyChoice = EnumDestinyChoice.FAMILY; page = 5; break;
            case 8: destinyChoice = EnumDestinyChoice.ALONE; page = 5; break;
            case 9: destinyChoice = EnumDestinyChoice.VILLAGE; page = 5; break;
            case 10: // No destiny
                setDestinyComplete();
                cleanUpOnClose();
                mc.displayGuiScreen(null);
                NetMCA.INSTANCE.sendToServer(new NetMCA.DestinyChoice(EnumDestinyChoice.NONE.getId()));
                break;
            case 11: // Confirmation button to spawn destiny area
                setDestinyComplete();
                cleanUpOnClose();
                mc.displayGuiScreen(null);
                NetMCA.INSTANCE.sendToServer(new NetMCA.DestinyChoice(destinyChoice.getId()));
                break;
            case 12: page = 4; break;
            case 13: 
                NetMCA.INSTANCE.sendToServer(new NetMCA.DestinyChoice(EnumDestinyChoice.CANCEL.getId()));
                cleanUpOnClose();
                mc.displayGuiScreen(null);
                break;
        }

        if (oldPage != page) {
            drawControls();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void drawControls() {
        buttonList.clear();
        
        if (page > 1) {
            buttonList.add(new GuiButton(0, width / 2 - 200, height / 2 + 90, 65, 20, "Back"));
        }

        if (page == 1) {
            buttonList.add(new GuiButton(1, width / 2 - 65, height / 2 + 10, 65, 20, TextFormatting.AQUA + "Male"));
            buttonList.add(new GuiButton(2, width / 2 + 2, height / 2 + 10, 65, 20, TextFormatting.LIGHT_PURPLE + "Female"));
        } else if (page == 2) {
            buttonList.add(new GuiButton(3, width / 2 - 97, height / 2 + 10, 65, 20, TextFormatting.AQUA + "Males"));
            buttonList.add(new GuiButton(4, width / 2 - 32, height / 2 + 10, 65, 20, TextFormatting.GREEN + "Either"));
            buttonList.add(new GuiButton(5, width / 2 + 33, height / 2 + 10, 65, 20, TextFormatting.LIGHT_PURPLE + "Females"));
        } else if (page == 3) {
            if (nameTextField == null) {
                nameTextField = new GuiTextField(-3, fontRenderer, width / 2 - 100, height / 2 - 5, 200, 20);
                nameTextField.setText(player.getName());
            }
            GuiButton doneButton = new GuiButton(6, width / 2 - 32, height / 2 + 30, 65, 20, MCA.getConfig().enableStructureSpawning ? "Continue" : "Done");
            doneButton.enabled = !nameTextField.getText().trim().isEmpty();
            buttonList.add(doneButton);
        } else if (page == 4) {
            buttonList.add(new GuiButton(7, width / 2 - 46, height / 2 - 40, 95, 20, "I have a family."));
            buttonList.add(new GuiButton(8, width / 2 - 46, height / 2 - 20, 95, 20, "I live alone."));
            buttonList.add(new GuiButton(9, width / 2 - 46, height / 2 + 0, 95, 20, "I live in a village."));
            buttonList.add(new GuiButton(10, width / 2 - 46, height / 2 + 20, 95, 20, "None of these."));
        } else if (page == 5) {
            buttonList.add(new GuiButton(11, width / 2 - 46, height / 2 - 20, 95, 20, "Yes"));
            buttonList.add(new GuiButton(12, width / 2 - 46, height / 2 - 0, 95, 20, "No"));
            buttonList.add(new GuiButton(13, width / 2 - 46, height / 2 + 20, 95, 20, "Cancel"));
        }
    }

    private void setDestinyComplete() {
        data.setHasChosenDestiny(true);
    }
}
