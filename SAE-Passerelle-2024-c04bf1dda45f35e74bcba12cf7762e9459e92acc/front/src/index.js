// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'
import Accueil from './components/accueil.vue'
import Connexion from './components/connexion.vue'
import Inscription from './components/inscription.vue'
import Profil from './components/profil.vue'
import Board from './components/board.vue'

const routes = [
  {
    path: '/',
    name: 'accueil',
    component: Accueil
  },
  {
    path: '/connexion',
    name: 'connexion',
    component: Connexion
  },
  {
    path: '/inscription',
    name: 'inscription',
    component: Inscription
  },
  {
    path: '/profil',
    name: 'profil',
    component: Profil
  },
  {
    path: '/jouer',
    name: 'jouer',
    component: Board
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
