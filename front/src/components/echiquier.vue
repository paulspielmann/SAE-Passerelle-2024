<template>
  <div id="board">
    <div class="row" v-for="row in rows" :key="row.id">
      <div
        class="square"
        v-for="square in row.squares"
        :key="square.id"
        :class="[square.color]"
        @mousedown="startDrag(square)"
        @mouseup="endDrag(square)"
        :style="{ cursor: square.piece ? 'grab' : 'default' }"
      >
        <img v-if="square.piece" :src="getPieceImage(square.piece)" :alt="square.piece" draggable="false" />
      </div>
    </div>
  </div>
</template>



<script>
import blackRookImage from './img/black_rook.svg';
import blackKnightImage from './img/black_knight.svg';
import blackBishopImage from './img/black_bishop.svg';
import blackQueenImage from './img/black_queen.svg';
import blackKingImage from './img/black_king.svg';
import blackPawnImage from './img/black_pawn.svg';

import whiteRookImage from './img/white_rook.svg';
import whiteKnightImage from './img/white_knight.svg';
import whiteBishopImage from './img/white_bishop.svg';
import whiteQueenImage from './img/white_queen.svg';
import whiteKingImage from './img/white_king.svg';
import whitePawnImage from './img/white_pawn.svg';

export default {
  data() {
    return {
      pieceImages: {
        black_rook: blackRookImage,
        black_knight: blackKnightImage,
        black_bishop: blackBishopImage,
        black_queen: blackQueenImage,
        black_king: blackKingImage,
        black_pawn: blackPawnImage,
        white_rook: whiteRookImage,
        white_knight: whiteKnightImage,
        white_bishop: whiteBishopImage,
        white_queen: whiteQueenImage,
        white_king: whiteKingImage,
        white_pawn: whitePawnImage,
      },
      rows: [
        {
          id: 1,
          squares: [
            { id: 1, color: 'light', piece: 'black_rook' },
            { id: 2, color: 'dark', piece: 'black_knight' },
            { id: 3, color: 'light', piece: 'black_bishop' },
            { id: 4, color: 'dark', piece: 'black_queen' },
            { id: 5, color: 'light', piece: 'black_king' },
            { id: 6, color: 'dark', piece: 'black_bishop' },
            { id: 7, color: 'light', piece: 'black_knight' },
            { id: 8, color: 'dark', piece: 'black_rook' },
          ],
        },
        {
          id: 2,
          squares: [
            { id: 1, color: 'dark', piece: 'black_pawn' },
            { id: 2, color: 'light', piece: 'black_pawn' },
            { id: 3, color: 'dark', piece: 'black_pawn' },
            { id: 4, color: 'light', piece: 'black_pawn' },
            { id: 5, color: 'dark', piece: 'black_pawn' },
            { id: 6, color: 'light', piece: 'black_pawn' },
            { id: 7, color: 'dark', piece: 'black_pawn' },
            { id: 8, color: 'light', piece: 'black_pawn' },
          ],
        },
        {
          id: 3,
          squares: [
            { id: 1, color: 'light', piece: null },
            { id: 2, color: 'dark', piece: null },
            { id: 3, color: 'light', piece: null },
            { id: 4, color: 'dark', piece: null },
            { id: 5, color: 'light', piece: null },
            { id: 6, color: 'dark', piece: null },
            { id: 7, color: 'light', piece: null },
            { id: 8, color: 'dark', piece: null },
          ],
        },
        {
          id: 4,
          squares: [
            { id: 1, color: 'dark', piece: null },
            { id: 2, color: 'light', piece: null },
            { id: 3, color: 'dark', piece: null },
            { id: 4, color: 'light', piece: null },
            { id: 5, color: 'dark', piece: null },
            { id: 6, color: 'light', piece: null },
            { id: 7, color: 'dark', piece: null },
            { id: 8, color: 'light', piece: null },
          ],
        },
        {
          id: 5,
          squares: [
            { id: 1, color: 'light', piece: null },
            { id: 2, color: 'dark', piece: null },
            { id: 3, color: 'light', piece: null },
            { id: 4, color: 'dark', piece: null },
            { id: 5, color: 'light', piece: null },
            { id: 6, color: 'dark', piece: null },
            { id: 7, color: 'light', piece: null },
            { id: 8, color: 'dark', piece: null },
          ],
        },
        {
          id: 6,
          squares: [
            { id: 1, color: 'dark', piece: null },
            { id: 2, color: 'light', piece: null },
            { id: 3, color: 'dark', piece: null },
            { id: 4, color: 'light', piece: null },
            { id: 5, color: 'dark', piece: null },
            { id: 6, color: 'light', piece: null },
            { id: 7, color: 'dark', piece: null },
            { id: 8, color: 'light', piece: null },
          ],
        },
        {
          id: 7,
          squares: [
            { id: 1, color: 'light', piece: 'white_pawn' },
            { id: 2, color: 'dark', piece: 'white_pawn' },
            { id: 3, color: 'light', piece: 'white_pawn' },
            { id: 4, color: 'dark', piece: 'white_pawn' },
            { id: 5, color: 'light', piece: 'white_pawn' },
            { id: 6, color: 'dark', piece: 'white_pawn' },
            { id: 7, color: 'light', piece: 'white_pawn' },
            { id: 8, color: 'dark', piece: 'white_pawn' },
          ],
        },
        {
          id: 8,
          squares: [
            { id: 1, color: 'dark', piece: 'white_rook' },
            { id: 2, color: 'light', piece: 'white_knight' },
            { id: 3, color: 'dark', piece: 'white_bishop' },
            { id: 4, color: 'light', piece: 'white_queen' },
            { id: 5, color: 'dark', piece: 'white_king' },
            { id: 6, color: 'light', piece: 'white_bishop' },
            { id: 7, color: 'dark', piece: 'white_knight' },
            { id: 8, color: 'light', piece: 'white_rook' },
          ],
        },
      ],
      dragStart: null,
    };
  },
  methods: {
    startDrag(square) {
      if (square.piece) {
        this.dragStart = square;
      }
    },
    endDrag(square) {
      if (this.dragStart) {
        square.piece = this.dragStart.piece;
        this.dragStart.piece = null;
        this.dragStart = null;
      }
    },
    getPieceImage(piece) {
      return this.pieceImages[piece] || '';
    },
  },
};
</script>


<style scoped>
.light {
  background-color: #f0d9b5; /* Couleur pour les cases claires */
}

.dark {
  background-color: #b58863; /* Couleur pour les cases foncées */
}

#board {
  display: grid;
  grid-template-columns: repeat(8, 50px);
  width: 400px;
  margin: auto;
}

.row {
  display: contents;
}

.square {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #000; /* Ajoute une bordure pour distinguer les cases */
  color: #000; /* Couleur du texte dans les cases */
}
</style>
