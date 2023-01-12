<script setup lang="ts">
import { PlayerAction, GamePhase, type BlackjackPlayer, type Seat } from "@/types/blackjack";
import { useActorsPainter, useCanvasInitializer, useInitialDealPainter, useCardsAndHandValuesPainter } from "../../components/composables/rendering/multiplayerTablePainter";
import { onMounted, onUnmounted, ref, computed, reactive } from "vue";
import { useSend } from "@/components/composables/communication/socket/websocket";
import { useBlackjackStore } from "../../stores/blackjackStore";
import { mapActions, storeToRefs } from "pinia";
import { bgImage, } from "../../types/images"
import { Command } from "@/types/sockethander";
import { TableType } from "@/types/casino";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const store = useBlackjackStore();
const { table, command, player, counter } = storeToRefs(store);

const unSubscribe = store.$subscribe((mutation, state) => {
    if (mutation.type === "patch object") {
        onStorePatch()
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
const onStorePatch = () => {
    drawTable(table.value.gamePhase === "PLAY" && command.value === Command.INITIAL_DEAL_DONE);
    if (table.value.gamePhase !== GamePhase.ROUND_COMPLETED) return
    betAmount.value = 0
    const tablePlayer = table.value.seats.find(seat => seat.player?.userName === player.value?.userName)?.player
    if (tablePlayer)
        previousBetAmount.value = tablePlayer.totalBet
}
const takeSeat = (seat: string) => {
    useSend({ action: "JOIN", seat: seat });
}

const counterText = computed<string>(() => {
    if (!counterVisible.value)
        return "not visible"
    if (table.value.gamePhase === GamePhase.ROUND_COMPLETED) {
        return "Next round starts in " + counter.value
    }
    if (betsAllowed.value) {
        return "Bet time left " + counter.value
    }
    if (table.value.gamePhase === GamePhase.INSURE) {
        return "Insurance time left " + counter.value
    }
    if (table.value.gamePhase === GamePhase.BET) {
        return "Place your bets " + counter.value
    }
    return "nope"
})
const adjustBet = (amount: number) => {
    betAmount.value = amount
    useSend({ action: PlayerAction.BET, amount: amount });
    player.value.totalBet = betAmount.value
    drawTable(false)
}

const hasSeat = computed<boolean>(() => {
    return player.value?.seatNumber >= 0
})

const canReduceMinimum = computed<boolean>(() => {
    return betsAllowed.value && betAmount.value - table.value.tableCard.thresholds.minimumBet >= 0
})

const canAddMinimum = computed<boolean>(() => {
    return betsAllowed.value && betAmount.value >= 0
        && betAmount.value + table.value.tableCard.thresholds.minimumBet <= table.value.tableCard.thresholds.maximumBet
        && player.value?.balance - (betAmount.value + table.value.tableCard.thresholds.minimumBet) >= 0
})
const canBetMinimum = computed<boolean>(() => {
    return betsAllowed.value
        && player.value?.balance >= table.value.tableCard.thresholds.minimumBet
        && betAmount.value !== table.value.tableCard.thresholds.minimumBet
})

const canBetMaximum = computed<boolean>(() => {
    return betsAllowed.value && player.value?.balance > table.value.tableCard.thresholds.maximumBet
})

const betsAllowed = computed<boolean>(() => {
    return table.value.gamePhase === GamePhase.BET && hasSeat.value && counter.value > 0
})

const counterVisible = computed<boolean>(() => {
    return counter.value > 1
})

const canTakeSeat = computed<boolean>(() => {
    const tableCard = table.value.tableCard
    if (tableCard.type === TableType.MULTIPLAYER)
        return !hasSeat.value && table.value.seats.map(seat => seat.available) != null
    return tableCard.availablePositions.length >= tableCard.thresholds.seatCount
})

const canAct = computed<boolean>(() => {
    return table.value.gamePhase === 'PLAY' && table.value.playerInTurn.userName === player.value?.userName
})

const canBetPrevious = computed<boolean>(() => {
    return betsAllowed.value && previousBetAmount.value > 0 && player?.value.balance >= previousBetAmount.value
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
    ctx.drawImage(bgImage, 0, 0, canvas.width, canvas.height);
    useActorsPainter(table.value, getCenterPlayer(), canvas);
    if (initialDeal)
        useInitialDealPainter(table.value, getCenterPlayer(), canvas)
    else
        useCardsAndHandValuesPainter(table.value, getCenterPlayer(), canvas)
}

const getCenterPlayer = (): BlackjackPlayer => {
    if (player.value?.userName) {
        return player.value;
    }
    const centerPlayer = table.value.seats.find((seat) => seat.player?.balance > 0)?.player as BlackjackPlayer;
    return centerPlayer
}

const getMainBoxActionRowStyle = (seatNumber: number) => {
    if (table.value.seats.some(seat => seat.player?.seatNumber >= 0)) {
        return { 'display': "inline", "margin-right": "45px", "left": "25px" }
    }
    return { 'display': "inline", 'bottom': "200px", "margin-right": "45px", "left": "25px" }
}

const getMainBoxPlayerActionStyle = () => {
    const liftUp = getCanvas().height / 5 + "px"
    return { 'display': "inline", 'bottom': liftUp, "margin-right": "45px", "left": "25px" }
}

const instructionStyle = computed(() => {
    const bottom = (getCanvas().height / 4).toString() + "px"
    const left = (getCanvas().width / 2).toString() + "px"
    const color = counter.value > 4 ? 'yellow' : 'red'
    return { 'left': left, 'bottom': bottom, "color": color, "font-size": 22 + "px" }
})

const insuranceClicked = ref<boolean>(false)
const insuranceAvailable = computed<boolean>(() => {
    return hasSeat.value && table.value.gamePhase === GamePhase.INSURE
        && player.value.balance >= player.value.totalBet / 2
        && !Number.isInteger(player.value.insuranceAmount)
        && insuranceClicked.value === false
});
</script>

<template v-if="canvasReady">
    <div style="position: relative">
        Welcome {{ player.userName }}
        <template v-if="counterText.length > 0">
            <!--<div :style="instructionStyle"> {{ counterText }}</div> -->
            <div> {{ counterText }}</div>
        </template>
        <canvas id="canvas" width="1800" height="600"></canvas>
        <div v-if="canTakeSeat" id="takeSeatRow">
            <div v-for="seat in getSeatsDescending" :key="seat.number" :id="seat.number.toString()"
                :style="getMainBoxActionRowStyle(seat.number)">
                <button v-if="seat.available" @click="takeSeat(seat.number.toString())">
                    Take seat {{ seat.number + 1 }}
                </button>
            </div>
        </div>
        <div v-if="betsAllowed" id="betRow" :style="getMainBoxPlayerActionStyle()">
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
        <div v-if="table.gamePhase === GamePhase.INSURE" id="insureRow" :style="getMainBoxActionRowStyle">
            <button v-if="insuranceAvailable" @click="insure()">
                Insure
            </button>
        </div>
        <div v-if="canAct" id="actionRow" style="position:relative; bottom:25px: left:50px">
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
    margin: 15px
}
</style>