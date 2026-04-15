package mca.enums;

import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public enum EnumConstraint {
    // 家庭关系约束
    FAMILY("family", (villager, player) -> villager.playerIsParent(player)),
    NOT_FAMILY("notfamily", (villager, player) -> !villager.playerIsParent(player)),

    // 年龄阶段约束
    BABY("baby", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) == EnumAgeState.BABY),
    NOT_BABY("!baby", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) != EnumAgeState.BABY),

    TODDLER("toddler", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) == EnumAgeState.TODDLER),
    NOT_TODDLER("!toddler", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) != EnumAgeState.TODDLER),

    CHILD("child", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) == EnumAgeState.CHILD),
    NOT_CHILD("!child", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) != EnumAgeState.CHILD),

    TEEN("teen", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) == EnumAgeState.TEEN),
    NOT_TEEN("!teen", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) != EnumAgeState.TEEN),

    ADULT("adult", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) == EnumAgeState.ADULT),
    NOT_ADULT("!adult", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) != EnumAgeState.ADULT),

    // 婚姻状态约束
    SPOUSE("spouse", (villager, player) -> villager.isMarriedTo(player.getUniqueID())),
    NOT_SPOUSE("notspouse", (villager, player) -> !villager.isMarriedTo(player.getUniqueID())),

    ENGAGED("engaged", (villager, player) -> villager.isEngagedTo(player.getUniqueID())),
    NOT_ENGAGED("!engaged", (villager, player) -> !villager.isEngagedTo(player.getUniqueID())),

    PROMISED("promised", (villager, player) -> villager.isPromisedTo(player.getUniqueID())),
    NOT_PROMISED("!promised", (villager, player) -> !villager.isPromisedTo(player.getUniqueID())),

    // 父母/子女关系约束
    PARENT("parent", (villager, player) -> villager.playerIsParent(player)),
    NOT_PARENT("!parent", (villager, player) -> !villager.playerIsParent(player)),

    KIDS("kids", (villager, player) -> villager.get(EntityVillagerMCA.HAS_BABY)),
    NOT_KIDS("!kids", (villager, player) -> !villager.get(EntityVillagerMCA.HAS_BABY)),

    // 交易能力约束
    TRADER("trader", (villager, player) -> villager.getProfessionForge() != ProfessionsMCA.guard 
            && villager.getProfessionForge() != ProfessionsMCA.bandit
            && villager.getProfessionForge() != ProfessionsMCA.child),
    NOT_TRADER("!trader", (villager, player) -> villager.getProfessionForge() == ProfessionsMCA.guard 
            || villager.getProfessionForge() == ProfessionsMCA.bandit
            || villager.getProfessionForge() == ProfessionsMCA.child),

    // 旧版兼容约束
    ADULTS("adults", (villager, player) -> EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)) == EnumAgeState.ADULT),
    HIDE_ON_FAIL("hideonfail", (villager, player) -> true);

    private String id;
    private BiPredicate<EntityVillagerMCA, EntityPlayer> predicate;

    EnumConstraint(String id) {
        this.id = id;
        this.predicate = (v, p) -> true;
    }

    EnumConstraint(String id, BiPredicate<EntityVillagerMCA, EntityPlayer> predicate) {
        this.id = id;
        this.predicate = predicate;
    }

    public String getId() {
        return id;
    }

    public boolean test(EntityVillagerMCA villager, EntityPlayer player) {
        return predicate.test(villager, player);
    }

    public static List<EnumConstraint> fromStringList(String constraints) {
        List<EnumConstraint> list = new ArrayList<>();

        if (constraints != null && !constraints.isEmpty()) {
            String[] splitConstraints = constraints.split("\\|");

            for (String s : splitConstraints) {
                EnumConstraint constraint = byValue(s);
                if (constraint != null) {
                    list.add(constraint);
                }
            }
        }

        return list;
    }

    public static EnumConstraint byValue(String value) {
        Optional<EnumConstraint> state = Arrays.stream(values()).filter((e) -> e.id.equalsIgnoreCase(value)).findFirst();
        return state.orElse(null);
    }

}
