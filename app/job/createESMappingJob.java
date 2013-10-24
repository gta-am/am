package job;

import com.tom.springutil.StopWatch;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import service.AMService;
import service.DefaultAMServiceImpl;
import utils.ElasticsearchHelper;

import java.util.concurrent.CountDownLatch;

/**
 * User: liuhongjiang
 * Date: 13-10-23
 * Time: 上午9:30
 * 功能说明:
 */
@OnApplicationStart(async = true)
public class CreateESMappingJob extends Job{

}
