package mca.entity;

import mca.enums.EnumAgeState;

public final class MCAVillagerDimensions {
    public static final float MAX_STANDING_HEIGHT = 1.95F;
    public static final float MAX_STANDING_WIDTH = 0.6F;
    public static final float SLEEPING_WIDTH = 0.2F;
    public static final float SLEEPING_HEIGHT = 0.2F;

    private MCAVillagerDimensions() {
    }

    public static float getCollisionWidth(EnumAgeState age) {
        return Math.min(age.getWidth(), MAX_STANDING_WIDTH);
    }

    public static float getCollisionHeight(EnumAgeState age) {
        return Math.min(age.getHeight(), MAX_STANDING_HEIGHT);
    }
}
