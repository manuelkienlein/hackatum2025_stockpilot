<script setup lang="ts">

import {SidebarInset, SidebarProvider, SidebarTrigger} from "@/components/ui/sidebar";
import {
  Breadcrumb, BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator
} from "@/components/ui/breadcrumb";
import AppSidebar from "@/components/AppSidebar.vue";
import {Separator} from "@/components/ui/separator";
</script>

<template>
  <SidebarProvider>
    <AppSidebar />
    <SidebarInset>
      <header class="flex h-16 shrink-0 items-center gap-2 border-b px-4">
        <SidebarTrigger class="-ml-1" />
        <Separator orientation="vertical" class="mr-2 h-4" />
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem class="hidden md:block">
              <BreadcrumbLink href="#">
                StockPilot
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator class="hidden md:block" />
            <BreadcrumbItem>
              <BreadcrumbPage>Seite</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
      </header>
      <div class="flex flex-1 flex-col gap-4 p-4">
        <div class="min-h-[100vh] flex-1 rounded-xl bg-muted/50 md:min-h-min p-8">
          <main class="flex-1 overflow-auto p-6">
            <router-view />
          </main>
        </div>
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>

<style scoped>

</style>