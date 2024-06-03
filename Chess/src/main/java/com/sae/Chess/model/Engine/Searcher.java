package com.sae.Chess.model.Engine;

import com.sae.Chess.model.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.random.*;

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
        board = b;
        evaluation = new Evaluation();
    }

    public Move StartSearch(boolean white) {
        System.out.println("Starting search for " + (isPlayingWhite ? "white" : "black"));
        bestEvalSoFar = 0;
        bestEvalThisIteration = 0;
        bestMoveSoFar = Move.NULL_MOVE;
        bestMoveThisIteration = Move.NULL_MOVE;

        isPlayingWhite = board.WhiteToMove;

        currentDepth = 0;
        searchCancelled = false;

        RunIterativeSearch();
        // In the event that we didn´t find a move, return any one of them
        if (bestMoveSoFar == Move.NULL_MOVE) {
            Random rd = new Random();
            ArrayList<Move> moves = board.mg.GenerateMoves();
            int r = rd.nextInt(0, moves.size());
            bestMoveSoFar = moves.get(r);
        }

        searchCancelled = false;

        return bestMoveSoFar;
    }

    public void EndSearch() {
        searchCancelled = true;
    }

    public void RunIterativeSearch() {
        for (int depth = 1; depth <= 2; depth++) {
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
            else {
                currentDepth = depth;
                bestMoveSoFar = bestMoveThisIteration;
                bestEvalSoFar = bestEvalThisIteration;
                
                bestEvalThisIteration = Integer.MIN_VALUE;
                bestMoveThisIteration = Move.NULL_MOVE;

                if (bestEvalSoFar == mateScore) {
                    break;
                }
            }
        }
    }

    // Negamax version of AlphaBeta
    public int Search(int depth, int plyFromRoot, int alpha, int beta) {
        if (searchCancelled) {
            return 0;
        }

        int score;

        if (depth == 0) {
            return Quiescence(alpha, beta);
        }

        ArrayList<Move> moves = board.mg.GenerateMoves();

        if (moves.size() == 0) {
            if (board.InCheck()) {
                return mateScore;
            }
            else {
                return 0;
            }
        }

        Move bestMoveinThisPosition = Move.NULL_MOVE;

        for (Move move: moves) {
            board.MakeMove(move, false);
            System.out.println("Board after makemove:\n" + board.toString());
            score = -Search(depth - 1, plyFromRoot + 1, -alpha, -beta);
            board.UnmakeMove(move, false);
            System.out.println("Board after unmakemove:\n" + board.toString());

            if (score >= beta) {
                return beta; // Fail-Hard cutoff might need to change to fail-soft
            }
            if (score > alpha) {
                bestMoveinThisPosition = move;
                alpha = score;

                if (plyFromRoot == 0) {
                    bestMoveThisIteration = new Move(move.value);
                    bestEvalThisIteration = score;
                    hasSearchedOneMove = true;
                }
            }
        }
        return alpha;
    }

    // Quiescence search: After our main search,
    // we search as deep as we can until we hit a "quiet" position
    // => a position without any checks, captures etc... possible
    // This is done to counter the horizon effect 
    // https://www.chessprogramming.org/Horizon_Effect
    public int Quiescence(int alpha, int beta) {
        if (searchCancelled) {
            return 0;
        }

        int eval = evaluation.Evaluate(board);

        if (eval >= beta) {
            return beta;
        }
        if (eval > alpha) {
            alpha = eval;
        }

        // Don't generate quiet moves in quiescence
        ArrayList<Move> moves = board.mg.GenerateMoves(false);

        // TODO: Add move ordering
        for (Move move: moves) {
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
