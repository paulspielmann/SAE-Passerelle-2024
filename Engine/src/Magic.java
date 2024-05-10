import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

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

    public static final long[] TempMagics = {
        0x0000000000000402L, 0x0000000000000204L, 0x0000000000000088L, 0x0000000000000100L,
        0x0000000000000020L, 0x0000000000000088L, 0x0000000000000044L, 0x0000000000000022L,
        0x0000000000000008L, 0x0000000000000400L, 0x0000000000000200L, 0x0000000000000088L,
        0x0000000000000100L, 0x0000000000000040L, 0x0000000000000080L, 0x0000000000000040L,
        0x0000000000000001L, 0x0000000000000001L, 0x0000000000000001L, 0x0000000000000001L,
        0x0000000000000001L, 0x0000000000000001L, 0x0000000000000001L, 0x0000000000000001L,
        0x0000000000000001L, 0x0000000000000001L, 0x0000000000000001L, 0x0000000000000001L,
        0x000000000
    };

    public static final int[] TempShitfs  = {
        6, 5, 5, 6,
        5, 5, 5, 5,
        4, 6, 5, 5,
        5, 5, 5, 5,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
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

            // RookMagics[i] = ComputeMagic(i, true, random);
            // BishopMagics[i] = ComputeMagic(i, false, random);

            RookMagics[i] = TempMagics[i];
            BishopMagics[i] = TempMagics[i];

            RookShifts[i] = ComputeShift(i, true);
            BishopShifts[i] = ComputeShift(i, false);

            RookAttacks[i] = CreateTable(i, true, RookMagics[i], RookShifts[i]);
            BishopAttacks[i] = CreateTable(i, false, BishopMagics[i], BishopShifts[i]);
            SaveTablesToFile("/home/Paul/Projects/SAE_PASSERELLE_2024/Engine/src/tables.txt");
        }
    }

    public void SaveTablesToFile(String filename) {
        try (FileOutputStream file = new FileOutputStream(filename);
             ObjectOutputStream object = new ObjectOutputStream(file)) {
            object.writeObject(RookAttacks);
            object.writeObject(BishopAttacks);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long ComputeMagic(int sq, boolean ortho, Random random) {
        HashMap<Long, Boolean> usedIndices = new HashMap<>();
        long magic;

        while (true) {
            magic = random.nextLong();
            boolean unique = true;

            for (Bitboard pattern : CreateBlockerBoards(MovementMask(sq, ortho))) {
                System.out.println("Pattern: " + pattern.board);
                long index = (pattern.board * magic) >>> (64 - (ortho ? RookShifts[sq] : BishopShifts[sq]));
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


    public static int NumBitsInTable(int sq, boolean ortho) {
        int num = Long.bitCount((ortho ? RookMask[sq].board : BishopMask[sq].board));
        return 1 << num;
    }

    public static int ComputeShift(int sq, boolean ortho) {
        return 64 - NumBitsInTable(sq, ortho);
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
            System.out.println(index + " " + moves);
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

        System.out.println("Square: " + square);

        for (Coord dir: dirs) {
            for (int dist = 1; dist < 8; dist++) {
                Coord c = Coord.Add(s, Coord.Scale(dir, dist));
                Coord next = Coord.Add(s, Coord.Scale(dir, dist + 1));
                System.out.println("Coord c: " + c + ", AS INDEX: " + c.Square());
                System.out.println("Coord next: " + next + ", AS INDEX: " + next.Square());

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

        System.out.println(movementMask.toString());
        for (int i = 0; i < 64; i++) {
            if (((movementMask.board >>> i) & 1) == 1) {
                indices.add(i);
            }
        }

        // There is 2^n bitboards, one for each arrangemnt of pieces
        int numBoards = 1 << indices.size();
        Bitboard[] boards = new Bitboard[numBoards];

        for (int index = 0; index < numBoards; index++) {
            boards[index] = new Bitboard();

            for (int bitIndex = 0; bitIndex < indices.size(); bitIndex++) {
                int bit = (index >> bitIndex) & 1;
                long b = ((long) bit) >>> indices.get(bitIndex);
                boards[index].board |= b;
            }
        }

        return boards;
    }
}
