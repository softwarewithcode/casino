import{n as m}from"./websocket-93f296e7.js";import{d as l,c as t,a as u,F as p,o as n,n as c,u as d,t as b,l as _}from"./index-7eaccc1f.js";const k=["id"],y=["onClick"],S=l({__name:"TakeSeatButtonRow",props:{seats:{}},setup(o){const s=o,i=r=>s.seats.some(a=>{var e;return((e=a.player)==null?void 0:e.seatNumber)>=0})?{display:"inline","margin-right":"45px",left:"35px"}:{display:"inline",bottom:"200px","margin-right":"45px",left:"50px"};return(r,a)=>(n(!0),t(p,null,u(r.seats,e=>(n(),t("div",{key:e.number,id:e.number.toString(),style:c(i(e.number))},[e.available?(n(),t("button",{key:0,onClick:f=>d(m)(e.number)}," Take seat "+b(e.number),9,y)):_("",!0)],12,k))),128))}});export{S as _};