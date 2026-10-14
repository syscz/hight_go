<template>
  <div
    class="media-tile"
    :class="{ selected, 'is-video': item.mediaType === 'video', grayscale }"
    @click="$emit('open', item)"
    @click.meta.prevent.stop="$emit('select', item.id)"
    @click.ctrl.prevent.stop="$emit('select', item.id)"
  >
    <input
      v-if="selectable"
      type="checkbox"
      class="tile-checkbox"
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
      class="tile-video"
      :src="frameSrc"
      preload="metadata"
      muted
      playsinline
    ></video>

    <template v-if="item.mediaType === 'video'">
      <div class="video-play-badge"><span>▶</span></div>
      <span v-if="durationText" class="video-duration">{{ durationText }}</span>
    </template>

    <div v-if="showTitle" class="tile-overlay">
      <span class="tile-title">{{ item.title || item.filename }}</span>
    </div>

    <div v-if="$slots.actions" class="tile-actions">
      <slot name="actions" />
    </div>

    <span v-if="$slots.badge" class="tile-badge"><slot name="badge" /></span>

    <button
      v-if="favoritable"
      class="tile-favorite"
      :class="{ active: item.isFavorite }"
      @click.stop="$emit('favorite', item)"
    >
      {{ item.isFavorite ? '♥' : '♡' }}
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { tileImageSrc, videoFrameSrc, formatDuration } from '@/utils/media'

const props = defineProps({
  item: { type: Object, required: true },
  selected: { type: Boolean, default: false },
  selectable: { type: Boolean, default: true },
  favoritable: { type: Boolean, default: true },
  showTitle: { type: Boolean, default: true },
  grayscale: { type: Boolean, default: false }
})

defineEmits(['open', 'select', 'favorite'])

const imgSrc = computed(() => tileImageSrc(props.item))
const frameSrc = computed(() => videoFrameSrc(props.item))
const durationText = computed(() => formatDuration(props.item.duration))
</script>

<style lang="scss" scoped>
.media-tile {
  position: relative;
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  background: var(--glass);
  transition: outline-color 0.2s;

  &.selected {
    outline: 2px solid #4fc3f7;
    outline-offset: -2px;
  }

  img,
  .tile-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  img {
    transition: transform 0.35s ease;
  }

  &:hover img {
    transform: scale(1.05);
  }

  &.grayscale img,
  &.grayscale .tile-video {
    filter: grayscale(55%);
    transition: filter 0.2s ease;
  }

  &:hover.grayscale img,
  &:hover.grayscale .tile-video {
    filter: grayscale(20%);
  }

  .tile-checkbox {
    position: absolute;
    top: 8px;
    left: 8px;
    width: 20px;
    height: 20px;
    cursor: pointer;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.2s;
  }

  @media (max-width: 768px) {
    .tile-checkbox {
      opacity: 1;
    }
  }

  .video-play-badge {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;

    span {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.45);
      border: 1.5px solid rgba(255, 255, 255, 0.8);
      color: white;
      font-size: 15px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding-left: 3px;
      backdrop-filter: blur(4px);
      transition: transform 0.2s ease, background 0.2s ease;
    }
  }

  &:hover .video-play-badge span {
    transform: scale(1.12);
    background: rgba(0, 0, 0, 0.6);
  }

  .video-duration {
    position: absolute;
    right: 6px;
    bottom: 6px;
    z-index: 1;
    background: rgba(0, 0, 0, 0.65);
    color: white;
    font-size: 0.72rem;
    font-weight: 600;
    line-height: 1;
    padding: 4px 6px;
    border-radius: 4px;
    font-variant-numeric: tabular-nums;
    pointer-events: none;
  }

  .tile-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    padding: 1.5rem 0.5rem 0.5rem;
    opacity: 0;
    transition: opacity 0.2s;
    pointer-events: none;

    .tile-title {
      color: white;
      font-size: 0.85rem;
      display: block;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  &:hover .tile-overlay {
    opacity: 1;
  }

  .tile-actions {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 1;
    display: flex;
    gap: 0.5rem;
    padding: 1.25rem 0.5rem 0.5rem;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
    opacity: 0;
    transition: opacity 0.2s;
    pointer-events: none;
  }

  &:hover .tile-actions {
    opacity: 1;
    pointer-events: auto;
  }

  .tile-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    z-index: 1;
    pointer-events: none;
  }

  .tile-favorite {
    position: absolute;
    top: 8px;
    right: 8px;
    background: rgba(0, 0, 0, 0.3);
    border: none;
    color: white;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 14px;
    opacity: 0;
    transition: opacity 0.2s;

    &.active {
      color: #ff6b6b;
    }
  }

  &:hover .tile-checkbox,
  &.selected .tile-checkbox,
  &:hover .tile-favorite,
  &.selected .tile-favorite,
  &.selected .tile-overlay {
    opacity: 1;
  }
}
</style>
