package com.sae.Chess.controller;

import com.sae.Chess.service.GameService;
import com.sae.Chess.model.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController() {
        gameService = new GameService();
        gameService.Init();
    }

    @GetMapping("/moves")
    public ArrayList<Move> GetCurrentPlayerMoves() {
        return gameService.GetCurrentPlayerMoves();
    }

    @PostMapping("/move")
    public ResponseEntity<String> MakeMove(@RequestBody Move move) {
        String fenString = gameService.MakeMove(move);
        return ResponseEntity.ok(fenString);
    }

    
}
