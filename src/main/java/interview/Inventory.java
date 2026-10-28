package interview;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Inventory {
    private record Stock(int quantity) {}

    private final ConcurrentMap<String, Stock> stocks = new ConcurrentHashMap<>();

    public void addStock(String productId, int quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        stocks.compute(productId, (id, existingStock) -> {
            int currentStock = existingStock == null ? 0 : existingStock.quantity;
            return new Stock(Math.addExact(currentStock, quantity));
        });
    }

    public int availableStock(String productId) {
        validateProductId(productId);
        return stocks.getOrDefault(productId, new Stock(0)).quantity();
    }

    public boolean reserve(String productId, int quantity) {
        validateProductId(productId);
        validateQuantity(quantity);

        AtomicBoolean reserved = new AtomicBoolean(false);

        stocks.compute(productId, (id, existingStock) -> {
            if (existingStock == null || existingStock.quantity() < quantity) return existingStock;
            Stock updatedStock = new Stock(Math.subtractExact(existingStock.quantity(), quantity));
            reserved.set(true);
            return updatedStock;
        });
        return reserved.get();
    }

    private static void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Invalid Product Id");
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid Quantity");
        }
    }
}
