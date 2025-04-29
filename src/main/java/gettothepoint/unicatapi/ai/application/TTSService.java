package gettothepoint.unicatapi.ai.application;

import java.io.File;

public interface TTSService {
    /**
     * 주어진 스크립트와 음성 모델로 음성 파일을 생성합니다.
     *
     * @param script 변환할 텍스트
     * @param voiceModel 사용할 음성 모델 (또는 voice_id)
     * @return 생성된 음성 파일
     */
    File create(String script, String voiceModel);
}