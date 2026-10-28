package interview;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RateLimiter {

    private record Window(long startedAt, int acceptedRequests){}

    private final ConcurrentMap<String, Window> requests = new ConcurrentHashMap<>();

    private final int maxRequests;
    private final long windowMillis;

    public RateLimiter(int maxRequests, long windowMillis) {
        if (maxRequests <= 0) throw new IllegalArgumentException("Max Requests must be positive");
        validateMillis(windowMillis);
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allowRequest(String clientId, long nowMillis) {
        validateId(clientId);
        if (nowMillis < 0) {
            throw new IllegalArgumentException("Current time must not be negative");
        }

        AtomicBoolean allowed = new AtomicBoolean(false);

        requests.compute(clientId, (id, window) -> {
            if (window == null || nowMillis - window.startedAt() >= windowMillis) {
                allowed.set(true);
                return new Window(nowMillis, 1);
            }
            if (window.acceptedRequests() >= maxRequests) return window;
            allowed.set(true);
            return new Window(window.startedAt(), window.acceptedRequests() + 1);
        });
        return allowed.get();
    }

    private static void validateId(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Invalid Client Id");
    }

    private static void validateMillis(long millis) {
        if (millis <= 0) throw new IllegalArgumentException("Window Millis must be positive");
    }
}
