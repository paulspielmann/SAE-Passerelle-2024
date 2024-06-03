<template>
  <div class="board-container">
    <Menu />
    <div class="main-container">
      <div class="board-section">
        <div :class="['timer', 'top', { active: currentPlayer === 'black' }]">
          Temps restant: {{ formatTime(timeRemainingTop) }}
          <div class="captured-pieces">
            <span v-if="pointDifference > 1">+{{ pointDifference }}</span>
          </div>
        </div>
        <div class="board">
          <div 
            v-for="square in displayBoard" 
            :key="square.index" 
            :class="['square', square.color, { highlight: square.highlight, selected: square === selectedSquare }]" 
            @click="onSquareClick(square)" @dragstart="onDragStart(square)"
            @drop="onDrop(square)" @dragover.prevent>
            <img v-if="square.piece" :src="getPieceImage(square.piece)" :alt="square.piece" class="piece-image"
              draggable="true" />
          </div>
        </div>
        <div :class="['timer', 'bottom', { active: currentPlayer === 'white' }]">
          Temps restant: {{ formatTime(timeRemainingBottom) }}
          <div class="captured-pieces">
            <span v-if="pointDifference < -1">+{{ Math.abs(pointDifference) }}</span>
          </div>
        </div>
      </div>
      <div class="moves-section">
        <h3 class="sticky-header">Coups joués</h3>
        <div class="navigation-buttons">
          <button @click="previousMove">⬅️ Précédent</button>
          <button @click="nextMove">➡️ Suivant</button>
        </div>
        <div>
          <button class="perft" @click="paul()">Run Perft</button>
        </div>
        <div class="moves-table-container" ref="movesTableContainer">
          <table class="moves-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Blancs</th>
                <th>Noirs</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(move, index) in formattedMoves" :key="index">
                <td>{{ index + 1 }}</td>
                <td @click="loadMove(index * 2 + 1)">{{ move.white }}</td>
                <td @click="loadMove(index * 2 + 2)">{{ move.black }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
  
</template>

<script>
import Menu from './menu.vue';
import { getMoves, makeMove, paul } from '@/api';

export default {
  data() {
    return {
      whitePerspective: true,
      whiteToMove: true,
      castlingRights: [true, true, true, true],
      fullMoveCount: 0,
      board: this.initBoard(),
      selectedSquare: null,
      playedMoves: [], // Store the played moves here
      fenStrings: [], // Store FEN strings after each move
      currentMoveIndex: 0, // Keep track of the current move index
      currentPlayer: 'white',
      timeRemainingTop: 600,
      timeRemainingBottom: 600,
      timer: null,
      whitePoints: 0,
      blackPoints: 0,
      pieceValues: {
        'pawn': 1,
        'knight': 3,
        'bishop': 3,
        'rook': 5,
        'queen': 9,
      },
      whiteCapturedPieces: [],
      blackCapturedPieces: [],
    };
  },

  created() {
    this.loadStartPos();
    this.updateBoard();
    this.startTimer();
  },

  beforeDestroy() {
    clearInterval(this.timer);
  },

  computed: {
    displayBoard() {
      const res = [];
      for (let rank = 0; rank < 8; rank++) {
        if (this.whitePerspective) {
          res.push(...this.board.slice(rank * 8, (rank + 1) * 8).reverse());
        } else {
          res.push(...this.board.slice(rank * 8, (rank + 1) * 8));
        }
      }
      return this.whitePerspective ? res.reverse() : res;
    },
    formattedMoves() {
      const moves = [];
      for (let i = 0; i < this.playedMoves.length; i += 2) {
        moves.push({
          white: this.playedMoves[i] || '',
          black: this.playedMoves[i + 1] || ''
        });
      }
      return moves;
    },
    pointDifference() {
      return this.whitePoints - this.blackPoints;
    }
  },

  methods: {
    async paul() {
      await paul();
    },

    initBoard() {
      const board = [];
      const files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];

      for (let i = 0; i < 64; i++) {
        const rank = Math.floor(i / 8) + 1;
        const file = files[i % 8];

        board.push({
          highlight: false,
          index: i,
          coordinate: `${file}${rank}`,
          color: (Math.floor(i / 8) + i) % 2 === 0 ? 'dark' : 'light',
          piece: null,
          whitePiece: null,
          availableMoves: [],
        });
      }

      return board;
    },

    getPieceImage(piece) {
      return `${piece}.svg`;
    },

    isEnnemyPiece(square) {
      return square.piece != null && this.whiteToMove ^ square.whitePiece;
    },

    async onSquareClick(square) {
      if (!this.selectedSquare && this.isEnnemyPiece(square)) {
        return;
      } else if (this.selectedSquare === square) {
        this.clearHighlights();
        this.selectedSquare = null;
      } else if (this.selectedSquare) {
        const move = this.selectedSquare.availableMoves.find(m => m.dest === square.index);
        if (move) {
          await this.makeMove(move);
          this.selectedSquare = null;
          this.clearHighlights();
        }
      } else {
        this.selectedSquare = square;
        this.highlightAvailableMoves(square);
      }
    },

    onDragStart(square) {
      if (square.piece) {
        this.selectedSquare = square;
        this.highlightAvailableMoves(square);
      }
    },

    async onDrop(square) {
      if (this.selectedSquare && this.selectedSquare !== square) {
        const move = this.selectedSquare.availableMoves.find(m => m.dest === square.index);
        if (move) {
          await this.makeMove(move);
          this.selectedSquare = null;
          this.clearHighlights();
        }
      }
    },

    highlightAvailableMoves(square) {
      this.clearHighlights();
      for (const move of square.availableMoves) {
        this.board[move.dest].highlight = true;
      }
    },

    clearHighlights() {
      this.board.forEach(square => {
        square.highlight = false;
      });
    },

    async makeMove(move) {
      try {
        const newFen = await makeMove(move); 
        this.loadFromFen(newFen);
        await this.updateBoard();
      } catch (error) {
        console.error('Error making move:', error);
      }
    },

    // TODO:
    // Change this from loading a new fen after making a move
    // to updating the 2/3 squares modified by the previous move
    async updateBoard() {
      try {
        const moves = await getMoves();

        this.board.forEach(square => {
          square.availableMoves = [];
        });

        // Update move information for current player
        for (const move of moves) {
          this.board[move.source].availableMoves.push(move);
        }

      } catch (error) {
        console.error('Error updating board:', error);
      }
    },

    getPieceFromChar(piece) {
      switch (piece) {
        case 'p':
          return 'black_pawn';
        case 'n':
          return 'black_knight';
        case 'b':
          return 'black_bishop';
        case 'r':
          return 'black_rook';
        case 'q':
          return 'black_queen';
        case 'k':
          return 'black_king';

        case 'P':
          return 'white_pawn';
        case 'N':
          return 'white_knight';
        case 'B':
          return 'white_bishop';
        case 'R':
          return 'white_rook';
        case 'Q':
          return 'white_queen';
        case 'K':
          return 'white_king';

        default:
          return '';
      }
    },

    loadFromFen(fen) {
      this.board = this.initBoard();

      const fields = fen.split(' ');

      let file = 0;
      let rank = 7;

      for (const c of fields[0]) {
        if (c == '/') {
          file = 0;
          rank--;
        } else {
          if (c >= '0' && c <= '9') {
            file += parseInt(c);
          } else {
            let index = rank * 8 + file;
            this.board[index].piece = this.getPieceFromChar(c);
            this.board[index].whitePiece = /^[A-Z]*$/.test(c);
            file++;
          }
        }
      }
      this.whiteToMove = fields[1] == 'w';

      const castle = fields[2];
      this.castlingRights[0] = castle.includes('K');
      this.castlingRights[1] = castle.includes('Q');
      this.castlingRights[2] = castle.includes('k');
      this.castlingRights[3] = castle.includes('q');

      this.enPassantTarget = fields[3] === '-' ? null : fields[3];
      this.fullMoveCount = fields[5];
    },

    loadStartPos() {
      const startPos = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
      this.fenStrings = [startPos];
      this.loadFromFen(startPos);
      this.currentMoveIndex = 0;
    },

    loadMove(index) {
      if (index === 0) {
        this.loadStartPos();
      } else {
        this.currentMoveIndex = index;
        this.loadFromFen(this.fenStrings[index]);
        this.updateBoard();
      }
    },

    previousMove() {
      if (this.currentMoveIndex > 0) {
        this.currentMoveIndex--;
        this.loadMove(this.currentMoveIndex);
      }
    },

    nextMove() {
      if (this.currentMoveIndex < this.fenStrings.length - 1) {
        this.currentMoveIndex++;
        this.loadMove(this.currentMoveIndex);
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.movesTableContainer;
        container.scrollTop = container.scrollHeight;
      });
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

    switchPlayer() {
      this.currentPlayer = this.currentPlayer === 'white' ? 'black' : 'white';
    },
  },

  mounted() {
    this.updateBoard();
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
  overflow: hidden;
  display: flex;
  height: 100vh;
}

.main-container {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  padding: 20px;
}

.board-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
}

.board {
  display: flex;
  flex-wrap: wrap;
  width: 850px;
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

.captured-piece-image {
  width: 24px;
  height: 24px;
  margin: 0 2px;
}

.light {
  background-color: #b58863;
}

.dark {
  background-color: #f0d9b5;
}

.highlight {
  background-color: yellow !important;
}

.selected {
  background-color: rgba(0, 255, 0, 0.5) !important;
}

.timer {
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0;
  text-align: center;
  position: relative;
}

.timer.active {
  color: #FF4500;
}

.captured-pieces {
  font-size: 16px;
  margin-top: 5px;
}

.moves-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-left: 20px;
  max-height: 80vh; /* Set a maximum height for the moves section */
  overflow: hidden;
}

.sticky-header {
  position: sticky;
  top: 0;
  background-color: #f7f7f7;
  z-index: 1;
  margin-bottom: 10px;
  width: 100%;
  text-align: center;
}

.navigation-buttons {
  margin-bottom: 10px;
}

.navigation-buttons button {
  margin: 0 5px;
  padding: 5px 10px;
  font-size: 16px;
  cursor: pointer;
}

.moves-table-container {
  max-height: 60vh; /* Set the height for the table container */
  overflow-y: auto; /* Enable vertical scrolling */
  width: 100%;
}

.moves-table {
  width: 100%;
  border-collapse: collapse;
}

.moves-table th, .moves-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.moves-table th {
  background-color: #f2f2f2;
}

.moves-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.moves-table tr:hover {
  background-color: #ddd;
}
</style>
