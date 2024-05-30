<template>
  <div class="container">
    <div class="navigation">
      <p><a href="">Page d'accueil</a></p>
      <p><a href="profil.php">Profil</a></p>
    </div>
    <div class="main-content">
      <div class="board-container">
        <div :class="['timer', 'top', { active: currentPlayer === 'black' }]">Temps restant: {{ formatTime(timeRemainingTop) }}</div>
        <div id="board">
          <div class="row" v-for="row in rows" :key="row.id">
            <div
              class="square"
              v-for="square in row.squares"
              :key="square.id"
              :class="[square.color, { highlight: square.highlight }]"
              @mousedown="startDrag(square)"
              @mouseup="endDrag(square)"
              :style="{ cursor: square.piece ? 'grab' : 'default' }"
            >
              <img v-if="square.piece" :src="getPieceImage(square.piece)" :alt="square.piece" class="piece-image" draggable="false" />
            </div>
          </div>
        </div>
        <div :class="['timer', 'bottom', { active: currentPlayer === 'white' }]">Temps restant: {{ formatTime(timeRemainingBottom) }}</div>
      </div>
      <div class="moves-block" ref="movesBlock">
        <h2>Coups joués</h2>
        <table>
          <tbody>
            <tr v-for="(move, index) in moves" :key="index">
              <td>{{ index + 1 }}</td>
              <td>{{ move }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>




<script>
import axios from 'axios';

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
      timeRemainingTop: 600,  
      timeRemainingBottom: 600,  
      moves: [],
      dragStart: null,
      currentPlayer: 'white',  
      timer: null,
    };
  },
  mounted() {
    this.startTimer();
  },
  beforeDestroy() {
    clearInterval(this.timer);
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
          if (row === 2) piece = 'white_pawn';
          if (row === 7) piece = 'black_pawn';
          if (row === 1) piece = ['white_rook', 'white_knight', 'white_bishop', 'white_queen', 'white_king', 'white_bishop', 'white_knight', 'white_rook'][col];
          if (row === 8) piece = ['black_rook', 'black_knight', 'black_bishop', 'black_queen', 'black_king', 'black_bishop', 'black_knight', 'black_rook'][col];
          squares.push({ id, color, piece, highlight: false });
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
      if (square.piece && this.isCurrentPlayerPiece(square.piece)) {
        this.dragStart = square;
        this.highlightAvailableMoves(square.id);
      } else {
        this.dragStart = null;
      }
    },
    endDrag(square) {
      if (this.dragStart && this.dragStart.piece && this.dragStart !== square) {
        const startSquare = this.dragStart;
        const endSquare = square;
        this.movePiece(startSquare, endSquare);
        this.switchPlayer();
      }
      this.clearHighlights();
      this.dragStart = null;
    },
    highlightAvailableMoves(squareId) {
      const availableMoves = this.getMockAvailableMoves(squareId);
      for (const move of availableMoves) {
        const square = this.findSquareById(move);
        if (square) {
          square.highlight = true;
        }
      }
    },
    clearHighlights() {
      for (const row of this.rows) {
        for (const square of row.squares) {
          square.highlight = false;
        }
      }
    },
    movePiece(startSquare, endSquare) {
      endSquare.piece = startSquare.piece;
      startSquare.piece = null;
      this.moves.push(`${startSquare.id}-${endSquare.id}`);
      this.scrollToBottom();
    },
    switchPlayer() {
      this.currentPlayer = this.currentPlayer === 'white' ? 'black' : 'white';
    },
    isCurrentPlayerPiece(piece) {
      const pieceColor = piece.startsWith('white') ? 'white' : 'black';
      return pieceColor === this.currentPlayer;
    },
    getMockAvailableMoves(squareId) {
      if (squareId === 'e2') return ['e3', 'e4'];
      if (squareId === 'd7') return ['d6', 'd5'];
      return [];
    },
    startTimer() {
      this.timer = setInterval(() => {
        if (this.currentPlayer === 'white') {
          this.timeRemainingBottom--;
        } else {
          this.timeRemainingTop--;
        }
      }, 1000);
    },
    formatTime(seconds) {
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = seconds % 60;
      return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const movesBlock = this.$refs.movesBlock;
        movesBlock.scrollTop = movesBlock.scrollHeight;
      });
    }
  },
};
</script>


<style scoped>


body {
  font-family: 'Arial', sans-serif;
  background-color: #f7f7f7;
  color: #333;
  margin: 0;
  padding: 0;
  overflow: hidden; /* Empêcher le défilement */
  display: flex;
  height: 100vh; /* Hauteur de la page sur 100% de la vue */
}

.container {
  display: flex;
  width: 100%;
  height: 100%;
}

.navigation {
  width: 200px;
  background: #fff;
  padding: 20px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.navigation p {
  margin: 10px 0;
}

a {
  text-decoration: none;
  color: #007BFF;
}

a:hover {
  text-decoration: underline;
}

.main-content {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
  padding: 20px;
}

.board-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
}

#board {
  display: grid;
  grid-template-columns: repeat(8, 100px);
  grid-gap: 0;
  width: 800px;
  margin: auto;
  user-select: none; 
}

.row {
  display: contents;
}

.square {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #000;
  cursor: pointer;
  outline: none; 
  user-select: none; 
}

.piece-image {
  width: 80px;
  height: 80px;
  pointer-events: none;
  outline: none; 
  user-select: none; 
}

.light {
  background-color: #f0d9b5;
}

.dark {
  background-color: #b58863;
}

.highlight {
  background-color: yellow !important; 
}


.timer {
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0;
  text-align: center;
}

.timer.active {
  color: #FF4500;
}

.moves-block {
  width: 300px;
  height: 400px; 
  overflow-y: auto; 
}

.moves-block h2 {
  text-align: center;
}

.moves-block table {
  width: 100%;
  border-collapse: collapse;
}

.moves-block td {
  padding: 5px;
  border: 1px solid #ddd;
  text-align: center;
}

.moves-block tr:nth-child(even) {
  background-color: #f2f2f2;
}

.moves-block tr:hover {
  background-color: #ddd;
}

.number {
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

.letter {
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

</style>




