<template>
  <div class="page-container fade-in-up">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="query.username" placeholder="搜索用户名..." clearable style="width: 260px" prefix-icon="Search" @keyup.enter="loadData" />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }"><span class="id-text">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar">{{ row.username?.charAt(0)?.toUpperCase() }}</div>
              <span class="name-text">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <span :class="['role-badge', row.role === 1 ? 'admin' : 'user']">
              {{ row.role === 1 ? '管理员' : '用户' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
                <el-icon><component :is="row.status === 1 ? 'Lock' : 'Unlock'" /></el-icon>
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-popconfirm title="确定删除该用户？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button size="small" text type="danger"><el-icon><Delete /></el-icon>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
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
import { userApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, username: '' })

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.page(query)
    list.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const toggleStatus = async (row) => {
  await userApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  loadData()
}

const handleDelete = async (id) => {
  await userApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.id-text { color: var(--gray-400); font-weight: 600; font-size: 13px; }
.name-text { font-weight: 600; color: var(--gray-900); }

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--primary);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 12px;
  flex-shrink: 0;
}

.role-badge {
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;

  &.admin { background: #FDF4FF; color: #A855F7; }
  &.user { background: var(--gray-100); color: var(--gray-600); }
}

.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
