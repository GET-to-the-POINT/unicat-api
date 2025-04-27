package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.domain.storage.FileNameTransformer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class DefaultFileNameTransformer implements FileNameTransformer {

    @Override
    public String transform(String filename, InputStream content) {
        try {
            if (!content.markSupported()) {
                throw new IllegalArgumentException("InputStream은 mark/reset을 지원해야 합니다");
            }
            content.mark(Integer.MAX_VALUE);
            String hash = sha256(content);
            content.reset();

            int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
            String dirPart = lastSlash >= 0 ? filename.substring(0, lastSlash + 1) : "";
            String namePart = lastSlash >= 0 ? filename.substring(lastSlash + 1) : filename;

            int lastDot = namePart.lastIndexOf('.');
            boolean hasExt = lastDot > 0 && lastDot < namePart.length() - 1;

            String baseName = hasExt ? namePart.substring(0, lastDot) : namePart;
            String ext = hasExt ? namePart.substring(lastDot) : "";

            return dirPart + baseName + "." + hash + ext;
        } catch (IOException e) {
            throw new RuntimeException("파일명 변환 중 오류 발생", e);
        }
    }

    private String sha256(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 해시 생성 실패", e);
        }
    }
}