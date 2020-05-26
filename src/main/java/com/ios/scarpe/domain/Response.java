/**
 * @author Sridev Balakrishnan
 * @Purpose: POJO class for Response Bean object
 * @param:
 * @return: Response
 * 
*/

package com.ios.scarpe.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

	SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
	SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
	
	private String userName;
	private Date date;
	private String title;
	private String review;
	private String rating;
		    
	@JsonProperty("attributes")
	private void unpackNameFromNestedObject(Map<String, String> attributes) {
		String sDate = null;
		userName = attributes.get("userName");
		try {
			Date dDate = inFormat.parse(attributes.get("date"));
			sDate = new SimpleDateFormat("MM-dd-yyyy").format(dDate);
			date = formatter.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
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
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n----- Review Information-----\n");
		sb.append("Name: " + getUserName() + "\n");
		sb.append("Date: " + getDate() + "\n");
		sb.append("Title: " + getTitle() + "\n");
		sb.append("Review: " + getReview() + "\n");
		sb.append("Rating: " + getRating() + "\n");
		sb.append("*****************************");
		return sb.toString();
	}
	
}
