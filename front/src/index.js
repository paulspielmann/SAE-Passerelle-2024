import { createRouter, createWebHistory } from 'vue-router';
import Accueil from '../views/Home.vue';
import OtherPage from '../views/OtherPage.vue';

const routes = [
  {
    path: 'components/accueil.vue',
    name: 'Accueil',
    component: Accueil,
  },
  {
    path: '/other',
    name: 'OtherPage',
    component: OtherPage,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
