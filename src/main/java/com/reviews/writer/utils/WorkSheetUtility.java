/**
 * @author SridevBalakrishnan
 * @purpose To format the workbook
 *
 */
package com.reviews.writer.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.reviews.domain.Response;
import com.reviews.domain.ReviewSentiment;


public class WorkSheetUtility {

	static final Logger log = LogManager.getLogger(WorkSheetUtility.class.getName());
	static final String DATE_FORMAT = "MM-dd-yyyy";
	static final String REVIEWS = "REVIEWS";
	static final String REVIEW_SENTIMENTS = "REVIEWSENTIMENTS";
	enum ColumnsReviews { TYPE, DATE, RATING, USERNAME, TITLE, COMMENTS, LIKES }
	enum ColumnsReviewSentiments { TYPE, DATE, RATING, USERNAME, COMMENTS, ENTITY, SALIENCE, MAGNITUDE, SCORE }

	/*
	 * CreationHelper createS instances of various things like DataFormat,
	 * Hyperlink, RichTextString etc, in a format (XSSF) independent way
	 */
	public CreationHelper createCreationHelper (Workbook workbook) {
		return workbook.getCreationHelper();
	}

	// Create a Sheet
	public XSSFSheet createSheet (Workbook workbook, String workSheetName) {
		XSSFSheet sheet = (XSSFSheet) workbook.createSheet(workSheetName);
		sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
		return sheet;
	}

	// Create a Font for styling header cells
	public Font createHeaderFont (Workbook workbook) {			
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setColor(IndexedColors.BLUE.getIndex());
		return headerFont;
	}

	// Create a CellStyle with the font
	public CellStyle createHeaderCellStyle (Workbook workbook, Font headerFont) {
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		return headerCellStyle;
	}

	// Create a Row
	public Row createHeaderRow (XSSFSheet sheet) {
		return sheet.createRow(0);
	}

	// Create Cell Style for formatting Date
	public CellStyle createDateCellStyle (Workbook workbook, CreationHelper createHelper) {
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));
		return dateCellStyle;
	}

	// Create Cell Style for wrapping text
	public CellStyle createWrapCellStyle (Workbook workbook) {
		CellStyle wrapCellStyle = workbook.createCellStyle();
		wrapCellStyle.setWrapText(true);
		return wrapCellStyle;
	}
	
	public Cell createColsHeadings (String sheetType, Row headerRow, CellStyle headerCellStyle) {
		
		Cell cell = null;
		
		if (sheetType.equalsIgnoreCase(REVIEWS)) {
			ColumnsReviews[] arr = ColumnsReviews.values();			
			for (ColumnsReviews col : arr) { 	            
				cell = headerRow.createCell(col.ordinal());
				cell.setCellValue(col.toString());
				cell.setCellStyle(headerCellStyle);
			} 
		}
		else {
			ColumnsReviewSentiments[] arr = ColumnsReviewSentiments.values();			
			for (ColumnsReviewSentiments col : arr) { 	            
				cell = headerRow.createCell(col.ordinal());
				cell.setCellValue(col.toString());
				cell.setCellStyle(headerCellStyle);
			} 
		}
		return cell;
	}
	
	// Create Other rows and cells with review data
	public Row createRows (String sheetType, XSSFSheet sheet, Response review, ReviewSentiment reviewSentiment, int rowNum, CellStyle dateCellStyle, CellStyle wrapCellStyle) {
		
		Row row;
		
		if (sheetType.equalsIgnoreCase(REVIEWS)) {
			row = sheet.createRow(rowNum);
			row.createCell(0).setCellValue(review.getType());

			Cell dateOfReviewCell = row.createCell(1);
			dateOfReviewCell.setCellValue(review.getDate());
			dateOfReviewCell.setCellStyle(dateCellStyle);		

			row.createCell(2).setCellValue(review.getRating());
			row.createCell(3).setCellValue(review.getUserName());

			Cell titleCell = row.createCell(4);
			titleCell.setCellValue(review.getTitle());
			titleCell.setCellStyle(wrapCellStyle);

			Cell commentsCell = row.createCell(5);
			commentsCell.setCellValue(review.getReview());
			commentsCell.setCellStyle(wrapCellStyle);

			row.createCell(6).setCellValue(review.getGoogleLikes());
		}
		else {
			row = sheet.createRow(rowNum);
			row.createCell(0).setCellValue(reviewSentiment.getType());

			Cell dateOfReviewCell = row.createCell(1);
			dateOfReviewCell.setCellValue(reviewSentiment.getDate());
			dateOfReviewCell.setCellStyle(dateCellStyle);		

			row.createCell(2).setCellValue(reviewSentiment.getRating());
			row.createCell(3).setCellValue(reviewSentiment.getUserName());

			Cell commentsCell = row.createCell(4);
			commentsCell.setCellValue(reviewSentiment.getComments());
			commentsCell.setCellStyle(wrapCellStyle);

			row.createCell(5).setCellValue(reviewSentiment.getEntity());				
			row.createCell(6).setCellValue(reviewSentiment.getSalience());				
			row.createCell(7).setCellValue(reviewSentiment.getMagnitude());
			row.createCell(8).setCellValue(reviewSentiment.getScore());
		}
		return row;
	}

	public XSSFSheet resize (String sheetType, XSSFSheet sheet) {
		if (sheetType.equalsIgnoreCase(REVIEWS)) {
			ColumnsReviews[] arr = ColumnsReviews.values();	
			for (ColumnsReviews col : arr) {
				if (col.ordinal() == 5) {
					sheet.setColumnWidth(5, 15000);
				}
				else
					sheet.autoSizeColumn(col.ordinal());
			}
		}
		else {
			ColumnsReviewSentiments[] arr = ColumnsReviewSentiments.values();	
			for (ColumnsReviewSentiments col : arr) {
				if (col.ordinal() == 4) {
					sheet.setColumnWidth(4, 15000);
				}
				else
					sheet.autoSizeColumn(col.ordinal());
			}
		}
		return sheet;
	}

	// Write the output to a file
	public void writerFile (Workbook workbook, String outputFile) throws IOException {	
		try (FileOutputStream fileOut = new FileOutputStream(outputFile);) {
			workbook.write(fileOut);
		} catch (FileNotFoundException e) {
			log.error("Exception while creating file {}", e.getMessage());
		}
	}
}
