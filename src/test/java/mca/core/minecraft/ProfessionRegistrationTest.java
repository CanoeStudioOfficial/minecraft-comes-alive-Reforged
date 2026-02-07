package mca.core.minecraft;

import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests to verify the registration of MCA villager professions.
 */
public class ProfessionRegistrationTest {

    @BeforeAll
    public static void setup() {
        // Initialize careers as they are registered during the Forge registration event in the real mod
        ProfessionsMCA.registerCareers();
    }

    @Test
    public void testRedEngineerRegistration() {
        VillagerProfession profession = ProfessionsMCA.red_engineer;
        assertNotNull(profession, "Red Engineer profession should not be null");
        assertNotNull(profession.getRegistryName(), "Red Engineer registry name should not be null");
        assertEquals("mca:red_engineer", profession.getRegistryName().toString());
        
        VillagerCareer career = ProfessionsMCA.red_engineer_career;
        assertNotNull(career, "Red Engineer career should not be null");
        assertEquals("red_engineer", career.getName());
    }

    @Test
    public void testSinisterMerchantRegistration() {
        VillagerProfession profession = ProfessionsMCA.sinister_merchant;
        assertNotNull(profession, "Sinister Merchant profession should not be null");
        assertNotNull(profession.getRegistryName(), "Sinister Merchant registry name should not be null");
        assertEquals("mca:sinister_merchant", profession.getRegistryName().toString());

        VillagerCareer career = ProfessionsMCA.sinister_merchant_career;
        assertNotNull(career, "Sinister Merchant career should not be null");
        assertEquals("sinister_merchant", career.getName());
    }

    @Test
    public void testAlchemistRegistration() {
        VillagerProfession profession = ProfessionsMCA.alchemist;
        assertNotNull(profession, "Alchemist profession should not be null");
        assertNotNull(profession.getRegistryName(), "Alchemist registry name should not be null");
        assertEquals("mca:alchemist", profession.getRegistryName().toString());

        VillagerCareer career = ProfessionsMCA.alchemist_career;
        assertNotNull(career, "Alchemist career should not be null");
        assertEquals("alchemist", career.getName());
    }
}
