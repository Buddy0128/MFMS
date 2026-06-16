package com.mfms.service;

import com.mfms.dto.ImportHistoryResponse;
import com.mfms.dto.ImportResultResponse;
import com.mfms.entity.*;
import com.mfms.enums.*;
import com.mfms.exception.BusinessException;
import com.mfms.repository.*;
import com.mfms.util.ContributionSchedule;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final InterestPaymentRepository interestPaymentRepository;
    private final PrincipalPaymentRepository principalPaymentRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final AdminRepository adminRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public ImportResultResponse importMembers(MultipartFile file, Long adminId) {
        validateFile(file);

        int processed = 0;
        int added = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();
        List<Member> existingMembers = memberRepository.findAll();
        Map<String, Member> membersByName = existingMembers.stream()
                .collect(Collectors.toMap(member -> normalizeName(member.getFullName()), member -> member,
                        (first, second) -> first, LinkedHashMap::new));
        Map<String, Member> membersByPhone = existingMembers.stream()
                .filter(member -> !member.getPhoneNumber().startsWith("IMP"))
                .collect(Collectors.toMap(Member::getPhoneNumber, member -> member,
                        (first, second) -> first, LinkedHashMap::new));
        Map<String, Member> membersByCode = existingMembers.stream()
                .collect(Collectors.toMap(member -> normalizeCode(member.getMemberCode()), member -> member,
                        (first, second) -> first, LinkedHashMap::new));
        Set<String> fileKeys = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter();
            HeaderColumns headers = findHeaders(sheet, formatter, evaluator);

            for (int rowIndex = headers.rowIndex() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter, evaluator)) continue;
                processed++;

                try {
                    String name = cellText(row.getCell(headers.name()), formatter, evaluator).trim();
                    if (name.isBlank()) throw new IllegalArgumentException("Member name is required");
                    String phone = normalizePhone(cellText(row.getCell(headers.phone()), formatter, evaluator));
                    String memberCode = normalizeCode(cellText(row.getCell(headers.memberCode()), formatter, evaluator));
                    if (!phone.isBlank() && !phone.matches("^[0-9]{10}$")) {
                        throw new IllegalArgumentException("Phone number must be 10 digits");
                    }

                    BigDecimal totalDeposit = money(row.getCell(headers.deposit()), formatter, evaluator, "Total Deposit");
                    BigDecimal currentLoan = money(row.getCell(headers.loan()), formatter, evaluator, "Current Loan Amount");
                    BigDecimal totalInterest = money(row.getCell(headers.interest()), formatter, evaluator, "Total Interest Paid");
                    if (totalDeposit.signum() < 0 || currentLoan.signum() < 0 || totalInterest.signum() < 0) {
                        throw new IllegalArgumentException("Amounts cannot be negative");
                    }

                    String duplicateKey = !memberCode.isBlank() ? "code:" + memberCode
                            : !phone.isBlank() ? "phone:" + phone
                            : "name:" + normalizeName(name);
                    if (!fileKeys.add(duplicateKey)) {
                        throw new IllegalArgumentException("Duplicate member row in this file");
                    }

                    String normalizedName = normalizeName(name);
                    Member member = !memberCode.isBlank() ? membersByCode.get(memberCode) : null;
                    if (member == null && !phone.isBlank()) member = membersByPhone.get(phone);
                    if (member == null) member = membersByName.get(normalizedName);
                    if (member == null) {
                        member = createImportedMember(name, memberCode, phone);
                        membersByName.put(normalizedName, member);
                        membersByCode.put(normalizeCode(member.getMemberCode()), member);
                        if (!member.getPhoneNumber().startsWith("IMP")) {
                            membersByPhone.put(member.getPhoneNumber(), member);
                        }
                        added++;
                    } else {
                        member.setFullName(name.trim());
                        if (!phone.isBlank() && !phone.equals(member.getPhoneNumber())) {
                            if (memberRepository.existsByPhoneNumber(phone)) {
                                throw new IllegalArgumentException("Phone number already belongs to another member");
                            }
                            member.setPhoneNumber(phone);
                            membersByPhone.put(phone, member);
                        }
                        updated++;
                    }

                    member.setTotalDeposit(totalDeposit);
                    member.setCurrentLoanAmount(currentLoan);
                    member.setTotalInterestPaid(totalInterest);
                    member.setImportedData(true);
                    member.setLastImportedAt(LocalDateTime.now());
                    member.setStatus(EntityStatus.ACTIVE);
                    memberRepository.save(member);

                    rebuildContributionTimeline(member, totalDeposit);
                    updateImportedLoan(member, currentLoan, totalInterest);
                } catch (Exception rowError) {
                    errors.add("Row " + (rowIndex + 1) + ": " + rowError.getMessage());
                }
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("Unable to read Excel file: " + exception.getMessage());
        }

        String importedBy = adminRepository.findById(adminId)
                .map(Admin::getName)
                .orElse("System");
        ImportHistory history = importHistoryRepository.save(ImportHistory.builder()
                .fileName(Optional.ofNullable(file.getOriginalFilename()).orElse("upload.xlsx"))
                .totalRows(processed)
                .newMembers(added)
                .updatedMembers(updated)
                .failedRecords(errors.size())
                .importedBy(importedBy)
                .build());

        activityLogService.log(adminId, "Imported Excel Data",
                history.getFileName() + ": " + processed + " rows, " + errors.size() + " failed");

        return ImportResultResponse.builder()
                .totalRowsProcessed(processed)
                .newMembersAdded(added)
                .existingMembersUpdated(updated)
                .failedRecords(errors.size())
                .importDateTime(history.getImportedAt())
                .errors(errors)
                .build();
    }

    public List<ImportHistoryResponse> getHistory() {
        return importHistoryRepository.findAllByOrderByImportedAtDesc().stream()
                .map(history -> ImportHistoryResponse.builder()
                        .id(history.getId())
                        .fileName(history.getFileName())
                        .totalRows(history.getTotalRows())
                        .newMembers(history.getNewMembers())
                        .updatedMembers(history.getUpdatedMembers())
                        .failedRecords(history.getFailedRecords())
                        .importedBy(history.getImportedBy())
                        .importedAt(history.getImportedAt())
                        .build())
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("Please select an Excel file");
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("Excel file is too large. Please upload a file under 10 MB.");
        }
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw new BusinessException("Only .xlsx and .xls files are supported");
        }
    }

    private HeaderColumns findHeaders(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int lastHeaderCandidate = Math.min(sheet.getLastRowNum(), 20);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= lastHeaderCandidate; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            Integer name = findColumn(row, formatter, evaluator,
                    value -> value.equals("member") || value.contains("member name"));
            Integer memberCode = findColumn(row, formatter, evaluator,
                    value -> value.equals("code") || value.contains("member code"));
            Integer phone = findColumn(row, formatter, evaluator,
                    value -> value.contains("phone") || value.contains("mobile"));
            Integer deposit = findColumn(row, formatter, evaluator, value -> value.contains("deposit"));
            Integer loan = findColumn(row, formatter, evaluator, value -> value.contains("loan"));
            Integer interest = findColumn(row, formatter, evaluator, value -> value.contains("interest"));
            if (name != null && deposit != null && loan != null && interest != null) {
                return new HeaderColumns(rowIndex, name, memberCode, phone, deposit, loan, interest);
            }
        }
        throw new BusinessException(
                "Required columns not found. Expected Member Name, Total Deposit, Current Loan Amount and Total Interest Paid.");
    }

    private Integer findColumn(Row row, DataFormatter formatter, FormulaEvaluator evaluator,
                               Predicate<String> matcher) {
        for (Cell cell : row) {
            String value = normalizeHeader(cellText(cell, formatter, evaluator));
            if (matcher.test(value)) return cell.getColumnIndex();
        }
        return null;
    }

    private boolean isBlankRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (Cell cell : row) {
            if (!cellText(cell, formatter, evaluator).isBlank()) return false;
        }
        return true;
    }

    private String cellText(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator);
    }

    private BigDecimal money(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator, String field) {
        String value = cellText(cell, formatter, evaluator).trim();
        if (value.isBlank()) return BigDecimal.ZERO;
        String cleaned = value.replaceAll("[₹,$\\s]", "");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(field + " must be a number");
        }
    }

    private Member createImportedMember(String name, String memberCode, String phoneNumber) {
        String code = memberCode.isBlank() ? nextMemberCode() : memberCode.toUpperCase(Locale.ROOT);
        if (memberRepository.existsByMemberCode(code)) {
            throw new IllegalArgumentException("Member code already belongs to another member");
        }
        String phone = phoneNumber.isBlank() ? nextPlaceholderPhone() : phoneNumber;
        return memberRepository.save(Member.builder()
                .memberCode(code)
                .fullName(name.trim())
                .phoneNumber(phone)
                .joinDate(ContributionSchedule.START_MONTH.atDay(1))
                .status(EntityStatus.ACTIVE)
                .importedData(true)
                .build());
    }

    private String nextMemberCode() {
        long number = memberRepository.count() + 1;
        String code;
        do {
            code = String.format("M%04d", number++);
        } while (memberRepository.existsByMemberCode(code));
        return code;
    }

    private String nextPlaceholderPhone() {
        long number = memberRepository.count() + 1;
        String phone;
        do {
            phone = String.format("IMP%09d", number++);
        } while (memberRepository.existsByPhoneNumber(phone));
        return phone;
    }

    private void rebuildContributionTimeline(Member member, BigDecimal totalDeposit) {
        int paidMonths = ContributionSchedule.paidMonths(totalDeposit);
        int expectedMonths = ContributionSchedule.expectedMonths();
        YearMonth month = ContributionSchedule.START_MONTH;

        for (int index = 0; index < expectedMonths; index++) {
            YearMonth period = month.plusMonths(index);
            boolean paid = index < paidMonths;
            Contribution contribution = contributionRepository
                    .findByMemberIdAndMonthAndYear(member.getId(), period.getMonthValue(), period.getYear())
                    .orElseGet(() -> Contribution.builder()
                            .member(member)
                            .month(period.getMonthValue())
                            .year(period.getYear())
                            .build());
            contribution.setAmount(ContributionSchedule.MONTHLY_AMOUNT);
            contribution.setStatus(paid ? ContributionStatus.PAID : ContributionStatus.PENDING);
            contribution.setPaymentDate(paid ? period.atEndOfMonth() : null);
            contributionRepository.save(contribution);
        }
    }

    private void updateImportedLoan(Member member, BigDecimal currentLoan, BigDecimal totalInterest) {
        Loan loan = loanRepository
                .findByBorrowerTypeAndBorrowerIdAndImportedBalance(BorrowerType.MEMBER, member.getId(), true)
                .orElseGet(() -> Loan.builder()
                        .borrowerType(BorrowerType.MEMBER)
                        .borrowerId(member.getId())
                        .interestRate(new BigDecimal("1.00"))
                        .loanDate(ContributionSchedule.START_MONTH.atDay(1))
                        .importedBalance(true)
                        .build());
        loan.setLoanAmount(currentLoan);
        loan.setOutstandingAmount(currentLoan);
        loan.setStatus(currentLoan.signum() > 0 ? LoanStatus.ACTIVE : LoanStatus.CLOSED);
        loan = loanRepository.save(loan);

        interestPaymentRepository.deleteByLoanId(loan.getId());
        principalPaymentRepository.deleteByLoanId(loan.getId());
        if (totalInterest.signum() > 0) {
            interestPaymentRepository.save(InterestPayment.builder()
                    .loan(loan)
                    .amount(totalInterest)
                    .paymentDate(LocalDate.now())
                    .build());
        }
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private record HeaderColumns(int rowIndex, int name, Integer memberCode, Integer phone,
                                 int deposit, int loan, int interest) {
    }
}
