
	var socket= new WebSocket('ws://localhost:8080/casino/blackjack/e021c3bf-ffd9-4f75-953f-61639222e50d');
	 	if(socket){
	 		console.log("got connection")
	 	
	 	}
		// Connection opened
		socket.addEventListener('open', (event) => {
			console.log("open")		
		});
		
	socket.addEventListener('message', (event) => {
			const ret=JSON.parse(event.data)			
			console.log("message in:"+ret.title)
			document.getElementById('tableData').innerHTML +=ret.title +"<br>"+ret.table +"<br>"
		    if(ret.title==="BET_PHASE_STARTS"){
		    	console.log("bet phase starts")
		    	updateAllPlayersBalances(ret.table.players)
		    	document.getElementById('betsContainer').style.visibility = 'visible'
		    	document.getElementById('joinContainer').style.visibility = 'hidden' 
		    	timer=ret.table.thresholds.betPhaseTime
		    	clearInterval(interval)
		     	interval=setInterval(() => {
		    		  timer--
		    		  document.getElementById('timer').innerHTML =+timer 
		    		  if(timer==0){
		    			  clearInterval(interval)
		    			  document.getElementById('betsContainer').style.visibility = 'hidden'
		    		}
		    	}, 1000);
		    }
			if(ret.title==="INSURANCE_PHASE_STARTS"){
		    	document.getElementById('insureContainer').style.visibility = 'visible';
		    	console.log(JSON.stringify(ret.table.thresholds))
		    	timer=ret.table.thresholds.secondPhaseTime
		    	updateAllPlayersBalances(ret.table.players)
		    	updateMyCards(ret.table.players.find(player=>player.name===myName))
		    	updateDealerCards(ret.table.dealerHand)
		    	document.getElementById('betsContainer').style.visibility = 'hidden'
		    	clearInterval(interval)
		     	interval=setInterval(() => {
		    		  timer--
		    		  document.getElementById('timer').innerHTML =+timer 
		    		  if(timer<=0){
		    			  clearInterval(interval)
		    			  document.getElementById('insureButton').style.visibility = 'hidden';
		    		  }
		    		}, 1000)
		    }
			if(ret.title==="NEW_PLAYER"){
		    	document.getElementById('joinedText').style.visibility = 'visible';
		    	
		    }
			if(ret.title==="LOGIN"){
				// user gets own data here and inits the page
		    	myName = ret.player.name
		    	myBalance = ret.player.initialBalance
		    	document.getElementById('myProfile').style.visibility = 'visible';
		    	console.log("myName:"+myName+ " myBalance:"+myBalance +" players:"+ret.table.players.length)
		    	visualizeMyProfile()
		    }
		    if(ret.title==="ROUND_COMPLETED"){
				document.getElementById('insureContainer').style.visibility = 'hidden';
				clearInterval(interval)
		    	updateAllPlayersBalances(ret.table.players)
		    	updateMyCards(ret.table.players[0])
		    	updateDealerCards(ret.table.dealerHand)
		    	document.getElementById('roundCompletedContainer').style.visibility = 'visible';
		    	console.log("myName:"+myName+ " myBalance:"+myBalance +" players:"+ret.table.players.length)
		    	updateWinnings(ret.table.players.find(player=>player.name===myName))
		    	visualizeMyProfile()
		    }
		  
			if(ret.title==="PLAY_TURN"){
				console.log("playTurn message:"+ret)
				timer=ret.table.thresholds.playerHandTime
				clearInterval(interval)
				document.getElementById('myCardsContainer').innerHTML =""
				
				document.getElementById('insureContainer').style.visibility = 'hidden';
		    	document.getElementById('actionContainer').style.visibility = 'visible';
		    	updateAllPlayersBalances(ret.table.players)
		    	const playerInTurnIsMe=ret.player.name===myName
		    	if(playerInTurnIsMe){
					updateDealerCards(ret.table.dealerHand)
		   			updateMyCards(ret.table.players[0])
				
			    	const take=ret.player.actions.includes("TAKE");
			    	const stand=ret.player.actions.includes("STAND");
			    	const split=ret.player.actions.includes("SPLIT");
			    	const doubl=ret.player.actions.includes("DOUBLE");
			    	if(take)
			    		document.getElementById('take').style.visibility = 'visible';
			    	if(stand)
			    		document.getElementById('stand').style.visibility = 'visible';
			    	if(split)
			    		document.getElementById('split').style.visibility = 'visible';
			    	if(doubl)
			    		document.getElementById('doubl').style.visibility = 'visible';
					
			     	interval=setInterval(() => {
			    		  timer--
			    		  document.getElementById('timer').innerHTML =+timer 
			    		  if(timer<=0){
			    			  clearInterval(interval)
			    			  document.getElementById('insureButton').style.visibility = 'hidden';
			    		  }
			    		}, 1000)
			    }
		    }
		});