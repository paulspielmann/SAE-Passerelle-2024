public class Move {
    // A square index (0 to 63) can fit in just 6 bits. This allows us to
    // encode a move in a 16 bit short : 6 bits of source and destination
    // squares, with 4 remaining bits for additional info (like a check)
    public short value;

    // 4 bit flags
    public static final int None = 0b0000;
    public static final int EnPassant = 0b0001;
    public static final int Castle = 0b0010;
    public static final int PawnDoubleMove = 0b0011;

    public static final int PromoteKnight = 0b0100;
    public static final int PromoteBishop = 0b0101;
    public static final int PromoteRook = 0b0110;
    public static final int PromoteQueen = 0b0111;

    // Masks
    public static final int SourceMask = 0b0000000000111111;
    public static final int DestMask   = 0b0000111111000000;
    public static final int FlagMask   = 0b1111000000000000;

    public Move(short val) {
        value = val;
    }

    public Move(int source, int dest, int flag) {
        value = (short) (source | dest << 6 | flag << 12);
    }

    public int source = value & SourceMask;
    public int dest = value & DestMask;
    public int flag = value >> 12;

    public boolean IsPromotion = flag >= PromoteKnight;

    public int GetPromotionPiece() {
        switch (flag) {
        case PromoteKnight:
            return Piece.Knight;
        case PromoteBishop:
            return Piece.Bishop;
        case PromoteRook:
            return Piece.Rook;
        case PromoteQueen:
            return Piece.Queen;
        default:
            return Piece.None;
        }
    }
}
