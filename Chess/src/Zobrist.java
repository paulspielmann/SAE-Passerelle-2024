import java.util.Random;

public class Zobrist {
    // Helper class to calculate zobrist hashes which is a 64 bit long
    // which reprsents the current game state (see GameState class)
    // Useful to quickly detect position already evaluated and avoid duplicate work

    public static long[][] pieces = new long[Piece.IndexMax + 1][64];
    // Each player can have 4 different castling rights state : none, ks, qs, and both
    // So there are at any point a maximum of 16 possible states
    public static long[] castlingRights = new long[16];
    public static long[] epFile = new long[9]; // 0 -> no ep
    public static long sideToMove;

    public Zobrist() {
        Random rd = new Random(75179395);

        for (int i = 0; i < 64; i++) {
            for (int piece: Piece.PieceIndices) {
                pieces[piece][i] = rd.nextLong();
            }
        }

        for (int i = 0; i < castlingRights.length; i++) {
            castlingRights[i] = rd.nextLong();
        }

        for (int i = 0; i < epFile.length; i++) {
            epFile[i] = i == 0 ? 0 : rd.nextLong();
        }

        sideToMove = rd.nextLong();
    }

    public static long CalculateZobristKey(Board b) {
        long key = 0;

        for (int i = 0; i < 64; i++) {
            int piece = b.Square[i];

            if (Piece.GetType(piece) != Piece.None) {
                key ^= pieces[piece][i];
            }
        }
        key ^= epFile[b.CurrentGameState.epFile];

        if (b.MoveColour == Piece.Black) {
            key ^= sideToMove;
        }

        key ^= castlingRights[b.CurrentGameState.castlingRights];
        return key;
    }
}
