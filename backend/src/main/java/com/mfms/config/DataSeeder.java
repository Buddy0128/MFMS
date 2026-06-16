package com.mfms.config;

import com.mfms.entity.*;
import com.mfms.enums.*;
import com.mfms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import com.mfms.util.ContributionSchedule;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final ExternalBorrowerRepository borrowerRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final PrincipalPaymentRepository principalPaymentRepository;
    private final InterestPaymentRepository interestPaymentRepository;

    @Value("${mfms.seed-demo-data:false}")
    private boolean seedDemoData;

    private static final String[] MEMBER_NAMES = {
            "Ramesh Patel", "Suresh Shah", "Mahesh Desai", "Dinesh Mehta", "Rajesh Kumar",
            "Nilesh Joshi", "Hitesh Rao", "Mukesh Singh", "Ganesh Iyer", "Yogesh Nair",
            "Prakash Reddy", "Vikash Gupta", "Ashok Sharma", "Deepak Verma", "Sanjay Tiwari",
            "Anil Kapoor", "Sunil Khanna", "Manoj Agarwal", "Vijay Malhotra", "Rahul Saxena"
    };

    @Override
    @Transactional
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            seedAdmins();
        }
        if (!seedDemoData || memberRepository.count() > 0) {
            log.info("Demo financial data seeding disabled or data already exists.");
            return;
        }
        log.info("Seeding MFMS demo financial data...");
        seedMembers();
        seedBorrowers();
        seedContributions();
        seedLoans();
        log.info("Database seeding completed.");
    }

    private void seedAdmins() {
        adminRepository.save(Admin.builder().name("Admin One").phoneNumber("9999999991").pin("1234").status(EntityStatus.ACTIVE).build());
        adminRepository.save(Admin.builder().name("Admin Two").phoneNumber("9999999992").pin("5678").status(EntityStatus.ACTIVE).build());
    }

    private void seedMembers() {
        for (int i = 0; i < MEMBER_NAMES.length; i++) {
            BigDecimal deposit = i == 0 ? new BigDecimal("30000") : new BigDecimal("29000");
            memberRepository.save(Member.builder()
                    .memberCode(String.format("M%03d", i + 1))
                    .fullName(MEMBER_NAMES[i])
                    .phoneNumber(String.format("9876543%03d", i + 1))
                    .joinDate(ContributionSchedule.START_MONTH.atDay(1))
                    .status(EntityStatus.ACTIVE)
                    .totalDeposit(deposit)
                    .build());
        }
    }

    private void seedBorrowers() {
        String[] names = {"External A", "External B", "External C", "External D", "External E"};
        for (int i = 0; i < names.length; i++) {
            borrowerRepository.save(ExternalBorrower.builder()
                    .fullName(names[i])
                    .phoneNumber(String.format("8765432%03d", i + 1))
                    .address("Address " + (i + 1) + ", City")
                    .status(EntityStatus.ACTIVE)
                    .build());
        }
    }

    private void seedContributions() {
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            int paidMonths = ContributionSchedule.paidMonths(member.getTotalDeposit());
            List<YearMonth> periods = ContributionSchedule.expectedPeriods();
            for (int index = 0; index < periods.size(); index++) {
                YearMonth period = periods.get(index);
                boolean paid = index < paidMonths;
                contributionRepository.save(Contribution.builder()
                        .member(member)
                        .month(period.getMonthValue())
                        .year(period.getYear())
                        .amount(ContributionSchedule.MONTHLY_AMOUNT)
                        .status(paid ? ContributionStatus.PAID : ContributionStatus.PENDING)
                        .paymentDate(paid ? period.atEndOfMonth() : null)
                        .build());
            }
        }
    }

    private void seedLoans() {
        List<Member> members = memberRepository.findAll();
        List<ExternalBorrower> borrowers = borrowerRepository.findAll();

        // 5 member loans
        for (int i = 0; i < 5; i++) {
            Member member = members.get(i);
            Loan loan = loanRepository.save(Loan.builder()
                    .borrowerType(BorrowerType.MEMBER)
                    .borrowerId(member.getId())
                    .loanAmount(new BigDecimal(String.valueOf(10000 + i * 5000)))
                    .outstandingAmount(new BigDecimal(String.valueOf(10000 + i * 5000)))
                    .interestRate(new BigDecimal("1.00"))
                    .loanDate(LocalDate.now().minusMonths(3))
                    .status(LoanStatus.ACTIVE)
                    .build());

            if (i < 2) {
                PrincipalPayment pp = principalPaymentRepository.save(PrincipalPayment.builder()
                        .loan(loan).amount(new BigDecimal("5000"))
                        .paymentDate(LocalDate.now().minusMonths(1)).build());
                loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(pp.getAmount()));
                loanRepository.save(loan);
            }

            interestPaymentRepository.save(InterestPayment.builder()
                    .loan(loan).amount(new BigDecimal("100"))
                    .paymentDate(LocalDate.now().minusMonths(1)).build());
        }

        // 5 external loans
        for (int i = 0; i < 5; i++) {
            ExternalBorrower borrower = borrowers.get(i);
            Loan loan = loanRepository.save(Loan.builder()
                    .borrowerType(BorrowerType.EXTERNAL)
                    .borrowerId(borrower.getId())
                    .loanAmount(new BigDecimal(String.valueOf(20000 + i * 10000)))
                    .outstandingAmount(new BigDecimal(String.valueOf(20000 + i * 10000)))
                    .interestRate(new BigDecimal("5.00"))
                    .loanDate(LocalDate.now().minusMonths(2))
                    .status(LoanStatus.ACTIVE)
                    .build());

            interestPaymentRepository.save(InterestPayment.builder()
                    .loan(loan).amount(new BigDecimal("500"))
                    .paymentDate(LocalDate.now().minusMonths(1)).build());
        }
    }
}
