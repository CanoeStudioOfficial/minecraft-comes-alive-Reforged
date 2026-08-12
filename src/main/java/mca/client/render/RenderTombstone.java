package mca.client.render;

import mca.client.model.ModelTombstone;
import mca.tile.TileTombstone;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/** Renders the 5.3.1 stone model and the TileEntitySign text on its face. */
public class RenderTombstone extends TileEntitySpecialRenderer<TileTombstone> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("mca:textures/blocks/tombstone.png");
    private final ModelTombstone model = new ModelTombstone();

    @Override
    public void render(TileTombstone tile, double x, double y, double z, float partialTicks,
                       int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 1.59F, z + 0.53F);
        GlStateManager.rotate(-rotationForMeta(tile.getBlockMetadata()), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.5F, 0.0F);
        GlStateManager.scale(1.4F, 1.4F, 1.4F);

        bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        model.renderTombstone(0.0625F);
        GlStateManager.popMatrix();

        FontRenderer font = getFontRenderer();
        GlStateManager.translate(0.0F, -1.15F, 0.07F);
        GlStateManager.scale(0.017F / 2.0F, -0.017F / 2.0F, 0.017F / 2.0F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.017F);
        GlStateManager.depthMask(false);

        for (int line = 0; line < tile.signText.length; line++) {
            ITextComponent component = tile.signText[line];
            String text = component == null ? "" : component.getUnformattedText();
            if (line == tile.lineBeingEdited) {
                text = "> " + text + " <";
            }
            font.drawString(text, -font.getStringWidth(text) / 2, line * 10 - tile.signText.length * 5, 0x000000);
        }

        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private float rotationForMeta(int meta) {
        return meta * 360.0F / 16.0F;
    }
}
