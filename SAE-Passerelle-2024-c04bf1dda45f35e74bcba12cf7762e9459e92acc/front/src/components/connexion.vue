<template>
    <div class="container">
      <h1>Connexion</h1>
      <form @submit.prevent="login">
        <div>
          <label for="username">Nom d'utilisateur</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div>
          <label for="password">Mot de passe</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <button type="submit">Se connecter</button>
      </form>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'Connexion',
    data() {
      return {
        username: '',
        password: ''
      };
    },
    methods: {
      async login() {
        try {
          const response = await axios.post('/path/to/connexion.php', {
            username: this.username,
            password: this.password
          });
          // Gérer la réponse de votre serveur PHP
          console.log(response.data);
        } catch (error) {
          console.error('Erreur lors de la connexion', error);
        }
      }
    }
  };
  </script>
  
  <style scoped>
  @import '../assets/stylesheet.css'; /* Utilisez le bon chemin vers le fichier CSS */
  </style>
  