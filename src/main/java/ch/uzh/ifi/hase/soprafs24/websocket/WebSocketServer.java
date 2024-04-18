package ch.uzh.ifi.hase.soprafs24.websocket;

import java.io.IOException;
import java.util.*;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket/{lobbyId}")
public class WebSocketServer {

    // Set to store all active sessions
    private static final Map<Session,Boolean> sessions = Collections.synchronizedMap(new HashMap<>());



    @OnOpen
    public void onOpen(Session session) {
        // Add session to the set of active sessions
        sessions.put(session, Boolean.FALSE);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (Objects.equals(message, "READY")) {
            Boolean ready = sessions.get(session);
            if (ready != null) {
                sessions.put(session, !ready); // Toggle the boolean value
            }
            //Ready Check to see if anyone is still not Ready if not then broadcast message that everyone is ready
            boolean anyoneNotReady = sessions.containsValue(Boolean.FALSE);
            if (!anyoneNotReady) {
                broadcast("START");
                sessions.replaceAll((s, v) -> Boolean.FALSE);
            }
        }
        if (Objects.equals(message, "STOP")) {
            broadcast("STOP");
        }

    }


    @OnClose
    public void onClose(Session session) {
        // Remove session from the set of active sessions
        sessions.remove(session);
    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }

    // Broadcast message to all connected clients
    private void broadcast(String message) throws IOException {
        for (Session s : sessions.keySet()) {
            s.getBasicRemote().sendText(message);
        }
    }


}
