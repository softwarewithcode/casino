<script setup lang="ts">
import type { TableCard } from "@/types/casino";
import { defineComponent } from "vue";
import { useOpenTable } from "@/components/composables/communication/socket/websocket";

const props = defineProps<{ card: TableCard }>();

const openTable = (gameType: string, tableId: string) => {
    useOpenTable(gameType, tableId);
};
</script>
<template>

    <div class="tableCard" @click="openTable(card.game, card.id)">
        Min: {{ card.thresholds.minimumBet }} € ?<br />
        Max: {{ card.thresholds.maximumBet }} € ? <br />
        Available seats: {{ card.availablePositions.length }}
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