package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.ai.domain.dto.AIGenerate;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIServiceDummy implements OpenAIService {

    @Override
    public AIGenerate create(PromptRequest promptRequest) {
        List<String> scripts = List.of(
            "더미 데이터 - 고양이의 하루",
            "고양이는 오늘 아침 작은 모험을 시작했습니다.",
            "그녀는 창밖으로 뛰어내려 정원으로 향했습니다.",
            "잎 사이로 숨어 있는 나비를 따라 뛰놀았습니다.",
            "고양이는 담장을 넘어 옆집 강아지와 인사를 나눴습니다.",
            "모래밭에서 뒹굴며 햇살을 즐겼습니다.",
            "나무 그늘 아래서 잠시 휴식을 취했습니다.",
            "길 잃은 새끼 고양이 한 마리를 발견했습니다.",
            "함께 집으로 돌아가는 길에 달빛이 비쳤습니다.",
            "오늘 하루는 작은 모험과 따뜻한 기억으로 가득했습니다."
        );
        return new AIGenerate(scripts);
    }
}
