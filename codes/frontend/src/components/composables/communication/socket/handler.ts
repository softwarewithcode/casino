export function socketHandler(data: any) {
  console.log("incoming data from socket" + JSON.parse(data));
  switch (data.title) {
    case "LOGIN":
      console.log("login");
  }
}
