<template>
  <div class="hotel-detail" v-if="hotel">
    <div class="hero" :style="{ backgroundImage: `url(${hotel.coverImage})` }">
      <div class="hero-overlay">
        <div class="container hero-content">
          <button class="back-btn" @click="$router.back()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            返回
          </button>
          <div class="hero-info">
            <div class="hotel-rating-lg">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              {{ hotel.rating }}
            </div>
            <h1>{{ hotel.name }}</h1>
            <div class="hero-meta">
              <span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/></svg>
                {{ hotel.address }}
              </span>
              <span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z" stroke="currentColor" stroke-width="2"/></svg>
                {{ hotel.phone }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="container content">
      <section class="desc-section fade-in-up">
        <h2>关于酒店</h2>
        <p>{{ hotel.description }}</p>
      </section>

      <section class="rooms-section">
        <h2 class="fade-in-up">可预订房间</h2>
        <div class="room-list">
          <div v-for="(room, i) in rooms" :key="room.id" class="room-card fade-in-up" :style="{ animationDelay: `${i * 0.06}s` }">
            <div class="room-body">
              <div class="room-type-badge">{{ room.roomType }}</div>
              <h3>{{ room.name }}</h3>
              <div class="room-capacity">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke="currentColor" stroke-width="2"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/></svg>
                可住 {{ room.capacity }} 人
              </div>
              <div class="amenities" v-if="room.amenities">
                <span v-for="(a, j) in parseAmenities(room.amenities)" :key="j" class="amenity-tag">{{ a }}</span>
              </div>
            </div>
            <div class="room-action">
              <div class="room-price">
                <span class="price-amount">¥{{ room.price }}</span>
                <span class="price-unit">/晚</span>
              </div>
              <button class="btn btn-primary" @click="handleBook(room.id)">立即预订</button>
            </div>
          </div>
        </div>
        <div v-if="rooms.length === 0" class="empty-state">
          <p>暂无可预订房间</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { hotelApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showToast = inject('showToast')
const hotel = ref(null)
const rooms = ref([])

const parseAmenities = (str) => { try { return JSON.parse(str) } catch { return [] } }

const handleBook = (roomId) => {
  if (!userStore.token) { showToast('请先登录', 'warning'); router.push('/login'); return }
  router.push(`/booking/${roomId}`)
}

onMounted(async () => {
  const id = route.params.id
  hotel.value = await hotelApi.getById(id)
  rooms.value = await hotelApi.getRooms(id)
})
</script>

<style lang="scss" scoped>
.hero {
  height: 380px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.9) 0%, rgba(15, 23, 42, 0.3) 100%);
  display: flex;
  align-items: flex-end;
}

.hero-content { padding-bottom: 48px; width: 100%; }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-family: var(--font-body);
  cursor: pointer;
  margin-bottom: 24px;
  transition: all var(--transition);

  &:hover { background: rgba(255, 255, 255, 0.2); }
}

.hotel-rating-lg {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(201, 169, 110, 0.2);
  color: var(--accent);
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.hero-info {
  h1 {
    font-family: var(--font-display);
    font-size: 40px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 12px;
  }
}

.hero-meta {
  display: flex;
  gap: 24px;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--gray-300);
    font-size: 15px;

    svg { color: var(--gray-400); }
  }
}

.content { padding: 48px 24px; }

.desc-section {
  background: var(--gray-50);
  border-radius: var(--radius);
  padding: 32px;
  margin-bottom: 48px;
  border: 1px solid var(--gray-100);

  h2 { font-size: 18px; font-weight: 600; margin-bottom: 16px; color: var(--gray-900); }
  p { color: var(--gray-600); line-height: 1.8; }
}

.rooms-section {
  h2 { font-size: 22px; font-weight: 600; margin-bottom: 24px; color: var(--gray-900); }
}

.room-list { display: flex; flex-direction: column; gap: 16px; }

.room-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px;
  background: #fff;
  border: 1px solid var(--gray-100);
  border-radius: var(--radius);
  transition: all var(--transition);

  &:hover { box-shadow: var(--shadow); border-color: var(--gray-200); }
}

.room-type-badge {
  display: inline-block;
  background: var(--blue-light);
  color: var(--blue);
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}

.room-body {
  h3 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin-bottom: 8px; }
}

.room-capacity {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--gray-500);
  font-size: 14px;
  margin-bottom: 12px;

  svg { color: var(--gray-400); }
}

.amenities { display: flex; flex-wrap: wrap; gap: 6px; }

.amenity-tag {
  background: var(--gray-50);
  border: 1px solid var(--gray-100);
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  color: var(--gray-600);
}

.room-action { text-align: right; }

.room-price {
  margin-bottom: 14px;

  .price-amount { font-size: 28px; font-weight: 700; color: var(--gray-900); }
  .price-unit { color: var(--gray-400); font-size: 14px; }
}

.empty-state { text-align: center; padding: 60px; color: var(--gray-400); }
</style>
