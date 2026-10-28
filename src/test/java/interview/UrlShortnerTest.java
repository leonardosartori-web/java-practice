package interview;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class UrlShortnerTest {
    @Test
    void rejectsInvalidStrings() {
        UrlShortner urlShortner = new UrlShortner();
        assertThrows(IllegalArgumentException.class,
                () -> urlShortner.shorten(null));
        assertThrows(IllegalArgumentException.class,
                () -> urlShortner.shorten(""));
        assertThrows(IllegalArgumentException.class,
                () -> urlShortner.shorten("   "));

        assertThrows(IllegalArgumentException.class,
                () -> urlShortner.resolve(null));
        assertThrows(IllegalArgumentException.class,
                () -> urlShortner.resolve(""));
        assertThrows(IllegalArgumentException.class,
                () -> urlShortner.resolve("   "));
    }

    @Test
    void addUrl() {
        UrlShortner urlShortner = new UrlShortner();
        String code = urlShortner.shorten("Ciao");

        assertEquals(urlShortner.resolve(code).get(), "Ciao");
    }

    @Test
    void resolvesRegisteredUrl() {
        UrlShortner urlShortner = new UrlShortner();

        String code = urlShortner.shorten("https://example.com");

        assertEquals(
                Optional.of("https://example.com"),
                urlShortner.resolve(code)
        );
    }

    @Test
    void concurrentShortenThreads() throws Exception {
        UrlShortner urlShortner = new UrlShortner();

        int threads = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        Callable<String> task = () -> {
            ready.countDown();
            start.await();
            return urlShortner.shorten("Hello");
        };

        try {
            List<Future<String>> tasks = IntStream.range(0, threads).mapToObj(i -> executorService.submit(task)).toList();
            ready.await();
            start.countDown();
            Set<String> results = new HashSet<>();
            for (Future<String> t : tasks) {
                String code = t.get();
                results.add(code);
                assertEquals(Optional.of("Hello"), urlShortner.resolve(code));
            }
            assertEquals(1, results.size());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void concurrentInsert() throws Exception {
        UrlShortner urlShortner = new UrlShortner();

        List<String> urls = Stream.of("A", "B").toList();

        ExecutorService executorService = Executors.newFixedThreadPool(urls.size());

        CountDownLatch ready = new CountDownLatch(urls.size());
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<String>> tasks = urls.stream().map(
                    i -> executorService.submit(() -> {
                        ready.countDown();
                        start.await();
                        return urlShortner.shorten(i);
                    })).toList();

            ready.await();
            start.countDown();

            Set<String> results = new HashSet<>();

            for (int i = 0; i < 2; i++) {
                Future<String> t = tasks.get(i);
                String code = t.get();

                assertFalse(results.contains(code));

                results.add(code);

                assertEquals(Optional.of(urls.get(i)), urlShortner.resolve(code));
            }

            assertEquals(2, results.size());

        } finally {
            executorService.shutdownNow();
        }
    }
}
