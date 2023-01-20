import{v as Re}from"./runtime-dom.esm-bundler-95cf2023.js";import{S as D,a as Le,b as W,T as Oe}from"./websocket-af6a0948.js";import{a as We,C as Y}from"./blackjackMessageHandler-c5e844b2.js";import{d as He,E as H,G as ae,H as Ue,I as Me,J as y,t as v,A as ze,B as g,u as l,K as U,L as C,F as $e,v as Ve,w as S,M as je,o as x,C as Ye,D as Fe}from"./router-d03cead7.js";import{_ as Ke}from"./_plugin-vue_export-helper-c27b6911.js";var b=(e=>(e.TAKE="TAKE",e.DOUBLE_DOWN="DOUBLE_DOWN",e.STAND="STAND",e.SPLIT="SPLIT",e.INSURE="INSURE",e.BET="BET",e))(b||{}),T=(e=>(e.INSURE="INSURE",e.BET="BET",e.ROUND_COMPLETED="ROUND_COMPLETED",e))(T||{});const _={color:"blue",faceAndSize:"20px Arial"},ie={color:"E97451",faceAndSize:"22px Arial"},le=76,ue=107.2,Xe=21,Je=34.6,qe={x:le,y:ue};function Qe(e){const t={x:Ze(e),y:Ge(e)},r=t.x*le+Xe*t.x,n=t.y*ue+Je*t.y+.7;return{position:{x:r,y:n},size:qe}}const Ze=e=>{if(e.rank<8){if(e.suit===D.HEART)return 0;if(e.suit===D.DIAMOND)return 1;if(e.suit===D.CLUB)return 2;if(e.suit===D.SPADE)return 3}return e.suit===D.HEART?4:e.suit===D.DIAMOND?5:e.suit===D.CLUB?6:7},Ge=e=>e.rank===1||e.rank===8?0:e.rank===2||e.rank===9?1:e.rank===3||e.rank===10?2:e.rank===4||e.rank===11?3:e.rank===5||e.rank===12?4:e.rank===6||e.rank===13?5:6,et=document.getElementById("cardsSprite"),tt=document.getElementById("bgImage"),z="-",de=1500;let m,p;const F=.72;let P,X;const K=6;function nt(e){const t=document.documentElement.clientWidth;e.width=t>800?800:t,e.height=document.documentElement.clientHeight>800?800:document.documentElement.clientHeight}function rt(e,t,r){J(r,t,e),r.getContext("2d")&&(!t||Object.keys(t).length===0||(ht(t,r,ye(e,t)),mt(t.seatNumber,e,r),ft(e,r)))}function at(e,t,r){J(r,t,e);const n=ce(e);n.length!==0&&(ot(n,t,r),st(e,r),me(n,t,r))}const st=(e,t)=>{e.dealerHand.cards.forEach((r,n)=>{q(z,r,n,0,t)})},ot=(e,t,r)=>{e.forEach(n=>{n.hands.forEach((a,i)=>{a.cards.forEach((u,c)=>{q(n.userName,u,c,i,r)})})})};async function it(e,t,r){if(!r.getContext("2d"))return;J(r,t,e);const a=ce(e);se(a,r);const i=e.dealerHand.cards[0];await fe(de,z,i,0,0,r),se(a,r),me(a,t,r)}const ce=e=>e.seats.map(t=>t.player).filter(t=>{var r;return(t==null?void 0:t.hands)&&((r=t.hands[0])==null?void 0:r.cards.length)>0}),se=async(e,t)=>{for(let r=0;r<e.length;r++){const n=e[r],a=n.hands[0].cards.findIndex(c=>!c.hasOwnProperty("visible")),i=n.hands[0].cards[a],u=0;await fe(de,n.userName,i,a,u,t),i.visible=!0}},me=(e,t,r)=>{for(const n of e){const a=ve(n.userName),i=Q(a,p,m);he(n.userName);const u=n.userName===t.userName?R.y:w.y;lt(n).forEach((c,d)=>{const I=i.y+u/2+u*d;c.values.length===1?k(c.values[0].toString(),{x:i.x+p-50,y:I},r,_):k(c.values[0].toString()+"/"+c.values[1].toString(),{x:i.x+p-50,y:I},r,_)})}},lt=e=>e?e.hands:[],ut=e=>new Promise(t=>setTimeout(t,e)),J=(e,t,r)=>{P=t,X=r,m=e.height/4,p=e.width/4},fe=async(e,t,r,n,a,i)=>{await ut(e),q(t,r,n,a,i)},q=(e,t,r,n,a)=>{const i=a.getContext("2d");if(!t||!i){console.error("cannot draw");return}const u=Qe(t),c=he(e),d=dt(e,r,n);i.drawImage(et,u.position.x,u.position.y,u.size.x,u.size.y,d.x,d.y,c.x,c.y)};let M,w,R;const dt=(e,t,r)=>{if(e===z){const c=p+t*M.x+20,d=m+m;return{x:c,y:d}}if(e===P.userName){const c=p+t*R.x+20,d=3*m+r*R.y;return{x:c,y:d}}const n=ve(e);let a=Q(n,p,m),i=a.x+w.x*t+2,u=a.y+a.y/3+r*w.y;return{x:i,y:u}},he=e=>{if(e===z){if(!M){const r=m*.65;M={x:r*F,y:r}}return M}const t=ct(e);if(e===P.userName){if(!R){const r=t>0?m/2:m;R={x:r*F,y:r}}return R}if(!w){const r=t>0?m/2.5:m/1.5;w={x:r*F,y:r}}return w},ct=e=>{var r;const t=(r=X.seats.find(n=>{var a;return((a=n.player)==null?void 0:a.userName)===e}))==null?void 0:r.player.hands.length;if(!t)throw new Error("no hand count");return t},oe=(e,t)=>{if(e===t.seats.length-1)return 0;let r=e;return r++,r},mt=(e,t,r)=>{if(!r.getContext("2d"))return;let a=oe(e,t);for(let i=0;i<t.seats.length;i++){const u=t.seats.find(c=>c.number===a);e!==u.number&&yt(Q(i,p,m),u,r,ye(t,u.player)),a=oe(a,t)}},ye=(e,t)=>{var r;return t?t.userName===((r=e.playerInTurn)==null?void 0:r.userName):!1},ve=e=>{if(P.userName===e)return K;const t=X.seats.find(r=>{var n;return((n=r.player)==null?void 0:n.userName)===e});if(!t)throw new Error("player not found "+e);return P.seatNumber>t.number?K-(P.seatNumber-t.number):t.number-P.seatNumber-1},Q=(e,t,r)=>{let n={};return e===0?n={x:0,y:r}:e===1?n={x:0,y:0}:e===2?n={x:t,y:0}:e===3?n={x:2*t,y:0}:e===4?n={x:3*t,y:0}:e===5?n={x:3*t,y:r}:e===K&&(n={x:0,y:r*3}),n},ft=(e,t)=>{if(!t.getContext("2d"))return;Z({x:p,y:m},{x:2*p,y:2*m},t,!1);const n="Dealer"+(e.dealerHand.values[0]>0?" has "+e.dealerHand.values[0].toString():"");k(n,{x:p+10,y:m+20},t,ie)},ht=(e,t,r)=>{if(!t.getContext("2d"))return;Z({x:0,y:t.height*.75},{x:t.width,y:t.height},t,r),k(e.userName,{x:5,y:3*m+18},t,_),k("total "+e.balance,{x:5,y:3*m+35},t,ie);const a=e.totalBet!=null?e.totalBet:0;k("bet "+a,{x:5,y:3*m+55},t,_)},yt=(e,t,r,n)=>{if(Z(e,{x:p,y:m},r,n),!t.player){k("Seat "+(t.number+1),{x:e.x+10,y:e.y+50},r,_);return}k(t.player.userName,{x:e.x+5,y:e.y+18},r,_),k("$"+t.player.balance,{x:e.x+5,y:e.y+35},r,_)},Z=(e,t,r,n)=>{const a=r.getContext("2d");if(!a)return;const i=a.strokeStyle,u=a.lineWidth;n&&(a.strokeStyle="green",a.lineWidth=10),a.strokeRect(e.x,e.y,t.x,t.y),a.strokeStyle=i,a.lineWidth=u},k=(e,t,r,n)=>{const a=r.getContext("2d");if(!a||!e)return;const i=a.font,u=a.fillStyle;a.font=n.faceAndSize,a.fillStyle=n.color,a.fillText(e,t.x,t.y),a.font=i,a.fillStyle=u},vt=e=>(Ye("data-v-b78f41ca"),e=e(),Fe(),e),xt={style:{position:"relative"}},pt=vt(()=>S("canvas",{id:"canvas",width:"1800",height:"600"},null,-1)),bt={key:1,id:"takeSeatRow"},gt=["id"],Ct=["onClick"],St=["disabled"],kt=["disabled"],It=["disabled"],Bt=["disabled"],Tt=["disabled"],Et=["disabled"],Nt=["disabled"],Dt={id:"actionRow",style:{position:"absolute",bottom:"25px: left:50px"}},Pt=He({__name:"BlackjackTableView",props:{tableId:null},setup(e){const t=H(!1),r=We(),{table:n,command:a,player:i}=ae(r),{counter:u}=ae(r),c=r.$subscribe((s,o)=>{s.type==="patch object"&&xe()}),d=H(0),I=H(0);Ue(()=>{t.value=!0,nt(L()),V(!1)}),Me(()=>{c(),Le(),r.$reset()});const xe=()=>{var o;if(V(n.value.gamePhase==="PLAY"&&a.value===Y.INITIAL_DEAL_DONE),n.value.gamePhase!==T.ROUND_COMPLETED)return;d.value=0;const s=(o=n.value.seats.find(h=>{var N,B;return((N=h.player)==null?void 0:N.userName)===((B=i.value)==null?void 0:B.userName)}))==null?void 0:o.player;s&&(I.value=s.totalBet>n.value.tableCard.thresholds.maximumBet?n.value.tableCard.thresholds.maximumBet:s.totalBet)},pe=s=>{W({action:"JOIN",seat:s})},G=y(()=>{var s;return u.value<=0||!t.value?"":n.value.gamePhase===T.ROUND_COMPLETED?"Next round starts in "+u.value:n.value.gamePhase===T.INSURE?"Insurance time left "+u.value:a.value===Y.PLAYER_TIME_START||a.value===Y.INITIAL_DEAL_DONE?"Player "+((s=n.value.playerInTurn)==null?void 0:s.userName)+" "+u.value:E.value?"Place your bets "+u.value:n.value.gamePhase===T.BET?"Bet phase "+u.value:""}),A=s=>{d.value=s,W({action:b.BET,amount:s}),i.value.totalBet=d.value,V(!1)},$=y(()=>{var s;return((s=i.value)==null?void 0:s.seatNumber)>=0}),be=y(()=>E.value&&d.value-n.value.tableCard.thresholds.minimumBet>=0),ge=y(()=>{var s;return E.value&&d.value>=0&&d.value+n.value.tableCard.thresholds.minimumBet<=n.value.tableCard.thresholds.maximumBet&&((s=i.value)==null?void 0:s.balance)-(d.value+n.value.tableCard.thresholds.minimumBet)>=0}),Ce=y(()=>{var s;return E.value&&((s=i.value)==null?void 0:s.balance)>=n.value.tableCard.thresholds.minimumBet&&d.value!==n.value.tableCard.thresholds.minimumBet}),Se=y(()=>{var s;return E.value&&((s=i.value)==null?void 0:s.balance)>n.value.tableCard.thresholds.maximumBet}),E=y(()=>n.value.gamePhase===T.BET&&$.value&&u.value>0),ke=y(()=>{const s=n.value.tableCard;return s.type===Oe.MULTIPLAYER?!$.value&&n.value.seats.map(o=>o.available)!=null:s.availablePositions.length>=s.thresholds.seatCount}),Ie=y(()=>{var s;return n.value.gamePhase==="PLAY"&&n.value.playerInTurn.userName===((s=i.value)==null?void 0:s.userName)}),Be=y(()=>E.value&&I.value>0&&(i==null?void 0:i.value.balance)>=I.value),Te=y(()=>d.value>0),Ee=()=>{te.value=!0,W({action:b.INSURE})},O=s=>{W({action:s})},Ne=y(()=>n.value.seats.sort((s,o)=>s.number-o.number)),L=()=>document.getElementById("canvas"),De=()=>{const s=L(),o=s.getContext("2d");return o==null||o.clearRect(0,0,s.width,s.height),s},V=async s=>{const o=De(),h=o.getContext("2d");h&&(h.drawImage(tt,0,0,o.width,o.height),rt(n.value,j(),o),s?it(n.value,j(),o):at(n.value,j(),o))},j=()=>{var o,h;return(o=i.value)!=null&&o.userName?i.value:(h=n.value.seats.find(N=>{var B;return(B=N.player)==null?void 0:B.userName}))==null?void 0:h.player},ee=s=>n.value.seats.some(o=>{var h;return((h=o.player)==null?void 0:h.seatNumber)>=0})?{display:"inline","margin-right":"45px",left:"35px"}:{display:"inline",bottom:"200px","margin-right":"45px",left:"50px"},Pe=()=>({display:"inline",bottom:L().height/8+"px","margin-right":"45px",left:"55px"}),_e=y(()=>{const s=(L().height*.72).toString()+"px",o=(L().width/3).toString()+"px",h=u.value>4?"yellow":"red";return{left:o,top:s,color:h,"font-size":22+"px","z-index":10}}),Ae=()=>i.value.hands?i.value.hands.length===1?"Take":i.value.hands[0].active?"Take ( first )":"Take ( second )":"",te=H(!1),we=y(()=>$.value&&n.value.gamePhase===T.INSURE&&i.value.balance>=i.value.totalBet/2&&te.value===!1&&i.value.hands[0].cards.length>0);return(s,o)=>{var h,N,B,ne,re;return x(),v("div",xt,[ze(" Welcome "+g((h=l(i))==null?void 0:h.userName)+" ",1),l(G)?(x(),v("div",{key:0,style:U([{position:"absolute"},l(_e)])},g(l(G)),5)):C("",!0),pt,l(ke)?(x(),v("div",bt,[(x(!0),v($e,null,Ve(l(Ne),f=>(x(),v("div",{key:f.number,id:f.number.toString(),style:U(ee(f.number))},[f.available?(x(),v("button",{key:0,onClick:_t=>pe(f.number.toString())}," Take seat "+g(f.number+1),9,Ct)):C("",!0)],12,gt))),128))])):C("",!0),l(E)?(x(),v("div",{key:2,id:"betRow",style:U([{position:"absolute"},Pe()])},[S("button",{disabled:!l(be),onClick:o[0]||(o[0]=f=>A(d.value-l(n).tableCard.thresholds.minimumBet))}," Reduce "+g(l(n).tableCard.thresholds.minimumBet),9,St),S("button",{disabled:!l(Ce),onClick:o[1]||(o[1]=f=>A(l(n).tableCard.thresholds.minimumBet))}," Minimum "+g(l(n).tableCard.thresholds.minimumBet),9,kt),S("button",{disabled:!l(Be),onClick:o[2]||(o[2]=f=>A(I.value))}," Previous "+g(I.value),9,It),S("button",{disabled:!l(ge),onClick:o[3]||(o[3]=f=>A(d.value+l(n).tableCard.thresholds.minimumBet))}," Add "+g(l(n).tableCard.thresholds.minimumBet),9,Bt),S("button",{disabled:!l(Se),onClick:o[4]||(o[4]=f=>A(l(n).tableCard.thresholds.maximumBet))}," Max "+g(l(n).tableCard.thresholds.maximumBet),9,Tt),S("button",{disabled:!l(Te),onClick:o[5]||(o[5]=f=>A(0))}," Remove bet ",8,Et)],4)):C("",!0),l(n).gamePhase===l(T).INSURE?(x(),v("div",{key:3,id:"insureRow",style:U([{position:"absolute"},ee])},[S("button",{disabled:!l(we),onClick:o[6]||(o[6]=f=>Ee())}," Insure ",8,Nt)])):C("",!0),je(S("div",Dt,[(N=l(n).playerInTurn)!=null&&N.actions.includes(l(b).TAKE)?(x(),v("button",{key:0,onClick:o[7]||(o[7]=f=>O(l(b).TAKE))},g(Ae()),1)):C("",!0),(B=l(n).playerInTurn)!=null&&B.actions.includes(l(b).SPLIT)?(x(),v("button",{key:1,onClick:o[8]||(o[8]=f=>O(l(b).SPLIT))}," Split ")):C("",!0),(ne=l(n).playerInTurn)!=null&&ne.actions.includes(l(b).DOUBLE_DOWN)?(x(),v("button",{key:2,onClick:o[9]||(o[9]=f=>O(l(b).DOUBLE_DOWN))}," Double down ")):C("",!0),(re=l(n).playerInTurn)!=null&&re.actions.includes(l(b).STAND)?(x(),v("button",{key:3,onClick:o[10]||(o[10]=f=>O(l(b).STAND))}," Stand ")):C("",!0)],512),[[Re,l(Ie)]])])}}});const Wt=Ke(Pt,[["__scopeId","data-v-b78f41ca"]]);export{Wt as default};