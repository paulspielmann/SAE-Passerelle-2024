<template>
  <div class="home-container">
    <h1>Bienvenue sur le jeu d'échecs en ligne</h1>
    <p>Prêt à vous lancer dans des parties passionnantes ? Connectez-vous ou créez un compte dès maintenant !</p>
    <div class="button-container">
      <router-link to="/inscription" class="button">S'inscrire</router-link>
      <router-link to="/connexion" class="button">Se connecter</router-link>
      <router-link to="/jouer" class="button">Jouer</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Accueil'
};
</script>

<style scoped>
@import '../assets/stylesheet.css';

.home-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  height: 100vh;
  margin: 0;
}

.button-container {
  margin-top: 20px;
}

.button {
  display: inline-block;
  padding: 10px 20px;
  margin: 0 10px;
  background-color: #ff4081;
  color: white;
  text-decoration: none;
  border-radius: 5px;
  transition: background-color 0.3s, transform 0.3s;
}

.button:hover {
  background-color: #e91e63;
  transform: scale(1.05);
}
</style>
