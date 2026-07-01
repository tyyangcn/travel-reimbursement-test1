<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getAllocationList,
  getAllowanceList,
  getOperationLogList,
  getReimbursementDetail,
  getTripList,
} from '@/api/reimbursement'
import type {
  AllocationDetail,
  AllowanceDetail,
  OperationLogDetail,
  ReimbursementDetail,
  TripDetail,
} from '@/types/reimbursement'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ReimbursementDetail>()
const tripList = ref<TripDetail[]>([])
const allowanceList = ref<AllowanceDetail[]>([])
const allocationList = ref<AllocationDetail[]>([])
const operationLogList = ref<OperationLogDetail[]>([])

function formatAmount(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

async function loadDetail() {
  const id = String(route.params.id)
  loading.value = true

  try {
    const [main, trips, allowances, allocations, operationLogs] = await Promise.all([
      getReimbursementDetail(id),
      getTripList(id),
      getAllowanceList(id),
      getAllocationList(id),
      getOperationLogList(id),
    ])
    detail.value = main
    tripList.value = trips
    allowanceList.value = allowances
    allocationList.value = allocations
    operationLogList.value = operationLogs
  } finally {
    loading.value = false
  }
}

function closeDetail() {
  router.push('/reimburse/list')
}

onMounted(loadDetail)
</script>

<template>
  <main v-loading="loading" class="detail-page">
    <header class="document-header">
      <h1>差旅费用报销单</h1>
      <div class="document-date">
        提单日期
        <span>{{ detail?.documentDate }}</span>
      </div>
    </header>

    <div v-if="detail" class="detail-content">
      <section>
        <div class="section-title">基础信息</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="报销单号">
            {{ detail.reimNo }}
          </el-descriptions-item>
          <el-descriptions-item label="单据状态">
            {{ detail.statusName }}
          </el-descriptions-item>
          <el-descriptions-item label="报销人">
            {{ detail.reimUserName }}[{{ detail.reimUserNo }}]
          </el-descriptions-item>
          <el-descriptions-item label="报销部门">
            [{{ detail.reimDeptNo }}]{{ detail.reimDeptName }}
          </el-descriptions-item>
          <el-descriptions-item label="费用归属公司">
            {{ detail.companyName }}
          </el-descriptions-item>
          <el-descriptions-item label="业务类型">
            {{ detail.businessTypeName }}
          </el-descriptions-item>
          <el-descriptions-item label="报销标题" :span="3">
            {{ detail.reimTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="出差事由" :span="3">
            {{ detail.travelReason }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="content-section">
        <div class="section-title">补录行程</div>
        <el-table :data="tripList" border>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="出行人员" min-width="160">
            <template #default="{ row }"> {{ row.travelerName }}/{{ row.travelerNo }} </template>
          </el-table-column>
          <el-table-column label="出差日期" min-width="210">
            <template #default="{ row }">
              {{ row.departureDate }} 至 {{ row.arrivalDate }}
            </template>
          </el-table-column>
          <el-table-column label="行程" min-width="160">
            <template #default="{ row }">
              {{ row.departureCityName }} - {{ row.arrivalCityName }}
            </template>
          </el-table-column>
          <el-table-column prop="tripDescription" label="行程说明" min-width="220" />
        </el-table>
      </section>

      <section class="content-section">
        <div class="section-title">
          补助信息
          <span class="section-amount">
            {{ formatAmount(detail.totalAllowanceAmount) }}
          </span>
        </div>
        <el-table :data="allowanceList" border>
          <el-table-column type="expand">
            <template #default="{ row }">
              <el-table :data="row.calendarList" class="calendar-table">
                <el-table-column prop="allowanceDate" label="补助日期" />
                <el-table-column prop="mealActualAmount" label="餐费补助" />
                <el-table-column prop="trafficActualAmount" label="交通补助" />
                <el-table-column prop="communicationActualAmount" label="通讯补助" />
                <el-table-column prop="dailyActualAmount" label="当日合计" />
              </el-table>
            </template>
          </el-table-column>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="travelerName" label="出行人" />
          <el-table-column label="出差日期" min-width="210">
            <template #default="{ row }"> {{ row.startDate }} 至 {{ row.endDate }} </template>
          </el-table-column>
          <el-table-column prop="allowanceDays" label="补助天数" width="100" />
          <el-table-column label="行程">
            <template #default="{ row }">
              {{ row.departureCityName }} - {{ row.allowanceCityName }}
            </template>
          </el-table-column>
          <el-table-column prop="allowanceCityName" label="补助城市" />
          <el-table-column label="申请金额" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.applyAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="补助金额" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.allowanceAmount) }}
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="content-section">
        <div class="section-title">费用合计</div>
        <div class="amount-summary">
          <span>补助总金额 {{ formatAmount(detail.totalAllowanceAmount) }}</span>
          <span>餐费补助 {{ formatAmount(detail.mealAllowanceAmount) }}</span>
          <span>交通补助 {{ formatAmount(detail.trafficAllowanceAmount) }}</span>
          <span> 通讯补助 {{ formatAmount(detail.communicationAllowanceAmount) }} </span>
        </div>
      </section>

      <section class="content-section">
        <div class="section-title">
          费用归属及分摊
          <span class="section-amount">
            分摊金额：{{ formatAmount(detail.totalAllowanceAmount) }}
          </span>
        </div>
        <el-table :data="allocationList" border show-summary>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="费用归属" min-width="220">
            <template #default="{ row }"> [{{ row.companyNo }}]{{ row.companyName }} </template>
          </el-table-column>
          <el-table-column label="项目" min-width="220">
            <template #default="{ row }"> [{{ row.projectNo }}]{{ row.projectName }} </template>
          </el-table-column>
          <el-table-column label="分摊比例" align="right">
            <template #default="{ row }">
              {{ (Number(row.allocationRatio) * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column label="分摊金额" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.allocationAmount) }}
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="content-section">
        <div class="section-title">备注信息</div>
        <div class="remark-content">{{ detail.remark || '无' }}</div>
      </section>

      <section class="content-section">
        <div class="section-title">操作日志</div>
        <el-table :data="operationLogList" border>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="operationTypeName" label="操作类型" width="110" />
          <el-table-column prop="operatorName" label="操作人" width="140" />
          <el-table-column prop="operationContent" label="操作内容" min-width="260" />
          <el-table-column label="操作结果" width="110">
            <template #default="{ row }">
              {{ row.operationResult === 'SUCCESS' ? '成功' : row.operationResult }}
            </template>
          </el-table-column>
          <el-table-column prop="operationTime" label="操作时间" width="190" />
        </el-table>
      </section>
    </div>

    <footer class="detail-actions">
      <el-button type="primary" @click="closeDetail">关闭</el-button>
    </footer>
  </main>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding-bottom: 72px;
  background: #f5f6f8;
  color: #303133;
  box-sizing: border-box;
}

.document-header {
  position: relative;
  height: 54px;
  border-bottom: 1px solid #ebeef5;
  background: #ffffff;
}

.document-header h1 {
  margin: 0;
  line-height: 54px;
  text-align: center;
  font-size: 20px;
}

.document-date {
  position: absolute;
  top: 0;
  right: 24px;
  min-width: 180px;
  line-height: 54px;
  text-align: right;
  white-space: nowrap;
}

.document-date span {
  margin-left: 12px;
}

.detail-content {
  width: calc(100% - 40px);
  max-width: 1180px;
  margin: 18px auto;
  padding: 16px 20px 28px;
  background: #ffffff;
  box-sizing: border-box;
}

.content-section {
  margin-top: 18px;
}

.section-title {
  position: relative;
  height: 36px;
  margin-bottom: 12px;
  padding-left: 22px;
  line-height: 36px;
  background: #f0f2f5;
  font-size: 16px;
  font-weight: 500;
}

.section-title::before {
  position: absolute;
  top: 9px;
  bottom: 9px;
  left: 12px;
  width: 3px;
  background: #409eff;
  content: '';
}

.section-amount {
  margin-left: 16px;
  color: #606266;
  font-size: 14px;
  font-weight: 400;
}

.calendar-table {
  width: calc(100% - 80px);
  margin: 8px 40px 16px;
}

.amount-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 0 40px;
  line-height: 44px;
}

.remark-content {
  min-height: 70px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  white-space: pre-wrap;
}

.detail-actions {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  border-top: 1px solid #ebeef5;
  background: #ffffff;
}
</style>
