package interview;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Cart {

    private record Item(BigDecimal unitPrice, int quantity) {}

    private final Map<String, Item> items = new HashMap<>();

    public synchronized void addItem(String productId, BigDecimal unitPrice, int quantity) {

        validateProductId(productId);
        validateUnitPrice(unitPrice);
        validateQuantity(quantity);

        Item existingItem = items.get(productId);
        if (existingItem == null) {
            items.put(productId, new Item(unitPrice, quantity));
            return;
        }
        else {
            if (existingItem.unitPrice.compareTo(unitPrice) != 0) throw new IllegalArgumentException("Unit price must be the same for the same product");
            items.put(productId, new Item(unitPrice, Math.addExact(existingItem.quantity(), quantity)));
        }
    }

    public synchronized BigDecimal total() {
        BigDecimal tot = BigDecimal.ZERO;
        for (Item i : items.values()) {
            tot = tot.add(i.unitPrice.multiply(BigDecimal.valueOf(i.quantity)));
        }
        return tot;
    }

    private static void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product Id must not be null or blank");
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Invalid quantity");
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() <= 0) throw new IllegalArgumentException("Invalid Unit Price");
    }

}
