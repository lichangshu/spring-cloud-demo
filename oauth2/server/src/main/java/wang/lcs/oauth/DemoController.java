package wang.lcs.oauth;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class DemoController {

    @Resource
    RestTemplate restTemplate;

    @Value("${server.port}")
    private int port;

    @Value("${spring.cloud.client.ipAddress}")
    private String ipadd;

    @RequestMapping(value = { "/", "/index", "/index.html", "/rpc/index.html" })
    @ResponseBody
    public String index() {
        return String.format("request index in %s:%s", ipadd, port);
    }

    @RequestMapping("/curl")
    @ResponseBody
    public String curl() {
        return restTemplate.getForObject("http://serv/index.html", String.class);
    }
}
