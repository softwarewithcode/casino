import { useTableStore } from "../../../../stores/tableStore";
import { useRouter, useRoute } from "vue-router";
import router from "../../../../router/router";
import { Command } from "@/types/blackjack";
const store = useTableStore();
//const router = useRouter()

export function handle(data: any) {
  console.log("incoming data from socket" + data);
  store.$patch({
    command: data.title,
  });
  switch (data.title) {
    case Command.LOGIN:
      console.log("login");
      store.$patch({
        player: data.player,
      });
      break;
    case Command.OPEN_TABLE:
      handleTableOpen(data);
      break;
    case Command.NEW_PLAYER:
      updateTable(data);
      break;
    case Command.PLAYER_LEFT: //Server keeps the player in list until round is finished
      updateTable(data);
      break;
  }
}
const w = m => new Promise(r => setTimeout(r, m))

const handleTableOpen = async (data: any) => {
  console.log("handleTableOpen");
  let table = data.table;
  router.push({ name: "blackjack", params: { tableId: table.id } });
  store.$patch({
    table: table,
  });
};

const updateTable = async (data: any) => {
  console.log("updateTable:");
  store.$patch({
    table: data.table,
    command: data.title,
  });
};
