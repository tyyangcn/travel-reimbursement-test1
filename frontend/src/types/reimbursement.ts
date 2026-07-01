export interface ReimbursementQuery {
  reimNo?: string
  reimTitle?: string
  travelReason?: string
  companyId?: string
  reimDeptId?: string
  reimUserId?: string
  businessTypeId?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface ReimbursementPageItem {
  id: string
  reimNo: string
  reimTitle: string
  travelReason: string
  companyName: string
  reimDeptName: string
  reimDeptNo: string
  reimUserName: string
  reimUserNo: string
  businessTypeName: string
  status: number
  statusName: string
  totalAllowanceAmount: number
  createTime: string
}

export interface ReimbursementPage {
  records: ReimbursementPageItem[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ReimbursementForm {
  documentDate: string
  reimTitle: string
  travelReason: string
  reimUserId: string
  reimUserNo: string
  reimUserName: string
  reimDeptId: string
  reimDeptNo: string
  reimDeptName: string
  companyId: string
  companyNo: string
  companyName: string
  businessTypeId: string
  businessTypeNo: string
  businessTypeName: string
  remark?: string
}

export interface TripForm {
  travelerId: string
  travelerNo: string
  travelerName: string
  departureCityNo: string
  departureCityName: string
  arrivalCityNo: string
  arrivalCityName: string
  arrivalCityType: string
  departureDate: string
  arrivalDate: string
  tripDescription: string
}

export interface TripDraft extends TripForm {
  tempId: string
}

export interface ReimbursementDetail extends ReimbursementForm {
  id: string
  reimNo: string
  mealAllowanceAmount: number
  trafficAllowanceAmount: number
  communicationAllowanceAmount: number
  totalAllowanceAmount: number
  status: number
  statusName: string
  createUserName: string
  createTime: string
  updateTime: string
}

export interface TripDetail extends TripForm {
  id: string
  reimId: string
  sortNo: number
  createTime: string
  updateTime: string
}

export interface AllowanceCalendar {
  id: string
  allowanceId: string
  tripId: string
  allowanceDate: string
  weekdayNo: number
  mealSelected: number
  mealStandardAmount: number
  mealActualAmount: number
  trafficSelected: number
  trafficStandardAmount: number
  trafficActualAmount: number
  communicationSelected: number
  communicationStandardAmount: number
  communicationActualAmount: number
  dailyStandardAmount: number
  dailyActualAmount: number
}

export interface AllowanceDetail {
  id: string
  reimId: string
  tripId: string
  travelerId: string
  travelerName: string
  startDate: string
  endDate: string
  allowanceDays: number
  departureCityName: string
  allowanceCityNo: string
  allowanceCityName: string
  cityType: string
  standardTotalAmount: number
  applyAmount: number
  allowanceAmount: number
  sortNo: number
  calendarList: AllowanceCalendar[]
}

export interface AllowanceCalendarAdjustForm {
  calendarId: string
  mealSelected: number
  mealActualAmount: number
  trafficSelected: number
  trafficActualAmount: number
  communicationSelected: number
  communicationActualAmount: number
}

export interface AllocationDetail {
  id: string
  reimId: string
  companyId: string
  companyNo: string
  companyName: string
  projectId: string
  projectNo: string
  projectName: string
  allocationRatio: number
  allocationAmount: number
  sortNo: number
  createTime: string
  updateTime: string
}

export interface AllocationSaveForm {
  companyId: string
  companyNo: string
  companyName: string
  projectId: string
  projectNo: string
  projectName: string
  allocationRatio: number
  allocationAmount: number
}

export interface AllocationDraft {
  tempId: string
  companyId: string
  companyNo: string
  companyName: string
  projectId: string
  projectNo: string
  projectName: string
  allocationPercent: number
  allocationAmount: number
}

export interface OperationLogDetail {
  id: string
  operationType: string
  operationTypeName: string
  operatorName: string
  operationResult: string
  operationContent: string
  operationTime: string
}

export interface DictionaryOption {
  id: string
  no: string
  name: string
  type?: string
  departmentId?: string
  departmentNo?: string
  departmentName?: string
}

export interface ReimbursementDictionaries {
  companies: DictionaryOption[]
  departments: DictionaryOption[]
  employees: DictionaryOption[]
  businessTypes: DictionaryOption[]
  cities: DictionaryOption[]
  projects: DictionaryOption[]
}
