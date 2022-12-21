<script setup lang="ts">
import { onMounted, onUnmounted, onBeforeMount } from "vue"
import { fetchTables } from "../components/composables/communication/http"
import type { CasinoTable, TableDescription } from "../types/casino"
import {tableStore} from "../stores/tableStore"
import TableDescriptionView from "../views/TableDescriptionView.vue";
import Blackjack from "./TableDescriptionView.vue";

const store = tableStore()
store.populateStore()

</script>

<template>
    <main>Casino</main>
    <div v-for="(table, index) in store.getTables" :key="index">
        <TableDescriptionView :description="table" />
    </div>
</template>
