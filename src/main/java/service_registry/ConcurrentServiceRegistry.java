package service_registry;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentServiceRegistry {

    private final ConcurrentMap<String, String> services = new ConcurrentHashMap<>();
    private final int capacity = 1;

    public void register(String id, String address) {
        validate(id, "Id");
        validate(address, "Address");

        synchronized (services) {
            if (size() >= capacity) {
                throw new IllegalStateException("Registry is full");
            }

            String existingId = services.putIfAbsent(id, address);
            if (existingId != null) throw new IllegalArgumentException("Service ID already registered");
        }
    }

    public Optional<String> find(String id) {
        validate(id, "Id");
        return Optional.ofNullable(services.get(id));
    }

    public boolean unregister(String id) {
        validate(id, "Id");
        String removedAddress = services.remove(id);
        return removedAddress != null;
    }

    public int size() { return services.size(); }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
    }
}
