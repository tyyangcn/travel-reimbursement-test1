<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  copyReimbursement,
  deleteReimbursement,
  getReimbursementDictionaries,
  getReimbursementPage,
  submitReimbursement,
  voidReimbursement,
} from '@/api/reimbursement'
import type { DictionaryOption, ReimbursementPageItem } from '@/types/reimbursement'
import { EditPen, MoreFilled } from '@element-plus/icons-vue'

const tableData = ref<ReimbursementPageItem[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const router = useRouter()
const companyOptions = ref<DictionaryOption[]>([])
const departmentOptions = ref<DictionaryOption[]>([])
const userOptions = ref<DictionaryOption[]>([])
const businessTypeOptions = ref<DictionaryOption[]>([])
const queryForm = ref({
  reimNo: '',
  reimTitle: '',
  travelReason: '',
  companyId: '',
  reimDeptId: '',
  reimUserId: '',
  businessTypeId: '',
})

async function loadReimbursementList() {
  loading.value = true

  try {
    const page = await getReimbursementPage({
      ...queryForm.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })

    tableData.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadReimbursementList()
}

function handleReset() {
  queryForm.value = {
    reimNo: '',
    reimTitle: '',
    travelReason: '',
    companyId: '',
    reimDeptId: '',
    reimUserId: '',
    businessTypeId: '',
  }

  pageNum.value = 1
  loadReimbursementList()
}

async function handleSubmit(row: ReimbursementPageItem) {
  try {
    await ElMessageBox.confirm(`确定提交报销单 ${row.reimNo} 吗？`, '提交确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await submitReimbursement(row.id)

    ElMessage.success('提交成功')
    await loadReimbursementList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

async function handleVoid(row: ReimbursementPageItem) {
  try {
    await ElMessageBox.confirm(`确定作废报销单 ${row.reimNo} 吗？作废后不可编辑。`, '作废确认', {
      confirmButtonText: '确定作废',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await voidReimbursement(row.id)
    ElMessage.success('作废成功')
    await loadReimbursementList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

async function handleDelete(row: ReimbursementPageItem) {
  try {
    await ElMessageBox.confirm(
      `确定删除报销单 ${row.reimNo} 吗？删除后列表中将不再显示。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await deleteReimbursement(row.id)
    ElMessage.success('删除成功')

    if (tableData.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    await loadReimbursementList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

async function handleCopy(row: ReimbursementPageItem) {
  try {
    await ElMessageBox.confirm(`确定复制报销单 ${row.reimNo} 吗？`, '复制确认', {
      confirmButtonText: '确定复制',
      cancelButtonText: '取消',
      type: 'info',
    })

    const newId = await copyReimbursement(row.id)
    ElMessage.success('复制成功，已生成新的草稿')
    await router.push(`/reimburse/${newId}/edit`)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

function handlePageChange() {
  loadReimbursementList()
}

function handlePageSizeChange() {
  pageNum.value = 1
  loadReimbursementList()
}

function handleCreate() {
  router.push('/reimburse/create')
}

function openDetail(id: string) {
  router.push(`/reimburse/${id}`)
}

function openEdit(id: string) {
  router.push(`/reimburse/${id}/edit`)
}

onMounted(async () => {
  const dictionaries = await getReimbursementDictionaries()
  companyOptions.value = dictionaries.companies
  departmentOptions.value = dictionaries.departments
  userOptions.value = dictionaries.employees
  businessTypeOptions.value = dictionaries.businessTypes
  await loadReimbursementList()
})
</script>

<template>
  <main class="reimbursement-page">
    <el-form :model="queryForm" label-width="110px" class="search-form">
      <div class="search-grid">
        <el-form-item label="报销单号">
          <el-input v-model="queryForm.reimNo" placeholder="请输入" clearable />
        </el-form-item>

        <el-form-item label="标题">
          <el-input v-model="queryForm.reimTitle" placeholder="请输入" clearable />
        </el-form-item>

        <el-form-item label="事由">
          <el-input v-model="queryForm.travelReason" placeholder="请输入" clearable />
        </el-form-item>

        <el-form-item label="费用归属公司">
          <el-select v-model="queryForm.companyId" placeholder="请选择" clearable>
            <el-option
              v-for="item in companyOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="报销部门">
          <el-select v-model="queryForm.reimDeptId" placeholder="请选择" clearable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="报销人">
          <el-select v-model="queryForm.reimUserId" placeholder="请选择" clearable>
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="`${item.name}[${item.no}]`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="业务类型">
          <el-select v-model="queryForm.businessTypeId" placeholder="请选择" clearable>
            <el-option
              v-for="item in businessTypeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <div class="search-actions">
          <el-button type="primary" plain @click="handleCreate">新增</el-button>
          <el-button type="primary" plain @click="handleReset">清除</el-button>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </div>
    </el-form>
    <el-table v-loading="loading" :data="tableData" border class="reimbursement-table">
      <el-table-column type="index" label="序号" width="60" align="center" />

      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-button
              link
              type="primary"
              title="提交"
              :disabled="row.status !== 0"
              @click.stop="handleSubmit(row)"
            >
              <svg class="submit-document-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M5 3.5h9l4 4v5.5" />
                <path d="M14 3.5v4h4" />
                <path d="M5 3.5v17h7" />
                <path d="M11 16h8" />
                <path d="m16 13 3 3-3 3" />
              </svg>
            </el-button>

            <el-button
              link
              type="primary"
              title="编辑"
              :disabled="row.status !== 0"
              @click="openEdit(row.id)"
            >
              <el-icon><EditPen /></el-icon>
            </el-button>

            <el-dropdown trigger="click">
              <el-button link type="primary">
                <el-icon><MoreFilled /></el-icon>
              </el-button>

              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="row.status !== 0" @click="handleDelete(row)">
                    删除
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="row.status === 2" @click="handleVoid(row)">
                    作废
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleCopy(row)"> 复制 </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="报销单号" width="190">
        <template #default="{ row }">
          <el-link type="primary" :underline="false" @click="openDetail(row.id)">
            {{ row.reimNo }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column label="单据状态" width="100">
        <template #default="{ row }">
          <el-link type="primary" :underline="false">
            {{ row.statusName }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column label="单据类型" width="120"> 日常报销单 </el-table-column>

      <el-table-column label="报销人" width="160">
        <template #default="{ row }"> {{ row.reimUserName }}[{{ row.reimUserNo }}] </template>
      </el-table-column>

      <el-table-column label="报销部门" width="160">
        <template #default="{ row }"> [{{ row.reimDeptNo }}]{{ row.reimDeptName }} </template>
      </el-table-column>

      <el-table-column prop="companyName" label="费用归属公司" width="160" show-overflow-tooltip />

      <el-table-column prop="businessTypeName" label="业务类型" width="130" />

      <el-table-column label="报销标题" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" :underline="false">
            {{ row.reimTitle }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column prop="travelReason" label="报销事由" min-width="180" show-overflow-tooltip />

      <el-table-column label="补助金额" width="110" align="right">
        <template #default="{ row }">
          {{ Number(row.totalAllowanceAmount).toFixed(2) }}
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="120">
        <template #default="{ row }">
          {{ row.createTime?.slice(0, 10) }}
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </div>
  </main>
</template>

<style scoped>
.reimbursement-page {
  min-height: 100vh;
  padding: 20px;
  background: #ffffff;
  color: #303133;
  box-sizing: border-box;
}

.search-form {
  margin-bottom: 20px;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  column-gap: 36px;
  row-gap: 10px;
  align-items: center;
}

.search-grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-grid :deep(.el-input),
.search-grid :deep(.el-select) {
  width: 100%;
}

.search-grid :deep(.el-input__wrapper),
.search-grid :deep(.el-select__wrapper) {
  min-height: 32px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.search-actions :deep(.el-button) {
  min-width: 64px;
  margin-left: 0;
}

.reimbursement-table {
  width: 100%;
  font-size: 13px;
}

.reimbursement-table :deep(.el-table__header th) {
  height: 40px;
  color: #303133;
  background: #f5f5f5;
  font-weight: 500;
}

.reimbursement-table :deep(.el-table__row td) {
  height: 39px;
  padding: 0;
}

.reimbursement-table :deep(.cell) {
  white-space: nowrap;
}

.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.operation-buttons :deep(.el-button) {
  margin-left: 0;
  padding: 4px;
  font-size: 16px;
}

.submit-document-icon {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.7;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.reimbursement-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 40px);
  padding: 20px;
  background: #ffffff;
  color: #303133;
  box-sizing: border-box;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
  padding-bottom: 40px;
}
</style>
