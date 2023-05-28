<script setup lang = "ts">
import { useCasinoStore } from "../stores/casinoStore"
import { useViewIdToViewNameMapper } from "@/components/composables/common/Views";

const store = useCasinoStore()
store.populateStore()

</script>

<template>
    <main>
        <h1>Casino games</h1>
        <div style="display: block" v-for="(gameType, index) in store.getTypes" :key="index">
            <router-link :to="{ name: useViewIdToViewNameMapper(gameType.index) }">{{ gameType.type }}</router-link>
        </div>
    </main>
</template>

