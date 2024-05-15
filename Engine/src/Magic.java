import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.*;

// Magic bitboards :
// Precompute all sliders (rook, bishop, queen) moves
// for any config of origin square and blocker pieces
public class Magic {
    // Legal masks on empty board
    public static Bitboard[] RookMask;
    public static Bitboard[] BishopMask;

    public static HashMap<Long, Bitboard>[] RookAttacks;
    public static HashMap<Long, Bitboard>[] BishopAttacks;

    public static long[] RookMagics;
    public static int[] RookShifts;
    public static long[] BishopMagics;
    public static int[] BishopShifts;

    public static final long[] KNOWN_ROOK_MAGICS =
    {
        0xA8002C000108020L, 0x6C00049B0002001L, 0x100200010090040L, 0x2480041000800801L,
        0x280028004000800L, 0x900410008040022L, 0x280020001001080L, 0x2880002041000080L,
        0xA000800080400034L, 0x4808020004000L, 0x2290802004801000L, 0x411000D00100020L,
        0x402800800040080L, 0xB000401004208L, 0x2409000100040200L, 0x1002100004082L,
        0x4000808004000200L, 0x8001000400000080L, 0x280080100080080L, 0x800080204000L,
        0x4000200800800400L, 0x8001000400000200L, 0x100100200010080L, 0x800040080020080L,
        0x100020010080080L, 0x200010010080080L, 0x8004000800800200L, 0x4008008000800200L,
        0x280800800800100L, 0x2808000800800800L, 0x800080800080800L, 0x800080200040000L,
        0x2808000800800800L, 0x1000800800800100L, 0x4000800800800200L, 0x800080200040000L,
        0x1000800800800200L, 0x800080080080800L, 0x800080200080000L, 0x400080200080000L,
        0x800080080080800L, 0x800080200080000L, 0x400080200080000L, 0x800080200080000L,
        0x800080200080000L, 0x800080080080800L, 0x800080200080000L, 0x800080200080000L,
        0x800080080080800L, 0x800080200080000L, 0x800080200080000L, 0x800080200080000L,
        0x800080080080800L, 0x800080200080000L, 0x800080200080000L, 0x800080080080800L,
        0x800080200080000L, 0x800080200080000L, 0x800080080080800L, 0x800080200080000L,
        0x800080200080000L, 0x800080080080800L, 0x800080200080000L, 0x800080200080000L
    };

    public static final long[] KNOWN_BISHOP_MAGICS =
    {
        0x89A1121896040240L, 0x2004844802002010L, 0x2068080051921000L, 0x62880A0220200808L,
        0x0004042004000000L, 0x0100822020200011L, 0xC00444222012000AL, 0x0028808801216001L,
        0x0400492088408100L, 0x0201D2008C000400L, 0x4000082004801010L, 0x20004821880A00L,
        0x8200000108201080L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L,
        0x800080204820800L, 0x800080204820800L, 0x800080204820800L, 0x800080204820800L
    };

    public static Bitboard GetSliderAttacks(int sq, long blockers, boolean ortho) {
        return ortho ? GetRookAttacks(sq, blockers) : GetBishopAttacks(sq, blockers);
    }

    public static Bitboard GetRookAttacks(int sq, long blockers) {
        Long key = ((blockers & RookMask[sq].board) *
                    RookMagics[sq]) >>> RookShifts[sq];
        return RookAttacks[sq].get(key);
    }

    public static Bitboard GetBishopAttacks(int sq, long blockers) {
        long key = ((blockers & BishopMask[sq].board) *
                    BishopMagics[sq]) >>> BishopShifts[sq];
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

        Random random = new Random();

        for (int i = 0; i < 64; i++) {
            RookMask[i] = MovementMask(i, true);
            BishopMask[i] = MovementMask(i, false);

            RookShifts[i] = ComputeShift(i, true);
            BishopShifts[i] = ComputeShift(i, false);

            RookMagics[i] = KNOWN_ROOK_MAGICS[i];
            //RookMagics[i] = ComputeMagic(i, true, random);
            //BishopMagics[i] = ComputeMagic(i, false, random);
            BishopMagics[i] = KNOWN_BISHOP_MAGICS[i];

            RookAttacks[i] = CreateTable(i, true, RookMagics[i], RookShifts[i]);
            BishopAttacks[i] = CreateTable(i, false, BishopMagics[i], BishopShifts[i]);
        }
    }

    private static long ComputeMagic(int sq, boolean ortho, Random random) {
        long magic;

        while (true) {
            magic = random.nextLong() & random.nextLong() & random.nextLong();
            boolean unique = true;
            HashMap<Long, Boolean> usedIndices = new HashMap<>();

            for (Bitboard pattern: CreateBlockerBoards(MovementMask(sq, ortho))) {
                long shift = ortho ? RookShifts[sq] : BishopShifts[sq];
                long index = (pattern.board * magic) >>> shift;

                if (usedIndices.containsKey(index)) {
                    unique = false;
                    break;
                }
                usedIndices.put(index, true);
            }
            if (unique) {
                break;
            }
        }

        return magic;
    }

    public static int ComputeShift(int sq, boolean ortho) {
        int numBits = Long.bitCount((ortho ? RookMask[sq].board : BishopMask[sq].board));
        int shift = 64 - numBits;
        return shift;
    }

    public static HashMap<Long, Bitboard> CreateTable(int sq, boolean ortho, long magic, int shift) {
        int numBits = 64 - shift;
        int lookupSize = 1 << numBits;

        HashMap<Long, Bitboard> table = new HashMap<Long, Bitboard>(lookupSize);

        Bitboard movementMask = MovementMask(sq, ortho);
        Bitboard[] blockers = CreateBlockerBoards(movementMask);

        for (Bitboard pattern: blockers) {
            long index = (pattern.board * magic) >>> shift;
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
                Coord c = Coord.Scale(dir, dist);
                Coord coord = Coord.Add(s, c);

                if (coord.isValid()) {
                    res.SetBit(coord.Square());
                    if (blockers.Contains(coord.Square())) {
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
                    mask.SetBit(c.Square());
                }
                else { break; }
            }
        }
        return mask;
    }

    public static Bitboard[] CreateBlockerBoards(Bitboard movementMask) {
        ArrayList<Integer> indices = new ArrayList<>();

        // Go through the legal movement mask and for each bit whose value
        // is 1, add its index to the list of indices
        for (int i = 0; i < 64; i++) {
            if (((movementMask.board >>> i) & 1) == 1) { indices.add(i); }
        }

        // There is 2^n bitboards, one for each arrangement of pieces
        int numBoards = 1 << indices.size();
        Bitboard[] boards = new Bitboard[numBoards];

        for (int index = 0; index < numBoards; index++) {
            boards[index] = new Bitboard();

            for (int bitIndex = 0; bitIndex < indices.size(); bitIndex++) {
                int bit = (index >> bitIndex) & 1;
                long b = ((long) bit) << indices.get(bitIndex);
                boards[index].board |= b;
            }
        }

        return boards;
    }
}
