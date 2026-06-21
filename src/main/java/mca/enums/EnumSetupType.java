package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumSetupType {
    ALONE(0, "alone"),
    FAMILY(1, "family"),
    VILLAGE(2, "village"),
    NONE(3, "none");

    private final int id;
    private final String name;

    public static EnumSetupType byId(int id) {
        for (EnumSetupType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return NONE;
    }
}
