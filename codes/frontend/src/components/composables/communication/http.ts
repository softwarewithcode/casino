const httpBase = import.meta.env.VITE_CASINO_HTTP_ENDPOINT
const acceptHeader = new Headers({ Accept: "application/json" })
const requestInit: RequestInit = {
	method: "GET",
	headers: acceptHeader
}
export async function fetchTables(gameType: string) {
	const finalURI = httpBase + `/${gameType}/tables`
	const resp = await fetch(finalURI, requestInit)
	const tables = await resp.json()
	return tables
}
