package com.sae.Chess.model;

import java.util.ArrayList;
import java.util.Stack;

// Represents current state of the board
// This includes stuff like side to move, en passant, castling rights, and so on
public class Board {
    public static String startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

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
    public Zobrist zobrist;

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
    public ArrayList<Move> moveHistory;
    public Stack<GameState> gsHistory;

    // TODO:
    // repetition rule
    // Add parameter to Make/Unmake Move to
    // record or not the move in the game history
    // PGN
    // UCI

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
        zobrist = new Zobrist();
    }

    public Board(Board b) {
        PlyCount = b.PlyCount;
        FullMoveCount = b.FullMoveCount;
        Square = new int[64];

        for (int i = 0; i < 64; i++) {
            Square[i] = b.Square[i];
        }

        KingSquareIndex = new int[2];
        KingSquareIndex[0] = b.KingSquareIndex[0];
        KingSquareIndex[1] = b.KingSquareIndex[1];

        Pieces = new Bitboard[7];
        Pieces[0] = new Bitboard(b.Pieces[0].board);
        Pieces[1] = new Bitboard(b.Pieces[1].board);
        Pieces[2] = new Bitboard(b.Pieces[2].board);
        Pieces[3] = new Bitboard(b.Pieces[3].board);
        Pieces[4] = new Bitboard(b.Pieces[4].board);
        Pieces[5] = new Bitboard(b.Pieces[5].board);
        Pieces[6] = new Bitboard(b.Pieces[6].board);

        Colours = new Bitboard[2];
        Colours[0] = new Bitboard(b.Colours[0].board);
        Colours[1] = new Bitboard(b.Colours[1].board);

        CastlingRights = b.CastlingRights;
        FriendlyIndex = b.FriendlyIndex;
        OpponentIndex = b.OpponentIndex;
        MoveColour = b.MoveColour;
        OpponentColour = b.OpponentColour;

        precomputed = new Precomputed();
        magic = new Magic();
        zobrist = new Zobrist();

        WhiteToMove = b.WhiteToMove;
        moveHistory = new ArrayList<Move>();

        PieceCount = b.PieceCount;
        PieceCountNoPawnsNoKings = 0;

        AllPieces = new Bitboard(b.AllPieces.board);
        EnemyDiagSliders = new Bitboard(b.EnemyDiagSliders.board);
        EnemyOrthoSliders = new Bitboard(b.EnemyOrthoSliders.board);
        FriendlyDiagSliders = new Bitboard(b.FriendlyDiagSliders.board);
        FriendlyOrthoSliders = new Bitboard(b.FriendlyOrthoSliders.board);
        mg = new MoveGenerator(this);

        CurrentGameState = new GameState(b.CurrentGameState.capturedPieceType,
                b.CurrentGameState.epFile,
                b.CurrentGameState.castlingRights,
                b.CurrentGameState.fiftyMoveCount,
                b.CurrentGameState.zobristKey);

        gsHistory = new Stack<GameState>();
        gsHistory.push(CurrentGameState);
        moves = mg.GenerateMoves();
    }

    public Board(String fen) {
        new Board().LoadFromFen(fen);
    }

    // TODO: Separate this to a game result class
    // TODO: and also handle stalemate and threefold repetitions
    public boolean IsCheckMate() {
        // moves = mg.GenerateMoves();
        // if (moves.size() == 0) {
        // if (InCheck()) {
        // return true;
        // }
        // }
        return false;
    }

    // Is current player in check ?
    public boolean InCheck() {
        if (hasCachedCheckValue) {
            return cachedCheckValue;
        }
        cachedCheckValue = CalculateCheckState();
        hasCachedCheckValue = true;
        return cachedCheckValue;
    }

    public void UpdateMoves() {
        moves = mg.GenerateMoves();
    }

    public boolean CalculateCheckState() {
        int square = KingSquareIndex[FriendlyIndex];
        Bitboard blockers = AllPieces;

        if (EnemyOrthoSliders.board != 0) {
            Bitboard rookAttacks = Magic.GetRookAttacks(square, blockers.board);
            if ((rookAttacks.board & EnemyOrthoSliders.board) != 0) {
                return true;
            }
        }
        if (EnemyDiagSliders.board != 0) {
            Bitboard bishopAttacks = Magic.GetBishopAttacks(square, blockers.board);
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

        Bitboard pawnAttackmask = WhiteToMove ? Precomputed.pawnAttacks[square][WhiteIndex]
                : Precomputed.pawnAttacks[square][BlackIndex];
        if ((pawnAttackmask.board & enemyPawns.board) != 0) {
            return true;
        }

        return false;
    }

    public void MakeMove(Move move) {
        MakeMove(move, true);
    }

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
        Colours[FriendlyIndex].UnsetBit(source);
        Colours[FriendlyIndex].SetBit(dest);

        Square[dest] = movedPiece;
        Square[source] = Piece.None;

        if (capturedType != Piece.None) {
            System.out.println("Captured piece type: " + Piece.ToChar(capturedPiece));
            int captureSquare = dest;

            if (isEnPassant) {
                // If en passant, the captured pawn is one rank above
                // the destination square
                captureSquare = dest + (WhiteToMove ? -8 : 8);
                Square[captureSquare] = Piece.None;
                Pieces[Piece.Pawn].UnsetBit(captureSquare);
                Colours[OpponentIndex].UnsetBit(captureSquare);
            }

            if (capturedType != Piece.Pawn) {
                PieceCountNoPawnsNoKings--;
            }

            // Remove captured piece from bitboards
            if (movedType != capturedType) {
                Pieces[capturedType].UnsetBit(dest);
            }

            Colours[WhiteToMove ? BlackIndex : WhiteIndex].UnsetBit(dest);
            newZobristKey ^= Zobrist.pieces[capturedPiece][captureSquare];
        }

        if (movedType == Piece.King) {
            KingSquareIndex[FriendlyIndex] = dest;
            // Mask out side to move's castling rights
            newCastlingRights &= WhiteToMove ? 0b1100 : 0b0011;

            if (flag == Move.Castle) {
                int rookP = Piece.MkPiece(Piece.Rook, MoveColour);
                boolean kingside = dest == BoardHelper.g1 || dest == BoardHelper.g8;
                int rookFrom = kingside ? dest + 1 : dest - 2;
                int rookTo = kingside ? dest - 1 : dest + 1;

                Pieces[Piece.Rook].SetBit(rookTo);
                Pieces[Piece.Rook].UnsetBit(rookFrom);
                Colours[FriendlyIndex].ToggleBits(rookFrom, rookTo);
                Square[rookFrom] = Piece.None;
                Square[rookTo] = rookP;

                newZobristKey ^= Zobrist.pieces[rookP][rookFrom];
                newZobristKey ^= Zobrist.pieces[rookP][rookTo];
            }
        }

        if (isPromotion) {
            PieceCountNoPawnsNoKings++;
            int promotionType = move.GetPromotionPiece();
            int promotionPiece = Piece.MkPiece(promotionType, MoveColour);

            Pieces[Piece.Pawn].UnsetBit(dest);
            Pieces[promotionType].SetBit(dest);
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

        int newFiftyMoveCount = CurrentGameState.fiftyMoveCount + 1;

        CastlingRights = newCastlingRights;

        AllPieces.board = Colours[WhiteIndex].board | Colours[BlackIndex].board;
        UpdateSliders();

        GameState newState = new GameState(capturedType,
                new_epFile,
                newCastlingRights,
                newFiftyMoveCount,
                newZobristKey);

        System.out.println("Pushing new gamestate to stack:");
        System.out.println("capturedType:" + capturedType);

        if (record) {
            PlyCount++;
            moveHistory.add(move);
        }
        gsHistory.push(newState);
        CurrentGameState = newState;

        hasCachedCheckValue = false;
        UpdateMoves();
    }

    public void UnmakeMove(Move move) {
        UnmakeMove(move, true);
    }

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

        System.out.println("Undoing capture ? " + captureUndo);

        int movedPiece = promoUndo ? Piece.MkPiece(Piece.Pawn, MoveColour) : Square[dest];
        int movedType = Piece.GetType(movedPiece);
        int capturedType = CurrentGameState.capturedPieceType;

        if (promoUndo) {
            int promotedPiece = Square[dest];
            int promotedType = Piece.GetType(promotedPiece);
            PieceCountNoPawnsNoKings--;
            Pieces[promotedType].UnsetBit(dest);
            Pieces[Piece.Pawn].UnsetBit(dest);
        }

        Pieces[movedType].UnsetBit(dest);
        Pieces[movedType].SetBit(source);
        Colours[FriendlyIndex].UnsetBit(dest);
        Colours[FriendlyIndex].SetBit(source);

        Square[dest] = Piece.None;
        Square[source] = movedPiece;

        // Restore captured piece
        if (captureUndo) {
            System.out.println("Captured piece type: " + Piece.ToChar(capturedType));
            int captureSquare = dest;
            int capturedPiece = Piece.MkPiece(capturedType, OpponentColour);

            if (epUndo) {
                captureSquare = dest + (colourUndone ? -8 : 8);
            }
            if (capturedType != Piece.Pawn) {
                PieceCountNoPawnsNoKings++;
            }

            Pieces[capturedType].SetBit(captureSquare);
            Colours[OpponentIndex].SetBit(captureSquare);
            Square[captureSquare] = capturedPiece;
        }

        if (movedType == Piece.King) {
            KingSquareIndex[FriendlyIndex] = source;

            if (flag == Move.Castle) {
                boolean kingside = dest == BoardHelper.g1 || dest == BoardHelper.g8;
                int before = kingside ? dest + 1 : dest - 2;
                int after = kingside ? dest - 1 : dest + 1;

                Pieces[Piece.Rook].SetBit(before);
                Pieces[Piece.Rook].UnsetBit(after);
                Colours[FriendlyIndex].SetBit(before);
                Colours[FriendlyIndex].UnsetBit(after);
                Square[before] = Piece.Rook | MoveColour;
                Square[after] = Piece.None;
            }
        }
        AllPieces.board = Colours[WhiteIndex].board | Colours[BlackIndex].board;
        UpdateSliders();

        gsHistory.pop();
        CurrentGameState = gsHistory.peek();
        CastlingRights = CurrentGameState.castlingRights;
        if (record) {
            PlyCount--;
        }
        hasCachedCheckValue = false;

        moveHistory.remove(move);
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
        moveHistory = new ArrayList<Move>();
        PlyCount = 0;
        PieceCount = 0;
        PieceCountNoPawnsNoKings = 0;
        Pieces = new Bitboard[7];
        for (int i = 0; i < 7; i++) {
            Pieces[i] = new Bitboard();
        }
        Colours = new Bitboard[2];
        Colours[0] = new Bitboard();
        Colours[1] = new Bitboard();
        AllPieces = new Bitboard(0);
        EnemyDiagSliders = new Bitboard();
        EnemyOrthoSliders = new Bitboard();
        FriendlyDiagSliders = new Bitboard();
        FriendlyOrthoSliders = new Bitboard();
        mg = new MoveGenerator(this);

        CurrentGameState = new GameState(Piece.None, 0, 0b1111, 0, 0);
        gsHistory = new Stack<GameState>();
        gsHistory.push(CurrentGameState);
    }

    public void LoadStartPos() {
        LoadFromFen(startingFen);
    }

    // Load game state and position from standard FEN string
    public void LoadFromFen(String fen) {
        String[] fields = fen.split(" ");

        int file = 0;
        int rank = 7; // FEN strings have rank 8 at the leftmost extremity

        for (char c : fields[0].toCharArray()) {
            if (c == '/') {
                file = 0;
                rank--;
            } else {
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c);
                } else {
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

        int epf = (fields[3].contains("-") ? 0 : Integer.parseInt(fields[3]));

        FullMoveCount = Integer.parseInt(fields[5]);

        CurrentGameState = new GameState(epf, CastlingRights);
        gsHistory.push(CurrentGameState);
        CurrentGameState.zobristKey = Zobrist.CalculateZobristKey(this);
        UpdateMoves();
    }

    public String toFenString() {
        StringBuilder res = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {
            int empties = 0;
            for (int file = 0; file < 8; file++) {
                int piece = Square[rank * 8 + file];
                if (piece == Piece.None) {
                    empties++;
                } else {
                    if (empties > 0) {
                        res.append(empties);
                        empties = 0;
                    }
                    res.append(Piece.ToChar(piece));
                }
            }
            if (empties > 0) {
                res.append(empties);
            }
            if (rank > 0) {
                res.append('/');
            }
        }

        res.append(' ').append(WhiteToMove ? 'w' : 'b');

        res.append(' ');
        boolean whiteKC = (CurrentGameState.castlingRights & 1) == 1;
        boolean whiteQC = (CurrentGameState.castlingRights >> 1 & 1) == 1;
        boolean blackKC = (CurrentGameState.castlingRights >> 2 & 1) == 1;
        boolean blackQC = (CurrentGameState.castlingRights >> 3 & 1) == 1;
        res.append(whiteKC ? "K" : "").append(whiteQC ? "Q" : "")
                .append(blackKC ? "k" : "").append(blackQC ? "q" : "");

        res.append(" - "); // TODO: Fix en passant

        res.append(" " + CurrentGameState.fiftyMoveCount);
        res.append(" " + ((PlyCount / 2) + 1));
        return res.toString();
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        String[] data = new String[8];

        data[0] = (InCheck() ? "CHECK" : "");
        data[1] = "Captured Piece= " + Piece.ToChar(CurrentGameState.capturedPieceType);
        data[2] = "En passant file= " + CurrentGameState.epFile;
        data[3] = "Castling rights= " + Integer.toBinaryString(CastlingRights);
        data[4] = "Zobrist hash= " + CurrentGameState.zobristKey;

        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int p = Square[rank * 8 + file];
                res.append(Piece.ToChar(p)).append(" ");
            }

            if (rank < data.length) {
                res.append(" | ").append(data[7 - rank]);
            }

            res.append("\n"); // " " + (rank + 1) + "\n";
        }
        // res += "ABCDEFGH";
        return res.toString();
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

    public String SquareArrayToString() {
        String res = "[";

        for (int i : Square) {
            if (i != 0) {
                res += Piece.ToChar(i) + ", ";
            }
        }

        res = res.substring(0, res.length() - 2);
        res += "]";

        return res;
    }
}
