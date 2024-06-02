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