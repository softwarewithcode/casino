import{d as he,h as L,i as ye,c as g,b,o as f,s as re,j as je,k as p,g as ue,t as N,u as y,n as ce,l as T,e as pe,m as Ge,v as Ye,F as qe,p as Ke,q as Je,_ as Qe}from"./index-a7a178b0.js";import{a as Z,k as V,P as E,b as de,G as Ze,r as G,i as et,C as tt,d as ot,e as I,l as m,T as Y,m as at,g as st,h as nt}from"./websocket-a4726c1d.js";import{u as it}from"./casinoStore-b4063b77.js";import{l as lt,m as rt,n as ut,o as ct,p as pt,q as dt,r as me,e as U,u as C,s as D,t as W,v as K,w as ht,d as yt,x as mt,k as gt,y as q,f as F,h as J,z as ft,A as xt,j as Ct,_ as vt}from"./TakeSeatButtonRow.vue_vue_type_script_setup_true_lang-4aaec450.js";const X=(e,t)=>{const a=e.y+t;return{x:e.x,y:a}},A=(e,t)=>({x:e.x+t,y:e.y}),w=(e,t,a)=>{const l=e.x+t,i=e.y+a;return{x:l,y:i}},At={value:1e4,image:lt},kt={value:1e3,image:rt},Tt={value:100,image:ut},bt={value:10,image:ct},St={value:5,image:pt},_t={value:1,image:dt},ee=e=>{if(e===0)return{};let t=e;const a=Math.floor(t/1e4);a>=1&&(t-=a*1e4);const l=Math.floor(t/1e3);l>=1&&(t-=l*1e3);const i=Math.floor(t/100);i>=1&&(t-=i*100);const n=Math.floor(t/10);n>=1&&(t-=n*10);const r=Math.floor(t/5);r>=1&&(t-=r*5);const c=Math.floor(t/1);c>=1&&(t-=c);const u=new Map;return u.set(At,a),u.set(kt,l),u.set(Tt,i),u.set(bt,n),u.set(St,r),u.set(_t,c),{chips:u}};let x,v,M,Q,z,te,B,k,H;const It="Pot:";let j,ge,oe;function Bt(e,t,a){Pt(a,t,e),a.getContext("2d")&&(Ft(a),!(!t||!t.userName)&&(wt(t,a,Z(e,t)),Et(a,t.seatNumber,e),Ot(a,e),Rt(a,e)))}const Pt=(e,t,a)=>{te=a,x=e.height/4,v=e.width/4,Q=e.width/4,M=e.width,z={x:0,y:e.height*.75},B={x:e.width/25,y:e.height/25},e.width/20,a.seats.length-1,e.width/2,e.height/2,k={x:75,y:75},j={x:v*1.25,y:1.5*x},ge=A(j,v),H={x:k.x/2,y:k.y/2};const l=e.width/25;oe={x:l,y:l}};function fe(e,t){K(e,q,{x:0,y:0},{x:q.width,y:q.height},t,oe)}const wt=(e,t,a)=>{var c;if(!t.getContext("2d"))return;const i=V(e,E.ACTIVE)?void 0:me;U(t,{x:0,y:t.height*.75},{x:t.width,y:t.height},a,i),C(t,{x:5,y:3*x+18},e.userName,F),C(t,{x:5,y:3*x+35},"total "+e.currentBalance,J);const n={x:0,y:t.height*.75},r={x:t.width/3,y:n.y+10};if(e.cards&&e.cards.length>=2?(D(t,e.cards[0],r,k),D(t,e.cards[1],A(r,95),k)):e.cards&&e.cards.length===0&&(console.log("mainPlayerCards:"+e.cards.length),W(t,r,H),W(t,A(r,50),H)),e.chipsOnTable>0){const u=ee(e.chipsOnTable),P=w(z,M/3,-B.y-10),O=A(P,250);C(t,X(P,-10),e.chipsOnTable.toFixed(2),J),ae(t,P,O,u)}if(((c=te.button)==null?void 0:c.seatNumber)===e.seatNumber){const u=w(z,M*.75,-25);fe(t,u)}if(e.lastAction){const u=z,P={x:M,y:Q},O=w(u,M/3,Q/2);xe(t,u,P,O,e)}},xe=(e,t,a,l,i)=>{i.lastAction&&(U(e,t,a,!1,ft),C(e,l,i.lastAction,xt))},Nt=(e,t,a,l)=>{var n;if(U(e,t,{x:v,y:x},l),!a.player){C(e,{x:t.x+10,y:t.y+50},"Seat "+a.number,F);return}V(a.player,E.ACTIVE)||U(e,t,{x:v,y:x},l,me),C(e,{x:t.x+5,y:t.y+18},a.player.userName,F),C(e,{x:t.x+5,y:t.y+35},"$"+a.player.currentBalance,F);const i=w(t,50,50);if(a.player.cards&&a.player.cards.length>=2?(D(e,a.player.cards[0],i,k),D(e,a.player.cards[1],A(i,k.x),k)):a.player.cards&&a.player.cards.length===0&&(W(e,i,H),W(e,A(i,50),H)),C(e,{x:t.x+5,y:t.y+35},"$"+a.player.currentBalance,F),a.player.chipsOnTable>0){const r=ee(a.player.chipsOnTable),c=X(t,x-B.y);C(e,X(c,-10),a.player.chipsOnTable.toFixed(2),J);const u=w(t,v,x-20);ae(e,c,u,r)}if(((n=te.button)==null?void 0:n.seatNumber)===a.number){const r=w(t,v-oe.x,0);fe(e,r)}if(a.player.lastAction){const r=t,c={x:v,y:x},u=w(r,20,x/2);xe(e,r,c,u,a.player)}},ae=(e,t,a,l)=>{let i=0;for(let[n,r]of l.chips.entries())if(!(r<1))for(let c=0;c<r;c++){const u=A(t,B.x*i);u.x>A(a,-B.x).x?K(e,ht,{x:0,y:0},{x:n.image.width,y:n.image.height},A(a,-B.x),B):K(e,n.image,{x:0,y:0},{x:n.image.width,y:n.image.height},u,B),i++}},Et=(e,t,a)=>{if(!e.getContext("2d"))return;let i=de(t,a);for(let n=0;n<a.seats.length;n++){const r=a.seats.find(c=>c.number===i);if(t!==r.number){const c=yt(n,v,x,a.seats.length-1);Nt(e,c,r,Z(a,r.player))}i=de(i,a)}};function Ft(e){mt(e,gt,{x:0,y:0},{x:e.width,y:e.height})}function Ot(e,t){if(t.pots)for(const a of t.pots){const l=It+a.amountWithTableChips.toFixed(2);if(t.gamePhase!==Ze.PRE_FLOP){const i=ee(a.amountWithTableChips);ae(e,j,ge,i)}C(e,X(j,-20),l,F)}}function Rt(e,t){if(!t.tableCards||t.tableCards.length===0)return;const a={x:v*1.15,y:e.height/2};for(let l=0;l<t.tableCards.length;l++)D(e,t.tableCards[l],A(a,l*k.x),k)}const Lt={style:{padding:"5px"}},Mt=["min","max","value","step"],Vt=he({__name:"RangeView",props:{range:{},step:{}},emits:[G],setup(e,{emit:t}){const a=e,l=L(a.range.min);ye(()=>{t(G,a.range.min)});const i=n=>{l.value=n.target.value,t(G,l.value)};return(n,r)=>(f(),g("span",Lt,[b("input",{type:"range",onInput:i,min:n.range.min,max:n.range.max,value:l.value,step:n.step,id:"range"},null,40,Mt)]))}}),Dt=e=>(Ke("data-v-b3848918"),e=e(),Je(),e),Ht={style:{position:"relative"}},$t=Dt(()=>b("canvas",{id:"canvas",width:"800",height:"600"},null,-1)),zt={key:1,id:"takeSeatRow"},Ut={key:0,style:{position:"relative",top:"-55px",float:"left"}},Wt={style:{position:"relative",top:"-55px"}},Xt={key:3,style:{border:"1px solid black",padding:"10px"}},jt=he({__name:"TexasHoldemTableView",props:{tableId:{}},setup(e){const t=et(),a=it(),l=L(!1),{table:i,mainPlayer:n,lastActor:r,command:c}=re(t),{counter:u}=re(a),P=t.$subscribe((o,s)=>{o.type==="patch object"&&O()}),O=()=>{if(Ce()){Ae();const o=r.value;setTimeout(ke,2100,o)}R(),c.value===tt.ROUND_COMPLETED&&setTimeout(ve,2500)},Ce=()=>{var o;return(o=r.value)==null?void 0:o.lastAction},ve=()=>{t.clearTableAndPlayers(),R()},Ae=()=>{var s;const o=(s=i.value.seats.find(d=>d.player&&d.player.userName===r.value.userName))==null?void 0:s.player;o&&(o.lastAction=r.value.lastAction)};ye(()=>{l.value=!0,R()}),je(()=>{P(),nt()});const ke=o=>{be(o),R()},R=async()=>{const o=Te(),s=ot(i.value,n.value);Bt(i.value,s,o)},Te=()=>{const o=Ct(document.getElementById("canvas"));if(!o.getContext("2d"))throw new Error("no canvas");return o},be=o=>{t.resetLastActionFromPlayer(o.userName)},se=p(()=>{var o;return((o=n.value)==null?void 0:o.seatNumber)>=0}),Se=p(()=>i.value.tableCard.availablePositions.length>0&&!se.value),_e=p(()=>{var o;return(o=n.value.actions)==null?void 0:o.flatMap(s=>s.type).includes(m.CHECK)}),Ie=p(()=>{var o;return(o=n.value.actions)==null?void 0:o.flatMap(s=>s.type).includes(m.BET_RAISE)}),Be=p(()=>{var o;return(o=n.value.actions)==null?void 0:o.flatMap(s=>s.type).includes(m.CALL)}),Pe=p(()=>{var o;return(o=n.value.actions)==null?void 0:o.flatMap(s=>s.type).includes(m.FOLD)}),we=p(()=>{var o;return(o=n.value.actions)==null?void 0:o.flatMap(s=>s.type).includes(m.ALL_IN)}),Ne=p(()=>{var o,s;return((s=(o=n.value)==null?void 0:o.actions)==null?void 0:s.length)>0&&Z(i.value,n.value)&&V(n.value,E.ACTIVE)}),Ee=p(()=>n.value.currentBalance<=i.value.tableCard.gameData.bigBlind+i.value.tableCard.gameData.smallBlind&&!ie.value),Fe=p(()=>st(i.value)),h=p(()=>{var o,s;return(s=(o=n.value.actions)==null?void 0:o.find(d=>d.type===m.BET_RAISE))==null?void 0:s.range}),Oe=p(()=>i.value.seats.filter(o=>o.player).map(o=>o.player).some(o=>o.chipsOnTable>0)?"RaiseTo":"Bet"),Re=p(()=>({left:"200px",top:"350px",color:"yellow","font-size":"22px","z-index":10})),Le=p(()=>{const o={"background-color":"green","font-size":"120%"};return(V(n.value,E.SIT_OUT)||$.value)&&(o["background-color"]="red"),o}),Me=p(()=>h.value?h.value.max*.001:0),Ve=()=>n.value.currentBalance+n.value.chipsOnTable,S=L(h.value===void 0?0:h.value.min),De=o=>S.value=parseFloat(o),ne=o=>{!S.value||!h.value||(S.value=o(S.value,Me.value,h.value),S.value=Number(S.value.toFixed(2)))},He=(o,s,d)=>o-s>=d.min?o-s:d.min,$e=(o,s,d)=>o+s<=d.max?o+s:d.max,ze=o=>I({action:m.BET_RAISE,amount:o}),$=L(!1),Ue=()=>{!$.value&&V(n.value,[E.NEW,E.ACTIVE])?(I({action:Y.SIT_OUT_NEXT_HAND}),$.value=!0):($.value=!1,I({action:Y.CONTINUE_GAME}))},We=()=>{I({action:m.FOLD}),n.value.cards=[],R()},ie=L(!1),Xe=()=>{I({action:Y.RELOAD_CHIPS}),ie.value=!0};return(o,s)=>{var d,le;return f(),g(qe,null,[ue(" Texas holdem table "),b("div",Ht,[ue(" Welcome "+N((d=y(n))==null?void 0:d.userName)+" ",1),y(at)()&&y(u)>0?(f(),g("div",{key:0,style:ce([{position:"absolute"},Re.value])},N(y(u)),5)):T("",!0),$t,Se.value?(f(),g("div",zt,[pe(vt,{seats:Fe.value},null,8,["seats"])])):T("",!0),b("div",null,[se.value?(f(),g("span",Ut,[b("button",{onClick:s[0]||(s[0]=_=>Ue()),id:"sitOut",name:"sitOut",style:ce(Le.value)}," 🕐 ",4)])):T("",!0),Ge(b("div",Wt,[Pe.value?(f(),g("button",{key:0,onClick:s[1]||(s[1]=_=>We())}," Fold ")):T("",!0),_e.value?(f(),g("button",{key:1,onClick:s[2]||(s[2]=_=>y(I)({action:y(m).CHECK}))}," Check ")):T("",!0),Be.value?(f(),g("button",{key:2,onClick:s[3]||(s[3]=_=>y(I)({action:y(m).CALL}))}," Call "+N(y(t).callAmount),1)):T("",!0),Ie.value&&((le=h.value)!=null&&le.min)?(f(),g("span",Xt,[pe(Vt,{range:h.value,step:h.value.max*.01,min:h.value.min,max:h.value.max,onRangeInputUpdate:De},null,8,["range","step","min","max"]),b("button",{onClick:s[4]||(s[4]=_=>ne(He))}," -"),b("button",{onClick:s[5]||(s[5]=_=>ze(S.value))},N(Oe.value)+" "+N(S.value),1),b("button",{onClick:s[6]||(s[6]=_=>ne($e))}," +")])):T("",!0),we.value?(f(),g("button",{key:4,onClick:s[7]||(s[7]=_=>y(I)({action:y(m).ALL_IN})),style:{position:"relative",float:"right"}}," All in "+N(Ve()),1)):T("",!0)],512),[[Ye,Ne.value]]),Ee.value?(f(),g("button",{key:1,onClick:s[8]||(s[8]=_=>Xe()),style:{position:"relative",float:"right",top:"-55px"}}," Reload ")):T("",!0)])])],64)}}});const Jt=Qe(jt,[["__scopeId","data-v-b3848918"]]);export{Jt as default};