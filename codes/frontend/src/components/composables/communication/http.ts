const httpBase = import.meta.env.VITE_CASINO_HTTP_ENDPOINT
const acceptHeader = new Headers({ Accept: "application/json" })
const requestInit: RequestInit = {
	method: "GET",
	headers: acceptHeader
}
export async function useTablesFetch(gameType: string) {
	const finalURI = httpBase + `/${gameType}/tables`
	const resp = await fetch(finalURI, requestInit)
	return await resp.json()
}
export async function useGametypesFetch() {
	const finalURI = httpBase + `/gametypes`
	const resp = await fetch(finalURI, requestInit)
	return await resp.json()
}
