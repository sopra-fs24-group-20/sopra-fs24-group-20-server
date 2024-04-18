package ch.uzh.ifi.hase.soprafs24.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Service
public class WebSocketService {

    private static ApplicationContext applicationContext = null;

    @Autowired
    public WebSocketService(ApplicationContext applicationContext) {
        WebSocketService.applicationContext = applicationContext;
    }

    public static void startWebSocket(Long lobbyId) {
        // Make sure that the ServerEndpointExporter is initialized
        ServerEndpointExporter serverEndpointExporter = applicationContext.getBean(ServerEndpointExporter.class);
        serverEndpointExporter.afterPropertiesSet();
    }
}
