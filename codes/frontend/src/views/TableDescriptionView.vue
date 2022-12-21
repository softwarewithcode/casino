<script setup lang="ts">
import type { TableDescription } from "@/types/casino"
import { defineComponent } from "vue"
import { createSocket,send } from "@/components/composables/communication/socket/websocket"
import { useRouter, useRoute } from 'vue-router'
const props = defineProps<{
  description:TableDescription
}>()
const router = useRouter()
const openTable = (tableId:string) => {
    createSocket(tableId)
 
    router.push({ name: 'blackjack', params: { id: tableId} })
}
</script>
<template>
    Description
    <div style="border-style: dashed solid">
        Game: {{ description.game}} <br>
        Min: {{ description.thresholds.minimumBet}} € ?<br>
        Max: {{ description.thresholds.maximumBet}} € ? <br>
        Available seats: {{ description.availablePositions.length}} <br>
        <button @click="openTable(description.id)"> Open</button>
    </div>
</template>