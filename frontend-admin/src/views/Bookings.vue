<template>
  <div class="page-container fade-in-up">
    <div class="page-header">
      <h2>预订管理</h2>
    </div>
    <div class="card">
      <div class="search-bar">
        <el-select v-model="query.status" placeholder="预订状态" clearable style="width: 160px" @change="loadData">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v.text" :value="Number(k)" />
        </el-select>
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="订单号" width="80">
          <template #default="{ row }"><span class="id-text">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="username" label="客户" width="110">
          <template #default="{ row }"><span class="name-text">{{ row.username }}</span></template>
        </el-table-column>
        <el-table-column prop="hotelName" label="酒店" min-width="140" />
        <el-table-column prop="roomName" label="房间" width="120" />
        <el-table-column label="入住/离店" width="200">
          <template #default="{ row }">
            <div class="date-range">
              <span>{{ row.checkInDate }}</span>
              <el-icon class="date-arrow"><Right /></el-icon>
              <span>{{ row.checkOutDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template #default="{ row }"><span class="price-text">¥{{ row.totalPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="remark-text">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" style="width: 100px" @change="updateStatus(row.id, row.status)">
              <el-option v-for="(v, k) in statusMap" :key="k" :label="v.text" :value="Number(k)" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { bookingApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, status: null })
const statusMap = {
  0: { text: '待确认', type: 'warning' },
  1: { text: '已确认', type: 'primary' },
  2: { text: '已入住', type: 'success' },
  3: { text: '已完成', type: 'info' },
  4: { text: '已取消', type: 'danger' }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await bookingApi.page(query)
    list.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const updateStatus = async (id, status) => {
  await bookingApi.updateStatus(id, status)
  ElMessage.success('状态更新成功')
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.id-text { color: var(--gray-400); font-weight: 600; font-size: 13px; }
.name-text { font-weight: 600; color: var(--gray-900); }
.price-text { font-weight: 700; color: var(--gray-900); }
.remark-text { color: var(--gray-500); font-size: 13px; }

.date-range {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--gray-600);
}

.date-arrow { color: var(--gray-300); font-size: 12px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
