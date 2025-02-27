//package taeniverse.unicatApi.mvc.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import taeniverse.unicatApi.mvc.service.YouTubeAnalyticsService;
//
//@RestController
//@RequestMapping("/youtube/analytics")
//public class YouTubeAnalyticsController {
//
//    private final YouTubeAnalyticsService youtubeAnalyticsService;
//
//    public YouTubeAnalyticsController(YouTubeAnalyticsService youtubeAnalyticsService) {
//        this.youtubeAnalyticsService = youtubeAnalyticsService;
//    }
//
//    // 클라이언트가 요청하면 YouTube API를 호출하여 다양한 메트릭을 반환
//    @GetMapping("/advanced-stats")
//    public String getAdvancedVideoAnalytics(@RequestParam String startDate, @RequestParam String endDate) {
//        try {
//            // 서비스 메서드를 호출하여 여러 메트릭 데이터를 가져오기
//            return youtubeAnalyticsService.getAdvancedVideoAnalytics(startDate, endDate);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error retrieving data";  // 에러 발생시 반환
//        }
//    }
//}
