<script setup lang="ts">
import { useSeatTaker } from '@/components/composables/common/table';
import type { CasinoPlayer, Seat } from '@/types/casino';
const props = defineProps<{ seats: Seat<CasinoPlayer>[] }>();

const getRowStyle = (seatNumber: number) => {
    if (props.seats.some(seat => seat.player?.seatNumber >= 0))
        return { 'display': "inline", "margin-right": "45px", "left": "35px" }
    return { 'display': "inline", 'bottom': "200px", "margin-right": "45px", "left": "50px" }
}
</script>

<template>
    <div v-for="seat in seats" :key="seat.number" :id="seat.number.toString()" :style="getRowStyle(seat.number)">
        <button v-if="seat.available" @click="useSeatTaker(seat.number)">
            Take seat {{ seat.number }}
        </button>
    </div>
</template>