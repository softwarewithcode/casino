
<script setup lang="ts">
import TableCardView from "../TableCardView.vue";
import { useTexasHoldemStore } from "../../stores/texasHoldemStore";

const store = useTexasHoldemStore();
store.populateStore()

</script>

<template>
    <h1>Holdem cash game tables</h1><br>
    <div v-for="(tableCard) in store.tableCards" :key="tableCard.id">
        <TableCardView :card="tableCard">
            {{ tableCard.gameData.betType.toLowerCase() }} <br>
            MinBuyIn: {{ tableCard.gameData.minBuyIn }} <br>
            MaxBuyIn: {{ tableCard.gameData.maxBuyIn }} <br>
            Rake: {{ tableCard.gameData.rakePercent * 100 }}% / {{ tableCard.gameData.rakeCap }} <br>
            Players: {{ tableCard.thresholds.maxPlayers - tableCard.availablePositions.length }}/{{
                tableCard.thresholds.maxPlayers }}
            <br>

        </TableCardView>
    </div>
</template>