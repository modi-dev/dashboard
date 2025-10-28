import java.net.HttpURLConnection;
import java.net.URL;

public class test_servers {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:3001/servers");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            System.out.println("Servers page Response Code: " + responseCode);
            
            if (responseCode == 200) {
                System.out.println("Servers page is working!");
            } else {
                System.out.println("Servers page has issues. Response code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

