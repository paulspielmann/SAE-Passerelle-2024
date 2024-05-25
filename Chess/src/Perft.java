import java.util.ArrayList;

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

    public static long RunPerft(Board b, int depth, boolean print) {
        if (depth == 0) {
            return 1;
        }

        long nodes = 0;

        ArrayList<Move> moves = b.mg.GenerateMoves();
        for (Move move : moves) {
            if (b.Square[move.dest] != Piece.None) {
                captures++;
            }

            if (move.IsEnPassant()) {
                ep++;
            }

            if (move.flag == Move.Castle) {
                castles++;
            }

            b.MakeMove(move);

            if (print) {
                System.out.println("After MakeMove(): " + move.toString() + "\n"
                                   + b.toString());

                // BitboardUtil.print2(b.Colours[0], b.Colours[1],
                //                     "White pieces", "Black pieces");

                // for (int i = 1; i < b.Pieces.length / 2 + 1; i++) {
                //     BitboardUtil.print2(b.Pieces[i], b.Pieces[b.Pieces.length - i],
                //                         Piece.ToChar(i) + " bitboard",
                //                         Piece.ToChar(b.Pieces.length - i) + " bitboard");
                // }
            }

            if (move.IsPromotion()) {
                promotions++;
            }

            if (b.InCheck()) {
                checks++;
            }

            if (b.IsCheckMate()) {
                checkmates++;
            }

            nodes += RunPerft(b, depth - 1, print);
            b.UnmakeMove(move);

            if (print) {
                System.out.println("After UNMakeMove(): " + move.toString() + "\n"
                                   + b.toString());

                // BitboardUtil.print2(b.Colours[0], b.Colours[1],
                //                     "White pieces", "Black pieces");

                // for (int i = 1; i < b.Pieces.length / 2 + 1; i++) {
                //     BitboardUtil.print2(b.Pieces[i], b.Pieces[b.Pieces.length - i],
                //                         Piece.ToChar(i) + " bitboard",
                //                         Piece.ToChar(b.Pieces.length - i) + " bitboard");

                // }
            }

        }
        return nodes;
    }

    public static long PerftDivide(Board b, int depth, boolean print) {
        long totalNodes = 0;
        captures = 0;
        ep = 0;
        castles = 0;
        promotions = 0;
        checks = 0;

        System.out.println("Initial pos:\n" + b.toString());

        for (Move move : b.mg.GenerateMoves()) {
            if (b.Square[move.dest] != Piece.None) {
                captures++;
            }

            b.MakeMove(move);

            if (print) {
                System.out.println("After MakeMove(): " + move.toString() + "\n"
                        + b.toString());

                // BitboardUtil.print2(b.Colours[0], b.Colours[1],
                //                     "White pieces", "Black pieces");

                // for (int i = 1; i < b.Pieces.length / 2 + 1; i++) {
                //     BitboardUtil.print2(b.Pieces[i], b.Pieces[b.Pieces.length - i],
                //                         Piece.ToChar(i) + " bitboard",
                //                         Piece.ToChar(b.Pieces.length - i) + " bitboard");
                // }
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

            if (b.InCheck()) {
                checks++;
            }

            long nodes = RunPerft(b, depth - 1, print);

            b.UnmakeMove(move);

            if (print) {
                System.out.println("After UNMakeMove(): " + move.toString() + "\n"
                                   + b.toString());

                // BitboardUtil.print2(b.Colours[0], b.Colours[1],
                //                     "White pieces", "Black pieces");

                // for (int i = 1; i < b.Pieces.length / 2 + 1; i++) {
                //     BitboardUtil.print2(b.Pieces[i], b.Pieces[b.Pieces.length - i],
                //                         Piece.ToChar(i) + " bitboard",
                //                         Piece.ToChar(b.Pieces.length - i) + " bitboard");
                // }
            }

            //System.out.println("After " + depth + " iterations:\n" + b.toString());


            System.out.println(move.toString() + ": " + nodes);
            totalNodes += nodes;
        }

        System.out.println("TOTAL: " + totalNodes
                           + ", captures: " + captures
                           + ", ep: " + ep
                           + ", castles: " + castles
                           + ", promotions: " + promotions
                           + ", checks: " + checks
                           + ", checkmates: " + checkmates);
        return totalNodes;
    }
}
