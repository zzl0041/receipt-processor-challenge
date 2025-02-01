package receipts;

import receipts.model.Receipt;

import java.util.UUID;

public class ReceiptService {

    private final ReceiptRepository repository;
    private final ReceiptPointsCalculator calculator;

    public ReceiptService(ReceiptRepository repository, ReceiptPointsCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    public String processReceipt(Receipt receipt) {
        String id = UUID.randomUUID().toString();
        int points = calculator.calculatePoints(receipt);
        repository.save(id, points);
        return id;
    }

    public Integer getPoints(String id) {
        return repository.getPoints(id);
    }
}
