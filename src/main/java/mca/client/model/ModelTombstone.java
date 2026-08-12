package mca.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/** The 5.3.1 tombstone silhouette, kept as a TESR so its text remains dynamic. */
public class ModelTombstone extends ModelBase {
    private final ModelRenderer base;
    private final ModelRenderer textArea;
    private final ModelRenderer topCurve;

    public ModelTombstone() {
        textureWidth = 64;
        textureHeight = 64;

        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 14, 1, 6);
        base.setRotationPoint(-7F, 23F, -3F);

        textArea = new ModelRenderer(this, 0, 11);
        textArea.addBox(0F, 0F, 0F, 12, 8, 2);
        textArea.setRotationPoint(-6F, 15F, -1F);

        topCurve = new ModelRenderer(this, 35, 18);
        topCurve.addBox(0F, 0F, 0F, 10, 1, 2);
        topCurve.setRotationPoint(-5F, 14F, -1F);
    }

    public void renderTombstone(float scale) {
        base.render(scale);
        textArea.render(scale);
        topCurve.render(scale);
    }
}
