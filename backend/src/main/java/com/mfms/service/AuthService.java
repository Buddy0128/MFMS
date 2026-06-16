package com.mfms.service;

import com.mfms.dto.AuthResponse;
import com.mfms.dto.AdminLoginRequest;
import com.mfms.dto.MemberLoginRequest;
import com.mfms.entity.Admin;
import com.mfms.entity.Member;
import com.mfms.enums.EntityStatus;
import com.mfms.enums.UserRole;
import com.mfms.exception.BusinessException;
import com.mfms.repository.AdminRepository;
import com.mfms.repository.MemberRepository;
import com.mfms.security.JwtTokenProvider;
import com.mfms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByPhoneNumberAndStatus(request.getPhoneNumber(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Invalid phone number or PIN", HttpStatus.UNAUTHORIZED));

        if (!admin.getPin().equals(request.getPin())) {
            throw new BusinessException("Invalid phone number or PIN", HttpStatus.UNAUTHORIZED);
        }

        UserPrincipal principal = new UserPrincipal(admin.getId(), admin.getPhoneNumber(),
                admin.getName(), UserRole.ADMIN, admin.getPin());
        String token = jwtTokenProvider.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .role(UserRole.ADMIN.name())
                .id(admin.getId())
                .name(admin.getName())
                .phoneNumber(admin.getPhoneNumber())
                .build();
    }

    public AuthResponse memberLogin(MemberLoginRequest request) {
        Member member = memberRepository.findByPhoneNumberAndStatus(request.getPhoneNumber(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Phone number not registered", HttpStatus.UNAUTHORIZED));

        UserPrincipal principal = new UserPrincipal(member.getId(), member.getPhoneNumber(),
                member.getFullName(), UserRole.MEMBER, null);
        String token = jwtTokenProvider.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .role(UserRole.MEMBER.name())
                .id(member.getId())
                .name(member.getFullName())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }
}
