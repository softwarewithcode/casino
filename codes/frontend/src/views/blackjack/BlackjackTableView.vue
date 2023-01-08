<script setup lang="ts">
import { PlayerAction, GamePhase, type BlackjackPlayer, type Seat } from "@/types/blackjack";
import { useActorsPainter, useCanvasInitializer, useInitialDealPainter, useCardsAndHandValuesPainter } from "../../components/composables/rendering/canvasUtils";
import { onMounted, ref, computed, reactive } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useBlackjackStore } from "../../stores/blackjackStore";
import { mapActions, storeToRefs } from "pinia";
import { Command } from "@/types/sockethander";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const store = useBlackjackStore();
const { table, command, player, counter } = storeToRefs(store);

store.$subscribe((mutation, state) => {
    if (mutation.type === "patch object") {
        drawTable(table.value.gamePhase === "PLAY" && command.value === Command.INITIAL_DEAL_DONE);
        if (table.value.gamePhase === GamePhase.ROUND_COMPLETED) {
            betAmount.value = 0
            // const player = table.value.seats.find(seat => seat.player?.name === player.value?.name)?.player
            // if (player)
            //     previousBetAmount.value = player.totalBet
        }
    }
})
const betAmount = ref<number>(0)
const previousBetAmount = ref<number>(-1)

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
}

const canBetMinimum = computed<boolean>(() => {

    return betsAllowed.value && player.value.balance > table.value.tableCard.thresholds.minimumBet
})

const canIncreaseMinimum = computed<boolean>(() => {
    return betsAllowed.value && betAmount.value > 0 && table.value.tableCard.thresholds.minimumBet + betAmount.value <= table.value.tableCard.thresholds.maximumBet
})
const canBetMaximum = computed<boolean>(() => {
    return betsAllowed.value && player.value.balance > table.value.tableCard.thresholds.maximumBet && counter.value > 1
})

const betsAllowed = computed<boolean>(() => {
    return table.value.gamePhase === GamePhase.BET && player.value.seatNumber >= 0
})

const betPhaseRunning = computed<boolean>(() => {
    return table.value.gamePhase === GamePhase.BET && counter.value > 1
})

const counterVisible = computed<boolean>(() => {
    return counter.value > 1
})

const canBetPrevious = computed<boolean>(() => {
    return betsAllowed.value && previousBetAmount.value != -1 && player.value.balance >= previousBetAmount.value
})
const hasBet = computed<boolean>(() => {
    return betAmount.value > 0
})
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
const instructionStyle = computed(() => {
    const bottom = (getCanvas().height / 2).toString() + "px"
    const left = (getCanvas().width / 2).toString() + "px"
    const color = counter.value > 5 ? 'green' : 'red'
    return { 'left': left, 'bottom': bottom, "color": color }
})

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
        <div v-if="betPhaseRunning" id="betRow">
            <template v-if="counterVisible">
                <span :style="instructionStyle"> Bet time left {{ counter }}</span>
            </template>

            <button :disabled="!canBetMinimum" @click="adjustBet(table.tableCard.thresholds.minimumBet)">
                Bet min ({{ table.tableCard.thresholds.minimumBet }})
            </button>
            <button :disabled="!canBetPrevious" @click="adjustBet(previousBetAmount)">
                Bet previous ({{ player.lastBet }})
            </button>
            <button :disabled="!canBetMaximum" @click="adjustBet(table.tableCard.thresholds.maximumBet)">
                Bet max ({{ table.tableCard.thresholds.maximumBet }} )
            </button>
            <button :disabled="!hasBet" @click="adjustBet(0)">
                Remove bet
            </button>
            <button :disabled="canIncreaseMinimum"
                @click="adjustBet(betAmount + table.tableCard.thresholds.minimumBet)">
                Increase by minimum{{ table.tableCard.thresholds.minimumBet }}
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