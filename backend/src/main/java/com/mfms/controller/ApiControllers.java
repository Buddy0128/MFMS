package com.mfms.controller;

import com.mfms.dto.*;
import com.mfms.enums.ContributionStatus;
import com.mfms.enums.LoanStatus;
import com.mfms.security.UserPrincipal;
import com.mfms.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
class AuthController {

    private final AuthService authService;

    @PostMapping("/admin/login")
    @Operation(summary = "Admin login with phone and PIN")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(authService.adminLogin(request));
    }

    @PostMapping("/member/login")
    @Operation(summary = "Member login with phone number")
    public ResponseEntity<AuthResponse> memberLogin(@Valid @RequestBody MemberLoginRequest request) {
        return ResponseEntity.ok(authService.memberLogin(request));
    }
}

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard")
class AdminDashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}

@RestController
@RequestMapping("/member/dashboard")
@RequiredArgsConstructor
@Tag(name = "Member Dashboard")
class MemberDashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<MemberDashboardResponse> getDashboard(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(dashboardService.getMemberDashboard(user.getId()));
    }
}

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
@Tag(name = "Members")
class MemberController {
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAll(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(memberService.searchMembers(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberDetails(id));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request,
                                                  @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(memberService.createMember(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody MemberRequest request,
                                                  @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(memberService.updateMember(id, request, user.getId()));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<MemberResponse> disable(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(memberService.disableMember(id, user.getId()));
    }
}

@RestController
@RequestMapping("/admin/contributions")
@RequiredArgsConstructor
@Tag(name = "Contributions")
class ContributionController {
    private final ContributionService contributionService;

    @GetMapping
    public ResponseEntity<List<ContributionResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContributionStatus status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (search != null) return ResponseEntity.ok(contributionService.search(search));
        if (status != null || year != null || month != null) {
            return ResponseEntity.ok(contributionService.filter(status, year, month));
        }
        return ResponseEntity.ok(contributionService.getAll());
    }

    @GetMapping("/report")
    public ResponseEntity<List<ContributionResponse>> monthlyReport(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(contributionService.getMonthlyReport(year, month));
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<ContributionSummaryResponse>> summaries() {
        return ResponseEntity.ok(contributionService.getAllSummaries());
    }

    @GetMapping("/members/{memberId}/summary")
    public ResponseEntity<ContributionSummaryResponse> memberSummary(@PathVariable Long memberId) {
        return ResponseEntity.ok(contributionService.getSummary(memberId));
    }

    @GetMapping("/members/{memberId}/timeline")
    public ResponseEntity<List<ContributionResponse>> memberTimeline(@PathVariable Long memberId) {
        return ResponseEntity.ok(contributionService.getByMemberId(memberId));
    }

    @GetMapping("/pending-report")
    public ResponseEntity<PendingContributionReportResponse> pendingReport() {
        return ResponseEntity.ok(contributionService.getPendingReport());
    }

    @PostMapping
    public ResponseEntity<ContributionResponse> record(@Valid @RequestBody ContributionRequest request,
                                                        @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(contributionService.recordContribution(request, user.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContributionResponse> updateStatus(@PathVariable Long id,
                                                              @RequestParam ContributionStatus status,
                                                              @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(contributionService.updateStatus(id, status, user.getId()));
    }
}

@RestController
@RequestMapping("/admin/loans")
@RequiredArgsConstructor
@Tag(name = "Loans")
class LoanController {
    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LoanStatus status) {
        return ResponseEntity.ok(loanService.search(search, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoan(id));
    }

    @PostMapping
    public ResponseEntity<LoanResponse> issue(@Valid @RequestBody LoanRequest request,
                                               @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(loanService.issueLoan(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody LoanUpdateRequest request,
                                                @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(loanService.updateLoan(id, request, user.getId()));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<LoanResponse> close(@PathVariable Long id,
                                               @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(loanService.closeLoan(id, user.getId()));
    }

    @GetMapping("/{id}/principal-payments")
    public ResponseEntity<List<PaymentResponse>> principalPayments(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getPrincipalPayments(id));
    }

    @GetMapping("/{id}/interest-payments")
    public ResponseEntity<List<PaymentResponse>> interestPayments(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getInterestPayments(id));
    }
}

@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
class PaymentController {
    private final LoanService loanService;

    @PostMapping("/principal")
    public ResponseEntity<PaymentResponse> addPrincipal(@Valid @RequestBody PaymentRequest request,
                                                         @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(loanService.addPrincipalPayment(request, user.getId()));
    }

    @PostMapping("/interest")
    public ResponseEntity<PaymentResponse> addInterest(@Valid @RequestBody PaymentRequest request,
                                                        @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(loanService.addInterestPayment(request, user.getId()));
    }
}

@RestController
@RequestMapping("/admin/borrowers")
@RequiredArgsConstructor
@Tag(name = "External Borrowers")
class ExternalBorrowerController {
    private final ExternalBorrowerService borrowerService;

    @GetMapping
    public ResponseEntity<List<ExternalBorrowerResponse>> getAll(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(borrowerService.search(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalBorrowerService.ExternalBorrowerDetail> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(borrowerService.getDetails(id));
    }

    @PostMapping
    public ResponseEntity<ExternalBorrowerResponse> create(@Valid @RequestBody ExternalBorrowerRequest request,
                                                            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(borrowerService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalBorrowerResponse> update(@PathVariable Long id,
                                                            @Valid @RequestBody ExternalBorrowerRequest request,
                                                            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(borrowerService.update(id, request, user.getId()));
    }
}

@RestController
@RequestMapping("/admin/activity-logs")
@RequiredArgsConstructor
@Tag(name = "Activity Logs")
class ActivityLogController {
    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getAll() {
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }
}

@RestController
@RequestMapping("/admin/imports")
@RequiredArgsConstructor
@Tag(name = "Excel Data Import")
class ExcelImportController {
    private final ExcelImportService excelImportService;

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultResponse> importExcel(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(excelImportService.importMembers(file, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<ImportHistoryResponse>> history() {
        return ResponseEntity.ok(excelImportService.getHistory());
    }
}

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
class ReportController {
    private final ReportService reportService;

    @GetMapping("/members/excel")
    public ResponseEntity<byte[]> membersExcel() throws Exception {
        return excelResponse(reportService.exportMembersExcel(), "members-report.xlsx");
    }

    @GetMapping("/contributions/excel")
    public ResponseEntity<byte[]> contributionsExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws Exception {
        return excelResponse(reportService.exportContributionsExcel(from, to), "contributions-report.xlsx");
    }

    @GetMapping("/loans/excel")
    public ResponseEntity<byte[]> loansExcel() throws Exception {
        return excelResponse(reportService.exportLoansExcel(), "loans-report.xlsx");
    }

    @GetMapping("/fund/pdf")
    public ResponseEntity<byte[]> fundPdf() throws Exception {
        byte[] data = reportService.exportFundReportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fund-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    private ResponseEntity<byte[]> excelResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "Member Portal")
class MemberPortalController {
    private final MemberService memberService;
    private final ContributionService contributionService;
    private final LoanService loanService;

    @GetMapping("/profile")
    public ResponseEntity<MemberResponse> profile(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(memberService.getMember(user.getId()));
    }

    @GetMapping("/contributions")
    public ResponseEntity<List<ContributionResponse>> contributions(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(contributionService.getByMemberId(user.getId()));
    }

    @GetMapping("/contributions/summary")
    public ResponseEntity<ContributionSummaryResponse> contributionSummary(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(contributionService.getSummary(user.getId()));
    }

    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> loans(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(loanService.getLoansByBorrower(
                com.mfms.enums.BorrowerType.MEMBER, user.getId()));
    }
}
