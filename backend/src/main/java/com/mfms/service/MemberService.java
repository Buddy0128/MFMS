package com.mfms.service;

import com.mfms.dto.LoanResponse;
import com.mfms.dto.MemberDetailResponse;
import com.mfms.dto.MemberRequest;
import com.mfms.dto.MemberResponse;
import com.mfms.dto.PaymentResponse;
import com.mfms.entity.Member;
import com.mfms.enums.BorrowerType;
import com.mfms.enums.EntityStatus;
import com.mfms.enums.LoanStatus;
import com.mfms.exception.BusinessException;
import com.mfms.exception.ResourceNotFoundException;
import com.mfms.mapper.EntityMapper;
import com.mfms.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final EntityMapper mapper;
    private final LoanService loanService;
    private final ContributionService contributionService;
    private final ActivityLogService activityLogService;

    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream().map(mapper::toMemberResponse).toList();
    }

    public List<MemberResponse> searchMembers(String query) {
        if (query == null || query.isBlank()) return getAllMembers();
        return memberRepository.search(query).stream().map(mapper::toMemberResponse).toList();
    }

    public MemberResponse getMember(Long id) {
        return mapper.toMemberResponse(findMember(id));
    }

    public MemberDetailResponse getMemberDetails(Long id) {
        Member member = findMember(id);
        List<LoanResponse> loans = loanService.getLoansByBorrower(BorrowerType.MEMBER, id);
        BigDecimal outstanding = member.isImportedData()
                ? member.getCurrentLoanAmount()
                : loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(LoanResponse::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingInterest = loans.stream()
                .map(LoanResponse::getPendingInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestPaid = member.isImportedData()
                ? member.getTotalInterestPaid()
                : loans.stream()
                .map(LoanResponse::getTotalInterestPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaymentResponse> interestPayments = loans.stream()
                .flatMap(l -> loanService.getInterestPayments(l.getId()).stream())
                .toList();

        return MemberDetailResponse.builder()
                .member(mapper.toMemberResponse(member))
                .contributionSummary(contributionService.getSummary(id))
                .outstandingAmount(outstanding)
                .pendingInterest(pendingInterest)
                .totalInterestPaid(totalInterestPaid)
                .contributions(contributionService.getByMemberId(id))
                .loans(loans)
                .interestPayments(interestPayments)
                .build();
    }

    @Transactional
    public MemberResponse createMember(MemberRequest request, Long adminId) {
        if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Phone number already registered");
        }
        String memberCode = generateMemberCode();
        Member member = Member.builder()
                .memberCode(memberCode)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .joinDate(request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now())
                .status(EntityStatus.ACTIVE)
                .totalDeposit(request.getTotalDeposit() != null ? request.getTotalDeposit() : BigDecimal.ZERO)
                .build();
        member = memberRepository.save(member);
        contributionService.rebuildTimeline(member);
        activityLogService.log(adminId, "Created Member",
                "Member: " + member.getFullName() + " (" + member.getMemberCode() + ")");
        return mapper.toMemberResponse(member);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest request, Long adminId) {
        Member member = findMember(id);
        if (!member.getPhoneNumber().equals(request.getPhoneNumber())
                && memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Phone number already registered");
        }
        member.setFullName(request.getFullName());
        member.setPhoneNumber(request.getPhoneNumber());
        if (request.getJoinDate() != null) member.setJoinDate(request.getJoinDate());
        if (request.getStatus() != null) member.setStatus(request.getStatus());
        if (request.getTotalDeposit() != null) {
            if (request.getTotalDeposit().signum() < 0) {
                throw new BusinessException("Total Deposit cannot be negative");
            }
            member.setTotalDeposit(request.getTotalDeposit());
        }
        member = memberRepository.save(member);
        contributionService.rebuildTimeline(member);
        activityLogService.log(adminId, "Updated Member", "Member: " + member.getFullName());
        return mapper.toMemberResponse(member);
    }

    @Transactional
    public MemberResponse disableMember(Long id, Long adminId) {
        Member member = findMember(id);
        member.setStatus(EntityStatus.INACTIVE);
        member = memberRepository.save(member);
        activityLogService.log(adminId, "Disabled Member", "Member: " + member.getFullName());
        return mapper.toMemberResponse(member);
    }

    Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    private String generateMemberCode() {
        long count = memberRepository.count() + 1;
        return String.format("M%03d", count);
    }
}
