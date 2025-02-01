package receipts;

import receipts.model.Item;
import receipts.model.Receipt;
import receipts.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHandlers {

    private final ReceiptService receiptService;

    // Regex to match GET /receipts/{id}/points
    private final Pattern getPointsPattern = Pattern.compile("^/receipts/([^/]+)/points$");

    // For validating "total" format => ^\\d+\\.\\d{2}$
    private final Pattern totalPattern = Pattern.compile("^\\d+\\.\\d{2}$");
    // For validating "shortDescription" => ^[\\w\\s\\-]+$
    private final Pattern shortDescPattern = Pattern.compile("^[\\w\\s\\-]+$");
    // For validating "retailer" => ^[\\w\\s\\-&]+$
    private final Pattern retailerPattern = Pattern.compile("^[\\w\\s\\-&]+$");

    public HttpHandlers(ReceiptService service) {
        this.receiptService = service;
    }

    /**
     * POST /receipts/process
     */
    public HttpHandler processReceiptHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            System.out.println("post received");
            String body = readRequestBody(exchange);
            try {
                // Deserialize JSON into Receipt
                Receipt receipt = JsonUtil.fromJson(body, Receipt.class);

                // Validate required fields from the OpenAPI spec
                if (!isValidReceipt(receipt)) {
                    sendResponse(exchange, 400, "Bad Request (invalid receipt data)");
                    return;
                }

                // Process receipt: calculate points, store in memory
                String id = receiptService.processReceipt(receipt);

                // Return JSON: { "id": "uuid" }
                String responseJson = "{\"id\":\"" + id + "\"}";
                sendJsonResponse(exchange, 200, responseJson);

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "Bad Request");
            }
        }
    };

    /**
     * GET /receipts/{id}/points
     */
    public HttpHandler getPointsHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            URI uri = exchange.getRequestURI();
            Matcher matcher = getPointsPattern.matcher(uri.getPath());
            if (!matcher.matches()) {
                sendResponse(exchange, 404, "Not Found");
                return;
            }

            String id = matcher.group(1);
            Integer points = receiptService.getPoints(id);

            if (points == null) {
                // 404 if no receipt found
                sendResponse(exchange, 404, "Not Found");
            } else {
                // Return JSON: { "points": ... }
                String responseJson = "{\"points\":" + points + "}";
                sendJsonResponse(exchange, 200, responseJson);
            }
        }
    };

    /**
     * Basic server / validation helpers
     */
    private boolean isValidReceipt(Receipt r) {
        // Required fields in the schema: retailer, purchaseDate, purchaseTime, items, total
        if (r.getRetailer() == null || r.getPurchaseDate() == null
                || r.getPurchaseTime() == null || r.getItems() == null || r.getItems().isEmpty()
                || r.getTotal() == null) {
            return false;
        }
        // Basic pattern checks
        if (!retailerPattern.matcher(r.getRetailer()).matches()) {
            return false;
        }
        if (!totalPattern.matcher(r.getTotal()).matches()) {
            return false;
        }
        // For each item, check shortDescription + price
        for (Item i : r.getItems()) {
            if (i.getShortDescription() == null || i.getPrice() == null) {
                return false;
            }
            if (!shortDescPattern.matcher(i.getShortDescription()).matches()) {
                return false;
            }
            if (!totalPattern.matcher(i.getPrice()).matches()) {
                return false;
            }
        }
        // Additional checks for date/time format could be done with Java's LocalDate.parse() / LocalTime.parse(),
        // but if parse fails in pointsCalculator, it just doesn't add points.
        // We'll consider them "valid enough" for this example.

        return true;
    }

    // Read request body as a UTF-8 string
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    // Send a plain text response
    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Send a JSON response
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
