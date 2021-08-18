import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UserInterfaceView {
    private Controller controller = new Controller();
    private Pattern pattern;

    public void runInterface() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the name of the city or 0 for exit:");
            String city = scanner.nextLine();
            if (city.equals("0")) break;

            if (!Pattern.matches("[a-zA-Z]*\\s?-?[a-zA-Z]*", city)) {
                System.out.println("Please enter the name of the city correctly!");
                continue;
            }

            System.out.println("Enter 1 for today's weather forecast; " +
                    "Enter 5 for 5 days weather forecast; Enter 0 for exit:");

            String command = scanner.nextLine();

            if (command.equals("0")) break;
            if (!Pattern.matches("[1,5,0]", command)) {
                System.out.println("Enter 0, 1 or 5!");
                continue;
            }

            try {
                controller.getWeather(command, city);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}