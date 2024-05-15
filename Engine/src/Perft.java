public class Perft {
    // https://www.chessprogramming.org/Perft
    // Given a starting position and gamestate information
    // Construct a tree of strictly legal nodes up to a certain depth
    // Count all leaf nodes and compare against Known values to assert
    // correctness and test performance of the movegen

    public static long _Perft(Board b, int depth) {
        if (depth == 0) {
            return 1;
        }

        long nodes = 0;
        for(Move move: b.mg.GenerateMoves()) {
            b.MakeMove(move);
            //System.out.println(b.toString());
            nodes += _Perft(b, depth - 1);
            b.UnmakeMove(move);
        }

        return nodes;
    }

    public static long PerftDivide(Board b, int depth) {
        System.out.println("Running Perft Divide at depth " + depth);
        long totalNodes = 0;

        for (Move move: b.mg.GenerateMoves()) {
            b.MakeMove(move);
            long nodes = _Perft(b, depth - 1);
            b.UnmakeMove(move);
            System.out.println(move.toString() + ": " + nodes);
            totalNodes += nodes;
        }

        System.out.println("TOTAL: " + totalNodes);
        return totalNodes;
    }
}
