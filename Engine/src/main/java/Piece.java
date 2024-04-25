public class Piece {
    public static int None = 0;
    public static int Pawn = 1;
    public static int Knight = 2;
    public static int Bishop = 3;
    public static int Rook = 4;
    public static int Queen = 5;
    public static int King = 6;

    public static int White = 0;
    public static int Black = 8;

    public static int WhitePawn = Pawn | White; // 1
    public static int WhiteKnight = Knight | White; // 2
    public static int WhiteBishop = Bishop | White; // 3
    public static int WhiteRook = Rook | White; // 4
    public static int WhiteQueen = Queen | White; // 5
    public static int WhiteKing = King | White; // 6

    public static int BlackPawn = Pawn | Black; // 9
    public static int BlackKnight = Knight | Black; // 10
    public static int BlackBishop = Bishop | Black; // 11
    public static int BlackRook = Rook | Black; // 12
    public static int BlackQueen = Queen | Black; // 13
    public static int BlackKing = King | Black; // 14

    public static int IndexMax = BlackKing; // = 14

    public static int typeMask = 0b0111;
    public static int colourMask = 0b1000;

    public static int MkPiece(int type, int colour) { return type | colour; }

    public static int GetType(int piece) { return piece & typeMask; }

    public boolean IsColour(int piece, int colour) {
        return (piece & colourMask) == colour && piece != 0;
    }

    public static boolean IsLinearSlider(int piece) {
        return GetType(piece) == Queen || GetType(piece) == Rook;
    }

    public static boolean IsDiagonalSlider(int piece) {
        return GetType(piece) == Bishop || GetType(piece) == Queen;
    }

    public static boolean IsSlider(int piece) {
        return IsDiagonalSlider(piece) || IsLinearSlider(piece);
    }
}
