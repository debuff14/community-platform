<template>
  <div class="placeholder">
    <el-empty :description="`「${title}」功能将在后续里程碑开发`" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '该功能')
</script>

<style scoped>
.placeholder {
  background: #fff;
  border-radius: 8px;
  padding: 60px 0;
}
</style>
