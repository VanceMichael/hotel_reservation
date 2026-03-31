<template>
  <div class="bookings-page container">
    <h1 class="fade-in-up">我的预订</h1>

    <div class="booking-list">
      <div v-for="(booking, i) in bookings" :key="booking.id" class="booking-card card fade-in-up" :style="{ animationDelay: `${i * 0.06}s` }">
        <div class="booking-left">
          <div class="booking-hotel">{{ booking.hotelName }}</div>
          <div class="booking-room">{{ booking.roomName }}</div>
          <div class="booking-dates">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/><path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            {{ booking.checkInDate }} — {{ booking.checkOutDate }}
          </div>
          <div class="booking-remark" v-if="booking.remark">{{ booking.remark }}</div>
        </div>
        <div class="booking-right">
          <div class="booking-price">¥{{ booking.totalPrice }}</div>
          <div class="booking-actions">
            <span :class="['status-badge', `status-${booking.status}`]">
              <span class="status-dot"></span>
              {{ statusMap[booking.status] }}
            </span>
            <button v-if="booking.status < 2" class="btn btn-outline btn-sm" @click="handleCancel(booking.id)">
              取消预订
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="bookings.length === 0 && !loading" class="empty-state fade-in-up">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none"><rect x="3" y="4" width="18" height="18" rx="2" stroke="var(--gray-300)" stroke-width="1.5"/><path d="M16 2v4M8 2v4M3 10h18" stroke="var(--gray-300)" stroke-width="1.5" stroke-linecap="round"/></svg>
      <p>暂无预订记录</p>
      <router-link to="/hotels" class="btn btn-primary">去预订</router-link>
    </div>

    <div v-if="hasMore" class="load-more">
      <button class="btn btn-outline" :disabled="loading" @click="loadMore">
        {{ loading ? '加载中...' : '加载更多' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, inject } from 'vue'
import { bookingApi } from '../api'

const showToast = inject('showToast')
const showConfirm = inject('showConfirm')
const bookings = ref([])
const loading = ref(false)
const current = ref(1)
const total = ref(0)
const size = 10

const statusMap = { 0: '待确认', 1: '已确认', 2: '已入住', 3: '已完成', 4: '已取消' }
const hasMore = computed(() => bookings.value.length < total.value)

const loadData = async () => {
  loading.value = true
  try {
    const res = await bookingApi.page({ current: current.value, size })
    bookings.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const loadMore = async () => {
  loading.value = true
  current.value++
  try {
    const res = await bookingApi.page({ current: current.value, size })
    bookings.value.push(...res.records)
  } finally { loading.value = false }
}

const handleCancel = async (id) => {
  const confirmed = await showConfirm('确定要取消此预订吗？', '取消预订')
  if (!confirmed) return
  try {
    await bookingApi.cancel(id)
    showToast('取消成功')
    loadData()
  } catch (e) { showToast(e.message || '取消失败', 'error') }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.bookings-page {
  padding: 48px 24px;

  h1 {
    font-family: var(--font-display);
    font-size: 32px;
    font-weight: 600;
    margin-bottom: 32px;
  }
}

.booking-list { display: flex; flex-direction: column; gap: 16px; }

.booking-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  transition: all var(--transition);

  &:hover { box-shadow: var(--shadow); }
}

.booking-left {
  .booking-hotel { font-size: 17px; font-weight: 600; color: var(--gray-900); margin-bottom: 4px; }
  .booking-room { color: var(--gray-500); font-size: 14px; margin-bottom: 10px; }
}

.booking-dates {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--gray-600);
  font-size: 14px;

  svg { color: var(--gray-400); }
}

.booking-remark {
  margin-top: 8px;
  color: var(--gray-400);
  font-size: 13px;
}

.booking-right {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  
  .booking-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.booking-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--gray-900);
  margin-bottom: 8px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;

  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  &.status-0 { background: var(--orange-light); color: #D97706; .status-dot { background: #D97706; } }
  &.status-1 { background: var(--blue-light); color: var(--blue); .status-dot { background: var(--blue); } }
  &.status-2 { background: var(--green-light); color: var(--green); .status-dot { background: var(--green); } }
  &.status-3 { background: var(--gray-100); color: var(--gray-500); .status-dot { background: var(--gray-400); } }
  &.status-4 { background: var(--red-light); color: var(--red); .status-dot { background: var(--red); } }
}

.btn-sm { padding: 6px 16px; font-size: 13px; }

.empty-state {
  text-align: center;
  padding: 80px 0;

  svg { margin-bottom: 16px; }
  p { color: var(--gray-400); font-size: 15px; margin-bottom: 24px; }
}

.load-more { text-align: center; margin-top: 40px; }
</style>
