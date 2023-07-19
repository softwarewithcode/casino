import{y as b,z as h,V as P}from"./index-a7a178b0.js";import{a as S,u as A}from"./casinoStore-b4063b77.js";const C="BLACKJACK",p="TEXAS_HOLDEM";var _=(e=>(e.BLACKJACK="blackjack",e.TEXAS_HOLDEM="texas-holdem",e))(_||{}),m=(e=>(e.SIT_OUT="SIT_OUT",e.ACTIVE="ACTIVE",e.NEW="NEW",e))(m||{});const te="rangeInputUpdate",g=b("blackjackStore",{state:()=>({tables:[],table:{},command:{},player:{}}),getters:{getTables(e){return e.tables},getTable(e){return e.table},getPlayer(e){return e.player}},actions:{async populateStore(){try{let e=await S(_.BLACKJACK);this.tables=e.sort((t,a)=>t.thresholds.minimumBet-a.thresholds.minimumBet)}catch(e){alert(e)}},logout(e){this.player=e}}});var s=(e=>(e.NEW_PLAYER="NEW_PLAYER",e.BET_TIME_START="BET_TIME_START",e.INSURANCE_TIME_START="INSURANCE_TIME_START",e.PLAYER_TIME_START="PLAYER_TIME_START",e.LOGIN="LOGIN",e.ROUND_COMPLETED="ROUND_COMPLETED",e.PLAYER_LEFT="PLAYER_LEFT",e.STATUS_UPDATE="STATUS_UPDATE",e.OPEN_TABLE="OPEN_TABLE",e.TIMED_OUT="TIMED_OUT",e.INITIAL_DEAL_DONE="INITIAL_DEAL_DONE",e.NO_BETS_NO_DEAL="NO_BETS_NO_DEAL",e.SHOWDOWN="SHOWDOWN",e))(s||{});let u=-1,T;function N(){T=A(),D()}function ae(){return u!==-1}function E(){u&&(clearInterval(u),u=-1)}function D(){E(),u=setInterval(()=>{T.reduceCounter(),T.getCounter<=0&&E()},1e3)}const re=(e,t)=>{if(e===t.seats.length-1)return 0;let a=e;return a++,a++},se=(e,t)=>R(t,e.activePlayer),ne=(e,t)=>e?t&&t.length>0?t.includes(e.status):t===e.status:!1,R=(e,t)=>!e||!t?!1:e.userName===t.userName,le=e=>f({action:"JOIN",seat:e.toString()}),O=async(e,t)=>await h.push({name:t,params:{tableId:e.id}}),ce=(e,t)=>{var a;return t!=null&&t.userName?t:(a=e.seats.find(r=>{var c;return(c=r==null?void 0:r.player)==null?void 0:c.userName}))==null?void 0:a.player},oe=e=>e.seats.filter(t=>t).sort((t,a)=>t.number-a.number),ie=e=>(e==null?void 0:e.seatNumber)>=0,ue=()=>document.getElementById("canvas"),o=g(),d=A();function k(e){switch(o.$patch({command:e.title}),o.getPlayer&&(e.table.players.find(a=>a.userName===o.getPlayer.userName)||o.logout({})),e.title){case s.OPEN_TABLE:B(e);break;case s.LOGIN:o.$patch({player:e.player});break;case s.NO_BETS_NO_DEAL:M(e);break;case s.BET_TIME_START:case s.PLAYER_TIME_START:case s.INSURANCE_TIME_START:case s.INITIAL_DEAL_DONE:I(e);break;case s.ROUND_COMPLETED:H(e);break;default:y(e)}}const B=async e=>{let t=e.table;O(t,P.BLACKJACK_TABLE),I(e)},M=async e=>{let t={table:e.table,player:e.player,command:e.title};o.$patch(t)},y=async e=>{let t={table:e.table,command:e.title};const a=e.table.players.find(r=>{var c;return r.userName===((c=o.getPlayer)==null?void 0:c.userName)});o.getPlayer&&a&&(t.player=a),U(e.table.counterTime),o.$patch(t)},U=async e=>{let t={counter:e};d.$patch(t)},I=async e=>{y(e),N()},H=async e=>{const t=e.table.tableCard.gameData.roundDelay;e.table.counterTime=t/1e3,y(e),N()};var n=(e=>(e.ALL_IN="ALL_IN",e.CHECK="CHECK",e.FOLD="FOLD",e.BET_RAISE="BET_RAISE",e.CALL="CALL",e))(n||{}),v=(e=>(e.RELOAD_CHIPS="RELOAD_CHIPS",e.SIT_OUT_NEXT_HAND="SIT_OUT_NEXT_HAND",e.CONTINUE_GAME="CONTINUE_GAME",e))(v||{}),$=(e=>(e.PRE_FLOP="PRE_FLOP",e.FLOP="FLOP",e.TURN="TURN",e.RIVER="RIVER",e.ROUND_COMPLETED="ROUND_COMPLETED",e))($||{});const K=b("texasHoldemStore",{state:()=>({tables:[],table:{},command:{},mainPlayer:{},lastActor:{},holeCards:[]}),getters:{tableCards(e){return e.tables},callAmount(e){var t;return(t=e.mainPlayer.actions)==null?void 0:t.find(a=>a.type===n.CALL).range.max},hasMainPlayerHoleCards(e){return e.mainPlayer.cards&&e.mainPlayer.cards.length>=2}},actions:{async populateStore(){try{let e=await S(_.TEXAS_HOLDEM);this.tables=e}catch(e){alert(e)}},resetLastActionFromPlayer(e){var a;if(this.mainPlayer&&this.mainPlayer.userName===e){this.mainPlayer.lastAction=void 0;return}let t=(a=this.table.seats.find(r=>r.player&&r.player.userName===e))==null?void 0:a.player;t&&(t.lastAction=void 0)},clearTableAndPlayers(){E(),this.table.seats.filter(e=>e.player).map(e=>e.player).forEach(e=>{e.actions=[],e.lastAction=void 0,e.cards=void 0,e.chipsOnTable=0}),this.table.pots=[],this.mainPlayer.actions=[],this.mainPlayer.cards=void 0,this.mainPlayer.chipsOnTable=0},logout(){this.mainPlayer={}}}}),l=K(),w=A();function J(e){switch(l.$patch({command:e.title}),l.mainPlayer&&(e.table.players.find(a=>{var r;return a.userName===((r=l.mainPlayer)==null?void 0:r.userName)})||l.logout()),e.title){case s.OPEN_TABLE:x(e);break;case s.LOGIN:l.$patch({mainPlayer:e.player});break;case s.NO_BETS_NO_DEAL:l.clearTableAndPlayers();break;case s.ROUND_COMPLETED:X(e);break;default:V(e)}}const x=async e=>{let t=e.table;O(t,P.TEXAS_HOLDEM_TABLE),L(e)},L=async e=>{let t={table:e.table,command:e.title};j(e,t),z(e,t),W(e.table.counterTime),l.$patch(t)},F=e=>{if(e.title===n.ALL_IN.toString())return n.ALL_IN;if(e.title===n.BET_RAISE.toString())return n.BET_RAISE;if(e.title===n.CALL.toString())return n.CALL;if(e.title===n.FOLD.toString())return n.FOLD;if(e.title===n.CHECK.toString())return n.CHECK},W=async e=>{let t={counter:e};w.$patch(t)},V=async e=>{L(e),N()},X=async e=>{E(),L(e)},Y=e=>e.cards;function j(e,t){const a=e.table.players.find(r=>{var c;return r.userName===((c=l.mainPlayer)==null?void 0:c.userName)});l.mainPlayer&&a&&(t.mainPlayer=a,Y(e)?t.mainPlayer.cards=e.cards:l.hasMainPlayerHoleCards?t.mainPlayer.cards=l.mainPlayer.cards:t.mainPlayer.cards=void 0)}function z(e,t){const a=F(e);if(a){const r=e.table.players.find(c=>c.userName===e.player.userName);t.lastActor=r,t.lastActor.lastAction=a}}let i;const q="ws://localhost:8080/casino",Q='{ "action": "OPEN_TABLE"}';async function Ee(e,t){const a=q+`/${e}/${t}`;await Z(a.toLocaleLowerCase())}const f=e=>i.send(JSON.stringify(e)),Te=()=>i==null?void 0:i.close();function Z(e){i=new WebSocket(e),i.onopen=a=>f(JSON.parse(Q)),i.onmessage=a=>{let r=JSON.parse(a.data);if(!r.table){alert("error");return}switch(r.table.tableCard.game){case C:k(r);break;case p:J(r);break;default:throw new Error("no handler for data "+JSON.stringify(r))}},i.onerror=a=>{console.error("socket error"+JSON.stringify(a)),t()},i.onclose=a=>{console.log("socket closes, bye!"),t()};const t=()=>{console.log("closed")}}export{s as C,$ as G,m as P,v as T,se as a,re as b,ue as c,ce as d,f as e,ie as f,oe as g,Te as h,K as i,Ee as j,ne as k,n as l,ae as m,le as n,te as r,g as u};