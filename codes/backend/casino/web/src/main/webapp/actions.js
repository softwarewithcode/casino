function act(title, amount) {
	const json = { amount: amount, action: title }
	const jsonObj = JSON.stringify(json);
	console.log("calling :" + title + " with" + jsonObj)
	socket.send(jsonObj);
}
function trySeat(seat) {
	const json = { seat: seat, action: 'JOIN' }
	const jsonObj = JSON.stringify(json);
	console.log("reserving seat :" + seat + " with" + jsonObj)
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

function updateDealerCards(dealerHand) {
	document.getElementById('dealerCardsContainer').innerHTML = ""
	dealerHand.cards.forEach(card => {
		document.getElementById('dealerCardsContainer').innerHTML += "Dealer Card:   " + getSuit(card.suit) + card.rank + " " + "<br>"
	})

	document.getElementById('dealerCardsContainer').innerHTML += "(dealer has " + (dealerHand.values[0]) + ")"
}

function updateWinnings(player) {
	if (player.payout > 0)
		document.getElementById('payouts').innerHTML += "Dealer gives you back " + player.payout + " checkBalance"
	else
		document.getElementById('payouts').innerHTML += "You lost"
}
