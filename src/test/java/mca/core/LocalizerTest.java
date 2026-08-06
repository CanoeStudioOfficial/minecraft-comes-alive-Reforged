package mca.core;

import mca.core.forge.ServerProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

public class LocalizerTest {
    @BeforeEach
    public void setup() throws Exception {
        MCA.proxy = new ServerProxy();
        
        // Initialize MCA through reflection because its instance field has no public setter.
        MCA mcaInstance = new MCA();
        Field instanceField = MCA.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, mcaInstance);
    }

    @Test
    public void testLocalizerInitialization() {
        Localizer localizer = new Localizer();
        String localized = localizer.localize("itemGroup.MCA");
        Assertions.assertEquals("Minecraft Comes Alive", localized);
    }

    @Test
    public void testLocalizerVariables() {
        Localizer localizer = new Localizer();
        String localized = localizer.localize("mining.search.nearby", "Gold");
        Assertions.assertEquals("There's some Gold nearby...", localized);
    }
}
