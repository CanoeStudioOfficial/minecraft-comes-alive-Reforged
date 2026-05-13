package mca.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public enum EnumGender {
    UNASSIGNED(0, "unassigned"),
    MALE(1, "male"),
    FEMALE(2, "female");

    private int id;
    private String strName;

    EnumGender(int id, String strName) {
        this.id = id;
        this.strName = strName;
    }

    public int getId() {
        return id;
    }

    public String getStrName() {
        return strName;
    }

    public static EnumGender byId(int id) {
        Optional<EnumGender> gender = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return gender.orElse(UNASSIGNED);
    }

    public static EnumGender getRandom() {
        return new Random().nextBoolean() ? MALE : FEMALE;
    }

    public static EnumGender byName(String name) {
        Optional<EnumGender> gender = Arrays.stream(values()).filter((e) -> e.getStrName().equals(name)).findFirst();
        return gender.orElse(UNASSIGNED);
    }
}
