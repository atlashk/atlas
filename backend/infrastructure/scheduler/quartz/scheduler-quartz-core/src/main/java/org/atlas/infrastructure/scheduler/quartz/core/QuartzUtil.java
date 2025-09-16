package org.atlas.infrastructure.scheduler.quartz.core;

import lombok.experimental.UtilityClass;
import org.quartz.JobExecutionContext;

@UtilityClass
public class QuartzUtil {

  public static String getJobName(JobExecutionContext context) {
    return context.getJobDetail().getKey().getName();
  }
}
