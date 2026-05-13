package mca.enums;

import java.util.Arrays;
import java.util.Optional;

public enum EnumMarriageState {
    NOT_MARRIED(0),
    ENGAGED(1),
    MARRIED(2);

    private int id;

    EnumMarriageState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static EnumMarriageState byId(int id) {
        Optional<EnumMarriageState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(NOT_MARRIED);
    }
}
