/**
 * @author Sridev Balakrishnan
 * @purpose Uses Google Cloud's NLP to find label fields and perform sentiment analysis
 * @input Reviews excel file
 * @output EntitySentiment excel file
 */
package com.reviews.analyze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.cloud.language.v1beta2.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.Document.Type;
import com.google.cloud.language.v1beta2.EncodingType;
import com.google.cloud.language.v1beta2.Entity;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.cloud.language.v1beta2.AnalyzeEntitySentimentRequest;

import com.reviews.domain.Response;
import com.reviews.domain.ReviewSentiment;
import com.reviews.writer.WriteSentimentsExcel;

public class AnalyzeEntitySentiment {

	static final Logger log = LogManager.getLogger(AnalyzeEntitySentiment.class.getName());
	static ArrayList<ReviewSentiment> reviewSetimentLst = new ArrayList<>();
	static final String FILE_NAME = "C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_reviews.xlsx";
	static final String[] items = {"account", "bill", "cable", "crash", "auto", "phone", "ios", "android", "internet", 
			"tv", "email", "password", "security", "chat", "support", "work", "bad", "good", "faq", 
			"face", "touch", "login", "wifi", "modem", "router"};

	public static void main(String... args) throws Exception {

		List<Response> respLst = new ArrayList<>();

		try (FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
				Workbook workbook = new XSSFWorkbook(excelFile)) {
			
			XSSFSheet datatypeSheet = (XSSFSheet) workbook.getSheetAt(0);
			String colType = "TYPE";
			String colDate = "DATE";
			String colRating = "RATING";
			String colUserName = "USERNAME";
			String colComments = "COMMENTS";

			Integer columnNoType = null;
			Integer columnNoDate = null;
			Integer columnNoRating = null;
			Integer columnNoUserName = null;
			Integer columnNoComments = null;

			//output all not null values to the list

			Row firstRow = datatypeSheet.getRow(0);

			for(Cell cell:firstRow){
				if (cell.getStringCellValue().equals(colType)){
					columnNoType = cell.getColumnIndex();
				}
				else if (cell.getStringCellValue().equals(colDate)){
					columnNoDate = cell.getColumnIndex();
				}
				else if (cell.getStringCellValue().equals(colRating)){
					columnNoRating = cell.getColumnIndex();
				}
				else if (cell.getStringCellValue().equals(colUserName)){
					columnNoUserName = cell.getColumnIndex();
				}
				else if (cell.getStringCellValue().equals(colComments)){
					columnNoComments = cell.getColumnIndex();
				}
			}

			if (columnNoType != null){
				for (Row row : datatypeSheet) {
					Response resp = new Response();

					if (row.getRowNum() == 0)
						continue;

					Cell cType = row.getCell(columnNoType);
					Cell cDate = row.getCell(columnNoDate);
					Cell cRating = row.getCell(columnNoRating);
					Cell cUserName = row.getCell(columnNoUserName);
					Cell cComments = row.getCell(columnNoComments);

					resp.setType(cType.getStringCellValue());
					DataFormatter formatter = new DataFormatter();
					String sDate = formatter.formatCellValue(cDate);
					resp.setDate(new SimpleDateFormat("MM-dd-yyyy").parse(sDate));
					resp.setRating(cRating.getStringCellValue());
					resp.setUserName(cUserName.getStringCellValue());
					resp.setReview(cComments.getStringCellValue());
					respLst.add(resp);
				}
			}
		}
		catch (FileNotFoundException e) {
			log.error("File Not Found Exception in sentiment analysis >>> {0}",e);
		} catch (IOException e) {
			log.error("IO Exception in sentiment analysis >>> {0}",e);
		}
		List<ReviewSentiment> reviews = computeSentimentIndex(respLst);
		WriteSentimentsExcel.writer(reviews);
	}

	public static List<ReviewSentiment> computeSentimentIndex(List<Response> respLst) {
		List<ReviewSentiment> revSentLst = new ArrayList<>();
		for (Response r: respLst) {
			List<ReviewSentiment> rs = entitySentimentText(r);
			if (!rs.isEmpty())
				log.info("{}", rs);
			revSentLst.addAll(rs);
		}
		return revSentLst;
	}


	/** Detects the entity sentiments in the string {@code text} using the Language API. */
	public static List<ReviewSentiment> entitySentimentText(Response resp) {

		String text = resp.getReview();
		List<ReviewSentiment> rsLst = new ArrayList<>();

		// Instantiate the Language client
		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
			AnalyzeEntitySentimentRequest request =
					AnalyzeEntitySentimentRequest.newBuilder()
					.setDocument(doc)
					.setEncodingType(EncodingType.UTF16)
					.build();

			// detect entity sentiments in the given string
			AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);

			/**
			@score of the sentiment ranges between -1.0 (negative) and 1.0 (positive) and corresponds to the overall emotional 
					leaning of the text.
			@magnitude indicates the overall strength of emotion (both positive and negative) within the given text, 
					between 0.0 and +inf. Unlike score, magnitude is not normalized; each expression of emotion within the text 
					(both positive and negative) contributes to the text's magnitude (so longer text blocks may have greater 
					magnitudes).
			 */
			for (Entity entity : response.getEntitiesList()) {
				String entityName = entity.getName();
				if (containsItemFromArray(entityName, items)) {
					ReviewSentiment rs = new ReviewSentiment();
					rs.setType(resp.getType());
					rs.setDate(resp.getDate());
					rs.setRating(resp.getRating());
					rs.setUserName(resp.getUserName());
					rs.setComments(resp.getReview());
					log.info("Entity: {}", entityName);
					rs.setEntity(entity.getName());
					log.info("Salience: {}", (double) Math.round(entity.getSalience()*1000)/1000.0d);
					rs.setSalience((double) Math.round(entity.getSalience()*1000)/1000.0d);
					log.info("Sentiment Magnitude: {}", (double) Math.round(entity.getSentiment().getMagnitude()*1000)/1000.0d);
					rs.setMagnitude((double) Math.round(entity.getSentiment().getMagnitude()*1000)/1000.0d);
					log.info("Sentiment Score: {}", (double) Math.round(entity.getSentiment().getScore()*1000)/1000.0d);
					rs.setScore((double) Math.round(entity.getSentiment().getScore()*1000)/1000.0d);
					rsLst.add(rs);
				}
			}
		}
		catch (Exception e) {
			log.error("Exception in sentiment analysis >>> {0}",e);
		}
		return rsLst;
	}

	public static boolean containsItemFromArray(String inputString, String[] items) {
		// Convert the array of String items as a Stream
		// In case of a match returns true, false otherwise
		return Arrays.stream(items).anyMatch(inputString::contains);
	}
}
