package mca.enums;

import java.util.Arrays;

public enum EnumDialogueType {
    CHILDP("childp"),
    CHILD("child"),
    ADULT("adult"),
    SPOUSE("spouse");

    private String id;

    EnumDialogueType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static EnumDialogueType byValue(String value) {
        return Arrays.stream(values()).filter(c -> c.getId().equals(value)).findFirst().orElse(null);
    }
}
