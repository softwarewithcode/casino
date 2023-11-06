<script setup lang="ts">
import { PlayerAction, GamePhase, type BlackjackPlayer } from "@/types/blackjack";
import { TableType, type Seat } from "@/types/casino";
import { useActorsPainter, useInitialDealPainter, useCardsAndHandValuesPainter } from "../../components/blackjack/blackjackTablePainter";
import { useCanvasInitializer, useCanvasClearer } from "../../components/composables/rendering/commonPainter";
import { onMounted, onUnmounted, ref, computed, reactive } from "vue";
import { useSocketSend, useSocketClose } from "@/components/composables/communication/socket/websocket";
import { useBlackjackStore } from "../../stores/blackjackStore";
import { mapActions, storeToRefs } from "pinia";
import { useActivePlayerChecker, useDescendingSeatsSorter, useGameCanvasFinder, useSeatChecker, useHeroFinder } from "@/components/composables/common/table"
import { bgImage, } from "../../types/images"
import { ServerCommand } from "@/types/servercommands";
import TakeSeatButtonRow from "../TakeSeatButtonRow.vue";
import { useCasinoStore } from "@/stores/casinoStore";
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const blackjackStore = useBlackjackStore();
const casinoStore = useCasinoStore();
const { table, command, player } = storeToRefs(blackjackStore);
const { counter } = storeToRefs(casinoStore);
const unSubscribe = blackjackStore.$subscribe((mutation, state) => {
    if (mutation.type === "patch object") {
        onStorePatch()
    }
})
const betAmount = ref<number>(0)
const previousBetAmount = ref<number>(0)

onMounted(() => {
    canvasReady.value = true;
    useCanvasInitializer(useGameCanvasFinder());
    drawTable(false);
});

onUnmounted(() => {
    unSubscribe()
    console.log("Closing:")
    useSocketClose()
    blackjackStore.$reset()
})
const onStorePatch = () => {
    drawTable(table.value.gamePhase === "PLAY" && command.value === ServerCommand.INITIAL_DEAL_DONE);
    if (table.value.gamePhase !== GamePhase.ROUND_COMPLETED) return
    betAmount.value = 0
    const tablePlayer = table.value.seats.find(seat => seat.player?.userName === player.value?.userName)?.player
    if (tablePlayer)
        previousBetAmount.value = tablePlayer.totalBet > table.value.tableCard.gameData.maxBet ? table.value.tableCard.gameData.maxBet : tablePlayer.totalBet
}
const counterText = computed<string>(() => {
    if (counter.value <= 0 || !canvasReady.value)
        return ""
    if (table.value.gamePhase === GamePhase.ROUND_COMPLETED)
        return "Next round starts in " + counter.value

    if (table.value.gamePhase === GamePhase.INSURE)
        return "Insurance time left " + counter.value

    if (command.value === ServerCommand.PLAYER_TIME_START || command.value === ServerCommand.INITIAL_DEAL_DONE)
        return "Player " + table.value.activePlayer?.userName + " " + counter.value

    if (betsAllowed.value)
        return "Place your bets " + counter.value

    if (table.value.gamePhase === GamePhase.BET)
        return "Bet phase " + counter.value
    return ""
})
const adjustBet = (amount: number) => {
    betAmount.value = amount
    useSocketSend({ action: PlayerAction.BET, amount: amount });
    player.value.totalBet = betAmount.value
    drawTable(false)
}

const hasSeat = computed<boolean>(() => useSeatChecker(player.value))
const canReduceMinimum = computed<boolean>(() => betsAllowed.value && betAmount.value - table.value.tableCard.gameData.minBet >= 0)
const canAddMinimum = computed<boolean>(() => {
    return betsAllowed.value && betAmount.value >= 0
        && betAmount.value + table.value.tableCard.gameData.minBet <= table.value.tableCard.gameData.maxBet
        && player.value?.currentBalance - (betAmount.value + table.value.tableCard.gameData.minBet) >= 0
})
const canBetMinimum = computed<boolean>(() => {
    return betsAllowed.value
        && player.value?.currentBalance >= table.value.tableCard.gameData.minBet
        && betAmount.value !== table.value.tableCard.gameData.minBet
})

const canBetMaximum = computed<boolean>(() => betsAllowed.value && player.value?.currentBalance > table.value.tableCard.gameData.maxBet)
const betsAllowed = computed<boolean>(() => table.value.gamePhase === GamePhase.BET && hasSeat.value && counter.value > 0)
const canTakeSeat = computed<boolean>(() => {
    const tableCard = table.value.tableCard
    if (tableCard.type === TableType.MULTIPLAYER)
        return !hasSeat.value && table.value.seats.map(seat => seat.available) != null
    return tableCard.availablePositions.length >= tableCard.thresholds.seatCount
})

const canAct = computed<boolean>(() => table.value.gamePhase === 'PLAY' && useActivePlayerChecker(table.value, player.value))
const canBetPrevious = computed<boolean>(() => betsAllowed.value && previousBetAmount.value > 0 && player?.value.currentBalance >= previousBetAmount.value)
const hasBet = computed<boolean>(() => betAmount.value > 0)
const insure = () => {
    insuranceClicked.value = true
    useSocketSend({ action: PlayerAction.INSURE })
}
const sendAction = (action: string) => useSocketSend({ action: action })

const getSeatsDescending = computed<Array<Seat<BlackjackPlayer>>>(() => useDescendingSeatsSorter(table.value))



const drawTable = async (initialDeal: boolean) => {
    const canvas: HTMLCanvasElement = useCanvasClearer(useGameCanvasFinder())
    const ctx = canvas.getContext("2d")
    if (!ctx) return
    ctx.drawImage(bgImage, 0, 0, canvas.width, canvas.height);
    const mainBoxPlayer = useHeroFinder(table.value, player.value) as BlackjackPlayer
    useActorsPainter(table.value, mainBoxPlayer, canvas);
    if (initialDeal)
        useInitialDealPainter(table.value, mainBoxPlayer, canvas)
    else
        useCardsAndHandValuesPainter(table.value, mainBoxPlayer, canvas)
}


const getMainBoxPlayerActionStyle = () => {
    const liftUp = useGameCanvasFinder().height / 8 + "px"
    return { 'display': "inline", 'bottom': liftUp, "margin-right": "45px", "left": "55px" }
}

const getInstructionStyle = computed(() => {
    const top = (useGameCanvasFinder().height * 0.72).toString() + "px"
    const left = (useGameCanvasFinder().width / 3).toString() + "px"
    const color = counter.value > 4 ? 'yellow' : 'red'
    return {
        'left': left, 'top': top, "color": color, "font-size": 22 + "px", 'z-index': 10
    }
})

const getHandNumberText = () => {
    if (!player.value.hands) {
        return ""
    }
    if (player.value.hands.length === 1) {
        return "Take"
    }
    return player.value.hands[0].active ? "Take ( first )" : "Take ( second )"
}

const insuranceClicked = ref<boolean>(false)
/**
     * TODO
     * Blackjack, single player table -> watcher sees disabled insure button in insurance case
     */
const insuranceAvailable = computed<boolean>(() => {
    return hasSeat.value && table.value.gamePhase === GamePhase.INSURE
        && player.value.currentBalance >= player.value.totalBet / 2
        && insuranceClicked.value === false
        && player.value.hands[0].cards.length > 0
});
</script>

<template v-if="canvasReady">
    <div style="position: relative">
        Welcome {{ player?.userName }}
        <div v-if="counterText" style="position:absolute" :style="getInstructionStyle">{{ counterText }}</div>
        <canvas id="canvas" width="1800" height="600"></canvas>
        <div v-if="canTakeSeat" id="takeSeatRow">
            <TakeSeatButtonRow :seats=getSeatsDescending />
        </div>
        <div v-if="betsAllowed" id="betRow" style="position:absolute" :style="getMainBoxPlayerActionStyle()">
            <button :disabled="!canReduceMinimum" @click="adjustBet(betAmount - table.tableCard.gameData.minBet)">
                Reduce {{ table.tableCard.gameData.minBet }}
            </button>
            <button :disabled="!canBetMinimum" @click="adjustBet(table.tableCard.gameData.minBet)">
                Minimum {{ table.tableCard.gameData.minBet }}
            </button>
            <button :disabled="!canBetPrevious" @click="adjustBet(previousBetAmount)">
                Previous {{ previousBetAmount }}
            </button>
            <button :disabled="!canAddMinimum" @click="adjustBet(betAmount + table.tableCard.gameData.minBet)">
                Add {{ table.tableCard.gameData.minBet }}
            </button>
            <button :disabled="!canBetMaximum" @click="adjustBet(table.tableCard.gameData.maxBet)">
                Max {{ table.tableCard.gameData.maxBet }}
            </button>
            <button :disabled="!hasBet" @click="adjustBet(0)">
                Remove bet
            </button>
        </div>
        <div v-if="table.gamePhase === GamePhase.INSURE" id="insureRow" style="position:absolute">
            <button :disabled="!insuranceAvailable" @click="insure()">
                Insure
            </button>
        </div>
        <div v-show="canAct" id="actionRow" style="position:absolute; bottom:25px: left:50px">
            <button v-if="table.activePlayer?.actions.includes(PlayerAction.TAKE)" @click="sendAction(PlayerAction.TAKE)">
                {{ getHandNumberText() }}
            </button>
            <button v-if="table.activePlayer?.actions.includes(PlayerAction.SPLIT)" @click="sendAction(PlayerAction.SPLIT)">
                Split
            </button>
            <button v-if="table.activePlayer?.actions.includes(PlayerAction.DOUBLE_DOWN)"
                @click="sendAction(PlayerAction.DOUBLE_DOWN)">
                Double down
            </button>
            <button v-if="table.activePlayer?.actions.includes(PlayerAction.STAND)" @click="sendAction(PlayerAction.STAND)">
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