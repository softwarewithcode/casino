<script setup lang="ts">
import { PlayerAction, GamePhase, type BlackjackPlayer, type Seat } from "@/types/blackjack";
import { useActorsPainter, useCanvasInitializer, useInitialDealPainter, useCardsAndHandValuesPainter } from "../components/composables/rendering/canvasUtils";
import { onMounted, ref, computed, reactive } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useBlackjackStore } from "../stores/blackjackStore";
import { mapActions, storeToRefs } from "pinia";
import { Command } from "@/types/sockethander";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const store = useBlackjackStore();
const { table, command, player, counter } = storeToRefs(store);

store.$subscribe((mutation, state) => {
    if (mutation.type === "patch object") {
        drawTable(table.value.gamePhase === "PLAY" && command.value === Command.INITIAL_DEAL_DONE);
    }
})
const betAmount = ref<number>(0)

onMounted(() => {
    canvasReady.value = true;
    useCanvasInitializer(getCanvas());
    drawTable(false);
});
const takeSeat = (seat: string) => {
    useSend({ action: "JOIN", seat: seat });
};
const adjustBet = (amount: number) => {
    betAmount.value = amount
    useSend({ action: PlayerAction.BET, amount: amount });
};
const insure = () => {
    insuranceClicked.value = true
    useSend({ action: PlayerAction.INSURE })
}
const sendAction = (action: string) => {
    useSend({ action: action });
};

const getSeatsDescending = computed<Array<Seat>>(() => {
    return table.value.seats.sort((a, b) => a.number - b.number);
});

const getCanvas = (): HTMLCanvasElement => {
    return document.getElementById("canvas") as HTMLCanvasElement;
};

const clearCanvas = (): HTMLCanvasElement => {
    const canvas = getCanvas()
    const ctx = canvas.getContext("2d")
    ctx?.clearRect(0, 0, canvas.width, canvas.height)
    return canvas
}

const drawTable = async (initialDeal: boolean) => {
    const canvas: HTMLCanvasElement = clearCanvas()
    useActorsPainter(table.value, getCenterPlayer(), canvas);
    if (initialDeal)
        useInitialDealPainter(table.value, getCenterPlayer(), canvas)
    else
        useCardsAndHandValuesPainter(table.value, getCenterPlayer(), canvas)

}

const getCenterPlayer = (): BlackjackPlayer => {
    if (player.value?.name) {
        return player.value;
    }
    const centerPlayer = table.value.seats.find((seat) => seat.player?.balance > 0)?.player as BlackjackPlayer;
    return centerPlayer
}

const seatStyle = (seatNumber: number) => {
    return { 'display': "inline", 'bottom': "200px", "margin-right": "45px", "left": "50px" }
}

const insuranceClicked = ref<boolean>(false)
const insuranceAvailable = computed<boolean>(() => {
    return player.value.seatNumber >= 0 && table.value.gamePhase === GamePhase.INSURE
        && player.value.balance >= player.value.totalBet / 2 && !Number.isInteger(player.value.insuranceAmount) && insuranceClicked.value === false
});
</script>

<template v-if="canvasReady">
    <div style="position: relative">
        Table {{ table?.tableCard?.id }} {{ table.gamePhase }}{{ table.playerInTurn?.name }}
        <canvas id="canvas" width="1800" height="600" style="border-style: dashed solid"></canvas>
        <div id="buttonRow">
            <div v-for="(seat, index) in getSeatsDescending" :key="seat.number" :id="seat.number.toString()"
                :style="seatStyle(seat.number)">
                <button v-if="seat.available && !Number.isInteger(player.seatNumber)"
                    @click="takeSeat(seat.number.toString())">
                    Take {{ seat.number + 1 }}
                </button>
            </div>
        </div>
        <div v-if="table.gamePhase === GamePhase.BET" id="betRow">
            <template v-if="counter >= 0">
                Bet time left {{ counter }}
            </template>
            <button v-if="player.seatNumber >= 0 && betAmount > 0" @click="adjustBet(betAmount * 0.1)">
                - -
            </button>
            <button v-if="player.seatNumber >= 0 && betAmount > 0" @click="adjustBet(betAmount * 0.05)">
                -
            </button>
            <button v-if="player.seatNumber >= 0 && player.balance > table.tableCard.thresholds.minimumBet"
                @click="adjustBet(table.tableCard.thresholds.minimumBet)">
                Bet min ({{ table.tableCard.thresholds.minimumBet }})
            </button>
            <button
                v-if="player.seatNumber >= 0 && Number.isInteger(player.lastBet) && player.balance >= player.lastBet"
                @click="adjustBet(5)">
                Bet previous ({{ player.lastBet }})
            </button>
            <button v-if="player.seatNumber >= 0 && player.balance >= table.tableCard.thresholds.maximumBet"
                @click="adjustBet(table.tableCard.thresholds.maximumBet)">
                Bet max ({{ table.tableCard.thresholds.maximumBet }} )
            </button>
            <button
                v-if="player.seatNumber >= 0 && betAmount > 0 && betAmount * 1.1 <= table.tableCard.thresholds.maximumBet"
                @click="adjustBet(betAmount * 1.1)">
                +
            </button>
            <button
                v-if="player.seatNumber >= 0 && betAmount > 0 && betAmount < table.tableCard.thresholds.maximumBet && player.balance >= betAmount"
                @click="adjustBet(betAmount * 1.05)">
                + +
            </button>
            <button v-if="player.seatNumber >= 0" @click="adjustBet(0)">
                Remove bet
            </button>
            Current bet {{ betAmount }}
        </div>
        <div v-if="table.gamePhase === GamePhase.INSURE" id="insureRow">
            Insurance time left {{ counter }}
            <button v-if="insuranceAvailable" @click="insure()">
                Insure
            </button>
        </div>
        <div v-if="table.gamePhase === GamePhase.ROUND_COMPLETED" id="betRoundStartsRow">
            Next bet round starts {{ counter }}
        </div>
        <div v-if="table.gamePhase === 'PLAY' && table.playerInTurn.name === player.name" id="actionRow"
            style="position:relative; bottom:25px: left:50px">
            Player {{ table?.playerInTurn.name }} {{ counter }}
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.TAKE)"
                @click="sendAction(PlayerAction.TAKE)">
                Take
            </button>
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.SPLIT)"
                @click="sendAction(PlayerAction.SPLIT)">
                Split
            </button>
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.DOUBLE_DOWN)"
                @click="sendAction(PlayerAction.DOUBLE_DOWN)">
                Double down
            </button>
            <button v-if="table.playerInTurn.actions.includes(PlayerAction.STAND)"
                @click="sendAction(PlayerAction.STAND)">
                Stand
            </button>
        </div>
    </div>
</template>
<style scoped>
button {
    margin: 20px
}
</style>