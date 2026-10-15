import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Inventory {
    private static final int CAPACITY = 10;
    private final List<Item> items = new ArrayList<>();

    public boolean add(Item item) {
        if (item == null || items.size() >= CAPACITY) return false;
        return items.add(item);
    }

    public Optional<Item> get(int index) {
        if (index < 0 || index >= items.size()) return Optional.empty();
        return Optional.of(items.get(index));
    }

    public <T extends Item> Optional<T> findFirst(Class<T> type) {
        return items.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }
    public boolean isFull() { return items.size() >= CAPACITY; }

    public boolean remove(Item item) { return items.remove(item); }

    public int totalValue() {
        return items.stream().mapToInt(Item::getValue).sum();
    }

    public List<Item> list() { return Collections.unmodifiableList(items); }
}
