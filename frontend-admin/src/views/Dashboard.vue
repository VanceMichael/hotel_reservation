<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div v-for="(item, i) in stats" :key="item.title" class="stat-card" :style="{ animationDelay: `${i * 0.08}s` }">
        <div class="stat-header">
          <div class="stat-icon" :style="{ background: item.bg }">
            <el-icon :size="22" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-trend up" v-if="item.value > 0">
            <el-icon :size="12"><Top /></el-icon>
          </div>
        </div>
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-title">{{ item.title }}</div>
      </div>
    </div>

    <div class="card fade-in-up" style="animation-delay: 0.3s">
      <div class="card-header">
        <h3>最近预订</h3>
        <el-button text type="primary" @click="$router.push('/bookings')">
          查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="bookings" style="width: 100%">
        <el-table-column prop="id" label="订单号" width="80">
          <template #default="{ row }">
            <span class="order-id">#{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="客户" />
        <el-table-column prop="hotelName" label="酒店" />
        <el-table-column prop="roomName" label="房间" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi, hotelApi, roomApi, bookingApi } from '../api'

const stats = ref([
  { title: '用户总数', value: 0, icon: 'Avatar', color: '#3B82F6', bg: '#EFF6FF' },
  { title: '酒店数量', value: 0, icon: 'School', color: '#10B981', bg: '#ECFDF5' },
  { title: '房间数量', value: 0, icon: 'Key', color: '#F59E0B', bg: '#FFFBEB' },
  { title: '预订数量', value: 0, icon: 'Calendar', color: '#EF4444', bg: '#FEF2F2' }
])
const bookings = ref([])
const statusMap = {
  0: { text: '待确认', type: 'warning' },
  1: { text: '已确认', type: 'primary' },
  2: { text: '已入住', type: 'success' },
  3: { text: '已完成', type: 'info' },
  4: { text: '已取消', type: 'danger' }
}

onMounted(async () => {
  const [users, hotels, rooms, bookingData] = await Promise.all([
    userApi.page({ size: 1 }),
    hotelApi.page({ size: 1 }),
    roomApi.page({ size: 1 }),
    bookingApi.page({ size: 5 })
  ])
  stats.value[0].value = users.total
  stats.value[1].value = hotels.total
  stats.value[2].value = rooms.total
  stats.value[3].value = bookingData.total
  bookings.value = bookingData.records
})
</script>

<style lang="scss" scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
  border: 1px solid var(--gray-100);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp 0.4s ease-out both;
  transition: all var(--transition);

  &:hover {
    box-shadow: var(--shadow);
    transform: translateY(-2px);
  }
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-trend {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 6px;

  &.up { background: var(--green-light); color: var(--green); }
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1;
  margin-bottom: 6px;
  letter-spacing: -0.02em;
}

.stat-title {
  font-size: 13px;
  color: var(--gray-500);
  font-weight: 500;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--gray-900);
  }
}

.order-id {
  font-weight: 600;
  color: var(--gray-500);
  font-size: 13px;
}

.price-text {
  font-weight: 600;
  color: var(--gray-900);
}
</style>
