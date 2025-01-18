package receipts;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        ReceiptRepository repository = new ReceiptRepository();
        ReceiptPointsCalculator calculator = new ReceiptPointsCalculator();
        ReceiptService service = new ReceiptService(repository, calculator);
        HttpHandlers handlers = new HttpHandlers(service);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/receipts/process", handlers.processReceiptHandler);
        server.createContext("/receipts", handlers.getPointsHandler);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }
}

