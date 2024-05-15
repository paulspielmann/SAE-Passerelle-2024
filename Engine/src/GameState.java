public class GameState {
    public int capturedPieceType;
    public int epFile;
    public int castlingRights;
    public int fiftyMoveCount;
    public long zobristKey;

    public static int ClearWhiteKingsideMask = 0b1110;
    public static int ClearWhiteQueensideMask = 0b1101;
    public static int ClearBlackKingsideMask = 0b1011;
    public static int ClearBlackQueensideMask = 0b0111;

    public GameState() {
        capturedPieceType = Piece.None;
        epFile = 0;
        castlingRights = 0b1111;
        fiftyMoveCount = 0;
        zobristKey = 0;
    }

    public GameState(int p, int e, int c, int f, long z) {
        capturedPieceType = p;
        epFile = e;
        castlingRights = c;
        fiftyMoveCount = f;
        zobristKey = z;
    }

    public boolean CanCastleQS(boolean white) {
        int mask = white ? 1 : 3;
        return (castlingRights & mask) != 0;
    }

    public boolean CanCastleKS(boolean white) {
        int mask = white ? 0 : 2;
        return (castlingRights & mask) != 0;
    }

}
