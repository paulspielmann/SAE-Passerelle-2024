<template>
    <div class="container">
      <h1>Inscription</h1>
      <form @submit.prevent="register">
        <div>
          <label for="username">Nom d'utilisateur</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div>
          <label for="email">Email</label>
          <input type="email" id="email" v-model="email" required>
        </div>
        <div>
          <label for="password">Mot de passe</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <button type="submit">S'inscrire</button>
      </form>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'Inscription',
    data() {
      return {
        username: '',
        email: '',
        password: ''
      };
    },
    methods: {
      async register() {
        try {
          const response = await axios.post('/path/to/inscription.php', {
            username: this.username,
            email: this.email,
            password: this.password
          });
          // Gérer la réponse de votre serveur PHP
          console.log(response.data);
        } catch (error) {
          console.error('Erreur lors de l\'inscription', error);
        }
      }
    }
  };
  </script>
  
  <style scoped>
  @import '../assets/stylesheet.css'; /* Utilisez le bon chemin vers le fichier CSS */
  </style>
  