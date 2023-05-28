<script setup lang="ts">

import { rangeInputUpdate, type Range } from '@/types/casino';
import { ref, onMounted } from 'vue';

const props = defineProps<{ range: Range, step: number }>();
const emit = defineEmits([rangeInputUpdate])
const inputValue = ref<number>(props.range.min)

onMounted(() => {
    emit(rangeInputUpdate, props.range.min)
})

const updateValue = (event) => {
    inputValue.value = event.target.value
    emit(rangeInputUpdate, inputValue.value)
}

</script>
<template>
    <span style="padding:5px">
        <input type="range" @input="updateValue" :min="range.min" :max="range.max" :value="inputValue" :step="step"
            id="range">
    </span>
</template>
