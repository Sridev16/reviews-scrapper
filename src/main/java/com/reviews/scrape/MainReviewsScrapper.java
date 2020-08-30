/**
 * @author Sridev Balakrishnan
 * @Purpose: Polls mobile app reviews from Apple & Google Play sites; analyzes the data & extracts keywords 
 * @Input: 
 * @Output: Writes to an Excel file
 */

package com.reviews.scrape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.reviews.analyze.AnalyzeEntitySentiment;
import com.reviews.domain.Response;
import com.reviews.domain.ReviewSentiment;
import com.reviews.writer.ResponseWriterExcel;

import io.github.crew102.rapidrake.RakeAlgorithm;
import io.github.crew102.rapidrake.data.SmartWords;
import io.github.crew102.rapidrake.model.RakeParams;
import io.github.crew102.rapidrake.model.Result;

public class MainReviewsScrapper {

	static final Logger log = LogManager.getLogger(MainReviewsScrapper.class.getName());
	static final String URL = "https://amp-api.apps.apple.com/v1/catalog/us/apps/1234273194/reviews?l=en-US&platform=web&additionalPlatforms=appletv%2Cipad%2Ciphone%2Cmac";
	static final String POS_TAGGER_URL = Objects.requireNonNull(IOSReviewsScrapper.class.getClassLoader().
			getResource("en-pos-maxent.bin")).getPath(); // The path to your POS tagging model
	static final String SENT_DETECT_URL = Objects.requireNonNull(IOSReviewsScrapper.class.getClassLoader().
			getResource("en-sent.bin")).getPath(); // The path to your sentence detection model
	static final double THRESHOLD_RAKE_VAL = 4.00;
	static final String OUTPUT_FILE_R = "C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_reviews.xlsx";
	static final String WORKSHEET_NAME_R = "iOS Optimum App Reviews";
	static final String OUTPUT_FILE_RS = "C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_NLP.xlsx";
	static final String WORKSHEET_NAME_RS = "Optimum App Reviews NLP";
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		List<Response> responseLst = new ArrayList<>();
		List<ReviewSentiment> revSentLst;

		// Fetch iOS reviews
		responseLst.addAll(IOSReviewsScrapper.getReviews(URL));
		int nIOSReviews = responseLst.size();
		log.info("Total iOS reviews fetched for Optimum Support app >>> {}", nIOSReviews);

		// Fetch Google Play reviews and concatenate
		responseLst.addAll(AndroidReviewsScrapper.webPageSurf());
		int nAndroidReviews = responseLst.size() - nIOSReviews;
		log.info("Total Android reviews fetched for Optimum Support app >>> {}", nAndroidReviews);

		// Sort by date; latest review first
		responseLst.sort(Comparator.comparing(Response::getDate).reversed());

		// Write sorted data to MS Excel file
		ResponseWriterExcel.writer(responseLst, null, OUTPUT_FILE_R, WORKSHEET_NAME_R);

		// Create a string builder for data analysis
		StringBuilder review = new StringBuilder();
		for (Response response : responseLst) {
			review.append(" " +response.getReview());
		}

		// NLP keyword extractions
		String[] stopWords = new SmartWords().getSmartWords(); 
		String[] stopPOS = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"}; 
		int minWordChar = 1;
		boolean shouldStem = true;
		String phraseDelims = "[-,.?():;\"!/]"; 
		RakeParams params = new RakeParams(stopWords, stopPOS, minWordChar, shouldStem, phraseDelims);

		// Create a RakeAlgorithm object
		RakeAlgorithm rakeAlg = new RakeAlgorithm(params, POS_TAGGER_URL, SENT_DETECT_URL);

		// Call the rake method
		Result result = rakeAlg.rake(review.toString());
		result = result.distinct();
		HashMap<String, Float> resultsMap = new HashMap<>();
		String[] resultKeywords = result.getFullKeywords();
		float[] resultScore = result.getScores();
		// Print the result
		for (int i=0; i < resultScore.length; i ++) {
			if (resultScore[i] > THRESHOLD_RAKE_VAL)
				resultsMap.put(resultKeywords[i], resultScore[i]);	    	
		}
		Map<String, Float> sortedMap = 
				resultsMap.entrySet().stream()
				.sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue,
						(e1, e2) -> e1, LinkedHashMap::new));
		sortedMap.forEach((key, value) -> log.info(String.format("[ %s ] [ %s ]", key, value)));
		
		revSentLst = AnalyzeEntitySentiment.computeSentimentIndex(responseLst);
		ResponseWriterExcel.writer(null, revSentLst, OUTPUT_FILE_RS, WORKSHEET_NAME_RS);
		System.exit(0);
	}

}
