package com.sae.Chess.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Move NULL_MOVE = new Move(0, 0);

    // A square index (0 to 63) can fit in just 6 bits. This allows us to
    // encode a move in a 16 bit short : 6 bits of source and destination
    // squares, with 4 remaining bits for additional info (promo, en passant..)
    public short value;
    public int source;
    public int dest;
    public int flag;

    // 4 bit flags
    public static final int None = 0b0000;
    public static final int EnPassant = 0b0001;
    public static final int Castle = 0b0010;
    public static final int PawnDoubleMove = 0b0011;

    public static final int PromoteKnight = 0b0100;
    public static final int PromoteBishop = 0b0101;
    public static final int PromoteRook = 0b0110;
    public static final int PromoteQueen = 0b0111;

    public static final int WhiteMove = 0b0000;
    public static final int BlackMove = 0b1000;

    // Masks
    public static final int SourceMask = 0b0000000000111111;
    public static final int DestMask   = 0b0000111111000000;
    public static final int FlagMask   = 0b1111000000000000;

    public Move(short val) {
        value = val;
        source = source();
        dest = dest();
        flag = flag();
    }

    public Move(int source, int dest) {
        this(source, dest, Move.None);
    }

    @JsonCreator
    public Move(@JsonProperty("source") int source, 
                @JsonProperty("dest") int dest, 
                @JsonProperty("flag") int flag) {
        value = (short) (source | dest << 6 | flag << 12);
        this.source = source;
        this.dest = dest;
        this.flag = flag;
    }

    public int source() { return value & SourceMask; }

    public int dest() { return value & DestMask; }

    public int flag() { return value >> 12; }

    public boolean IsPromotion() { return flag() >= PromoteKnight; }

    public boolean IsEnPassant() { return flag() == EnPassant; }

    public int GetPromotionPiece() {
        switch (flag()) {
        case PromoteKnight:
            return Piece.Knight;
        case PromoteBishop:
            return Piece.Bishop;
        case PromoteRook:
            return Piece.Rook;
        case PromoteQueen:
            return Piece.Queen;
        default:
            return Piece.None;
        }
    }

    public String toBinaryString() {
        String res = "";
        for (int i = 15; i >= 0; i--) {
            res += ((value >> i) & 1);
        }
        return res;
    }

    public String toPrettyString() {
        String res = "";
        res += BoardHelper.FileLetterFromIndex(source);
        res += BoardHelper.RealRankIndex(source);
        res += "->";
        res += BoardHelper.FileLetterFromIndex(dest);
        res += BoardHelper.RealRankIndex(dest);
        return res;
    }

    public String toString() {
        String res = "";
        res += BoardHelper.FileLetterFromIndex(source);
        res += BoardHelper.RealRankIndex(source);
        res += BoardHelper.FileLetterFromIndex(dest);
        res += BoardHelper.RealRankIndex(dest);
        res += IsPromotion() ? Piece.ToChar(GetPromotionPiece() | Piece.Black) : "";
        return res;
    }
}
