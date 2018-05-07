package wang.lcs.oauth;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class DemoController {

    @Resource
    RestTemplate restTemplate;

    @RequestMapping("/login")
    public String login() {
        return "/login";
    }

    @Value("${server.port}")
    private int port;

    @Value("${spring.cloud.client.ipAddress}")
    private String ipadd;

    @RequestMapping(value = { "/", "/index", "/index.html" })
    @ResponseBody
    public String index() {
        return "request index in [" + ipadd + "]:" + port;
    }

    @RequestMapping(value = { "/i/info.html", "/rpc/info.html" })
    @ResponseBody
    public String curl(HttpServletRequest request) {
        return "Your request uri : " + request.getRequestURI();
    }

}
