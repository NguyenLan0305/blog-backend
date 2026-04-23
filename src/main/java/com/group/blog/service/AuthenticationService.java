package com.group.blog.service;

import com.group.blog.dto.request.AuthenticationRequest;
import com.group.blog.dto.request.IntrospectRequest;
import com.group.blog.dto.response.AuthenticationResponse;
import com.group.blog.dto.response.IntrospectResponse;
import com.group.blog.entity.User;
import com.group.blog.exception.AppException;
import com.group.blog.exception.ErrorCode;
import com.group.blog.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Date expiryTime = new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli());
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.PASSWORD_LOGIN_INCORRECT);
        var token = generateToken(user, expiryTime);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .expiryTime(expiryTime)
                .build();
    }

    private String generateToken(User user, Date expiryTime) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("group.com")
                .issueTime(new Date())
                .expirationTime(expiryTime)
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            // Duyệt qua tập hợp các đối tượng UserRole
            // Lấy ra giá trị chuỗi (role) và nối lại
            user.getRoles().forEach(userRole -> {
                stringJoiner.add(userRole.getRole());
            });
        }
        return stringJoiner.toString();
    }


    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = false;
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            var verified = signedJWT.verify(verifier);
            isValid = verified && expiryTime != null && expiryTime.after(new Date());
        } catch (ParseException | JOSEException | IllegalArgumentException e) {
            // Nếu token sai định dạng (bị thiếu dấu chấm, chữ ký bị chỉnh sửa...)
            // Không văng lỗi 500, chỉ log lại và mặc định isValid vẫn là false
            log.warn("Token invalid: {}", e.getMessage());
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();


    }
}
