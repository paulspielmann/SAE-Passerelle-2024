import java.util.List;

public class MoveGenerator {
    // This is the biggest amount of moves possible at any given position
    public static int maxMoves = 218;

    Precomputed precomputedMoveData;
    public Board board;
    public List<Move> moves;
    public boolean generateQuietMoves;

    public int index;
    public int enemyIndex;
    public int friendlyKingSquare;
    public boolean whiteToMove;
    public boolean inCheck;
    public boolean inDoubleCheck;

    public long checkRayMask;
    public long pinRays;
    public long notPinRays;
    public long opponentAttackMap;
    public long opponentAttackMapNoPawns;
    public long opponentPawnAttacks;
    public long opponentSlidingAttacks;

    // We might need to make these Bitboards
    public long emptySquares;
    public long friendlyPieces;
    public long enemyPieces;
    public long allPieces;

    // Captures only -> 1s in place of ennemy pieces
    // Otherwise -> 1s everywhere
    public long moveTypeMask;

    public MoveGenerator(Board b) {
        precomputedMoveData = b.precomputed;
        board = b;
    }

    // Call this everytime we generate moves from a given position to reset state
    public void init() {
        whiteToMove = board.WhiteToMove;
        index = whiteToMove ? Board.WhiteIndex : Board.BlackIndex;
        enemyIndex = 1 - index;
        friendlyKingSquare = board.KingSquareIndex[board.FriendlyIndex];
        enemyPieces = board.Colours[enemyIndex].board;
        friendlyPieces = board.Colours[index].board;
        allPieces = enemyPieces | friendlyPieces;
        emptySquares = ~allPieces;
    }

    public void GenerateMoves() {
        GenerateMoves(true);
    }

    public void GenerateMoves(boolean quietMoves) {
        init();

    }

    public void GenerateKingMoves() {

    }

    public void GenerateLinearSliders() {

    }

    public void GenerateDiagonalSliders() {

    }

    public void GenerateKnightMoves() {

    }

    public void GeneratePawnMoves(boolean white) {
        int dir = white ? 1 : -1;
        int offset = dir * 8;
        int p = Piece.MkPiece(Piece.Pawn, white ? Piece.White : Piece.Black);

        Bitboard pawns = new Bitboard(board.Pieces[p].board);

        long promotionMask = white ? BitboardUtil.Rank8 : BitboardUtil.Rank1;
        long doublePushMask = white ? BitboardUtil.Rank4 : BitboardUtil.Rank5;

        Bitboard push = new Bitboard(BitboardUtil.Shift(pawns.board, offset)
                                     & emptySquares & checkRayMask);

        Bitboard promos = new Bitboard(push.board & promotionMask & checkRayMask);
        Bitboard pushNoPromo = new Bitboard(push.board & ~promotionMask);


        long edgeMaskA = whiteToMove ? BitboardUtil.NotFileA : BitboardUtil.NotFileH;
        long edgeMaskB = whiteToMove ? BitboardUtil.NotFileH : BitboardUtil.NotFileA;

        Bitboard capturesA = new Bitboard(
                                BitboardUtil.Shift(pawns.board & edgeMaskA, dir * 7)
                                & enemyPieces);

        Bitboard capturesB = new Bitboard(
                                BitboardUtil.Shift(pawns.board & edgeMaskB, dir * 9)
                                & enemyPieces);
        Bitboard capturePromosA = new Bitboard(capturesA.board & promotionMask & checkRayMask);
        Bitboard capturePromosB = new Bitboard(capturesB.board & promotionMask & checkRayMask);
        capturesA.board &= checkRayMask & ~promotionMask;
        capturesB.board &= checkRayMask & ~promotionMask;

        if (generateQuietMoves) {

            // Single pushes
            while (pushNoPromo.board != 0) {
                int dest = BitboardUtil.PopLSB(pushNoPromo);
                int source = dest - offset;

                if (!IsPinned(source) ||
                Precomputed.alignMask[source][friendlyKingSquare] ==
                Precomputed.alignMask[dest][friendlyKingSquare]) {
                    moves.add(new Move(source, dest));
                }
            }

            // Double
            Bitboard doublePush = new Bitboard( BitboardUtil.Shift(push, offset)
                                                & emptySquares
                                                & doublePushMask
                                                & checkRayMask);

            while (doublePush.board != 0) {
                int dest = BitboardUtil.PopLSB(doublePush);
                int source = dest - offset * 2;

                if (!IsPinned(source) ||
                Precomputed.alignMask[source][friendlyKingSquare] ==
                Precomputed.alignMask[dest][friendlyKingSquare]) {
                    moves.add(new Move(source, dest));
                }
            }
        }

        while (capturesA.board != 0) {
            int dest = BitboardUtil.PopLSB(capturesA);
            int source = dest - dir * 7;

            if (!IsPinned(source) ||
            Precomputed.alignMask[source][friendlyKingSquare] ==
            Precomputed.alignMask[dest][friendlyKingSquare]) {
                moves.add(new Move(source, dest));
            }
        }

        while (capturesB.board != 0) {
            int dest = BitboardUtil.PopLSB(capturesB);
            int source = dest - dir * 9;

            if (!IsPinned(source) ||
            Precomputed.alignMask[source][friendlyKingSquare] ==
            Precomputed.alignMask[dest][friendlyKingSquare]) {
                moves.add(new Move(source, dest));
            }
        }

        while (promos.board != 0) {
            int dest = BitboardUtil.PopLSB(promos);
            int source = dest - offset;
            if (!IsPinned(source)) {
                GeneratePromotions(source, dest, moves);
            }
        }

        while (capturePromosA.board != 0) {
            int dest = BitboardUtil.PopLSB(capturesPromosA);
            int source = dest - dir * 7;

            if (!IsPinned(source) ||
            Precomputed.alignMask[source][friendlyKingSquare] ==
            Precomputed.alignMask[dest][friendlyKingSquare]) {
                                GeneratePromotions(source, dest, moves);
            }
        }

        while (capturePromosB.board != 0) {
            int dest = BitboardUtil.PopLSB(capturePromosB);
            int source = dest - dir * 9;

            if (!IsPinned(source) ||
            Precomputed.alignMask[source][friendlyKingSquare] ==
            Precomputed.alignMask[dest][friendlyKingSquare]) {
                GeneratePromotions(source, dest, moves);
            }
        }

        // TODO: EN PASSANT XDDDD
    }

    public void GeneratePromotions(int source, int dest, List<Move> moves) {
        moves.add(new Move(source, dest, Move.PromoteQueen));
        moves.add(new Move(source, dest, Move.PromoteKnight));
    }

    public boolean IsPinned(int square) {
        return ((pinRays >>> square) & 1) != 0;
    }

    public void UpdateSlideAttacks(Bitboard b, boolean ortho) {
        int fKingIndex = board.KingSquareIndex[board.FriendlyIndex];
        long blockers = board.AllPieces.board & ~(1L << fKingIndex);

        while (b.board != 0) {
            int source = BitboardUtil.PopLSB(b);
            Bitboard moves = Magic.GetSliderAttacks(source, blockers, ortho);

            opponentSlidingAttacks |= moves.board;
        }
    }

    public void GenerateSlidingAttacks() {
        opponentSlidingAttacks = 0;

        UpdateSlideAttacks(board.EnemyOrthoSliders, true);
        UpdateSlideAttacks(board.EnemyDiagSliders, false);
    }

    public void CalculateAttackData() {
        GenerateSlidingAttacks();
        int startDir = 0;
        int endDir = 8;

        int friendlyColour = board.FriendlyIndex * 8;
        int enemyColour = Math.abs(board.FriendlyIndex - 8);
        int enemyIndex = Math.abs(board.FriendlyIndex - 1);
        int eKingIndex = board.KingSquareIndex[enemyIndex];

        int eQueen = Piece.MkPiece(Piece.Queen, enemyColour);
        int eRook = Piece.MkPiece(Piece.Rook, enemyColour);
        int eBishop = Piece.MkPiece(Piece.Bishop, enemyColour);

        if (board.Pieces[eQueen].board != 0) {
            startDir = (board.Pieces[eRook].board != 0) ? 0 : 4;
            endDir = (board.Pieces[eBishop].board != 0) ? 8 : 4;
        }

        for (int dir = startDir; dir < endDir; dir++) {
            boolean diag = dir > 3;
            Bitboard slider = diag ? board.EnemyDiagSliders : board.EnemyOrthoSliders;

            if ((Precomputed.dirRayMask[dir][friendlyKingSquare].board) == 0) {
                continue;
            }

            int n = Precomputed.edgeDistance[friendlyKingSquare][dir];
            int dirOffset = Precomputed.dirs[dir];
            boolean isFriendlyAlongRay = false;
            long rayMask = 0;

            for (int i = 0; i < n; i++) {
                int sq = friendlyKingSquare + dirOffset * (i + 1);
                rayMask |= 1L << sq;
                int piece = board.Square[sq];

                if (piece != Piece.None) {
                    if (Piece.IsColour(piece, friendlyColour)) {
                        // Maybe pin ?
                        if (!isFriendlyAlongRay) {
                            isFriendlyAlongRay = true;
                        }
                        // We meet second piece along the ray, so not a pin
                        else {
                            break;
                        }
                    }

                    // Enemy piece
                    else {
                        int pieceType = Piece.GetType(piece);

                        if (diag && Piece.IsDiagonalSlider(pieceType) ||
                            !diag && Piece.IsLinearSlider(pieceType)) {

                            // Friendly piece is blocking check -> pin
                            if (isFriendlyAlongRay) {
                                pinRays |= rayMask;
                            }
                            // No blockers -> check
                            else {
                                checkRayMask |= rayMask;
                                inDoubleCheck = inCheck;
                                inCheck = true;
                            }
                            break;
                        }
                        else {
                            // Enemy piece can't move in current dir
                            // thus its blocking checks/pins
                            break;
                        }
                    }
                }
            }
            if (inDoubleCheck) {
                // If we're in double check, only the king can move
                break;
            }
        }

        notPinRays = ~pinRays;

        long eKnightAttacks = 0;
        Bitboard knights = board.Pieces[Piece.MkPiece(Piece.Knight, enemyColour)];
        Bitboard fKing = board.Pieces[Piece.MkPiece(Piece.King, friendlyColour)];

        while (knights.board != 0) {
            int knight = BitboardUtil.PopLSB(knights);
            Bitboard knightAttacks = Precomputed.knightAttacks[knight];
            eKnightAttacks |= knightAttacks.board;

            if ((knightAttacks.board & fKing.board) != 0) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMask |= 1L << knight;
            }
        }

        opponentPawnAttacks = 0;
        Bitboard opponentPawns = board.Pieces[Piece.MkPiece(Piece.Pawn, enemyColour)];
        opponentPawnAttacks = BitboardUtil.PawnAttacks(opponentPawns, !whiteToMove);
        if (opponentPawns.Contains(friendlyKingSquare)) {
            inDoubleCheck = inCheck;
            inCheck = true;
            Bitboard origin = Precomputed.pawnAttacks[friendlyKingSquare][enemyColour / 8];
            long pawnCheckMap = opponentPawns.board & origin.board;
            checkRayMask |= pawnCheckMap;
        }

        opponentAttackMapNoPawns =
            opponentSlidingAttacks
            | eKnightAttacks
            | Precomputed.kingAttacks[eKingIndex].board;

        opponentAttackMap = opponentAttackMap | opponentPawnAttacks;
        if (!inCheck) {
            checkRayMask = Long.MAX_VALUE;
        }
    }
}
