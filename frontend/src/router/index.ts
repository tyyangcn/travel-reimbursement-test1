import { createRouter, createWebHistory } from 'vue-router'
import ReimbursementList from '@/views/reimbursement/ReimbursementList.vue'
import ReimbursementForm from '@/views/reimbursement/ReimbursementForm.vue'
import ReimbursementDetail from '@/views/reimbursement/ReimbursementDetail.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/reimburse/list',
    },
    {
      path: '/reimburse/list',
      name: 'reimbursement-list',
      component: ReimbursementList,
    },
    {
      path: '/reimburse/create',
      name: 'reimbursement-create',
      component: ReimbursementForm,
    },
    {
      path: '/reimburse/:id/edit',
      name: 'reimbursement-edit',
      component: ReimbursementForm,
    },
    {
      path: '/reimburse/:id',
      name: 'reimbursement-detail',
      component: ReimbursementDetail,
    },
  ],
})

export default router
