package com.sae.Chess.model.Engine;

import com.sae.Chess.model.*;

public class Engine {
    public Board board;
    public Searcher searcher;

    public Engine(Board b) {
        board = b;
        searcher = new Searcher(b);
    }

    public Move getMove() {
        return searcher.StartSearch();
    }
}
