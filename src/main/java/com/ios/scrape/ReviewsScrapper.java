/**
 * @author Sridev Balakrishnan
 * @Purpose: To read Optimum Support App reviews from apple site
 * @Input:
 * @Output: Sorts the data in descending order of date and stores them in an XLSX file
 * 
 */

package com.ios.scrape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.ios.scarpe.domain.Response;

import io.github.crew102.rapidrake.RakeAlgorithm;
import io.github.crew102.rapidrake.data.SmartWords;
import io.github.crew102.rapidrake.model.RakeParams;
import io.github.crew102.rapidrake.model.Result;

public class ReviewsScrapper {
	
	static final Logger log = Logger.getLogger(ReviewsScrapper.class.getName());
	
	static final String URL = "https://amp-api.apps.apple.com/v1/catalog/us/apps/1234273194/reviews?l=en-US&platform=web&additionalPlatforms=appletv%2Cipad%2Ciphone%2Cmac";
	static final String FIRST_PART_URL = "https://amp-api.apps.apple.com";
	static final String LAST_PART_URL = "&platform=web&additionalPlatforms=appletv%2Cipad%2Ciphone%2Cmac";
	static final String HTTP_METHOD = "GET";
	static final String AUTH_PROP = "Authorization";
	static final String AUTH_KEY = "Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IldlYlBsYXlLaWQifQ.eyJpc3MiOiJBTVBXZWJQbGF5IiwiaWF0IjoxNTg4OTEyMDA1LCJleHAiOjE2MDQ0NjQwMDV9.TWF75lk72kCfAeUn9Hv1GYvD3InqFafYLcsTo4-2hxbcYRqnDZuUaqLAxLziIC-mgE7bNOq8goC_LCh7kvxacw";
	static final String JSON_NODE_NEXT = "next";
	static final String JSON_NODE_DATA = "data";
	static final String JSON_SEARCH_DATA = "offset";
	static final String STR_REPLACE_FROM = "\"";
	static final String STR_REPLACE_TO = "";
    static final String POS_TAGGER_URL = Objects.requireNonNull(ReviewsScrapper.class.getClassLoader().
    									getResource("en-pos-maxent.bin")).getPath(); // The path to your POS tagging model
    static final String SENT_DETECT_URL = Objects.requireNonNull(ReviewsScrapper.class.getClassLoader().
										getResource("en-sent.bin")).getPath(); // The path to your sentence detection model
    static final double THRESHOLD_RAKE_VAL = 4.00;
	static ArrayList<Response> responses = new ArrayList<>();


	public static void main(String... args) throws IOException {
		
		// Fetch reviews from iOS site
		getReviews(URL);
		
		// Sort by date; latest review first
		responses.sort(Comparator.comparing(Response::getDate).reversed());
		
		// Print sorted data to MS Excel file
		WriteToExcel.writer(responses);
		
		// Create a string builder for data analysis
		StringBuilder review = new StringBuilder();
		for (Response response : responses) {
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
	    sortedMap.forEach((key, value) -> log.info("[ " + key + " ] [" + value +"]"));	    
	}

	static void getReviews(String strURL) throws IOException {

		JsonNode jsonURL = null;
		List<Response> reviews = new ArrayList<>();

		try {
			URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(HTTP_METHOD);
			connection.setDoOutput(true);
			connection.setRequestProperty(AUTH_PROP, AUTH_KEY);

			StringBuilder strResp = service(connection);

			ObjectMapper objectMapper = new ObjectMapper();
			jsonURL = objectMapper.readTree(strResp.toString()).path(JSON_NODE_NEXT);
			JsonNode jsonNode = objectMapper.readTree(strResp.toString()).path(JSON_NODE_DATA);
			CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class,
					Response.class);
			reviews = objectMapper.reader(collectionType).readValue(jsonNode);
			reviews.forEach(x -> responses.add(x));
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info(jsonURL);
		if (jsonURL != null && jsonURL.toString().contains(JSON_SEARCH_DATA)) {
			String returnURL = jsonURL.toString().replace(STR_REPLACE_FROM, STR_REPLACE_TO);
			if (returnURL != null) {
				getReviews(FIRST_PART_URL + returnURL + LAST_PART_URL);
			}
		}
	}

	public static StringBuilder service(HttpURLConnection connection) {

		StringBuilder strResp = new StringBuilder();

		try (InputStream content = connection.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(content))) {
			String line;
			strResp = new StringBuilder();
			while ((line = in.readLine()) != null) {
				strResp.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResp;
	}
}
