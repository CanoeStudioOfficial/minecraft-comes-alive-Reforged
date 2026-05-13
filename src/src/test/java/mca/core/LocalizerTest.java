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
        
        // 使用反射初始化 MCA 实例，因为它是私有的且没有公开的 setter
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
