<template>
  <div class="booking-page container" v-if="room">
    <div class="page-top fade-in-up">
      <button class="back-link" @click="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        返回
      </button>
      <h1>确认预订</h1>
    </div>

    <div class="booking-layout">
      <form class="booking-form card fade-in-up" style="animation-delay: 0.1s" @submit.prevent="handleSubmit">
        <h3>预订信息</h3>
        <div class="form-row">
          <DatePicker 
            v-model="form.checkInDate" 
            label="入住日期" 
            :min="today" 
            required 
          />
          <DatePicker 
            v-model="form.checkOutDate" 
            label="离店日期" 
            :min="minCheckOut" 
            required 
          />
        </div>
        <div class="form-group">
          <label>备注信息</label>
          <textarea v-model="form.remark" class="input" rows="3" placeholder="如有特殊需求请在此说明（选填）"></textarea>
        </div>
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading || totalPrice <= 0">
          {{ loading ? '提交中...' : '确认预订' }}
        </button>
      </form>

      <div class="booking-summary fade-in-up" style="animation-delay: 0.15s">
        <div class="summary-card card">
          <h3>预订摘要</h3>
          <div class="summary-hotel">{{ room.hotelName }}</div>
          <div class="summary-room">{{ room.name }} · {{ room.roomType }}</div>
          <div class="summary-divider"></div>
          <div class="summary-row">
            <span>房价/晚</span>
            <span>¥{{ room.price }}</span>
          </div>
          <div class="summary-row" v-if="nights > 0">
            <span>入住天数</span>
            <span>{{ nights }} 晚</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-total">
            <span>总计</span>
            <span class="total-price">¥{{ totalPrice || '--' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { roomApi, bookingApi } from '../api'
import DatePicker from '../components/DatePicker.vue'

const route = useRoute()
const router = useRouter()
const showToast = inject('showToast')
const room = ref(null)
const loading = ref(false)
const today = new Date().toISOString().split('T')[0]
const form = reactive({ roomId: null, checkInDate: '', checkOutDate: '', remark: '' })

const minCheckOut = computed(() => {
  if (!form.checkInDate) return today
  const d = new Date(form.checkInDate)
  d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

const nights = computed(() => {
  if (!form.checkInDate || !form.checkOutDate) return 0
  return Math.max(0, Math.ceil((new Date(form.checkOutDate) - new Date(form.checkInDate)) / 86400000))
})

const totalPrice = computed(() => {
  if (!room.value || nights.value <= 0) return 0
  return (room.value.price * nights.value).toFixed(2)
})

const handleSubmit = async () => {
  if (nights.value <= 0) { showToast('请选择有效日期', 'error'); return }
  loading.value = true
  try {
    await bookingApi.create(form)
    showToast('预订成功')
    router.push('/bookings')
  } catch (e) { showToast(e.message || '预订失败', 'error') }
  finally { loading.value = false }
}

onMounted(async () => {
  form.roomId = Number(route.params.roomId)
  room.value = await roomApi.getById(route.params.roomId)
})
</script>

<style lang="scss" scoped>
.booking-page { padding: 40px 24px; }

.page-top {
  margin-bottom: 32px;

  h1 { font-family: var(--font-display); font-size: 32px; font-weight: 600; margin-top: 12px; }
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--gray-500);
  font-size: 14px;
  font-family: var(--font-body);
  cursor: pointer;
  padding: 0;

  &:hover { color: var(--primary); }
}

.booking-layout {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 28px;
  max-width: 900px;
}

.booking-form {
  padding: 32px;

  h3 { font-size: 17px; font-weight: 600; margin-bottom: 24px; color: var(--gray-900); }
}

.form-row { display: flex; gap: 16px; margin-bottom: 20px; }

.form-group {
  margin-bottom: 20px;
  flex: 1;

  label { display: block; margin-bottom: 8px; font-weight: 500; font-size: 14px; color: var(--gray-700); }
  textarea.input { resize: vertical; }
}

.btn-block { width: 100%; height: 48px; font-size: 15px; margin-top: 8px; }

.summary-card {
  padding: 28px;
  position: sticky;
  top: 100px;

  h3 { font-size: 17px; font-weight: 600; margin-bottom: 20px; color: var(--gray-900); }
}

.summary-hotel { font-weight: 600; color: var(--gray-900); margin-bottom: 4px; }
.summary-room { color: var(--gray-500); font-size: 14px; }

.summary-divider {
  height: 1px;
  background: var(--gray-100);
  margin: 16px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--gray-600);
  margin-bottom: 8px;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--gray-900);
}

@media (max-width: 768px) {
  .booking-layout { grid-template-columns: 1fr; }
}
</style>
