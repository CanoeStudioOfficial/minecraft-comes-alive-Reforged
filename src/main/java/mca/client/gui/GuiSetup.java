package mca.client.gui;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.enums.EnumGender;
import mca.enums.EnumSetupType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSetup extends GuiScreen {
    private int page = 1;
    private EnumGender genderPreference = EnumGender.UNASSIGNED;
    private GuiTextField nameField;

    // Page 1
    private GuiButton maleButton;
    private GuiButton femaleButton;

    // Page 2
    private GuiButton malesButton;
    private GuiButton eitherButton;
    private GuiButton femalesButton;

    // Page 3
    private GuiButton continueButton;

    // Page 4
    private GuiButton aloneButton;
    private GuiButton familyButton;
    private GuiButton villageButton;
    private GuiButton noneButton;
    private GuiButton cancelButton;

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
            malesButton = new GuiButton(3, width / 2 - 100, height / 2 + 5, 60, buttonHeight, MCA.getLocalizer().localize("gui.button.males"));
            eitherButton = new GuiButton(4, width / 2 - 30, height / 2 + 5, 60, buttonHeight, MCA.getLocalizer().localize("gui.button.either"));
            femalesButton = new GuiButton(5, width / 2 + 40, height / 2 + 5, 60, buttonHeight, MCA.getLocalizer().localize("gui.button.females"));
            buttonList.add(malesButton);
            buttonList.add(eitherButton);
            buttonList.add(femalesButton);
        } else if (page == 3) {
            if (nameField == null) {
                nameField = new GuiTextField(10, fontRenderer, width / 2 - 100, height / 2 - 10, 200, buttonHeight);
                nameField.setText(mc.player.getName());
            }
            continueButton = new GuiButton(6, width / 2 - buttonWidth / 2, height / 2 + 25, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.continue"));
            continueButton.enabled = !nameField.getText().trim().isEmpty();
            buttonList.add(continueButton);
        } else if (page == 4) {
            aloneButton = new GuiButton(7, width / 2 - buttonWidth / 2, height / 2 - 45, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.alone"));
            familyButton = new GuiButton(8, width / 2 - buttonWidth / 2, height / 2 - 15, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.family"));
            villageButton = new GuiButton(9, width / 2 - buttonWidth / 2, height / 2 + 15, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.village"));
            noneButton = new GuiButton(10, width / 2 - buttonWidth / 2, height / 2 + 45, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.none"));
            buttonList.add(aloneButton);
            buttonList.add(familyButton);
            buttonList.add(villageButton);
            buttonList.add(noneButton);
            cancelButton = new GuiButton(11, width / 2 - buttonWidth / 2, height / 2 + 75, buttonWidth, buttonHeight, MCA.getLocalizer().localize("gui.button.cancel"));
            buttonList.add(cancelButton);
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
            if (button == malesButton) {
                genderPreference = EnumGender.MALE;
                page = 3;
                initGui();
            } else if (button == eitherButton) {
                genderPreference = EnumGender.UNASSIGNED;
                page = 3;
                initGui();
            } else if (button == femalesButton) {
                genderPreference = EnumGender.FEMALE;
                page = 3;
                initGui();
            }
        } else if (page == 3) {
            if (button == continueButton) {
                page = 4;
                initGui();
            }
        } else if (page == 4) {
            if (button == aloneButton) {
                sendChoice(EnumSetupType.ALONE);
                mc.displayGuiScreen(null);
            } else if (button == familyButton) {
                sendChoice(EnumSetupType.FAMILY);
                mc.displayGuiScreen(null);
            } else if (button == villageButton) {
                sendChoice(EnumSetupType.VILLAGE);
                mc.displayGuiScreen(null);
            } else if (button == noneButton) {
                sendChoice(EnumSetupType.NONE);
                mc.displayGuiScreen(null);
            } else if (button == cancelButton) {
                mc.displayGuiScreen(null);
            }
        }
    }

    private void sendChoice(EnumSetupType type) {
        NetMCA.INSTANCE.sendToServer(new NetMCA.SetupComplete(type, nameField == null ? "" : nameField.getText(), genderPreference));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (page == 3 && nameField != null) {
            nameField.textboxKeyTyped(typedChar, keyCode);
            initGui();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (page == 3 && nameField != null) {
            nameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.title.setup"), width / 2, height / 2 - 80, 0xFFFFFF);
        
        if (page == 1) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.label.choose_gender"), width / 2, height / 2 - 50, 0xFFFFFF);
        } else if (page == 2) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.label.choose_preference"), width / 2, height / 2 - 50, 0xFFFFFF);
        } else if (page == 3) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.label.choose_name"), width / 2, height / 2 - 45, 0xFFFFFF);
            if (nameField != null) nameField.drawTextBox();
        } else if (page == 4) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.label.choose_destiny"), width / 2, height / 2 - 70, 0xFFFFFF);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
