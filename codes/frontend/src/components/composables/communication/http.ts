const tablesEndpoint = "http://localhost:8080/casino/blackjack/tables";
const acceptHeader = new Headers({ Accept: "application/json" });
const requestInit: RequestInit = {
  method: "GET",
  headers: acceptHeader,
};

export async function fetchTables() {
  const api: string = tablesEndpoint;
  const resp = await fetch(api, requestInit);
  const tables = await resp.json();
  return tables;
}
