package com.epac.cap.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.utils.LogUtils;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class PDFFilesValidator {

	@Autowired
	private static LookupDAO lookupDAO;
	
	public static String localFilePath = "/etc/epac";
	public static final String FILE_REPO = "FILEREPOSITORY";

	public static void download(String fileUrl, String localFilePath, Part part) throws Exception {
		LogUtils.start();
		StandardFileSystemManager manager = new StandardFileSystemManager();

		try {
			manager.init();

			FileObject localFile = manager.resolveFile(localFilePath);
			
			LogUtils.debug("downloading file " + fileUrl);
			// Create remote file object
			if (fileUrl.toLowerCase().startsWith("smb")) {
				GetSMBFiles(part);
			} else {
				String connectionString = createConnectionString(fileUrl);

				FileObject remoteFile = manager.resolveFile(connectionString, createDefaultOptions(fileUrl));

				// Copy local file to sftp server
				localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
				LogUtils.debug("File successfully downloaded " + fileUrl);
				LogUtils.end();
			}	
			
		} catch (FileSystemException e) {
			LogUtils.error("File download failed: " + fileUrl, e);
			LogUtils.end();
			throw e;
		} catch (UnsupportedEncodingException e) {
			LogUtils.error("File download failed: " + fileUrl, e);
			LogUtils.end();
			throw e;
		} finally {
			manager.close();
		}
	}
	
	protected static String createConnectionString(String fileURL) throws UnsupportedEncodingException {
		String urlConnection = fileURL;

		String hostname = System.getProperty(ConfigurationConstants.SFTP_HOSTNAME);
		String username = URLEncoder.encode(System.getProperty(ConfigurationConstants.SFTP_USERNAME), "ISO-8859-1");
		String password = URLEncoder.encode(System.getProperty(ConfigurationConstants.SFTP_PASSWORD), "ISO-8859-1");
		System.out.println("******"+ password +"******");

		if (fileURL.toLowerCase().startsWith("/incoming")) {
			urlConnection = "sftp://" + username + ":" + password + "@" + hostname + urlConnection;
		} else if (fileURL.toLowerCase().startsWith("sftp://")) {
			urlConnection = "sftp://" + username + ":" + password + "@" + hostname
					+ urlConnection.replace(urlConnection.substring(0, urlConnection.indexOf("/incoming")), "");
		}

		String urlConnection2 = urlConnection.replace(password, "*****");

		LogUtils.debug("File URL "+urlConnection2);

		return urlConnection;
	}

	protected static FileSystemOptions createDefaultOptions(String fileUrl) throws FileSystemException {
		// Create SFTP/FTP/HTTP options
		FileSystemOptions opts = new FileSystemOptions();

		if (fileUrl.toLowerCase().startsWith("ftp://")) {

			// Root directory set to user home
			FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);

			// Timeout is count by Milliseconds
			FtpFileSystemConfigBuilder.getInstance().setDataTimeout(opts, 10000);
		}
		if (fileUrl.toLowerCase().startsWith("sftp://")) {

			// SSH Key checking
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");

			// Root directory set to user home
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);

			// Timeout is count by Milliseconds
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 100000000);
		}
		if (fileUrl.toLowerCase().startsWith("http://")) {

			// Root directory set to user home
			HttpFileSystemConfigBuilder.getInstance().setMaxConnectionsPerHost(opts, 0);

			// Timeout is count by Milliseconds
			HttpFileSystemConfigBuilder.getInstance();
		}

		return opts;
	}

	protected static void GetSMBFiles(Part part) throws IOException, PersistenceException {
		LogUtils.start();
		
		File filePath = generateFilePath(part);
		jcifs.Config.registerSmbURLHandler();
		String domain = (System.getProperty(ConfigurationConstants.SMB_DOMAIN) != null) ? System.getProperty(ConfigurationConstants.SMB_DOMAIN): "";
		String pass = (System.getProperty(ConfigurationConstants.SMB_PASSWORD) != null) ? System.getProperty(ConfigurationConstants.SMB_PASSWORD): "Ep@c1520$$";
		String user = (System.getProperty(ConfigurationConstants.SFTP_USERNAME) != null) ? System.getProperty(ConfigurationConstants.SFTP_USERNAME): "epac";
		LogUtils.debug(domain + " " + pass + " " + user);
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, pass);

		StaticUserAuthenticator authS = new StaticUserAuthenticator(domain, user, pass);

		FileSystemOptions opts = new FileSystemOptions();

		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, authS);

		String url = "smb://192.168.1.165/EPAC/Test Books/incoming/Cindy_documents/csvFiles/";
		SmbFile smbFile = new SmbFile(url, auth);
		FileSystemManager fs = VFS.getManager();
		String[] files = smbFile.list();
		for (String file : files)
			System.out.println(file.toString());

		for (String file : files) {
			SmbFile remFile = new SmbFile(url + file, auth);
			SmbFileInputStream smbfos = new SmbFileInputStream(remFile);
			OutputStream out = new FileOutputStream("/Users/islem/Desktop/Cengage Project/Test/" + file);
			byte[] b = new byte[8192];
			int n;
			while ((n = smbfos.read(b)) > 0) {
				out.write(b, 0, n);
			}
			smbfos.close();
			out.close();
		}

	}
	
	private static File generateFilePath(Part part) throws PersistenceException{
		File result = null;
		LookupItem item = lookupDAO.read(FILE_REPO, Preference.class);
		if(item != null ){
			result = new File(item.getName());//The value of the FILEREPOSITORY Preference 
		}else{
			result = new File(System.getProperty(ConfigurationConstants.DIR_DOWNLOAD));
		}
		if(result.exists()){
			result = new File(result.getPath()+File.separator+part.getIsbn()+"_"+part.getPartNum()+File.separator+part.getCategory());
			result.mkdirs();
		}
		return result;
	}

}
