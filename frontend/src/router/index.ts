import { createRouter, createWebHistory } from 'vue-router'

// Importiere deine Seitenkomponenten
import PageView from '@/views/PageView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import StockDetailsView from "../views/StockDetailsView.vue";
import AccountLayout from "../layouts/AccountLayout.vue";
import PortfolioView from "../views/PortfolioView.vue";
import OrderView from "../views/OrderView.vue";
import StockListView from "../views/StockListView.vue";
import WatchlistView from "../views/WatchlistView.vue";
import NetWorthView from "../views/NetWorthView.vue";

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Dashboard', component: LoginView },
    { path: '/register', name: 'Register', component: RegisterView },
    { path: '/settings', name: 'Settings', component: PageView },
    { path: '/profile', name: 'Profile', component: PageView },
    { path: '/pages', name: 'PageViewEmpty', component: PageView },
    { path: '/pages/:id', name: 'PageView', component: PageView },
    { name: 'Account', component: AccountLayout,
        meta: {
            breadcrumb: "StockPilot",
        },
        children: [
            {
                path: '/stocks/:symbol',
                name: 'StockDetailsView',
                component: StockDetailsView,
                meta: {
                    breadcrumb: (route) => `Stock ${route.params.symbol}`,
                },
            },
            {
                path: '/stocks',
                name: 'StockListView',
                component: StockListView,
                meta: {
                    breadcrumb: "Stocks",
                },
            },
            {
                path: '/portfolio',
                name: 'PortfolioView',
                component: PortfolioView,
                meta: {
                    breadcrumb: "Portfolio",
                },
            },
            {
                path: '/orders',
                name: 'OrderView',
                component: OrderView,
                meta: {
                    breadcrumb: "Orders",
                },
            },
            {
                path: '/watchlist',
                name: 'WatchlistView',
                component: WatchlistView,
                meta: {
                    breadcrumb: "Watchlist",
                },
            },
            {
                path: '/networth',
                name: 'NetworthView',
                component: NetWorthView,
                meta: {
                    breadcrumb: "Net Worth",
                },
            },
        ]
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router