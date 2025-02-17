package com.xxl.job.core.thread;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.util.SpringBeanUtils;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 17/3/2.
 */
public class ExecutorRegistryThread {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();
    public static ExecutorRegistryThread getInstance(){
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;
    public void start(final String appname, final String address){

        // valid
        if (appname==null || appname.trim().length()==0) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appname is null.");
            return;
        }
        if (XxlJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
            return;
        }

        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // registry
                while (!toStop) {
                    try {
                        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appname, address);
                        for (AdminBiz adminBiz: XxlJobExecutor.getAdminBizList()) {
                            try {
                                ReturnT<String> registryResult = adminBiz.registry(registryParam);
                                if (registryResult!=null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                    refreshAdmin(registryResult.getContent());
                                    registryResult = ReturnT.SUCCESS;
                                    logger.debug(">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                    break;
                                } else {
                                    logger.info(">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                }
                            } catch (Throwable e) {
                                logger.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);
                            }

                        }
                    } catch (Throwable e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                    try {
                        if (!toStop) {
                            TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                        }
                    } catch (Throwable e) {
                        if (!toStop) {
                            logger.warn(">>>>>>>>>>> xxl-job, executor registry thread interrupted, error msg:{}", e.getMessage());
                        }
                    }
                }

                // registry remove
                try {
                    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appname, address);
                    for (AdminBiz adminBiz: XxlJobExecutor.getAdminBizList()) {
                        try {
                            ReturnT<String> registryResult = adminBiz.registryRemove(registryParam);
                            if (registryResult!=null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ReturnT.SUCCESS;
                                logger.info(">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                break;
                            } else {
                                logger.info(">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            }
                        } catch (Throwable e) {
                            if (!toStop) {
                                logger.info(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, e);
                            }

                        }

                    }
                } catch (Throwable e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor registry thread destroy.");

            }
        });
        registryThread.setDaemon(true);
        registryThread.setName("xxl-job, executor ExecutorRegistryThread");
        registryThread.start();
    }

    private void refreshAdmin(String adminAddress){
        try {
            if(StringUtils.isEmpty(adminAddress)){
                return;
            }
            if(logger.isDebugEnabled()){
                logger.info(">>>>>>>>>>> xxl-job refresh admin address success, adminAddress:{}", adminAddress);
            }
            XxlJobExecutor xxlJobExecutor = SpringBeanUtils.getBean(XxlJobExecutor.class);
            if(xxlJobExecutor == null){
                return;
            }

            xxlJobExecutor.initAdminBizList(adminAddress, xxlJobExecutor.getAccessToken(), xxlJobExecutor.getTimeout());

            if(logger.isDebugEnabled()){
                List<AdminBiz> adminBizList = XxlJobExecutor.getAdminBizList();
                if(adminBizList == null){
                    logger.debug(">>>>>>>>>>> xxl-job refresh admin address success, new adminAddress is null}");
                    return;
                }
                for(AdminBiz adminBiz: adminBizList){
                    if(adminBiz instanceof AdminBizClient){
                        AdminBizClient client = (AdminBizClient) adminBiz;
                        logger.debug(">>>>>>>>>>> xxl-job refresh admin address success, new adminAddress:{}", client.getAddressUrl());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(">>>>>>>>>>> xxl-job, refreshAdmin error", e);
        }
    }

    public void toStop() {
        toStop = true;

        // interrupt and wait
        if (registryThread != null) {
            registryThread.interrupt();
            try {
                registryThread.join();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

}
