<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <p class="empty-title">{{ title }}</p>
    <p v-if="hint" class="empty-hint">{{ hint }}</p>
    <el-button v-if="ctaText" type="primary" round class="empty-cta" @click="$emit('cta')">
      {{ ctaText }}
    </el-button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '🔍' },
  title: { type: String, required: true },
  hint: { type: String, default: '' },
  ctaText: { type: String, default: '' }
})

defineEmits(['cta'])
</script>

<style lang="scss" scoped>
.empty-state {
  text-align: center;
  padding: 4rem 2rem;

  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    filter: saturate(0.85);
  }

  .empty-title {
    color: var(--text-secondary);
    font-size: 1.25rem;
    font-weight: 500;
    margin: 0 0 0.5rem;
  }

  .empty-hint {
    font-size: 0.95rem;
    color: var(--text-muted);
    margin: 0 0 1.25rem;
  }

  .empty-cta {
    background: linear-gradient(135deg, #81d4fa 0%, #4fc3f7 100%);
    border: none;
    font-weight: 600;
  }
}
</style>
