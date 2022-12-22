import { useTableStore } from "../../../../stores/tableStore";
import { useRouter, useRoute } from "vue-router";
import router from "../../../../router/router";
const store = useTableStore();
//const router = useRouter()

export function handle(data: any) {
  console.log("incoming data from socket" + data);

  const handleTableOpen = async (data: any) => {
    console.log("PATCH:" + JSON.stringify(data));
    let table = data.table;
    store.$patch({
      table: table,
    });
    router.push({ name: "blackjack", params: { tableId: table.id } });
  };

  const updateTable = async (data: any) => {
    console.log("updatePlayer:" + JSON.stringify(data));
    store.$patch({
      table: data.table, // updates whole table instead of players.. TODO
    });
  };

  switch (data.title) {
    case "LOGIN":
      console.log("login");
    case "WATCHER_JOIN":
      handleTableOpen(data);
    case "NEW_PLAYER":
      updateTable(data);
    case "PLAYER_LEFT": //Server keeps the player in list until round is finished
      updateTable(data);
  }
}
