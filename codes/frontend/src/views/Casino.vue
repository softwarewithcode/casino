<script setup lang="ts">
import { onMounted, onUnmounted, onBeforeMount } from "vue"
import { fetchTables } from "../components/composables/communication/http"
import type { CasinoTable, TableCard } from "../types/casino"
import {useTableStore} from "../stores/tableStore"
import TableCardView from "./TableCardView.vue";
import Blackjack from "./TableCardView.vue";

const store = useTableStore()
store.populateStore()

</script>

<template>
    <main>Casino</main>
    <div v-for="(table) in store.getTables" :key="table.id">
        <TableCardView :card = "table" />
    </div>
</template>
