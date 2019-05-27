package jp.co.fujifilm.xmf.oc.error;

/**
 * Service層->Resource層用の例外基本形
 * Exception Base for Service -> Resource
 * @author UneTakao
 * @since 2015/01/19
 */
public class AppException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7591644826505119667L;

	/**
	 * contains redundantly the HTTP status of the response sent back to the client in case of error, so that
	 * the developer does not have to look into the response headers. If null a default
	 */
	Integer status;

	/** application specific error code */
	String code;

	/** application specific error sub code */
	String subCode;

	/**
	 * Constructor
	 * @param status HTTP Status Code
	 * @param code Error detial code
	 * @param message Error message
	 */
	public AppException(int status, String code, String subCode, String message) {
		super(message);
		this.status = status;
		this.code = code;
		this.subCode = subCode;
	}

	/**
	 * コンストラクタ。
	 * @x.history 2015/01/19 FFS/UneTakao:create
	 */
	public AppException() { }

	/**
	 * (getter)
	 * @return
	 * @x.history 2015/01/19 FFS/UneTakao:create
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * (setter)
	 * @param status
	 * @x.history 2015/01/19 FFS/UneTakao:create
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * (getter)
	 * @return
	 * @x.history 2015/01/19 FFS/UneTakao:create
	 */
	public String getCode() {
		return code;
	}

	/**
	 * (setter)
	 * @param code
	 * @x.history 2015/01/19 FFS/UneTakao:create
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * (getter)
	 * @return
	 * @x.history 2015/05/29 FFS/MitsutoshiTaira:create
	 */
	public String getSubCode() {
		return subCode;
	}

	/**
	 * (setter)
	 * @param subCode
	 * @x.history 2015/05/29 FFS/MitsutoshiTaira:create
	 */
	public void setSubCode(String subCode) {
		this.subCode = subCode;
	}
}
