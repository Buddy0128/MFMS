export interface AuthResponse {
  token: string;
  role: 'ADMIN' | 'MEMBER';
  id: number;
  name: string;
  phoneNumber: string;
}

export interface Member {
  id: number;
  memberCode: string;
  fullName: string;
  phoneNumber: string;
  joinDate: string;
  status: 'ACTIVE' | 'INACTIVE';
  totalDeposit: number;
  currentLoanAmount: number;
  totalInterestPaid: number;
  paidMonths: number;
  pendingMonths: number;
  lastPaidMonth: string | null;
  completionPercentage: number;
  extraAmount: number;
  contributionValidationMessage: string | null;
  importedData: boolean;
  createdAt: string;
}

export interface Contribution {
  id: number;
  memberId: number;
  memberName: string;
  memberCode: string;
  month: number;
  year: number;
  amount: number;
  status: 'PAID' | 'PENDING';
  paymentDate: string | null;
  createdAt: string;
}

export interface Loan {
  id: number;
  borrowerType: 'MEMBER' | 'EXTERNAL';
  borrowerId: number;
  borrowerName: string;
  loanAmount: number;
  outstandingAmount: number;
  interestRate: number;
  monthlyInterest: number;
  pendingInterest: number;
  totalInterestPaid: number;
  loanDate: string;
  status: 'ACTIVE' | 'CLOSED';
  createdAt: string;
}

export interface Payment {
  id: number;
  loanId: number;
  amount: number;
  paymentDate: string;
  createdAt: string;
}

export interface ExternalBorrower {
  id: number;
  fullName: string;
  phoneNumber: string;
  address: string;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
}

export interface ActivityLog {
  id: number;
  adminName: string;
  action: string;
  description: string;
  createdAt: string;
}

export interface ChartDataPoint {
  label?: string;
  value: number;
  category?: string;
}

export interface Dashboard {
  totalMembers: number;
  totalContributions: number;
  totalExpectedCollection: number;
  totalActualCollection: number;
  totalPendingCollection: number;
  expectedContributionMonths: number;
  totalFundCollected: number;
  totalInterestEarned: number;
  availableFund: number;
  moneyLoanedOut: number;
  outstandingPrincipal: number;
  activeLoans: number;
  closedLoans: number;
  externalBorrowers: number;
  pendingContributions: number;
  totalPendingContributionAmount: number;
  pendingInterest: number;
  contributionTrend: ChartDataPoint[];
  interestTrend: ChartDataPoint[];
  loanDistribution: ChartDataPoint[];
  recentContributions: Contribution[];
  recentLoans: Loan[];
  recentInterestPayments: Payment[];
  recentActivities: ActivityLog[];
}

export interface ContributionSummary {
  memberId: number;
  memberCode: string;
  memberName: string;
  totalDeposit: number;
  monthlyContribution: number;
  expectedMonths: number;
  paidMonths: number;
  pendingMonths: number;
  lastPaidMonth: string | null;
  completionPercentage: number;
  expectedAmount: number;
  pendingAmount: number;
  extraAmount: number;
  validationMessage: string | null;
}

export interface PendingContributionReport {
  expectedMonths: number;
  monthlyContribution: number;
  totalMembers: number;
  totalExpectedCollection: number;
  totalActualCollection: number;
  totalPendingCollection: number;
  members: ContributionSummary[];
}

export interface ImportResult {
  totalRowsProcessed: number;
  newMembersAdded: number;
  existingMembersUpdated: number;
  failedRecords: number;
  importDateTime: string;
  errors: string[];
}

export interface ImportHistory {
  id: number;
  fileName: string;
  totalRows: number;
  newMembers: number;
  updatedMembers: number;
  failedRecords: number;
  importedBy: string;
  importedAt: string;
}

export interface MemberDashboard {
  member: Member;
  contributionSummary: ContributionSummary;
  totalContributions: number;
  outstandingPrincipal: number;
  pendingInterest: number;
  totalInterestPaid: number;
  availableFund: number;
  contributions: Contribution[];
  loans: Loan[];
  recentTransactions: Payment[];
}

export interface MemberDetail {
  member: Member;
  contributionSummary: ContributionSummary;
  outstandingAmount: number;
  pendingInterest: number;
  totalInterestPaid: number;
  contributions: Contribution[];
  loans: Loan[];
  interestPayments: Payment[];
}

export interface BorrowerDetail {
  borrower: ExternalBorrower;
  outstandingAmount: number;
  pendingInterest: number;
  loans: Loan[];
  interestPayments: Payment[];
}
