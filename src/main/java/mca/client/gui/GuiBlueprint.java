package mca.client.gui;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.structure.McaStructure;
import mca.structure.McaStructureRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiBlueprint extends GuiScreen {
    private final List<McaStructure> structures = new ArrayList<>();
    private GuiButton previousButton;
    private GuiButton nextButton;
    private GuiButton scanButton;
    private GuiButton closeButton;
    private int page;

    @Override
    public void initGui() {
        structures.clear();
        structures.addAll(McaStructureRegistry.all());
        buttonList.clear();
        previousButton = new GuiButton(1, width / 2 - 115, height - 45, 55, 20, "<");
        nextButton = new GuiButton(2, width / 2 - 55, height - 45, 55, 20, ">");
        scanButton = new GuiButton(3, width / 2 + 5, height - 45, 100, 20, MCA.getLocalizer().localize("gui.blueprint.scan"));
        closeButton = new GuiButton(4, width / 2 - 50, height - 20, 100, 20, MCA.getLocalizer().localize("gui.button.exit"));
        buttonList.add(previousButton);
        buttonList.add(nextButton);
        buttonList.add(scanButton);
        buttonList.add(closeButton);
        updateButtonState();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == previousButton) {
            page = Math.max(0, page - 1);
        } else if (button == nextButton) {
            page = Math.min(Math.max(0, structures.size() - 1), page + 1);
        } else if (button == scanButton) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.BlueprintScan());
        } else if (button == closeButton) {
            mc.displayGuiScreen(null);
        }
        updateButtonState();
    }

    private void updateButtonState() {
        previousButton.enabled = page > 0;
        nextButton.enabled = page + 1 < structures.size();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.title.blueprint"), width / 2, 20, 0xFFFFFF);
        if (structures.isEmpty()) {
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.blueprint.empty"), width / 2, height / 2, 0xFFFFFF);
        } else {
            McaStructure structure = structures.get(page);
            drawCenteredString(fontRenderer, structure.getId(), width / 2, 55, 0x55AAFF);
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.blueprint.dimensions", String.valueOf(structure.getWidth()), String.valueOf(structure.getHeight()), String.valueOf(structure.getLength())), width / 2, 78, 0xFFFFFF);
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.blueprint.blocks", String.valueOf(structure.getBlockCount())), width / 2, 96, 0xFFFFFF);
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.blueprint.palette", String.valueOf(structure.getPalette().size())), width / 2, 114, 0xFFFFFF);
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.blueprint.page", String.valueOf(page + 1), String.valueOf(structures.size())), width / 2, 140, 0xAAAAAA);
            drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.blueprint.independent"), width / 2, height / 2 + 20, 0x77CC77);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
