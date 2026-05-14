package org.example.agent.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class skill_whether {
    
    @Value("${weather.api-key:}")
    private String weatherApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final Map<String, String> CITY_ADCODE_MAP = new java.util.HashMap<>();
    private static final Map<String, String> CITY_NAME_ALIASES = new java.util.HashMap<>();
    
    static {
        CITY_ADCODE_MAP.put("北京", "110000");
        CITY_ADCODE_MAP.put("上海", "310000");
        CITY_ADCODE_MAP.put("广州", "440100");
        CITY_ADCODE_MAP.put("深圳", "440300");
        CITY_ADCODE_MAP.put("重庆", "500000");
        CITY_ADCODE_MAP.put("成都", "510100");
        CITY_ADCODE_MAP.put("杭州", "330100");
        CITY_ADCODE_MAP.put("南京", "320100");
        CITY_ADCODE_MAP.put("武汉", "420100");
        CITY_ADCODE_MAP.put("西安", "610100");
        CITY_ADCODE_MAP.put("天津", "120000");
        CITY_ADCODE_MAP.put("苏州", "320500");
        CITY_ADCODE_MAP.put("青岛", "370200");
        CITY_ADCODE_MAP.put("大连", "210200");
        CITY_ADCODE_MAP.put("郑州", "410100");
        CITY_ADCODE_MAP.put("长沙", "430100");
        CITY_ADCODE_MAP.put("济南", "370100");
        CITY_ADCODE_MAP.put("合肥", "340100");
        CITY_ADCODE_MAP.put("福州", "350100");
        CITY_ADCODE_MAP.put("昆明", "530100");
        CITY_ADCODE_MAP.put("贵阳", "520100");
        CITY_ADCODE_MAP.put("南宁", "450100");
        CITY_ADCODE_MAP.put("南昌", "360100");
        CITY_ADCODE_MAP.put("太原", "140100");
        CITY_ADCODE_MAP.put("石家庄", "130100");
        CITY_ADCODE_MAP.put("哈尔滨", "230100");
        CITY_ADCODE_MAP.put("长春", "220100");
        CITY_ADCODE_MAP.put("沈阳", "210100");
        CITY_ADCODE_MAP.put("乌鲁木齐", "650100");
        CITY_ADCODE_MAP.put("兰州", "620100");
        CITY_ADCODE_MAP.put("银川", "640100");
        CITY_ADCODE_MAP.put("西宁", "630100");
        CITY_ADCODE_MAP.put("拉萨", "540100");
        CITY_ADCODE_MAP.put("海口", "460100");
        CITY_ADCODE_MAP.put("三亚", "460200");
        CITY_ADCODE_MAP.put("厦门", "350200");
        CITY_ADCODE_MAP.put("宁波", "330200");
        CITY_ADCODE_MAP.put("温州", "330300");
        CITY_ADCODE_MAP.put("东莞", "441900");
        CITY_ADCODE_MAP.put("佛山", "440600");
        CITY_ADCODE_MAP.put("无锡", "320200");
        CITY_ADCODE_MAP.put("常州", "320400");
        CITY_ADCODE_MAP.put("徐州", "320300");
        CITY_ADCODE_MAP.put("南通", "320600");
        CITY_ADCODE_MAP.put("扬州", "321000");
        CITY_ADCODE_MAP.put("桂林", "450300");
        CITY_ADCODE_MAP.put("珠海", "440400");
        CITY_ADCODE_MAP.put("惠州", "441300");
        CITY_ADCODE_MAP.put("烟台", "370600");
        CITY_ADCODE_MAP.put("潍坊", "370700");
        CITY_ADCODE_MAP.put("淄博", "370300");
        
        CITY_NAME_ALIASES.put("北京市", "北京");
        CITY_NAME_ALIASES.put("上海市", "上海");
        CITY_NAME_ALIASES.put("广州市", "广州");
        CITY_NAME_ALIASES.put("深圳市", "深圳");
        CITY_NAME_ALIASES.put("重庆市", "重庆");
        CITY_NAME_ALIASES.put("蓉城", "成都");
        CITY_NAME_ALIASES.put("成都市", "成都");
        CITY_NAME_ALIASES.put("杭城", "杭州");
        CITY_NAME_ALIASES.put("杭州市", "杭州");
        CITY_NAME_ALIASES.put("金陵", "南京");
        CITY_NAME_ALIASES.put("南京市", "南京");
        CITY_NAME_ALIASES.put("江城", "武汉");
        CITY_NAME_ALIASES.put("武汉市", "武汉");
        CITY_NAME_ALIASES.put("长安", "西安");
        CITY_NAME_ALIASES.put("西安市", "西安");
        CITY_NAME_ALIASES.put("津门", "天津");
        CITY_NAME_ALIASES.put("天津市", "天津");
        CITY_NAME_ALIASES.put("鹏城", "深圳");
        CITY_NAME_ALIASES.put("魔都", "上海");
        CITY_NAME_ALIASES.put("帝都", "北京");
        CITY_NAME_ALIASES.put("羊城", "广州");
        CITY_NAME_ALIASES.put("穗城", "广州");
    }
    
    public String handleWeatherQuery(String message) {
        try {
            String city = extractCityFromMessage(message);
            
            if (city == null || city.isEmpty()) {
                return "请告诉我您要查询哪个城市的天气？例如：北京天气、上海天气";
            }

            String weatherData = getWeatherFromAPI(city);
            
            if (weatherData != null && !weatherData.isEmpty()) {
                return weatherData;
            }

            return "抱歉，暂时无法获取 " + city + " 的天气信息，请稍后再试。";
        } catch (Exception e) {
            e.printStackTrace();
            return "天气查询失败: " + e.getMessage();
        }
    }
    
    public String handleWeatherQueryWithCity(String city) {
        try {
            if (city == null || city.isEmpty()) {
                return "请告诉我您要查询哪个城市的天气？例如：北京天气、上海天气";
            }

            String weatherData = getWeatherFromAPI(city);
            
            if (weatherData != null && !weatherData.isEmpty()) {
                return weatherData;
            }

            return "抱歉，暂时无法获取 " + city + " 的天气信息，请稍后再试。";
        } catch (Exception e) {
            e.printStackTrace();
            return "天气查询失败: " + e.getMessage();
        }
    }
    
    private String extractCityFromMessage(String message) {
        message = message.replaceAll("天气|预报|怎么样|如何|今天|明天|后天", "").trim();
        message = message.replaceAll("^[\\s\\u3000]*[查看询问请给我帮我查查询]+[\\s\\u3000]*", "").trim();
        
        if (message.isEmpty()) {
            return null;
        }
        
        return message;
    }
    
    private String getWeatherFromAPI(String city) {
        try {
            if (weatherApiKey == null || weatherApiKey.isEmpty() || "your_amap_api_key_here".equals(weatherApiKey)) {
                System.out.println("警告：未配置有效的高德地图 API Key");
                return generateMockWeather(city);
            }

            String adcode = convertCityToAdcode(city);
            
            if (adcode == null || adcode.isEmpty()) {
                System.err.println("未找到城市 " + city + " 的adcode");
                return fuzzySearchCity(city);
            }

            String encodedAdcode = java.net.URLEncoder.encode(adcode, "UTF-8");
            String encodedKey = java.net.URLEncoder.encode(weatherApiKey, "UTF-8");
            
            String url = String.format(
                "https://restapi.amap.com/v3/weather/weatherInfo?city=%s&key=%s&extensions=base&output=JSON",
                encodedAdcode, encodedKey
            );

            System.out.println("正在调用高德天气 API: " + url.replaceAll(weatherApiKey, "****"));
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> data = response.getBody();

            if (data != null) {
                System.out.println("天气 API 响应成功");
                
                String status = (String) data.get("status");
                String info = (String) data.get("info");
                
                if (!"1".equals(status)) {
                    System.err.println("天气API调用失败: " + info);
                    return generateMockWeather(city);
                }

                List<Map<String, Object>> lives = (List<Map<String, Object>>) data.get("lives");
                
                if (lives == null || lives.isEmpty()) {
                    System.err.println("天气数据为空");
                    return generateMockWeather(city);
                }

                Map<String, Object> live = lives.get(0);
                if (live == null) {
                    return generateMockWeather(city);
                }

                String province = (String) live.get("province");
                String cityName = (String) live.get("city");
                String weather = (String) live.get("weather");
                String temperature = (String) live.get("temperature");
                String windDirection = (String) live.get("winddirection");
                String windPower = (String) live.get("windpower");
                String humidity = (String) live.get("humidity");
                String reportTime = (String) live.get("reporttime");

                if (cityName == null) cityName = city;
                if (weather == null) weather = "未知";
                if (temperature == null) temperature = "N/A";
                if (windDirection == null) windDirection = "N/A";
                if (windPower == null) windPower = "N/A";
                if (humidity == null) humidity = "N/A";

                StringBuilder result = new StringBuilder();
                result.append(String.format(
                    "🌤️ %s%s 天气情况：\n\n" +
                    "📊 天气状况：%s\n" +
                    "🌡️ 当前温度：%s°C\n" +
                    "💨 风向：%s\n" +
                    "💪 风力：%s级\n" +
                    "💧 湿度：%s%%\n" +
                    "🕐 更新时间：%s",
                    cityName, 
                    province != null && !province.equals(cityName) ? " (" + province + ")" : "",
                    weather, 
                    temperature,
                    windDirection,
                    windPower,
                    humidity,
                    formatReportTime(reportTime)
                ));
                
                String emoji = getWeatherEmoji(weather);
                result.insert(0, emoji + "\n\n");
                
                return result.toString();
            }
        } catch (Exception e) {
            System.err.println("天气API调用失败: " + e.getMessage());
        }
        
        return generateMockWeather(city);
    }
    
    private String convertCityToAdcode(String city) {
        if (city == null || city.isEmpty()) {
            return null;
        }
        
        String normalizedCity = normalizeCityName(city);
        
        String adcode = CITY_ADCODE_MAP.get(normalizedCity);
        if (adcode != null) {
            return adcode;
        }
        
        return null;
    }
    
    private String normalizeCityName(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String trimmed = input.trim();
        
        String alias = CITY_NAME_ALIASES.get(trimmed);
        if (alias != null) {
            return alias;
        }
        
        for (Map.Entry<String, String> entry : CITY_NAME_ALIASES.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        if (trimmed.endsWith("市")) {
            String withoutShi = trimmed.substring(0, trimmed.length() - 1);
            if (CITY_ADCODE_MAP.containsKey(withoutShi)) {
                return withoutShi;
            }
        }
        
        return trimmed;
    }
    
    private String fuzzySearchCity(String input) {
        if (input == null || input.isEmpty()) {
            return "抱歉，未能识别城市名称，请提供更准确的城市名。";
        }
        
        String normalized = normalizeCityName(input);
        
        String matchedCity = null;
        double maxSimilarity = 0;
        
        for (String city : CITY_ADCODE_MAP.keySet()) {
            double similarity = calculateSimilarity(normalized, city);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                matchedCity = city;
            }
        }
        
        if (maxSimilarity >= 0.6) {
            System.out.println("模糊匹配: " + input + " -> " + matchedCity + " (相似度: " + maxSimilarity + ")");
            return getWeatherFromAPI(matchedCity);
        }
        
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("抱歉，未找到城市 \"").append(input).append("\" 的天气信息。\n\n");
        suggestions.append("💡 您可能想查询：\n");
        
        int count = 0;
        for (String city : CITY_ADCODE_MAP.keySet()) {
            if (city.contains(normalized) || normalized.contains(city)) {
                suggestions.append("• ").append(city).append("\n");
                count++;
                if (count >= 5) break;
            }
        }
        
        if (count == 0) {
            suggestions.append("• 北京\n• 上海\n• 广州\n• 深圳\n• 成都");
        }
        
        suggestions.append("\n\n请使用以上城市名称重新查询。");
        
        return suggestions.toString();
    }
    
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0;
        }
        
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        if (str1.contains(str2) || str2.contains(str1)) {
            return 0.8;
        }
        
        int distance = levenshteinDistance(str1, str2);
        int maxLen = Math.max(str1.length(), str2.length());
        
        if (maxLen == 0) {
            return 1.0;
        }
        
        return 1.0 - (double) distance / maxLen;
    }
    
    private int levenshteinDistance(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[m][n];
    }
    
    private String getWeatherEmoji(String weather) {
        if (weather == null) return "🌤️";
        
        if (weather.contains("晴")) return "☀️";
        if (weather.contains("云") || weather.contains("阴")) return "☁️";
        if (weather.contains("雷")) return "⛈️";
        if (weather.contains("雪")) return "❄️";
        if (weather.contains("雾") || weather.contains("霾")) return "🌫️";
        if (weather.contains("雨")) return "🌧️";
        
        return "🌤️";
    }
    
    private String formatReportTime(String reportTime) {
        if (reportTime == null || reportTime.length() < 16) {
            return reportTime;
        }
        try {
            String date = reportTime.substring(0, 10);
            String time = reportTime.substring(11, 16);
            return date + " " + time;
        } catch (Exception e) {
            return reportTime;
        }
    }
    
    private String generateMockWeather(String city) {
        String[] weathers = {"晴", "多云", "小雨", "阴天", "雷阵雨"};
        String[] icons = {"☀️", "⛅", "🌧️", "☁️", "⛈️"};
        int index = (int) (Math.random() * weathers.length);
        
        String weather = weathers[index];
        String icon = icons[index];
        double temp = 15 + Math.random() * 15;
        int humidity = 40 + (int) (Math.random() * 40);
        
        return String.format(
            "%s %s 天气（模拟数据）：\n\n" +
            "📊 天气状况：%s\n" +
            "🌡️ 当前温度：%.1f°C\n" +
            "💧 湿度：%d%%\n\n" +
            "💡 提示：配置高德地图 API Key 可获取准确数据\n" +
            "🔗 注册地址：https://lbs.amap.com/api/webservice/guide/create-project/get-key",
            icon, city, weather, temp, humidity
        );
    }
}
