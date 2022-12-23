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
        me: data.player,
      });
    case Command.OPEN_TABLE:
      handleTableOpen(data);
    case Command.NEW_PLAYER:
      updateTable(data);
    case Command.PLAYER_LEFT: //Server keeps the player in list until round is finished
      updateTable(data);
  }
}

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
