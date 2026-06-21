package mca.enums;

import mca.core.MCA;

import java.util.Arrays;

public enum EnumMoveState {
    MOVE(0, ""),
    STAY(1, "gui.label.staying"),
    FOLLOW(2, "gui.label.following");

    private int id;
    private String friendlyName;

    EnumMoveState(int id, String friendlyName) {
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public int getId() {
        return id;
    }

    public static EnumMoveState byId(int id) {
        return Arrays.stream(values()).filter(s -> s.id == id).findFirst().orElse(MOVE);
    }

    public String getFriendlyName() {
        return MCA.getLocalizer().localize(friendlyName);
    }
}
