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

public class Bitboard {
    public long board;

    public Bitboard() {
        board = 0x0000000000000000L;
    }

    public Bitboard(long b) {
        board = b;
    }

    public void ToggleBit(int index) { board ^= 1L << index; }
    public void SetBit(int index) { board |= 1L << index; }
    public void UnsetBit(int index) { board &= ~(1L << index); }
    public boolean Contains(int index) { return ((board >> index) & 1) != 0; }
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

    // Should these return bitboards ? See MoveGenerator.java
    public static long Shift(Bitboard b, int n) {
        return (n > 0 ? b.board << n : b.board >> -n);
    }

    public static long ShiftSouth(Bitboard b) {
        b.board >>>= 8;
        return b.board;
    }

    public static long ShiftNorth(Bitboard b) {
        b.board <<= 8;
        return b.board;
    }

    public static long ShiftEast(Bitboard b) {
        b.board = (b.board << 1) & BitboardUtil.NotFileA;
        return b.board;
    }

    public static long ShiftNoEa(Bitboard b) {
        b.board = (b.board << 9) & BitboardUtil.NotFileA;
        return b.board;
    }

    public static long ShiftSoEa(Bitboard b) {
        b.board = (b.board >> 7) & BitboardUtil.NotFileA;
        return b.board;
    }

    public static long ShiftWest(Bitboard b) {
        b.board = (b.board >> 1) & BitboardUtil.NotFileH;
        return b.board;
    }
    public static long ShiftSoWe(Bitboard b) {
        b.board = (b.board >> 9) & BitboardUtil.NotFileH;
        return b.board;
    }
    public static long ShiftNoWe(Bitboard b) {
        b.board = (b.board << 7) & BitboardUtil.NotFileH;
        return b.board;
    }

    public static int PopLSB(Bitboard b) {
        int i = Long.numberOfTrailingZeros(b.board);
        b.board &= (b.board - 1);
        return i; // Return LSB index
    }

    public static long PawnAttacks(Bitboard pawns, boolean white) {
        if (white) {
            return (BitboardUtil.ShiftNoEa(pawns) | BitboardUtil.ShiftNoWe(pawns));
        }
        else {
            return (BitboardUtil.ShiftSoEa(pawns) | BitboardUtil.ShiftSoWe(pawns));
        }
    }
}
