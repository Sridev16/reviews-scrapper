/**
 * @author Sridev Balakrishnan
 * @Purpose: Excel data writer
 * @Input: Array of Reviews
 * @Output: Writes to an Excel file
 */
package com.reviews.writer;

import java.io.IOException;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.reviews.domain.Response;
import com.reviews.domain.ReviewSentiment;

import com.reviews.writer.utils.WorkSheetUtility;

public class ResponseWriterExcel {

	static final Logger log = LogManager.getLogger(ResponseWriterExcel.class.getName());
	static final String DATE_FORMAT = "MM-dd-yyyy";
	static final String REVIEWS = "REVIEWS";
	static final String REVIEW_SENTIMENTS = "REVIEWSENTIMENTS";

	private ResponseWriterExcel() {
		throw new IllegalStateException("Utility class");
	}

	enum ColumnsReviews { TYPE, DATE, RATING, USERNAME, TITLE, COMMENTS, LIKES }
	enum ColumnsReviewSentiments { TYPE, DATE, RATING, USERNAME, COMMENTS, ENTITY, SALIENCE, MAGNITUDE, SCORE }

	public static void writer(List<Response> reviews, List<ReviewSentiment> sentiments, String outputFile, String wsName) throws IOException {

		WorkSheetUtility wsUtil = new WorkSheetUtility();

		try (
				// Create a Workbook
				Workbook workbook = new XSSFWorkbook()
				) {

			/*
			 * CreationHelper createS instances of various things like DataFormat,
			 * Hyperlink, RichTextString etc, in a format (XSSF) independent way
			 */
			CreationHelper createHelper = wsUtil.createCreationHelper(workbook);

			// Create a Sheet
			XSSFSheet sheet = wsUtil.createSheet(workbook, wsName);

			// Create a Font for styling header cells
			Font headerFont = wsUtil.createHeaderFont(workbook);

			// Create a CellStyle with the font
			CellStyle headerCellStyle = wsUtil.createHeaderCellStyle(workbook, headerFont);

			// Create a Row
			Row headerRow = wsUtil.createHeaderRow (sheet);

			// Create column headings
			if (reviews != null && !reviews.isEmpty()) 
				wsUtil.createColsHeadings(REVIEWS, headerRow, headerCellStyle);
			else
				wsUtil.createColsHeadings(REVIEW_SENTIMENTS, headerRow, headerCellStyle);	

				// Create Cell Style for formatting Date
				CellStyle dateCellStyle = wsUtil.createDateCellStyle(workbook, createHelper);

				// Create Cell Style for wrapping text
				CellStyle wrapCellStyle = wsUtil.createWrapCellStyle(workbook);

				// Create Other rows and cells with review data
				int rowNum = 1;
				if (reviews != null && !reviews.isEmpty()) {
					for (Response review : reviews) {
						wsUtil.createRows(REVIEWS, sheet, review, null, rowNum++, dateCellStyle, wrapCellStyle);					
					}
				}
				else {
					for (ReviewSentiment review : sentiments) {
						wsUtil.createRows(REVIEW_SENTIMENTS, sheet, null, review, rowNum++, dateCellStyle, wrapCellStyle);	
					}
				}

				// Resize all columns to fit the content size
				if (reviews != null && !reviews.isEmpty()) {
					wsUtil.resize(REVIEWS, sheet);
				}
				else {
					wsUtil.resize(REVIEW_SENTIMENTS, sheet);
				}

				// Write the output to a file
				wsUtil.writerFile(workbook, outputFile);
			} catch (Exception ex) {
				log.error("Exception writing data to excel: ", ex);
			}
		}
	}
