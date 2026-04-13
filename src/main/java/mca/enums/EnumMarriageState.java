package mca.enums;

import java.util.Arrays;
import java.util.Optional;

public enum EnumMarriageState {
    NOT_MARRIED(0, "notMarried"),
    PROMISED(1, "promised"),
    ENGAGED(2, "engaged"),
    MARRIED(3, "married"),
    WIDOWED(4, "widow");

    private int id;
    private String icon;

    EnumMarriageState(int id, String icon) {
        this.id = id;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isMarried() {
        return this == MARRIED;
    }

    public static EnumMarriageState byId(int id) {
        Optional<EnumMarriageState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(NOT_MARRIED);
    }
}
