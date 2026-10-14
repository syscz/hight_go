<template>
  <div class="calendar-page">
    <div class="calendar-header">
      <h2>日历</h2>
      <div class="nav-controls">
        <el-button size="small" @click="changeMonth(-1)">‹ 上月</el-button>
        <span class="month-label">{{ monthLabel }}</span>
        <el-button size="small" @click="changeMonth(1)">下月 ›</el-button>
        <el-button size="small" type="primary" @click="goToday">今天</el-button>
      </div>
    </div>

    <div class="calendar-card">
      <div class="weekdays">
        <span v-for="w in ['日', '一', '二', '三', '四', '五', '六']" :key="w">{{ w }}</span>
      </div>
      <div class="days-grid">
        <span
          v-for="n in firstDay"
          :key="'empty-' + n"
          class="day-cell empty"
        ></span>
        <div
          v-for="d in daysInMonth"
          :key="d"
          class="day-cell"
          :class="{
            'has-media': mediaOfDay(dateKey(d)).length > 0,
            'selected': selectedDate === dateKey(d),
            'today': dateKey(d) === todayKey
          }"
          @click="selectDay(d)"
        >
          <span class="day-num">{{ d }}</span>
          <span v-if="mediaOfDay(dateKey(d)).length > 0" class="day-count">
            {{ mediaOfDay(dateKey(d)).length }}
          </span>
        </div>
      </div>
    </div>

    <div v-if="selectedDate" class="day-media">
      <h3>{{ selectedDate }} · {{ mediaOfDay(selectedDate).length }} 个媒体</h3>
      <div v-if="mediaOfDay(selectedDate).length" class="media-grid">
        <MediaTile
          v-for="item in mediaOfDay(selectedDate)"
          :key="item.id"
          :item="item"
          :selectable="false"
          :favoritable="false"
          @open="goToDetail"
        />
      </div>
      <div v-else class="empty-day">这一天没有媒体</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/index'
import MediaTile from '@/components/MediaTile.vue'

const router = useRouter()
const timeline = ref({})

const now = new Date()
const currentYear = ref(now.getFullYear())
const currentMonth = ref(now.getMonth())
const selectedDate = ref('')
const todayKey = computed(() => dateKey(now.getDate()))

const daysInMonth = computed(() => new Date(currentYear.value, currentMonth.value + 1, 0).getDate())
const firstDay = computed(() => new Date(currentYear.value, currentMonth.value, 1).getDay())
const monthLabel = computed(() => `${currentYear.value}年${currentMonth.value + 1}月`)

const dateKey = (d) =>
  `${currentYear.value}-${String(currentMonth.value + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`

const mediaOfDay = (key) => timeline.value[key] || []

const fetchTimeline = async () => {
  try {
    const response = await api.get('/media/timeline')
    timeline.value = response.data
  } catch (error) {
    console.error('获取时间线失败', error)
  }
}

const changeMonth = (delta) => {
  let m = currentMonth.value + delta
  let y = currentYear.value
  if (m < 0) {
    m = 11
    y--
  } else if (m > 11) {
    m = 0
    y++
  }
  currentMonth.value = m
  currentYear.value = y
}

const goToday = () => {
  currentYear.value = now.getFullYear()
  currentMonth.value = now.getMonth()
  selectedDate.value = todayKey.value
}

const selectDay = (d) => {
  selectedDate.value = dateKey(d)
}

const goToDetail = (item) => {
  router.push(`/media/${item.id}`)
}

onMounted(() => {
  fetchTimeline()
})
</script>

<style lang="scss" scoped>
.calendar-page {
  max-width: 900px;
  margin: 0 auto;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;

  h2 {
    margin: 0;
    color: var(--text-primary);
  }

  .nav-controls {
    display: flex;
    align-items: center;
    gap: 0.6rem;

    .month-label {
      color: var(--text-primary);
      font-weight: 600;
      min-width: 110px;
      text-align: center;
    }
  }
}

.calendar-card {
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 1.25rem;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 0.5rem;

  span {
    text-align: center;
    color: var(--text-muted);
    font-size: 0.85rem;
    padding: 0.4rem 0;
  }
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.35rem;
}

.day-cell {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;

  &.empty {
    cursor: default;
  }

  .day-num {
    color: var(--text-secondary);
    font-size: 0.9rem;
    font-weight: 500;
  }

  .day-count {
    position: absolute;
    top: 6px;
    right: 8px;
    background: rgba(79, 195, 247, 0.25);
    color: var(--accent);
    font-size: 0.7rem;
    font-weight: 700;
    border-radius: 20px;
    padding: 1px 6px;
  }

  &:hover:not(.empty) {
    background: var(--glass-strong);
  }

  &.has-media {
    background: rgba(79, 195, 247, 0.08);
    border-color: rgba(79, 195, 247, 0.25);
  }

  &.today {
    outline: 1px solid rgba(79, 195, 247, 0.7);
  }

  &.selected {
    background: rgba(79, 195, 247, 0.22);
    border-color: #4fc3f7;
    box-shadow: var(--shadow-glow);
  }
}

.day-media {
  margin-top: 1.5rem;

  h3 {
    color: var(--text-primary);
    margin: 0 0 1rem;
    font-size: 1.05rem;
  }

  .empty-day {
    color: var(--text-muted);
    text-align: center;
    padding: 2rem;
    background: var(--glass);
    border: 1px dashed var(--glass-border);
    border-radius: var(--radius);
  }
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.45rem;
}
</style>
