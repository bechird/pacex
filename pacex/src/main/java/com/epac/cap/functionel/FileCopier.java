package com.epac.cap.functionel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.epac.cap.utils.LogUtils;

public class FileCopier {

	public FileCopier() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		File srcFolder = new File("/in/TEST");
		File destFolder = new File("/out/TEST");

    	//make sure source exists
    	if(!srcFolder.exists()){

           System.out.println("Directory does not exist.");
           
           //just exit
           System.exit(0);

        }else{

        	deleteDir(destFolder);
           try{
        	copyFile(srcFolder,destFolder);
           }catch(IOException e){
        	e.printStackTrace();
        	//error, just exit
                System.exit(0);
           }
        }

    	System.out.println("Done");
    }

    static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
    
    public static void copyFile(File src, File dest)
    	throws IOException{

    	if(src.isDirectory()){

    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();
    		  // System.out.println("Directory copied from "+ src + "  to " + dest);
   			LogUtils.debug("Directory copied from "
                    + src + "  to " + dest);

    		}
			else {
	        	deleteDir(dest);
	   		   dest.mkdir();
    		  // System.out.println("Directory copied from " + src + "  to " + dest);
   			LogUtils.debug("Directory copied from "
                    + src + "  to " + dest);
				}

    		//list all the directory contents
    		String files[] = src.list();

    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   copyFile(srcFile,destFile);
    		}

    	}else{
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
    	        OutputStream out = new FileOutputStream(dest);

    	        byte[] buffer = new byte[1024];

    	        int length;
    	        //copy the file content in bytes
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }

    	        in.close();
    	        out.close();
    	        System.out.println("File copied from " + src + " to " + dest);
    	}
    
}}
