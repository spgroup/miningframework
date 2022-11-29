import jdk.incubator.http.HttpClient;
import jdk.incubator.http.WebSocket;
import java.io.IOException;
import java.net.URI;

public class ConnectionHandover {

    static {
        LoggingHelper.setupLogging();
    }

    public static void main(String[] args) throws IOException {
        try (DummyWebSocketServer server = new DummyWebSocketServer()) {
            server.open();
            URI uri = server.getURI();
            WebSocket.Builder webSocketBuilder = HttpClient.newHttpClient().newWebSocketBuilder(uri, new WebSocket.Listener() {
            });
            WebSocket ws1 = webSocketBuilder.buildAsync().join();
            try {
                ws1.abort();
            } catch (IOException ignored) {
            }
            WebSocket ws2 = webSocketBuilder.buildAsync().join();
            try {
                ws2.abort();
            } catch (IOException ignored) {
            }
        }
    }
}