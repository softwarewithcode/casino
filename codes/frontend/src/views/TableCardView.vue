<script setup lang="ts">
import { TableType, type TableCard } from "@/types/casino";
import { defineComponent } from "vue";
import { useOpenTable } from "@/components/composables/communication/socket/websocket";

const props = defineProps<{ card: TableCard }>();

const openTable = (gameType: string, tableId: string) => {
    useOpenTable(gameType, tableId);
};

const getAvailableSeats = (card: TableCard) => {
    if (card.type === TableType.SINGLE_PLAYER)
        return card.availablePositions.length === card.thresholds.seatCount ? 1 : 0
    return card.availablePositions.length
}
</script>
<template>

    <div class="tableCard" @click="openTable(card.game, card.id)">
        Type: {{ card.type.toLowerCase() }}<br>
        Min: {{ card.thresholds.minimumBet }} € <br>
        Max: {{ card.thresholds.maximumBet }} € <br>
        Available seats: {{ getAvailableSeats(card) }}
    </div>
</template>

<style scoped>
.tableCard {
    border-style: dashed solid;
    cursor: pointer;
}

.tableCard:hover {
    background-color: yellow;
}
</style>