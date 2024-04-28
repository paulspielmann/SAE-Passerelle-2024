import java.util.List;

public class MoveGenerator {
    public static int maxMoves = 256;

    public Board board;
    public List<Move> moves;

    public int index;
    public int ennemyIndex;
    public boolean whiteToMove;
    public boolean inCheck;
    public boolean inDoubleCheck;

    // We might need to make these Bitboards
    public long emptySquares;
    public long friendlyPieces;
    public long ennemyPieces;
    public long allPieces;

    public MoveGenerator(Board b) {
        board = b;

    }

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
        // Get the index for current colour pawns
        int p = Piece.MkPiece(Piece.Pawn, white ? Piece.White : Piece.Black);

        Bitboard pawns = board.Pieces[p];

        long promotion = white ? BitboardUtil.Rank8 : BitboardUtil.Rank1;
        long push = BitboardUtil.Shift(pawns, offset) & empties;
    }
}
