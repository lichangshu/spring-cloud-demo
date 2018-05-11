package wang.lcs.oauth;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class DemoController {

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

}
