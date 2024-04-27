public class Main {
    public static void main(String[] args) {
        Board b = new Board();

        b.LoadFromFen(Board.startingFen);
        System.out.println(b.toString());
    }
}
