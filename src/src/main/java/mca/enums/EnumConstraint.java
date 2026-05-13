package mca.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum EnumConstraint {
    FAMILY("family"),
    NOT_FAMILY("notfamily"),
    ADULTS("adults"),
    SPOUSE("spouse"),
    NOT_SPOUSE("notspouse"),
    HIDE_ON_FAIL("hideonfail");

    private String id;

    EnumConstraint(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static List<EnumConstraint> fromStringList(String constraints) {
        List<EnumConstraint> list = new ArrayList<>();

        if (constraints != null && !constraints.isEmpty()) {
            String[] splitConstraints = constraints.split("\\|");

            for (String s : splitConstraints) {
                EnumConstraint constraint = byValue(s);
                if (s != null) {
                    list.add(constraint);
                }
            }
        }

        return list;
    }

    public static EnumConstraint byValue(String value) {
        Optional<EnumConstraint> state = Arrays.stream(values()).filter((e) -> e.id.equals(value)).findFirst();
        return state.orElse(null);
    }

}
