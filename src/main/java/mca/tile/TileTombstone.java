package mca.tile;

import net.minecraft.tileentity.TileEntitySign;

/** Stores the four editable lines displayed on an MCA tombstone. */
public class TileTombstone extends TileEntitySign {
    @Override
    public boolean getIsEditable() {
        return true;
    }
}
