<template>
  <div class="page-container fade-in-up">
    <div class="page-header">
      <h2>房间管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增房间
      </el-button>
    </div>
    <div class="card">
      <div class="search-bar">
        <el-select v-model="query.hotelId" placeholder="选择酒店" clearable style="width: 220px" @change="loadData">
          <el-option v-for="h in hotels" :key="h.id" :label="h.name" :value="h.id" />
        </el-select>
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }"><span class="id-text">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="hotelName" label="所属酒店" min-width="140" />
        <el-table-column prop="name" label="房间名称" min-width="120">
          <template #default="{ row }"><span class="name-text">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="roomType" label="房型" width="100">
          <template #default="{ row }">
            <span class="type-badge">{{ row.roomType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格/晚" width="110">
          <template #default="{ row }"><span class="price-text">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳" width="80">
          <template #default="{ row }">{{ row.capacity }}人</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '可用' : '不可用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" text type="primary" @click="openDialog(row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-popconfirm title="确定删除该房间？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房间' : '新增房间'" width="520px" destroy-on-close append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="所属酒店" prop="hotelId">
          <el-select v-model="form.hotelId" placeholder="选择酒店" style="width: 100%">
            <el-option v-for="h in hotels" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间名称" prop="name"><el-input v-model="form.name" placeholder="请输入房间名称" /></el-form-item>
        <el-form-item label="房型" prop="roomType"><el-input v-model="form.roomType" placeholder="如：大床房、双床房" /></el-form-item>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="价格/晚" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="容纳人数" prop="capacity"><el-input-number v-model="form.capacity" :min="1" style="width: 100%" /></el-form-item>
        </div>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">可用</el-radio>
            <el-radio :value="0">不可用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { roomApi, hotelApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const hotels = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, hotelId: null })
const formRef = ref()
const form = reactive({ id: null, hotelId: null, name: '', roomType: '', price: 0, capacity: 2, status: 1 })
const rules = {
  hotelId: [{ required: true, message: '请选择酒店', trigger: 'change' }],
  name: [{ required: true, message: '请输入房间名称', trigger: 'blur' }],
  roomType: [{ required: true, message: '请输入房型', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await roomApi.page(query)
    list.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const loadHotels = async () => {
  const res = await hotelApi.page({ size: 100 })
  hotels.value = res.records
}

const openDialog = (row) => {
  Object.assign(form, row || { id: null, hotelId: null, name: '', roomType: '', price: 0, capacity: 2, status: 1 })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await roomApi.update(form.id, form) : await roomApi.create(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

const handleDelete = async (id) => {
  await roomApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadHotels(); loadData() })
</script>

<style lang="scss" scoped>
.id-text { color: var(--gray-400); font-weight: 600; font-size: 13px; }
.name-text { font-weight: 600; color: var(--gray-900); }
.price-text { font-weight: 700; color: var(--gray-900); }
.type-badge {
  background: var(--blue-light);
  color: var(--blue);
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
