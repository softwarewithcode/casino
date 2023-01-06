<script setup lang="ts">
import { onMounted, onUnmounted, onBeforeMount } from "vue"
import { fetchTables } from "../components/composables/communication/http"
import type { CasinoTable, TableCard } from "../types/casino"
import { useBlackjackStore } from "../stores/blackjackStore"
import TableCardView from "./TableCardView.vue";
import Blackjack from "./TableCardView.vue";

const store = useBlackjackStore()
store.populateStore()

</script>

<template>
    <main>Casino</main>
    <div v-for="(table) in store.getTables" :key="table.id">
        <TableCardView :card="table" />
    </div>
</template>
