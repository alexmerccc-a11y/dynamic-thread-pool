package cn.bugstack.middleware.dynamic.thread.pool.trigger;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import cn.bugstack.middleware.dynamic.thread.pool.types.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/dynamic/thread/pool/")
public class DynamicThreadPoolController {

    @Autowired
    private DynamicThreadPoolService dynamicThreadPoolService;

    /**
     * 查询线程池数据
     *
     * <p>读取的是 starter 定时任务写入的 Redis List：THREAD_POOL_CONFIG_LIST_KEY。
     * 这个接口通常给首页表格使用。</p>
     *
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list'
     */
    @RequestMapping(value = "query_thread_pool_list", method = RequestMethod.GET)
    public Response<List<ThreadPoolConfigEntity>> queryThreadPoolList() {
        try {
            // 这里的 key 必须和 starter 中 RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY 保持一致。
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(dynamicThreadPoolService.queryThreadPoolList())
                    .build();
        } catch (Exception e) {
            log.error("查询线程池数据异常", e);
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 查询线程池配置
     *
     * <p>读取的是 starter 上报的单个线程池配置：
     * THREAD_POOL_CONFIG_PARAMETER_LIST_KEY_{appName}_{threadPoolName}。</p>
     *
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_config?appName=dynamic-thread-pool-test-app&threadPoolName=threadPoolExecutor01'
     */
    @RequestMapping(value = "query_thread_pool_config", method = RequestMethod.GET)
    public Response<ThreadPoolConfigEntity> queryThreadPoolConfig(@RequestParam String appName, @RequestParam String threadPoolName) {
        try {
            // appName 区分应用，threadPoolName 区分这个应用里的具体线程池。
            String cacheKey = "THREAD_POOL_CONFIG_PARAMETER_LIST_KEY" + "_" + appName + "_" + threadPoolName;
            ThreadPoolConfigEntity threadPoolConfigEntity = dynamicThreadPoolService.queryThreadPoolConfigByName(threadPoolName);
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(threadPoolConfigEntity)
                    .build();
        } catch (Exception e) {
            log.error("查询线程池配置异常", e);
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 修改线程池配置
     *
     * <p>这个接口不直接修改线程池，因为线程池在业务应用进程里。
     * 它只负责往 Redis Topic 发布一条消息，真正的修改由业务应用里的 ThreadPoolConfigAdjustListener 完成。</p>
     *
     * curl --request POST \
     * --url http://localhost:8089/api/v1/dynamic/thread/pool/update_thread_pool_config \
     * --header 'content-type: application/json' \
     * --data '{
     * "appName":"dynamic-thread-pool-test-app",
     * "threadPoolName": "threadPoolExecutor01",
     * "corePoolSize": 1,
     * "maximumPoolSize": 10
     * }'
     */
    @RequestMapping(value = "update_thread_pool_config", method = RequestMethod.POST)
    public Response<Boolean> updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity request) {
        try {
            log.info("修改线程池配置开始 {} {} {}", request.getAppName(), request.getThreadPoolName(), JSON.toJSONString(request));
            // Topic 名称要带 appName，这样只会通知目标应用，避免误改其他应用的线程池。


            // 发布后，订阅了这个 Topic 的业务应用会收到 ThreadPoolConfigEntity，并调整本地线程池。
            dynamicThreadPoolService.updateThreadPoolConfig(request);
            log.info("修改线程池配置完成 {} {}", request.getAppName(), request.getThreadPoolName());
            return Response.<Boolean>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("修改线程池配置异常 {}", JSON.toJSONString(request), e);
            return Response.<Boolean>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

}
