package gettothepoint.unicatapi.application.service.ai;


public class PromptBuilder {

    public String scriptPrompt(String style, String content) {
        return switch (style) {
            case "funny" -> this.funnyPrompt(content);
            case "sad" -> this.sadPrompt(content);
            default -> this.defaultPrompt(content);
        };
    }

    public String funnyPrompt(String content) {
        return "Write a funny script about " + content + ".";
    }

    public String sadPrompt(String content) {
        return "Write a sad script about " + content + ".";
    }

    public String defaultPrompt(String content) {
        return "Write a script about " + content + ".";
    }

    public String imagePrompt(String style, String content) {
        return switch (style) {
            case "pixar" -> this.pixarPrompt(content);
            case "ghibli" -> this.ghibliPrompt(content);
            default -> this.defaultImagePrompt(content);
        };
    }

    public String pixarPrompt(String content) {
        return "Create a Pixar-style image of " + content + ".";
    }

    public String ghibliPrompt(String content) {
        return "Create a video of " + content + ".";
    }

    public String defaultImagePrompt(String content) {
        return "Create an image of " + content + ".";
    }
}
