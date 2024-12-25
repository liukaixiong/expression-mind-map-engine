package com.liukx.expression.engine.server.components;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.server.enums.SyncDataEnums;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.service.SyncDataService;
import com.liukx.expression.engine.server.service.model.sync.BaseSyncData;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 数据同步管理器
 *
 * @author liukaixiong
 * @date 2024/2/20
 */
@Component
public class SyncDataManager implements InitializingBean {
    private final Logger LOG = getLogger(SyncDataManager.class);
    @Autowired
    private List<SyncDataService> syncDataServiceList;

    private Map<SyncDataEnums, InternalSyncDataService> syncDataMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.syncDataMap = syncDataServiceList.stream().map(var -> {
            Class<?> resolve = ResolvableType.forInstance(var).as(SyncDataService.class).getGeneric().resolve();
            return new InternalSyncDataService(var.syncType(), var, resolve);
        }).collect(Collectors.toMap(InternalSyncDataService::getType, Function.identity()));
    }

    public BaseSyncData export(SyncDataEnums type, Long id) {
        LOG.info("start export data : {} -> {} ", type, id);

        InternalSyncDataService syncDataService = this.syncDataMap.get(type);

        Throws.check(syncDataService == null, "数据执行类型有误,请核对[" + type + "]!");

        Object export = syncDataService.getService().export(id);

        return new BaseSyncData(type, export);
    }

    public boolean importData(BaseSyncData baseSyncData) {

        SyncDataEnums type = baseSyncData.getType();

        InternalSyncDataService syncDataService = this.syncDataMap.get(type);

        LOG.info("start import data : {}", syncDataService);

        Object data = baseSyncData.getData();

        Object convert = Convert.convert(syncDataService.getClazz(), data);

        return syncDataService.getService().importData(convert);
    }

    static class InternalSyncDataService {

        private final SyncDataEnums type;
        private final SyncDataService service;
        private final Class<?> clazz;

        public InternalSyncDataService(SyncDataEnums type, SyncDataService service, Class<?> clazz) {
            this.type = type;
            this.service = service;
            this.clazz = clazz;
        }

        public SyncDataEnums getType() {
            return type;
        }

        public SyncDataService getService() {
            return service;
        }

        public Class<?> getClazz() {
            return clazz;
        }
    }

}
