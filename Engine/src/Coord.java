// Coord Struct which can represent both an actual valid coord on the board
// as well as a direction offset (for sliding pieces)
public class Coord {
    public int file;
    public int rank;

    public Coord(int file, int rank) {
        this.file = file;
        this.rank = rank;
    }

    public Coord(int square) {
        file = BoardHelper.FileIndex(square);
        rank = BoardHelper.RankIndex(square);
    }

    public int Square() { return BoardHelper.Index(this); }

    public boolean isValid() {
        return rank < 8 && rank >= 0 && file < 8 && file >= 0;
    }

    public static Coord Add(Coord a, Coord b) {
        return new Coord(a.file + b.file, a.rank + b.rank);
    }
    public static Coord Sub(Coord a, Coord b) {
        return new Coord(a.file - b.file, a.rank - b.rank);
    }

    public static Coord Scale(Coord c, int s) {
        return new Coord(c.file * s, c.rank * s);
    }

    public String toString() {
        if (isValid()) {
            return "File: " + BoardHelper.FileLetterFromIndex(file) + ";Rank: " + rank;
        }
        else {
            return "Coord used as a direction offset, x: " + rank + "y: " + file;
        }

    }
}
