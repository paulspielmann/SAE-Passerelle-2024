package com.sae.Chess.model.Engine;

import com.sae.Chess.model.*;
import java.util.ArrayList;

public class Searcher {
    public Board board;
    final int mateScore = 100000;
    final int positiveInfinity = 999999999;
    final int negativeInfinity = -positiveInfinity;

    public int currentIterDepth;
    public int currentDepth;
    public Move bestMoveSoFar;
    public int bestEvalSoFar;

    public boolean isPlayingWhite;
    public Move bestMoveThisIteration;
    public int bestEvalThisIteration;

    public boolean searchCancelled;
    public boolean hasSearchedOneMove;

    public Evaluation evaluation;

    public Searcher(Board b) {
        board = new Board(b);
        evaluation = new Evaluation(board);
    }

    public Move StartSearch() {
        System.out.println("Starting search for " + (isPlayingWhite ? "white" : "black"));
        bestEvalSoFar = 0;
        bestEvalThisIteration = 0;
        bestMoveSoFar = Move.NULL_MOVE;
        bestMoveThisIteration = Move.NULL_MOVE;

        isPlayingWhite = board.WhiteToMove;

        currentDepth = 0;
        searchCancelled = false;

        RunIterativeSearch();
        // In the event that we didn't find a move, return any one of them
        if (bestMoveSoFar == Move.NULL_MOVE) {
            bestMoveSoFar = board.mg.GenerateMoves().get(0);
        }

        searchCancelled = false;

        return bestMoveSoFar;
    }

    public void EndSearch() {
        searchCancelled = true;
    }

    public void RunIterativeSearch() {
        for (int depth = 1; depth <= 256; depth++) {
            hasSearchedOneMove = false;
            currentIterDepth = depth;
            Search(depth, 0, negativeInfinity, positiveInfinity);

            if (searchCancelled) {
                if (hasSearchedOneMove) {
                    bestMoveSoFar = bestMoveThisIteration;
                    bestEvalSoFar = bestEvalThisIteration;
                }
                break;
            }
        }
    }

    public int Search(int depth, int ply, int alpha, int beta) {
        if (depth == 0) {
            return Quiescence(alpha, beta);
        }

        currentDepth = Math.max(currentDepth, ply);

        ArrayList<Move> moves = board.mg.GenerateMoves();
        if (moves.isEmpty()) {
            if (board.InCheck()) {
                return -mateScore + ply;
            } else {
                return 0;
            }
        }

        int eval;
        for (Move move : moves) {
            board.MakeMove(move, false);
            eval = -Search(depth - 1, ply + 1, -beta, -alpha);
            board.UnmakeMove(move, false);

            if (eval >= beta) {
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
                if (ply == 0) {
                    bestMoveThisIteration = move;
                    bestEvalThisIteration = eval;
                }
            }
        }

        return alpha;
    }

    public int Quiescence(int alpha, int beta) {
        int eval = evaluation.Evaluate();

        if (eval >= beta) {
            return beta;
        }
        if (eval > alpha) {
            alpha = eval;
        }

        ArrayList<Move> moves = board.mg.GenerateMoves();
        for (Move move : moves) {
            board.MakeMove(move, false);
            eval = -Quiescence(-beta, -alpha);
            board.UnmakeMove(move, false);

            if (eval >= beta) {
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }

        return alpha;
    }
}
