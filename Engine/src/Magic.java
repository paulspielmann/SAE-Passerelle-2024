import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

// Magic bitboards :
// Precompute all sliders (rook, bishop, queen) moves
// for any config of origin square and blocker pieces
public class Magic {
    // Legal masks
    public static Bitboard[] RookMask;
    public static Bitboard[] BishopMask;

    public static HashMap<Long, Bitboard>[] RookAttacks;
    public static HashMap<Long, Bitboard>[] BishopAttacks;

    public static long[] RookMagics;
    public static int[] RookShifts;
    public static long[] BishopMagics;
    public static int[] BishopShifts;

    public static Bitboard GetSliderAttacks(int sq, long blockers, boolean ortho) {
        return ortho ? GetRookAttacks(sq, blockers) : GetBishopAttacks(sq, blockers);
    }

    public static Bitboard GetRookAttacks(int sq, long blockers) {
        Long key = ((blockers & RookMask[sq].board) * RookMagics[sq]) >> RookShifts[sq];
        return RookAttacks[sq].get(key);
    }

    public static Bitboard GetBishopAttacks(int sq, long blockers) {
        long key = ((blockers & BishopMask[sq].board) * BishopMagics[sq]) >> BishopShifts[sq];
        return BishopAttacks[sq].get(key);
    }

    public Magic() {
        RookMask = new Bitboard[64];
        BishopMask = new Bitboard[64];
        RookAttacks = new HashMap[64];
        BishopAttacks = new HashMap[64];

        RookMagics = new long[64];
        RookShifts = new int[64];
        BishopMagics = new long[64];
        BishopShifts = new int[64];

        for (int i = 0; i < 64; i++) {
            RookMask[i] = MovementMask(i, true);
            BishopMask[i] = MovementMask(i, false);

            RookMagics[i] = ComputeMagic(i, true);
            BishopMagics[i] = ComputeMagic(i, false);

            RookShifts[i] = ComputeShift(i, true);
            BishopShifts[i] = ComputeShift(i, false);

            RookAttacks[i] = CreateTable(i, true, RookMagics[i], RookShifts[i]);
            BishopAttacks[i] = CreateTable(i, false, BishopMagics[i], BishopShifts[i]);
        }
    }

    // TODO : optimize this
    public static long ComputeMagic(int sq, boolean ortho) {
        long magic = (long) (Math.random() * Long.MAX_VALUE);
        HashMap<Long, Boolean> usedIndices = new HashMap<>();

        while (true) {
            boolean unique = true;

            for (Bitboard pattern: CreateBlockerBoards(MovementMask(sq, ortho))) {
                long index = (pattern.board * magic) >>> (64 - RookShifts[sq]);
                System.out.println("Computing magic index: " + index);
                if (usedIndices.containsKey(index)) {
                    unique = false;
                    break;
                }
                usedIndices.put(index, true);
            }
            if (unique) {
                break;
            } else {
                magic = (long) (Math.random() * Long.MAX_VALUE);
            }
        }
        return magic;
    }

    public static int ComputeShift(int sq, boolean ortho) {
        return 64 - Long.bitCount((ortho ? RookMask[sq].board : BishopMask[sq].board));
    }

    public static HashMap<Long, Bitboard> CreateTable(int sq, boolean ortho, long magic, int shift) {
        int numBits = 64 - shift;
        int lookupSize = 1 << numBits;

        HashMap<Long, Bitboard> table = new HashMap<Long, Bitboard>(lookupSize);

        Bitboard movementMask = MovementMask(sq, ortho);
        Bitboard[] blockers = CreateBlockerBoards(movementMask);

        for (Bitboard pattern: blockers) {
            long index = (pattern.board * magic) >> shift;
            Bitboard moves = LegalMoves(sq, pattern, ortho);
            table.put(index, moves);
        }
        return table;
    }

    public static Bitboard LegalMoves(int source, Bitboard blockers, boolean ortho) {
        Bitboard res = new Bitboard();
        Coord[] dirs = ortho ? BoardHelper.RookDirs : BoardHelper.BishopDirs;
        Coord s = new Coord(source);

        for (Coord dir: dirs) {
            for (int dist = 1; dist < 8; dist++) {
                Coord c = Coord.Add(s, Coord.Scale(dir, dist));

                if (c.isValid()) {
                    res.SetBit(c.Square());
                    if (blockers.Contains(c.Square())) {
                        break;
                    }
                }
                else { break; }
            }
        }
        return res;
    }

    public static Bitboard MovementMask(int square, boolean ortho) {
        Bitboard mask = new Bitboard();
        Coord[] dirs = ortho ? BoardHelper.RookDirs : BoardHelper.BishopDirs;
        Coord s = new Coord(square);

        for (Coord dir: dirs) {
            for (int dist = 1; dist < 8; dist++) {
                Coord c = Coord.Add(s, Coord.Scale(dir, dist));
                Coord next = Coord.Add(s, Coord.Scale(dir, dist + 1));

                if (next.isValid()) {
                    mask.SetBit(square);
                }
                else { break; }
            }
        }
        return mask;
    }

    public static Bitboard[] CreateBlockerBoards(Bitboard movementMask) {
        ArrayList<Integer> indices = new ArrayList<>();

        for (int i = 0; i < 64; i++) {
            if (((movementMask.board >> i) & 1) == 1) {
                indices.add(i);
            }
        }

        // There is 2^n bitboards, one for each arrangemnt of pieces
        int numBoards = 1 << indices.size();
        Bitboard[] boards = new Bitboard[numBoards];

        for (int index = 0; index < numBoards; index++) {
            for (int bitIndex = 0; bitIndex < indices.size(); bitIndex++) {
                int bit = (index >> bitIndex) & 1;
                boards[index] = new Bitboard();
                boards[index].board |= (long) bit << indices.get(bitIndex);
            }
        }

        return boards;
    }
}
