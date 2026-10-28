package service_registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServiceRegistry {

    private final Map<String, String> services = new HashMap<>();

    public void register(String id, String address) {
        validate(id, "Id");
        validate(address, "Address");

        if (services.containsKey(id)) throw new IllegalArgumentException("Id already registered");
        services.put(id, address);
    }

    public Optional<String> find(String id) {
        validate(id, "Id");
        String a = services.get(id);
        return Optional.ofNullable(a);
    }

    public boolean unregister(String id) {
        validate(id, "Id");
        String d = services.remove(id);
        return d != null;
    }

    public int size() {
        return services.size();
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
    }
}
