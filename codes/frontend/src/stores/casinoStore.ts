import type { GameType } from "@/types/casino"
import { defineStore } from "pinia"
import { fetchGameTypes } from "../components/composables/communication/http"

export const useCasinoStore = defineStore("casinoStore", {
	state: () => ({
		gameTypes: [] as GameType[],
		locale: "us-en" //new Intl.locale
	}),
	getters: {
		getTypes(state): GameType[] {
			return state.gameTypes
		}
	},
	actions: {
		async populateStore() {
			try {
				this.gameTypes = await fetchGameTypes()
			} catch (error) {
				alert(error)
			}
		},
		async login() {
			//?? sessionStorage? location
		}
	}
})
