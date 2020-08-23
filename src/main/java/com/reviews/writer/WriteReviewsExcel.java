/**
 * @author Sridev Balakrishnan
 * @Purpose: Excel data writer
 * @Input: Array of Reviews
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
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.reviews.domain.Response;

public class WriteReviewsExcel {

	static final Logger log = LogManager.getLogger(WriteReviewsExcel.class.getName());

	static final String OUTPUT_FILE = "C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_reviews.xlsx";
	static final String WORKSHEET_NAME = "iOS Optimum App Reviews";
	static final String DATE_FORMAT = "MM-dd-yyyy";

	private WriteReviewsExcel() {
		throw new IllegalStateException("Utility class");
	}

	enum Columns { TYPE, DATE, RATING, USERNAME, TITLE, COMMENTS, LIKES }

	public static void writer(List<Response> reviews) throws IOException {

		try (
				// Create a Workbook
				Workbook workbook = new XSSFWorkbook()
				) {

			/*
			 * CreationHelper createS instances of various things like DataFormat,
			 * Hyperlink, RichTextString etc, in a format (XSSF) independent way
			 */
			CreationHelper createHelper = workbook.getCreationHelper();

			// Create a Sheet
			XSSFSheet sheet = (XSSFSheet) workbook.createSheet(WORKSHEET_NAME);
			sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);

			// Create a Font for styling header cells
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 12);
			headerFont.setColor(IndexedColors.BLUE.getIndex());

			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);

			// Create a Row
			Row headerRow = sheet.createRow(0);

			// Create column headings
			Columns[] arr = Columns.values();			
			for (Columns col : arr) { 	            
				Cell cell = headerRow.createCell(col.ordinal());
				cell.setCellValue(col.toString());
				cell.setCellStyle(headerCellStyle);
			} 

			// Create Cell Style for formatting Date
			CellStyle dateCellStyle = workbook.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));

			// Create Cell Style for formatting Date
			CellStyle wrapCellStyle = workbook.createCellStyle();
			wrapCellStyle.setWrapText(true);

			// Create Other rows and cells with review data
			int rowNum = 1;
			for (Response review : reviews) {
				Row row = sheet.createRow(rowNum++);

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

			// Resize all columns to fit the content size
			for (Columns col : arr) {
				if (col.ordinal() == 5) {
					sheet.setColumnWidth(5, 15000);
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
