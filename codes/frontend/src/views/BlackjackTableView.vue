<script setup lang="ts">
import type { BlackjackPlayer, BlackjackTable, Seat } from "@/types/blackjack";
import { useActorsPainter, useCanvasInitializer} from "../components/composables/rendering/canvasUtils";
import { useCounter} from "../components/composables/timing/clock";
import { onMounted, ref, computed, reactive } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useTableStore } from "../stores/tableStore";
import { storeToRefs } from "pinia";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const store = useTableStore();
const { table, command, commandPlayerId, player, counter } = storeToRefs(store);

store.$subscribe((mutation, state) => {
  if (mutation.type === "patch object") {
    console.log("mutate:" + mutation.payload);
    drawTable();
  }
});
onMounted(() => {
  console.log("onMounted");
  canvasReady.value = true;
  useCanvasInitializer(getCanvas());
  drawTable();
});
const takeSeat = (seat: string) => {
  useSend({ action: "JOIN", seat: seat });
};

const bet = (amount: number) => {
  useSend({ action: "BET", amount:amount });
};

const orderedSeatsByNumber = computed<Array<Seat>>(() => {
  return table.value.seats.sort((a, b) => a.number - b.number);
});

const getCanvas = (): HTMLCanvasElement => {
  return document.getElementById("canvas") as HTMLCanvasElement;
};

const clearCanvas = (): HTMLCanvasElement =>{
    const canvas = getCanvas()
    const ctx = canvas.getContext("2d")
    ctx?.clearRect(0, 0, canvas.width, canvas.height)
    return canvas
}
const drawTable = () => {
    console.log("drawTable");
    useActorsPainter(table.value, clearCanvas(), getCenterPlayer());
}

const getCenterPlayer = (): BlackjackPlayer => {
  if (player.value?.name) {
    return player.value;
  }
  const centerPlayer= table.value.seats.find((seat) => seat.player?.balance > 0)?.player as BlackjackPlayer;
  return centerPlayer
}

const seatStyle = (seatNumber:number) => {
    return { 'display': "inline" }
}

</script>

<template>
  <div style="position: relative">
    Table {{ table?.tableCard?.id }} {{ player }} ready:{{ canvasReady }}
    <canvas id="canvas" width="1800" height="600" style="border-style: dashed solid"></canvas>
    <div id="buttonRow">
        <div v-if="canvasReady" v-for="(seat, index) in orderedSeatsByNumber" :key="seat.number" :id="seat.number.toString()" :style =" seatStyle(seat.number)">
            <button v-if="seat.available && !player.seatNumber" @click="takeSeat(seat.number.toString())" >
                Take {{ seat.number + 1 }}
            </button>
        </div>
    </div>
    <div v-if="canvasReady" id="actionRow"> 
        {{ counter }}       
        <button v-if="command === 'BET_PHASE_STARTS' " @click="bet(10)" >
            Bet 50 
        </button>
    </div>
  </div>
</template>
