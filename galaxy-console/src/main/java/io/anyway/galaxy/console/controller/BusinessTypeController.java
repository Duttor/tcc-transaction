package io.anyway.galaxy.console.controller;

import io.anyway.galaxy.console.domain.BusinessTypeInfo;
import io.anyway.galaxy.console.service.BusinessTypeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/4.
 */

@Controller
@RequestMapping("businessType")
public class BusinessTypeController {

    @Autowired
    private BusinessTypeInfoService businessTypeInfoService;

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public int insertBusinessType(BusinessTypeInfo businessTypeInfo) {
        return businessTypeInfoService.update(businessTypeInfo);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public int updateBusinessType(BusinessTypeInfo businessTypeInfo) {
        return businessTypeInfoService.update(businessTypeInfo);
    }

    @RequestMapping(value="{id}", method = RequestMethod.GET)
    @ResponseBody
    public BusinessTypeInfo getBusinessType(Model model, @PathVariable long id) {
        return businessTypeInfoService.get(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<BusinessTypeInfo> listBusinessType(BusinessTypeInfo businessTypeInfo) {
        return businessTypeInfoService.list(businessTypeInfo);
    }

    @RequestMapping(value="{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public BusinessTypeInfo delBusinessType(Model model, @PathVariable long id) {
        return businessTypeInfoService.get(id);
    }
}
