package io.anyway.galaxy.console.controller;

import io.anyway.galaxy.console.domain.DataSourceInfo;
import io.anyway.galaxy.console.service.DatasourceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/8.
 */
@Controller
@RequestMapping("datasource")
public class DatasourceController {

    @Autowired
    private DatasourceInfoService datasourceInfoService;

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public int insertDatasource(DataSourceInfo dataSourceInfo) {
        return datasourceInfoService.update(dataSourceInfo);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public int updateDatasource(DataSourceInfo dataSourceInfo) {
        return datasourceInfoService.update(dataSourceInfo);
    }

    @RequestMapping(value="{id}", method = RequestMethod.GET)
    @ResponseBody
    public DataSourceInfo getDatasource(Model model, @PathVariable long id) {
        return datasourceInfoService.get(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<DataSourceInfo> listDatasource(DataSourceInfo dataSourceInfo) {
        return datasourceInfoService.list(dataSourceInfo);
    }

    @RequestMapping(value="{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public DataSourceInfo delDatasource(Model model, @PathVariable long id) {
        return datasourceInfoService.get(id);
    }
}
