import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

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

            RookMagics[i] = ComputeMagic(i, true, random);
            BishopMagics[i] = ComputeMagic(i, false, random);

            RookAttacks[i] = CreateTable(i, true, RookMagics[i], RookShifts[i]);
            BishopAttacks[i] = CreateTable(i, false, BishopMagics[i], BishopShifts[i]);
        }
    }



    private static long ComputeMagic(int sq, boolean ortho, Random random) {
        long magic;

        while (true) {
            magic = random.nextLong();
            boolean unique = true;
            HashMap<Long, Boolean> usedIndices = new HashMap<>();

            for (Bitboard pattern: CreateBlockerBoards(MovementMask(sq, ortho))) {
                long shift = ortho ? RookShifts[sq] : BishopShifts[sq];
                long index = (pattern.board * magic) >>> (64 - shift);
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
                //System.out.println(coord.toString());

                if (coord.isValid()) {
                    res.SetBit(coord.Square());
                    if (blockers.Contains(coord.Square())) {
                        break;
                    }
                }
                else { break; }
            }
        }
        // System.out.println("for blockers:\n" + blockers.toString());
        // System.out.println("Legal moves for piece type "
        //                    + (ortho ? "ortho " : "diag ")
        //                    + "at square " + source + "\n");
        // System.out.println(res.toString());
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
        //System.out.println("Legal moves bitboard for square " + square + " " +  (ortho ? "ortho" : "diag"));
        //System.out.println(mask.toString());
        return mask;
    }

    public static Bitboard[] CreateBlockerBoards(Bitboard movementMask) {
        ArrayList<Integer> indices = new ArrayList<>();

        // Go through the legal movement mask and for each bit whose value
        // is 1, add its index to the list of indices
        for (int i = 0; i < 64; i++) {
            if (((movementMask.board >>> i) & 1) == 1) {
                indices.add(i);
                //System.out.println("Movemask:\n" + movementMask.toString());
                //System.out.println("Indices: " + indices.toString());
            }
        }

        // There is 2^n bitboards, one for each arrangement of pieces
        int numBoards = 1 << indices.size();
        Bitboard[] boards = new Bitboard[numBoards];

        for (int index = 0; index < numBoards; index++) {
            boards[index] = new Bitboard();

            for (int bitIndex = 0; bitIndex < indices.size(); bitIndex++) {
                int bit = (index >> bitIndex) & 1;
                long b = ((long) bit) << indices.get(bitIndex);
                //System.out.println("DEBUG INFO :"
                //                   + "\nBit index: " + bitIndex
                //                   + "\nBit: " + bit
                //                   + "\nIndex.get(bitIndex): " + indices.get(bitIndex)
                //                   + "\nb:\n" + BitboardUtil.toFormattedString(b));
                //System.out.println(BitboardUtil.toFormattedString(b));
                boards[index].board |= b;
            }
        }

        return boards;
    }
}
