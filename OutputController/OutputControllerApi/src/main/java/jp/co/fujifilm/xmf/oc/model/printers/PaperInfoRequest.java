package jp.co.fujifilm.xmf.oc.model.printers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PaperInfoRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaperInfoRequest {

    private int width;

    private int length;

    private int feedLength;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getFeedLength() {
		return feedLength;
	}

	public void setFeedLength(int feedLength) {
		this.feedLength = feedLength;
	}

}
