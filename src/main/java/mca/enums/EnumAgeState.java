package mca.enums;

import mca.core.MCA;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Optional;

public enum EnumAgeState {
    // 参数: id, width(模型宽度), height(模型高度), breasts(胸部发育), head(头部缩放), speed(移动速度), pitch(音调)
    UNASSIGNED(-1, 1.0f, 0.9f, 1.0f, 1.0f, 1.0f, 1.0f),
    BABY(0, 0.45f, 0.4f, 0.0f, 1.5f, 0.0f, 1.6f),
    TODDLER(1, 0.6f, 0.55f, 0.0f, 1.3f, 0.65f, 1.4f),
    CHILD(2, 0.7f, 0.65f, 0.0f, 1.2f, 0.9f, 1.2f),
    TEEN(3, 0.85f, 0.85f, 0.5f, 1.0f, 1.05f, 1.0f),
    ADULT(4, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);

    private static final EnumAgeState[] VALUES = values();
    private static final int MAX_AGE = 24000; // 默认最大年龄 (20分钟 = 24000 ticks)

    private final int id;
    private final float width;      // 模型宽度
    private final float height;     // 模型高度
    private final float breasts;    // 胸部发育程度
    private final float head;       // 头部缩放
    private final float speed;      // 移动速度
    private final float pitch;      // 音调

    EnumAgeState(int id, float width, float height, float breasts, float head, float speed, float pitch) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.breasts = breasts;
        this.head = head;
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

    public float getBreasts() {
        return breasts;
    }

    public float getHead() {
        return head;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPitch() {
        return pitch;
    }

    public static int getMaxAge() {
        return MAX_AGE;
    }

    public static int getStageDuration() {
        return getMaxAge() / 4;
    }

    public static EnumAgeState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }

    public static EnumAgeState random() {
        return byCurrentAge((int) (-MCA.getRandom().nextFloat() * getMaxAge()));
    }

    /**
     * Returns a float ranging from 0 to 1 representing the progress between stages.
     */
    public static float getDelta(float age) {
        return 1 - (-age % getStageDuration()) / getStageDuration();
    }

    public static int getIdByAge(int age) {
        return MathHelper.clamp(1 + (age + getMaxAge()) / getStageDuration(), 0, 5);
    }

    public static EnumAgeState byCurrentAge(int age) {
        return byId(getIdByAge(age));
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

    public String getName() {
        return name().toLowerCase();
    }

    public EnumAgeState getNext() {
        if (this == ADULT) {
            return this;
        }
        return byId(ordinal());
    }

    public int toAge() {
        return (ordinal() - 1) * getStageDuration() - getMaxAge();
    }
}
