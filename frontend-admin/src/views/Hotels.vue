<template>
  <div class="page-container fade-in-up">
    <div class="page-header">
      <h2>酒店管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增酒店
      </el-button>
    </div>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="query.name" placeholder="搜索酒店名称..." clearable style="width: 260px" prefix-icon="Search" @keyup.enter="loadData" />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }"><span class="id-text">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="coverImage" label="封面" width="100">
          <template #default="{ row }">
            <div class="cover-img">
              <el-image :src="row.coverImage" fit="cover" style="width: 64px; height: 44px; border-radius: 6px;" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="140">
          <template #default="{ row }"><span class="name-text">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip min-width="160" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <div class="rating-badge">{{ row.rating }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" text type="primary" @click="openDialog(row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-popconfirm title="确定删除该酒店？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑酒店' : '新增酒店'" width="560px" destroy-on-close append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="酒店名称" prop="name"><el-input v-model="form.name" placeholder="请输入酒店名称" /></el-form-item>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" placeholder="请输入酒店地址" /></el-form-item>
        <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            @change="handleImageChange"
          >
            <div v-if="form.coverImage" class="cover-preview">
              <el-image :src="form.coverImage" fit="cover" />
              <div class="cover-mask">
                <el-icon><Plus /></el-icon>
                <span>更换图片</span>
              </div>
            </div>
            <div v-else class="cover-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传封面</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="酒店描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入酒店描述" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
import { hotelApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, name: '' })
const formRef = ref()
const form = reactive({ id: null, name: '', address: '', phone: '', coverImage: '', description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await hotelApi.page(query)
    list.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const openDialog = (row) => {
  Object.assign(form, row || { id: null, name: '', address: '', phone: '', coverImage: '', description: '', status: 1 })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await hotelApi.update(form.id, form) : await hotelApi.create(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

const handleDelete = async (id) => {
  await hotelApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleImageChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.coverImage = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.id-text { color: var(--gray-400); font-weight: 600; font-size: 13px; }
.name-text { font-weight: 600; color: var(--gray-900); }

.rating-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--orange-light);
  color: var(--orange);
  font-weight: 700;
  font-size: 13px;
  padding: 2px 10px;
  border-radius: 6px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.cover-uploader {
  width: 100%;
  
  :deep(.el-upload) {
    width: 100%;
  }
}

.cover-preview {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  
  .el-image {
    width: 100%;
    height: 100%;
  }
  
  .cover-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    opacity: 0;
    transition: opacity 0.3s;
    
    .el-icon { font-size: 24px; }
    span { font-size: 14px; }
  }
  
  &:hover .cover-mask {
    opacity: 1;
  }
}

.cover-placeholder {
  width: 100%;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
  
  .el-icon { font-size: 32px; }
  span { font-size: 14px; }
  
  &:hover {
    border-color: #1890ff;
    color: #1890ff;
  }
}
</style>
