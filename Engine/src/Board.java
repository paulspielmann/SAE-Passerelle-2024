import java.util.List;

// Represents current state of the board
// This includes stuff like side to move, en passant, castling rights, and so on
public class Board {
    public String startingFen =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static short WhiteIndex = 0;
    public static short BlackIndex = 1;

    // Keep track of all piece types and colours in a separate array
    // for operations where the bitboard is not the easiest/most efficient
    public int[] Square;
    public int[] KingSquare;

    public boolean WhiteToMove;
    public int PieceCount;

    // We store each type of piece in a bitboard regardless of colour
    public long[] PieceBitboards;
    // And info about the color of each piece in 2 bitboards (white & black)
    public long[] ColourBitboards;

    public int plyCount;
    public int fullMoveCount;
    public List<Move> moves;

    // TODO:
    // Ply count
    // Fifty move counter for repetition rule
    // FEN/PGN/whatever
    // UCI
    // https://www.chessprogramming.org/Zobrist_Hashing

    public Board() {
        plyCount = 0;
        fullMoveCount = 1; // This always starts at 1
        Square = new int[64];
    }

    public void UpdateBitboards(int piece, int source, int dest) {

    }

    public void MakeMove(Move move) {
        int source = move.source;
        int dest = move.dest;
        int movedPiece = Square[source];
        int flag = move.flag;
        int capturedPiece = Square[dest];
    }

    public void LoadFromFen(String fen) {
        String[] fields = fen.split(" ");
        String[] rows = fields[0].split("/");

        for (String s: rows) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);

                // Do stuff
            }
        }

    }

    public String toString() {
        int i = 1;
        String res = "";
        for (int p: Square) {
            res = res + Piece.ToChar(p);
            i++;
            if (i % 8 == 0) res += "\n";
        }
        return res;
    }
}
