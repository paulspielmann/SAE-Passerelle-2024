public class BoardHelper {
    public String files = "abcdefgh";
    public String ranks = "12345678";

    public final int a1 = 0;
    public final int b1 = 1;
    public final int c1 = 2;
    public final int d1 = 3;
    public final int e1 = 4;
    public final int f1 = 5;
    public final int g1 = 6;
    public final int h1 = 7;

    public final int a8 = 56;
    public final int b8 = 57;
    public final int c8 = 58;
    public final int d8 = 59;
    public final int e8 = 60;
    public final int f8 = 61;
    public final int g8 = 62;
    public final int h8 = 63;

    // The following functions return respectively, the rank
    // and file indexes (0 to 7) of a square represented as an int
    public static int RankIndex(int square) { return square >> 3; }
    public static int FileIndex(int square) { return square & 7; }

    public static int Index(int file, int rank) { return rank * 8 + file; }
    public static int Index(Coord coord) { return 0; } // TODO

}
