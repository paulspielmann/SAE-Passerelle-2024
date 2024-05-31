package com.sae.Chess.controller;

import com.sae.Chess.service.GameService;
import com.sae.Chess.model.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController() {
        gameService = new GameService();
        gameService.Init();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/moves")
    public ArrayList<Move> GetCurrentPlayerMoves() {
        return gameService.GetCurrentPlayerMoves();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/move")
    public void MakeMove(Move move) {
        gameService.MakeMove(move);
    }

    
}
