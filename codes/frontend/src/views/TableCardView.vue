<script setup lang="ts">
import { TableType, type TableCard } from "@/types/casino";
import { useOpenTable } from "@/components/composables/communication/socket/websocket";

const props = defineProps<{ card: TableCard }>();

const openTable = (gameType: string, tableId: string) => {
    useOpenTable(gameType, tableId);
};

const getStatusText = (card: TableCard) => {
    // i18n becomes more difficult!
    if (card.type === TableType.MULTIPLAYER)
        return card.availablePositions.length > 0 ? "Available seats " + card.availablePositions.length : "Full"
    return card.availablePositions.length === card.thresholds.seatCount ? "Available seats 1" : "Full"
}
</script>
<template>

    <div class="tableCard" @click="openTable(card.game, card.id)">
        Type: {{ card.type.toLowerCase() }}<br>
        Min: {{ card.thresholds.minimumBet }} € <br>
        Max: {{ card.thresholds.maximumBet }} € <br>
        {{ getStatusText(card) }} <br>
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