package mca.enums;

import java.util.Arrays;

public enum EnumReaperAttackState {
    IDLE(0),
    PRE(1),
    POST(2),
    REST(3),
    BLOCK(4);

    private int id;

    EnumReaperAttackState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static EnumReaperAttackState fromId(int id) {
        return Arrays.stream(values()).filter(s -> s.id == id).findFirst().orElse(IDLE);
    }
}
