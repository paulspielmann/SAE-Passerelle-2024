package com.sae.Chess.model;

import java.io.Serializable;
// A bitboard (for a piece type and color) is a 64 bit "long" where each bit
// represents a square on the board. A one bit implies the existence of a piece
// of this piece-type on the square associated with the bit's index.

// We use the "Least Significant File Mapping" : the e4 square (Rank 3, File 4)
// is indexed like so : Rank * 8 + File = 28
// The bitboard is little endian although converting back and from big endian
// is as simple as XORing the square index with 63

// Compass rose :
// noWe         nort         noEa
//         +7    +8    +9
//             \  |  /
// west    -1 <-  0 -> +1    east
//             /  |  \
//         -9    -8    -7
// soWe         sout         soEa

public class Bitboard implements Serializable {
    public Long board;

    public Bitboard() {
        board = 0x0000000000000000L;
    }

    public Bitboard(long b) {
        board = b;
    }

    public boolean equals(Bitboard b) { return board.equals(b.board); }

    public void ToggleBit(int index) {
        board ^= 1L << index;
    }

    public void ToggleBits(int a, int b) {
        board ^= (1L << a | 1L << b);
    }

    public void SetBit(int index) {
        board |= (1L << index);
    }

    public void UnsetBit(int index) {
        board &= ~(1L << index);
    }

    public boolean Contains(int index) {
        return ((board >>> index) & 1) != 0;
    }

    public String toString() {
        return BitboardUtil.toFormattedString(board);
    }

    public String toHString(int hsq) {
        return BitboardUtil.toHString(board, hsq);
    }
}

class BitboardUtil {
    public static long FileA = 0x0101010101010101L;
    public static long FileH = 0x8080808080808080L;
    public static long NotFileA = ~FileA;
    public static long NotFileH = ~FileH;

    public static long Rank1 = 0x00000000000000FFL;
    public static long Rank2 = Rank1 << 8;
    public static long Rank3 = Rank2 << 8;
    public static long Rank4 = Rank3 << 8;
    public static long Rank5 = Rank4 << 8;
    public static long Rank6 = Rank5 << 8;
    public static long Rank7 = Rank6 << 8;
    public static long Rank8 = Rank7 << 8;

    public static long DiagA1 = 0x8040201008040201L;
    public static long DiagA8 = 0x0102040810204080L;

    public static long LightSquares = 0x55AA55AA55AA55AAL;
    public static long DarkSquares = 0xAA55AA55AA55AA55L;

    public static long WhiteKingSideMask = 1L << BoardHelper.f1 | 1L << BoardHelper.g1;
    public static long BlackKingSideMask = 1L << BoardHelper.f8 | 1L << BoardHelper.g8;

    public static long WhiteQueenSideMask =
        1L << BoardHelper.c1 | 1L << BoardHelper.d1;
    public static long BlackQueenSideMask =
        1L << BoardHelper.c8 | 1L << BoardHelper.d8;

    public static long WhiteCastleBlockMask = WhiteQueenSideMask | 1L << BoardHelper.b1;
    public static long BlackCastleBlockMask = BlackQueenSideMask | 1L << BoardHelper.b8;

    public static long Shift(Bitboard b, int n) {
        return (n > 0 ? b.board << n : b.board >>> -n);
    }

    public static long Shift(long b, int n) {
        return (n > 0 ? b << n : b >>> -n);
    }

    public static int PopLSB(Bitboard b) {
        int i = Long.numberOfTrailingZeros(b.board);
        b.board &= (b.board - 1);
        return i; // Return LSB index
    }

    public static long PawnAttacks(Bitboard pawns, boolean white) {
        long res = 0L;
        if (white) {
            res |= Shift(pawns.board, 9) & NotFileA;
            res |= Shift(pawns.board, 7) & NotFileH;
            return res;
        }
        else {
            res |= Shift(pawns.board, -9) & NotFileH;
            res |= Shift(pawns.board, -7) & NotFileA;
            return res;
        }
    }

    public static String toFormattedString(long bb) {
        return toFormattedString(bb, -1);
    }

    public static String toFormattedString(long bb, int i) {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int sq = rank * 8 + file;
                if (sq == i) {
                    sb.append("X ");
                }
                else {
                    long mask = 1L << sq;
                    char b = (bb & mask) != 0 ? '1' : '0';
                    sb.append(b).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String toHString(long bb, int hsq) {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int sq = rank * 8 + file;
                long mask = 1L << sq;
                char b = (bb & mask) != 0 ? '1' : '.';
                if (sq == hsq) {
                    sb.append("X ");
                }
                else {
                    sb.append(b).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void print2(Bitboard a, Bitboard b, int hsq, String an, String bn) {
        String[] patternLines = a.toHString(hsq).split("\n");
        String[] legalMovesLines = b.toHString(hsq).split("\n");

        if (an == "" || bn == "") {
            System.out.println(" First Bitboard         Second Bitboard");
            System.out.println("---------------         ----------------");
        }
        else {
            int columnWidth = 16;
            int maxWidth = Math.max(an.length(), bn.length());
            maxWidth = Math.max(maxWidth, columnWidth);
            String pattern = "%" + (-maxWidth) + "s         %" + (-maxWidth) + "s";
            String res = String.format(pattern,
                                       format(an, maxWidth),
                                       format(bn, maxWidth));
            System.out.println(res);
            System.out.println("---------------         ----------------");
        }
        for (int i = 0; i < patternLines.length; i++) {
            System.out.printf("%s         %s%n", patternLines[i], legalMovesLines[i]);
        }
    }

    public static void print2(long a, long b, String an, String bn) {
        print2(new Bitboard(a), new Bitboard(b), an, bn);
    }

    public static void print2(Bitboard a, Bitboard b, String an, String bn) {
        print2(a, b, -1, an, bn);
    }

    public static void print2(Bitboard a, Bitboard b, int hsq) {
        print2(a, b, -1, "", "");
    }

    public static void print2(Bitboard a, Bitboard b) {
        print2(a, b, -1);
    }

    public static String format(String s, int n) {
        if (s.length() > n) {
            return s.substring(0, n);
        }
        else {
            return String.format("%-" + n + "s", s);
        }
    }
}
