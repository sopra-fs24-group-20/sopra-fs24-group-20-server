package ch.uzh.ifi.hase.soprafs24.controller;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs24.service.RoundService;

@RestController
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/round/letters")
    public char getLetter() {
        return roundService.Generate_letter();
    }
}
