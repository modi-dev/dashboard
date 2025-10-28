import java.net.HttpURLConnection;
import java.net.URL;

public class test_main {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:3001/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            System.out.println("Main page Response Code: " + responseCode);
            
            if (responseCode == 200) {
                System.out.println("Main page is working!");
            } else {
                System.out.println("Main page has issues. Response code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

