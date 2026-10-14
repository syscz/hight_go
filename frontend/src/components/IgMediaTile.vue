<template>
  <div
    class="ig-tile"
    :class="{ selected }"
    @click="$emit('open', item)"
    @click.meta.prevent.stop="$emit('select', item.id)"
    @click.ctrl.prevent.stop="$emit('select', item.id)"
  >
    <input
      v-if="selectable"
      type="checkbox"
      class="ig-checkbox"
      :checked="selected"
      @click.stop
      @change="$emit('select', item.id)"
    />

    <img
      v-if="imgSrc"
      :src="imgSrc"
      :alt="item.title || item.filename"
      loading="lazy"
      v-img-fade
    />
    <video
      v-else-if="item.mediaType === 'video'"
      class="ig-video"
      :src="frameSrc"
      preload="metadata"
      muted
      playsinline
    ></video>

    <template v-if="item.mediaType === 'video'">
      <div class="ig-play"><span>▶</span></div>
      <span v-if="durationText" class="ig-duration">{{ durationText }}</span>
    </template>

    <div class="ig-veil">
      <span class="ig-veil-item" :class="{ fav: item.isFavorite }">
        {{ item.isFavorite ? '♥' : '♡' }}
      </span>
      <span class="ig-veil-item">👁 {{ item.viewCount || 0 }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { tileImageSrc, videoFrameSrc, formatDuration } from '@/utils/media'

const props = defineProps({
  item: { type: Object, required: true },
  selected: { type: Boolean, default: false },
  selectable: { type: Boolean, default: true }
})

defineEmits(['open', 'select'])

const imgSrc = computed(() => tileImageSrc(props.item))
const frameSrc = computed(() => videoFrameSrc(props.item))
const durationText = computed(() => formatDuration(props.item.duration))
</script>

<style lang="scss" scoped>
.ig-tile {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  cursor: pointer;
  background: var(--glass);

  &.selected {
    outline: 2px solid #4fc3f7;
    outline-offset: -2px;
  }

  img,
  .ig-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .ig-checkbox {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 20px;
    height: 20px;
    cursor: pointer;
    z-index: 3;
    opacity: 0;
    transition: opacity 0.2s;
    accent-color: #4fc3f7;
  }

  .ig-play {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 1;
    pointer-events: none;

    span {
      font-size: 0.9rem;
      color: white;
      text-shadow: 0 1px 4px rgba(0, 0, 0, 0.7);
    }
  }

  .ig-duration {
    position: absolute;
    top: 8px;
    right: 26px;
    z-index: 1;
    background: rgba(0, 0, 0, 0.65);
    color: white;
    font-size: 0.7rem;
    font-weight: 600;
    line-height: 1;
    padding: 3px 5px;
    border-radius: 4px;
    font-variant-numeric: tabular-nums;
    pointer-events: none;
  }

  .ig-veil {
    position: absolute;
    inset: 0;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1.4rem;
    background: rgba(0, 0, 0, 0.45);
    opacity: 0;
    transition: opacity 0.2s ease;
    pointer-events: none;

    .ig-veil-item {
      color: white;
      font-size: 1.05rem;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
      text-shadow: 0 1px 6px rgba(0, 0, 0, 0.6);

      &.fav {
        color: #ff6b6b;
      }
    }
  }

  &:hover .ig-veil {
    opacity: 1;
  }

  &:hover .ig-checkbox,
  &.selected .ig-checkbox {
    opacity: 1;
  }
}

@media (hover: none) {
  /* 触屏设备：无 hover，隐藏数据遮罩，常显多选框 */
  .ig-tile .ig-veil {
    display: none;
  }

  .ig-tile .ig-checkbox {
    opacity: 1;
  }
}
</style>
