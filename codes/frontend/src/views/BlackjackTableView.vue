<script setup lang="ts">
import type { BlackjackPlayer, BlackjackTable } from "@/types/blackjack";
import { onMounted, ref, computed } from "vue";
import  {useSend}  from "@/components/composables/communication/socket/websocket";
import { useTableStore}  from "../stores/tableStore";
import { storeToRefs } from "pinia";

const props = defineProps<{ tableId: string }>();
const store = useTableStore();
const { table, command, commandPlayerId, me } = storeToRefs(store);

onMounted(() => {});
const takeSeat = (seat: string) => {
  useSend({ action: "JOIN", seat: seat });
};

let player: BlackjackPlayer | undefined;

const iHaveSeat = computed<boolean>(() => {
    console.log("seat:"+me.value?.seatNumber)
  return me.value?.seatNumber >= 0;
})

const isPhase = (phase: String) => {
  console.log("phasse:" + table.value.title); 
  return table.value.title === phase;
}
</script>

<template>
    Table {{ table.tableCard.id }}
    <div style=" border-style: dashed solid; height: 200px;  width: 700px;  display: flex; "  >
      <div id="players" style="align-self: flex-end">
        <span v-for="seat in table.seats" :key="seat.number">
          <span v-if="!iHaveSeat && seat.available">
            <button @click="takeSeat(seat.number.toString())">
              Take seat {{ seat.number }}
            </button>
          </span>
          <span v-else-if="!seat.available">
              {{seat.player.name}} <br>
              Money:{{seat.player.balance}} <br>
              Bet: {{seat.player.totalBet}} <br>
          </span>
        </span>
      </div>
    </div>
  </template>
