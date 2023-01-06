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

    <div style="border-style: dashed solid">
        Game: {{ card.game }} <br />
        Min: {{ card.thresholds.minimumBet }} € ?<br />
        Max: {{ card.thresholds.maximumBet }} € ? <br />
        Available seats: {{ card.availablePositions.length }} <br />
        <button @click="openTable(card.game, card.id)">Open</button>
    </div>
</template>
