package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.toss")
public record TossProperties (String clientKey, String secretKey,String approveUrl,String billingUrl){
}
