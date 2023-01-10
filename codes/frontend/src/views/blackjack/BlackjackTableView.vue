<script setup lang="ts">
import { PlayerAction, GamePhase, type BlackjackPlayer, type Seat } from "@/types/blackjack";
import { useActorsPainter, useCanvasInitializer, useInitialDealPainter, useCardsAndHandValuesPainter } from "../../components/composables/rendering/multiSeatPainter";
import { onMounted, onUnmounted, ref, computed, reactive } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useBlackjackStore } from "../../stores/blackjackStore";
import { mapActions, storeToRefs } from "pinia";
import { bgImage, } from "../../components/composables/rendering/images"
import { Command } from "@/types/sockethander";
import { TableType } from "@/types/casino";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const store = useBlackjackStore();
const { table, command, player, counter } = storeToRefs(store);

const unSubscribe = store.$subscribe((mutation, state) => {
    if (mutation.type === "patch object") {
        drawTable(table.value.gamePhase === "PLAY" && command.value === Command.INITIAL_DEAL_DONE);
        if (table.value.gamePhase !== GamePhase.ROUND_COMPLETED) return
        betAmount.value = 0
        const tablePlayer = table.value.seats.find(seat => seat.player?.name === player.value?.name)?.player
        if (tablePlayer)
            previousBetAmount.value = tablePlayer.totalBet
    }

})
const betAmount = ref<number>(0)
const previousBetAmount = ref<number>(0)

onMounted(() => {
    canvasReady.value = true;
    useCanvasInitializer(getCanvas());
    drawTable(false);
});

onUnmounted(() => {
    unSubscribe()
})
const takeSeat = (seat: string) => {
    useSend({ action: "JOIN", seat: seat });
};
const adjustBet = (amount: number) => {
    betAmount.value = amount
    useSend({ action: PlayerAction.BET, amount: amount });
}

const canReduceMinimum = computed<boolean>(() => {
    return betsAllowed.value && betAmount.value - table.value.tableCard.thresholds.minimumBet >= 0
})

const canAddMinimum = computed<boolean>(() => {
    return betsAllowed.value && betAmount.value >= 0
        && betAmount.value + table.value.tableCard.thresholds.minimumBet <= table.value.tableCard.thresholds.maximumBet
        && player.value.balance - (betAmount.value + table.value.tableCard.thresholds.minimumBet) >= 0
})
const canBetMinimum = computed<boolean>(() => {
    return betsAllowed.value
        && player.value.balance >= table.value.tableCard.thresholds.minimumBet
        && betAmount.value !== table.value.tableCard.thresholds.minimumBet
})

const canBetMaximum = computed<boolean>(() => {
    return betsAllowed.value && player.value.balance > table.value.tableCard.thresholds.maximumBet
})

const betsAllowed = computed<boolean>(() => {
    return table.value.gamePhase === GamePhase.BET && player.value.seatNumber >= 0 && counter.value > 0
})

const betPhaseRunning = computed<boolean>(() => {
    return table.value.gamePhase === GamePhase.BET && counter.value > 1
})

const counterVisible = computed<boolean>(() => {
    return counter.value > 1
})

const isTakeSeatRowVisible = computed<boolean>(() => {
    if (table.value.tableCard.type === TableType.SINGLE_PLAYER) {
        return table.value.seats.filter(seat => !seat.available).length === 0
    }
    return true
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
    const ctx = canvas.getContext("2d")
    if (!ctx) return
    ctx.drawImage(bgImage, 0, 0);
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
        <div v-if="isTakeSeatRowVisible" id="takeSeatRow">
            <div v-for="seat in getSeatsDescending" :key="seat.number" :id="seat.number.toString()"
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
            <div> Current bet {{ betAmount }}</div>
            <button :disabled="!canReduceMinimum" @click="adjustBet(betAmount - table.tableCard.thresholds.minimumBet)">
                Reduce {{ table.tableCard.thresholds.minimumBet }}
            </button>
            <button :disabled="!canBetMinimum" @click="adjustBet(table.tableCard.thresholds.minimumBet)">
                Minimum {{ table.tableCard.thresholds.minimumBet }}
            </button>
            <button :disabled="!canBetPrevious" @click="adjustBet(previousBetAmount)">
                Previous {{ previousBetAmount }}
            </button>
            <button :disabled="!canAddMinimum" @click="adjustBet(betAmount + table.tableCard.thresholds.minimumBet)">
                Add {{ table.tableCard.thresholds.minimumBet }}
            </button>
            <button :disabled="!canBetMaximum" @click="adjustBet(table.tableCard.thresholds.maximumBet)">
                Max {{ table.tableCard.thresholds.maximumBet }}
            </button>
            <button :disabled="!hasBet" @click="adjustBet(0)">
                Remove bet
            </button>


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