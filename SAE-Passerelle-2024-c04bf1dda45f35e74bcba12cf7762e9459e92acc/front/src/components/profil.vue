<template>
  <div class="container">
    <h1>Bienvenue, {{ username }}</h1>
    <button @click="startGame">Lancer une partie d'échecs</button>
    <!-- Add the chess game component or logic here -->
  </div>
</template>

<script>
export default {
  name: 'Profil',
  props: ['username'],
  methods: {
    startGame() {
      // Logic to start a chess game
      console.log('Partie d\'échecs lancée par', this.username);
    }
  }
};
</script>

<style scoped>
@import '../assets/stylesheet.css'; /* Utilisez le bon chemin vers le fichier CSS */
</style>
