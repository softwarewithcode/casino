<script setup lang="ts">
import type { TableDescription } from "@/types/casino"
import { defineComponent } from "vue"
import { createSocket,send } from "@/components/composables/communication/socket/websocket"
const props = defineProps<{
  description:TableDescription
}>()

const takeSeat = (seat:string) =>{
    send({action:"JOIN", seat:seat})
}
</script>

<template>
TABLE
</template>
