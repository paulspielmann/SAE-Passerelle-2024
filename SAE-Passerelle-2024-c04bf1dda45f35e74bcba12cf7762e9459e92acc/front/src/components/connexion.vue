<template>
  <div class="container">
    <div class="form-container">
      <h1>Connexion</h1>
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">Nom d'utilisateur</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div class="form-group">
          <label for="password">Mot de passe</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <button type="submit">Se connecter</button>
      </form>
    </div>
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
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.form-container {
  background-color: #f9f9f9;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  width: 100%;
}

h1 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background-color: #007bff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}
</style>
