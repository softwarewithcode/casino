import{s as c,n as l,t,F as p,v as i,u,w as m,p as a,x as y,y as _,z as d,A as g,B as f}from"./router-ca7b6956.js";import{u as k}from"./blackjackMessageHandler-a072c78d.js";/* empty css                                                                      */const h=c("casinoStore",{state:()=>({gameTypes:[],locale:"us-en"}),getters:{getTypes(e){return e.gameTypes}},actions:{async populateStore(){try{this.gameTypes=await k()}catch(e){alert(e)}},async login(){}}}),C=m("h1",null," Casino tables",-1),b=l({__name:"Casino",setup(e){const s=h();return s.populateStore(),(S,T)=>{const o=d("router-link");return a(),t("main",null,[C,(a(!0),t(p,null,i(u(s).getTypes,(n,r)=>(a(),t("div",{style:{display:"block"},key:r},[y(o,{to:{name:"blackjackFront"}},{default:_(()=>[g(f(n.type),1)]),_:2},1024)]))),128))])}}});export{b as default};