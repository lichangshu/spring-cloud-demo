package wang.lcs.oauth;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class DemoController {

    @Resource
    OAuth2RestTemplate auth2RestTemplate;
    @Resource
    RestTemplate restTemplate;

    @Value("${server.port}")
    private int port;

    @Value("${spring.cloud.client.ipAddress}")
    private String ipadd;

    @RequestMapping(value = { "/", "/index", "/index.html" })
    public String index() {
        return "request index in [" + ipadd + "]:" + port;
    }

    @RequestMapping(value = { "/i/info.html", "/rpc/info.html" })
    public String curl(HttpServletRequest request) {
        return "Your request uri : " + request.getRequestURI();
    }

    @RequestMapping(value = { "/login" })
    public OAuth2AccessToken getAccessToken(String username, String password) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, password));
        auth2RestTemplate.getOAuth2ClientContext().setAccessToken(null);
        try {
            return auth2RestTemplate.getAccessToken();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof InvalidGrantException) {
                throw new IllegalAccessError("User name or password error !");
            }
            throw e;
        }
    }
}
