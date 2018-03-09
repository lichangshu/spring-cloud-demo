package wang.lcs.serv;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class AuthController {

    @Resource
    RestTemplate rest;

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
        return "reqest index in [" + ipadd + "]:" + port;
    }

    @RequestMapping("/curl")
    @ResponseBody
    public String loginOk() {
        return rest.getForObject("http://ICO-DEMO/index.html", String.class);
    }
}
