<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowDown,
  ArrowRight,
  CirclePlus,
  CopyDocument,
  Delete,
  EditPen,
} from '@element-plus/icons-vue'
import {
  adjustAllowanceCalendar,
  createReimbursement,
  createTrip,
  getAllocationList,
  getAllowanceList,
  getReimbursementDetail,
  getReimbursementDictionaries,
  getTripList,
  removeTrip,
  saveAllocationList,
  submitReimbursement,
  updateReimbursement,
  updateTrip,
} from '@/api/reimbursement'
import type {
  AllocationDraft,
  AllocationSaveForm,
  AllowanceDetail,
  DictionaryOption,
  ReimbursementForm,
  TripDraft,
  TripForm,
} from '@/types/reimbursement'

const today = new Intl.DateTimeFormat('en-CA').format(new Date())
const route = useRoute()
const router = useRouter()
const saving = ref(false)
const loading = ref(false)
const reimbursementId = String(route.params.id ?? '')
const initialTripIds = new Set<string>()
const totalAllowanceAmount = ref(0)
const allowanceAmounts = reactive({
  meal: 0,
  traffic: 0,
  communication: 0,
})
const allowanceList = ref<AllowanceDetail[]>([])
const allowanceDialogVisible = ref(false)
const adjustingAllowance = ref<AllowanceDetail>()
const allowanceCalendarDraft = ref<AllowanceDetail['calendarList']>([])
const sectionExpanded = reactive({
  basic: true,
  trip: true,
  allowance: true,
  total: true,
  allocation: true,
  remark: true,
})
const allowanceTravelerSummary = computed(() =>
  allowanceList.value.map((item) => `${item.travelerName}:${item.allowanceDays}天`).join('，'),
)
const selectedStandardTotal = computed(() =>
  allowanceCalendarDraft.value.reduce(
    (total, item) =>
      total +
      (item.mealSelected ? Number(item.mealStandardAmount) : 0) +
      (item.trafficSelected ? Number(item.trafficStandardAmount) : 0) +
      (item.communicationSelected ? Number(item.communicationStandardAmount) : 0),
    0,
  ),
)
const adjustedAllowanceTotal = computed(() =>
  allowanceCalendarDraft.value.reduce((total, item) => total + Number(item.dailyActualAmount), 0),
)
const allAllowanceSelected = computed({
  get: () =>
    allowanceCalendarDraft.value.length > 0 &&
    allowanceCalendarDraft.value.every((_, index) => isAllowanceRowSelected(index)),
  set: (selected: boolean) => {
    allowanceCalendarDraft.value.forEach((_, index) => {
      setAllowanceRowSelected(index, selected)
    })
  },
})
const allocationList = ref<AllocationDraft[]>([])
const allocationPercentTotal = computed(() =>
  allocationList.value.reduce((sum, item) => sum + Number(item.allocationPercent), 0),
)
const allocationAmountTotal = computed(() =>
  allocationList.value.reduce((sum, item) => sum + Number(item.allocationAmount), 0),
)
const mainFormRef = ref<FormInstance>()
const tripFormRef = ref<FormInstance>()
const mainFormRules: FormRules = {
  reimTitle: [
    { required: true, whitespace: true, message: '请输入报销标题', trigger: 'blur' },
    { max: 500, message: '报销标题不能超过500个字符', trigger: 'blur' },
  ],
  reimUserId: [{ required: true, message: '请选择报销人', trigger: 'change' }],
  reimDeptId: [{ required: true, message: '请选择报销部门', trigger: 'change' }],
  companyId: [{ required: true, message: '请选择费用归属公司', trigger: 'change' }],
  businessTypeId: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  travelReason: [
    { required: true, whitespace: true, message: '请输入出差事由', trigger: 'blur' },
    { max: 500, message: '出差事由不能超过500个字符', trigger: 'blur' },
  ],
}
const tripFormRules: FormRules = {
  travelerId: [{ required: true, message: '请选择出行人', trigger: 'change' }],
  departureCityNo: [{ required: true, message: '请选择出发城市', trigger: 'change' }],
  arrivalCityNo: [{ required: true, message: '请选择到达城市', trigger: 'change' }],
  tripDescription: [
    { required: true, whitespace: true, message: '请输入行程说明', trigger: 'blur' },
    { max: 500, message: '行程说明不能超过500个字符', trigger: 'blur' },
  ],
}

const form = reactive<ReimbursementForm>({
  documentDate: today,
  reimTitle: '',
  travelReason: '',

  reimUserId: '',
  reimUserNo: '',
  reimUserName: '',

  reimDeptId: '',
  reimDeptNo: '',
  reimDeptName: '',

  companyId: '',
  companyNo: '',
  companyName: '',

  businessTypeId: '',
  businessTypeNo: '',
  businessTypeName: '',

  remark: '',
})
const userOptions = ref<DictionaryOption[]>([])
const departmentOptions = ref<DictionaryOption[]>([])
const companyOptions = ref<DictionaryOption[]>([])
const businessTypeOptions = ref<DictionaryOption[]>([])
const projectOptions = ref<DictionaryOption[]>([])
const cityOptions = ref<DictionaryOption[]>([])

async function loadDictionaries() {
  const dictionaries = await getReimbursementDictionaries()
  userOptions.value = dictionaries.employees
  departmentOptions.value = dictionaries.departments
  companyOptions.value = dictionaries.companies
  businessTypeOptions.value = dictionaries.businessTypes
  projectOptions.value = dictionaries.projects
  cityOptions.value = dictionaries.cities
}

function handleUserChange(id: string) {
  const option = userOptions.value.find((item) => item.id === id)

  form.reimUserNo = option?.no ?? ''
  form.reimUserName = option?.name ?? ''
  if (option?.departmentId) {
    form.reimDeptId = option.departmentId
    form.reimDeptNo = option.departmentNo ?? ''
    form.reimDeptName = option.departmentName ?? ''
  }
}

function handleDepartmentChange(id: string) {
  const option = departmentOptions.value.find((item) => item.id === id)

  form.reimDeptNo = option?.no ?? ''
  form.reimDeptName = option?.name ?? ''
}

function handleCompanyChange(id: string) {
  const option = companyOptions.value.find((item) => item.id === id)

  form.companyNo = option?.no ?? ''
  form.companyName = option?.name ?? ''
}

function handleBusinessTypeChange(id: string) {
  const option = businessTypeOptions.value.find((item) => item.id === id)

  form.businessTypeNo = option?.no ?? ''
  form.businessTypeName = option?.name ?? ''
}

function recalculateAllocations() {
  if (allocationList.value.length === 0) {
    return
  }

  const remainingPercent = allocationList.value
    .slice(1)
    .reduce((sum, item) => sum + Number(item.allocationPercent || 0), 0)
  allocationList.value[0]!.allocationPercent = Number((100 - remainingPercent).toFixed(2))

  let remainingAmount = totalAllowanceAmount.value
  allocationList.value.slice(1).forEach((item) => {
    item.allocationAmount = Number(
      ((totalAllowanceAmount.value * item.allocationPercent) / 100).toFixed(2),
    )
    remainingAmount -= item.allocationAmount
  })
  allocationList.value[0]!.allocationAmount = Number(remainingAmount.toFixed(2))
}

function addAllocation() {
  allocationList.value.push({
    tempId: crypto.randomUUID(),
    companyId: allocationList.value.length === 0 ? form.companyId : '',
    companyNo: allocationList.value.length === 0 ? form.companyNo : '',
    companyName: allocationList.value.length === 0 ? form.companyName : '',
    projectId: '',
    projectNo: '',
    projectName: '',
    allocationPercent: allocationList.value.length === 0 ? 100 : 0,
    allocationAmount: allocationList.value.length === 0 ? totalAllowanceAmount.value : 0,
  })
  recalculateAllocations()
}

async function removeAllocation(index: number) {
  if (allocationList.value.length === 1) {
    ElMessage.warning('至少保留一条分摊信息')
    return
  }

  try {
    await ElMessageBox.confirm('确定删除当前分摊信息吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  allocationList.value.splice(index, 1)
  recalculateAllocations()
}

function handleAllocationCompanyChange(index: number, companyId: string) {
  const row = allocationList.value[index]
  const company = companyOptions.value.find((item) => item.id === companyId)
  if (!row || !company) {
    return
  }

  row.companyNo = company.no
  row.companyName = company.name
}

function handleAllocationProjectChange(index: number, projectId: string) {
  const row = allocationList.value[index]
  const project = projectOptions.value.find((item) => item.id === projectId)
  if (!row || !project) {
    return
  }

  row.projectNo = project.no
  row.projectName = project.name
}

function handleAllocationPercentChange(index: number) {
  if (index === 0) {
    return
  }

  const otherPercentTotal = allocationList.value
    .slice(1)
    .reduce((sum, item) => sum + Number(item.allocationPercent || 0), 0)
  if (otherPercentTotal > 100) {
    allocationList.value[index]!.allocationPercent = 0
    ElMessage.warning('第2行起的分摊比例合计不能超过100%')
  }
  recalculateAllocations()
}

function distributeAllocationsEqually() {
  const rowCount = allocationList.value.length
  if (rowCount === 0) {
    return
  }

  const commonPercent = Math.floor(10000 / rowCount) / 100
  allocationList.value.forEach((item, index) => {
    item.allocationPercent =
      index === 0 ? Number((100 - commonPercent * (rowCount - 1)).toFixed(2)) : commonPercent
  })
  recalculateAllocations()
}

async function clearRemark() {
  if (!form.remark) {
    return
  }

  try {
    await ElMessageBox.confirm('确定清空备注信息吗？', '删除备注确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    form.remark = ''
  } catch {
    // User cancelled the operation.
  }
}

function openAllowanceAdjustment(allowance: AllowanceDetail) {
  adjustingAllowance.value = allowance
  allowanceCalendarDraft.value = allowance.calendarList.map((item) => ({ ...item }))
  allowanceDialogVisible.value = true
}

function recalculateCalendarRow(index: number) {
  const row = allowanceCalendarDraft.value[index]
  if (!row) {
    return
  }

  row.mealActualAmount = row.mealSelected ? Number(row.mealStandardAmount) : 0
  row.trafficActualAmount = row.trafficSelected ? Number(row.trafficStandardAmount) : 0
  row.communicationActualAmount = row.communicationSelected
    ? Number(row.communicationStandardAmount)
    : 0
  row.dailyActualAmount =
    row.mealActualAmount + row.trafficActualAmount + row.communicationActualAmount
}

function isAllowanceRowSelected(index: number) {
  const row = allowanceCalendarDraft.value[index]
  return Boolean(
    row && row.mealSelected === 1 && row.trafficSelected === 1 && row.communicationSelected === 1,
  )
}

function setAllowanceRowSelected(index: number, selected: boolean) {
  const row = allowanceCalendarDraft.value[index]
  if (!row) {
    return
  }
  row.mealSelected = selected ? 1 : 0
  row.trafficSelected = selected ? 1 : 0
  row.communicationSelected = selected ? 1 : 0
  row.mealActualAmount = selected ? Number(row.mealStandardAmount) : 0
  row.trafficActualAmount = selected ? Number(row.trafficStandardAmount) : 0
  row.communicationActualAmount = selected ? Number(row.communicationStandardAmount) : 0
  recalculateCalendarRow(index)
}

function isAllowanceCategorySelected(
  category: 'mealSelected' | 'trafficSelected' | 'communicationSelected',
) {
  return (
    allowanceCalendarDraft.value.length > 0 &&
    allowanceCalendarDraft.value.every((item) => item[category] === 1)
  )
}

function setAllowanceCategorySelected(
  category: 'mealSelected' | 'trafficSelected' | 'communicationSelected',
  amountField: 'mealActualAmount' | 'trafficActualAmount' | 'communicationActualAmount',
  standardField: 'mealStandardAmount' | 'trafficStandardAmount' | 'communicationStandardAmount',
  selected: boolean,
) {
  allowanceCalendarDraft.value.forEach((item, index) => {
    item[category] = selected ? 1 : 0
    item[amountField] = selected ? Number(item[standardField]) : 0
    recalculateCalendarRow(index)
  })
}

function handleAllowanceItemToggle(
  index: number,
  amountField: 'mealActualAmount' | 'trafficActualAmount' | 'communicationActualAmount',
  standardField: 'mealStandardAmount' | 'trafficStandardAmount' | 'communicationStandardAmount',
  selected: boolean,
) {
  const row = allowanceCalendarDraft.value[index]
  if (!row) {
    return
  }
  row[amountField] = selected ? Number(row[standardField]) : 0
  recalculateCalendarRow(index)
}

function formatWeekday(weekdayNo: number) {
  return `星期${['一', '二', '三', '四', '五', '六', '日'][weekdayNo - 1] ?? ''}`
}

async function saveAllowanceAdjustment() {
  if (!reimbursementId || !adjustingAllowance.value) {
    return
  }

  saving.value = true
  try {
    await adjustAllowanceCalendar(
      reimbursementId,
      adjustingAllowance.value.id,
      allowanceCalendarDraft.value.map((item) => ({
        calendarId: item.id,
        mealSelected: item.mealSelected,
        mealActualAmount: Number(item.mealActualAmount),
        trafficSelected: item.trafficSelected,
        trafficActualAmount: Number(item.trafficActualAmount),
        communicationSelected: item.communicationSelected,
        communicationActualAmount: Number(item.communicationActualAmount),
      })),
    )
    allowanceDialogVisible.value = false
    ElMessage.success('补助明细调整成功')
    await loadEditData()
  } finally {
    saving.value = false
  }
}

const tripList = ref<TripDraft[]>([])
const tripDialogVisible = ref(false)
const editingTripIndex = ref(-1)
const tripDateRange = ref<[string, string] | []>([])

function createEmptyTrip(): TripForm {
  return {
    travelerId: '',
    travelerNo: '',
    travelerName: '',
    departureCityNo: '',
    departureCityName: '',
    arrivalCityNo: '',
    arrivalCityName: '',
    arrivalCityType: '',
    departureDate: '',
    arrivalDate: '',
    tripDescription: '',
  }
}

const tripForm = reactive<TripForm>(createEmptyTrip())

function resetTripForm() {
  Object.assign(tripForm, createEmptyTrip())
  tripDateRange.value = []
  editingTripIndex.value = -1
  tripFormRef.value?.clearValidate()
}

function openCreateTrip() {
  resetTripForm()

  if (form.reimUserId) {
    tripForm.travelerId = form.reimUserId
    tripForm.travelerNo = form.reimUserNo
    tripForm.travelerName = form.reimUserName
  }

  tripDialogVisible.value = true
}

function openEditTrip(index: number) {
  const trip = tripList.value[index]
  if (!trip) {
    return
  }

  Object.assign(tripForm, trip)
  tripDateRange.value = [trip.departureDate, trip.arrivalDate]
  editingTripIndex.value = index
  tripDialogVisible.value = true
}

function handleTripTravelerChange(id: string) {
  const option = userOptions.value.find((item) => item.id === id)
  tripForm.travelerNo = option?.no ?? ''
  tripForm.travelerName = option?.name ?? ''
}

function handleDepartureCityChange(no: string) {
  const option = cityOptions.value.find((item) => item.no === no)
  tripForm.departureCityName = option?.name ?? ''
}

function handleArrivalCityChange(no: string) {
  const option = cityOptions.value.find((item) => item.no === no)
  tripForm.arrivalCityName = option?.name ?? ''
  tripForm.arrivalCityType = option?.type ?? ''
}

function disableFutureDate(date: Date) {
  const currentDate = new Date()
  currentDate.setHours(23, 59, 59, 999)
  return date.getTime() > currentDate.getTime()
}

async function saveTrip() {
  const [departureDate, arrivalDate] = tripDateRange.value

  if (!tripFormRef.value) {
    return
  }

  try {
    await tripFormRef.value.validate()
  } catch {
    return
  }

  if (!departureDate || !arrivalDate) {
    ElMessage.warning('请选择出发到达日期')
    return
  }

  if (arrivalDate < departureDate) {
    ElMessage.warning('到达日期不能早于出发日期')
    return
  }

  if (arrivalDate > today) {
    ElMessage.warning('到达日期不能晚于当前日期')
    return
  }

  const hasOverlap = tripList.value.some(
    (trip, index) =>
      index !== editingTripIndex.value &&
      trip.travelerId === tripForm.travelerId &&
      trip.departureDate <= arrivalDate &&
      trip.arrivalDate >= departureDate,
  )

  if (hasOverlap) {
    ElMessage.warning('该出行人的行程日期存在重叠')
    return
  }

  tripForm.departureDate = departureDate
  tripForm.arrivalDate = arrivalDate

  const isEditing = editingTripIndex.value >= 0
  const editingTrip = isEditing ? tripList.value[editingTripIndex.value] : undefined

  if (reimbursementId) {
    saving.value = true

    try {
      if (editingTrip && initialTripIds.has(editingTrip.tempId)) {
        await updateTrip(reimbursementId, editingTrip.tempId, tripForm)
      } else {
        await createTrip(reimbursementId, tripForm)
      }

      tripDialogVisible.value = false
      await loadEditData()
      ElMessage.success(isEditing ? '行程修改并重新计算成功' : '行程添加并重新计算成功')
    } catch {
      return
    } finally {
      saving.value = false
    }
    return
  }

  const trip: TripDraft = {
    ...tripForm,
    tempId: isEditing ? (editingTrip?.tempId ?? crypto.randomUUID()) : crypto.randomUUID(),
  }

  if (isEditing) {
    tripList.value.splice(editingTripIndex.value, 1, trip)
  } else {
    tripList.value.push(trip)
  }

  tripDialogVisible.value = false
  ElMessage.success(isEditing ? '行程修改成功' : '行程添加成功')
}

async function deleteTrip(index: number) {
  const trip = tripList.value[index]
  if (!trip) {
    return
  }

  try {
    await ElMessageBox.confirm('确定删除这条行程吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    if (reimbursementId && initialTripIds.has(trip.tempId)) {
      await removeTrip(reimbursementId, trip.tempId)
      await loadEditData()
    } else {
      tripList.value.splice(index, 1)
    }

    ElMessage.success('删除成功')
  } catch {
    // User cancelled the operation.
  }
}

async function copyTrip(index: number) {
  const trip = tripList.value[index]
  if (!trip) {
    return
  }

  if (reimbursementId) {
    await createTrip(reimbursementId, toTripForm(trip))
    await loadEditData()
    ElMessage.success('复制并重新计算成功')
  } else {
    tripList.value.push({
      ...trip,
      tempId: crypto.randomUUID(),
    })
    ElMessage.success('复制成功')
  }
}

function toTripForm(trip: TripDraft): TripForm {
  return {
    travelerId: trip.travelerId,
    travelerNo: trip.travelerNo,
    travelerName: trip.travelerName,
    departureCityNo: trip.departureCityNo,
    departureCityName: trip.departureCityName,
    arrivalCityNo: trip.arrivalCityNo,
    arrivalCityName: trip.arrivalCityName,
    arrivalCityType: trip.arrivalCityType,
    departureDate: trip.departureDate,
    arrivalDate: trip.arrivalDate,
    tripDescription: trip.tripDescription,
  }
}

async function loadEditData() {
  if (!reimbursementId) {
    return
  }

  loading.value = true

  try {
    const [main, trips, allowances, allocations] = await Promise.all([
      getReimbursementDetail(reimbursementId),
      getTripList(reimbursementId),
      getAllowanceList(reimbursementId),
      getAllocationList(reimbursementId),
    ])

    if (main.status !== 0) {
      ElMessage.warning('只有草稿状态的报销单允许编辑')
      await router.replace(`/reimburse/${reimbursementId}`)
      return
    }

    Object.assign(form, {
      documentDate: main.documentDate,
      reimTitle: main.reimTitle,
      travelReason: main.travelReason,
      reimUserId: main.reimUserId,
      reimUserNo: main.reimUserNo,
      reimUserName: main.reimUserName,
      reimDeptId: main.reimDeptId,
      reimDeptNo: main.reimDeptNo,
      reimDeptName: main.reimDeptName,
      companyId: main.companyId,
      companyNo: main.companyNo,
      companyName: main.companyName,
      businessTypeId: main.businessTypeId,
      businessTypeNo: main.businessTypeNo,
      businessTypeName: main.businessTypeName,
      remark: main.remark ?? '',
    })

    initialTripIds.clear()
    tripList.value = trips.map((trip) => {
      initialTripIds.add(trip.id)
      return {
        ...trip,
        tempId: trip.id,
      }
    })

    totalAllowanceAmount.value = Number(main.totalAllowanceAmount)
    allowanceAmounts.meal = Number(main.mealAllowanceAmount)
    allowanceAmounts.traffic = Number(main.trafficAllowanceAmount)
    allowanceAmounts.communication = Number(main.communicationAllowanceAmount)
    allowanceList.value = allowances
    allocationList.value = allocations.map((item) => ({
      ...item,
      tempId: item.id,
      allocationPercent: Number(item.allocationRatio) * 100,
      allocationAmount: Number(item.allocationAmount),
    }))

    if (allocationList.value.length === 0) {
      addAllocation()
    }
    recalculateAllocations()
  } finally {
    loading.value = false
  }
}

async function validateMainForm() {
  if (!mainFormRef.value) {
    return false
  }

  try {
    await mainFormRef.value.validate()
    return true
  } catch {
    return false
  }
}

function validateAllocations() {
  if (!reimbursementId) {
    return true
  }

  if (allocationList.value.length === 0) {
    ElMessage.warning('请至少添加一条费用分摊')
    return false
  }

  if (allocationList.value.some((item) => !item.companyId || !item.projectId)) {
    ElMessage.warning('请选择每条分摊信息的费用归属和项目')
    return false
  }

  if (Math.abs(allocationPercentTotal.value - 100) > 0.0001) {
    ElMessage.warning('费用分摊比例合计必须为100%')
    return false
  }

  if (Math.abs(allocationAmountTotal.value - totalAllowanceAmount.value) > 0.001) {
    ElMessage.warning('费用分摊金额合计必须等于补助总金额')
    return false
  }

  return true
}

function buildAllocationPayload(): AllocationSaveForm[] {
  const payload = allocationList.value.map((item) => ({
    companyId: item.companyId,
    companyNo: item.companyNo,
    companyName: item.companyName,
    projectId: item.projectId,
    projectNo: item.projectNo,
    projectName: item.projectName,
    allocationRatio: Number((item.allocationPercent / 100).toFixed(4)),
    allocationAmount: item.allocationAmount,
  }))

  return payload
}

async function persistDraft() {
  let targetId = reimbursementId

  if (targetId) {
    await updateReimbursement(targetId, form)

    const currentTripIds = new Set(
      tripList.value.map((trip) => trip.tempId).filter((id) => initialTripIds.has(id)),
    )

    for (const id of initialTripIds) {
      if (!currentTripIds.has(id)) {
        await removeTrip(targetId, id)
      }
    }

    for (const trip of tripList.value) {
      const tripData = toTripForm(trip)

      if (initialTripIds.has(trip.tempId)) {
        await updateTrip(targetId, trip.tempId, tripData)
      } else {
        await createTrip(targetId, tripData)
      }
    }

    const latestMain = await getReimbursementDetail(targetId)
    totalAllowanceAmount.value = Number(latestMain.totalAllowanceAmount)
    allowanceAmounts.meal = Number(latestMain.mealAllowanceAmount)
    allowanceAmounts.traffic = Number(latestMain.trafficAllowanceAmount)
    allowanceAmounts.communication = Number(latestMain.communicationAllowanceAmount)
    recalculateAllocations()
    await saveAllocationList(targetId, buildAllocationPayload())
  } else {
    targetId = await createReimbursement(form)

    for (const trip of tripList.value) {
      await createTrip(targetId, toTripForm(trip))
    }
  }

  return targetId
}

async function saveDraft() {
  if (!(await validateMainForm()) || !validateAllocations()) {
    return
  }

  saving.value = true

  try {
    await persistDraft()
    ElMessage.success(reimbursementId ? '报销单修改成功' : '报销单草稿保存成功')
    await router.push('/reimburse/list')
  } finally {
    saving.value = false
  }
}

async function submitDraft() {
  if (!(await validateMainForm()) || !validateAllocations()) {
    return
  }

  try {
    await ElMessageBox.confirm('提交后单据将进入审批中，确定提交吗？', '提交确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  saving.value = true

  try {
    const targetId = await persistDraft()
    await submitReimbursement(targetId)
    ElMessage.success('提交成功')
    await router.push('/reimburse/list')
  } finally {
    saving.value = false
  }
}

async function closeForm() {
  try {
    await ElMessageBox.confirm('当前填写内容尚未保存，确定关闭吗？', '关闭确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await router.push('/reimburse/list')
  } catch {
    // User cancelled the operation.
  }
}

onMounted(async () => {
  await loadDictionaries()
  await loadEditData()
})
</script>

<template>
  <main v-loading="loading" class="form-page">
    <header class="document-header">
      <h1>差旅费用报销单</h1>
      <div class="document-date">
        提单日期
        <span>{{ form.documentDate }}</span>
      </div>
    </header>

    <div class="form-content">
      <section class="form-section">
        <div
          class="section-title collapsible-title"
          @click="sectionExpanded.basic = !sectionExpanded.basic"
        >
          <span>基础信息</span>
          <el-icon>
            <ArrowDown v-if="sectionExpanded.basic" />
            <ArrowRight v-else />
          </el-icon>
        </div>

        <el-form
          v-show="sectionExpanded.basic"
          ref="mainFormRef"
          :model="form"
          :rules="mainFormRules"
          label-width="120px"
        >
          <el-form-item label="报销标题" prop="reimTitle">
            <el-input v-model="form.reimTitle" placeholder="请输入报销标题" />
          </el-form-item>

          <div class="three-column-grid">
            <el-form-item label="报销人" prop="reimUserId">
              <el-select v-model="form.reimUserId" placeholder="请选择" @change="handleUserChange">
                <el-option
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="`${item.name}[${item.no}]`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="报销部门" prop="reimDeptId">
              <el-select
                v-model="form.reimDeptId"
                placeholder="请选择"
                @change="handleDepartmentChange"
              >
                <el-option
                  v-for="item in departmentOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="费用归属公司" prop="companyId">
              <el-select
                v-model="form.companyId"
                placeholder="请选择"
                @change="handleCompanyChange"
              >
                <el-option
                  v-for="item in companyOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="业务类型" prop="businessTypeId" class="business-type-item">
            <el-select
              v-model="form.businessTypeId"
              placeholder="请选择"
              @change="handleBusinessTypeChange"
            >
              <el-option
                v-for="item in businessTypeOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="出差事由" prop="travelReason">
            <el-input v-model="form.travelReason" type="textarea" :rows="3" placeholder="请输入" />
          </el-form-item>
        </el-form>
      </section>

      <section class="form-section trip-section">
        <div
          class="section-title section-title-with-action collapsible-title"
          @click="sectionExpanded.trip = !sectionExpanded.trip"
        >
          <span class="section-heading">
            补录行程
            <el-icon>
              <ArrowDown v-if="sectionExpanded.trip" />
              <ArrowRight v-else />
            </el-icon>
          </span>
          <el-button link type="primary" @click.stop="openCreateTrip">
            <el-icon><CirclePlus /></el-icon>
            补录行程
          </el-button>
        </div>

        <el-table v-show="sectionExpanded.trip" :data="tripList" border class="trip-table">
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
          <el-table-column
            prop="tripDescription"
            label="行程说明"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column label="操作" width="130" align="center">
            <template #default="{ $index }">
              <el-button link type="primary" title="删除" @click="deleteTrip($index)">
                <el-icon><Delete /></el-icon>
              </el-button>
              <el-button link type="primary" title="编辑" @click="openEditTrip($index)">
                <el-icon><EditPen /></el-icon>
              </el-button>
              <el-button link type="primary" title="复制" @click="copyTrip($index)">
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <template v-if="reimbursementId">
        <section class="form-section trip-section">
          <div
            class="section-title allowance-section-title collapsible-title"
            @click="sectionExpanded.allowance = !sectionExpanded.allowance"
          >
            <span class="section-heading">
              补助信息
              <el-icon>
                <ArrowDown v-if="sectionExpanded.allowance" />
                <ArrowRight v-else />
              </el-icon>
              <span class="section-subtitle">{{ totalAllowanceAmount.toFixed(2) }}</span>
              <span v-if="allowanceTravelerSummary" class="section-subtitle">
                （{{ allowanceTravelerSummary }}）
              </span>
            </span>
          </div>
          <div v-show="sectionExpanded.allowance">
            <el-alert
              title="1、请根据实际出差日期选择补助  2、出差期间当日有用餐安排的请自行核减当日餐补  3、出差期间当日有用车的，请自行核减当日交补"
              type="warning"
              :closable="false"
              show-icon
              class="allowance-notice"
            />
            <el-table :data="allowanceList" border class="allowance-table">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="travelerName" label="出行人" min-width="130" />
              <el-table-column label="出差日期" min-width="210">
                <template #default="{ row }"> {{ row.startDate }} 至 {{ row.endDate }} </template>
              </el-table-column>
              <el-table-column prop="allowanceDays" label="补助天数" width="100" align="center" />
              <el-table-column label="行程" min-width="150">
                <template #default="{ row }">
                  {{ row.departureCityName }}-{{ row.allowanceCityName }}
                </template>
              </el-table-column>
              <el-table-column prop="allowanceCityName" label="补助城市" min-width="120" />
              <el-table-column label="申请金额" width="120" align="right">
                <template #default="{ row }">
                  {{ Number(row.applyAmount).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="补助金额" width="120" align="right">
                <template #default="{ row }">
                  {{ Number(row.allowanceAmount).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="90" align="center">
                <template #default="{ row }">
                  <el-button
                    link
                    type="primary"
                    title="调整补助明细"
                    @click="openAllowanceAdjustment(row)"
                  >
                    <el-icon><EditPen /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

        <section class="form-section trip-section">
          <div
            class="section-title collapsible-title"
            @click="sectionExpanded.total = !sectionExpanded.total"
          >
            <span>费用合计</span>
            <el-icon>
              <ArrowDown v-if="sectionExpanded.total" />
              <ArrowRight v-else />
            </el-icon>
          </div>
          <div v-show="sectionExpanded.total" class="amount-summary">
            <span>补助总金额 {{ totalAllowanceAmount.toFixed(2) }}</span>
            <span>餐费补助 {{ allowanceAmounts.meal.toFixed(2) }}</span>
            <span>交通补助 {{ allowanceAmounts.traffic.toFixed(2) }}</span>
            <span> 通讯补助 {{ allowanceAmounts.communication.toFixed(2) }} </span>
          </div>
        </section>

        <section class="form-section trip-section">
          <div
            class="section-title section-title-with-action collapsible-title"
            @click="sectionExpanded.allocation = !sectionExpanded.allocation"
          >
            <span class="section-heading">
              费用归属及分摊
              <el-icon>
                <ArrowDown v-if="sectionExpanded.allocation" />
                <ArrowRight v-else />
              </el-icon>
              <span class="section-subtitle">
                （分摊金额：{{ totalAllowanceAmount.toFixed(2) }}）
              </span>
            </span>
            <el-button link type="primary" @click.stop="distributeAllocationsEqually">
              均摊
            </el-button>
          </div>

          <div v-show="sectionExpanded.allocation">
            <el-table :data="allocationList" border class="allocation-table">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="费用归属" min-width="230">
                <template #default="{ row, $index }">
                  <el-select
                    v-model="row.companyId"
                    placeholder="请选择"
                    @change="handleAllocationCompanyChange($index, $event)"
                  >
                    <el-option
                      v-for="company in companyOptions"
                      :key="company.id"
                      :label="`[${company.no}]${company.name}`"
                      :value="company.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="项目" min-width="230">
                <template #default="{ row, $index }">
                  <el-select
                    v-model="row.projectId"
                    placeholder="请选择"
                    @change="handleAllocationProjectChange($index, $event)"
                  >
                    <el-option
                      v-for="project in projectOptions"
                      :key="project.id"
                      :label="`[${project.no}]${project.name}`"
                      :value="project.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="分摊比例" min-width="180">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.allocationPercent"
                    :min="0"
                    :max="100"
                    :precision="2"
                    :step="10"
                    controls-position="right"
                    :disabled="$index === 0"
                    @change="handleAllocationPercentChange($index)"
                  />
                  <span class="percent-symbol">%</span>
                </template>
              </el-table-column>
              <el-table-column label="分摊金额" min-width="170">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.allocationAmount"
                    :precision="2"
                    :controls="false"
                    disabled
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button link type="primary" title="删除" @click="removeAllocation($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-button link type="primary" class="add-allocation" @click="addAllocation">
              <el-icon><CirclePlus /></el-icon>
              添加一行
            </el-button>

            <div class="allocation-total">
              <span>合计</span>
              <span>{{ allocationPercentTotal.toFixed(2) }}%</span>
              <span>CNY {{ allocationAmountTotal.toFixed(2) }}</span>
            </div>
          </div>
        </section>
      </template>

      <section class="form-section trip-section">
        <div
          class="section-title section-title-with-action collapsible-title"
          @click="sectionExpanded.remark = !sectionExpanded.remark"
        >
          <span class="section-heading">
            备注信息
            <el-icon>
              <ArrowDown v-if="sectionExpanded.remark" />
              <ArrowRight v-else />
            </el-icon>
          </span>
          <el-button link type="primary" :disabled="!form.remark" @click.stop="clearRemark">
            <el-icon><Delete /></el-icon>
            删除备注
          </el-button>
        </div>
        <el-input
          v-show="sectionExpanded.remark"
          v-model="form.remark"
          type="textarea"
          :rows="4"
          maxlength="1000"
          show-word-limit
          placeholder="请输入备注"
        />
      </section>
    </div>

    <el-dialog
      v-model="tripDialogVisible"
      :title="editingTripIndex >= 0 ? '编辑行程' : '补录行程'"
      width="820px"
      destroy-on-close
      @closed="resetTripForm"
    >
      <el-alert
        title="仅可补录未从申请单带入或未产生费用的行程信息"
        type="warning"
        :closable="false"
        show-icon
        class="trip-alert"
      />

      <el-form
        ref="tripFormRef"
        :model="tripForm"
        :rules="tripFormRules"
        label-width="140px"
        class="trip-dialog-form"
      >
        <el-form-item label="出行人" prop="travelerId">
          <el-select
            v-model="tripForm.travelerId"
            placeholder="请选择"
            @change="handleTripTravelerChange"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="`${item.name}[${item.no}]`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="出发城市" prop="departureCityNo">
          <el-select
            v-model="tripForm.departureCityNo"
            placeholder="请选择"
            filterable
            @change="handleDepartureCityChange"
          >
            <el-option
              v-for="item in cityOptions"
              :key="item.no"
              :label="item.name"
              :value="item.no"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="到达城市" prop="arrivalCityNo">
          <el-select
            v-model="tripForm.arrivalCityNo"
            placeholder="请选择"
            filterable
            @change="handleArrivalCityChange"
          >
            <el-option
              v-for="item in cityOptions"
              :key="item.no"
              :label="item.name"
              :value="item.no"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="出发到达日期" required>
          <el-date-picker
            v-model="tripDateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="出发日期"
            end-placeholder="到达日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disableFutureDate"
          />
        </el-form-item>

        <el-form-item label="行程说明" prop="tripDescription">
          <el-input
            v-model="tripForm.tripDescription"
            type="textarea"
            :rows="3"
            maxlength="500"
            placeholder="请输入行程说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="tripDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTrip">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="allowanceDialogVisible" title="补助日历" width="1280px" destroy-on-close>
      <div class="allowance-dialog-content">
        <aside v-if="adjustingAllowance" class="allowance-trip-panel">
          <div class="trip-type">
            <strong>出差类型</strong>
            <span>{{ form.businessTypeName }}</span>
          </div>
          <div class="trip-timeline">
            <div>
              <span>开始日期</span>
              <strong>{{ adjustingAllowance.startDate }}</strong>
            </div>
            <div class="trip-route">
              <span>行程天数</span>
              <strong>
                {{ adjustingAllowance.departureCityName }} -
                {{ adjustingAllowance.allowanceCityName }}
                {{ adjustingAllowance.allowanceDays }}天
              </strong>
            </div>
            <div>
              <span>结束日期</span>
              <strong>{{ adjustingAllowance.endDate }}</strong>
            </div>
          </div>
          <div class="allowance-totals">
            <div>
              <span>补助金额</span>
              <strong>CNY {{ adjustedAllowanceTotal.toFixed(2) }}</strong>
            </div>
            <div>
              <span>标准总额</span>
              <strong>CNY {{ selectedStandardTotal.toFixed(2) }}</strong>
            </div>
            <div>
              <span>补助金额</span>
              <strong>CNY {{ adjustedAllowanceTotal.toFixed(2) }}</strong>
            </div>
          </div>
        </aside>

        <div class="allowance-calendar-panel">
          <div class="calendar-toolbar">
            <strong>出差补助</strong>
            <el-checkbox v-model="allAllowanceSelected">全选</el-checkbox>
          </div>
          <el-table :data="allowanceCalendarDraft" border class="allowance-calendar-table">
            <el-table-column label="出差日期" width="165" align="center">
              <template #default="{ row, $index }">
                <div>{{ row.allowanceDate }}</div>
                <div class="weekday-row">
                  {{ formatWeekday(row.weekdayNo) }}
                  <el-checkbox
                    :model-value="isAllowanceRowSelected($index)"
                    @change="setAllowanceRowSelected($index, Boolean($event))"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="补助城市" min-width="125" align="center">
              <template #default>{{ adjustingAllowance?.allowanceCityName }}</template>
            </el-table-column>
            <el-table-column min-width="210" align="center">
              <template #header>
                餐费补助
                <el-checkbox
                  :model-value="isAllowanceCategorySelected('mealSelected')"
                  @change="
                    setAllowanceCategorySelected(
                      'mealSelected',
                      'mealActualAmount',
                      'mealStandardAmount',
                      Boolean($event),
                    )
                  "
                />
              </template>
              <template #default="{ row, $index }">
                <div class="daily-standard">
                  CNY {{ Number(row.mealStandardAmount).toFixed(2) }} / 天
                </div>
                <div class="allowance-input">
                  <el-checkbox
                    v-model="row.mealSelected"
                    :true-value="1"
                    :false-value="0"
                    @change="
                      handleAllowanceItemToggle(
                        $index,
                        'mealActualAmount',
                        'mealStandardAmount',
                        Boolean($event),
                      )
                    "
                  />
                  <el-input-number
                    v-model="row.mealActualAmount"
                    :min="0.01"
                    :max="Number(row.mealStandardAmount)"
                    :precision="2"
                    :controls="false"
                    :disabled="row.mealSelected !== 1"
                    @change="recalculateCalendarRow($index)"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="210" align="center">
              <template #header>
                交通补助
                <el-checkbox
                  :model-value="isAllowanceCategorySelected('trafficSelected')"
                  @change="
                    setAllowanceCategorySelected(
                      'trafficSelected',
                      'trafficActualAmount',
                      'trafficStandardAmount',
                      Boolean($event),
                    )
                  "
                />
              </template>
              <template #default="{ row, $index }">
                <div class="daily-standard">
                  CNY {{ Number(row.trafficStandardAmount).toFixed(2) }} / 天
                </div>
                <div class="allowance-input">
                  <el-checkbox
                    v-model="row.trafficSelected"
                    :true-value="1"
                    :false-value="0"
                    @change="
                      handleAllowanceItemToggle(
                        $index,
                        'trafficActualAmount',
                        'trafficStandardAmount',
                        Boolean($event),
                      )
                    "
                  />
                  <el-input-number
                    v-model="row.trafficActualAmount"
                    :min="0.01"
                    :max="Number(row.trafficStandardAmount)"
                    :precision="2"
                    :controls="false"
                    :disabled="row.trafficSelected !== 1"
                    @change="recalculateCalendarRow($index)"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="210" align="center">
              <template #header>
                通讯补助
                <el-checkbox
                  :model-value="isAllowanceCategorySelected('communicationSelected')"
                  @change="
                    setAllowanceCategorySelected(
                      'communicationSelected',
                      'communicationActualAmount',
                      'communicationStandardAmount',
                      Boolean($event),
                    )
                  "
                />
              </template>
              <template #default="{ row, $index }">
                <div class="daily-standard">
                  CNY {{ Number(row.communicationStandardAmount).toFixed(2) }} / 天
                </div>
                <div class="allowance-input">
                  <el-checkbox
                    v-model="row.communicationSelected"
                    :true-value="1"
                    :false-value="0"
                    @change="
                      handleAllowanceItemToggle(
                        $index,
                        'communicationActualAmount',
                        'communicationStandardAmount',
                        Boolean($event),
                      )
                    "
                  />
                  <el-input-number
                    v-model="row.communicationActualAmount"
                    :min="0.01"
                    :max="Number(row.communicationStandardAmount)"
                    :precision="2"
                    :controls="false"
                    :disabled="row.communicationSelected !== 1"
                    @change="recalculateCalendarRow($index)"
                  />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="allowanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveAllowanceAdjustment">
          保存调整
        </el-button>
      </template>
    </el-dialog>

    <footer class="form-actions">
      <el-button @click="closeForm">关闭</el-button>
      <el-button type="primary" :loading="saving" @click="saveDraft">
        {{ reimbursementId ? '保存修改' : '保存草稿' }}
      </el-button>
      <el-button v-if="reimbursementId" type="primary" :loading="saving" @click="submitDraft">
        提交
      </el-button>
    </footer>
  </main>
</template>

<style scoped>
.form-page {
  min-height: 100vh;
  padding-bottom: 72px;
  background: #f5f6f8;
  color: #303133;
  box-sizing: border-box;
}

.document-header {
  position: relative;
  height: 54px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.document-header h1 {
  margin: 0;
  line-height: 54px;
  text-align: center;
  font-size: 20px;
  font-weight: 600;
}

.document-date {
  position: absolute;
  top: 0;
  right: 24px;
  min-width: 180px;
  line-height: 54px;
  text-align: right;
  white-space: nowrap;
  color: #606266;
  font-size: 14px;
}

.document-date span {
  margin-left: 12px;
}

.form-content {
  width: calc(100% - 40px);
  max-width: 1180px;
  margin: 18px auto;
  padding: 16px 20px 28px;
  background: #ffffff;
  box-sizing: border-box;
}

.form-section {
  width: 100%;
}

.section-title {
  position: relative;
  height: 36px;
  margin-bottom: 14px;
  padding-left: 22px;
  line-height: 36px;
  background: #f0f2f5;
  color: #303133;
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

.collapsible-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 14px;
  cursor: pointer;
  user-select: none;
}

.section-heading {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.collapsible-title > .el-icon,
.section-heading > .el-icon {
  color: #909399;
}

.three-column-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  column-gap: 28px;
}

.business-type-item {
  width: calc((100% - 56px) / 3);
}

.form-content :deep(.el-select) {
  width: 100%;
}

.form-content :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form-content :deep(.el-input__wrapper),
.form-content :deep(.el-select__wrapper) {
  min-height: 32px;
}

.trip-section {
  margin-top: 12px;
}

.section-title-with-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 14px;
}

.section-title-with-action :deep(.el-button) {
  font-size: 14px;
}

.trip-table {
  width: 100%;
  font-size: 13px;
}

.trip-table :deep(.el-table__header th) {
  height: 40px;
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

.trip-table :deep(.el-table__row td) {
  height: 40px;
  padding: 0;
}

.trip-table :deep(.el-button) {
  margin-left: 4px;
  padding: 4px;
}

.allowance-table,
.allowance-calendar-table {
  width: 100%;
  font-size: 13px;
}

.allowance-alert {
  margin-bottom: 14px;
}

.allowance-notice {
  margin: 0 0 12px;
}

.allowance-dialog-content {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  min-height: 420px;
}

.allowance-trip-panel {
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #ffffff;
}

.trip-type {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 42px;
  padding: 0 16px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
}

.trip-type span {
  color: #606266;
}

.trip-timeline {
  padding: 20px 18px;
}

.trip-timeline > div {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-height: 52px;
  padding-left: 20px;
}

.trip-timeline > div::before {
  position: absolute;
  top: 5px;
  left: 2px;
  width: 8px;
  height: 8px;
  border: 2px solid #409eff;
  border-radius: 50%;
  background: #ffffff;
  content: '';
}

.trip-timeline > div:not(:last-child)::after {
  position: absolute;
  top: 17px;
  bottom: -5px;
  left: 7px;
  width: 1px;
  background: #c6e2ff;
  content: '';
}

.trip-timeline span,
.allowance-totals span {
  color: #909399;
  font-size: 12px;
}

.trip-timeline strong {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.trip-route strong {
  color: #409eff;
}

.allowance-totals {
  border-top: 1px solid #ebeef5;
}

.allowance-totals > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 42px;
  padding: 0 16px;
  border-bottom: 1px solid #ebeef5;
}

.allowance-totals strong {
  color: #f56c6c;
  font-weight: 500;
}

.allowance-calendar-panel {
  min-width: 0;
}

.calendar-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 42px;
  padding: 0 14px;
  border: 1px solid #dcdfe6;
  border-bottom: 0;
  background: #f5f7fa;
}

.allowance-calendar-table :deep(.el-table__header th) {
  height: 44px;
  background: #f5f7fa;
  color: #303133;
}

.allowance-calendar-table :deep(.el-table__row td) {
  padding: 10px 0;
}

.weekday-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}

.daily-standard {
  margin-bottom: 8px;
  color: #e6a23c;
  font-size: 12px;
}

.allowance-input {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.allowance-input :deep(.el-input-number) {
  width: 118px;
}

.allowance-input :deep(.el-input__inner) {
  text-align: right;
}

.standard-amount {
  display: block;
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}

.amount-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 0 40px;
  line-height: 44px;
}

.section-subtitle {
  color: #606266;
  font-size: 14px;
  font-weight: 400;
}

.allocation-table :deep(.el-input-number) {
  width: calc(100% - 24px);
}

.allocation-table :deep(.el-select) {
  width: 100%;
}

.percent-symbol {
  margin-left: 6px;
}

.add-allocation {
  display: flex;
  margin: 10px auto;
}

.allocation-total {
  display: grid;
  grid-template-columns: 1fr 180px 250px;
  align-items: center;
  min-height: 40px;
  padding: 0 20px;
  background: #fff7e6;
  color: #e6a23c;
}

.trip-alert {
  margin-bottom: 18px;
}

.trip-dialog-form {
  width: 620px;
  margin: 0 auto;
}

.trip-dialog-form :deep(.el-select),
.trip-dialog-form :deep(.el-date-editor) {
  width: 100%;
}

.form-actions {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 64px;
  border-top: 1px solid #ebeef5;
  background: #ffffff;
}

.form-actions :deep(.el-button) {
  min-width: 72px;
}

@media (max-width: 1100px) {
  .allowance-dialog-content {
    grid-template-columns: 1fr;
  }
}
</style>
