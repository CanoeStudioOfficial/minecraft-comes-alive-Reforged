package mca.enums;

import mca.core.MCA;

import java.util.Arrays;
import java.util.Optional;

public enum EnumAgeState {
    UNASSIGNED(-1, 0.6f, 1.95f, 1.0f, 1.0f, 1.0f),
    BABY(0, 0.27f, 0.78f, 0.4f, 0.0f, 1.6f),
    TODDLER(1, 0.36f, 1.07f, 0.55f, 0.65f, 1.4f),
    CHILD(2, 0.42f, 1.27f, 0.65f, 0.9f, 1.2f),
    TEEN(3, 0.51f, 1.66f, 0.85f, 1.05f, 1.0f),
    ADULT(4, 0.6f, 1.95f, 1.0f, 1.0f, 1.0f);

    private int id;
    private float width;
    private float height;
    private float scaleForAge;
    private float speed;
    private float pitch;

    EnumAgeState(int id, float width, float height, float scaleForAge, float speed, float pitch) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.scaleForAge = scaleForAge;
        this.speed = speed;
        this.pitch = pitch;
    }

    public int getId() {
        return id;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getScaleForAge() {
        return scaleForAge;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPitch() {
        return pitch;
    }

    public EnumAgeState getNext() {
        if (this == ADULT || this == UNASSIGNED) {
            return this;
        }

        return byId(id + 1);
    }

    public static EnumAgeState byId(int id) {
        Optional<EnumAgeState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(UNASSIGNED);
    }

    public static EnumAgeState byCurrentAge(int startingAge, int growingAge) {
        if (growingAge >= 0) {
            return EnumAgeState.ADULT;
        }

        int step = startingAge / 4;
        if (growingAge >= step) {
            return EnumAgeState.TEEN;
        } else if (growingAge >= step * 2) {
            return EnumAgeState.CHILD;
        } else if (growingAge >= step * 3) {
            return EnumAgeState.TODDLER;
        } else {
            return EnumAgeState.BABY;
        }
    }

    public static float getDelta(int startingAge, int growingAge) {
        if (startingAge >= 0 || growingAge >= 0) {
            return 1.0F;
        }

        int stageDuration = Math.max(1, Math.abs(startingAge) / 4);
        return 1.0F - Math.floorMod(-growingAge, stageDuration) / (float) stageDuration;
    }

    public String localizedName() {
        return MCA.getLocalizer().localize("enum.agestate." + name().toLowerCase());
    }
}
