package receipts;

import receipts.model.Receipt;
import receipts.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHandlers {

    private final ReceiptService receiptService;

    private final Pattern pointsPattern = Pattern.compile("^/receipts/([^/]+)/points$");

    public HttpHandlers(ReceiptService service) {
        this.receiptService = service;
    }

    public HttpHandler processReceiptHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = readRequestBody(exchange);

            try {
                Receipt receipt = JsonUtil.fromJson(body, Receipt.class);

                String id = receiptService.processReceipt(receipt);

                String responseJson = "{\"id\":\"" + id + "\"}";
                sendJsonResponse(exchange, 200, responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "Bad Request");
            }
        }
    };

    public HttpHandler getPointsHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            URI uri = exchange.getRequestURI();
            Matcher matcher = pointsPattern.matcher(uri.getPath());
            if (!matcher.matches()) {
                sendResponse(exchange, 404, "Not Found");
                return;
            }

            String id = matcher.group(1);
            Integer points = receiptService.getPoints(id);
            if (points == null) {
                sendResponse(exchange, 404, "Not Found");
            } else {
                String responseJson = "{\"points\":" + points + "}";
                sendJsonResponse(exchange, 200, responseJson);
            }
        }
    };

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

