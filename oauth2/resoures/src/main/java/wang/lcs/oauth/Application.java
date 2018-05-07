package wang.lcs.oauth;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
@EnableResourceServer
@Configuration
public class Application extends SpringBootServletInitializer {

    public String SPARKLR_RESOURCE_ID = "app";

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        // 支持 http://username:password@demo.com/foo/ 的格式
        return new RestTemplate() {
            @Override
            protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
                return super.doExecute(url, method, request -> {
                    requestCallback.doWithRequest(request);
                    String auth = url.getUserInfo();
                    if (auth != null) {
                        HttpHeaders headers = request.getHeaders();
                        List<String> auths = headers.get("Authorization");
                        if (auths == null) {
                            String encode = Base64.getEncoder().encodeToString(auth.getBytes(Charset.forName("US-ASCII")));
                            headers.add("Authorization", "Basic " + encode);
                        }
                    }

                }, responseExtractor);
            }
        };
    }

    @Configuration
    @EnableWebSecurity
    public static class OAuth2SecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/rpc/**").authorizeRequests()//
                    .anyRequest().authenticated()//
                    .and().httpBasic()//
                    .and().csrf().disable();
        }
    }

    @Configuration
    public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/i/**").authorizeRequests()//
                    .antMatchers("/i/**").access("#oauth2.hasScope('read') or (!#oauth2.isOAuth() and hasRole('ROLE_USER'))");
        }

    }
}
