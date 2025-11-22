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

import { User2, ChevronUp, } from "lucide-vue-next"


const data = {
  navMain: [
    {
      title: "My Account",
      url: "#",
      items: [
        {
          title: "Dashboard",
          url: "#",
        },
        {
          title: "Net Worth",
          url: "#",
        },
      ],
    },
    {
      title: "Trading",
      url: "#",
      items: [
        {
          title: "Portfolio",
          url: "#",
        },
        {
          title: "Order",
          url: "#",
        },
      ],
    },
    {
      title: "Analytics",
      url: "#",
      items: [
        {
          title: "Stocks",
          url: "#",
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
                <SidebarMenuItem v-for="childItem in item.items" :key="childItem.title">
                  <SidebarMenuButton as-child :is-active="childItem.isActive">
                    <a :href="childItem.url">{{ childItem.title }}</a>
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
                                <User2 /> Username
                                <ChevronUp class="ml-auto" />
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