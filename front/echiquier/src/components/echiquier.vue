<template>
    <div id="board">
      <div class="row" v-for="row in rows" :key="row.id">
        <div class="square" v-for="square in row.squares" :key="square.id" :class="[square.color]">
        {{ square.label }}
        <img v-if="square.image" :src="square.image" alt="Pawn">
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        rows: [
          { id: 1, squares: [ { id: 1, label: '8', color: 'light', image: 'img/black_rook.svg' }
 , { id: 2, color: 'dark', image: 'img/black_bishop.svg' }, { id: 3, color: 'light', image: 'img/black_knight.svg' }, { id: 4, color: 'dark', image: 'img/black_queen.svg' }, { id: 5, color: 'light', image: 'img/black_king.svg'}, { id: 6, color: 'dark', image: 'img/black_knight.svg' }, { id: 7, color: 'light', image: 'img/black_bishop.svg' }, { id: 8, color: 'dark', image: 'img/black_rook.svg' }] },
          { id: 2, squares: [{ id: 1, label: '7', color: 'dark', image: 'img/black_pawn.svg' }, { id: 2, color: 'light', image: 'img/black_pawn.svg'  }, { id: 3, color: 'dark', image: 'img/black_pawn.svg'  }, { id: 4, color: 'light', image: 'img/black_pawn.svg'  }, { id: 5, color: 'dark', image: 'img/black_pawn.svg'  }, { id: 6, color: 'light', image: 'img/black_pawn.svg'  }, { id: 7, color: 'dark', image: 'img/black_pawn.svg'  }, { id: 8, color: 'light', image: 'img/black_pawn.svg'  }] },
          { id: 3, squares: [{ id: 1, label: '6', color: 'light' }, { id: 2,  color: 'dark' }, { id: 3, color: 'light' }, { id: 4, color: 'dark' }, { id: 5, color: 'light' }, { id: 6, color: 'dark' }, { id: 7, color: 'light' }, { id: 8, color: 'dark' }] },
          { id: 4, squares: [{ id: 1, label: '5', color: 'dark' }, { id: 2, color: 'light' }, { id: 3, color: 'dark' }, { id: 4, color: 'light' }, { id: 5, color: 'dark' }, { id: 6, color: 'light' }, { id: 7, color: 'dark' }, { id: 8, color: 'light' }] },
          { id: 5, squares: [{ id: 1, label: '4', color: 'light' }, { id: 2,  color: 'dark' }, { id: 3, color: 'light' }, { id: 4, color: 'dark' }, { id: 5, color: 'light' }, { id: 6, color: 'dark' }, { id: 7, color: 'light' }, { id: 8, color: 'dark' }] },
          { id: 6, squares: [{ id: 1, label: '3', color: 'dark' }, { id: 2, color: 'light' }, { id: 3, color: 'dark' }, { id: 4, color: 'light' }, { id: 5, color: 'dark' }, { id: 6, color: 'light' }, { id: 7, color: 'dark' }, { id: 8, color: 'light' }] },
          { id: 7, squares: [{ id: 1, label: '2', color: 'light', image: 'img/white_pawn.svg' }, { id: 2,  color: 'dark', image: 'img/white_pawn.svg'  }, { id: 3, color: 'light', image: 'img/white_pawn.svg'  }, { id: 4, color: 'dark', image: 'img/white_pawn.svg'  }, { id: 5, color: 'light', image: 'img/white_pawn.svg'  }, { id: 6, color: 'dark', image: 'img/white_pawn.svg'  }, { id: 7, color: 'light', image: 'img/white_pawn.svg'  }, { id: 8, color: 'dark', image: 'img/white_pawn.svg'  }] },
          { id: 8, squares: [{ id: 1, label: '1   a', color: 'dark', image: 'img/white_rook.svg' }, { id: 2, label:'b', color: 'light', image: 'img/white_knight.svg' }, { id: 3, label:'c', color: 'dark', image: 'img/white_bishop.svg' }, { id: 4, label:'d', color: 'light', image: 'img/white_queen.svg' }, { id: 5, label:'e', color: 'dark', image: 'img/white_king.svg' }, { id: 6, label:'f', color: 'light', image: 'img/white_bishop.svg' }, { id: 7, label:'g', color: 'dark', image: 'img/white_knight.svg' }, { id: 8, label:'h', color: 'light', image: 'img/white_rook.svg' }] }
        ]
      };
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
  