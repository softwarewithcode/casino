<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from "vue";
import { useSocketSend, useSocketClose } from "@/components/composables/communication/socket/websocket";
import { useCasinoStore } from "@/stores/casinoStore";
import { useTableJoiner, useJoinChecker, isPlayerInTable, useGameCanvasFinder, useHeroFinder } from "@/components/composables/common/table"
import { useRouletteTablePainter } from "@/components/roulette/rouletteTablePainter";
import { useBetPositionFinder, } from "@/components/roulette/betPositionHelper";
import { ServerCommand } from "@/types/servercommands";
import { useRouletteStore } from "@/stores/rouletteStore";
import { storeToRefs } from "pinia";
import { type RouletteBetPosition, PlayerAction, RouletteGamePhase, type RoulettePaintData, type RoundResult } from "@/types/roulette";
import { allChips } from "@/types/chips";
import { TableType } from "@/types/casino";
import { isPositionInBetweenVectorPointsInclusive } from "@/components/composables/common/math";
import { useVectorAdder } from "@/types/vectors";

const rouletteStore = useRouletteStore()
const casinoStore = useCasinoStore()
const selectedBetPosition = ref<RouletteBetPosition>()
const highlightWinningNumber = ref<number | null>(null)
const randomNumber = ref<number | null>(null)
const canvasReady = ref<boolean>(false);
const { table, mainCharacter, serverCommand } = storeToRefs(rouletteStore)
const { counter } = storeToRefs(casinoStore)
const mouseX = ref<number>()
const mouseY = ref<number>()
const highlightEffectiveNumbers = ref<boolean>(true)
let spinInterval
let winningNumberHighlightTimeout
const unSubscribe = rouletteStore.$subscribe((mutation, state) => {
    if (mutation.type === "patch object")
        onStorePatch()
})
const onStorePatch = () => {
    if (serverCommand.value === ServerCommand.LOGIN)
        attachListeners()
    else if (serverCommand.value === ServerCommand.ROUND_COMPLETED)
        finalizeRound()
    if (mainCharacter.value) {
        if (table.value.gamePhase === RouletteGamePhase.BET)
            populateChipSelection()
        else {
            mainCharacter.value.selectedChip = undefined
            selectedBetPosition.value = undefined
        }
    }
    drawTable(serverCommand.value === ServerCommand.INIT_DATA)
}
watch(() => rouletteStore.getTable.gamePhase, (gamePhase) => {
    if (gamePhase === RouletteGamePhase.SPINNING)
        spinWheel()
})
const spinWheel = () => {
    spinInterval = setInterval(() => {
        if (table.value.gamePhase !== RouletteGamePhase.SPINNING) {
            clearInterval(spinInterval)
            randomNumber.value = null
            drawTable(false)
            return
        }
        randomNumber.value = getRandomInteger()
        drawTable(false)
    }, 400)
}
const finalizeRound = () => {
    highlightWinningNumber.value = table.value.wheel.resultBoard[table.value.wheel.resultBoard.length - 1].winningNumber
    winningNumberHighlightTimeout = setTimeout(() => {
        highlightWinningNumber.value = null
        drawTable(false)
    }, 6000)
}

const getRandomInteger = () => {
    return Math.floor(Math.random() * (table.value.tableCard.gameData.tableNumbers.max - table.value.tableCard.gameData.tableNumbers.min) + table.value.tableCard.gameData.tableNumbers.min);
}

const attachListeners = () => {
    canvas.value.addEventListener("pointermove", handlePointerMove, false)
    canvas.value.addEventListener("pointerout", handlePointerOut, false)
    canvas.value.addEventListener("pointerup", tryAddBet, false)
    window.addEventListener("contextmenu", tryRemovePile, false)
}
const tryRemovePile = (event: MouseEvent) => {
    event.preventDefault()
    const betPosition = useBetPositionFinder(rouletteStore.betPositions, event)
    if (betPosition)
        useSocketSend({ action: PlayerAction.REMOVE_BET_FROM_POSITION, position: betPosition.number })
}
const handlePointerMove = (event: PointerEvent) => {
    mouseX.value = event.offsetX
    mouseY.value = event.offsetY
    tryChooseChipFromChipSelection(event)
    trySelectNewBetPosition(event)
}

const handlePointerOut = () => {
    mainCharacter.value.selectedChip = undefined
    drawTable(false)
}

const tryChooseChipFromChipSelection = (event: PointerEvent) => {
    if (!mainCharacter.value?.chips)
        return
    const pointerPosition = { x: event.offsetX, y: event.offsetY }
    for (const chip of mainCharacter.value.chips) {
        const searchAreaSize = useVectorAdder(chip.position!, chip.size!.x, chip.size!.y)
        if (isPositionInBetweenVectorPointsInclusive(pointerPosition, chip.position!, searchAreaSize)) {
            mainCharacter.value.selectedChip = { value: chip.value, position: { x: event.offsetX, y: event.offsetY }, image: chip.image, size: chip.size }
            break
        }
    }
    if (mainCharacter.value.selectedChip) {
        const newPosition = useVectorAdder(pointerPosition, -mainCharacter.value.selectedChip.size!.x / 2, -mainCharacter.value.selectedChip.size!.y / 2)
        mainCharacter.value.selectedChip.position = newPosition
        drawTable(false)
    }
}
const tryAddBet = (event: PointerEvent) => {
    if (!hasSelectedChip()) return
    const betPosition = useBetPositionFinder(rouletteStore.betPositions, event)
    if (betPosition)
        useSocketSend({ action: PlayerAction.BET, position: betPosition.number, amount: mainCharacter.value.selectedChip!.value })
}

const hasSelectedChip = (): boolean => {
    return mainCharacter.value.selectedChip ? true : false
}
const removeLast = () => useSocketSend({ action: PlayerAction.REMOVE_LAST_OR_ALL, removeAllBets: false })
const removeAll = () => useSocketSend({ action: PlayerAction.REMOVE_LAST_OR_ALL, removeAllBets: true })

const play = () => {
    highlightWinningNumber.value = null
    useSocketSend({ action: PlayerAction.PLAY, spinId: table.value.wheel.spinId })
    drawTable(false)
}
const repeatLast = () => useSocketSend({ action: PlayerAction.REPEAT_LAST })
const fetchTableBetPositionMap = () => useSocketSend({ action: PlayerAction.FETCH_BET_POSITIONS })

const canvas = ref<HTMLCanvasElement>(useGameCanvasFinder())
const trySelectNewBetPosition = (event: PointerEvent) => {
    const betPosition = useBetPositionFinder(rouletteStore.betPositions, event)
    if (betPosition === selectedBetPosition.value) {
        return
    }
    unselectBetPosition()
    if (betPosition)
        selectBetPosition(betPosition)
    drawTable(false)
}
const selectBetPosition = (position: RouletteBetPosition) => {
    selectedBetPosition.value = position
    selectedBetPosition.value.selected = true
}
const unselectBetPosition = () => {
    if (selectedBetPosition.value)
        selectedBetPosition.value.selected = false
    selectedBetPosition.value = undefined
}

onMounted(() => {
    canvas.value = document.getElementsByTagName("canvas")[0];
    canvasReady.value = true;
    fetchTableBetPositionMap()
})

const populateChipSelection = () => {
    const minBet = table.value.tableCard.gameData.minBet
    const maxBet = table.value.tableCard.gameData.maxBet
    mainCharacter.value.chips = allChips.filter(chip => chip.value <= mainCharacter.value.currentBalance && chip.value >= minBet && chip.value <= maxBet)
}

onUnmounted(() => {
    clearTimeout(winningNumberHighlightTimeout)
    clearInterval(spinInterval)
    unSubscribe()
    useSocketClose()
    unMountListeners()
    rouletteStore.$reset()
})

const unMountListeners = () => {
    canvas.value.removeEventListener("pointermove", handlePointerMove, false)
    canvas.value.removeEventListener("pointermove", handlePointerMove, false)
    canvas.value.removeEventListener("pointerup", tryAddBet, false)
    window.removeEventListener("contextmenu", tryRemovePile, false)
}

const drawTable = (recalculate: boolean) => {
    const paintData: RoulettePaintData = {
        table: table.value,
        betPositions: rouletteStore.betPositions,
        canvas: canvas.value,
        hero: rouletteStore.mainCharacter,
        recalculate: recalculate,
        selectedBetPosition: selectedBetPosition.value,
        highlightEffectiveNumbers: highlightEffectiveNumbers.value,
        highlightNumber: highlightWinningNumber.value,
        randomNumber: randomNumber.value
    }
    useRouletteTablePainter(paintData)
}

const getNumberStyle = number => {
    if (number === 0)
        return "green"
    return rouletteStore.getRedNumbers.includes(number) ? "red" : "black"
}
const latestRound = computed<RoundResult | undefined>(() =>
    mainCharacter.value?.roundResults?.length > 0 ? mainCharacter.value.roundResults[mainCharacter.value.roundResults.length - 1] : undefined
)
const canRemoveBets = computed<boolean>(() => mainCharacter.value?.actions?.includes(PlayerAction.REMOVE_LAST_OR_ALL))
const timerVisible = computed<boolean>(() => counter.value >= 0 && table.value.type === TableType.MULTIPLAYER && table.value.gamePhase === RouletteGamePhase.BET)
</script>
<template v-if="canvasReady">
    <div style="position: relative">
        Welcome {{ mainCharacter?.userName }}
        <div class="resultBoard">
            <ul class="results">
                <li v-for="result in rouletteStore.getReversedResultBoard" :class="getNumberStyle(result.winningNumber)">
                    &nbsp; {{ result.winningNumber }},
                </li>
            </ul>
        </div>
        <div v-if="timerVisible" class="counter">{{ counter }}</div>
        <canvas id="canvas" width="800" height="600"></canvas>
        <section v-if="latestRound" class="roundResults">
            <header>
                Results of round {{ latestRound.spinResult.roundNumber }}
            </header>
            Winning number = {{ latestRound.spinResult.winningNumber }}<br>
            Winnings = {{ latestRound.playerResult.totalWinnings }} <br>
            RemainingBets = {{ latestRound.playerResult.totalRemainingBets }}<br>
            Played bets = {{ latestRound.playerResult.totalBets }}<br>
        </section>
        <div v-if="isPlayerInTable(table, mainCharacter)" class="highlightSelection">
            <input type="radio" id="highlightNumbers" :value="true" @click="drawTable(false)"
                v-model="highlightEffectiveNumbers" />
            <label for="highlightNumbers"> Numbers </label>
            <input type="radio" id="highlightPosition" :value="false" @change="drawTable(false)"
                v-model="highlightEffectiveNumbers" />
            <label for="highlightPosition">Position</label>
        </div>

        <div v-if="useJoinChecker(table, mainCharacter)" id="takeSeatRow" class="joinInfo">
            <button @click="useTableJoiner">
                JOIN TABLE
            </button>
        </div>
        <div v-else-if="!isPlayerInTable(table, mainCharacter)" class="joinInfo">
            Cannot play in this table.
        </div>
        <div style="top:-120px" v-show="isPlayerInTable(table, mainCharacter)">
            <button :disabled="!canRemoveBets" @click="removeAll">Remove
                bets</button>
            <button :disabled="!canRemoveBets" @click="removeLast">Remove
                last bet</button>

        </div>
        <span v-if="mainCharacter?.actions?.includes(PlayerAction.PLAY)" style="top:-120px">
            <button @click="play">Play</button>
        </span>
        <span v-if="mainCharacter?.actions?.includes(PlayerAction.REPEAT_LAST)" style="top:-120px">
            <button @click="repeatLast">Repeat last</button>
        </span>
    </div>
</template>
<style scoped>
button {
    margin: 15px
}

.roundResults {
    position: absolute;
    width: 160px;
    top: 150px;
    left: 820px;
    padding-bottom: 50px;
}

.roundResults header {
    font-size: 17px;
    font-weight: bold;
}

.highlightSelection {
    color: black;
    position: absolute;
    width: 160px;
    top: 320px;
    left: 820px
}

.counter {
    position: relative;
    color: yellow;
    top: 45px;
    z-index: 100;
    font-weight: bolder;
    left: 5px;
    font-size: 30px;
}

.joinInfo {
    top: -140px;
    position: relative
}

.resultBoard {
    height: 50px;
}

.results {
    list-style-type: none;
    display: inline-block;
    height: 60px;
}

.results li {
    font-family: "Times New Roman";
    font-size: 20px;
    display: inline-block;
    height: 50px
}

.results li.black {
    color: black;
}

.results li.red {
    color: red;
}

.results li.zero {
    color: green;
}

.results li:first-child {
    font-size: 30px;
}
</style>@/components/roulette/betPositionHelper