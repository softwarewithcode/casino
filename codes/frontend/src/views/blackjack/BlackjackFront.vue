
<script setup lang="ts">
import TableCardView from "../TableCardView.vue";
import { useBlackjackStore } from "../../stores/blackjackStore";

const store = useBlackjackStore();
store.populateStore()

</script>

<template>
    <h1>Blackjack tables</h1><br>
    <div v-for="(table) in store.getTables" :key="table.id">
        <TableCardView :card="table" />
    </div>

</template>
