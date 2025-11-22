import { createRouter, createWebHistory } from 'vue-router'

// Importiere deine Seitenkomponenten
import PageView from '@/views/PageView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import StockDetailsView from "../views/StockDetailsView.vue";
import AccountLayout from "../layouts/AccountLayout.vue";

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Dashboard', component: LoginView },
    { path: '/register', name: 'Register', component: RegisterView },
    { path: '/settings', name: 'Settings', component: PageView },
    { path: '/profile', name: 'Profile', component: PageView },
    { path: '/pages', name: 'PageViewEmpty', component: PageView },
    { path: '/pages/:id', name: 'PageView', component: PageView },
    { name: 'Account', component: AccountLayout, children: [{
        path: '/stocks/:id',
            name: 'StockDetailsView',
            component: StockDetailsView,
        }] },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router