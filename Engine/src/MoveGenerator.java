import java.util.List;

public class MoveGenerator {
    // This is the biggest amount of moves possible at any given position
    public static int maxMoves = 218;

    Precomputed precomputedMoveData;
    public Board board;
    public List<Move> moves;
    public boolean generateQuietMoves;

    public int index;
    public int ennemyIndex;
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
    public long ennemyPieces;
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
        ennemyIndex = 1 - index;

        ennemyPieces = board.Colours[ennemyIndex].board;
        friendlyPieces = board.Colours[index].board;
        allPieces = ennemyPieces | friendlyPieces;
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

        Bitboard pawns = board.Pieces[p];

        long promotionMask = white ? BitboardUtil.Rank8 : BitboardUtil.Rank1;
        long doublePushMask = white ? BitboardUtil.Rank4 : BitboardUtil.Rank5;

        Bitboard push =new Bitboard(BitboardUtil.Shift(pawns, offset) & emptySquares & checkRayMask);
        Bitboard promos = new Bitboard(push.board & promotionMask & checkRayMask);
        Bitboard pushNoPromo = new Bitboard(push.board & ~promotionMask);

        Bitboard captures = new Bitboard(BitboardUtil.PawnAttacks(pawns, white) & ennemyPieces);
        Bitboard capturePromos = new Bitboard(captures.board & promotionMask & checkRayMask);
        captures.board &= checkRayMask & ~promotionMask;

        if (generateQuietMoves) {
            while (pushNoPromo.board != 0) {
                int dest = BitboardUtil.PopLSB(pushNoPromo);
                int source = dest - offset;

            }
        }
    }

    public void UpdateSlideAttacks(Bitboard b, boolean ortho) {
        long blockers = board.
    }

    public void GenerateSlidingAttacks() {
        opponentSlidingAttacks = 0;


    }
}
