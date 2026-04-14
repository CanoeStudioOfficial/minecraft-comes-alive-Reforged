package mca.enums;

import mca.core.MCA;

import java.util.Arrays;
import java.util.Optional;

public enum EnumAgeState {
    UNASSIGNED(-1, 0.8f, 2.0f, 1.5f, 0.9f, 1.0f, 1.0f, 1.0f),
    BABY(0, 0.3f, 0.5f, 0.4f, 0.45f, 0.4f, 1.5f, 0.0f),
    TODDLER(1, 0.3f, 0.6f, 0.5f, 0.6f, 0.55f, 1.3f, 0.65f),
    CHILD(2, 0.5f, 1.1f, 1f, 0.7f, 0.65f, 1.2f, 0.9f),
    TEEN(3, 0.6f, 1.6f, 1.35f, 0.85f, 0.85f, 1.0f, 1.05f),
    ADULT(4, 0.8f, 2f, 1.5f, 1.0f, 1.0f, 1.0f, 1.0f);

    private int id;
    private float width;
    private float height;
    private float scaleForAge;
    private float modelWidth;
    private float modelHeight;
    private float headScale;
    private float pitch;

    EnumAgeState(int id, float width, float height, float scaleForAge, float modelWidth, float modelHeight, float headScale, float pitch) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.scaleForAge = scaleForAge;
        this.modelWidth = modelWidth;
        this.modelHeight = modelHeight;
        this.headScale = headScale;
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

    public float getModelWidth() {
        return modelWidth;
    }

    public float getModelHeight() {
        return modelHeight;
    }

    public float getHeadScale() {
        return headScale;
    }

    public float getPitch() {
        return pitch;
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

    public String localizedName() {
        return MCA.getLocalizer().localize("enum.agestate." + name().toLowerCase());
    }

    public EnumAgeState getNext() {
        if (this == ADULT) {
            return this;
        }
        return byId(id + 1);
    }
}
