package com.khoubyari.example.service;

import com.khoubyari.example.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test") // Ensure the test profile is active
public class ServicePropertiesTest {

    @Autowired
    private ServiceProperties serviceProperties;

    @Test
    public void testPropertiesLoadedFromTestProfile() {
        // This value for 'name' comes from application.yml's "test" profile
        assertEquals("test profile:", serviceProperties.getName());

        // The 'healthy' property is not defined in the "test" profile in application.yml
        // So it should use the default value from the ServiceProperties class definition.
        assertTrue("Default healthy property should be true", serviceProperties.isHealthy());
    }
}
