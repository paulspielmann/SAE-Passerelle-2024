public class BoardHelper {
    public static String files = "abcdefgh";
    public static String ranks = "12345678";

    public static final int a1 = 0;
    public static final int b1 = 1;
    public static final int c1 = 2;
    public static final int d1 = 3;
    public static final int e1 = 4;
    public static final int f1 = 5;
    public static final int g1 = 6;
    public static final int h1 = 7;

    public static final int a8 = 56;
    public static final int b8 = 57;
    public static final int c8 = 58;
    public static final int d8 = 59;
    public static final int e8 = 60;
    public static final int f8 = 61;
    public static final int g8 = 62;
    public static final int h8 = 63;

    public static final Coord[] RookDirs =
    {
        new Coord(1, 0),
        new Coord(-1, 0),
        new Coord(0, 1),
        new Coord(0, -1),
    };

    public static final Coord[] BishopDirs =
    {
        new Coord(1, 1),
        new Coord(-1, 1),
        new Coord(1, -1),
        new Coord(-1, -1),
    };

    // Standard notation and not 0-indexed
    public static int RealRankIndex(int square) { return RankIndex(square) + 1; }
    // The following functions return respectively, the rank
    // and file indexes (0 to 7) of a square represented as an int
    public static int RankIndex(int square) { return square >>> 3; }
    public static int FileIndex(int square) { return square & 7; }

    public static int Index(int file, int rank) { return rank * 8 + file; }
    public static int Index(Coord c) { return Index(c.file, c.rank); }

    public static String SquareString(int index) {
        return "" + FileLetterFromIndex(index) + RealRankIndex(index);
    }

    public static char FileLetterFromIndex(int index) {
        switch (index % 8) {
        case 0:
            return 'a';
        case 1:
            return 'b';
        case 2:
            return 'c';
        case 3:
            return 'd';
        case 4:
            return 'e';
        case 5:
            return 'f';
        case 6:
            return 'g';
        case 7:
            return 'h';
        default:
            return '?';
        }
    }
}
