//Tester code
/*
fetchTables()
async function fetchTables(){
	const tablesAPI = "http://localhost:8080/casino/blackjack/tables";
	const acceptHeader = new Headers({ Accept: "application/json" });
	const requestInit = {
 		method: "GET",
  		headers: acceptHeader,
  		body: null
	}
	let tables=null
    const resp = await fetch(tablesAPI, requestInit);
	 if(resp.status < 400)
    	tables = await resp.json();
    this.tables =tables;
    //return { tables: tables, statusCode: resp.status };
    console.log("tables"+ this.tables)
	showTables(tables)
}

function act(title, amount) {
	const json = { amount: amount, action: title }
	const jsonObj = JSON.stringify(json);
	console.log("calling :" + title + " with" + jsonObj)
	socket.send(jsonObj);
}
const w = m => new Promise(r => setTimeout(r, m)) // artificial wait
async function trySeat(seat) {
	const json = { seat: seat, action: 'JOIN' }
	const jsonObj = JSON.stringify(json);
	console.log("reserving seat :" + seat + " with" + jsonObj)
	await openTable(tables[0].id)
	await w(1000) // waiting for WS initialization in this tester.
	socket.send(jsonObj);
}
function joinAsWatcher() {
	const json = { action: 'JOIN' }
	const jsonObj = JSON.stringify(json);
	console.log("joining as watcher " + jsonObj)
	socket.send(jsonObj);
}


function placeBet(amount) {
	const json = { amount: amount, action: 'BET' }
	const jsonObj = JSON.stringify(json);
	console.log("placing starting bet:" + jsonObj)
	socket.send(jsonObj);
}

function clearCardsContainer(){
	document.getElementById('playerCardsContainer').innerHTML = ""
	document.getElementById('dealerCardsContainer').innerHTML = ""
	document.getElementById('otherPlayersCardsContainer').innerHTML = ""
	
}
function updateAllPlayersBalances(players) {
	const myResult = players.filter(player => player.name === myName)
	if (myResult && myResult.length > 0)
		myBalance = myResult[0].balance
	//other players' balances TODOs
	visualizeMyProfile()
}

function startBetPhase(fromTime) {
	timer=fromTime;
	clearInterval(interval)
	interval = setInterval(() => {
		timer--
		document.getElementById('timer').innerHTML = +timer
		if (timer == 0) {
			clearInterval(interval)
			document.getElementById('betsContainer').style.visibility = 'hidden'
		}
	}, 1000);
}

function visualizeMyProfile() {
	document.getElementById('myProfile').innerHTML = myName + " balance:" + myBalance;
}

function updatePlayerCards(player) {
	console.log("update cards:" + player.name)
	const cards = player.hands[0].cards
	document.getElementById('playerCardsContainer').innerHTML = ""
	document.getElementById('playerCardsContainer').innerHTML =player.name+ " cards <br>"
	cards.forEach(card => {
		document.getElementById('playerCardsContainer').innerHTML +=getSuit(card.suit) + card.rank + " " + "<br>"
	})
	if (player.hands[0].isBlackjack)
		document.getElementById('playerCardsContainer').innerHTML += " blackjack. use case ends"
	document.getElementById('playerCardsContainer').innerHTML += "( hand " + player.hands[0].values[0] + ") - "
	if (player.hands[0].values[1])
		document.getElementById('playerCardsContainer').innerHTML += "(  " + player.hands[0].values[1] + ")"
}

function getSuit(suit) {
	if (suit == "SPADE")
		return '\u2664'
	if (suit == "HEART")
		return '\u2661'
	if (suit == "DIAMOND")
		return '\u2662'
	if (suit == "CLUB")
		return '\u2667'
}


function showTables(tables) {
	console.log("TABLES:"+JSON.stringify(tables))
		tables.forEach( table => {
			document.getElementById('tablesContainer').innerHTML += "Table:ID= "+table.id 
	
			+"<br><br>"
		}
	)
}

function updateDealerCards(dealerHand) {
	document.getElementById('dealerCardsContainer').innerHTML = ""
	dealerHand.cards.forEach(card => {
		document.getElementById('dealerCardsContainer').innerHTML += "Dealer Card:   " + getSuit(card.suit) + card.rank + " " + "<br>"
	})

	document.getElementById('dealerCardsContainer').innerHTML += "(dealer has " + (dealerHand.values[0]) + ")"
}

function updateWinnings(player) {
	if (player.payout > 0)
		document.getElementById('roundCompletedContainer').innerHTML += "Dealer gives you back " + player.payout + " checkBalance"
	else
		document.getElementById('roundCompletedContainer').innerHTML += "You lost"
}
*/