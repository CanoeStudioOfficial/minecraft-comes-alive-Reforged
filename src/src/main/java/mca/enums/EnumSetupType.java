package mca.enums;

public enum EnumSetupType {
    ALONE(0, "alone"),
    FAMILY(1, "family"),
    VILLAGE(2, "village"),
    NONE(3, "none");

    private final int id;
    private final String name;

    EnumSetupType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static EnumSetupType byId(int id) {
        for (EnumSetupType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return NONE;
    }
}
