import java.util.ArrayList;

public class Main {
    public static String TestFen1 = "7k/3n1b2/8/3R3P/8/3BB3/2K5/N6r w - - 0 1";

    public static int[] StartPosDepth6 = { 1, 20, 400, 8902, 197281, 4865609 };

    public static void main(String[] args) {
        Board b = new Board();
        b.Init();
        b.LoadFromFen(Board.startingFen);
        Perft.PerftDivide(b, 5);
    }
}
