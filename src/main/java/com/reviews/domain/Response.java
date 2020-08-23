/**
 * @author Sridev Balakrishnan
 * @Purpose: POJO class for Response Bean object
*/

package com.reviews.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author SridevBalakrishnan
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
	
	static final Logger log = LogManager.getLogger(Response.class.getName());
	
	SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
	SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
	
	private String userName;
	private Date date;
	private String title;
	private String review;
	private String rating;
	private String googleLikes;
	private String type;
		    
	@JsonProperty("attributes")
	private void unpackNameFromNestedObject(Map<String, String> attributes) {
		String sDate = null;
		userName = attributes.get("userName");
		try {
			Date dDate = inFormat.parse(attributes.get("date"));
			sDate = new SimpleDateFormat("MM-dd-yyyy").format(dDate);
			date = formatter.parse(sDate);
		} catch (ParseException e) {
			log.error("Exception when formatting date {}", date, e);
		}
		title = attributes.get("title");
		review = attributes.get("review");
		rating = attributes.get("rating");
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReview() {
		return review.replace("’", "'");
	}

	public void setReview(String review) {
		this.review = review.replace("’", "'");
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	/**
	 * @return the googleLikes
	 */
	public String getGoogleLikes() {
		return googleLikes;
	}

	/**
	 * @param googleLikes the googleLikes to set
	 */
	public void setGoogleLikes(String googleLikes) {
		this.googleLikes = googleLikes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n----- Review Information-----\n");
		sb.append("Name: " + getUserName() + "\n");
		sb.append("Date: " + getDate() + "\n");
		sb.append("Title: " + getTitle() + "\n");
		sb.append("Review: " + getReview().replace("’", "'") + "\n");
		sb.append("Rating: " + getRating() + "\n");
		sb.append("Likes: " + getGoogleLikes() + "\n");		
		sb.append("*****************************");
		return sb.toString();
	}
	
}
