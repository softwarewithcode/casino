
<script setup lang="ts">
import TableCardView from "../TableCardView.vue";
import { useRouletteStore } from "../../stores/rouletteStore";

const store = useRouletteStore();
store.populateStore()

</script>

<template>
    <h1>Roulette tables</h1><br>
    <div v-for="(tableCard) in store.getTableCards" :key="tableCard.id">
        <TableCardView :card="tableCard">
            <br>
            MinBet: {{ tableCard.gameData.minBet }} <br>
            MaxBet: {{ tableCard.gameData.maxBet }} <br>
            Players: {{ tableCard.thresholds.maxPlayers - tableCard.availablePositions.length }}/{{
                tableCard.thresholds.maxPlayers }}
            <br>
        </TableCardView>
    </div>
</template>