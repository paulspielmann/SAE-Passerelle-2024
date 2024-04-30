// Magic bitboards :
// Precompute all sliders (rook, bishop, queen) moves
// for any config of origin square and blocker pieces
public class Magic {
    // Legal masks
    public static long[] RookMask;
    public static long[] BishopMask;

    public static long[][] RookAttacks;
    public static long[][] BishopAttacks;


    public static long GetSliderAttcks(int sq, long blockers, boolean ortho) {
        return ortho ? GetRookAttcks(sq, blockers) : GetBishopAttcks(sq, blockers);
    }

    public static long GetRoookAttcks(int sq, int blockers) {
        long key = ((blockers & RookMask[sq]) * RookMagics[sq]) >> RookShifts[sq];
    }

    public static long GetBishopAttcks(int sq, int blockers) {
        long key = ((blockers & BishopMask[sq]) * BishopMagics[sq]) >> BishopShifts[sq];
    }

    public Magic() {
        RookMask = new long[64];
        BishopMask = new long[64];

        for (int i = 0; i < 64; i++) {

        }
    }
}
