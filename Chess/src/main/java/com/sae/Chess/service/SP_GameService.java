package com.sae.Chess.service;

import com.sae.Chess.model.PlayerTimer;
import com.sae.Chess.model.Board;
import com.sae.Chess.model.Move;
import com.sae.Chess.model.Perft;
import com.sae.Chess.model.Engine.Engine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SP_GameService {
    public boolean firstMove;
    private boolean whiteToMove;

    private PlayerTimer whiteTimer;
    private PlayerTimer blackTimer;

    private Board board;
    private ArrayList<Move> currentPlayerMoves;

    private Engine engine;

    public SP_GameService() {
        this(5, 3);
    }
    
    public SP_GameService(int t, int i) {
        this.board = new Board();
        board.Init();
        
        this.whiteTimer = new PlayerTimer(t, i, this::onWhiteTimeout);
        this.blackTimer = new PlayerTimer(t, i, this::onBlackTimeout);
        this.whiteToMove = true;
                
        this.board.LoadStartPos();
        this.currentPlayerMoves = board.moves;
        
        this.engine = new Engine(board);
        firstMove = true;
    }

    public void Paul() {
        board.LoadFromFen("8/3p4/8/2P5/2R3pk/p1K5/5P2/8 w HAha - 0 1");
        Perft.PerftDivide(board, 5, true);
    }

    public void StartGame() {
        whiteTimer.start();
    }

    // Makes a move on the board, gets a move from the engine
    // and returns the FEN string of the new position
    public String MakeMove(Move move) {
        if (firstMove) {
            StartGame();
            firstMove = false;
        }

        board.MakeMove(move);
        SwitchTurn();
        currentPlayerMoves = board.moves;
        
        return board.toFenString();   
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
        board.WhiteToMove = whiteToMove;
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
