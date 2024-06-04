package com.sae.Chess.controller;

import com.sae.Chess.model.Move;
import com.sae.Chess.service.SP_GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/game/sp")
public class GameController {
    private final SP_GameService gameService;

    @Autowired
    public GameController(SP_GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/moves")
    public ArrayList<Move> GetCurrentPlayerMoves() {
        System.out.println("Got request: getMoves()");
        return gameService.GetCurrentPlayerMoves();
    }

    @PostMapping("/move")
    public ResponseEntity<String> MakeMove(@RequestBody Move move) {
        try {
            String fenString = gameService.MakeMove(move);
            return ResponseEntity.ok(fenString);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error making move: " + e.getMessage());
        }
    }

    @PostMapping("/paul")
    public void Paul() {
        gameService.Paul();
    }
}
