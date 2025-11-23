<script setup lang="ts">
import {
  Sidebar,
  SidebarHeader,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarFooter
} from "@/components/ui/sidebar"

import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu"

import {
  User2,
  ChevronUp,
  BarChart3,
  LineChart,
  ListChecks,
  ShoppingCart,
  ChartNoAxesCombined, Warehouse,
} from "lucide-vue-next"

const data = {
  navMain: [
    {
      title: "My Account",
      url: "#",
      items: [
        {
          title: "Dashboard",
          url: "/dashboard",
          icon: BarChart3
        },
        {
          title: "Net Worth",
          url: "/net-worth",
          icon: ChartNoAxesCombined
        },
      ],
    },
    {
      title: "Trading",
      url: "#",
      items: [
        {
          title: "Portfolio",
          url: "/portfolio",
          icon: Warehouse,
        },
        {
          title: "Order",
          url: "/orders",
          icon: ShoppingCart,
        },
      ],
    },
    {
      title: "Analytics",
      url: "#",
      items: [
        {
          title: "Stocks",
          url: "/stocks",
          icon: LineChart
        },
        {
          title: "Watchlist",
          url: "/watchlist",
          icon: ListChecks
        },
      ],
    },
  ],
}
</script>

<template>
  <Sidebar>
    <SidebarHeader />

    <SidebarContent>
      <SidebarGroup v-for="item in data.navMain" :key="item.title">
        <SidebarGroupLabel>{{ item.title }}</SidebarGroupLabel>

        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem
                v-for="childItem in item.items"
                :key="childItem.title"
            >
              <SidebarMenuButton as-child :is-active="childItem.isActive">
                <a
                    :href="childItem.url"
                    class="flex items-center gap-2"
                >
                  <!-- Icon -->
                  <component
                      :is="childItem.icon"
                      class="w-4 h-4 text-muted-foreground"
                  />

                  <!-- Title -->
                  {{ childItem.title }}
                </a>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter>
      <SidebarMenu>
        <SidebarMenuItem>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <SidebarMenuButton variants="secondary">
                <User2 class="w-4 h-4" /> Username
                <ChevronUp class="ml-auto w-4 h-4" />
              </SidebarMenuButton>
            </DropdownMenuTrigger>

            <DropdownMenuContent
                side="top"
                class="w-[--reka-popper-anchor-width]"
            >
              <DropdownMenuItem>
                <span>Sign out</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarFooter>
  </Sidebar>
</template>
