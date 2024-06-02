import { createRouter, createWebHistory } from 'vue-router';
import Accueil from './components/accueil.vue';
import Board from './components/board.vue';
import Connexion from './components/connexion.vue';
import Inscription from './components/inscription.vue';
import Profil from './components/profil.vue';  // Importer le composant Profil

const routes = [
  {
    path: '/',
    name: 'Accueil',
    component: Accueil,
  },
  {
    path: '/jouer',
    name: 'Board',
    component: Board,
  },
  {
    path: '/inscription',
    name: 'Inscription',
    component: Inscription,
  },
  {
    path: '/connexion',
    name: 'Connexion',
    component: Connexion,
  },
  {
    path: '/profil',
    name: 'Profil',
    component: Profil,
  },
];

const router = createRouter({
  history: createWebHistory('/'), // Définir explicitement la base de l'URL
  routes,
});

export default router;
