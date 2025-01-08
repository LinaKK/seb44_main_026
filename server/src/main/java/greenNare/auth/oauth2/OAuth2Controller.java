package greenNare.auth.oauth2;

import org.springframework.beans.factory.annotation.Value;
import greenNare.Response.SingleResponseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.clientId}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private List<String> scope;

    private String scopeString = String.join(" ", scope);

    private final String endPoint = "https://accounts.google.com/o/oauth2/auth";

    @GetMapping("/google")
    public ResponseEntity getGoogleOAuth2EndPoint(){
        String authUrl = endPoint +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scopeString +
                "&access_type=offline";

        SingleResponseDto responseDto = new SingleResponseDto(authUrl);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

}
