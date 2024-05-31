package com.sae.Chess.service;

import com.sae.Chess.model.PlayerTimer;
import com.sae.Chess.model.Board;
import com.sae.Chess.model.Move;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GameService {
    private boolean whiteToMove;

    private PlayerTimer whiteTimer;
    private PlayerTimer blackTimer;

    private Board board;
    private ArrayList<Move> currentPlayerMoves;
    
    public GameService() {
        this(5, 3);
    }
    
    public GameService(int t, int i) {
        this.board = new Board();
        this.whiteTimer = new PlayerTimer(t, i, this::onWhiteTimeout);
        this.whiteTimer = new PlayerTimer(t, i, this::onBlackTimeout);
        
        this.whiteToMove = true;
        this.currentPlayerMoves = new ArrayList<>();
    }

    public void Init() { Init(true); }

    public void Init(boolean official) {
        board.Init();

        if (official) {
            board.LoadStartPos();
        }
        currentPlayerMoves = board.moves;
    }

    public void StartGame() {
        whiteTimer.start();
    }

    public void MakeMove(Move move) {
        board.MakeMove(move);
        SwitchTurn();
        currentPlayerMoves = board.moves;
    }

    public void SwitchTurn() {
        if (whiteToMove) {
            whiteTimer.stop();
            blackTimer.start();
        }
        else {
            blackTimer.stop();
            whiteTimer.start();
        }
        whiteToMove = !whiteToMove;
    }

    private void onWhiteTimeout() {
        System.out.println("");
    }


    private void onBlackTimeout() {
        System.out.println("");
    }

    public ArrayList<Move> GetCurrentPlayerMoves() {
        return currentPlayerMoves;
    }
}
