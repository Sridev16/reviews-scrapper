/**
 * @author Sridev Balakrishnan
 *
 */
package com.ios.scrape;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

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

import com.ios.scarpe.domain.Response;

public class WriteToExcel {

	private WriteToExcel() {
		throw new IllegalStateException("Utility class");
	}

	enum Columns { DATE, RATING, USERNAME, TITLE, COMMENTS }

	static void writer(List<Response> reviews) throws IOException {

		try (
				// Create a Workbook
				Workbook workbook = new XSSFWorkbook() // new HSSFWorkbook() for generating `.xls` file
		) {

			/*
			 * CreationHelper helps us create instances of various things like DataFormat,
			 * Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way
			 */
			CreationHelper createHelper = workbook.getCreationHelper();

			// Create a Sheet
			XSSFSheet sheet = (XSSFSheet) workbook.createSheet("iOS Optimum App Reviews");
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
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("MM-dd-yyyy"));

			// Create Other rows and cells with employees data
			int rowNum = 1;
			for (Response review : reviews) {
				Row row = sheet.createRow(rowNum++);

				Cell dateOfReviewCell = row.createCell(0);
				dateOfReviewCell.setCellValue(review.getDate());
				dateOfReviewCell.setCellStyle(dateCellStyle);
				
				row.createCell(1).setCellValue(review.getRating());
				row.createCell(2).setCellValue(review.getUserName());
				row.createCell(3).setCellValue(review.getTitle());
				row.createCell(4).setCellValue(review.getReview());

			}

			// Resize all columns to fit the content size
			for (Columns col : arr)
				sheet.autoSizeColumn(col.ordinal());
			
			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream(
					"C:\\Users\\SridevBalakrishnan\\Desktop\\optimum_support_ios_reviews.xlsx");
			workbook.write(fileOut);
			fileOut.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
