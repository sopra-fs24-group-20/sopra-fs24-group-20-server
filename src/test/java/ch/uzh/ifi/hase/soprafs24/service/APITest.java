package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class APITest {

    private RoundService roundService;
    private HttpServer httpServer;

    @BeforeEach
    void setup() throws IOException {
        roundService = new RoundService();
        httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        httpServer.createContext("/w/api.php", exchange -> {
            String response = "{\"query\":{\"pages\":{\"-1\":{\"ns\":0,\"title\":\"existingWord\",\"missing\":\"\"}}}}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        httpServer.start();

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8000");
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(0);
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
    }

    @Test
    void checkWordExists_ExistingWord_ShouldReturnTrue() {
        assertTrue(roundService.checkWordExists("Tree"), "The word should exist.");
    }
    @Test
    void checkWordExists_Not_ExistingWord_ShouldReturnFalse() {
        assertFalse(roundService.checkWordExists("Treeeeeeeeee"), "The word should exist.");
    }
}
