package mca.enums;

import java.util.Arrays;

public enum EnumDialogueType {
    UNASSIGNED("unassigned"),
    BABY("baby"),
    TODDLER("toddler"),
    TODDLERP("toddlerp"),
    CHILD("child"),
    CHILDP("childp"),
    TEEN("teen"),
    TEENP("teenp"),
    ADULT("adult"),
    ADULTP("adultp"),
    SPOUSE("spouse"),
    ENGAGED("engaged");

    private String id;

    EnumDialogueType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static EnumDialogueType byValue(String value) {
        return Arrays.stream(values()).filter(c -> c.getId().equals(value)).findFirst().orElse(ADULT);
    }

    /**
     * 根据年龄状态获取对应的对话类型
     */
    public static EnumDialogueType fromAgeState(EnumAgeState ageState) {
        switch (ageState) {
            case BABY:
                return BABY;
            case TODDLER:
                return TODDLER;
            case CHILD:
                return CHILD;
            case TEEN:
                return TEEN;
            case ADULT:
                return ADULT;
            default:
                return UNASSIGNED;
        }
    }

    /**
     * 获取玩家子女版本的对话类型
     */
    public EnumDialogueType toChildVariant() {
        switch (this) {
            case TODDLER:
                return TODDLERP;
            case CHILD:
                return CHILDP;
            case TEEN:
                return TEENP;
            case ADULT:
                return ADULTP;
            default:
                return UNASSIGNED;
        }
    }
}
