package greenNare.auth.handler;

import greenNare.auth.jwt.JwtTokenizer;
import greenNare.auth.utils.CustomAuthorityUtils;
import greenNare.cache.CacheService;
import greenNare.member.entity.Member;
import greenNare.member.service.MemberService;
import org.springframework.cache.Cache;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class OAuth2MemberSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils customAuthorityUtils;
    private final MemberService memberService;

    private final CacheService cacheService;

    public OAuth2MemberSuccessHandler(JwtTokenizer jwtTokenizer,
                                      CustomAuthorityUtils customAuthorityUtils,
                                      MemberService memberService,
                                      CacheService cacheService){
        this.jwtTokenizer = jwtTokenizer;
        this.customAuthorityUtils = customAuthorityUtils;
        this.memberService = memberService;
        this.cacheService = cacheService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var oAuth2User = (OAuth2User)authentication.getPrincipal();
        String email = String.valueOf(oAuth2User.getAttributes().get("email"));

        //가입된 회원인지 확인
        try {
            memberService.findMemberByEmail(email);
        } catch (Exception e){
            //가입안된 이메일이면 DB 저장
            String name = String.valueOf(oAuth2User.getAttributes().get("name"));
            Member newMember = new Member();
            newMember.setEmail(email);
            newMember.setName(name);
            newMember.setPassword(UUID.randomUUID().toString()+"!");
            newMember.setPoint(0);
            memberService.createMember(newMember);
        }

        //토큰 생성해서 반환
        Member member = memberService.findMemberByEmail(email);
        String accessToken = delegateAccessToken(member);
        String refreshToken = delegateRefreshToken(member);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);
        response.setStatus(HttpServletResponse.SC_OK);

        return;
    }

    private String delegateAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", member.getEmail());
        claims.put("roles", member.getRoles());
        claims.put("memberId", member.getMemberId());

        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;

    }

    private String delegateRefreshToken(Member member){
        String subject = member.getEmail();

        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);
        cacheService.putCache("RefreshToken", Integer.toString(member.getMemberId()) ,refreshToken);

        return refreshToken;
    }

}
