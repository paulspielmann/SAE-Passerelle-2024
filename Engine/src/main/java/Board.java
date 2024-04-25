import java.util.List;

// Represents current state of the board
// This includes stuff like side to move, en passant, castling rights, and so on
public class Board {
    public static short WhiteIndex = 0;
    public static short BlackIndex = 1;

    // Keep track of all piece types and colours in a separate array
    // for operations where the bitboard is not the easiest/most efficient
    public int[] Square;
    public int[] KingSquare;

    public boolean WhiteToMove;
    public int PieceCount;

    // We store each type of piece in a bitboard regardless of colour
    public long[] PieceBitBoards;
    // And info about the color of each piece in 2 bitboards (white & black)
    public long[] ColourBitBoards;

    public List<Move> moves;

    // TODO:
    // Ply count
    // Fifty move counter for repetition rule
    // FEN/PGN/whatever
    // UCI
    // https://www.chessprogramming.org/Zobrist_Hashing

    public Board() {
        Square = new int[64];
    }
}
