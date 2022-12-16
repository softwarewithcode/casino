function join() {
	const json = { userId: '', action: 'JOIN' }
	const jsonObj = JSON.stringify(json);
	console.log("join:" + jsonObj)
	socket.send(jsonObj);
}
function placeBet(amount) {
	const json = { amount: amount, action: 'BET' }
	const jsonObj = JSON.stringify(json);
	console.log("placing starting bet:" + jsonObj)
	socket.send(jsonObj);
}
function take(message) {
	document.getElementById('actionContainer').style.visibility = 'hidden'
	const json = { userId: '', action: 'TAKE' }
	const jsonObj = JSON.stringify(json);
	console.log("taking card:" + jsonObj)
	socket.send(jsonObj);
}

function split(message) {
	document.getElementById('actionContainer').style.visibility = 'hidden'
	const json = { userId: '', action: 'SPLIT' }
	const jsonObj = JSON.stringify(json);
	console.log("taking card:" + jsonObj)
	socket.send(jsonObj);
}


function doubleDown(message) {
	document.getElementById('actionContainer').style.visibility = 'hidden'
	const json = { userId: '', action: 'DOUBLE_DOWN' }
	const jsonObj = JSON.stringify(json);
	console.log("taking card:" + jsonObj)
	socket.send(jsonObj);
}

function stand(message) {
	document.getElementById('actionContainer').style.visibility = 'hidden'
	const json = { userId: '', action: 'STAND' }
	const jsonObj = JSON.stringify(json);
	console.log("taking card:" + jsonObj)
	socket.send(jsonObj);
}
function insure(message) {
	document.getElementById('insureContainer').style.visibility = 'hidden'
	const json = { action: 'INSURE' }
	const jsonObj = JSON.stringify(json);
	console.log("insuring:" + jsonObj)
	socket.send(jsonObj);
}
function updateAllPlayersBalances(players) {
	const myResult = players.filter(player => player.name === myName)
	if (myResult)
		myBalance = myResult[0].balance
	//other players' balances TODOs
	visualizeMyProfile()
}

function visualizeMyProfile() {
	document.getElementById('myProfile').innerHTML = myName + " balance:" + myBalance;
}

function updateMyCards(player) {
	console.log("update cards:" + player)
	const cards = player.hands[0].cards
	document.getElementById('myCardsContainer').innerHTML =""
	cards.forEach(card => {
		document.getElementById('myCardsContainer').innerHTML += "My Card:  " + getSuit(card.suit) + card.rank + " " + "<br>"
	})
	if (player.hands[0].isBlackjack)
		document.getElementById('myCardsContainer').innerHTML += " blackjack. use case ends"
	document.getElementById('myCardsContainer').innerHTML += "( hand "+player.hands[0].values[0] +") - "
	if(player.hands[0].values[1])
		document.getElementById('myCardsContainer').innerHTML += "(  "+player.hands[0].values[1] +")"
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
	document.getElementById('dealerCardsContainer').innerHTML =""
	dealerHand.cards.forEach(card => {
		document.getElementById('dealerCardsContainer').innerHTML += "Dealer Card:   " + getSuit(card.suit) + card.rank + " " + "<br>"
	})
	
	document.getElementById('dealerCardsContainer').innerHTML += "(dealer has " +(dealerHand.values[0]) +")"
}

function updateWinnings(player){
	if(player.payout>0)
		document.getElementById('payouts').innerHTML += "Dealer gives you back "+player.payout +" checkBalance"
	else
		document.getElementById('payouts').innerHTML += "You lost" 
}
