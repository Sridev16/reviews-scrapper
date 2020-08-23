/**
 * @author Sridev Balakrishnan
 * @Purpose: POJO class for Entity Sentiment bean
*/

package com.reviews.domain;

import java.util.Date;

public class ReviewSentiment {

	public ReviewSentiment() {
		// empty constructor
	}

	public ReviewSentiment (String type, Date date, String rating, String userName, 
			String comments, String entity, double salience, double magnitude, double score) {
		this.type = type;
		this.date = date;
		this.rating = rating;
		this.userName = userName;
		this.comments = comments;
		this.entity = entity;
		this.salience = salience;
		this.magnitude = magnitude;
		this.score = score;
	}
	
	private String type;
	private Date date;
	private String rating;
	private String userName;
	private String comments;
	private String entity;
	private double salience;
	private double magnitude;
	private double score;

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the rating
	 */
	public String getRating() {
		return rating;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(String rating) {
		this.rating = rating;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the entity
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	/**
	 * @return the salience
	 */
	public double getSalience() {
		return salience;
	}

	/**
	 * @param salience the salience to set
	 */
	public void setSalience(double salience) {
		this.salience = salience;
	}

	/**
	 * @return the magnitude
	 */
	public double getMagnitude() {
		return magnitude;
	}

	/**
	 * @param magnitude the magnitude to set
	 */
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n----- Sentiment Information-----\n");
		sb.append("Type: " + getType() + "\n");
		sb.append("Date: " + getDate() + "\n");
		sb.append("Rating: " + getRating() + "\n");
		sb.append("Username: " + getUserName() + "\n");
		sb.append("Entity: " + getEntity() + "\n");
		sb.append("Salience: " + getSalience() + "\n");
		sb.append("Magnitude: " + getMagnitude() + "\n");
		sb.append("Score: " + getScore() + "\n");		
		sb.append("*****************************");
		return sb.toString();
	}

}
