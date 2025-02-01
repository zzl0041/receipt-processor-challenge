package receipts.model;

import java.util.List;

public class Receipt {

    // e.g., "M&M Corner Market"
    private String retailer;

    // e.g., "2022-01-01"
    private String purchaseDate;

    // e.g., "13:01" (24-hour format)
    private String purchaseTime;

    // Array of items; each has shortDescription + price
    private List<Item> items;

    // e.g., "6.49" (pattern: ^\\d+\\.\\d{2}$)
    private String total;

    public Receipt() {}

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
