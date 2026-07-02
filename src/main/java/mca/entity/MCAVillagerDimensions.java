package mca.entity;

import mca.enums.EnumAgeState;

public final class MCAVillagerDimensions {
    public static final double BASE_MOVEMENT_SPEED = 0.5D;
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

    public static float getCollisionWidth(EnumAgeState age, float delta) {
        EnumAgeState next = age.getNext();
        return Math.min(lerp(delta, age.getWidth(), next.getWidth()), MAX_STANDING_WIDTH);
    }

    public static float getCollisionHeight(EnumAgeState age, float delta) {
        EnumAgeState next = age.getNext();
        return Math.min(lerp(delta, age.getHeight(), next.getHeight()), MAX_STANDING_HEIGHT);
    }

    public static float getRenderScale(EnumAgeState age, float delta) {
        EnumAgeState next = age.getNext();
        return lerp(delta, age.getScaleForAge(), next.getScaleForAge());
    }

    public static double getMovementSpeed(EnumAgeState age, float delta) {
        EnumAgeState next = age.getNext();
        return BASE_MOVEMENT_SPEED * lerp(delta, age.getSpeed(), next.getSpeed());
    }

    public static float getSoundPitch(EnumAgeState age, float delta) {
        EnumAgeState next = age.getNext();
        return lerp(delta, age.getPitch(), next.getPitch());
    }

    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
}
