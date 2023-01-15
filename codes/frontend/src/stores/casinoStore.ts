import type { GameType } from "@/types/casino"
import { defineStore } from "pinia"
import { useGametypesFetch } from "../components/composables/communication/http"

export const useCasinoStore = defineStore("casinoStore", {
	state: () => ({
		gameTypes: [] as GameType[],
		counter: {} as number
	}),
	getters: {
		getTypes(state): GameType[] {
			return state.gameTypes
		},
		getCounter(state) {
			return state.counter
		}
	},
	actions: {
		async populateStore() {
			try {
				this.gameTypes = await useGametypesFetch()
			} catch (error) {
				alert(error)
			}
		},
		reduceCounter() {
			let counterCurrent = this.counter
			counterCurrent--
			this.counter = counterCurrent
		},
		async login() {
			//?? sessionStorage? location
		}
	}
})
