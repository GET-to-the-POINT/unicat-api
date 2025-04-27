package gettothepoint.unicatapi.filestorage.domain.model;

import java.io.InputStream;

public record StoredFile(
    String filename,       // 저장될 파일명 (경로 포함)
    InputStream content,    // 파일 내용
    long size,              // 파일 크기 (바이트)
    String contentType      // 콘텐츠 타입 (MIME 타입)
) {}