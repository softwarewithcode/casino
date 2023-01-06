<script setup lang = "ts">
import { useCasinoStore } from "../stores/casinoStore"
import Blackjack from "./TableCardView.vue";

const store = useCasinoStore()
store.populateStore()

</script>

<template>
    <main>
        <h1> Casino tables</h1>
        <div style="display: block" v-for="(gameType, index) in store.getTypes" :key="index">
            <router-link :to="{ name: 'blackjackFront' }">{{ gameType.type }}</router-link>
        </div>
    </main>
</template>

