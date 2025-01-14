package com.xxl.job.executor.mvc.controller;

import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.executor.XxlJobExecutor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class IndexController {

    @RequestMapping("/")
    @ResponseBody
    String index() {
        return "xxl job executor running.";
    }

    @RequestMapping("/trigger")
    @ResponseBody
    String index2( ) {
        ExecutorBizClient client = new ExecutorBizClient("http://127.0.0.1:8080/xxl-job-admin", "supconit");

        TriggerParam param = new TriggerParam();
        param.setExecutorHandler("shardingJobHandler");
        param.setExecutorParams("111,2222");
        ReturnT<String> run = client.run(param);
        return run.getMsg();
    }

}