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
      rows: this.initializeBoard(),
      dragStart: null,
    };
  },
  methods: {
    initializeBoard() {
      const board = [];
      const letters = 'abcdefgh';
      for (let row = 8; row >= 1; row--) {
        const squares = [];
        for (let col = 0; col < 8; col++) {
          const id = `${letters[col]}${row}`;
          const color = (row + col) % 2 === 0 ? 'light' : 'dark';
          let piece = null;
          if (row === 2) piece = 'black_pawn';
          if (row === 7) piece = 'white_pawn';
          if (row === 1) piece = ['black_rook', 'black_knight', 'black_bishop', 'black_queen', 'black_king', 'black_bishop', 'black_knight', 'black_rook'][col];
          if (row === 8) piece = ['white_rook', 'white_knight', 'white_bishop', 'white_queen', 'white_king', 'white_bishop', 'white_knight', 'white_rook'][col];
          squares.push({ id, color, piece });
        }
        board.push({ id: row, squares });
      }
      return board;
    },
    findSquareById(id) {
      for (const row of this.rows) {
        for (const square of row.squares) {
          if (square.id === id) {
            return square;
          }
        }
      }
      return null;
    },
    getPieceImage(piece) {
      return this.pieceImages[piece] || '';
    },
    startDrag(square) {
      if (square.piece) {
        this.dragStart = square;
      }
    },
    endDrag(square) {
  if (this.dragStart && this.dragStart.piece && this.dragStart !== square) {
    const startSquare = this.dragStart;
    const endSquare = square;
    this.movePiece(startSquare, endSquare);
  }
  this.dragStart = null;
},
    movePiece(startSquare, endSquare) {
      endSquare.piece = startSquare.piece;
      startSquare.piece = null;
    },
  },
};
</script>

<style scoped>
.light {
  background-color: #f0d9b5;
}

.dark {
  background-color: #b58863;
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
  border: 1px solid #000;
  color: #000;
}
</style>
