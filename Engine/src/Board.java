import java.util.ArrayList;
import java.util.Stack;

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
    public int MoveColour; // = WhiteToMove ? Piece.White : Piece.Black;
    public int OpponentColour; // = WhiteToMove ? Piece.Black : Piece.White;
    public int FriendlyIndex; // = WhiteToMove ? WhiteIndex : BlackIndex;
    public int OpponentIndex; // = 1 - FriendlyIndex;
    public int PieceCount;
    public int PieceCountNoPawnsNoKings;
    public int CastlingRights;
    public GameState CurrentGameState;

    boolean cachedCheckValue;
    boolean hasCachedCheckValue;

    public MoveGenerator mg;
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

    public Magic magic;

    public int PlyCount;
    public int FullMoveCount;
    public ArrayList<Move> moves;
    public Stack<GameState> gsHistory;

    // TODO:
    // Fifty move counter for repetition rule
    // FEN/PGN/whatever -> FEN DONE
    // UCI
    // Zobrist's hash https://www.chessprogramming.org/Zobrist_Hashing

    public Board() {
        PlyCount = 0;
        FullMoveCount = 1; // This always starts at 1
        Square = new int[64];
        KingSquareIndex = new int[2];
        KingSquareIndex[0] = -1;
        KingSquareIndex[1] = -1;

        Pieces = new Bitboard[7];
        Pieces[0] = new Bitboard();
        Pieces[1] = new Bitboard();
        Pieces[2] = new Bitboard();
        Pieces[3] = new Bitboard();
        Pieces[4] = new Bitboard();
        Pieces[5] = new Bitboard();
        Pieces[6] = new Bitboard();
        Colours = new Bitboard[2];
        Colours[0] = new Bitboard();
        Colours[1] = new Bitboard();

        CastlingRights = 0b1111;
        FriendlyIndex = 0;
        OpponentIndex = 1;
        MoveColour = 0;
        OpponentColour = Piece.Black;

        precomputed = new Precomputed();
        magic = new Magic();
    }

    public Board(String fen) {
        new Board().LoadFromFen(fen);
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

        long enemyKnights = Pieces[Piece.Knight].board & Colours[OpponentIndex].board;
        if ((Precomputed.knightAttacks[square].board & enemyKnights) != 0) {
            return true;
        }

        Bitboard enemyPawns = new Bitboard(Pieces[Piece.Pawn].board
                                           & Colours[OpponentIndex].board);
        Bitboard pawnAttackmask = WhiteToMove ?
            Precomputed.pawnAttacks[square][WhiteIndex] :
            Precomputed.pawnAttacks[square][BlackIndex];
        if ((pawnAttackmask.board & enemyPawns.board) != 0) {
            return true;
        }

        return false;
    }

    public void MakeMove(Move move) { MakeMove(move, true); }

    public void MakeMove(Move move, boolean record) {
        int source = move.source;
        int dest = move.dest;
        int flag = move.flag;

        int movedPiece = Square[source];
        int movedType = Piece.GetType(movedPiece);

        int capturedPiece = Square[dest];
        int capturedType = Piece.GetType(capturedPiece);

        boolean isPromotion = move.IsPromotion();
        boolean isEnPassant = move.IsEnPassant();

        int newCastlingRights = CurrentGameState.castlingRights;
        int prevEpFile = CurrentGameState.epFile;
        int new_epFile = CurrentGameState.epFile;
        long newZobristKey = CurrentGameState.zobristKey;

        Pieces[movedType].UnsetBit(source);
        Pieces[movedType].SetBit(dest);
        Colours[WhiteToMove ? WhiteIndex : BlackIndex].UnsetBit(source);
        Colours[WhiteToMove ? WhiteIndex : BlackIndex].SetBit(dest);

        Square[source] = Piece.None;
        Square[dest] = movedPiece;

        if (capturedType != Piece.None) {
            int captureSquare = dest;

            if (isEnPassant) {
                // If en passant, the captured pawn is one rank above
                // the destination square
                captureSquare = dest + (WhiteToMove ? -8 : 8);
                Square[captureSquare] = Piece.None;
            }

            if (capturedType != Piece.Pawn) {
                PieceCountNoPawnsNoKings--;
            }

            // Remove captured piece from bitboards
            Pieces[capturedType].UnsetBit(dest);
            Colours[WhiteToMove ? BlackIndex : WhiteIndex].UnsetBit(dest);
            newZobristKey ^= Zobrist.pieces[capturedPiece][captureSquare];
        }

        if (movedType == Piece.King) {
            KingSquareIndex[MoveColour / 8] = dest;
            // Mask out side to move's castling rights
            newCastlingRights &= WhiteToMove ? 0b1100 : 0b0011;

            if (flag == Move.Castle) {
                int rookP = Piece.MkPiece(Piece.Rook, MoveColour);
                boolean kingside = dest == BoardHelper.g1 || dest == BoardHelper.g8;
                int rookFrom = kingside ? dest + 1 : dest - 2;
                int rookTo = kingside ? dest - 1 : dest + 2;

                Pieces[Piece.Rook].ToggleBits(rookFrom, rookTo);
                Colours[FriendlyIndex].ToggleBits(rookFrom, rookTo);
                Square[rookFrom] = Piece.None;
                Square[rookTo] = Piece.Rook | MoveColour;

                newZobristKey ^= Zobrist.pieces[rookP][rookFrom];
                newZobristKey ^= Zobrist.pieces[rookP][rookTo];
            }
        }

        if (isPromotion) {
            PieceCountNoPawnsNoKings++;
            int promotionType = move.GetPromotionPiece();
            int promotionPiece = Piece.MkPiece(promotionType, MoveColour);

            Pieces[movedType].ToggleBit(dest);
            Pieces[promotionType].ToggleBit(dest);
            Square[dest] = promotionPiece;
        }

        if (flag == Move.PawnDoubleMove) {
            int file = BoardHelper.FileIndex(source) + 1;
            new_epFile = file;
            newZobristKey ^= Zobrist.epFile[file];
        }

        // Any piece moving to or from a rook's square remove that colour's
        // castling rights on this side
        if (CurrentGameState.castlingRights != 0) {
            if (dest == BoardHelper.h1 || source == BoardHelper.h1) {
                newCastlingRights &= GameState.ClearWhiteKingsideMask;
            }
            if (dest == BoardHelper.a1 || source == BoardHelper.a1) {
                newCastlingRights &= GameState.ClearWhiteQueensideMask;
            }
            if (dest == BoardHelper.h8 || source == BoardHelper.h8) {
                newCastlingRights &= GameState.ClearBlackKingsideMask;
            }
            if (dest == BoardHelper.a8 || source == BoardHelper.a8) {
                newCastlingRights &= GameState.ClearBlackQueensideMask;
            }
        }

        // Handle zobrist hash stuff
        newZobristKey ^= Zobrist.sideToMove;
        newZobristKey ^= Zobrist.pieces[movedPiece][source];
        newZobristKey ^= Zobrist.pieces[Square[dest]][dest];
        newZobristKey ^= Zobrist.epFile[prevEpFile];


        WhiteToMove = !WhiteToMove;

        MoveColour = WhiteToMove ? Piece.White : Piece.Black;
        OpponentColour = WhiteToMove ? Piece.Black : Piece.White;
        FriendlyIndex = WhiteToMove ? WhiteIndex : BlackIndex;
        OpponentIndex = 1 - FriendlyIndex;

        PlyCount++;
        int newFiftyMoveCount = CurrentGameState.fiftyMoveCount + 1;

        AllPieces.board = Colours[WhiteIndex].board | Colours[BlackIndex].board;
        UpdateSliders();

        GameState newState = new GameState(capturedType,
                                           new_epFile,
                                           newCastlingRights,
                                           newFiftyMoveCount,
                                           newZobristKey);
        moves.add(move);
        gsHistory.push(newState);
        CurrentGameState = newState;
        hasCachedCheckValue = false;

        //System.out.println("After MakeMove: " + move.toString());
        //BitboardUtil.print2(Colours[0], Colours[1], -1);
        // System.out.println(toString());
    }

    public void UnmakeMove(Move move) { UnmakeMove(move, true); }

    public void UnmakeMove(Move move, boolean record) {
        WhiteToMove = !WhiteToMove;
        MoveColour = WhiteToMove ? Piece.White : Piece.Black;
        OpponentColour = WhiteToMove ? Piece.Black : Piece.White;
        FriendlyIndex = WhiteToMove ? WhiteIndex : BlackIndex;
        OpponentIndex = 1 - FriendlyIndex;

        boolean colourUndone = WhiteToMove;

        int source = move.source;
        int dest = move.dest;
        int flag = move.flag;

        boolean epUndo = move.IsEnPassant();
        boolean promoUndo = move.IsPromotion();
        boolean captureUndo = CurrentGameState.capturedPieceType != Piece.None;

        int movedPiece = promoUndo ? Piece.MkPiece(Piece.Pawn, MoveColour) : Square[dest];
        int movedType = Piece.GetType(movedPiece);
        int capturedType = CurrentGameState.capturedPieceType;

        if (promoUndo) {
            int promotedPiece = Square[dest];
            int promotedType = Piece.GetType(promotedPiece);
            PieceCountNoPawnsNoKings--;
            Pieces[promotedType].ToggleBit(dest);
            Pieces[Piece.Pawn].ToggleBit(dest);
        }

        //System.out.println("Moved back piece of type " + Piece.ToChar(movedType));
        Pieces[movedType].ToggleBits(source, dest);
        //System.out.println("Of colour " + OpponentIndex);
        Colours[FriendlyIndex].ToggleBits(source, dest);

        Square[dest] = Piece.None;
        Square[source] = movedPiece;

        // System.out.println("Current gamestate: "
        //                    + toString() + "\n"
        //                    + Pieces[movedType].toString() + "\n"
        //                    + Colours[MoveColour / 8].toString() + "\n"
        //                    + "Undoing : " + WhiteToMove);


        // Restore captured piece
        if (captureUndo) {
            int captureSquare = dest;
            int capturedPiece = Piece.MkPiece(capturedType, OpponentColour);

            if (epUndo) {
                captureSquare = dest + (colourUndone ? -8 : 8);
            }
            if (capturedType != Piece.Pawn) {
                PieceCountNoPawnsNoKings++;
            }

            Pieces[capturedType].ToggleBit(captureSquare);
            Colours[OpponentIndex].ToggleBit(captureSquare);
            Square[captureSquare] = capturedPiece;
        }

        if (movedType == Piece.King) {
            KingSquareIndex[FriendlyIndex] = source;

            if (flag == Move.Castle) {
                boolean kingside = dest == BoardHelper.g1 || dest == BoardHelper.g8;
                int before = kingside ? dest + 1 : dest - 2;
                int after = kingside ? dest - 1 : dest + 1;

                Pieces[Piece.Rook].ToggleBits(after, before);
                Colours[OpponentIndex].ToggleBits(after, before);
                Square[after] = Piece.None;
                Square[before] = Piece.Rook;
            }
        }
        AllPieces.board = Colours[WhiteIndex].board | Colours[BlackIndex].board;
        UpdateSliders();

        // System.out.println("After UnmakeMove: " + move.toString());
        // BitboardUtil.print2(Colours[0], Colours[1], -1);
        // System.out.println(toString());

        if (record) {
            moves.remove(moves.size() - 1);
            gsHistory.pop();
            CurrentGameState = gsHistory.peek();
            PlyCount--;
            hasCachedCheckValue = false;
        }
    }

    // Update nontrivial bitboards
    public void UpdateSliders() {
        int r = Piece.Rook;
        int b = Piece.Bishop;
        int q = Piece.Queen;

        long orthos = Pieces[r].board | Pieces[q].board;
        long diags = Pieces[b].board | Pieces[q].board;

        FriendlyOrthoSliders = new Bitboard(orthos & Colours[FriendlyIndex].board);
        FriendlyDiagSliders = new Bitboard(diags & Colours[FriendlyIndex].board);

        EnemyOrthoSliders = new Bitboard(orthos & Colours[OpponentIndex].board);
        EnemyDiagSliders = new Bitboard(diags & Colours[OpponentIndex].board);
    }

    public void Init() {
        WhiteToMove = true;
        moves = new ArrayList<Move>();
        CurrentGameState = new GameState(Piece.None, 0, 0b1111, 0, 0);
        gsHistory = new Stack<GameState>();
        gsHistory.push(CurrentGameState);
        PlyCount = 0;
        PieceCount = 0;
        PieceCountNoPawnsNoKings = 0;
        AllPieces = new Bitboard(0);
        Pieces = new Bitboard[Piece.IndexMax + 1];
        for (int i = 0; i <= 14; i++) { Pieces[i] = new Bitboard(); }
        Colours = new Bitboard[2];
        Colours[0] = new Bitboard();
        Colours[1] = new Bitboard();
        EnemyDiagSliders = new Bitboard();
        EnemyOrthoSliders = new Bitboard();
        FriendlyDiagSliders = new Bitboard();
        FriendlyOrthoSliders = new Bitboard();
        mg = new MoveGenerator(this);
    }

    public void LoadStartPos() {
        LoadFromFen(startingFen);
    }

    // Load game state and position from standard FEN string
    public void LoadFromFen(String fen) {
        String[] fields = fen.split(" ");

        int file = 0;
        int rank = 7; // FEN strings have rank 8 at the leftmost extremity

        for (char c: fields[0].toCharArray()) {
            if (c == '/') {
                file = 0;
                rank--;
            }
            else {
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c);
                }
                else {
                    int index = rank * 8 + file;
                    int piece = Piece.FromChar(c);
                    Square[index] = piece;
                    Pieces[Piece.GetType(piece)].SetBit(index);
                    Colours[Piece.GetColour(piece) >> 3].SetBit(index);
                    PieceCount++;
                    file++;

                    if (Piece.GetType(piece) == Piece.King) {
                        KingSquareIndex[Piece.GetColour(piece) / 8] = index;
                    }
                }
            }
        }
        WhiteToMove = (fields[1].equals("w"));

        AllPieces.board = Colours[WhiteIndex].board | Colours[BlackIndex].board;
        UpdateSliders();

        String castle = fields[2];
        boolean whiteKC = castle.contains("K");
        boolean whiteQC = castle.contains("Q");
        boolean blackKC = castle.contains("k");
        boolean blackQC = castle.contains("q");
        CastlingRights = (whiteKC ? 1 << 0 : 0) | (whiteQC ? 1 << 1 : 0)
            | (blackKC ? 1 << 2 : 0) | (blackQC ? 1 << 3 : 0);

        FullMoveCount = Integer.parseInt(fields[5]);
    }

    public String toString() {
        String res = "";
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int p = Square[rank * 8 + file];
                res += Piece.ToChar(p);
            }
            res += "\n"; //" " + (rank + 1)  + "\n";
        }
        //res += "ABCDEFGH";
        return res;
   }

    public String toStringSquareIndex() {
        String res = "";
        int i;
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                i = rank * 8 + file;
                res += i + (i >= 10 ? " " : "  ");
            }
            res += "\n";
        }
        return res;
    }
}
