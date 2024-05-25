public class Main {
    public static String TestFen1 =
        "7k/3n1b2/8/3R3P/8/3BB3/2K5/N6r w - - 0 1";

    public static String TestFen2 =
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 0";

    public static String TestFen3 =
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0";
    //"8/2p5/3p4/KP5r/1R3p1k/4P3/6P1/8 b - - 0 0";

    public static String TestFen4 =
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    //"r3k2r/Pppp1ppp/1b3nbN/nPP5/BB2P3/q4N2/Pp1P2PP/R2Q1RK1 b kq - 0 1";

    public static String TestFen5 =
        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8  ";

    public static void main(String[] args) {
        Board b = new Board();
        b.Init();
        //b.LoadStartPos();
        b.LoadFromFen(TestFen4);

        boolean print;

        if (args.length > 1) {
            print = true;
        }
        else {
            print = false;
        }

        Perft.PerftDivide(b, Integer.parseInt(args[0]), print);
    }
}
