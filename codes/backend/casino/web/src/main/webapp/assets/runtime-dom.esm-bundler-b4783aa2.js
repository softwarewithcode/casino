import{i as v,b as a,e as w,f as L,g as M,h as x,j as _,k as N,l as O,m as R,n as I,p as P,q as B}from"./router-dd9501cf.js";const H="http://www.w3.org/2000/svg",l=typeof document<"u"?document:null,h=l&&l.createElement("template"),z={insert:(t,e,n)=>{e.insertBefore(t,n||null)},remove:t=>{const e=t.parentNode;e&&e.removeChild(t)},createElement:(t,e,n,i)=>{const s=e?l.createElementNS(H,t):l.createElement(t,n?{is:n}:void 0);return t==="select"&&i&&i.multiple!=null&&s.setAttribute("multiple",i.multiple),s},createText:t=>l.createTextNode(t),createComment:t=>l.createComment(t),setText:(t,e)=>{t.nodeValue=e},setElementText:(t,e)=>{t.textContent=e},parentNode:t=>t.parentNode,nextSibling:t=>t.nextSibling,querySelector:t=>l.querySelector(t),setScopeId(t,e){t.setAttribute(e,"")},insertStaticContent(t,e,n,i,s,r){const c=n?n.previousSibling:e.lastChild;if(s&&(s===r||s.nextSibling))for(;e.insertBefore(s.cloneNode(!0),n),!(s===r||!(s=s.nextSibling)););else{h.innerHTML=i?`<svg>${t}</svg>`:t;const f=h.content;if(i){const o=f.firstChild;for(;o.firstChild;)f.appendChild(o.firstChild);f.removeChild(o)}e.insertBefore(f,n)}return[c?c.nextSibling:e.firstChild,n?n.previousSibling:e.lastChild]}};function D(t,e,n){const i=t._vtc;i&&(e=(e?[e,...i]:[...i]).join(" ")),e==null?t.removeAttribute("class"):n?t.setAttribute("class",e):t.className=e}function q(t,e,n){const i=t.style,s=a(n);if(n&&!s){for(const r in n)m(i,r,n[r]);if(e&&!a(e))for(const r in e)n[r]==null&&m(i,r,"")}else{const r=i.display;s?e!==n&&(i.cssText=n):e&&t.removeAttribute("style"),"_vod"in t&&(i.display=r)}}const g=/\s*!important$/;function m(t,e,n){if(_(n))n.forEach(i=>m(t,e,i));else if(n==null&&(n=""),e.startsWith("--"))t.setProperty(e,n);else{const i=W(t,e);g.test(n)?t.setProperty(N(i),n.replace(g,""),"important"):t[i]=n}}const b=["Webkit","Moz","ms"],p={};function W(t,e){const n=p[e];if(n)return n;let i=O(e);if(i!=="filter"&&i in t)return p[e]=i;i=R(i);for(let s=0;s<b.length;s++){const r=b[s]+i;if(r in t)return p[e]=r}return e}const S="http://www.w3.org/1999/xlink";function $(t,e,n,i,s){if(i&&e.startsWith("xlink:"))n==null?t.removeAttributeNS(S,e.slice(6,e.length)):t.setAttributeNS(S,e,n);else{const r=I(e);n==null||r&&!P(n)?t.removeAttribute(e):t.setAttribute(e,r?"":n)}}function j(t,e,n,i,s,r,c){if(e==="innerHTML"||e==="textContent"){i&&c(i,s,r),t[e]=n??"";return}if(e==="value"&&t.tagName!=="PROGRESS"&&!t.tagName.includes("-")){t._value=n;const o=n??"";(t.value!==o||t.tagName==="OPTION")&&(t.value=o),n==null&&t.removeAttribute(e);return}let f=!1;if(n===""||n==null){const o=typeof t[e];o==="boolean"?n=P(n):n==null&&o==="string"?(n="",f=!0):o==="number"&&(n=0,f=!0)}try{t[e]=n}catch{}f&&t.removeAttribute(e)}function U(t,e,n,i){t.addEventListener(e,n,i)}function F(t,e,n,i){t.removeEventListener(e,n,i)}function X(t,e,n,i,s=null){const r=t._vei||(t._vei={}),c=r[e];if(i&&c)c.value=i;else{const[f,o]=J(e);if(i){const T=r[e]=Y(i,s);U(t,f,T,o)}else c&&(F(t,f,c,o),r[e]=void 0)}}const A=/(?:Once|Passive|Capture)$/;function J(t){let e;if(A.test(t)){e={};let i;for(;i=t.match(A);)t=t.slice(0,t.length-i[0].length),e[i[0].toLowerCase()]=!0}return[t[2]===":"?t.slice(3):N(t.slice(2)),e]}let d=0;const K=Promise.resolve(),Q=()=>d||(K.then(()=>d=0),d=Date.now());function Y(t,e){const n=i=>{if(!i._vts)i._vts=Date.now();else if(i._vts<=n.attached)return;B(Z(i,n.value),e,5,[i])};return n.value=t,n.attached=Q(),n}function Z(t,e){if(_(e)){const n=t.stopImmediatePropagation;return t.stopImmediatePropagation=()=>{n.call(t),t._stopped=!0},e.map(i=>s=>!s._stopped&&i&&i(s))}else return e}const E=/^on[a-z]/,G=(t,e,n,i,s=!1,r,c,f,o)=>{e==="class"?D(t,i,s):e==="style"?q(t,n,i):M(e)?x(e)||X(t,e,n,i,c):(e[0]==="."?(e=e.slice(1),!0):e[0]==="^"?(e=e.slice(1),!1):V(t,e,i,s))?j(t,e,i,r,c,f,o):(e==="true-value"?t._trueValue=i:e==="false-value"&&(t._falseValue=i),$(t,e,i,s))};function V(t,e,n,i){return i?!!(e==="innerHTML"||e==="textContent"||e in t&&E.test(e)&&v(n)):e==="spellcheck"||e==="draggable"||e==="translate"||e==="form"||e==="list"&&t.tagName==="INPUT"||e==="type"&&t.tagName==="TEXTAREA"||E.test(e)&&a(n)?!1:e in t}const nt={beforeMount(t,{value:e},{transition:n}){t._vod=t.style.display==="none"?"":t.style.display,n&&e?n.beforeEnter(t):u(t,e)},mounted(t,{value:e},{transition:n}){n&&e&&n.enter(t)},updated(t,{value:e,oldValue:n},{transition:i}){!e!=!n&&(i?e?(i.beforeEnter(t),u(t,!0),i.enter(t)):i.leave(t,()=>{u(t,!1)}):u(t,e))},beforeUnmount(t,{value:e}){u(t,e)}};function u(t,e){t.style.display=e?t._vod:"none"}const y=L({patchProp:G},z);let C;function k(){return C||(C=w(y))}const it=(...t)=>{const e=k().createApp(...t),{mount:n}=e;return e.mount=i=>{const s=tt(i);if(!s)return;const r=e._component;!v(r)&&!r.render&&!r.template&&(r.template=s.innerHTML),s.innerHTML="";const c=n(s,!1,s instanceof SVGElement);return s instanceof Element&&(s.removeAttribute("v-cloak"),s.setAttribute("data-v-app","")),c},e};function tt(t){return a(t)?document.querySelector(t):t}export{it as c,nt as v};
