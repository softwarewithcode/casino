<script setup lang="ts">
import type { BlackjackPlayer, BlackjackTable } from "@/types/blackjack";
import { onMounted, ref } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useTableStore } from "../stores/tableStore";
import { storeToRefs } from "pinia";

const props = defineProps<{ tableId: string }>();
const store = useTableStore();
const { table } = storeToRefs(store);
const tbl = ref<BlackjackTable>();

const takeSeat = (seat: string) => {
  useSend({ action: "JOIN", seat: seat });
};

let player: BlackjackPlayer | undefined;
const getPlayerFromSeat = (seatNumber: number) => {
  player = table.value.players.find((player) => player.seatNumber === seatNumber
  );
  return player;
};
const isPhase = (phase: String) => {
  console.log("phase:" + table.value.title);
  return table.value.title === phase;
};
</script>

<template>
  Table {{ table.tableCard.id }}
  <div
    style="
      border-style: dashed solid;
      height: 200px;
      width: 700px;
      display: flex;
    "
  >
    <div id="players" style="align-self: flex-end">
      <span v-for="index in table.tableCard.thresholds.seatCount" :key="index">
        <span v-if="!getPlayerFromSeat(index)">
          <button @click="takeSeat(index.toString())">
            Take seat {{ index }}
          </button>
        </span>
        <span v-else>
          Player {{ player?.name }} balance: {{ player?.balance }}
          <span v-if="isPhase('BET_PHASE_STARTS')">
            BetPhase. Show coins, slider or <input type="text" id="q" />
          </span>
        </span>
      </span>
    </div>
  </div>
</template>
