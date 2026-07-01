import request from './request'
import type {
  AllocationDetail,
  AllocationSaveForm,
  AllowanceCalendarAdjustForm,
  AllowanceDetail,
  OperationLogDetail,
  ReimbursementDictionaries,
  ReimbursementDetail,
  ReimbursementForm,
  ReimbursementPage,
  ReimbursementQuery,
  TripDetail,
  TripForm,
} from '@/types/reimbursement'

export function getReimbursementPage(params: ReimbursementQuery) {
  return request.get<ReimbursementPage>('/reimburse/page', {
    params,
  })
}

export function submitReimbursement(id: string) {
  return request.post<void>(`/reimburse/${id}/submit`)
}

export function voidReimbursement(id: string) {
  return request.post<void>(`/reimburse/${id}/void`)
}

export function deleteReimbursement(id: string) {
  return request.delete<void>(`/reimburse/${id}`)
}

export function copyReimbursement(id: string) {
  return request.post<string>(`/reimburse/${id}/copy`)
}

export function createReimbursement(data: ReimbursementForm) {
  return request.post<string>('/reimburse', data)
}

export function createTrip(reimId: string, data: TripForm) {
  return request.post<string>(`/reimburse/${reimId}/trips`, data)
}

export function updateReimbursement(id: string, data: ReimbursementForm) {
  return request.put<void>(`/reimburse/${id}`, data)
}

export function updateTrip(reimId: string, tripId: string, data: TripForm) {
  return request.put<void>(`/reimburse/${reimId}/trips/${tripId}`, data)
}

export function removeTrip(reimId: string, tripId: string) {
  return request.delete<void>(`/reimburse/${reimId}/trips/${tripId}`)
}

export function getReimbursementDetail(id: string) {
  return request.get<ReimbursementDetail>(`/reimburse/${id}`)
}

export function getTripList(reimId: string) {
  return request.get<TripDetail[]>(`/reimburse/${reimId}/trips`)
}

export function getAllowanceList(reimId: string) {
  return request.get<AllowanceDetail[]>(`/reimburse/${reimId}/allowances`)
}

export function adjustAllowanceCalendar(
  reimId: string,
  allowanceId: string,
  data: AllowanceCalendarAdjustForm[],
) {
  return request.put<void>(`/reimburse/${reimId}/allowances/${allowanceId}/calendar`, data)
}

export function getAllocationList(reimId: string) {
  return request.get<AllocationDetail[]>(`/reimburse/${reimId}/allocations`)
}

export function saveAllocationList(reimId: string, data: AllocationSaveForm[]) {
  return request.post<void>(`/reimburse/${reimId}/allocations`, data)
}

export function getOperationLogList(reimId: string) {
  return request.get<OperationLogDetail[]>(`/reimburse/${reimId}/logs`)
}

export function getReimbursementDictionaries() {
  return request.get<ReimbursementDictionaries>('/dictionaries/reimbursement')
}
