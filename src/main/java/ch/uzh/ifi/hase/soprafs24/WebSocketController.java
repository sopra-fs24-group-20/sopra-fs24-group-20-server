package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/start-game")
    @SendTo("/topic/game-control")
    public String startGame(){
        // You could implement additional logic here
        return "{\"command\":\"start\"}";
    }

    @MessageMapping("/stop-game")
    @SendTo("/topic/game-control")
    public String stopGame() {
        return "{\"command\":\"stop\"}";
    }


}
