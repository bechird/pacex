package jp.co.fujifilm.xmf.oc.resources.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jp.co.fujifilm.xmf.oc.model.jobs.Job;

/**
 * Jobオブジェクトのテスト
 * @author UneTakao
 * @since 2015/03/04
 */
public class JobTest {

	/**
	 * データの入出力でデータに変化が無いことを確認
	 * @x.history 2015/03/03 FFS/UneTakao:新規作成
	 */
	@Test
	public void test_GetSet() {
		String jobId = "1000";


		Job jobInfo = new Job();
	//TODO skeiJob変更したから修正	jobInfo.setJobId(jobId);

		//TODO skeiJob変更したから修正	assertTrue(jobInfo.getJobId().equals(jobId));
	}


}
