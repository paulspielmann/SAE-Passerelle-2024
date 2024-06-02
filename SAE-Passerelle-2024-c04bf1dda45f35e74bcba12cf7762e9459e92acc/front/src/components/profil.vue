<template>
    <div class="container">
      <Menu />
      <h1>Profil de l'utilisateur</h1>
      <!-- Ajoutez les informations de profil ici -->
    </div>
  </template>
  
  <script>
  import Menu from './menu.vue';
  
  export default {
    name: 'Profil',
    components: {
      Menu
    }
  };
  </script>
  
  <style scoped>
  @import '../assets/stylesheet.css'; /* Réutiliser le fichier CSS pour le style */
  </style>
  