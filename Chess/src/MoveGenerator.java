import java.util.ArrayList;

public class MoveGenerator {
    // This is the biggest amount of moves possible at any given position
    public static int maxMoves = 218;

    public Board board;
    public boolean generateQuietMoves;

    public int index;
    public int enemyIndex;
    public int friendlyKingSquare;
    public boolean whiteToMove;
    public boolean inCheck;
    public boolean inDoubleCheck;

    // checkRayMask -> bitboard where 1s are squares where a piece
    // aims at the friendly king. When not in check it's set to all 1's
    public long checkRayMask;
    public long pinRays; // Rays where a slider piece pins
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
        board = b;
    }

    // Call this everytime we generate moves from a given position to reset state
    public void init() {
        inCheck = false;
        inDoubleCheck = false;
        checkRayMask = 0;
        pinRays = 0;

        whiteToMove = board.WhiteToMove;
        index = whiteToMove ? Board.WhiteIndex : Board.BlackIndex;
        enemyIndex = 1 - index;
        friendlyKingSquare = board.KingSquareIndex[index];
        enemyPieces = board.Colours[enemyIndex].board;
        friendlyPieces = board.Colours[index].board;
        allPieces = enemyPieces | friendlyPieces;
        emptySquares = ~allPieces;
        CalculateAttackData();
    }

    public ArrayList<Move> GenerateMoves() {
        return GenerateMoves(true);
    }

    public ArrayList<Move> GenerateMoves(boolean quietMoves) {
        init();
        generateQuietMoves = quietMoves;
        moveTypeMask = quietMoves ? -1L : enemyPieces;
        ArrayList<Move> moves = new ArrayList<Move>();

        GenerateKingMoves(moves);

        if (!inDoubleCheck) {
            GenerateSliders(moves);
            GeneratePawnMoves(moves, whiteToMove);
            GenerateKnightMoves(moves);
        }

        return moves;
    }

    public void GenerateKingMoves(ArrayList<Move> moves) {
        // Limit movement to empty, not attacked squares
        long legalMask = ~(opponentAttackMap | friendlyPieces);
        Bitboard kingMoves = new Bitboard(Precomputed.kingAttacks[friendlyKingSquare].board);
        kingMoves.board &= legalMask;

        while (kingMoves.board != 0) {
            int dest = BitboardUtil.PopLSB(kingMoves);
            moves.add(new Move(friendlyKingSquare, dest));
        }

        // Castling
        if (!inCheck && generateQuietMoves) {
            long blockers = opponentAttackMap | board.AllPieces.board;

            // Kingside
            if (board.CurrentGameState.CanCastleKS(whiteToMove)) {
                long castleMask = whiteToMove
                    ? BitboardUtil.WhiteKingSideMask
                    : BitboardUtil.BlackKingSideMask;

                if ((castleMask & blockers) == 0) {
                    int dest = whiteToMove ? BoardHelper.g1 : BoardHelper.g8;
                    moves.add(new Move(friendlyKingSquare, dest, Move.Castle));
                }
            }

            // Queenside
            if (board.CurrentGameState.CanCastleQS(whiteToMove)) {
                long castleMask = whiteToMove
                    ? BitboardUtil.WhiteQueenSideMask
                    : BitboardUtil.BlackQueenSideMask;

                if ((castleMask & blockers) == 0) {
                    int dest = whiteToMove ? BoardHelper.c1 : BoardHelper.c8;
                    moves.add(new Move(friendlyKingSquare, dest, Move.Castle));
                }
            }
        }
    }

    public void GenerateSliders(ArrayList<Move> moves) {
        // This mask will limit movement to squares which are empty/enemy
        // and if in check, along the ray of the check
        long moveMask = emptySquares | enemyPieces;
        moveMask &= checkRayMask & moveTypeMask;

        Bitboard orthos = board.FriendlyOrthoSliders;
        Bitboard diags = board.FriendlyDiagSliders;

        if (inCheck) {
            orthos.board &= ~pinRays;
            diags.board &= ~pinRays;
        }

        while (orthos.board != 0) {
            int source = BitboardUtil.PopLSB(orthos);
            Bitboard moveSquares = Magic.GetRookAttacks(source, allPieces);
            moveSquares.board &= moveMask;

            // If piece is pinned it may only move along the pinray
            if (IsPinned(source)) {
                moveSquares.board &=
                    Precomputed.alignMask[source][friendlyKingSquare].board;
            }

            while (moveSquares.board != 0) {
                int dest = BitboardUtil.PopLSB(moveSquares);
                moves.add(new Move(source, dest));
            }
        }

        while (diags.board != 0) {
            int source = BitboardUtil.PopLSB(diags);
            Bitboard moveSquares = Magic.GetBishopAttacks(source, allPieces);
            moveSquares.board &= moveMask;

            if (IsPinned(source)) { // Same logic as before
                moveSquares.board &=
                    Precomputed.alignMask[source][friendlyKingSquare].board;
            }

            while (moveSquares.board != 0) {
                int dest = BitboardUtil.PopLSB(moveSquares);
                moves.add(new Move(source, dest));
            }
        }
    }

    public void GenerateKnightMoves(ArrayList<Move> moves) {
        Bitboard knights = new Bitboard(board.Pieces[Piece.Knight].board
                                        & board.Colours[index].board
                                        & notPinRays);
        long moveMask = (emptySquares | enemyPieces) & checkRayMask & moveTypeMask;

        while (knights.board != 0) {
            int knightSquare = BitboardUtil.PopLSB(knights);
            Bitboard movePoss = new Bitboard(
                                Precomputed.knightAttacks[knightSquare].board
                                & moveMask);

            while (movePoss.board != 0) {
                int dest = BitboardUtil.PopLSB(movePoss);
                moves.add(new Move(knightSquare, dest));
            }
        }
    }

    public void GeneratePawnMoves(ArrayList<Move> moves, boolean white) {
        int dir = white ? 1 : -1;
        int offset = dir * 8;
        long p = board.Pieces[Piece.Pawn].board & board.Colours[index].board;

        Bitboard pawns = new Bitboard(p);

        long promotionMask = white ? BitboardUtil.Rank8 : BitboardUtil.Rank1;
        long doublePushMask = white ? BitboardUtil.Rank4 : BitboardUtil.Rank5;

        long pushMask = BitboardUtil.Shift(p, offset) & emptySquares;
        Bitboard push = new Bitboard(pushMask & checkRayMask);

        Bitboard promos = new Bitboard(push.board & promotionMask & checkRayMask);
        Bitboard pushNoPromo = new Bitboard(push.board & ~promotionMask);

        long edgeMaskA = whiteToMove ? BitboardUtil.NotFileA : BitboardUtil.NotFileH;
        long edgeMaskB = whiteToMove ? BitboardUtil.NotFileH : BitboardUtil.NotFileA;

        Bitboard capturesA = new Bitboard(
                                BitboardUtil.Shift(p & edgeMaskA, dir * 7)
                                & enemyPieces);

        Bitboard capturesB = new Bitboard(
                                BitboardUtil.Shift(p & edgeMaskB, dir * 9)
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
            Bitboard doublePush = new Bitboard( BitboardUtil.Shift(pushMask, offset)
                                                & emptySquares
                                                & doublePushMask
                                                & checkRayMask);

            while (doublePush.board != 0) {
                int dest = BitboardUtil.PopLSB(doublePush);
                int source = dest - offset * 2;

                if (!IsPinned(source) ||
                    Precomputed.alignMask[source][friendlyKingSquare] ==
                    Precomputed.alignMask[dest][friendlyKingSquare]) {
                    moves.add(new Move(source, dest, Move.PawnDoubleMove));
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
            int dest = BitboardUtil.PopLSB(capturePromosA);
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

        // En passant
        if (board.CurrentGameState.epFile > 0) {
            int epFile = board.CurrentGameState.epFile - 1;
            int epRank = board.WhiteToMove ? 5 : 2;
            int dest = epRank * 8 + epFile;
            int captureSquare = dest - offset;

            Bitboard crm = new Bitboard(checkRayMask);
            if (crm.Contains(captureSquare)) {
                Bitboard canEP = new Bitboard(
                                 pawns.board
                                 & Precomputed.pawnAttacks[dest][enemyIndex].board);

                while (canEP.board != 0) {
                    int source = BitboardUtil.PopLSB(canEP);

                    if (!IsPinned(source)
                        || Precomputed.alignMask[source][friendlyKingSquare].board
                        == Precomputed.alignMask[dest][friendlyKingSquare].board) {
                        if (!InCheckAfterEP(source, dest, captureSquare)) {
                            moves.add(new Move(source, dest, Move.EnPassant));
                        }
                    }
                }
            }
        }
    }

    public void GeneratePromotions(int source, int dest, ArrayList<Move> moves) {
        moves.add(new Move(source, dest, Move.PromoteQueen));
        moves.add(new Move(source, dest, Move.PromoteRook));
        moves.add(new Move(source, dest, Move.PromoteBishop));
        moves.add(new Move(source, dest, Move.PromoteKnight));
    }

    public boolean IsPinned(int square) {
        return ((pinRays >>> square) & 1) != 0;
    }

    public boolean InCheckAfterEP(int source, int dest, int capture) {
        Bitboard orthos = board.EnemyOrthoSliders;
        if (orthos.board != 0) {
            long blockers = (allPieces ^ (1L << capture | 1L << source | 1L << dest));
            Bitboard rookAttacks = Magic.GetRookAttacks(friendlyKingSquare, blockers);
            return (rookAttacks.board & orthos.board) != 0;
        }
        return false;
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

        Bitboard o = new Bitboard(board.EnemyOrthoSliders.board);
        Bitboard d = new Bitboard(board.EnemyDiagSliders.board);

        UpdateSlideAttacks(o, true);
        UpdateSlideAttacks(d, false);
    }

    public void CalculateAttackData() {
        GenerateSlidingAttacks();

        inCheck = false;
        inDoubleCheck = false;

        int startDir = 0;
        int endDir = 8;

        int friendlyColour = board.FriendlyIndex * 8;
        int enemyIndex = Math.abs(board.FriendlyIndex - 1);
        int eKingIndex = board.KingSquareIndex[enemyIndex];

        long eQueen = board.Pieces[Piece.Queen].board & board.Colours[enemyIndex].board;
        long eRook = board.Pieces[Piece.Rook].board & board.Colours[enemyIndex].board;
        long eBishop = board.Pieces[Piece.Bishop].board & board.Colours[enemyIndex].board;

        // If there are no queens + no rook and/or bishop we can optimize
        if (eQueen == 0) {
            startDir = (eRook != 0) ? 0 : 4;
            endDir = (eBishop != 0) ? 8 : 4;
        }

        for (int dir = startDir; dir < endDir; dir++) {
            boolean diag = dir >= 4;
            Bitboard slider = diag ? board.EnemyDiagSliders : board.EnemyOrthoSliders;

            if ((Precomputed.dirRayMask[dir][friendlyKingSquare].board & slider.board)
                == 0) {
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
                            // thus it's blocking checks/pins
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
        Bitboard knights = new Bitboard(board.Pieces[Piece.Knight].board
                                        & board.Colours[enemyIndex].board);
        Bitboard fKing = new Bitboard(board.Pieces[Piece.King].board
                                      & board.Colours[index].board);

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
        Bitboard opponentPawns = new Bitboard(board.Pieces[Piece.Pawn].board
                                              & board.Colours[enemyIndex].board);
        //Bitboard pawnAttacks = Precomputed.pawnAttacks[friendlyKingSquare][index];
        Bitboard pawnAttacks = new Bitboard(BitboardUtil.
                                            PawnAttacks(opponentPawns, !whiteToMove));

        if (pawnAttacks.Contains(friendlyKingSquare)) {
            inDoubleCheck = inCheck;
            inCheck = true;
            Bitboard origin = Precomputed.pawnAttacks[friendlyKingSquare][enemyIndex];
            long pawnCheckMap = opponentPawns.board & origin.board;
            checkRayMask |= pawnCheckMap;
        }

        opponentAttackMapNoPawns =
            opponentSlidingAttacks
            | eKnightAttacks
            | Precomputed.kingAttacks[eKingIndex].board;

        opponentAttackMap = opponentAttackMapNoPawns | pawnAttacks.board;

        if (!inCheck) {
            // This should be maximum long value as the MSB is sign bit
            checkRayMask = -1L;//Long.MAX_VALUE;
        }
    }
}
