package org.xson.tangyuan.timer.job;

import org.xson.tangyuan.timer.xml.vo.TimerVo;

public interface CustomJob {

	void execute(TimerVo config);

}
