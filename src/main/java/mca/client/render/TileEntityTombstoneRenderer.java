package mca.client.render;

import mca.tile.TileTombstone;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityTombstoneRenderer extends TileEntitySpecialRenderer<TileTombstone> {
    @Override
    public void render(TileTombstone te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        
        int rotation = te.getBlockMetadata();
        float angle = (float) (rotation * 360) / 16.0F;
        GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
        
        // Translate slightly forward to render on the face of the tombstone
        GlStateManager.translate(0.0F, 0.2F, -0.13F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        
        FontRenderer fontrenderer = this.getFontRenderer();
        GlStateManager.depthMask(false);

        for (int i = 0; i < te.signText.length; ++i) {
            ITextComponent itextcomponent = te.signText[i];
            if (itextcomponent != null) {
                String s = itextcomponent.getFormattedText();
                fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - 20, 0);
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
