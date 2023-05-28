<script setup lang="ts">

import { onMounted, onUnmounted, ref, computed, reactive } from "vue";
import { useSocketSend, useSocketClose } from "@/components/composables/communication/socket/websocket";
import { useCasinoStore } from "@/stores/casinoStore";
import { useActivePlayerChecker, useMainBoxPlayerFinder, useDescendingSeatsSorter, usePlayerAllowedStatusesChecker } from "@/components/composables/common/table"
import { useCanvasClearer } from "@/components/composables/rendering/commonPainter";
import { useTexasHoldemStore } from "@/stores/texasHoldemStore";
import { storeToRefs } from "pinia";
import { type HoldemPlayer, TableAction, GameAction } from "@/types/texasHoldem";
import { PlayerStatus, rangeInputUpdate, type Seat } from "@/types/casino";
import TakeSeatButtonRow from "../TakeSeatButtonRow.vue";
import { useHoldemTablePainter } from "@/components/holdem/holdemTablePainter"
import type { HoldemAction } from "@/types/texasHoldem";
import { type Range } from "@/types/casino";
import RangeView from "../RangeView.vue";

import { Command } from "@/types/sockethander";
import { useCounterRunningChecker } from "@/components/composables/timing/clock";
const holdemStore = useTexasHoldemStore();
const casinoStore = useCasinoStore()
const props = defineProps<{ tableId: string }>();
const canvasReady = ref<boolean>(false);
const { table, mainPlayer, lastActor, command } = storeToRefs(holdemStore)
const { counter } = storeToRefs(casinoStore)
const unSubscribe = holdemStore.$subscribe((mutation, state) => {
    if (mutation.type === "patch object")
        onStorePatch()
})
const onStorePatch = () => {
    if (isLastActorSet()) {
        setActionForTablePlayer()
        const resetPlayer = lastActor.value
        setTimeout(resetLastActionFromPlayerAndDrawTable, 2100, resetPlayer)
    }
    drawTable()
    if (command.value === Command.ROUND_COMPLETED) {
        setTimeout(finalizeRound, 2500)
    }
}

const isLastActorSet = (): GameAction | undefined => {
    return lastActor.value?.lastAction
}

const finalizeRound = () => {
    holdemStore.clearTableAndPlayers()
    drawTable()
}
const setActionForTablePlayer = () => {
    const updatablePlayer = table.value.seats.find(seat => seat.player && seat.player.userName === lastActor.value.userName)?.player
    if (updatablePlayer) {
        updatablePlayer.lastAction = lastActor.value.lastAction
    }
}
onMounted(() => {
    canvasReady.value = true;
    drawTable()
})

onUnmounted(() => {
    unSubscribe()
    useSocketClose()
})

const resetLastActionFromPlayerAndDrawTable = (playerWhosLastActionWillBeReset: HoldemPlayer) => {
    resetLastActionFromPlayer(playerWhosLastActionWillBeReset)
    drawTable()
}
const drawTable = async () => {
    const canvas: HTMLCanvasElement = findCanvas()
    const mainBoxPlayer = useMainBoxPlayerFinder(table.value, mainPlayer.value) as HoldemPlayer
    useHoldemTablePainter(table.value, mainBoxPlayer, canvas)
}

const findCanvas = () => {
    const canvas: HTMLCanvasElement = useCanvasClearer(document.getElementById("canvas") as HTMLCanvasElement)
    const ctx = canvas.getContext("2d")
    if (!ctx) throw new Error("no canvas")
    return canvas
}

const resetLastActionFromPlayer = (resetPlayer: HoldemPlayer) => {
    holdemStore.resetLastActionFromPlayer(resetPlayer.userName)
}
const hasSeat = computed<boolean>(() => mainPlayer.value?.seatNumber >= 0)
const canTakeSeat = computed<boolean>(() => {
    const tableCard = table.value.tableCard
    return tableCard.availablePositions.length > 0 && !hasSeat.value
})
const canCheck = computed<boolean>(() => mainPlayer.value.actions?.flatMap(action => action.type).includes(GameAction.CHECK))
const canBetOrRaise = computed<boolean>(() => mainPlayer.value.actions?.flatMap(action => action.type).includes(GameAction.BET_RAISE))
const canCall = computed<boolean>(() => mainPlayer.value.actions?.flatMap(action => action.type).includes(GameAction.CALL))
const canFold = computed<boolean>(() => mainPlayer.value.actions?.flatMap(action => action.type).includes(GameAction.FOLD))
const canAllIn = computed<boolean>(() => mainPlayer.value.actions?.flatMap(action => action.type).includes(GameAction.ALL_IN))
const canPlayerAct = computed<boolean>(() => mainPlayer.value?.actions?.length > 0 && useActivePlayerChecker(table.value, mainPlayer.value) && usePlayerAllowedStatusesChecker(mainPlayer.value, PlayerStatus.ACTIVE))
const canReload = computed<boolean>(() => mainPlayer.value.currentBalance <= table.value.tableCard.gameData.bigBlind + table.value.tableCard.gameData.smallBlind && !reloadClicked.value)
const getSeatsDescending = computed<Array<Seat<HoldemPlayer>>>(() => useDescendingSeatsSorter(table.value))
const getBetRange = computed<Range | undefined>(() => mainPlayer.value.actions?.find(action => action.type === GameAction.BET_RAISE)?.range)
const getBetButtonText = computed<string>(() => {
    if (table.value.seats.filter(seat => seat.player).map(seat => seat.player).some(seat => seat.chipsOnTable > 0))
        return "RaiseTo"
    return "Bet"
})
const getCounterStyle = computed(() => {
    return {
        'left': "200px", 'top': "350px", "color": "yellow", "font-size": 22 + "px", 'z-index': 10
    }
})
const getSitOutClockIndicatorStyle = computed(() => {
    const clockIconStyle = { 'background-color': "green", 'font-size': "120%" }
    if (usePlayerAllowedStatusesChecker(mainPlayer.value, PlayerStatus.SIT_OUT) || sitstOutNextHand.value)
        clockIconStyle["background-color"] = "red"
    return clockIconStyle
})
const getBetIncrement = computed<number>(() => getBetRange.value ? getBetRange.value.max * 0.001 : 0)
const getAllInAmount = () => mainPlayer.value.currentBalance + mainPlayer.value.chipsOnTable
const currentBetAmount = ref<number>(getBetRange.value === undefined ? 0 : getBetRange.value.min)
const updateBetToAmount = betUpdate => currentBetAmount.value = parseFloat(betUpdate)
const updateBetIncrementally = (updateBet: Function) => {
    if (!currentBetAmount.value || !getBetRange.value)
        return
    currentBetAmount.value = updateBet(currentBetAmount.value, getBetIncrement.value, getBetRange.value)
    currentBetAmount.value = Number(currentBetAmount.value.toFixed(2))
}
const minusBetStrategy = (currentBet: number, amount: number, range: Range) => currentBet - amount >= range.min ? currentBet - amount : range.min
const plusBetStrategy = (currentBet: number, amount: number, range: Range) => currentBet + amount <= range.max ? currentBet + amount : range.max
const applyBetOrRaiseAction = (amount: number) => useSocketSend({ action: GameAction.BET_RAISE, amount: amount })
const sitstOutNextHand = ref<boolean>(false)
const toggleSitOutNextHand = () => {
    if (!sitstOutNextHand.value && usePlayerAllowedStatusesChecker(mainPlayer.value, [PlayerStatus.NEW, PlayerStatus.ACTIVE])) {
        useSocketSend({ action: TableAction.SIT_OUT_NEXT_HAND })
        sitstOutNextHand.value = true
    }
    else {
        sitstOutNextHand.value = false
        useSocketSend({ action: TableAction.CONTINUE_GAME })
    }
}
const fold = () => {
    useSocketSend({ action: GameAction.FOLD })
    mainPlayer.value.cards = []
    drawTable()
}

const reloadClicked = ref<boolean>(false)
const reload = () => {
    useSocketSend({ action: TableAction.RELOAD_CHIPS })
    reloadClicked.value = true
}


</script>
<template v-if="canvasReady">
    Texas holdem table
    <div style="position: relative">
        Welcome {{ mainPlayer?.userName }}
        <div v-if="useCounterRunningChecker() && counter > 0" style="position:absolute" :style="getCounterStyle">
            {{ counter }}
        </div>
        <canvas id="canvas" width="800" height="600"></canvas>
        <div v-if="canTakeSeat" id="takeSeatRow">
            <TakeSeatButtonRow :seats=getSeatsDescending />
        </div>
        <div>
            <span v-if="hasSeat" style="position:relative;top:-55px;float:left">
                <button @click="toggleSitOutNextHand()" id="sitOut" name="sitOut" :style="getSitOutClockIndicatorStyle">
                    &#128336;
                </button>
            </span>
            <div v-show="canPlayerAct" style="position:relative;top:-55px">
                <button v-if="canFold" @click="fold()"> Fold
                </button>
                <button v-if="canCheck" @click="useSocketSend({ action: GameAction.CHECK })"> Check
                </button>
                <button v-if="canCall" @click="useSocketSend({ action: GameAction.CALL })"> Call {{
                    holdemStore.callAmount }}</button>
                <span v-if="canBetOrRaise && getBetRange?.min" style=" border:1px solid black; padding:10px">
                    <RangeView :range="getBetRange" :step="getBetRange.max * 0.01" :min="getBetRange.min"
                        :max="getBetRange.max" @rangeInputUpdate="updateBetToAmount" />
                    <button @click="updateBetIncrementally(minusBetStrategy)"> -</button>
                    <button @click="applyBetOrRaiseAction(currentBetAmount)">
                        {{ getBetButtonText }} {{ currentBetAmount }}
                    </button>
                    <button @click="updateBetIncrementally(plusBetStrategy)"> +</button>
                </span>
                <button v-if="canAllIn" @click="useSocketSend({ action: GameAction.ALL_IN })"
                    style="position:relative; float:right"> All in {{ getAllInAmount() }} </button>
            </div>
            <button v-if="canReload" @click="reload()" style="position:relative; float:right;top:-55px;"> Reload </button>
        </div>

    </div>
</template>
<style scoped>
button {
    margin: 15px
}
</style>
