
<script setup lang="ts">
import TableCardView from "../TableCardView.vue";
import { useBlackjackStore } from "../../stores/blackjackStore";

const store = useBlackjackStore();
store.populateStore()

</script>

<template>
    <h1>Blackjack tables</h1><br>
    <div v-for="(table) in store.getTables" :key="table.id">
        <TableCardView :card="table">
            Type: {{ table.type.toLowerCase() }}<br>
            Min: {{ table.gameData.minimumBet }} <br>
            Max: {{ table.gameData.maximumBet }} <br>
            Turn time {{ table.gameData.playerTime }}s <br>
        </TableCardView>
    </div>
</template>
