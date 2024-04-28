import java.util.List;

// Represents current state of the board
// This includes stuff like side to move, en passant, castling rights, and so on
public class Board {
    public static String startingFen =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static short WhiteIndex = 0;
    public static short BlackIndex = 1;

    // Keep track of all piece types and colours in a separate array
    // for operations where the bitboard is not the easiest/most efficient
    public int[] Square;

    public int CastlingRights;
    public boolean WhiteToMove;
    public int PieceCount;

    // We store each type of piece in a bitboard regardless of colour
    public Bitboard[] Pieces;
    // And info about the color of each piece in 2 bitboards (white & black)
    public Bitboard[] Colours;

    public int plyCount;
    public int fullMoveCount;
    public List<Move> moves;

    // TODO:
    // En passant squares
    // Castling rights
    // Checks
    // Fifty move counter for repetition rule
    // FEN/PGN/whatever
    // UCI
    // https://www.chessprogramming.org/Zobrist_Hashing

    public Board() {
        plyCount = 0;
        fullMoveCount = 1; // This always starts at 1
        Square = new int[64];

        Pieces = new Bitboard[6];
        Pieces[0] = new Bitboard();
        Pieces[1] = new Bitboard();
        Pieces[2] = new Bitboard();
        Pieces[3] = new Bitboard();
        Pieces[4] = new Bitboard();
        Pieces[5] = new Bitboard();
        Colours = new Bitboard[2];
        Colours[0] = new Bitboard();
        Colours[1] = new Bitboard();

        CastlingRights = 0b1111;
    }

    public boolean CanCastleQS(boolean white) {
        int mask = white ? 2 : 4;
        return (CastlingRights & mask) != 0;
    }

    public boolean CanCastleKS(boolean white) {
        int mask = white ? 1 : 4;
        return (CastlingRights & mask) != 0;
    }

    // TODO: Check legality of move (out of bounds, check...)
    public void MakeMove(Move move) {
        int source = move.source;
        int dest = move.dest;
        int movedPiece = Square[source];
        int movedType = Piece.GetType(movedPiece);
        int flag = move.flag;
        int capturedPiece = Square[dest];
        int capturedType = Piece.GetType(capturedPiece);

        Pieces[movedType].UnsetBit(source);
        Pieces[movedType].SetBit(dest);
        Colours[WhiteToMove ? WhiteIndex : BlackIndex].UnsetBit(source);
        Colours[WhiteToMove ? WhiteIndex : BlackIndex].SetBit(dest);

        if (capturedType != Piece.None) {
            Pieces[capturedType - 1].UnsetBit(dest);
            Colours[WhiteToMove ? BlackIndex : WhiteIndex].UnsetBit(dest);
        }
    }

    public void LoadFromFen(String fen) {
        String[] fields = fen.split(" ");

        int file = 0;
        int rank = 7; // FEN position starts with rank 8

        for (char c: fields[0].toCharArray()) {
            if (c == '/') {
                file = 0;
                rank--;
            }
            else {
                if (Character.isDigit(c)) {
                    file += (int) c - 48;
                }
                else {
                    int index = rank * 8 + file;
                    int piece = Piece.FromChar(c);

                    System.out.println("Adding piece " + c + " at index " + index);
                    Square[index] = piece;
                    Pieces[Piece.GetType(piece) - 1].SetBit(index);
                    Colours[Piece.GetColour(piece)].SetBit(index);
                    PieceCount++;
                    file++;
                }
            }
        }
        WhiteToMove = (fields[1] == "w");
        fullMoveCount = Integer.parseInt(fields[5]);
    }

    public String toString() {
        int i = 0;
        String res = "";
        for (int p: Square) {
            res = res + Piece.ToChar(p);
            i++;
            if (i % 8 == 0) res += "\n";
        }
        return res;
    }
}
