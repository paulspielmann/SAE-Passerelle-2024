<template>
  <div id="board">
    <div class="row" v-for="row in rows" :key="row.id">
      <div class="square" v-for="square in row.squares" :key="square.id" :class="[square.color]" @mousedown="startDrag(square)" @mouseup="endDrag(square)" :style="{ cursor: square.piece ? 'grab' : 'default' }">
        {{ square.label }}
        <img v-if="square.piece" :src="getPieceImage(square.piece, square.color)" alt="pawn" draggable="false">
      </div>
    </div>
  </div>
</template>

<script>
import pieceData from './pieces.json';

export default {
  data() {
    return {
      pieceData: pieceData, // Déclarez pieceData en tant que propriété
      pieces: pieceData.pieces,
      rows: [
        { id: 1, squares: [
          { id: 1, label: '8', color: 'light', piece: null },
          { id: 2, color: 'dark', piece: null },
          { id: 3, color: 'light', piece: null },
          { id: 4, color: 'dark', piece: null },
          { id: 5, color: 'light', piece: null },
          { id: 6, color: 'dark', piece: null },
          { id: 7, color: 'light', piece: null },
          { id: 8, color: 'dark', piece: null }
        ] },
        { id: 2, squares: [
          { id: 1, label: '7', color: 'dark', piece: null },
          { id: 2, color: 'light', piece: null },
          { id: 3, color: 'dark', piece: null },
          { id: 4, color: 'light', piece: null },
          { id: 5, color: 'dark', piece: null },
          { id: 6, color: 'light', piece: null },
          { id: 7, color: 'dark', piece: null },
          { id: 8, color: 'light', piece: null }
        ] },
        { id: 3, squares: [
          { id: 1, label: '6', color: 'light', piece: null },
          { id: 2, color: 'dark', piece: null },
          { id: 3, color: 'light', piece: null },
          { id: 4, color: 'dark', piece: null },
          { id: 5, color: 'light', piece: null },
          { id: 6, color: 'dark', piece: null },
          { id: 7, color: 'light', piece: null },
          { id: 8, color: 'dark', piece: null }
        ] },
        { id: 4, squares: [
          { id: 1, label: '5', color: 'dark', piece: null },
          { id: 2, color: 'light', piece: null },
          { id: 3, color: 'dark', piece: null },
          { id: 4, color: 'light', piece: null },
          { id: 5, color: 'dark', piece: null },
          { id: 6, color: 'light', piece: null },
          { id: 7, color: 'dark', piece: null },
          { id: 8, color: 'light', piece: null }
        ] },
        { id: 5, squares: [
          { id: 1, label: '4', color: 'light', piece: null },
          { id: 2, color: 'dark', piece: null },
          { id: 3, color: 'light', piece: null },
          { id: 4, color: 'dark', piece: null },
          { id: 5, color: 'light', piece: null },
          { id: 6, color: 'dark', piece: null },
          { id: 7, color: 'light', piece: null },
          { id: 8, color: 'dark', piece: null }
        ] },
        { id: 6, squares: [
          { id: 1, label: '3', color: 'dark', piece: null },
          { id: 2, color: 'light', piece: null },
          { id: 3, color: 'dark', piece: null },
          { id: 4, color: 'light', piece: null },
          { id: 5, color: 'dark', piece: null },
          { id: 6, color: 'light', piece: null },
          { id: 7, color: 'dark', piece: null },
          { id: 8, color: 'light', piece: null }
        ] },
        { id: 7, squares: [
          { id: 1, label: '2', color: 'light', piece: null },
          { id: 2, color: 'dark', piece: null },
          { id: 3, color: 'light', piece: null },
          { id: 4, color: 'dark', piece: null },
          { id: 5, color: 'light', piece: null },
          { id: 6, color: 'dark', piece: null },
          { id: 7, color: 'light', piece: null },
          { id: 8, color: 'dark', piece: null }
        ] },
        { id: 8, squares: [
          { id: 1, label: '1   a', color: 'dark', piece: null },
          { id: 2, label: 'b', color: 'light', piece: null },
          { id: 3, label: 'c', color: 'dark', piece: null },
          { id: 4, label: 'd', color: 'light', piece: null },
          { id: 5, label: 'e', color: 'dark', piece: null },
          { id: 6, label: 'f', color: 'light', piece: null },
          { id: 7, label: 'g', color: 'dark', piece: null },
          { id: 8, label: 'h', color: 'light', piece: null }
        ] }
      ],
      dragStart: null
  };
},
  methods: {
    initializeBoard() {
  let pieceIndex = 0;
  // Parcours des lignes de l'échiquier
  for (let i = 0; i < this.rows.length; i++) {
    const row = this.rows[i];
    // Parcours des cases de chaque ligne
    for (let j = 0; j < row.squares.length; j++) {
      const square = row.squares[j];
      // Attribution des couleurs aux cases en alternance
      square.color = (i + j) % 2 === 0 ? 'light' : 'dark';

      // Vérification s'il reste des pièces à placer
      if (pieceIndex < this.pieces.length) {
        const piece = this.pieces[pieceIndex];
        // Attribution des pièces en fonction de la position sur l'échiquier
        if (i < 2 || i > 5) {
          square.piece = piece;
          pieceIndex++;
        } else {
          square.piece = null; // Cases vides pour les lignes 3 à 6
        }
      } else {
        square.piece = null; // Case vide s'il n'y a plus de pièces disponibles
      }
    }
  }
},
    startDrag(square) {
      if (square.piece) {
        this.dragStart = square;
      }
    },
    endDrag(square) {
      if (this.dragStart) {
        
          // Move the piece only if the destination square is of a different color
          square.piece = this.dragStart.piece;
          this.dragStart.piece = null;
        this.dragStart = null;
      }
    },
    getPieceImage(piecename, color) {
  const pieces = this.pieceData.pieces; // Accédez aux pièces via la propriété pieceData
  const piece = pieces.find(piece => piece.name === piecename && piece.color === color);
  if (piece) {
    return piece.image;
  } else {
    console.error("Piece image not found for piece:", piecename, "and color:", color);
    return null;
  }
}


  },
  created() {
    this.initializeBoard();
  }
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
