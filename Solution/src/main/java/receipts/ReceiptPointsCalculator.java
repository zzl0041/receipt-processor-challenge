package receipts;

import receipts.model.Item;
import receipts.model.Receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReceiptPointsCalculator {

    public int calculatePoints(Receipt receipt) {
        int points = 0;

        // 1. One point for every alphanumeric character in the retailer name
        if (receipt.getRetailer() != null) {
            points += receipt.getRetailer()
                    .chars()
                    .filter(Character::isLetterOrDigit)
                    .count();
        }

        // 2. 50 points if the total is a round dollar amount (no cents)
        BigDecimal totalBD = parseBigDecimalSafe(receipt.getTotal());
        if (isRoundDollar(totalBD)) {
            points += 50;
        }

        // 3. 25 points if the total is multiple of 0.25
        if (isMultipleOfQuarter(totalBD)) {
            points += 25;
        }

        // 4. 5 points for every two items
        int itemCount = (receipt.getItems() == null) ? 0 : receipt.getItems().size();
        points += (itemCount / 2) * 5;

        // 5. If trimmed length of item desc is multiple of 3 => price * 0.2, round up
        if (receipt.getItems() != null) {
            for (Item item : receipt.getItems()) {
                String desc = item.getShortDescription() != null
                        ? item.getShortDescription().trim()
                        : "";
                if (!desc.isEmpty() && desc.length() % 3 == 0) {
                    BigDecimal priceBD = parseBigDecimalSafe(item.getPrice());
                    BigDecimal partialPoints = priceBD.multiply(new BigDecimal("0.2"));
                    partialPoints = partialPoints.setScale(0, RoundingMode.UP);
                    points += partialPoints.intValue();
                }
            }
        }

        // 6. 6 points if day in purchaseDate is odd
        LocalDate date = parseLocalDateSafe(receipt.getPurchaseDate());
        if (date != null && (date.getDayOfMonth() % 2 != 0)) {
            points += 6;
        }

        // 7. 10 points if time is after 14:00 and before 16:00
        LocalTime time = parseLocalTimeSafe(receipt.getPurchaseTime());
        if (time != null) {
            LocalTime twoPM = LocalTime.of(14, 0);
            LocalTime fourPM = LocalTime.of(16, 0);
            if (time.isAfter(twoPM) && time.isBefore(fourPM)) {
                points += 10;
            }
        }

        return points;
    }

    // Safely parse a BigDecimal from a string
    private BigDecimal parseBigDecimalSafe(String input) {
        try {
            return new BigDecimal(input);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // Check if value is integer (round dollar)
    private boolean isRoundDollar(BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0;
    }

    // Check if value is multiple of 0.25
    private boolean isMultipleOfQuarter(BigDecimal value) {
        BigDecimal quarter = new BigDecimal("0.25");
        BigDecimal remainder = value.remainder(quarter);
        return remainder.compareTo(BigDecimal.ZERO) == 0;
    }

    private LocalDate parseLocalDateSafe(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseLocalTimeSafe(String timeStr) {
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }
}
