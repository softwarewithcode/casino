import { ServerCommand } from "@/types/servercommands"
import { useStartCounter, useStopCounter } from "../../../timing/clock"
import { ViewName } from "@/components/composables/common/Views"
import { TableType } from "@/types/casino"
import { searchPlayerFromTable, useTableViewOpener } from "@/components/composables/common/table"
import { useCasinoStore } from "@/stores/casinoStore"

import { useRouletteStore } from "@/stores/rouletteStore"
import { BetName, type RoulettePlayer, type RouletteTable } from "@/types/roulette"

const rouletteStore = useRouletteStore()
const casinoStore = useCasinoStore()
export function useRouletteMessageHandler(data: any) {
	switch (data.title) {
		case ServerCommand.OPEN_TABLE:
			let patchObject = { table: data.table, serverCommand: data.title, mainCharacter: data.player }
			rouletteStore.$patch(patchObject)
			openTable(data)
			break
		case ServerCommand.LOGIN:
			rouletteStore.$patch({ serverCommand: data.title, mainCharacter: data.player, table: data.table })
			break
		case ServerCommand.BET_TIME_START:
			startBetPhase(data)
			break
		case ServerCommand.ROUND_COMPLETED:
			finalizeRound(data)
			break
		case ServerCommand.INIT_DATA:
			const patchObj = {
				serverCommand: data.title,
				betPositions: data.tableBetPositions,
				blackNumbers: data.tableBetPositions.find(position => position.type.name === BetName.BLACK)!.tableNumbers,
				redNumbers: data.tableBetPositions.find(position => position.type.name === BetName.RED)!.tableNumbers
			}
			rouletteStore.$patch(patchObj)
			break
		default: {
			patchRouletteStore(data)
		}
	}
}

const openTable = (data: any) => {
	let table: RouletteTable = data.table
	useTableViewOpener(table, ViewName.ROULETTE_TABLE)
}

const patchRouletteStore = (data: any) => {
	let roulettePatchObject = { table: data.table, serverCommand: data.title }
	if (rouletteStore.mainCharacter) {
		const heroFromTableData = searchPlayerFromTable(data.table, rouletteStore.getMainCharacter) as RoulettePlayer
		if (heroFromTableData) {
			rouletteStore.mainCharacter.positionsTotalAmounts = [] // direct clear from store before new values
			roulettePatchObject["mainCharacter"] = heroFromTableData
		}
	}
	rouletteStore.$patch(roulettePatchObject)
}
const startBetPhase = (data: any) => {
	patchRouletteStore(data)
	if (data.table.type === TableType.MULTIPLAYER) {
		const casinoPatchObject = { serverCommand: data.title, counter: data.table.counterTime }
		casinoStore.$patch(casinoPatchObject)
		useStartCounter()
	}
}
const finalizeRound = (data: any) => {
	useStopCounter()
	patchRouletteStore(data)
}
