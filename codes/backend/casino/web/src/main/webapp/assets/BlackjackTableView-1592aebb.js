import{d as _e,h as M,s as ae,i as ke,j as Te,k as v,c as C,g as Ae,t as E,u as d,n as ne,l as P,e as Le,b as B,m as Re,v as we,o as p,p as Oe,q as Ue,_ as Me}from"./index-a7a178b0.js";import{u as D,a as We,b as He,c as $e,d as F,e as K,f as _,g as z,h as oe,i as Ye,j as ze,k as je,_ as Ve}from"./TakeSeatButtonRow.vue_vue_type_script_setup_true_lang-4aaec450.js";import{a as X,b as se,u as Fe,c as w,C as j,d as Ke,e as V,f as Xe,g as qe,h as Ge}from"./websocket-a4726c1d.js";import{u as Je}from"./casinoStore-b4063b77.js";var ie=(e=>(e.MULTIPLAYER="MULTIPLAYER",e.SINGLE_PLAYER="SINGLE_PLAYER",e))(ie||{}),f=(e=>(e.TAKE="TAKE",e.DOUBLE_DOWN="DOUBLE_DOWN",e.STAND="STAND",e.SPLIT="SPLIT",e.INSURE="INSURE",e.BET="BET",e))(f||{}),S=(e=>(e.INSURE="INSURE",e.BET="BET",e.ROUND_COMPLETED="ROUND_COMPLETED",e))(S||{});const H="-",le=1500;let c,x,I,q;const O=6;function Qe(e,t,a){G(a,t,e),a.getContext("2d")&&(!t||Object.keys(t).length===0||(lt(t,a,X(e,t)),ot(t.seatNumber,e,a),it(e,a)))}function Ze(e,t,a){G(a,t,e);const s=ue(e);s.length!==0&&(tt(s,t,a),et(e,a),de(s,t,a))}const et=(e,t)=>{e.dealerHand.cards.forEach((a,s)=>{J(H,a,s,0,t)})},tt=(e,t,a)=>{e.forEach(s=>{s.hands.forEach((n,u)=>{n.cards.forEach((o,l)=>{J(s.userName,o,l,u,a)})})})};async function at(e,t,a){if(!a.getContext("2d"))return;G(a,t,e);const n=ue(e);re(n,a);const u=e.dealerHand.cards[0];await ce(le,H,u,0,0,a),re(n,a),de(n,t,a)}const ue=e=>e.seats.map(t=>t.player).filter(t=>{var a;return(t==null?void 0:t.hands)&&((a=t.hands[0])==null?void 0:a.cards.length)>0}),re=async(e,t)=>{for(let a=0;a<e.length;a++){const s=e[a],n=s.hands[0].cards.findIndex(l=>!l.hasOwnProperty("visible")),u=s.hands[0].cards[n],o=0;await ce(le,s.userName,u,n,o,t),u.visible=!0}},de=(e,t,a)=>{for(const s of e){const n=ve(s.userName),u=F(n,x,c,O);me(s.userName);const o=s.userName===t.userName?A.y:T.y;nt(s).forEach((l,y)=>{const m=u.y+o/2+o*y;l.values.length===1?D(a,{x:u.x+x-50,y:m},l.values[0].toString(),_):D(a,{x:u.x+x-50,y:m},l.values[0].toString()+"/"+l.values[1].toString(),_)})}},nt=e=>e?e.hands:[],G=(e,t,a)=>{I=t,q=a,c=e.height/4,x=e.width/4},ce=async(e,t,a,s,n,u)=>{await We(e),J(t,a,s,n,u)},J=(e,t,a,s,n)=>{const u=n.getContext("2d");if(!t||!u){console.error("cannot draw");return}const o=He(t),l=me(e),y=st(e,a,s);u.drawImage($e,o.position.x,o.position.y,o.size.x,o.size.y,y.x,y.y,l.x,l.y)};let W,T,A;const st=(e,t,a)=>{if(e===H){const l=x+t*W.x+20,y=c+c;return{x:l,y}}if(e===I.userName){const l=x+t*A.x+20,y=3*c+a*A.y;return{x:l,y}}const s=ve(e);let n=F(s,x,c,O),u=n.x+T.x*t+2,o=n.y+n.y/3+a*T.y;return{x:u,y:o}},me=e=>{if(e===H){if(!W){const a=c*.65;W={x:a*z,y:a}}return W}const t=rt(e);if(e===I.userName){if(!A){const a=t>0?c/2:c;A={x:a*z,y:a}}return A}if(!T){const a=t>0?c/2.5:c/1.5;T={x:a*z,y:a}}return T},rt=e=>{var a;const t=(a=q.seats.find(s=>{var n;return((n=s.player)==null?void 0:n.userName)===e}))==null?void 0:a.player.hands.length;if(!t)throw new Error("no hand count");return t},ot=(e,t,a)=>{if(!a.getContext("2d"))return;let n=se(e,t);for(let u=0;u<t.seats.length;u++){const o=t.seats.find(l=>l.number===n);e!==o.number&&ut(F(u,x,c,O),o,a,X(t,o.player)),n=se(n,t)}},ve=e=>{if(I.userName===e)return O;const t=q.seats.find(a=>{var s;return((s=a.player)==null?void 0:s.userName)===e});if(!t)throw new Error("player not found "+e);return I.seatNumber>t.number?O-(I.seatNumber-t.number):t.number-I.seatNumber-1},it=(e,t)=>{if(!t.getContext("2d"))return;K(t,{x,y:c},{x:2*x,y:2*c},!1);const s="Dealer"+(e.dealerHand.values[0]>0?" has "+e.dealerHand.values[0].toString():"");D(t,{x:x+10,y:c+20},s,oe)},lt=(e,t,a)=>{if(!t.getContext("2d"))return;K(t,{x:0,y:t.height*.75},{x:t.width,y:t.height},a),D(t,{x:5,y:3*c+18},e.userName,_),D(t,{x:5,y:3*c+35},"total "+e.currentBalance,oe);const n=e.totalBet!=null?e.totalBet:0;D(t,{x:5,y:3*c+55},"bet "+n,_)},ut=(e,t,a,s)=>{if(K(a,e,{x,y:c},s),!t.player){D(a,{x:e.x+10,y:e.y+50},"Seat "+t.number,_);return}D(a,{x:e.x+5,y:e.y+18},t.player.userName,_),D(a,{x:e.x+5,y:e.y+35},"$"+t.player.currentBalance,_)},dt=e=>(Oe("data-v-ec9e0467"),e=e(),Ue(),e),ct={style:{position:"relative"}},mt=dt(()=>B("canvas",{id:"canvas",width:"1800",height:"600"},null,-1)),vt={key:1,id:"takeSeatRow"},xt=["disabled"],ft=["disabled"],yt=["disabled"],gt=["disabled"],ht=["disabled"],bt=["disabled"],Ct={key:3,id:"insureRow",style:{position:"absolute"}},pt=["disabled"],Bt={id:"actionRow",style:{position:"absolute",bottom:"25px: left:50px"}},Dt=_e({__name:"BlackjackTableView",props:{tableId:{}},setup(e){const t=M(!1),a=Fe(),s=Je(),{table:n,command:u,player:o}=ae(a),{counter:l}=ae(s),y=a.$subscribe((i,r)=>{i.type==="patch object"&&xe()}),m=M(0),L=M(0);ke(()=>{t.value=!0,Ye(w()),Y(!1)}),Te(()=>{y(),Ge(),a.$reset()});const xe=()=>{var r;if(Y(n.value.gamePhase==="PLAY"&&u.value===j.INITIAL_DEAL_DONE),n.value.gamePhase!==S.ROUND_COMPLETED)return;m.value=0;const i=(r=n.value.seats.find(h=>{var b,R;return((b=h.player)==null?void 0:b.userName)===((R=o.value)==null?void 0:R.userName)}))==null?void 0:r.player;i&&(L.value=i.totalBet>n.value.tableCard.gameData.maximumBet?n.value.tableCard.gameData.maximumBet:i.totalBet)},Q=v(()=>{var i;return l.value<=0||!t.value?"":n.value.gamePhase===S.ROUND_COMPLETED?"Next round starts in "+l.value:n.value.gamePhase===S.INSURE?"Insurance time left "+l.value:u.value===j.PLAYER_TIME_START||u.value===j.INITIAL_DEAL_DONE?"Player "+((i=n.value.activePlayer)==null?void 0:i.userName)+" "+l.value:N.value?"Place your bets "+l.value:n.value.gamePhase===S.BET?"Bet phase "+l.value:""}),k=i=>{m.value=i,V({action:f.BET,amount:i}),o.value.totalBet=m.value,Y(!1)},$=v(()=>Xe(o.value)),fe=v(()=>N.value&&m.value-n.value.tableCard.gameData.minimumBet>=0),ye=v(()=>{var i;return N.value&&m.value>=0&&m.value+n.value.tableCard.gameData.minimumBet<=n.value.tableCard.gameData.maximumBet&&((i=o.value)==null?void 0:i.currentBalance)-(m.value+n.value.tableCard.gameData.minimumBet)>=0}),ge=v(()=>{var i;return N.value&&((i=o.value)==null?void 0:i.currentBalance)>=n.value.tableCard.gameData.minimumBet&&m.value!==n.value.tableCard.gameData.minimumBet}),he=v(()=>{var i;return N.value&&((i=o.value)==null?void 0:i.currentBalance)>n.value.tableCard.gameData.maximumBet}),N=v(()=>n.value.gamePhase===S.BET&&$.value&&l.value>0),be=v(()=>{const i=n.value.tableCard;return i.type===ie.MULTIPLAYER?!$.value&&n.value.seats.map(r=>r.available)!=null:i.availablePositions.length>=i.thresholds.seatCount}),Ce=v(()=>n.value.gamePhase==="PLAY"&&X(n.value,o.value)),pe=v(()=>N.value&&L.value>0&&(o==null?void 0:o.value.currentBalance)>=L.value),Be=v(()=>m.value>0),De=()=>{Z.value=!0,V({action:f.INSURE})},U=i=>V({action:i}),Ee=v(()=>qe(n.value)),Y=async i=>{const r=ze(w()),h=r.getContext("2d");if(!h)return;h.drawImage(je,0,0,r.width,r.height);const b=Ke(n.value,o.value);Qe(n.value,b,r),i?at(n.value,b,r):Ze(n.value,b,r)},Pe=()=>({display:"inline",bottom:w().height/8+"px","margin-right":"45px",left:"55px"}),Se=v(()=>{const i=(w().height*.72).toString()+"px",r=(w().width/3).toString()+"px",h=l.value>4?"yellow":"red";return{left:r,top:i,color:h,"font-size":"22px","z-index":10}}),Ne=()=>o.value.hands?o.value.hands.length===1?"Take":o.value.hands[0].active?"Take ( first )":"Take ( second )":"",Z=M(!1),Ie=v(()=>$.value&&n.value.gamePhase===S.INSURE&&o.value.currentBalance>=o.value.totalBet/2&&Z.value===!1&&o.value.hands[0].cards.length>0);return(i,r)=>{var h,b,R,ee,te;return p(),C("div",ct,[Ae(" Welcome "+E((h=d(o))==null?void 0:h.userName)+" ",1),Q.value?(p(),C("div",{key:0,style:ne([{position:"absolute"},Se.value])},E(Q.value),5)):P("",!0),mt,be.value?(p(),C("div",vt,[Le(Ve,{seats:Ee.value},null,8,["seats"])])):P("",!0),N.value?(p(),C("div",{key:2,id:"betRow",style:ne([{position:"absolute"},Pe()])},[B("button",{disabled:!fe.value,onClick:r[0]||(r[0]=g=>k(m.value-d(n).tableCard.gameData.minimumBet))}," Reduce "+E(d(n).tableCard.gameData.minimumBet),9,xt),B("button",{disabled:!ge.value,onClick:r[1]||(r[1]=g=>k(d(n).tableCard.gameData.minimumBet))}," Minimum "+E(d(n).tableCard.gameData.minimumBet),9,ft),B("button",{disabled:!pe.value,onClick:r[2]||(r[2]=g=>k(L.value))}," Previous "+E(L.value),9,yt),B("button",{disabled:!ye.value,onClick:r[3]||(r[3]=g=>k(m.value+d(n).tableCard.gameData.minimumBet))}," Add "+E(d(n).tableCard.gameData.minimumBet),9,gt),B("button",{disabled:!he.value,onClick:r[4]||(r[4]=g=>k(d(n).tableCard.gameData.maximumBet))}," Max "+E(d(n).tableCard.gameData.maximumBet),9,ht),B("button",{disabled:!Be.value,onClick:r[5]||(r[5]=g=>k(0))}," Remove bet ",8,bt)],4)):P("",!0),d(n).gamePhase===d(S).INSURE?(p(),C("div",Ct,[B("button",{disabled:!Ie.value,onClick:r[6]||(r[6]=g=>De())}," Insure ",8,pt)])):P("",!0),Re(B("div",Bt,[(b=d(n).activePlayer)!=null&&b.actions.includes(d(f).TAKE)?(p(),C("button",{key:0,onClick:r[7]||(r[7]=g=>U(d(f).TAKE))},E(Ne()),1)):P("",!0),(R=d(n).activePlayer)!=null&&R.actions.includes(d(f).SPLIT)?(p(),C("button",{key:1,onClick:r[8]||(r[8]=g=>U(d(f).SPLIT))}," Split ")):P("",!0),(ee=d(n).activePlayer)!=null&&ee.actions.includes(d(f).DOUBLE_DOWN)?(p(),C("button",{key:2,onClick:r[9]||(r[9]=g=>U(d(f).DOUBLE_DOWN))}," Double down ")):P("",!0),(te=d(n).activePlayer)!=null&&te.actions.includes(d(f).STAND)?(p(),C("button",{key:3,onClick:r[10]||(r[10]=g=>U(d(f).STAND))}," Stand ")):P("",!0)],512),[[we,Ce.value]])])}}});const It=Me(Dt,[["__scopeId","data-v-ec9e0467"]]);export{It as default};
