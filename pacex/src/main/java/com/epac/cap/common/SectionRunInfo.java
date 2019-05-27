package com.epac.cap.common;

import java.io.File;
import java.util.List;

public class SectionRunInfo {

	File genFile;
	List<String> errors;

	public SectionRunInfo() {
		// TODO Auto-generated constructor stub
	}

	public File getGenFile() {
		return genFile;
	}

	public void setGenFile(File genFile) {
		this.genFile = genFile;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

}
