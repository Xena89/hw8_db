import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AccuWeatherModel implements WeatherModel {
    private static final String PROTOCOL = "https";
    private static final String BASE_HOST = "dataservice.accuweather.com";
    private static final String FORECASTS = "forecasts";
    private static final String VERSION = "v1";
    private static final String DAILY = "daily";
    private static final String ONE_DAY = "1day";
    private static final String FIVE_DAYS = "5day";
    private static final String API_KEY = "ybltRTayKwRGC44B8Pve2qKVgIVkNfgY";
    private static final String API_KEY_QUERY_PARAM = "apikey";
    private static final String LOCATIONS = "locations";
    private static final String CITIES = "cities";
    private static final String AUTOCOMPLETE = "autocomplete";
    private String minTempF;
    private String maxTempF;
    private String date;
    private int TempCelsius;
    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private DataBaseRepository dataBaseRepository = new DataBaseRepository();

    public void getWeather(String selectedCity, Period period) throws IOException {
        switch (period) {
            case NOW:
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme(PROTOCOL)
                        .host(BASE_HOST)
                        .addPathSegment(FORECASTS)
                        .addPathSegment(VERSION)
                        .addPathSegment(DAILY)
                        .addPathSegment(ONE_DAY)
                        .addPathSegment(detectCityKey(selectedCity))
                        .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                        .build();

                Request request = new Request.Builder()
                        .url(httpUrl)
                        .build();

                Response oneDayForecastResponse = okHttpClient.newCall(request).execute();
                showAvarageWeatherForNdays(1, oneDayForecastResponse.body().string(), selectedCity);
                break;
            case FIVE_DAYS:
                HttpUrl httpUrlFiveDays = new HttpUrl.Builder()
                        .scheme(PROTOCOL)
                        .host(BASE_HOST)
                        .addPathSegment(FORECASTS)
                        .addPathSegment(VERSION)
                        .addPathSegment(DAILY)
                        .addPathSegment(FIVE_DAYS)
                        .addPathSegment(detectCityKey(selectedCity))
                        .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                        .build();

                Request requestFiveDays = new Request.Builder()
                        .url(httpUrlFiveDays)
                        .build();

                Response fiveDayForecastResponse = okHttpClient.newCall(requestFiveDays).execute();
                showAvarageWeatherForNdays(5, fiveDayForecastResponse.body().string(), selectedCity);
                break;
        }
    }

    @Override
    public List<Weather> getSavedToDBWeather() {
        return dataBaseRepository.getSavedToDBWeather();
    }

    private String detectCityKey(String selectCity) throws IOException {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(PROTOCOL)
                .host(BASE_HOST)
                .addPathSegment(LOCATIONS)
                .addPathSegment(VERSION)
                .addPathSegment(CITIES)
                .addPathSegment(AUTOCOMPLETE)
                .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                .addQueryParameter("q", selectCity)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .addHeader("accept", "application/json")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseString = response.body().string();

        String cityKey = objectMapper.readTree(responseString).get(0).at("/Key").asText();
        return cityKey;
    }
    private void showAvarageWeatherForNdays(int n, String response, String selectedCity) throws JsonProcessingException {
        for (int i = 0; i < n; i++) {
            minTempF = objectMapper.readTree(response).at("/DailyForecasts").get(i).at("/Temperature/Minimum/Value").asText();
            maxTempF = objectMapper.readTree(response).at("/DailyForecasts").get(i).at("/Temperature/Maximum/Value").asText();
            date = objectMapper.readTree(response).at("/DailyForecasts").get(i).at("/Date").asText();
            TempCelsius = (int) ((((Float.parseFloat(minTempF) + Float.parseFloat(maxTempF)) / 2) - 32) / 1.8);
            try {
                new DataBaseRepository().saveWeatherToDataBase(new Weather(selectedCity, date, TempCelsius));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            System.out.println(date + " Средняя температура в " + selectedCity + ": " + TempCelsius + " градусов по C");
        }
    }
}
