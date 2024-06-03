package com.sae.Chess.model.Engine;

import com.sae.Chess.model.*;

public class Evaluation {
    public final int pawnValue = 100;
    public final int knightValue = 300;
    public final int bishopValue = 300;
    public final int rookValue = 500;
    public final int queenValue = 900;

    // TODO: This is a very simple, dummy eval function that needs to be worked on
    public int Evaluate(Board board) {
        Bitboard pawns = new Bitboard(board.Pieces[Piece.Pawn].board);
        Bitboard knight = new Bitboard(board.Pieces[Piece.Knight].board);
        Bitboard bishop = new Bitboard(board.Pieces[Piece.Bishop].board);
        Bitboard rook = new Bitboard(board.Pieces[Piece.Rook].board);
        Bitboard queen = new Bitboard(board.Pieces[Piece.Queen].board);

        int pawnScore = BitboardUtil.PopCount(pawns.board) * pawnValue;
        int knightScore = BitboardUtil.PopCount(knight.board) * knightValue;
        int bishopScore = BitboardUtil.PopCount(bishop.board) * bishopValue;
        int rookScore = BitboardUtil.PopCount(rook.board) * rookValue;
        int queenScore = BitboardUtil.PopCount(queen.board) * queenValue;

        return pawnScore + knightScore + bishopScore + rookScore + queenScore;
    }
}
