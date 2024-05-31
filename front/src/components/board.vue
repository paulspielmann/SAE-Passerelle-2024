<template>
  <div class="board">
    <div 
    v-for="square in displayBoard" 
    :key="square.index" 
    :class="['square',
      square.color,
      {
        highlight: square.highlight,
        selected: square === selectedSquare
      }]" 
      @click="onSquareClick(square)" @dragstart="onDragStart(square)"
      @drop="onDrop(square)" @dragover.prevent>
      <img v-if="square.piece" :src="getPieceImage(square.piece)" :alt="square.piece" class="piece-image"
        draggable="true" />
    </div>
  </div>
  <div class="moves-list">
    <h3>Available Moves</h3>
    <ul>
      <li v-for="move in moves" :key="move">{{ move }}</li>
    </ul>
  </div>
</template>

<script>
import { getMoves, makeMove } from '@/api';

export default {
  data() {
    return {
      whitePerspective: true,

      whiteToMove: true,
      castlingRights: [true, true, true, true],
      fullMoveCount: 0,
      board: this.initBoard(),
      selectedSquare: null,
      moves: [],
    };
  },

  created() {
    this.loadStartPos();
    this.updateBoard();
  },

  computed: {
    displayBoard() {
      const res = [];
      for (let rank = 0; rank < 8; rank++) {
        if (this.whitePerspective) {
          res.push(...this.board.slice(rank * 8, (rank + 1) * 8).reverse());
        }
        else {
          res.push(...this.board.slice(rank * 8, (rank + 1) * 8));
        }
      }
      return this.whitePerspective ? res.reverse() : res;
    }
  },

  methods: {
    // Initializes an empty board
    // Setting up the pieces is handled by the FEN loading function
    // We also have a method LoadStartPos called unless told otherwise
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
          color: (Math.floor(i / 8) + i) % 2 === 0 ? 'light' : 'dark',

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
      return (square.piece != null) && (this.whiteToMove ^ square.whitePiece)
    },

    // Param square => square that is clicked on
    async onSquareClick(square) {
      if (!this.selectedSquare && this.isEnnemyPiece(square)) {
        return;
      }

      else if (this.selectedSquare === square) {
        this.clearHighlights();
        this.selectedSquare = null;
      }

      else if (this.selectedSquare) {
        const move = this.selectedSquare.availableMoves.find(m => m.dest === square.index);
        if (move) {
          await this.makeMove(move)
          this.selectedSquare = null;
          this.clearHighlights();
        }
      }

      else {
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
      })
    },

    async makeMove(move) {
      console.log(`Making move from ${move.source} to ${move.dest}`);
      try {
        const newFen = await makeMove(move);
        console.log(`Received new FEN: ${newFen}`);
        this.loadFromFen(newFen);
        await this.updateBoard();
      } catch (error) {
        console.error('Error making move:', error);
      }
    },

    async updateBoard() {
      try {
        const moves = await getMoves();

        this.board.forEach(square => {
          square.availableMoves = [];
        })

        // Update move information for current player
        for (const move of moves) {
          this.board[move.source].availableMoves.push(move);
        }

        this.moves = moves;

      } catch (error) {
        console.error('Error updating board:', error);
      }
    },

    getPieceFromChar(piece) {
      switch (piece) {
        case 'p':
          return 'black_pawn';
          break;
        case 'n':
          return 'black_knight';
          break;
        case 'b':
          return 'black_bishop';
          break;
        case 'r':
          return 'black_rook';
          break;
        case 'q':
          return 'black_queen';
          break;
        case 'k':
          return 'black_king';
          break;

        case 'P':
          return 'white_pawn';
          break;
        case 'N':
          return 'white_knight';
          break;
        case 'B':
          return 'white_bishop';
          break;
        case 'R':
          return 'white_rook';
          break;
        case 'Q':
          return 'white_queen';
          break;
        case 'K':
          return 'white_king';
          break;

        default:
          return '';
          break;
      }
    },

    // Parse a FEN (standard notation for chess positions) string
    loadFromFen(fen) {
      this.board = this.initBoard();

      const fields = fen.split(' ');

      let file = 0;
      let rank = 7; // FEN strings have rank 8 at the leftmost extremity

      for (const c of fields[0]) {
        if (c == '/') {
          file = 0;
          rank--;
        }
        else {
          if (c >= '0' && c <= '9') {
            file += parseInt(c);
          }
          else {
            let index = rank * 8 + file;
            this.board[index].piece = this.getPieceFromChar(c);
            this.board[index].whitePiece = /^[A-Z]*$/.test(c);
            file++;
          }
        }
      }
      this.whiteToMove = fields[1] == ("w");

      const castle = fields[2];
      this.castlingRights[0] = castle.includes("K");
      this.castlingRights[1] = castle.includes("Q");
      this.castlingRights[2] = castle.includes("k");
      this.castlingRights[3] = castle.includes("q");

      this.fullMoveCount = fields[5];
    },

    loadStartPos() {
      this.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
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
  /* Empêcher le défilement */
  display: flex;
  height: 100vh;
  /* Hauteur de la page sur 100% de la vue */
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

.light {
  background-color: #f0d9b5;
}

.dark {
  background-color: #b58863;
}

.highlight {
  background-color: purple !important;
}

.selected {
  background-color: rgba(0, 255, 0, 0.5) !important;
}

.moves-list {
  margin-top: 20px;
  text-align: center;
}

.moves-list ul {
  list-style-type: none;
  padding: 0;
}

.moves-list li {
  margin: 5px 0;
}
</style>