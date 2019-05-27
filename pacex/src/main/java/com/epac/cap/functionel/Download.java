package com.epac.cap.functionel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.model.WFSAction;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.EventTarget;
import com.epac.cap.utils.LogUtils;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.topic.TopicMatcher;
import com.mycila.event.api.topic.Topics;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

@Component
public class Download extends WFSAction implements IAction {

	/**
	 * 
	 */
	
	@Autowired
	private NotificationService notificationService;
	
	private static final long serialVersionUID = 1L;
	private static ExecutorService executor;
	//public static final String FILE_REPO = "FILEREPOSITORY";
	//String part_Num ;

	@Override
	public boolean handle(Object parameter) {
		/*WFSProgressHandler.broadcast("DOWNLOADING");*/
		
		String ssePayload ="DOWNLOADING";			
		com.epac.cap.sse.beans.Event event=new com.epac.cap.sse.beans.Event(EventTarget.WFSProgress, false, null, ssePayload);
		notificationService.broadcast(event);
		
		List<String> parameters = (List<String>) parameter;
		String fileUrl = parameters.get(0);
		String localFilePath = parameters.get(1);
		String part_Num = parameters.get(2);
		LogUtils.start();

		StandardFileSystemManager manager = new StandardFileSystemManager();

		try {
			manager.init();
			LogUtils.debug(" " + Thread.currentThread().getId());

			FileObject localFile = manager.resolveFile(localFilePath);

			LogUtils.debug("downloading file thread : " + Thread.currentThread().getId() + " URL ==>" + fileUrl);
			// Create remote file object
			if (fileUrl.toLowerCase().startsWith("smb")) {
				getSMBFiles(fileUrl, localFilePath);
			} else {
				String connectionString = createConnectionString(fileUrl);

				FileObject remoteFile = manager.resolveFile(connectionString, createDefaultOptions(fileUrl));

				// Copy local file to sftp server
				localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
				LogUtils.debug("File successfully downloaded " + fileUrl);
				LogUtils.end();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			manager.close();
		}
		TryFire(part_Num);
		return true;
	}

	@Override
	public boolean TryFire(String partNb) {
		NDispatcher.getDispatcher().publish(Topics.topic("cap/events/download/done"), partNb);
		return true;
	}

	@Override
	public void subscribe() {
		TopicMatcher matcher = Topics.only("cap/events/download");
		NDispatcher.getDispatcher().subscribe(matcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws Exception {
				LogUtils.debug("Received: " + event.toString() + " calling the download method ");
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							handle(event.getSource());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				executor.execute(task);
			}
		});
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub

	}

	public Download() {
		executor = Executors.newCachedThreadPool();
		subscribe();
	}

	protected static String createConnectionString(String fileURL) throws UnsupportedEncodingException {
		String urlConnection = fileURL;

		String hostname = System.getProperty(ConfigurationConstants.SFTP_HOSTNAME);
		String username = URLEncoder.encode(System.getProperty(ConfigurationConstants.SFTP_USERNAME), "ISO-8859-1");
		String password = URLEncoder.encode(System.getProperty(ConfigurationConstants.SFTP_PASSWORD), "ISO-8859-1");
		System.out.println("******" + password + "******");

		if (fileURL.toLowerCase().startsWith("/FILE_TRANSFER")) {
			urlConnection = "sftp://" + username + ":" + password + "@" + hostname + urlConnection;
		} else if (fileURL.toLowerCase().startsWith("sftp://")) {
			urlConnection = "sftp://" + username + ":" + password + "@" + hostname
					+ urlConnection.replace(urlConnection.substring(0, urlConnection.indexOf("/FILE_TRANSFER")), "");
		} else if (fileURL.toLowerCase().startsWith("ftp://")) {
			urlConnection = "ftp://" + username + ":" + password + "@" + hostname
					+ urlConnection.replace(urlConnection.substring(0, urlConnection.indexOf("/FILE_TRANSFER")), "");
		}

		String urlConnection2 = urlConnection.replace(password, "*****");

		LogUtils.debug("File URL " + urlConnection2);

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

	protected static void getSMBFiles(String url, String localFilePath) throws IOException, PersistenceException {
		LogUtils.start();

		jcifs.Config.registerSmbURLHandler();
		String domain = (System.getProperty(ConfigurationConstants.SMB_DOMAIN) != null)
				? System.getProperty(ConfigurationConstants.SMB_DOMAIN) : "";
		String pass = (System.getProperty(ConfigurationConstants.SMB_PASSWORD) != null)
				? System.getProperty(ConfigurationConstants.SMB_PASSWORD) : "Ep@c1520$$";
		String user = (System.getProperty(ConfigurationConstants.SMB_USERNAME) != null)
				? System.getProperty(ConfigurationConstants.SMB_USERNAME) : "epac";
		LogUtils.debug(domain + " " + pass + " " + user);
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, pass);

		StaticUserAuthenticator authS = new StaticUserAuthenticator(domain, user, pass);

		FileSystemOptions opts = new FileSystemOptions();

		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, authS);
		SmbFile smbFile = new SmbFile(url, auth);
		LogUtils.debug(smbFile.getCanonicalPath());
		SmbFile remFile = new SmbFile(smbFile.getCanonicalPath(), auth);
		SmbFileInputStream smbfos = new SmbFileInputStream(remFile);
		OutputStream out = new FileOutputStream(localFilePath);
		byte[] b = new byte[8192];
		int n;
		while ((n = smbfos.read(b)) > 0) {
			out.write(b, 0, n);
		}
		smbfos.close();
		out.close();

	}

}
