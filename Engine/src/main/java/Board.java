// Represents current state of the board
// This includes stuff like side to move, en passant, castling rights, and so on
public class Board {
    public static short White = 0;
    public static short Black = 1;

    public int[] Square;
    public int[] KingSquare;

    public int PieceCount;

    // We store each type of piece in a bitboard regardless of colour
    public long[] PieceBitBoards;
    // And info about the color of each piece in 2 bitboards (white & black)
    public long[] ColourBitBoards;
}
