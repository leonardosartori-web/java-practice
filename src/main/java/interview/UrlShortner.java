package interview;

import java.util.HashMap;
import java.util.Optional;

public class UrlShortner {

    private final HashMap<String, String> urlToCode = new HashMap<>();
    private final HashMap<String, String> codeToUrl = new HashMap<>();
    private long nextId = 0;

    public synchronized String shorten(String url) {
        validate(url);

        String existingCode = urlToCode.get(url);

        if (existingCode != null) return existingCode;

        String code = "u" + nextId;
        nextId++;

        codeToUrl.put(code, url);
        urlToCode.put(url, code);

        return code;
    }

    public Optional<String> resolve(String code) {
        validate(code);
        return Optional.ofNullable(codeToUrl.get(code));
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value must not be null or blank");
    }

}
