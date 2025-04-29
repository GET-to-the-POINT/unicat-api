package gettothepoint.unicatapi.common.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;

@ControllerAdvice
public class GlobalBinderConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setIgnoreInvalidFields(true);

        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                // 빈 문자열 들어올 경우 null 처리
                setValue(null);
            }
        });
    }
}