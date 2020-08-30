/**
 * @author Sridev Balakrishnan
 * @Purpose: Excel data writer
 * @Input: Array of EntitySentiment
 * @Output: Writes to an Excel file
 */
package com.reviews.writer;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.reviews.domain.ReviewSentiment;

import com.reviews.writer.utils.WorkSheetUtility;

public class WriteSentimentsExcel {
	
	static final Logger log = LogManager.getLogger(WriteSentimentsExcel.class.getName());
	
	static final String OUTPUT_FILE = "C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_NLP.xlsx";
	static final String WORKSHEET_NAME = "Optimum App Reviews NLP";
	static final String DATE_FORMAT = "MM-dd-yyyy";

	public WriteSentimentsExcel() {
		throw new IllegalStateException("Utility class");
	}

	enum Columns { TYPE, DATE, RATING, USERNAME, COMMENTS, ENTITY, SALIENCE, MAGNITUDE, SCORE }

	public static void writer(List<ReviewSentiment> reviews) throws IOException {
		
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
			XSSFSheet sheet = wsUtil.createSheet(workbook, WORKSHEET_NAME);

			// Create a Font for styling header cells
			Font headerFont = wsUtil.createHeaderFont(workbook);

			// Create a CellStyle with the font
			CellStyle headerCellStyle = wsUtil.createHeaderCellStyle(workbook, headerFont);

			// Create a Row
			Row headerRow = wsUtil.createHeaderRow (sheet);

			// Create column headings
			Columns[] arr = Columns.values();			
			for (Columns col : arr) { 	            
				Cell cell = headerRow.createCell(col.ordinal());
				cell.setCellValue(col.toString());
				cell.setCellStyle(headerCellStyle);
			} 

			// Create Cell Style for formatting Date
			CellStyle dateCellStyle = wsUtil.createDateCellStyle(workbook, createHelper);

			// Create Cell Style for wrapping text
			CellStyle wrapCellStyle = wsUtil.createWrapCellStyle(workbook);

			// Create Other rows and cells with review data
			int rowNum = 1;
			for (ReviewSentiment review : reviews) {
				Row row = sheet.createRow(rowNum++);
				
				row.createCell(0).setCellValue(review.getType());
				
				Cell dateOfReviewCell = row.createCell(1);
				dateOfReviewCell.setCellValue(review.getDate());
				dateOfReviewCell.setCellStyle(dateCellStyle);		
				
				row.createCell(2).setCellValue(review.getRating());
				row.createCell(3).setCellValue(review.getUserName());
				
				Cell commentsCell = row.createCell(4);
				commentsCell.setCellValue(review.getComments());
				commentsCell.setCellStyle(wrapCellStyle);
				
				row.createCell(5).setCellValue(review.getEntity());				
				row.createCell(6).setCellValue(review.getSalience());				
				row.createCell(7).setCellValue(review.getMagnitude());
				row.createCell(8).setCellValue(review.getScore());
			}

			// Resize all columns to fit the content size
			for (Columns col : arr) {
				if (col.ordinal() == 4) {
					sheet.setColumnWidth(4, 15000);
				}
				else
					sheet.autoSizeColumn(col.ordinal());
			}
			
			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream(OUTPUT_FILE);
			workbook.write(fileOut);
			fileOut.close();
		} catch (Exception ex) {
			log.error("Exception writing data to excel: ", ex);
		}
	}
}
