package mca.client.gui;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.data.FamilyTree;
import mca.entity.data.FamilyTreeNode;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiFamilyTree extends GuiScreen {
    private static final int HORIZONTAL_SPACING = 30;
    private static final int VERTICAL_SPACING = 80;
    private static final int NODE_WIDTH = 100;
    private static final int NODE_HEIGHT = 40;
    private static final ResourceLocation FAMILY_ICONS = new ResourceLocation(MCA.MODID, "textures/gui/family_icons.png");

    private final Map<UUID, FamilyTreeNode> nodes = new HashMap<>();
    private UUID focusedEntityId;
    private double scrollX;
    private double scrollY;
    private float zoom = 1.0f;

    private int lastMouseX;
    private int lastMouseY;

    public GuiFamilyTree(UUID entityId) {
        this.focusedEntityId = entityId;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 50, height - 30, 100, 20, MCA.getLocalizer().localize("gui.button.close")));
        
        // Request tree data from server
        NetMCA.INSTANCE.sendToServer(new NetMCA.GetFamilyTreeRequest(focusedEntityId));
    }

    public void setTreeData(NBTTagCompound nbt) {
        nodes.clear();
        for (String key : nbt.getKeySet()) {
            nodes.put(UUID.fromString(key), new FamilyTreeNode(nbt.getCompoundTag(key)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Handle dragging
        if (Mouse.isButtonDown(0)) {
            if (lastMouseX != 0 || lastMouseY != 0) {
                scrollX += (mouseX - lastMouseX);
                scrollY += (mouseY - lastMouseY);
            }
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else {
            lastMouseX = 0;
            lastMouseY = 0;
        }

        // Scissor for main view area
        ScaledResolution res = new ScaledResolution(mc);
        int scale = res.getScaleFactor();
        int scissorY = 40;
        int scissorHeight = height - 80;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // Scissor coordinates are in pixels from bottom-left. 
        // In GUI coordinates, 0 is top. In OpenGL, 0 is bottom.
        GL11.glScissor(0, (height - (scissorY + scissorHeight)) * scale, mc.displayWidth, scissorHeight * scale);

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2 + scrollX, height / 2 + scrollY, 0);
        GlStateManager.scale(zoom, zoom, 1.0);

        renderTree(mouseX, mouseY);

        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // UI overlay
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.family_tree.title"), width / 2, 10, 0xFFFFFF);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderTree(int mouseX, int mouseY) {
        FamilyTreeNode root = nodes.get(focusedEntityId);
        if (root == null) return;

        // Simple layout: current node at 0,0. Parents above, children below.
        drawNode(root, 0, 0, mouseX, mouseY);
        
        // Draw Parents
        int parentY = -VERTICAL_SPACING;
        FamilyTreeNode father = nodes.get(root.getFather());
        FamilyTreeNode mother = nodes.get(root.getMother());
        
        if (father != null) {
            drawLine(-HORIZONTAL_SPACING, parentY + NODE_HEIGHT/2, 0, -NODE_HEIGHT/2);
            drawNode(father, -HORIZONTAL_SPACING, parentY, mouseX, mouseY);
        }
        if (mother != null) {
            drawLine(HORIZONTAL_SPACING, parentY + NODE_HEIGHT/2, 0, -NODE_HEIGHT/2);
            drawNode(mother, HORIZONTAL_SPACING, parentY, mouseX, mouseY);
        }

        // Draw Children
        int childrenY = VERTICAL_SPACING;
        Set<UUID> children = root.getChildren();
        int childXOffset = -(children.size() - 1) * HORIZONTAL_SPACING / 2;
        
        int i = 0;
        for (UUID childId : children) {
            FamilyTreeNode child = nodes.get(childId);
            if (child != null) {
                int childX = childXOffset + i * HORIZONTAL_SPACING;
                drawLine(0, NODE_HEIGHT/2, childX, childrenY - NODE_HEIGHT/2);
                drawNode(child, childX, childrenY, mouseX, mouseY);
            }
            i++;
        }
    }

    private void drawNode(FamilyTreeNode node, int x, int y, int mouseX, int mouseY) {
        int left = x - NODE_WIDTH / 2;
        int top = y - NODE_HEIGHT / 2;
        int right = x + NODE_WIDTH / 2;
        int bottom = y + NODE_HEIGHT / 2;

        int color = 0xAA000000;
        if (node.getId().equals(focusedEntityId)) {
            color = 0xAA444400;
        }

        drawRect(left, top, right, bottom, color);
        drawHorizontalLine(left, right, top, 0xFFAAAAAA);
        drawHorizontalLine(left, right, bottom, 0xFFAAAAAA);
        drawVerticalLine(left, top, bottom, 0xFFAAAAAA);
        drawVerticalLine(right, top, bottom, 0xFFAAAAAA);

        int textColor = node.getGender() == EnumGender.MALE ? 0x5555FF : 0xFF55FF;
        if (node.isDeceased()) textColor = 0x777777;

        drawCenteredString(fontRenderer, node.getName(), x, y - 10, textColor);
        
        // Localize profession
        String profession = node.getProfession();
        if (profession.contains(":")) {
            profession = profession.split(":")[1];
        }
        String localizedProfession = MCA.getLocalizer().localize("entity.Villager." + profession);
        drawCenteredString(fontRenderer, localizedProfession, x, y + 2, 0xAAAAAA);

        // Draw icons
        mc.getTextureManager().bindTexture(FAMILY_ICONS);
        GlStateManager.color(1, 1, 1, 1);
        
        if (node.isDeceased()) {
            // Deceased icon at top-right of node
            drawModalRectWithCustomSizedTexture(right - 18, top + 2, 16, 16, 16, 16, 256, 256);
            // Draw deceased label
            drawCenteredString(fontRenderer, "(" + MCA.getLocalizer().localize("gui.family_tree.label.deceased") + ")", x, y + 12, 0x777777);
        }

        if (node.getMarriageState() == EnumMarriageState.MARRIED) {
            drawModalRectWithCustomSizedTexture(left + 2, bottom - 18, 16, 0, 16, 16, 256, 256);
        } else if (node.getMarriageState() == EnumMarriageState.ENGAGED) {
            drawModalRectWithCustomSizedTexture(left + 2, bottom - 18, 64, 0, 16, 16, 256, 256);
        }
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        GL11.glLineWidth(2.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x2, y2);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
