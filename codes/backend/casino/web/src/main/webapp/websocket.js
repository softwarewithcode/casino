//Tester code
var socket;
function openTable(tableId) {
	 socket = new WebSocket('ws://localhost:8080/casino/blackjack/' + tableId);
	//var socket= new WebSocket('ws://localhost:8080/casino/blackjack/'+tableId+'?watch=1');
	if (socket) {
		console.log("got connection")

	}
	addListener();
}

function addListener() {
	socket.addEventListener('message', (event) => {
		const ret = JSON.parse(event.data)
		console.log("message in:" + ret.title)
		document.getElementById('tableData').innerHTML += ret.title + "<br>" + ret.table + "<br>"

		if (ret.title === "BET_PHASE_STARTS") {
			console.log("bet phase starts")
			updateAllPlayersBalances(ret.table.players)
			document.getElementById('betsContainer').style.visibility = 'visible'
			document.getElementById('joinContainer').style.visibility = 'hidden'
			document.getElementById('playerCardsContainer').innerHTML = ""
			document.getElementById('roundCompletedContainer').innerHTML = ""
			clearCardsContainer()
			startBetPhase(ret.table.thresholds.betPhaseTime)
		}
		if (ret.title === "INSURANCE_PHASE_STARTS") {

			//If blackjack then should not show insurance container
			document.getElementById('insureContainer').style.visibility = 'visible';
			console.log(JSON.stringify(ret.table.thresholds))
			timer = ret.table.thresholds.secondPhaseTime
			updateAllPlayersBalances(ret.table.players)
			updatePlayerCards(ret.table.players.find(player => player.name === myName))
			updateDealerCards(ret.table.dealerHand)
			document.getElementById('betsContainer').style.visibility = 'hidden'
			clearInterval(interval)
			interval = setInterval(() => {
				timer--
				document.getElementById('timer').innerHTML = +timer
				if (timer <= 0) {
					clearInterval(interval)
					document.getElementById('insureButton').style.visibility = 'hidden';
				}
			}, 1000)
		}
		if (ret.title === "NEW_PLAYER") {
			document.getElementById('joinedText').style.visibility = 'visible';
			document.getElementById('joinedText').innerHTML += "<br>new player joined:" + ret.player.name + " seatNumber:" + ret.player.seatNumber
		}
		if (ret.title === "LOGIN") {
			// user gets own data here and inits the page
			myName = ret.player.name
			myBalance = ret.player.balance
			tableId = ret.table.id
			document.getElementById('myProfile').style.visibility = 'visible';
			console.log("myName:" + myName + " myBalance:" + myBalance + " players:" + ret.table.players.length)
			document.getElementById('joinedText').innerHTML += ret.table.id
			document.getElementById('joinedText').style.visibility = 'visible';
			visualizeMyProfile()
			if (ret.table.gamePhase === "BET" && ret.table.counterTime > 2) {
				startBetPhase(ret.table.counterTime)
				console.log("join start:" + ret.table.counterTime);
				document.getElementById('betsContainer').style.visibility = 'visible'
				document.getElementById('joinContainer').style.visibility = 'hidden'
			}
		}
		if (ret.title === "ROUND_COMPLETED") {
			document.getElementById('insureContainer').style.visibility = 'hidden';
			document.getElementById('actionContainer').style.display = 'none';
			document.getElementById('roundCompletedContainer').style.visibility = 'visible';
			document.getElementById('roundCompletedContainer').style.display = 'block';
			clearInterval(interval)
			updateAllPlayersBalances(ret.table.players)
			updatePlayerCards(ret.table.players[0])
			updateDealerCards(ret.table.dealerHand)
			console.log("myName:" + myName + " myBalance:" + myBalance + " players:" + ret.table.players.length)
			updateWinnings(ret.table.players.find(player => player.name === myName))
			visualizeMyProfile()
		}

		if (ret.title === "SERVER_WAITS_PLAYER_ACTION") {
			console.log("playTurn message:" + ret)
			timer = ret.table.thresholds.playerHandTime
			clearInterval(interval)
			document.getElementById('actionContainer').style.color = 'green'
			document.getElementById('insureContainer').style.visibility = 'hidden';
			document.getElementById('actionContainer').style.display = 'none';
			updateAllPlayersBalances(ret.table.players)
			const playerInTurnIsMe = ret.table.playerInTurn.name === myName ? true : false
			updateDealerCards(ret.table.dealerHand)
			updatePlayerCards(ret.player)
			document.getElementById('actionContainer').style.display = 'none';
			console.log("myTurn:" + playerInTurnIsMe)
			if (playerInTurnIsMe) {
				document.getElementById('actionContainer').style.color = 'green';
				document.getElementById('actionContainer').style.display = 'block';
				const take = ret.player.actions.includes("TAKE");
				const stand = ret.player.actions.includes("STAND");
				const split = ret.player.actions.includes("SPLIT");
				const doubl = ret.player.actions.includes("DOUBLE_DOWN");
				if (take)
					document.getElementById('take').style.visibility = 'visible';
				if (stand)
					document.getElementById('stand').style.visibility = 'visible';
				if (split)
					document.getElementById('split').style.visibility = 'visible';
				if (doubl)
					document.getElementById('doubl').style.visibility = 'visible';

				interval = setInterval(() => {
					timer--
					document.getElementById('timer').innerHTML = +timer
					if (timer <= 0) {
						clearInterval(interval)
						document.getElementById('insureButton').style.visibility = 'hidden';
					}
				}, 1000)
			}
		}
	})
};