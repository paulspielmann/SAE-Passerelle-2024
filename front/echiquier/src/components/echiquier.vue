<template>
  <div id="app">
    <div id="board">
      <div class="row" v-for="(row, rowIndex) in chessboard" :key="rowIndex">
        <div class="square" v-for="(square, colIndex) in row" :key="colIndex" :class="square.color" @mousedown="startDrag(square, rowIndex, colIndex)" @mouseup="endDrag(rowIndex, colIndex)">
          <img v-if="square.piece" :src="getImagePath(square.piece)" :alt="square.piece.name" draggable="false">
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      chessboard: []
    };
  },
  mounted() {
    this.createEmptyChessboard();
    this.initializeChessboard();
  },
  methods: {
    createEmptyChessboard() {
      const ROWS = 8;
      const COLS = 8;
      for (let i = 0; i < ROWS; i++) {
        let row = [];
        for (let j = 0; j < COLS; j++) {
          row.push({ piece: null, color: (i + j) % 2 === 0 ? 'light' : 'dark' });
        }
        this.chessboard.push(row);
      }
    },
    initializeChessboard() {
  // Placer les pièces pour le joueur noir
  this.placePiece({ name: 'rook', color: 'black' }, 0, 0);
  this.placePiece({ name: 'knight', color: 'black' }, 0, 1);
  this.placePiece({ name: 'bishop', color: 'black' }, 0, 2);
  this.placePiece({ name: 'queen', color: 'black' }, 0, 3);
  this.placePiece({ name: 'king', color: 'black' }, 0, 4);
  this.placePiece({ name: 'bishop', color: 'black' }, 0, 5);
  this.placePiece({ name: 'knight', color: 'black' }, 0, 6);
  this.placePiece({ name: 'rook', color: 'black' }, 0, 7);
  for (let i = 0; i < 8; i++) {
    this.placePiece({ name: 'pawn', color: 'black' }, 1, i);
  }

  // Placer les pièces pour le joueur blanc
  this.placePiece({ name: 'rook', color: 'white' }, 7, 0);
  this.placePiece({ name: 'knight', color: 'white' }, 7, 1);
  this.placePiece({ name: 'bishop', color: 'white' }, 7, 2);
  this.placePiece({ name: 'queen', color: 'white' }, 7, 3);
  this.placePiece({ name: 'king', color: 'white' }, 7, 4);
  this.placePiece({ name: 'bishop', color: 'white' }, 7, 5);
  this.placePiece({ name: 'knight', color: 'white' }, 7, 6);
  this.placePiece({ name: 'rook', color: 'white' }, 7, 7);
  for (let i = 0; i < 8; i++) {
    this.placePiece({ name: 'pawn', color: 'white' }, 6, i);
  }
},
placePiece(piece, row, col) {
  // Assurez-vous que this.chessboard est correctement initialisé
  if (!this.chessboard[row]) {
    this.chessboard[row] = [];
  }
  this.chessboard[row][col] = { piece: piece, color: (row + col) % 2 === 0 ? 'light' : 'dark' };
},
    startDrag(square, rowIndex, colIndex) {
      if (square.piece) {
        this.dragStart = { piece: square.piece, fromRow: rowIndex, fromCol: colIndex };
      }
    },
    endDrag(toRow, toCol) {
  if (this.dragStart) {
    const { piece, fromRow, fromCol } = this.dragStart;
    // Mettre à jour l'échiquier avec le nouveau positionnement de la pièce
    this.chessboard[toRow][toCol] = { piece: piece, color: this.chessboard[toRow][toCol].color };
    this.chessboard[fromRow][fromCol] = { piece: null, color: this.chessboard[fromRow][fromCol].color };
    this.dragStart = null;
  }
}
,
    getImagePath(piece) {
      // Retourner le chemin de l'image en fonction du type et de la couleur de la pièce
      return `img/${piece.color}_${piece.name}.svg`;
    }
  }
};
</script>

<style>
.light {
  background-color: #f0d9b5; /* Couleur pour les cases claires */
}

.dark {
  background-color: #b58863; /* Couleur pour les cases foncées */
}

#board {
  display: flex;
  flex-wrap: wrap;
  width: 400px;
}

.row {
  display: flex;
  width: 100%;
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
