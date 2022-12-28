<script setup lang="ts">
import { PlayerAction, type BlackjackPlayer, type Seat } from "@/types/blackjack";
import { useActorsPainter, useCanvasInitializer, useInitialDealPainter,useCardsAndHandValuesPainter} from "../components/composables/rendering/canvasUtils";
import { useCounterStart} from "../components/composables/timing/clock";
import { onMounted, ref, computed, reactive } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useTableStore } from "../stores/tableStore";
import { mapActions, storeToRefs } from "pinia";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const store = useTableStore();
const { table, command, commandPlayerId, player, counter } = storeToRefs(store);

        store.$subscribe((mutation, state) => {
        if (mutation.type === "patch object" ) {
            console.log("mutate:" + JSON.stringify(mutation.payload));
            drawTable(false);
        } 
        });

        store.$onAction(({ name, store, args, after, onError,  }) => {
                console.log(`OnAction name `+name +" mapActions "+mapActions.name )
                if(name ==="showInitialDeal"){
                    console.log("SHOW_INITIAL_DEAL")
                    drawTable(true)
                }
            }
        )
        onMounted(() => {
            console.log("onMounted");
            canvasReady.value = true;
            useCanvasInitializer(getCanvas());
            drawTable(false);
        });
        const takeSeat = (seat: string) => {
            useSend({ action: "JOIN", seat: seat });
        };

        const bet = (amount: number) => {
            useSend({ action: "BET", amount:amount });
        };
        const sendAction = (action: string) => {
            useSend({ action: action});
        };
   
        

const showInitialDeal = () => {

}

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
const drawTable = async (initialDeal:boolean)  => {
    console.log("drawTable");
    const canvas:HTMLCanvasElement = clearCanvas()
    useActorsPainter(table.value, getCenterPlayer(),canvas, );
    if(initialDeal){
        useInitialDealPainter(table.value, getCenterPlayer(), canvas)
    }else{
        useCardsAndHandValuesPainter(table.value, getCenterPlayer(),canvas)
    }
   
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

<template v-if="canvasReady">

    <div style="position: relative">
        Table {{ table?.tableCard?.id }} {{ table.gamePhase }}{{ table.playerInTurn?.name }}
        <canvas id="canvas" width="1800" height="600" style="border-style: dashed solid"></canvas>
        <div id="buttonRow">
            <div v-for="(seat, index) in orderedSeatsByNumber" :key="seat.number" :id="seat.number.toString()"
                :style=" seatStyle(seat.number)">
                <button v-if="seat.available && !Number.isInteger(player.seatNumber)"
                    @click="takeSeat(seat.number.toString())">
                    Take {{ seat.number + 1 }}
                </button>
            </div>
        </div>
        <div v-if=" player.seatNumber>=0 && table.gamePhase === 'BET' " id="betRow">
            {{ counter }}
            <button @click="bet(1)">
                Bet 1
            </button>
            <button @click="bet(5)">
                Bet 5
            </button>
            <button @click="bet(15)">
                Bet 15
            </button>
        </div>
        <div style="position:relative; bottom:25px: left:50px"
            v-if="table.gamePhase === 'PLAY' && table.playerInTurn.name === player.name " id="actionRow">
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.TAKE.toString())"
                @click="sendAction(PlayerAction.TAKE.toString())">
                Take
            </button>
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.SPLIT.toString())"
                @click="sendAction(PlayerAction.SPLIT.toString())">
                Split
            </button>
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.DOUBLE_DOWN.toString())"
                @click="sendAction(PlayerAction.DOUBLE_DOWN.toString())">
                Double down
            </button>
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.STAND.toString())"
                @click="sendAction(PlayerAction.STAND.toString())">
                Stand
            </button>
        </div>
    </div>
</template>
