package io.anyway.galaxy.console.dal.db;

import io.anyway.galaxy.console.dal.dto.DataSourceInfoDto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiong.j on 2016/8/1.
 */
public class DsTypeContextHolder {

    public final static String DEFAULT_SESSION_FACTORY = "default";
    public final static String DYNAMIC_SESSION_FACTORY = "dynamic";

    private final static String DS_INFO = "dsInfo";

    private final static String CONTEXT_TYPE = "contextType";

    private static final ThreadLocal<Map<Object, Object>> contextHolder = new ThreadLocal<Map<Object, Object>>();

    public static void setDsInfo(DataSourceInfoDto dsInfo) {
        init();
        contextHolder.get().put(DS_INFO, dsInfo);
    }

    public static DataSourceInfoDto getDsInfo() {
        init();
        if (contextHolder.get().containsKey(DS_INFO)) {
            return (DataSourceInfoDto)contextHolder.get().get(DS_INFO);
        }
        return null;
    }

    public static void setContextType(String contextType) {
        init();
        contextHolder.get().put(CONTEXT_TYPE, contextType);
    }

    public static String getContextType() {
        init();
        if (contextHolder.get().containsKey(CONTEXT_TYPE)) {
            return (String) contextHolder.get().get(CONTEXT_TYPE);
        }
        return null;
    }

    public static void clear() {
        contextHolder.remove();
    }

    private static void init(){
        if (contextHolder.get() == null) {
            Map<Object, Object> map = new HashMap<Object, Object>(2);
            contextHolder.set(map);
        }
    }
}
