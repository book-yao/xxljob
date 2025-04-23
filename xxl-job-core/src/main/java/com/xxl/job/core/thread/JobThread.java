package com.xxl.job.core.thread;

import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.PriorityParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.util.GsonTool;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * handler thread
 * @author xuxueli 2016-1-16 19:52:47
 */
public class JobThread extends Thread{
	private static Logger logger = LoggerFactory.getLogger(JobThread.class);

	private int jobId;
	private IJobHandler handler;
	private LinkedBlockingDeque<TriggerParam> triggerQueue;
	private Set<Long> triggerLogIdSet;		// avoid repeat trigger for the same TRIGGER_LOG_ID

	private volatile boolean toStop = false;
	private volatile String stopReason;

//    private volatile boolean running = false;    // if running job
	private volatile int idleTimes = 0;			// idle times
	/**
	 * 多线程执行状态隔离，防止最后一个任务运行中，其他线程达到最大次数退出
	 */
	private final List<AtomicBoolean> multiRunningThreadList;
	/**
	 * 内部线程池管理
	 */
	private ThreadPoolExecutor threadPool;

	public JobThread(int jobId, IJobHandler handler) {
		this.jobId = jobId;
		this.handler = handler;
		this.triggerQueue = new LinkedBlockingDeque<TriggerParam>();
		this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<Long>());

		// assign job thread name
		this.setName("xxl-job, JobThread-"+jobId+"-"+System.currentTimeMillis());
		int executeThreadNum = handler.executeThreadNum();
		if(executeThreadNum > 1) {
			threadPool = new ThreadPoolExecutor(
					executeThreadNum,
					executeThreadNum * 2,
					60L,
					TimeUnit.SECONDS,
					new SynchronousQueue<>(),
					new DefaultThreadFactory("xxl-job, jobThread  pool-jobId[" + jobId + "]"),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}
		multiRunningThreadList = new ArrayList<>(executeThreadNum);
		for (int i = 0; i < executeThreadNum; i++) {
			multiRunningThreadList.add(new AtomicBoolean(false));
		}
	}
	public IJobHandler getHandler() {
		return handler;
	}

    /**
     * new trigger to queue
     *
     * @param triggerParam
     * @return
     */
	public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
		// avoid repeat
		if (triggerLogIdSet.contains(triggerParam.getLogId())) {
			logger.info(">>>>>>>>>>> repeate trigger job, logId:{}", triggerParam.getLogId());
			return new ReturnT<String>(ReturnT.FAIL_CODE, "repeate trigger job, logId:" + triggerParam.getLogId());
		}

		triggerLogIdSet.add(triggerParam.getLogId());
		try{
			String executorParams = triggerParam.getExecutorParams();
			if(!StringUtils.isEmpty(executorParams)){
				PriorityParam priorityParam = GsonTool.fromJson(executorParams, PriorityParam.class);
				if(priorityParam != null && priorityParam.isPriority()){
					triggerQueue.addFirst(triggerParam);
					return ReturnT.SUCCESS;
				}
			}
		}catch (Exception e){
			logger.error(e.getMessage(), e);
		}

		triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
	}

    /**
     * kill job thread
     *
     * @param stopReason
     */
	public void toStop(String stopReason) {
		/**
		 * Thread.interrupt只支持终止线程的阻塞状态(wait、join、sleep)，
		 * 在阻塞出抛出InterruptedException异常,但是并不会终止运行的线程本身；
		 * 所以需要注意，此处彻底销毁本线程，需要通过共享变量方式；
		 */
		this.toStop = true;
		this.stopReason = stopReason;
		if(threadPool != null){
			threadPool.shutdownNow();
		}
		logger.info(">>>>>>>>>>> job thread stop, jobId:{}, toStop:{}, stopReason:{}", jobId, toStop, stopReason);
	}

    /**
     * is running job
     * @return
     */
    public boolean isRunningOrHasQueue() {
        return isRunning() || triggerQueue.size()>0;
    }

	private boolean isRunning(){
		for (AtomicBoolean atomicBoolean : multiRunningThreadList) {
			if (atomicBoolean != null && atomicBoolean.get()) {
				return true;
			}
		}
		return false;
	}

    @Override
	public void run() {

    	// init
    	try {
			handler.init();
		} catch (Throwable e) {
    		logger.error(e.getMessage(), e);
		}
		try{
			// execute
			int threadNum = this.handler.executeThreadNum();
			if(threadNum > 1){
				CountDownLatch countDownLatch = new CountDownLatch(threadNum);
				for (int i = 0; i < threadNum; i++) {
					int index = i;
					threadPool.execute(() -> {
						try {
							consumerQueue(index);
						}finally {
							countDownLatch.countDown();
						}
					});
				}
				// 等待所有线程结束
				countDownLatch.await();
			}else{
				consumerQueue(0);
			}
		}catch (Exception e){
			logger.error(">>>>>>>>>>> xxl-job, job multi thread execute error, jobId:{}", jobId, e);
		}

		// callback trigger request in queue
		while(triggerQueue !=null && triggerQueue.size()>0){
			TriggerParam triggerParam = triggerQueue.poll();
			if (triggerParam!=null) {
				// is killed
				TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
						triggerParam.getLogId(),
						triggerParam.getLogDateTime(),
						XxlJobContext.HANDLE_CODE_FAIL,
						stopReason + " [job not executed, in the job queue, killed.]")
				);
			}
		}

		// destroy
		try {
			handler.destroy();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		logger.info(">>>>>>>>>>> xxl-job jobId {} JobThread stoped, hashCode:{}", this.jobId, Thread.currentThread());
	}

	/**
	 * 消费队列任务
	 */
	private void consumerQueue(int index){
		while(!toStop){
			multiRunningThreadList.get(index).set(false);
//			running = false;
			idleTimes++;

			TriggerParam triggerParam = null;
			try {
				// to check toStop signal, we need cycle, so wo cannot use queue.take(), instand of poll(timeout)
				triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
				if (triggerParam!=null) {
//					running = true;
					multiRunningThreadList.get(index).set(true);
					idleTimes = 0;
					triggerLogIdSet.remove(triggerParam.getLogId());

					// log filename, like "logPath/yyyy-MM-dd/9999.log"
					String logFileName = XxlJobFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTime()), triggerParam.getLogId());
					XxlJobContext xxlJobContext = new XxlJobContext(
							triggerParam.getJobId(),
							triggerParam.getExecutorParams(),
							logFileName,
							triggerParam.getBroadcastIndex(),
							triggerParam.getBroadcastTotal());

					// init job context
					XxlJobContext.setXxlJobContext(xxlJobContext);

					// execute
					XxlJobHelper.log("<br>----------- xxl-job job execute start -----------<br>----------- Param:" + xxlJobContext.getJobParam());

					if (triggerParam.getExecutorTimeout() > 0) {
						// limit timeout
						Thread futureThread = null;
						FutureTask<Boolean> futureTask = null;
						try {
							futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
								@Override
								public Boolean call() throws Exception {

									// init job context
									XxlJobContext.setXxlJobContext(xxlJobContext);
									handler.execute();
									return true;
								}
							});
							// 有线程池，则不需要单独线程运行，本身就在线程内运行,避免创建线程消耗
							if(threadPool != null){
								threadPool.submit(futureTask);
							} else {
								futureThread = new Thread(futureTask);
								futureThread.start();
							}

							Boolean tempResult = futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
						} catch (TimeoutException e) {

							XxlJobHelper.log("<br>----------- xxl-job job execute timeout");
							XxlJobHelper.log(e);

							// handle result
							XxlJobHelper.handleTimeout("job execute timeout ");
						} finally {
							if(futureTask != null){
								futureTask.cancel(true);
							}
							if(futureThread != null){
								futureThread.interrupt();
							}
						}
					} else {
						// just execute
						handler.execute();
					}

					// valid execute handle data
					if (XxlJobContext.getXxlJobContext().getHandleCode() <= 0) {
						XxlJobHelper.handleFail("job handle result lost.");
					} else {
						String tempHandleMsg = XxlJobContext.getXxlJobContext().getHandleMsg();
						tempHandleMsg = (tempHandleMsg!=null&&tempHandleMsg.length()>50000)
								?tempHandleMsg.substring(0, 50000).concat("...")
								:tempHandleMsg;
						XxlJobContext.getXxlJobContext().setHandleMsg(tempHandleMsg);
					}
					XxlJobHelper.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- Result: handleCode="
							+ XxlJobContext.getXxlJobContext().getHandleCode()
							+ ", handleMsg = "
							+ XxlJobContext.getXxlJobContext().getHandleMsg()
					);

				} else {
					if (idleTimes > 30 * handler.executeThreadNum()) {
						// 所有线程都未执行，则停止线程
						if(triggerQueue.isEmpty() && !isRunning()) {	// avoid concurrent trigger causes jobId-lost
							logger.info(">>>>>>>>>>> xxl-job, jobId={},toStop={}, triggerQueueSize={}", jobId, toStop, triggerQueue.size());
							XxlJobExecutor.removeJobThread(jobId, "excutor idle times over limit.");
						}
					}
				}
			} catch (Throwable e) {
				Throwable throwable = e;
				if (toStop) {
					XxlJobHelper.log("<br>----------- JobThread toStop, stopReason:" + stopReason);
				}
				if(throwable instanceof ExecutionException){
					throwable = throwable.getCause();
				}
				if(throwable instanceof InvocationTargetException){
					throwable = ((InvocationTargetException) throwable).getTargetException();
				}

				// handle result
				StringWriter stringWriter = new StringWriter();
				throwable.printStackTrace(new PrintWriter(stringWriter));
				String errorMsg = stringWriter.toString();
				String param = triggerParam != null ? triggerParam.getExecutorParams() : null;
				String executorHandler = triggerParam != null ? triggerParam.getExecutorHandler() : null;
				logger.error("xxl-job handler:"+executorHandler+" execute error, executorParams:" + param, throwable);

				XxlJobHelper.handleFail(errorMsg);

				XxlJobHelper.log("<br>----------- JobThread Exception:" + errorMsg + "<br>----------- xxl-job job execute end(error) -----------");
			} finally {
				if(triggerParam != null) {
					// callback handler info
					if (!toStop) {
						// commonm
						TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
								triggerParam.getLogId(),
								triggerParam.getLogDateTime(),
								XxlJobContext.getXxlJobContext().getHandleCode(),
								XxlJobContext.getXxlJobContext().getHandleMsg() )
						);
					} else {
						// is killed
						TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
								triggerParam.getLogId(),
								triggerParam.getLogDateTime(),
								XxlJobContext.HANDLE_CODE_FAIL,
								stopReason + " [job running, killed]" )
						);
					}
				}
			}
		}
		logger.info(">>>>>>>>>>> xxl-job jobId {} JobThread stoped.", this.jobId);
	}
}
