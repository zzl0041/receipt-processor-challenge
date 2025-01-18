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

        if (receipt.getRetailer() != null) {
            points += receipt.getRetailer()
                    .chars()
                    .filter(Character::isLetterOrDigit)
                    .count();
        }

        BigDecimal totalBD = parseBigDecimalSafe(receipt.getTotal());
        if (isRoundDollar(totalBD)) {
            points += 50;
        }

        if (isMultipleOfQuarter(totalBD)) {
            points += 25;
        }

        int itemCount = (receipt.getItems() == null) ? 0 : receipt.getItems().size();
        points += (itemCount / 2) * 5;

        if (receipt.getItems() != null) {
            for (Item item : receipt.getItems()) {
                String desc = (item.getShortDescription() == null)
                        ? ""
                        : item.getShortDescription().trim();
                if (!desc.isEmpty() && desc.length() % 3 == 0) {
                    BigDecimal priceBD = parseBigDecimalSafe(item.getPrice());
                    BigDecimal partialPoints = priceBD.multiply(new BigDecimal("0.2"));
                    partialPoints = partialPoints.setScale(0, RoundingMode.UP);
                    points += partialPoints.intValue();
                }
            }
        }

        LocalDate date = parseDateSafe(receipt.getPurchaseDate());
        if (date != null && (date.getDayOfMonth() % 2 != 0)) {
            points += 6;
        }

        LocalTime time = parseTimeSafe(receipt.getPurchaseTime());
        if (time != null) {
            LocalTime twoPM = LocalTime.of(14, 0);
            LocalTime fourPM = LocalTime.of(16, 0);
            if (time.isAfter(twoPM) && time.isBefore(fourPM)) {
                points += 10;
            }
        }

        return points;
    }

    private BigDecimal parseBigDecimalSafe(String input) {
        try {
            return new BigDecimal(input);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDateSafe(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTimeSafe(String timeStr) {
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRoundDollar(BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0;
    }

    private boolean isMultipleOfQuarter(BigDecimal value) {
        BigDecimal quarter = new BigDecimal("0.25");
        BigDecimal remainder = value.remainder(quarter);
        return remainder.compareTo(BigDecimal.ZERO) == 0;
    }
}

