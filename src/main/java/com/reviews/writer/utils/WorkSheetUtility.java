/**
 * @author SridevBalakrishnan
 * @purpose To format the workbook
 *
 */
package com.reviews.writer.utils;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class WorkSheetUtility {

	static final String DATE_FORMAT = "MM-dd-yyyy";

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
}
