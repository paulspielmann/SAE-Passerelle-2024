public class Perft {
    // https://www.chessprogramming.org/Perft
    // Given a starting position and gamestate information
    // Construct a tree of strictly legal nodes up to a certain depth
    // Count all leaf nodes and compare against Known values to assert
    // correctness and test performance of the movegen
    public static long captures;
    public static long ep;
    public static long castles;
    public static long promotions;
    public static long checks;
    public static long checkmates;

    public static long RunPerft(Board b, int depth) {
        if (depth == 0) {
            return 1;
        }

        long nodes = 0;

        for(Move move: b.mg.GenerateMoves()) {
            if (b.Square[move.dest] != Piece.None) {
                captures++;
            }

            if (move.IsEnPassant()) {
                ep++;
            }

            if (move.IsPromotion()) {
                promotions++;
            }

            if (move.flag == Move.Castle) {
                castles++;
            }

            b.MakeMove(move);
            // System.out.println("Board after make move: " + move.toString()
            //                   + "\n" + b.toString());

            if (b.InCheck()) {
                checks++;
            }

            nodes += RunPerft(b, depth - 1);
            b.UnmakeMove(move);
            // System.out.println("Board after unmake move: " + move.toString()
            //                   + "\n" + b.toString());

        }

        return nodes;
    }

    public static long PerftDivide(Board b, int depth) {
        System.out.println("Running Perft divide at depth " + depth);

        long totalNodes = 0;
        captures = 0;
        ep = 0;
        castles = 0;
        promotions = 0;
        checks = 0;

        for (Move move: b.mg.GenerateMoves()) {
            if (b.Square[move.dest] != Piece.None) {
                captures++;
            }

            if (move.IsEnPassant()) {
                ep++;
            }

            if (move.IsPromotion()) {
                promotions++;
            }

            if (move.flag == Move.Castle) {
                castles++;
            }

            b.MakeMove(move);

            // System.out.println("Board after make move: " + move.toString()
            //                   + "\n" + b.toString());


            if (b.InCheck()) {
                checks++;
            }

            long nodes = RunPerft(b, depth - 1);

            b.UnmakeMove(move);

            // System.out.println("Board after unmake move: " + move.toString()
            //                   + "\n" + b.toString());


            System.out.println(move.toString() + ": " + nodes);
            totalNodes += nodes;
        }

        System.out.println("TOTAL: " + totalNodes
                           + ", captures: " + captures
                           + ", ep: " + ep
                           + ", castles: " + castles
                           + ", promotions: " + promotions
                           + ", checks: " + checks);
        return totalNodes;
    }
}
