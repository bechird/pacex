package com.epac.imposition.cover;

import com.epac.imposition.bookblock.ComposerException;
import com.epac.imposition.config.Constants;
import com.epac.imposition.config.LogUtils;

public class ImpositionJob {
	
	
	
	private float 		yPosition;
	
	private float 		SheetWidth;
	
	private float 		SheetHeight;

	private float 		CropMarkWidth;
	
	private float 		CropMarkHeight;
	
	private float 		MeasureLinePos;
	
	private float 		FoldingEdgeXpos;
	
	private Cover 		Cover;
	
	private Datamatrix 	BinderDatamatrix;
	
	private Datamatrix 	TrimmerDatamatrix;
	
	private Datamatrix	LeftDatamatrix;
	
	private Datamatrix 	RightDatamatrix;

	private double calculatedThickness;
	
	
	
	
	public float getyPosition() {
		return yPosition;
	}

	public void setyPosition(float yPosition) {
		this.yPosition = yPosition;
	}

	public float getSheetWidth() {
		return SheetWidth;
	}

	public void setSheetWidth(float sheetWidth) {
		SheetWidth = sheetWidth;
	}

	public float getSheetHeight() {
		return SheetHeight;
	}

	public void setSheetHeight(float sheetHeight) {
		SheetHeight = sheetHeight;
	}
	


	public float getCropMarkWidth() {
		return CropMarkWidth;
	}

	public void setCropMarkWidth(float cropMarkWidth) {
		CropMarkWidth = cropMarkWidth;
	}

	public float getCropMarkHeight() {
		return CropMarkHeight;
	}

	public void setCropMarkHeight(float cropMarkHeight) {
		CropMarkHeight = cropMarkHeight;
	}

	public float getMeasureLinePos() {
		return MeasureLinePos;
	}

	public void setMeasureLinePos(float measureLinePos) {
		MeasureLinePos = measureLinePos;
	}

	public float getFoldingEdgeXpos() {
		return FoldingEdgeXpos;
	}

	public void setFoldingEdgeXpos(float foldingEdgeXpos) {
		FoldingEdgeXpos = foldingEdgeXpos;
	}

	public Cover getCover() {
		return Cover;
	}

	public void setCover(Cover cover) {
		Cover = cover;
	}

	public Datamatrix getBinderDatamatrix() {
		return BinderDatamatrix;
	}

	public void setBinderDatamatrix(Datamatrix binderDatamatrix) {
		BinderDatamatrix = binderDatamatrix;
	}

	public Datamatrix getLeftDatamatrix() {
		return LeftDatamatrix;
	}

	public void setLeftDatamatrix(Datamatrix leftDatamatrix) {
		LeftDatamatrix = leftDatamatrix;
	}

	public Datamatrix getRightDatamatrix() {
		return RightDatamatrix;
	}

	public void setRightDatamatrix(Datamatrix rightDatamatrix) {
		RightDatamatrix = rightDatamatrix;
	}

	public Datamatrix getTrimmerDatamatrix() {
		return TrimmerDatamatrix;
	}

	public void setTrimmerDatamatrix(Datamatrix trimmerDatamatrix) {
		TrimmerDatamatrix = trimmerDatamatrix;
	}
	
	
	

	/**
	 * Parse the config.properties file and create new Composition job to start the imposition
	 * @return CompositionJob
	 * @throws ComposerException
	 */
	public static ImpositionJob create () throws ComposerException {
		ImpositionJob job = new ImpositionJob();

		try{
			/*
			float sheetWidth			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_SHEET_WIDTH));
			float sheetHeight			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_SHEET_HEIGHT));
			
	
			float cropmark_xmargin		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CROPMARK_XMARGIN));
			float cropmark_ymargin		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CROPMARK_YMARGIN));
			float cropmark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CROPMARK_WIDTH));
			float cropmark_height		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CROPMARK_HEIGHT));
			*/
			
			float ypos					= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_YPOSITION));
			float measureline_pos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_MEASURELINE_XPOS));
			 
			
			float foldingedge = 0;
			
			if(System.getProperty(Constants.IMPOSITION_COVER_FOLDINGEDGE_XPOS) != null){
				foldingedge	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_FOLDINGEDGE_XPOS));
			}
			
			
			float datamatrix_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_WIDTH));
			float datamatrix_height		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_HEIGHT));
			
			float datamatrixB_xpos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_DATAMATRIX_BINDER_XPOS));
			float datamatrixB_ypos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_DATAMATRIX_BINDER_YPOS));;
			

			
			float datamatrixT_xpos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_DATAMATRIX_TRIMMER_XPOS));
			float datamatrixT_ypos		= 0;
			
			if(System.getProperty(Constants.IMPOSITION_COVER_DATAMATRIX_TRIMMER_YPOS) != null)
				datamatrixT_ypos = Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_DATAMATRIX_TRIMMER_YPOS));
			
			/*
			float datamatrixL_xpos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_LEFT_XPOS));
			float datamatrixL_ypos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_LEFT_YPOS));
			
			float datamatrixR_xpos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_RIGHT_XPOS));
			float datamatrixR_ypos		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_RIGHT_YPOS));
			 */

			job.setyPosition(ypos);
			/*
			job.setSheetWidth(sheetWidth);
			job.setSheetHeight(sheetHeight);
			*/
			job.setFoldingEdgeXpos(foldingedge);
			/*
			job.setCropMarkXmargin(cropmark_xmargin);
			job.setCropMarkYmargin(cropmark_ymargin);
			job.setCropMarkWidth(cropmark_width);
			job.setCropMarkHeight(cropmark_height);
			*/
			job.setMeasureLinePos(measureline_pos);
	
			Datamatrix datamatrixB = new Datamatrix(datamatrix_width, datamatrix_height, datamatrixB_xpos, datamatrixB_ypos, "");
			Datamatrix datamatrixT = new Datamatrix(datamatrix_width, datamatrix_height, datamatrixT_xpos, datamatrixT_ypos, "");
			
			//Datamatrix datamatrixL = new Datamatrix(datamatrix_width, datamatrix_height, datamatrixL_xpos, datamatrixL_ypos, "");
			//Datamatrix datamatrixR = new Datamatrix(datamatrix_width, datamatrix_height, datamatrixR_xpos, datamatrixR_ypos, "");

			job.setBinderDatamatrix(datamatrixB);
			job.setTrimmerDatamatrix(datamatrixT);
			//job.setLeftDatamatrix(datamatrixL);
			//job.setRightDatamatrix(datamatrixR);
			

		}catch(Exception e){
			LogUtils.error("Error while parsing values from config file... ",e);
			throw new ComposerException("Error while parsing values from config file... "+e.getMessage());
		}

		return job;
	}

	public void setCalculatedThickness(double calculatedThickness) {
		this.calculatedThickness = calculatedThickness;
	}
	
	public double getCalculatedThickness() {
		return this.calculatedThickness;
	}
	
}
