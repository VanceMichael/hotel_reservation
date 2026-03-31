<template>
  <div class="page-container fade-in-up">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="query.username" placeholder="搜索用户名..." clearable style="width: 180px" prefix-icon="Search" @keyup.enter="loadData" />
        <el-select v-model="query.module" placeholder="选择模块" clearable style="width: 160px" @change="loadData">
          <el-option label="认证" value="认证" />
          <el-option label="用户管理" value="用户管理" />
          <el-option label="酒店管理" value="酒店管理" />
          <el-option label="房间管理" value="房间管理" />
          <el-option label="预订管理" value="预订管理" />
        </el-select>
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }"><span class="id-text">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="110">
          <template #default="{ row }">
            <span :class="row.username ? 'name-text' : 'name-anonymous'">{{ row.username || '匿名用户' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }">
            <span class="module-badge">{{ row.module }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="method" label="方法" show-overflow-tooltip min-width="180">
          <template #default="{ row }"><code class="method-text">{{ row.method }}</code></template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="duration" label="耗时" width="90">
          <template #default="{ row }">
            <span :class="['duration', row.duration > 500 ? 'slow' : 'fast']">{{ row.duration }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
import { logApi } from '../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, username: '', module: '' })

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await logApi.page(query)
    list.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.id-text { color: var(--gray-400); font-weight: 600; font-size: 13px; }
.name-text { font-weight: 600; color: var(--gray-900); }
.name-anonymous { color: var(--gray-400); font-style: italic; }

.module-badge {
  background: var(--blue-light);
  color: var(--blue);
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.method-text {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  color: var(--gray-600);
  background: var(--gray-50);
  padding: 2px 6px;
  border-radius: 4px;
}

.duration {
  font-weight: 600;
  font-size: 13px;

  &.fast { color: var(--green); }
  &.slow { color: var(--red); }
}

.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
