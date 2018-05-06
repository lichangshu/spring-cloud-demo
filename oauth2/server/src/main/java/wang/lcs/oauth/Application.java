package wang.lcs.oauth;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
@EnableAuthorizationServer
@Configuration
public class Application extends SpringBootServletInitializer {

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
            http.csrf().disable();
            http//
                    .requestMatchers().antMatchers("/oauth/**", "/login/**", "/logout/**")//
                    .and()//
                    .authorizeRequests()//
                    .antMatchers("/oauth/**").authenticated()//
                    .and()//
                    .formLogin().permitAll();
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Bean
        public TokenStore tokenStore() {
            return new InMemoryTokenStore();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().withUser("hello").password("hello").roles("ADMIN");
        }
    }

    @Component
    public static class AuthorizationServer extends AuthorizationServerConfigurerAdapter {
        @Autowired
        private TokenStore tokenStore;
        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory().withClient("client").secret("password")//
                    .authorizedGrantTypes("authorization_code").scopes("app");
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore(this.tokenStore).authenticationManager(this.authenticationManager);
        }
    }
}
