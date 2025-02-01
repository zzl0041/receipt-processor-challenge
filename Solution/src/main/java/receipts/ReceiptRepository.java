package receipts;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReceiptRepository {

    private final ConcurrentMap<String, Integer> storage = new ConcurrentHashMap<>();

    public void save(String id, int points) {
        storage.put(id, points);
    }

    public Integer getPoints(String id) {
        return storage.get(id);
    }
}
