<script setup lang="ts">
import type { TableDescription } from "@/types/casino"
import { defineComponent } from "vue"
import { createSocket,send } from "@/components/composables/communication/socket/websocket"
const props = defineProps<{
  desc:TableDescription
}>()

const takeSeat = (seat:string) =>{
    send({action:"JOIN", seat:seat})
}
</script>

<template>
    TABLE {{  desc.availablePositions }}

    <div style="border-style: dashed solid">
        <div v-for="position in desc.availablePositions">
            <button>Take seat {{position}}</button>
        </div>
    
    </div>
</template>
