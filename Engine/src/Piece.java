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
    public static int GetColour(int piece) { return piece & colourMask >> 3; }

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

    public static char ToChar(int piece) {
        switch (piece) {
        case 1: return 'p';
        case 2: return 'N';
        case 3: return 'B';
        case 4: return 'R';
        case 5: return 'Q';
        case 6: return 'K';

        case 9: return 'p';
        case 10: return 'n';
        case 11: return 'b';
        case 12: return 'r';
        case 13: return 'q';
        case 14: return 'k';
        default: return ' ';
        }
    }

    public static int FromChar(char c) {
        switch (c) {
        case 'P': return 1;
        case 'N': return 2;
        case 'B': return 3;
        case 'R': return 4;
        case 'Q': return 5;
        case 'K': return 6;

        case 'p': return 9;
        case 'n': return 10;
        case 'b': return 11;
        case 'r': return 12;
        case 'q': return 13;
        case 'k': return 14;
        default : return 0;
        }
    }
}
