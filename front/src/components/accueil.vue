<template>
  <div class="home-container">
    <h1>Bienvenue sur le jeu d'échecs en ligne</h1>
    <p>Prêt à vous lancer dans des parties passionnantes ? Connectez-vous ou créez un compte dès maintenant !</p>
    <div class="button-container">
      <router-link to="/inscription" class="button">S'inscrire</router-link>
      <router-link to="/connexion" class="button">Se connecter</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Accueil'
};
</script>

<style scoped>
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
  background-color: #007BFF;
  color: white;
  text-decoration: none;
  border-radius: 5px;
  transition: background-color 0.3s;
}

.button:hover {
  background-color: #0056b3;
}
</style>
