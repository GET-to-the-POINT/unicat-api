package gettothepoint.unicatapi.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties (String[] allowedOrigins, String[] allowedMethods, String[] allowedHeaders, boolean allowCredentials, long maxAge) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CorsProperties(
                String[] origins, String[] methods, String[] headers, boolean credentials, long age
        ))) return false;
        return allowCredentials == credentials &&
                maxAge == age &&
                java.util.Arrays.equals(allowedOrigins, origins) &&
                java.util.Arrays.equals(allowedMethods, methods) &&
                java.util.Arrays.equals(allowedHeaders, headers);
    }

    @Override
    public int hashCode() {
        int result = java.util.Arrays.hashCode(allowedOrigins);
        result = 31 * result + java.util.Arrays.hashCode(allowedMethods);
        result = 31 * result + java.util.Arrays.hashCode(allowedHeaders);
        result = 31 * result + Boolean.hashCode(allowCredentials);
        result = 31 * result + Long.hashCode(maxAge);
        return result;
    }

    @Override
    public String toString() {
        return "Cors{" +
                "allowedOrigins=" + java.util.Arrays.toString(allowedOrigins) +
                ", allowedMethods=" + java.util.Arrays.toString(allowedMethods) +
                ", allowedHeaders=" + java.util.Arrays.toString(allowedHeaders) +
                ", allowCredentials=" + allowCredentials +
                ", maxAge=" + maxAge +
                '}';
    }
}
