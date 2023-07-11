import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;

public class WebHandler
{
    WebHandler()
    {

    }

    public static void SendMeDailyPosts()
    {
        boolean IsConnected = false;

        Thread LoadingThread = new Thread(new LoadingDialog());
        LoadingThread.start();

        try {
            URL GoogleURL = new URL("http://www.google.com");
            URLConnection GoogleConnection = GoogleURL.openConnection();
            GoogleConnection.connect();
            System.out.println("Connected to internet");
            IsConnected = true;
        } catch (IOException e) {
            System.out.println("No internet connection available");
        }

        if(IsConnected) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");

            WebDriver driver = new ChromeDriver(options);
            WebDriver driver2 = new ChromeDriver(options);

            Vector<String> AttachmentLinks = new Vector<>();
            Vector<WebDriver> AttachmentDrivers = new Vector<>();

            EnterCredentials(driver);

            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

            WebElement element = driver.findElement(By.id("messagelist"));

            List<WebElement> emails = element.findElements(By.className("unread"));

            StringBuilder final_mails = new StringBuilder();

            if (emails.size() != 0) {
                String Last_URL;
                String MailContent = null;

                boolean first = true;

                for (WebElement mail : emails) {
                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(2000));
                    String Text = mail.getAttribute("innerText");
                    MailContent = null;

                    boolean ThereIsAttachment = true;
                    List<WebElement> AttachmentList = null;

                    if (!Text.isEmpty()) {
                        System.out.println(Text);

                        WebElement mailLink = mail.findElement(By.tagName("a"));
                        String hrefValue = mailLink.getAttribute("href");

                        if (first) {

                            EnterCredentials(driver2);

                            first = false;
                        }
                        driver2.get(hrefValue);
                        driver2.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

                        try {
                            AttachmentList = driver2.findElements(By.className("attachmentslist"));
                        } catch (Exception e) {
                            ThereIsAttachment = false;
                        }

                        if (ThereIsAttachment) {
                            for (WebElement Attachment : AttachmentList) {
                                System.out.println("ATTACHMENT LINK: " + Attachment.findElement(By.className("filename")).getAttribute("href"));
                                AttachmentLinks.add(Attachment.findElement(By.className("filename")).getAttribute("href"));


                                final_mails.append(Attachment.findElement(By.className("filename")).getAttribute("href")).append("\n");
                            }
                        }

                        System.out.println(hrefValue);

                        MailContent = driver2.findElement(By.id("message-content")).getAttribute("innerText");
                        System.out.println(MailContent);

                    }
                    final_mails.append(Text).append("\n");

                    if (MailContent != null) {
                        final_mails.append(MailContent).append("\n").append("\n").append("\n").append("\n").append("\n").append("\n").append("\n");
                    }


                }
                System.out.println("SIZE: " + emails.size());

                Path path = Paths.get("DailyUnreadMails.txt");

                try {
                    Files.write(path, final_mails.toString().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                driver2.quit();
                driver.quit();

                LoadingThread.interrupt();

                try {
                    Desktop.getDesktop().open(path.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < AttachmentLinks.size(); i++) {

                    AttachmentDrivers.get(i).get(AttachmentLinks.get(i));
                }

            } else {
                driver2.quit();
                driver.quit();

                LoadingThread.interrupt();

                String[] noNewMailMessages = {
                        "No new mail. Enjoy your day!",
                        "You're all caught up. No new messages.",
                        "Inbox empty. Nothing new to read.",
                        "No unread emails. Keep up the good work!",
                        "Great news! Your inbox is clear.",
                        "No new mail. Take a break and relax.",
                        "Congratulations! Zero unread emails.",
                        "No new messages. Take a moment to breathe.",
                        "Inbox up to date. No new emails found.",
                        "No unread emails. Keep up the productivity!",
                        "All clear! No new messages in your inbox.",
                        "No new mail. Time for a well-deserved break!",
                        "Nothing new to report. Inbox empty.",
                        "No unread emails. Enjoy the peace and quiet.",
                        "Zero new messages. Keep up the good habits!",
                        "No new mail. Take a moment for yourself.",
                        "Inbox empty. No new messages to read.",
                        "No new emails. Enjoy the clutter-free inbox!",
                        "No unread messages. Time for a celebration!",
                        "No new mail. Stay focused and keep it up!"
                };

                JOptionPane.showMessageDialog(null, noNewMailMessages[ThreadLocalRandom.current().nextInt(0, 20)], "Mail state", JOptionPane.INFORMATION_MESSAGE);

                exit(1);
            }
        }
        else {

            LoadingThread.interrupt();
            JOptionPane.showMessageDialog(null, "No internet connection available!", "Connection Error", JOptionPane.ERROR_MESSAGE);
            exit(1);
        }

    }

    public static void EnterCredentials(WebDriver driver)
    {
        driver.get("https://poczta.student.tu.kielce.pl/");

        WebElement usernamefield = driver.findElement(By.id("rcmloginuser"));
        WebElement passwordfield = driver.findElement(By.id("rcmloginpwd"));

        usernamefield.sendKeys(ReadDataFromJSONFile("credentials", "username"));
        passwordfield.sendKeys(ReadDataFromJSONFile("credentials", "password"));

        driver.findElement(By.id("rcmloginsubmit")).click();
    }

    public static String ReadDataFromJSONFile(String filename , String DataName)
    {
        try
        {
            String filecontent = new String(Files.readAllBytes(Paths.get(filename)));

            JSONArray jsonArray = new JSONArray(filecontent);

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has(DataName))
                {
                    return jsonObject.getString(DataName);
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Error reading the game meta data! :: key{ " + DataName + " }" );
            e.printStackTrace();
        }
        return filename;
    }

    static class LoadingDialog implements Runnable
    {
        String[] loadingMessages = {
                "Hold tight, we're preparing an extraordinary experience for you.",
                "Loading... Unleashing the power of code.",
                "Embrace the anticipation, greatness is loading.",
                "Preparing to take off into the digital realm.",
                "Just a few more moments, and the magic will begin.",
                "Loading... Discovering new dimensions of possibilities.",
                "Sit back and relax while we create digital wonders for you.",
                "Loading... Fueling the engines of innovation.",
                "Patience, young Padawan. The force of progress is awakening.",
                "Loading... Assembling the building blocks of your dreams.",
                "Time is relative, but our progress is constant.",
                "Loading... Forging a path to technological brilliance.",
                "Shhh... Can you hear the whispers of progress?",
                "Loading... Harnessing the power of imagination.",
                "Rome wasn't built in a day, and neither is groundbreaking software.",
                "Loading... Igniting the sparks of innovation.",
                "Hold on tight, we're about to enter a realm of digital wonders.",
                "Loading... Crafting a symphony of code and creativity.",
                "The gears are turning, progress is on its way.",
                "Loading... Unlocking the gates to a digital universe.",
                "Loading... Infusing your experience with cutting-edge technology.",
                "Hold on, we're paving the way for a seamless journey.",
                "Preparing for greatness. Loading in progress.",
                "Loading... Initiating the launch sequence.",
                "Sit tight, we're optimizing every bit for optimal performance.",
                "Loading... Unleashing the full potential of innovation.",
                "Just a moment, we're assembling the digital pieces.",
                "Loading... Empowering you with next-level features.",
                "Hold your breath, we're diving into the realm of possibilities.",
                "Loading... Creating a world tailored just for you.",
                "Stay patient, we're crafting a masterpiece behind the scenes.",
                "Loading... Unraveling the mysteries of code.",
                "Brace yourself, we're about to embark on a digital adventure.",
                "Loading... Unleashing the power of imagination and technology.",
                "Hang in there, we're weaving together the threads of innovation.",
                "Loading... Elevating your experience to new heights.",
                "Get ready, we're about to unveil a new era of possibilities.",
                "Loading... Blazing the trail towards a brighter future.",
                "Patience, we're aligning the stars for a remarkable experience.",
                "Loading... Harnessing the potential of ones and zeros."
        };

        String CurrentMessage;

        LoadingDialog()
        {
            super();
            CurrentMessage = loadingMessages[ThreadLocalRandom.current().nextInt(0,40)];
        }

        @Override
        public void run()
        {
                JOptionPane.showOptionDialog(null, CurrentMessage ,"Process" , JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
        }
    }

}
