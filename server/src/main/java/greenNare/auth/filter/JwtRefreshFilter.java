package greenNare.auth.filter;

import greenNare.auth.jwt.JwtTokenizer;
import greenNare.cache.CacheService;
import greenNare.member.entity.Member;
import greenNare.member.repository.MemberRepository;
import greenNare.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtRefreshFilter extends OncePerRequestFilter {

    private final JwtTokenizer jwtTokenizer;

    private final MemberService memberService;

    private final CacheService cacheService;

    public JwtRefreshFilter(JwtTokenizer jwtTokenizer, MemberService memberService, CacheService cacheService){
        this.jwtTokenizer = jwtTokenizer;
        this.memberService = memberService;
        this.cacheService = cacheService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().startsWith("/user/refresh")){

            //String refreshToken = request.getParameter("RefreshToken");
            String refreshToken = request.getHeader("RefreshToken");
            System.out.println("refresh: "+refreshToken);

            if((refreshToken != null) && validateRefreshToken(refreshToken)){
                Member member = memberService.findMemberByEmail(jwtTokenizer.getClaims(refreshToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey())).getBody().getSubject());

                String newAccessToken = reCreateAccessToken(member);
//                String newRefreshToken = reCreateRefreshToken(member);

                response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            else{
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }

        }
        filterChain.doFilter(request, response);

    }

    public boolean validateRefreshToken(String token){
        try {
            jwtTokenizer.getClaims(token, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));
            Claims claims = jwtTokenizer.getClaims(token, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey())).getBody();

            String cachedRefreshToken = cacheService.getCache("RefreshToken", claims.getSubject(), String.class);
            Claims cachedClaims = jwtTokenizer.getClaims(cachedRefreshToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey())).getBody();

            //캐시에 저장된 토큰과 비교
            if(!claims.equals(cachedClaims)) {
                System.out.println("claims != cached");
                System.out.println("claims: " + claims);
                System.out.println("cached: " + cachedClaims);
                return false;
            }
            //만료시간 확인
            if(cachedClaims.getExpiration().before(new Date())) {
                System.out.println("expired RefreshToken");
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.info("error: " + e);
            return false;
        }

    }

    private String reCreateAccessToken(Member member){

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

    private String reCreateRefreshToken(Member member){
        String subject = member.getEmail();

        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);
        cacheService.putCache("RefreshToken", Integer.toString(member.getMemberId()) ,refreshToken);

        return refreshToken;
    }

}
