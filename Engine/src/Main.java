public class Main {
    public static void main(String[] args) {
        Board b = new Board();
        b.Init();
        b.LoadFromFen(Board.startingFen);

        System.out.println(b.toString());

        int e2 = BoardHelper.Index(4, 1);
        int e4 = BoardHelper.Index(4, 3);
        int e7 = BoardHelper.Index(4, 6);
        int e5 = BoardHelper.Index(4, 4);

        Move e2e4 = new Move(e2, e4);
        Move e7e5 = new Move(e7, e5);

        b.MakeMove(e2e4); System.out.println(b.toString());
        b.MakeMove(e7e5); System.out.println(b.toString());

        System.out.println(b.toStringSquareIndex());
    }
}
