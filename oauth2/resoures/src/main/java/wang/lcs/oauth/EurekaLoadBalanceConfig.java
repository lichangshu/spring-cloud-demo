package wang.lcs.oauth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@Configuration
@ConditionalOnExpression("${eureka.client.enabled:true}")
public class EurekaLoadBalanceConfig {

    @Bean
    @LoadBalanced
    public OAuth2RestTemplate loadBalanceOauth2RestTemplate(OAuth2ClientContext oauth2ClientContext, ResourceOwnerPasswordResourceDetails resource) {
        return new OAuth2RestTemplate(resource, oauth2ClientContext);
    }

    @Bean
    @ConditionalOnBean(UserInfoTokenServices.class)
    public ApplicationListener<ApplicationReadyEvent> loadBalanceUserInfoTokenServices(UserInfoTokenServices service, //
            @Qualifier("loadBalanceOauth2RestTemplate") OAuth2RestTemplate oauth2RestTemplate) {
        return (ApplicationReadyEvent e) -> {
            service.setRestTemplate(oauth2RestTemplate);
        };
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> loadBalanceRemoteTokenServices(RemoteTokenServices service, //
            @Qualifier("loadBalanceOauth2RestTemplate") OAuth2RestTemplate oauth2RestTemplate) {
        return (ApplicationReadyEvent e) -> {
            service.setRestTemplate(oauth2RestTemplate);
        };
    }

}