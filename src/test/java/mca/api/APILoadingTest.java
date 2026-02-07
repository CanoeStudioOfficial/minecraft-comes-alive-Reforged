package mca.api;

import mca.core.MCA;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class APILoadingTest {

    @Test
    public void testResourceLoading() {
        // Test if the lang file can be loaded using MCA.class.getResourceAsStream
        String langFilePath = "/assets/mca/lang/en_us.lang";
        InputStream stream = MCA.class.getResourceAsStream(langFilePath);
        
        assertNotNull(stream, "Resource stream for en_us.lang should not be null when using MCA.class.getResourceAsStream");
    }

    @Test
    public void testAPIInitDoesNotCrash() {
        // API.init() internally calls getResourceAsStream. 
        // This test ensures that the logic we fixed (using MCA.class) works in the current environment.
        try {
            API.init();
        } catch (Exception e) {
            throw new RuntimeException("API.init() failed even after fix", e);
        }
    }
}
