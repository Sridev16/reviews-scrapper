/**
 * @author Sridev Balakrishnan
 * @Purpose: To read Optimum Support App reviews from apple site
 * @Input:
 * @Output: List of Android user reviews sorted by date
 * */

package com.reviews.scrape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.reviews.domain.Response;

public class IOSReviewsScrapper {

	static final Logger log = LogManager.getLogger(IOSReviewsScrapper.class.getName());

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
	static final String POS_TAGGER_URL = Objects.requireNonNull(IOSReviewsScrapper.class.getClassLoader().
			getResource("en-pos-maxent.bin")).getPath(); // The path to your POS tagging model
	static final String SENT_DETECT_URL = Objects.requireNonNull(IOSReviewsScrapper.class.getClassLoader().
			getResource("en-sent.bin")).getPath(); // The path to your sentence detection model
	static final double THRESHOLD_RAKE_VAL = 4.00;
	static List<Response> responses = new ArrayList<>();

	public static void main(String... args) throws IOException {

		// Fetch reviews from iOS site
		getReviews(URL);

		// Sort by date; latest review first
		responses.sort(Comparator.comparing(Response::getDate).reversed());

		System.exit(0);
	}

	@SuppressWarnings("deprecation")
	static List<Response> getReviews(String strURL) throws IOException {

		JsonNode jsonURL = null;
		List<Response> reviews = new ArrayList<>();

		try {
			URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(HTTP_METHOD);
			connection.setDoOutput(true);
			connection.setRequestProperty(AUTH_PROP, AUTH_KEY);
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			
			StringBuilder strResp = service(connection);

			ObjectMapper objectMapper = new ObjectMapper();
			jsonURL = objectMapper.readTree(strResp.toString()).path(JSON_NODE_NEXT);
			JsonNode jsonNode = objectMapper.readTree(strResp.toString()).path(JSON_NODE_DATA);
			CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(
					List.class, Response.class);
			reviews = objectMapper.reader(collectionType).readValue(jsonNode);
			reviews.forEach(x -> responses.add(x));
			reviews.forEach(x -> x.setType("iOS"));
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		if (jsonURL != null && jsonURL.toString().contains(JSON_SEARCH_DATA)) {
			String returnURL = jsonURL.toString().replace(STR_REPLACE_FROM, STR_REPLACE_TO);
			if (returnURL != null) {
				getReviews(FIRST_PART_URL + returnURL + LAST_PART_URL);
			}
		}
		return responses;
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
			log.error("Exception for {}", strResp, e);
		}
		return strResp;
	}
}
