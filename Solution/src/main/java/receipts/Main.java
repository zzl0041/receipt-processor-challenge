package receipts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        // Create main domain objects
        ReceiptRepository repository = new ReceiptRepository();
        ReceiptPointsCalculator calculator = new ReceiptPointsCalculator();
        ReceiptService service = new ReceiptService(repository, calculator);
        HttpHandlers handlers = new HttpHandlers(service);

        // Create server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8088), 0);

        // POST /receipts/process
        server.createContext("/receipts/process", handlers.processReceiptHandler);
        // GET /receipts/{id}/points
        //   We'll mount this on the base "/receipts" path to capture the sub-path
        server.createContext("/receipts", handlers.getPointsHandler);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8088.");
    }
}
