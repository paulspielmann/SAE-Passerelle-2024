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

public class BitboardUtil {
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

    // TODO : Functions to update specific bit(s) in bitboard
    // Problem : can't pass by reference in Java, need to make long wrapper class
}
