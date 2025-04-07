package gettothepoint.unicatapi.infrastructure.config;

import gettothepoint.unicatapi.common.propertie.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        S3Client client = S3Client.builder()
                .region(Region.of(s3Properties.region()))
                .endpointOverride(URI.create(s3Properties.endpoint()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Properties.accessKeyId(), s3Properties.secretAccessKey())))
                .serviceConfiguration(S3Configuration.builder().checksumValidationEnabled(false).build())
                .build();

        try {
            client.headBucket(b -> b.bucket(s3Properties.bucket()));
        } catch (NoSuchBucketException e) {
            client.createBucket(b -> b.bucket(s3Properties.bucket()));
        } catch (S3Exception e) {
            if (!e.awsErrorDetails().errorCode().equals("BucketAlreadyOwnedByYou")) {
                throw e;
            }
        }

        return client;
    }
}