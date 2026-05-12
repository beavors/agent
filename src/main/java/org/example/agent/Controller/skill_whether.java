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
            if (weatherApiKey == null || weatherApiKey.isEmpty() || "your_openweathermap_api_key_here".equals(weatherApiKey)) {
                System.out.println("警告：未配置有效的天气 API Key");
                return generateMockWeather(city);
            }

            String encodedCity = convertChineseToPinyin(city);
            encodedCity = java.net.URLEncoder.encode(encodedCity, "UTF-8");
            
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s,CN&appid=%s&units=metric&lang=zh_cn",
                encodedCity, weatherApiKey
            );

            System.out.println("正在调用天气 API: " + url.replaceAll(weatherApiKey, "****"));
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> data = response.getBody();

            if (data != null) {
                System.out.println("天气 API 响应成功");
                
                Map<String, Object> main = (Map<String, Object>) data.get("main");
                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) data.get("weather");
                Map<String, Object> wind = (Map<String, Object>) data.get("wind");
                Map<String, Object> sys = (Map<String, Object>) data.get("sys");

                if (main == null || weatherList == null || wind == null) {
                    System.err.println("天气数据格式错误");
                    return generateMockWeather(city);
                }

                Map<String, Object> weather = weatherList.get(0);

                double temp = ((Number) main.get("temp")).doubleValue();
                double feelsLike = ((Number) main.get("feels_like")).doubleValue();
                int humidity = ((Number) main.get("humidity")).intValue();
                String description = (String) weather.get("description");
                double windSpeed = ((Number) wind.get("speed")).doubleValue();
                
                String country = "";
                if (sys != null && sys.get("country") != null) {
                    country = " (" + sys.get("country") + ")";
                }

                StringBuilder result = new StringBuilder();
                result.append(String.format(
                    "🌤️ %s%s 天气情况：\n\n" +
                    "📊 天气状况：%s\n" +
                    "🌡️ 当前温度：%.1f°C\n" +
                    "🤔 体感温度：%.1f°C\n" +
                    "💧 湿度：%d%%\n" +
                    "💨 风速：%.1f m/s",
                    city, country, description, temp, feelsLike, humidity, windSpeed
                ));
                
                if (weather.containsKey("icon")) {
                    String iconCode = (String) weather.get("icon");
                    String emoji = getWeatherEmoji(iconCode);
                    result.insert(0, emoji + "\n\n");
                }
                
                return result.toString();
            }
        } catch (Exception e) {
            System.err.println("天气API调用失败: " + e.getMessage());
        }
        
        return generateMockWeather(city);
    }
    
    private String convertChineseToPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return chinese;
        }
        
        java.util.Map<String, String> cityMap = new java.util.HashMap<>();
        cityMap.put("北京", "Beijing");
        cityMap.put("上海", "Shanghai");
        cityMap.put("广州", "Guangzhou");
        cityMap.put("深圳", "Shenzhen");
        cityMap.put("重庆", "Chongqing");
        cityMap.put("成都", "Chengdu");
        cityMap.put("杭州", "Hangzhou");
        cityMap.put("南京", "Nanjing");
        cityMap.put("武汉", "Wuhan");
        cityMap.put("西安", "Xi'an");
        cityMap.put("天津", "Tianjin");
        cityMap.put("苏州", "Suzhou");
        cityMap.put("青岛", "Qingdao");
        cityMap.put("大连", "Dalian");
        cityMap.put("郑州", "Zhengzhou");
        cityMap.put("长沙", "Changsha");
        cityMap.put("济南", "Jinan");
        cityMap.put("合肥", "Hefei");
        cityMap.put("福州", "Fuzhou");
        cityMap.put("昆明", "Kunming");
        cityMap.put("贵阳", "Guiyang");
        cityMap.put("南宁", "Nanning");
        cityMap.put("南昌", "Nanchang");
        cityMap.put("太原", "Taiyuan");
        cityMap.put("石家庄", "Shijiazhuang");
        cityMap.put("哈尔滨", "Harbin");
        cityMap.put("长春", "Changchun");
        cityMap.put("沈阳", "Shenyang");
        cityMap.put("乌鲁木齐", "Urumqi");
        cityMap.put("兰州", "Lanzhou");
        cityMap.put("银川", "Yinchuan");
        cityMap.put("西宁", "Xining");
        cityMap.put("拉萨", "Lhasa");
        cityMap.put("海口", "Haikou");
        cityMap.put("三亚", "Sanya");
        cityMap.put("厦门", "Xiamen");
        cityMap.put("宁波", "Ningbo");
        cityMap.put("温州", "Wenzhou");
        cityMap.put("东莞", "Dongguan");
        cityMap.put("佛山", "Foshan");
        cityMap.put("无锡", "Wuxi");
        cityMap.put("常州", "Changzhou");
        cityMap.put("徐州", "Xuzhou");
        cityMap.put("南通", "Nantong");
        cityMap.put("扬州", "Yangzhou");
        cityMap.put("桂林", "Guilin");
        cityMap.put("珠海", "Zhuhai");
        cityMap.put("惠州", "Huizhou");
        cityMap.put("烟台", "Yantai");
        cityMap.put("潍坊", "Weifang");
        cityMap.put("淄博", "Zibo");
        
        String pinyin = cityMap.get(chinese);
        if (pinyin != null) {
            return pinyin;
        }
        
        return chinese;
    }
    
    private String getWeatherEmoji(String iconCode) {
        if (iconCode.startsWith("01")) return "☀️";
        if (iconCode.startsWith("02")) return "⛅";
        if (iconCode.startsWith("03") || iconCode.startsWith("04")) return "☁️";
        if (iconCode.startsWith("09") || iconCode.startsWith("10")) return "🌧️";
        if (iconCode.startsWith("11")) return "⛈️";
        if (iconCode.startsWith("13")) return "❄️";
        if (iconCode.startsWith("50")) return "🌫️";
        return "🌤️";
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
            "💡 提示：配置真实的天气 API Key 可获取准确数据\n" +
            "🔗 注册地址：https://openweathermap.org/api",
            icon, city, weather, temp, humidity
        );
    }
}
