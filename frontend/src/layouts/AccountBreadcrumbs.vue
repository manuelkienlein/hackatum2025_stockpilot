<template>
  <div v-if="crumbs.length">
    <Breadcrumb>
      <BreadcrumbList>
        <template
            v-for="(crumb, index) in crumbs"
            :key="index"
        >
          <BreadcrumbItem>
            <!-- alle bis auf den letzten sind Links -->
            <BreadcrumbLink
                v-if="index < crumbs.length - 1 && crumb.to"
                :to="crumb.to"
                class="cursor-pointer"
            >
              {{ crumb.label }}
            </BreadcrumbLink>
            <BreadcrumbLink
                v-else-if="index < crumbs.length - 1"
                class="text-muted-foreground cursor-default"
            >
              {{ crumb.label }}
            </BreadcrumbLink>

            <!-- letzter Eintrag ist die aktuelle Seite -->
            <BreadcrumbPage v-else>
              {{ crumb.label }}
            </BreadcrumbPage>
          </BreadcrumbItem>

          <BreadcrumbSeparator v-if="index < crumbs.length - 1" />
        </template>
      </BreadcrumbList>
    </Breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

import {
  Breadcrumb,
  BreadcrumbList,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";

// Typ, wie deine Route-Meta Breadcrumbs definieren kann
// meta: { breadcrumb: "Portfolio" }
// oder: meta: { breadcrumb: (route) => `Position ${route.params.symbol}` }
type CrumbTo =
    | string
    | {
  name?: string | symbol;
  path?: string;
  params?: Record<string, any>;
  query?: Record<string, any>;
};

interface Crumb {
  label: string;
  to?: CrumbTo;
}

const route = useRoute();

const crumbs = computed<Crumb[]>(() => {
  const result: Crumb[] = [];

  route.matched.forEach((record, index, all) => {
    const metaCrumb = record.meta?.breadcrumb as
        | string
        | false
        | ((r: typeof route) => string | Crumb | null)
        | Crumb
        | undefined;

    // breadcrumb explizit ausblenden
    if (metaCrumb === false) return;

    let crumb: Crumb | null = null;

    if (typeof metaCrumb === "string") {
      crumb = {
        label: metaCrumb,
      };
    } else if (typeof metaCrumb === "function") {
      const res = metaCrumb(route);
      if (!res) return;

      if (typeof res === "string") {
        crumb = { label: res };
      } else {
        crumb = res;
      }
    } else if (metaCrumb && typeof metaCrumb === "object") {
      crumb = metaCrumb;
    } else if (record.meta?.title) {
      // Fallback: title aus meta
      crumb = { label: String(record.meta.title) };
    } else if (record.name) {
      crumb = { label: String(record.name) };
    }

    if (!crumb) return;

    const isLast = index === all.length - 1;

    // Nur nicht-letzte Crumbs klickbar machen
    if (!isLast && !crumb.to) {
      // Standard: Route-Pfad mit Parametern
      crumb.to = { name: record.name as any, params: route.params, query: route.query };
    }

    result.push(crumb);
  });

  return result;
});
</script>