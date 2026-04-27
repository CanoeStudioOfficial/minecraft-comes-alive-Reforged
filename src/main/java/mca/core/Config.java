package mca.core;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Config implements Serializable {
    private transient final Configuration config;

    public String villagerSpawnMode;
    public double mcaVillagerSpawnRatio;
    public boolean enableDiminishingReturns;
    public boolean enableInfection;
    public int infectionChance;
    public int infectionTime;
    public float infectionChanceDecreasePerLevel;
    public float infirmaryInfectionChanceReduction;
    public boolean allowGrimReaper;
    public int guardSpawnRate;
    public boolean guardPatrolEnabled;
    public double guardPatrolSpeed;
    public int guardPatrolRadius;
    public int guardPatrolWaitTime;
    public int chanceToHaveTwins;
    public int marriageHeartsRequirement;
    public int engagementHeartsRequirement;
    public int bouquetHeartsRequirement;
    public int babyGrowUpTime;
    public int childGrowUpTime;
    public int villagerSpawnerCap;
    public int villagerSpawnerRateMinutes;
    public int villagePopulationCap;
    public boolean enablePopulationCap;
    public int villagerMaxHealth;
    public boolean allowTrading;
    public boolean logVillagerDeaths;
    public boolean enableRevivals;
    public String villagerChatPrefix;
    public boolean allowPlayerMarriage;
    public boolean enableAdminCommands;
    public boolean allowRoseGoldGeneration;
    public boolean allowSameGenderMarriage;
    public float geneticCompatibilityThreshold;

    public float traitInheritChance;
    public float geneticImmigrantChance;
    public float traitChance;

    public float interactionChanceFatigue;
    public int interactionFatigueCooldown;
    public int giftDesaturationQueueLength;
    public float giftDesaturationFactor;
    public double giftDesaturationExponent;
    public double giftSatisfactionFactor;

    public Config(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        addConfigValues();
    }

    private void addConfigValues() {
        villagerSpawnMode = config.get("General", "Villager Spawn Mode", "replace", "Modes: 'replace' - all vanilla villagers become MCA villagers; 'coexist' - both types spawn together; 'vanilla' - keep original villagers only.").getString();
        mcaVillagerSpawnRatio = config.get("General", "MCA Villager Spawn Ratio", 0.5, "In 'coexist' mode, chance that a spawning villager is MCA type (0.0-1.0). Default 0.5 (50%).").getDouble();
        enableDiminishingReturns = config.get("General", "Enable Interaction Fatigue?", true, "Should interactions yield diminishing returns over time?").getBoolean();
        enableInfection = config.get("General", "Enable Zombie Infection?", true, "Should zombies be able to infect villagers?").getBoolean();
        infectionChance = config.get("General", "Chance of Infection", 5, "Chance that a villager will be infected on hit from a zombie. Default is 5 for 5%.").getInt();
        infectionTime = config.get("General", "Infection Time", 72000, "Time in ticks until a villager turns into a zombie after infection. 20 ticks = 1 second; 72000 ticks = 1 hour.").getInt();
        infectionChanceDecreasePerLevel = (float) config.get("General", "Infection Chance Decrease Per Level", 0.25, "Reduction in infection chance per villager trading level.").getDouble();
        infirmaryInfectionChanceReduction = (float) config.get("General", "Infirmary Infection Chance Reduction", 0.5, "Reduction in infection chance if an infirmary is nearby (50% reduction by default).").getDouble();
        allowGrimReaper = config.get("General", "Allow Grim Reaper?", true, "Should the Grim Reaper boss be enabled?").getBoolean();
        guardSpawnRate = config.get("General", "Guard Spawn Rate", 6, "How many villagers that should be in a village before a guard spawns.").getInt();
        guardPatrolEnabled = config.get("General", "Guard Patrol Enabled", true, "Enable guards to patrol around the village.").getBoolean();
        guardPatrolSpeed = config.get("General", "Guard Patrol Speed", 0.5, "Movement speed of guards while patrolling.").getDouble();
        guardPatrolRadius = config.get("General", "Guard Patrol Radius", 64, "Radius within which guards will search for their village.").getInt();
        guardPatrolWaitTime = config.get("General", "Guard Patrol Wait Time", 120, "Time in ticks a guard will wait at each patrol point before moving to the next.").getInt();
        chanceToHaveTwins = config.get("General", "Chance to Have Twins", 2, "Chance that you will have twins. Default is 2 for 2%.").getInt();
        marriageHeartsRequirement = config.get("General", "Marriage Hearts Requirement", 100, "Number of hearts required to get married.").getInt();
        engagementHeartsRequirement = config.get("General", "Engagement Hearts Requirement", 50, "Number of hearts required to get engaged.").getInt();
        bouquetHeartsRequirement = config.get("General", "Bouquet Hearts Requirement", 25, "Number of hearts required to give a bouquet (promise).").getInt();
        babyGrowUpTime = config.get("General", "Baby Grow Up Time (Minutes)", 30, "Minutes it takes for a baby to be ready to grow up.").getInt();
        childGrowUpTime = config.get("General", "Child Grow Up Time (Minutes)", 60, "Minutes it takes for a child to grow into an adult.").getInt();
        villagerSpawnerCap = config.get("General", "Villager Spawner Cap", 5, "Maximum number of villagers that a spawner will create in the area before it stops.").getInt();
        villagerSpawnerRateMinutes = config.get("General", "Villager Spawner Rate", 30, "The spawner will spawn 1 villager per this many minutes.").getInt();
        enablePopulationCap = config.get("General", "Enable Village Population Cap", true, "If enabled, limits the maximum number of villagers in a village.").getBoolean();
        villagePopulationCap = config.get("General", "Village Population Cap", 50, "Maximum number of villagers allowed in a single village. Includes guards and children.").getInt();
        allowTrading = config.get("General", "Enable Trading?", true, "Is trading with villagers enabled?").getBoolean();
        logVillagerDeaths = config.get("General", "Log Villager Deaths?", true, "Should villager deaths be logged?").getBoolean();
        enableRevivals = config.get("General", "Enable Revivals?", true, "Should reviving dead villagers be enabled?").getBoolean();
        villagerChatPrefix = config.get("General", "Villager Chat Prefix", "", "Formatting prefix used for all chat with villagers.").getString();
        allowPlayerMarriage = config.get("General", "Allow Player Marriage?", true, "Enables or disables player marriage.").getBoolean();
        enableAdminCommands = config.get("General", "Enable Admin Commands?", true, "Enables or disables MCA admin commands for ops.").getBoolean();
        allowRoseGoldGeneration = config.get("General", "Allow Rose Gold World Generation", true, "If enabled, generates rose gold in your world. If disabled, generates stone instead.").getBoolean();
        villagerMaxHealth = config.get("General", "Villager Max Health", 20, "Each villager's maximum health. 1 point equals 1 heart.").getInt();
        allowSameGenderMarriage = config.get("General", "Allow Same Gender Marriage", true, "Allow villagers to marry players of the same gender.").getBoolean();
        geneticCompatibilityThreshold = (float) config.get("General", "Genetic Compatibility Threshold", 0.3, "Minimum genetic similarity required for relationship compatibility. (0.0-1.0)").getDouble();

        traitInheritChance = (float) config.get("Genetics", "Trait Inherit Chance", 0.5, "Chance for a trait to be inherited. (0.0-1.0)").getDouble();
        geneticImmigrantChance = (float) config.get("Genetics", "Genetic Immigrant Chance", 0.1, "Chance for a villager to be an 'immigrant' with random genes. (0.0-1.0)").getDouble();
        traitChance = (float) config.get("Genetics", "Trait Chance", 0.1, "Base chance for a villager to have a trait. (0.0-1.0)").getDouble();

        // 礼物系统配置
        giftDesaturationQueueLength = config.get("Gifts", "Gift Desaturation Queue Length", 10, "Number of recent gifts remembered for desaturation calculation.").getInt();
        giftDesaturationFactor = (float) config.get("Gifts", "Gift Desaturation Factor", 0.5, "Factor applied to gift desaturation penalty. (0.0-1.0)").getDouble();
        giftDesaturationExponent = config.get("Gifts", "Gift Desaturation Exponent", 0.5, "Exponent applied to gift value for desaturation calculation.").getDouble();
        giftSatisfactionFactor = config.get("Gifts", "Gift Satisfaction Factor", 1.0, "Multiplier for final gift value. 1.0 = normal, 2.0 = double, 0.5 = half.").getDouble();

        config.save();
    }

    public Configuration getInstance() {
        return config;
    }

    public List<IConfigElement> getCategories() {
        List<IConfigElement> elements = new ArrayList<>();

        for (String s : config.getCategoryNames()) {
            if (s.equals("server")) continue;

            IConfigElement element = new ConfigElement(config.getCategory(s));
            elements.addAll(element.getChildElements());
        }

        return elements;
    }
}