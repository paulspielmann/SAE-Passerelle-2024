package com.sae.Chess.model;

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

    public static int[] RookShifts;
    public static int[] BishopShifts;

    public static long[] RookMagics;
    public static long[] BishopMagics;

    public static Bitboard GetSliderAttacks(int sq, long blockers, boolean ortho) {
        return ortho ? GetRookAttacks(sq, blockers) : GetBishopAttacks(sq, blockers);
    }

    public static Bitboard GetRookAttacks(int sq, long blockers) {
        Long key = ((blockers & RookMask[sq].board) *
                    RookMagics[sq]) >>> RookShifts[sq];
        return new Bitboard(RookAttacks[sq].get(key).board);
    }

    public static Bitboard GetBishopAttacks(int sq, long blockers) {
        long key = ((blockers & BishopMask[sq].board) *
                    BishopMagics[sq]) >>> BishopShifts[sq];
        return new Bitboard(BishopAttacks[sq].get(key).board);
    }

    public Magic() {
        RookMask = new Bitboard[64];
        BishopMask = new Bitboard[64];
        RookAttacks = new HashMap[64];
        BishopAttacks = new HashMap[64];

        RookMagics = new long[64];
        BishopMagics = new long[64];
        RookShifts = new int[64];
        BishopShifts = new int[64];

        Random random = new Random();

        for (int i = 0; i < 64; i++) {
            RookMask[i] = MovementMask(i, true);
            BishopMask[i] = MovementMask(i, false);

            RookShifts[i] = ComputeShift(i, true);
            BishopShifts[i] = ComputeShift(i, false);

            if (!LoadMagics("magics.dat")) {
                System.out.println("Load failed, generating new magics...");
                RookMagics[i] = ComputeMagic(i, true, random);
                BishopMagics[i] = ComputeMagic(i, false, random);

                SaveMagics("magics.dat");
            }

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

    public static boolean LoadMagics(String filename) {
        try (ObjectInputStream ois
             = new ObjectInputStream(new FileInputStream(filename))) {
            RookMagics = (long[]) ois.readObject();
            BishopMagics = (long[]) ois.readObject();
            return true;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void SaveMagics(String filename) {
        try (ObjectOutputStream oos =
             new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(RookMagics);
            oos.writeObject(BishopMagics);
            System.out.println("Saved magics to file " + filename);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

            if (table.containsKey(index)) {
                System.out.println("COLLISION DETECTED!");
                System.out.println("Magic: " + magic);
                System.out.println("Square: " + sq +
                                   ", Pattern: " + pattern.board +
                                   ", Key: " + index);

                BitboardUtil.print2(table.get(index), moves,
                                    "Existing moves", "New moves");

            }

            table.put(index, moves);
        }

        return table;
    }

    public static Bitboard LegalMoves(int source, Bitboard blockers, boolean ortho) {
        Bitboard res = new Bitboard();
        Coord[] dirs = ortho ? BoardHelper.RookDirs : BoardHelper.BishopDirs;
        Coord s = new Coord(source);

        for (Coord dir : dirs) {
            for (int dist = 1; dist < 8; dist++) {
                Coord c = Coord.Scale(dir, dist);
                Coord coord = Coord.Add(s, c);

                if (coord.isValid()) {
                    res.SetBit(coord.Square());
                    if (blockers.Contains(coord.Square())) { break; }
                } else { break; }
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
