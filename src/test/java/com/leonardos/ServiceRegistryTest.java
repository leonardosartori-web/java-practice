package com.leonardos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import service_registry.ServiceRegistry;

import java.util.Optional;

public class ServiceRegistryTest {
    @Test
    void rejectsDuplicateWithoutChangingOriginalAddress() {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.register("payments", "https://original.example");

        assertThrows(
                IllegalArgumentException.class,
                () -> serviceRegistry.register("payments", "https://replacement.example")
        );

        assertEquals(
                Optional.of("https://original.example"),
                serviceRegistry.find("payments")
        );
        assertEquals(1, serviceRegistry.size());
    }
}
