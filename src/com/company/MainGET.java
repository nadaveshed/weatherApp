package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainGET {
    private static ArrayList<String> t = new ArrayList<String>();
    private static ArrayList<String> a = new ArrayList<String>(100);
    private static ArrayList<String> b = new ArrayList<String>();

    public static void main(String[] args) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        JSONArray a = (JSONArray) parser.parse(new FileReader("src/configuration.json"));

        for (Object o : a) {
            JSONObject jsonLineItem = (JSONObject) o;
            String freq = jsonLineItem.get("frequency").toString();
            String city = jsonLineItem.get("city_name").toString();
            String threshold = jsonLineItem.get("threshold").toString();

            interval(Integer.parseInt(freq), city, threshold);
        }
    }

    public static void interval(int frequency, String city, String threshold) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getApiData(city, threshold);
            }
        }, 0, frequency, TimeUnit.SECONDS);
    }

    private static void temperatureChange(String city, String threshold, String te) {

        a.add(0, city);
        b.add(0, te);

        double t1 = 0, t2 = 0;

        for (int i = t.size(); i > t.size(); i--) {
            int j = i-1;
                t1 = Double.parseDouble(b.get(i));
                t2 = Double.parseDouble(b.get(j));
                if (a.get(i) == a.get(j)) {
                    System.out.println(a.get(j));
                    if (t1 < t2) {
                        if ((t1 / t2) * 100 >= Integer.parseInt(threshold)) {
                            System.out.println("Warning the temperature is changed by more than " + threshold + "%");
                        }
                    }
                    else if (t1 > t2) {
                        if ((t2 / t1) * 100 >= Integer.parseInt(threshold)) {
                            System.out.println("Warning the temperature is changed by more than " + threshold + "%");
                        }
                    }
                }
            }

    }

    public static void getApiData(String city, String threshold) {

        try {

            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=e735b6b632e6c008be941b8dbdb346d4");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Getting the response code
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                String inline = "";
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }

                scanner.close();

                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline);

                JSONObject wind = (JSONObject) data_obj.get("wind");
                JSONObject temp = (JSONObject) data_obj.get("main");

                t.add(temp.get("temp").toString());

                System.out.println(
                        "Time: " + new java.sql.Timestamp(new java.util.Date().getTime()) +
                                "\nName: " + data_obj.get("name") +
                                "\nTemperature: " + temp.get("temp") + " °C" +
                                "\nWind speed: " + wind.get("speed") + "m/s" +
                                "\n--------------------------------"
                );
                temperatureChange(city, threshold, temp.get("temp").toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}