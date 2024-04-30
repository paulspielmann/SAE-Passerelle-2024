import java.util.ArrayList;

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
    public int[] KingSquareIndex;

    public boolean WhiteToMove;
    public int MoveColour = WhiteToMove ? Piece.White : Piece.Black;
    public int OpponentColour = WhiteToMove ? Piece.Black : Piece.White;
    public int FriendlyIndex = WhiteToMove ? WhiteIndex : BlackIndex;
    public int OpponentIndex = 1 - FriendlyIndex;
    public int PieceCount;
    public int CastlingRights;

    boolean cachedCheckValue;
    boolean hasCachedCheckValue;

    public Precomputed precomputed;

    // We store each type of piece in a bitboard regardless of colour
    public Bitboard[] Pieces;
    // And info about the color of each piece in 2 bitboards (white & black)
    public Bitboard[] Colours;

    // Some additional bitboards for movegen purposes
    // Note : the board keeps track of pieces by absolute colour
    // aswell as relative : "friendly"/"opponent" based on current colour's turn
    public Bitboard AllPieces;
    public Bitboard FriendlyOrthoSliders;
    public Bitboard FriendlyDiagSliders;
    public Bitboard EnemyOrthoSliders;
    public Bitboard EnemyDiagSliders;

    public int plyCount;
    public int fullMoveCount;
    public ArrayList<Move> moves;


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
        KingSquareIndex = new int[2];

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
        precomputed = new Precomputed();
    }

    // Is current player in check ?
    public boolean InCheck() {
        if (hasCachedCheckValue) { return cachedCheckValue; }
        cachedCheckValue = CalculateCheckState();
        hasCachedCheckValue = true;
        return cachedCheckValue;
    }

    public boolean CalculateCheckState() {
        int square = KingSquareIndex[MoveColour];
        Bitboard blockers = AllPieces;

        if (EnemyOrthoSliders.board != 0) {
            Bitboard rookAttacks = new Bitboard();
            if ((rookAttacks.board & EnemyOrthoSliders.board) != 0) {
                return true;
            }
        }
        if (EnemyDiagSliders.board != 0) {
            Bitboard bishopAttacks = new Bitboard();
            if ((bishopAttacks.board & EnemyDiagSliders.board) != 0) {
                return true;
            }
        }

        long enemyKnights = Pieces[Piece.MkPiece(Piece.Knight, OpponentColour)].board;
        if ((Precomputed.knightAttacks[square].board & enemyKnights) != 0) {
            return true;
        }

        Bitboard enemyPawns = Pieces[Piece.MkPiece(Piece.Pawn, OpponentColour)];
        Bitboard pawnAttackmask = WhiteToMove ?
            Precomputed.pawnAttacks[square][WhiteIndex] :
            Precomputed.pawnAttacks[square][BlackIndex];
        if ((pawnAttackmask.board & enemyPawns.board) != 0) {
            return true;
        }

        return false;
    }

    public boolean CanCastleQS(boolean white) {
        int mask = white ? 1 : 3;
        return (CastlingRights & mask) != 0;
    }

    public boolean CanCastleKS(boolean white) {
        int mask = white ? 0 : 2;
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

    public void MovePiece(int piece, int source, int dest) {
        Pieces[piece].ToggleBits(source, dest);
        Colours[MoveColour].ToggleBits(source, dest);

        Square[source] = Piece.None;
        Square[dest] = piece;
    }

    public void UpdateSliders() {
        int r = Piece.MkPiece(Piece.Rook, MoveColour);
        int b = Piece.MkPiece(Piece.Bishop, MoveColour);
        int q = Piece.MkPiece(Piece.Queen, MoveColour);

        FriendlyOrthoSliders = new Bitboard(Pieces[r].board | Pieces[q].board);
        FriendlyDiagSliders = new Bitboard(Pieces[b].board | Pieces[q].board);

        int R = Piece.MkPiece(Piece.Rook, OpponentColour);
        int B = Piece.MkPiece(Piece.Bishop, OpponentColour);
        int Q = Piece.MkPiece(Piece.Queen, OpponentColour);

        FriendlyOrthoSliders = new Bitboard(Pieces[R].board | Pieces[Q].board);
        FriendlyDiagSliders = new Bitboard(Pieces[B].board | Pieces[Q].board);
    }

    public void Init() {
        moves = new ArrayList<Move>();
        plyCount = 0;
        PieceCount = 0;
        AllPieces = new Bitboard(0);
        Pieces = new Bitboard[Piece.IndexMax + 1];
        Colours = new Bitboard[2];
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

                    //System.out.println("Adding piece " + c + " at index " + index);
                    Square[index] = piece;
                    Pieces[Piece.GetType(piece)].SetBit(index);
                    Colours[Piece.GetColour(piece)].SetBit(index);
                    PieceCount++;
                    file++;
                }
            }
        }
        AllPieces.board = Colours[WhiteIndex].board | Colours[BlackIndex].board;
        UpdateSliders();

        WhiteToMove = (fields[1] == "w");

        String castle = fields[2];
        boolean whiteKC = castle.contains("K");
        boolean whiteQC = castle.contains("Q");
        boolean blackKC = castle.contains("k");
        boolean blackQC = castle.contains("q");
        CastlingRights = (whiteKC ? 1 << 0 : 0) | (whiteQC ? 1 << 1 : 0)
            | (blackKC ? 1 << 2 : 0) | (blackQC ? 1 << 3 : 0);

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
