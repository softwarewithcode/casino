import{T as u}from"./TableCardView-3ad3086e.js";import{i}from"./websocket-93f296e7.js";import{d as m,c as o,F as r,a as c,u as _,b as t,o as n,e as d,w as h,g as a,t as s}from"./index-7eaccc1f.js";import"./casinoStore-cbc67f66.js";const p=t("h1",null,"Holdem cash game tables",-1),g=t("br",null,null,-1),x=t("br",null,null,-1),y=t("br",null,null,-1),f=t("br",null,null,-1),B=t("br",null,null,-1),k=t("br",null,null,-1),F=m({__name:"TexasHoldemFront",setup(D){const l=i();return l.populateStore(),(T,P)=>(n(),o(r,null,[p,g,(n(!0),o(r,null,c(_(l).tableCards,e=>(n(),o("div",{key:e.id},[d(u,{card:e},{default:h(()=>[a(s(e.gameData.betType.toLowerCase())+" ",1),x,a(" MinBuyIn: "+s(e.gameData.minBuyIn)+" ",1),y,a(" MaxBuyIn: "+s(e.gameData.maxBuyIn)+" ",1),f,a(" Rake: "+s(e.gameData.rakePercent*100)+"% / "+s(e.gameData.rakeCap)+" ",1),B,a(" Players: "+s(e.thresholds.maxPlayers-e.availablePositions.length)+"/"+s(e.thresholds.maxPlayers)+" ",1),k]),_:2},1032,["card"])]))),128))],64))}});export{F as default};