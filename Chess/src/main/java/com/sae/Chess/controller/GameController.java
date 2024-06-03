package com.sae.Chess.controller;

import com.sae.Chess.service.SP_GameService;
import com.sae.Chess.model.Move;
import com.sae.Chess.model.Perft;

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
@RequestMapping("/api/game/sp")
public class GameController {
    private final SP_GameService gameService;

    @Autowired
    public GameController() {
        gameService = new SP_GameService();
    }

    @GetMapping("/moves")
    public ArrayList<Move> GetCurrentPlayerMoves() {
        System.out.println("Got request: getMoves()");
        return gameService.GetCurrentPlayerMoves();
    }

    @PostMapping("/move")
    public ResponseEntity<String> MakeMove(@RequestBody Move move) {
        String fenString = gameService.MakeMove(move);
        return ResponseEntity.ok(fenString);
    }

    @PostMapping("/paul")
    public void Paul() {
        gameService.Paul();
    }
}
