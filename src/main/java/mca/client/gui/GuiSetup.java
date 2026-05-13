package mca.client.gui;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.enums.EnumGender;
import mca.enums.EnumSetupType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSetup extends GuiScreen {
    private int page = 1;

    // Page 1
    private GuiButton maleButton;
    private GuiButton femaleButton;

    // Page 2
    private GuiButton aloneButton;
    private GuiButton familyButton;
    private GuiButton villageButton;
    private GuiButton noneButton;

    @Override
    public void initGui() {
        buttonList.clear();
        int buttonWidth = 150;
        int buttonHeight = 20;

        if (page == 1) {
            maleButton = new GuiButton(1, width / 2 - buttonWidth / 2, height / 2 - 25, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.male"));
            femaleButton = new GuiButton(2, width / 2 - buttonWidth / 2, height / 2 + 5, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.female"));
            buttonList.add(maleButton);
            buttonList.add(femaleButton);
        } else if (page == 2) {
            aloneButton = new GuiButton(3, width / 2 - buttonWidth / 2, height / 2 - 45, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.alone"));
            familyButton = new GuiButton(4, width / 2 - buttonWidth / 2, height / 2 - 15, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.family"));
            villageButton = new GuiButton(5, width / 2 - buttonWidth / 2, height / 2 + 15, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.village"));
            noneButton = new GuiButton(6, width / 2 - buttonWidth / 2, height / 2 + 45, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.none"));
            buttonList.add(aloneButton);
            buttonList.add(familyButton);
            buttonList.add(villageButton);
            buttonList.add(noneButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (page == 1) {
            if (button == maleButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetPlayerGender(EnumGender.MALE));
                page = 2;
                initGui();
            } else if (button == femaleButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetPlayerGender(EnumGender.FEMALE));
                page = 2;
                initGui();
            }
        } else if (page == 2) {
            if (button == aloneButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetupComplete(EnumSetupType.ALONE));
                mc.displayGuiScreen(null);
            } else if (button == familyButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetupComplete(EnumSetupType.FAMILY));
                mc.displayGuiScreen(null);
            } else if (button == villageButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetupComplete(EnumSetupType.VILLAGE));
                mc.displayGuiScreen(null);
            } else if (button == noneButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetupComplete(EnumSetupType.NONE));
                mc.displayGuiScreen(null);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.title.setup"), width / 2, height / 2 - 80, 0xFFFFFF);
        
        if (page == 1) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.label.choose_gender"), width / 2, height / 2 - 50, 0xFFFFFF);
        } else if (page == 2) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.label.choose_destiny"), width / 2, height / 2 - 70, 0xFFFFFF);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
