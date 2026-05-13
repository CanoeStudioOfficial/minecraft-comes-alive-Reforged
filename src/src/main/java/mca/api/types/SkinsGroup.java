package mca.api.types;

import mca.enums.EnumGender;

public class SkinsGroup {
    private String gender;
    private String profession;
    private String[] paths;

    public SkinsGroup(String gender, String profession, String[] paths) {
        this.gender = gender;
        this.profession = profession;
        this.paths = paths;
    }

    public String getProfession() {
        return profession;
    }

    public String[] getPaths() {
        return paths;
    }

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }
}
