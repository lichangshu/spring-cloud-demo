package wang.lcs.oauth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnExpression("${eureka.client.enabled:true}")
public class EurekaLoadBalanceConfig {

    /**
     * 实现负载均衡的 RestTemplate 里的 方法(Copy 自 RemoteTokenServices 的代码)
     * 
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate tokenLoadBalance() {
        RestTemplate rt = new RestTemplate();
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            // Ignore 400
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400) {
                    super.handleError(response);
                }
            }
        });
        return rt;
    }

    /**
     * RemoteTokenServices 修改为 ribbon 的负载均衡, 覆盖 restTemplate 属性, 实现负载均衡
     * 
     * @param remoteTokenServices
     * @param tokenLoadBalance
     * @return
     */
    @Bean
    public RemoteTokenServices remoteTokenServicesUpdateRestTemplate(@Qualifier("tokenLoadBalance") RestTemplate tokenLoadBalance) {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setRestTemplate(tokenLoadBalance);
        return remoteTokenServices;
    }

    //ApplicationListener Event listener
    @Bean
    public ApplicationListener<ApplicationReadyEvent> oauth2RestTemplateUpdateAccessTokenProvider() {
        return (ApplicationReadyEvent e) -> {
            OAuth2RestTemplate oauth = e.getApplicationContext().getBean(OAuth2RestTemplate.class);
            RestTemplate loadBalance = e.getApplicationContext().getBean(RestTemplate.class);
            oauth.setAccessTokenProvider(new ResourceOwnerPasswordAccessTokenProvider() {
                @Override
                protected RestOperations getRestTemplate() {
                    setMessageConverters(loadBalance.getMessageConverters());
                    return loadBalance;
                }
            });
        };
    }
}