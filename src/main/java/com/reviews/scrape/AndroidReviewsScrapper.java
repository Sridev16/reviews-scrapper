/**
 * @author Sridev Balakrishnan
 * @Purpose: Polls mobile app reviews from Google Play site
 * @Input: 
 * @Output: List of Android user reviews sorted by date
 */

package com.reviews.scrape;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.Month;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.reviews.domain.Response;
import com.reviews.writer.ResponseWriterExcel;


public class AndroidReviewsScrapper {

	static final Logger log = LogManager.getLogger(AndroidReviewsScrapper.class.getName());

	static final String URL = "https://play.google.com/store/apps/details?id=com.optimum.lbsaopt&hl=en";
	static String chromeDriver = "webdriver.chrome.driver";
	static String chromeDriverPath = "";
	static String websiteLoadCheckString = "return((window.jQuery != null) && (jQuery.active === 0))";
	static String websiteLoadCheckBoolean = "true";
	static final String POS_TAGGER_URL = Objects.requireNonNull(IOSReviewsScrapper.class.getClassLoader().
			getResource("en-pos-maxent.bin")).getPath(); // The path to your POS tagging model
	static final String SENT_DETECT_URL = Objects.requireNonNull(IOSReviewsScrapper.class.getClassLoader().
			getResource("en-sent.bin")).getPath(); // The path to your sentence detection model
	static final double THRESHOLD_RAKE_VAL = 4.00; 
	static List<Response> responseLst =  new ArrayList<>();
	static String headerXPath = "//div[contains(@class,'XnFhVd')]";
	static String showMoreXPath = "//div[contains(@class,'PFAhAf')]";
	static String reviewXPath = "//div[contains(@class,'d15Mdf bAhLNe')]";
	static String commonXPath = ".//div[contains(@class,'xKpxId zc7KVe')]//div[contains(@class,'bAhLNe kx8XBd')]";
	static String nameXPath = commonXPath + "//span[contains(@class,'X43Kjb')]";
	static String ratingXPath = commonXPath + "//div//span[contains(@class,'nt2C1d')]//div//div";
	static String dateXPath = commonXPath + "//div//span[contains(@class,'p2TkOb')]";
	static String commentXPath = ".//div[contains(@class,'UD7Dzf')]//span";
	static String likesXPath = ".//div[contains(@class,'xKpxId zc7KVe')] //div[contains(@class,'YCMBp GVFJbb')]//div"
								+ "//div[contains(@class,'XlMhZe')]//div[contains(@class,'jUL89d y92BAb')]";
	static final String OUTPUT_FILE_RS = "C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_NLP.xlsx";
	static final String WORKSHEET_NAME_RS = "Optimum App Reviews NLP";

	private AndroidReviewsScrapper() {
		// Utility class
	}

	public static void main(String... args) throws IOException {

		// Fetch reviews from Google Play site 
		webPageSurf();

		// Sort by date; latest review first
		responseLst.sort(Comparator.comparing(Response::getDate).reversed());

		// Write sorted data to MS Excel file
		ResponseWriterExcel.writer(responseLst,null,OUTPUT_FILE_RS,WORKSHEET_NAME_RS);

		System.exit(0);
	}
	/**
	 * @throws IOException 
	 * 
	 */
	public static List<Response> webPageSurf() {
		
		Properties prop = new Properties();

		try {
			InputStream inStream = AndroidReviewsScrapper.class.getClassLoader().
					getResourceAsStream("application.properties");
			if (inStream == null) {
				log.error("Unable to find application.properties");
			}

			prop.load(inStream);

			chromeDriverPath = prop.getProperty("chrome.driver.path");
		}
		catch (Exception ex) {
			log.error("Exception in reading config file");
		}
		
		List<Response> reviews = new ArrayList<>();

		System.setProperty(chromeDriver, chromeDriverPath);

		// Initialize browser
		WebDriver driver = new ChromeDriver();

		// Open Google 
		driver.get(URL);

		// Maximize browser
		driver.manage().window().maximize();

		try {
			WebDriverWait driverWait =  new WebDriverWait(driver, 10);

			driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.xpath(headerXPath))));
			driver.findElement(By.xpath(headerXPath)).click();

			reviews = populateData(driver);
		}
		catch (TimeoutException e) {
			log.error("Timedout reading elements: ", e);
		}
		catch (Exception e) {
			log.error("Exception while reading data from webpage for <<< {} >>>", URL, e);
		}
		finally {
			log.info("Driver closed");			
			driver.close();
		}
		return reviews;
	}

	private static List<Response> populateData(WebDriver driver) throws ParseException {

		List<WebElement> reviews;

		// Scroll till end of page
		scrollTillEnd(driver);

		// Click SHOW MORE button
		WebDriverWait driverWait =  new WebDriverWait(driver, 10);
		driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
				(By.xpath(showMoreXPath))));
		driver.findElement(By.xpath(showMoreXPath)).click();

		// Scroll till end of page again
		scrollTillEnd(driver);

		WebElement element = driver.findElement(By.name("q"));
		JavascriptExecutor js = (JavascriptExecutor)driver;
		js.executeScript("arguments[0].scrollIntoView();", element); 	

		reviews = driver.findElements(By.xpath(reviewXPath));

		for(WebElement e: reviews) {

			Response r = new Response();

			// Populate username in bean
			r.setUserName(retrieveName(e));

			// Populate league in bean
			r.setRating(retrieveRating(e));

			// Populate commented date in bean
			boolean dateAvail = false;
			if (!retrieveDate(e).isEmpty() && retrieveDate(e) != null) {
				String[] dateArr = retrieveDate(e).split("\\,+");
				String monthDate = dateArr[0];
				String year = dateArr[1].trim();
				String[] result = monthDate.split("\\s+");
				String sMonth = result[0];
				String day = String.format("%02d", Integer.parseInt(result[1]));
				int iMonth = Month.valueOf(sMonth.toUpperCase()).getValue();
				String sDate = String.format("%02d", iMonth) + "-" + day + "-" + year;
				SimpleDateFormat outFormat = new SimpleDateFormat("MM-dd-yyyy");
				Date dDate = outFormat.parse(sDate);
				r.setDate(dDate);
				dateAvail = true;
			}

			// Populate review in bean
			r.setReview(retrieveReview(e));

			// Populate likes count
			r.setGoogleLikes(retrieveLikes(e));

			r.setType("Android");

			if (dateAvail)
				responseLst.add(r);
		}		
		return responseLst;
	}

	private static String retrieveName(WebElement e) {		

		return (e.findElement(By.xpath(nameXPath)).getText());
	}

	private static String retrieveRating(WebElement e) {

		WebElement ele = e.findElement(By.xpath(ratingXPath));
		String label = ele.getAttribute("aria-label");
		label = label.replaceAll("[^0-9]", "");
		return label;
	}

	private static String retrieveDate(WebElement e) {		

		return (e.findElement(By.xpath(dateXPath)).getText());
	}

	private static String retrieveReview(WebElement e) {		

		return (e.findElement(By.xpath(commentXPath)).getText());
	}

	private static String retrieveLikes(WebElement e) {		

		return (e.findElement(By.xpath(likesXPath)).getText());	
	}

	private static WebDriver scrollTillEnd (WebDriver driver) {

		try {
			long lastHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

			while (true) {
				((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
				Thread.sleep(2000);

				long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
				if (newHeight == lastHeight) {
					break;
				}
				lastHeight = newHeight;
			}
		}
		catch (TimeoutException | InterruptedException exTimeout) {
			log.error("TimeoutException while reading elements ", exTimeout);
			Thread.currentThread().interrupt();
		}
		catch (NoSuchElementException e) {
			log.error("Exception while reading elements ", e);
		}
		return driver;
	}
}