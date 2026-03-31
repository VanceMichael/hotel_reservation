<template>
  <div class="hotels-page">
    <div class="page-hero">
      <div class="container">
        <h1 class="fade-in-up">探索酒店</h1>
        <p class="fade-in-up" style="animation-delay: 0.1s">发现适合您的完美住所</p>
        <div class="search-box fade-in-up" style="animation-delay: 0.2s">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/><path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          <input v-model="keyword" type="text" class="search-input" placeholder="搜索酒店名称、地点..." @keyup.enter="loadData" />
          <button class="btn btn-primary" @click="loadData">搜索</button>
        </div>
      </div>
    </div>

    <div class="container hotel-content">
      <div class="hotel-list">
        <div v-for="(hotel, i) in hotels" :key="hotel.id" class="hotel-item fade-in-up" :style="{ animationDelay: `${i * 0.06}s` }" @click="$router.push(`/hotels/${hotel.id}`)">
          <div class="hotel-img-wrap">
            <img :src="hotel.coverImage" :alt="hotel.name" />
            <div class="hotel-rating">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              {{ hotel.rating }}
            </div>
          </div>
          <div class="hotel-body">
            <h3>{{ hotel.name }}</h3>
            <p class="hotel-addr">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/></svg>
              {{ hotel.address }}
            </p>
            <p class="hotel-desc">{{ hotel.description }}</p>
            <div class="hotel-meta">
              <span class="hotel-phone">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z" stroke="currentColor" stroke-width="2"/></svg>
                {{ hotel.phone }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="hotels.length === 0 && !loading" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none"><path d="M3 21V7l9-4 9 4v14" stroke="var(--gray-300)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 21V13h6v8" stroke="var(--gray-300)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        <p>暂无酒店数据</p>
      </div>

      <div v-if="hasMore" class="load-more">
        <button class="btn btn-outline" :disabled="loading" @click="loadMore">
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { hotelApi } from '../api'

const hotels = ref([])
const keyword = ref('')
const loading = ref(false)
const current = ref(1)
const total = ref(0)
const size = 6

const hasMore = computed(() => hotels.value.length < total.value)

const loadData = async () => {
  loading.value = true
  current.value = 1
  try {
    const res = await hotelApi.page({ current: 1, size, name: keyword.value, status: 1 })
    hotels.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const loadMore = async () => {
  loading.value = true
  current.value++
  try {
    const res = await hotelApi.page({ current: current.value, size, name: keyword.value, status: 1 })
    hotels.value.push(...res.records)
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.page-hero {
  background: var(--gray-50);
  padding: 60px 0 48px;
  border-bottom: 1px solid var(--gray-100);

  h1 {
    font-family: var(--font-display);
    font-size: 36px;
    font-weight: 600;
    color: var(--gray-900);
    margin-bottom: 8px;
  }

  p { color: var(--gray-500); font-size: 16px; margin-bottom: 28px; }
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1.5px solid var(--gray-200);
  border-radius: var(--radius);
  padding: 6px 6px 6px 18px;
  max-width: 560px;
  gap: 12px;
  transition: border-color var(--transition);

  &:focus-within { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06); }

  svg { color: var(--gray-400); flex-shrink: 0; }
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  font-family: var(--font-body);
  color: var(--gray-900);

  &::placeholder { color: var(--gray-400); }
}

.hotel-content { padding: 40px 24px 0; }

.hotel-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hotel-item {
  display: flex;
  background: #fff;
  border: 1px solid var(--gray-100);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition);

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-2px);

    img { transform: scale(1.05); }
  }
}

.hotel-img-wrap {
  width: 280px;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
}

.hotel-rating {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
  color: var(--accent);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hotel-body {
  padding: 24px 28px;
  flex: 1;
  display: flex;
  flex-direction: column;

  h3 { font-size: 19px; font-weight: 600; color: var(--gray-900); margin-bottom: 8px; }
}

.hotel-addr {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--gray-500);
  font-size: 14px;
  margin-bottom: 12px;

  svg { color: var(--gray-400); flex-shrink: 0; }
}

.hotel-desc {
  color: var(--gray-500);
  font-size: 14px;
  line-height: 1.7;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hotel-meta {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--gray-100);
}

.hotel-phone {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--gray-500);
  font-size: 13px;

  svg { color: var(--gray-400); }
}

.empty-state {
  text-align: center;
  padding: 80px 0;

  svg { margin-bottom: 16px; }
  p { color: var(--gray-400); font-size: 15px; }
}

.load-more { text-align: center; margin-top: 40px; }

@media (max-width: 768px) {
  .hotel-item { flex-direction: column; }
  .hotel-img-wrap { width: 100%; height: 200px; }
}
</style>
