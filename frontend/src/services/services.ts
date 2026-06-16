import api from './api';
import type {
  AuthResponse, Dashboard, Member, MemberDashboard, MemberDetail,
  Contribution, Loan, Payment, ExternalBorrower, BorrowerDetail, ActivityLog,
  ImportHistory, ImportResult, ContributionSummary, PendingContributionReport,
} from '../types';

export const authApi = {
  adminLogin: (phoneNumber: string, pin: string) =>
    api.post<AuthResponse>('/auth/admin/login', { phoneNumber, pin }),
  memberLogin: (phoneNumber: string) =>
    api.post<AuthResponse>('/auth/member/login', { phoneNumber }),
};

export const dashboardApi = {
  admin: () => api.get<Dashboard>('/admin/dashboard'),
  member: () => api.get<MemberDashboard>('/member/dashboard'),
};

export const memberApi = {
  getAll: (search?: string) => api.get<Member[]>('/admin/members', { params: { search } }),
  getById: (id: number) => api.get<MemberDetail>(`/admin/members/${id}`),
  create: (data: Partial<Member>) => api.post<Member>('/admin/members', data),
  update: (id: number, data: Partial<Member>) => api.put<Member>(`/admin/members/${id}`, data),
  disable: (id: number) => api.patch<Member>(`/admin/members/${id}/disable`),
  profile: () => api.get<Member>('/member/profile'),
};

export const contributionApi = {
  getAll: (params?: Record<string, string | number>) =>
    api.get<Contribution[]>('/admin/contributions', { params }),
  summaries: () => api.get<ContributionSummary[]>('/admin/contributions/summaries'),
  memberSummary: (memberId: number) =>
    api.get<ContributionSummary>(`/admin/contributions/members/${memberId}/summary`),
  memberTimeline: (memberId: number) =>
    api.get<Contribution[]>(`/admin/contributions/members/${memberId}/timeline`),
  pendingReport: () => api.get<PendingContributionReport>('/admin/contributions/pending-report'),
  record: (data: object) => api.post<Contribution>('/admin/contributions', data),
  updateStatus: (id: number, status: string) =>
    api.patch<Contribution>(`/admin/contributions/${id}/status`, null, { params: { status } }),
  memberContributions: () => api.get<Contribution[]>('/member/contributions'),
  memberOwnSummary: () => api.get<ContributionSummary>('/member/contributions/summary'),
};

export const loanApi = {
  getAll: (params?: Record<string, string>) => api.get<Loan[]>('/admin/loans', { params }),
  getById: (id: number) => api.get<Loan>(`/admin/loans/${id}`),
  issue: (data: object) => api.post<Loan>('/admin/loans', data),
  update: (id: number, data: object) => api.put<Loan>(`/admin/loans/${id}`, data),
  close: (id: number) => api.patch<Loan>(`/admin/loans/${id}/close`),
  principalPayments: (id: number) => api.get<Payment[]>(`/admin/loans/${id}/principal-payments`),
  interestPayments: (id: number) => api.get<Payment[]>(`/admin/loans/${id}/interest-payments`),
  memberLoans: () => api.get<Loan[]>('/member/loans'),
};

export const paymentApi = {
  principal: (data: object) => api.post<Payment>('/admin/payments/principal', data),
  interest: (data: object) => api.post<Payment>('/admin/payments/interest', data),
};

export const borrowerApi = {
  getAll: (search?: string) => api.get<ExternalBorrower[]>('/admin/borrowers', { params: { search } }),
  getById: (id: number) => api.get<BorrowerDetail>(`/admin/borrowers/${id}`),
  create: (data: object) => api.post<ExternalBorrower>('/admin/borrowers', data),
  update: (id: number, data: object) => api.put<ExternalBorrower>(`/admin/borrowers/${id}`, data),
};

export const activityApi = {
  getAll: () => api.get<ActivityLog[]>('/admin/activity-logs'),
};

export const importApi = {
  upload: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<ImportResult>('/admin/imports/excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  history: () => api.get<ImportHistory[]>('/admin/imports'),
};

export const reportApi = {
  membersExcel: () => api.get('/admin/reports/members/excel', { responseType: 'blob' }),
  contributionsExcel: () => api.get('/admin/reports/contributions/excel', { responseType: 'blob' }),
  loansExcel: () => api.get('/admin/reports/loans/excel', { responseType: 'blob' }),
  fundPdf: () => api.get('/admin/reports/fund/pdf', { responseType: 'blob' }),
};

export const downloadBlob = (data: Blob, filename: string) => {
  const url = window.URL.createObjectURL(data);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
};
