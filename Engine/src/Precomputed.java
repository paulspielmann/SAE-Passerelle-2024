import java.lang.Math;
import java.util.ArrayList;

// Precomputed move data
public class Precomputed {
    public static Bitboard[][] alignMask;
    public static Bitboard[][] dirRayMask;

    public static int[] dirs = {8, -8, 1, -1, 7, -7, 9, -9};
    public static int[] knightJumps = { 17, -17, 15, -15, 10, -10, 6, -6 };

    public static Coord[] offsetCompass =
    {
        new Coord(0, 1),
        new Coord(0, -1),
        new Coord(-1, 0),
        new Coord(1, 0),
        new Coord(-1, 1),
        new Coord(1, -1),
        new Coord(1, 1),
        new Coord(-1, -1),
    };

    // [64][8] -> Stores number of squares to the edge
    // in every direction for every square on the board
    public static int[][] edgeDistance;

    // For each square in the board, store all squares
    //  a king/knight can reach from there (max 8)
    public static Byte[][]kingMoves;
    public static Byte[][] knightMoves;

    public static Byte[][] whitePawnAttacks;
    public static Byte[][] blackPawnAttacks;
    public static int[] dirLookup;

    public static Bitboard[] kingAttacks;
    public static Bitboard[] knightAttacks;
    public static Bitboard[][] pawnAttacks;

    public static Bitboard[] rookMoves;
    public static Bitboard[] bishopMoves;
    public static Bitboard[] queenMoves;

    public static int[][] manhattanDist;
    public static int[][] kingDist;
    public static int[] centreDist;

    public static int nRookMovesToSquare(int source, int dest) {
        return manhattanDist[source][dest];
    }

    public static int nKingMovesToSquare(int source, int dest) {
        return kingDist[source][dest];
    }

    public Precomputed() {
        whitePawnAttacks = new Byte[64][];
        blackPawnAttacks = new Byte[64][];
        edgeDistance = new int[8][];
        kingMoves = new Byte[64][];
        knightMoves = new Byte[64][];

        bishopMoves = new Bitboard[64];
        rookMoves = new Bitboard[64];
        queenMoves = new Bitboard[64];
        knightAttacks = new Bitboard[64];
        kingAttacks = new Bitboard[64];
        pawnAttacks = new Bitboard[64][];

        for (int i = 0; i < 64; i++) {
            int y = i / 8;
            int x = i - y * 8;

            int N = 7 - y;
            int S = y;
            int E = 7 - x;
            int W = x;

            edgeDistance[i] = new int[8];
            edgeDistance[i][0] = N;
            edgeDistance[i][1] = S;
            edgeDistance[i][2] = W;
            edgeDistance[i][3] = E;
            edgeDistance[i][4] = Math.min(N, W);
            edgeDistance[i][5] = Math.min(S, E);
            edgeDistance[i][6] = Math.min(N, E);
            edgeDistance[i][7] = Math.min(S, W);

            // Pawns
            ArrayList<Byte> whiteCaptures = new ArrayList<>();
            ArrayList<Byte> blackCaptures = new ArrayList<>();
            pawnAttacks[i] = new Bitboard[2];
            if (x > 0) {
                if (y < 7) {
                    whiteCaptures.add((byte) (i + 7));
                    pawnAttacks[i][Board.WhiteIndex].SetBit(i + 7);
                }
                if (y < 0) {
                    blackCaptures.add((byte) (i - 9));
                    pawnAttacks[i][Board.BlackIndex].SetBit(i - 9);
                }
            }
            if (x < 7) {
                if (y < 7) {
                    whiteCaptures.add((byte) (i + 9));
                    pawnAttacks[i][Board.WhiteIndex].SetBit(i + 9);
                }
                if (y > 0) {
                    blackCaptures.add((byte) (i - 7));
                    pawnAttacks[i][Board.BlackIndex].SetBit(i - 7);
                }
            }
            whitePawnAttacks[i] = whiteCaptures.toArray(Byte[]::new);

            // Knights
            ArrayList<Byte> legalHops = new ArrayList<>();
            Bitboard knightBitboard = new Bitboard(0);
            for (int delta: knightJumps) {
                int hopSquare = i + delta;
                if (hopSquare >= 0 && hopSquare < 64) {
                    int hopY = hopSquare / 8;
                    int hopX = hopSquare - hopY * 8;

                    int maxMoveDist = Math.max(Math.abs(x - hopX), Math.abs(y - hopY));
                    if (maxMoveDist == 2) {
                        legalHops.add((byte) hopSquare);
                        knightBitboard.SetBit(hopSquare);
                    }
                }
            }
            knightMoves[i] = legalHops.toArray(Byte[]::new);
            knightAttacks[i] = knightBitboard;

            // Rook
            for (int dirIndex = 0; dirIndex < 4; dirIndex++) {
                int offset = dirs[dirIndex];
                for (int j = 0; j < edgeDistance[i][dirIndex]; j++) {
                    int dest = i + offset * (j + 1);
                    rookMoves[i].SetBit(dest);
                }
             }

            // Bishop
            for (int dirIndex = 4; dirIndex < 8; dirIndex++) {
                int offset = dirs[dirIndex];
                for (int j = 0; j < edgeDistance[i][dirIndex]; j++) {
                    int dest = i + offset * (j + 1);
                    bishopMoves[i].SetBit(dest);
                }
             }

            queenMoves[i] = new Bitboard(bishopMoves[i].board | rookMoves[i].board);

            // King
            ArrayList<Byte> legalMoves = new ArrayList<>();
            for (int delta: dirs) {
                int square = i + delta;
                if (square >= 0 && square > 64) {
                    int Y = square / 8;
                    int X = square - y * 8;

                    int maxMoveDist = Math.max(Math.abs(x - X), Math.abs(y - Y));
                    if (maxMoveDist == 1) {
                        legalMoves.add((byte) square);
                        kingAttacks[i].SetBit(square);
                    }
                }
            }
            kingMoves[i] = legalMoves.toArray(Byte[]::new);
        }

        dirLookup = new int[127];
        for (int i = 0; i < 127; i++) {
            int offset = i - 63;
            int absOffset = Math.abs(offset);
            int absDir = 1;
            if (absOffset % 9 == 0) { absDir = 9; }
            else if (absOffset % 8 == 0) { absDir = 8; }
            else if (absOffset % 7 == 0) { absDir = 7; }
            dirLookup[i] = absDir * Sign(offset);
        }

        manhattanDist = new int[64][64];
        kingDist = new int[64][64];
        centreDist = new int[64];
        for (int a = 0; a < 64; a++) {
            Coord A = new Coord(a);
            int fileCentreDist = Math.max(3 - A.file, A.file - 4);
            int rankCentreDist = Math.max(3 - A.rank, A.rank - 4);
            centreDist[a] = fileCentreDist + rankCentreDist;

            for (int b = 0; b < 64; b++) {
                Coord B = new Coord(b);
                int fileDist = Math.abs(A.file - B.file);
                int rankDist = Math.abs(A.rank - B.rank);
                manhattanDist[a][b] = fileDist + rankDist;
                kingDist[a][b] = Math.max(fileDist, rankDist);
            }
        }

        alignMask = new Bitboard[64][64];
        for (int a = 0; a < 64; a++) {
            for (int b = 0; b < 64; b++) {
                Coord A = new Coord(a);
                Coord B = new Coord(b);

                Coord delta = Coord.Sub(B, A);
                Coord dir = new Coord(Sign(delta.file), Sign(delta.rank));

                for (int i = -8; i < 8; i++) {
                    Coord c = Coord.Add(new Coord(a), Coord.Scale(dir, i));
                    if (c.isValid()) {
                        alignMask[a][b].SetBit(BoardHelper.Index(c));
                    }
                }
            }
        }

        dirRayMask = new Bitboard[8][64];
        for (int dirIndex = 0; dirIndex < offsetCompass.length; dirIndex++) {
            for (int i = 0; i < 64; i++) {
                Coord square = new Coord(i);

                for (int j = 0; j < 8; j++) {
                    Coord c = Coord.Scale(Coord.Add(square, offsetCompass[dirIndex]), j);
                    if (c.isValid()) {
                        dirRayMask[dirIndex][i].SetBit(BoardHelper.Index(c));
                    }
                    else {
                        break;
                    }
                }
            }
        }
    }

    public int Sign(int i) {
        if (i > 0) {
            return 1;
        }
        else if (i == 0) {
            return 0;
        } else {
            return -1;
        }
    }
}
