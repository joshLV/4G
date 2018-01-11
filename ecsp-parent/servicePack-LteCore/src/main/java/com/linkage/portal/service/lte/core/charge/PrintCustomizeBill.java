package com.linkage.portal.service.lte.core.charge;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.ecsp.core.DataRepository;
import com.ailk.ecsp.core.SysConstant;
import com.ailk.ecsp.intf.webservice.WSClient;
import com.ailk.ecsp.intf.webservice.WSConfig;
import com.ailk.ecsp.service.Service;
import com.ailk.ecsp.util.IConstant;
import com.ailk.ecsp.util.SoapUtil;
import com.al.ec.serviceplatform.client.DataMap;
import com.al.ecs.exception.ResultConstant;
import com.linkage.json.JacksonUtil;
import com.linkage.portal.service.lte.DataMapUtil;
import com.linkage.portal.service.lte.LteConstants;
import com.linkage.portal.service.lte.dto.TcpCont;


/**
 * 账单打印.
 * @createDate 2013-09-27
 * @author xuj
 */
public class PrintCustomizeBill extends Service {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public DataMap exec(DataMap dataMap, String serviceSerial) throws Exception {
    	
		String[] params = { "phoneNumber", "billingCycle" };
		dataMap = DataMapUtil.checkParam(dataMap, params);
		if (StringUtils.isNotBlank(dataMap.getResultCode())) {
			return dataMap;
		}
        String inXML = TcpCont.parseTemplate(dataMap.getInParam(), getClass().getSimpleName());

        if (StringUtils.isBlank(inXML)) {
			return DataMapUtil.setDataMapResult(dataMap, ResultConstant.R_INTF_PARAM_FAIL);
        }
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> inMap = new HashMap<String, Object>();
		try{
            inMap.put("in0", inXML);
	    	String url = DataRepository.getInstence().getSysParamValue(LteConstants.CON_CSB_URL_KEY,SysConstant.CON_SYS_PARAM_GROUP_INTF_URL);
			WSConfig config = new WSConfig();
			config.setUrl(url);	
			config.setMethodName("exchange"); //请求的接口名称
			config.setOutParamType(IConstant.CON_OUT_PARAM_TYPE_TO_XML);//输出参数格式
			config.setInParams(inMap);
			Map<String, Object> resMap = WSClient.getInstance().callWS(config);
			//记录接口日志
			dataMap.put("inIntParam", inXML);
			dataMap.put("outIntParam", MapUtils.getString(resMap, "result"));
			String resXml = MapUtils.getString(resMap, "result");
            if(StringUtils.isNotBlank(resXml)){
                int index = resXml.indexOf("<ContractRoot>");
                int end = resXml.indexOf("</ContractRoot>");
                if(end > index && end > 10)
                    resXml = resXml.substring(index, end + "</ContractRoot>".length());
            }
            if(ResultConstant.R_POR_SUCCESS.getCode().equals(MapUtils.getString(resMap, "resultCode"))){
                dataMap.setResultCode(ResultConstant.R_POR_SUCCESS.getCode());
            }else{
                dataMap.setResultCode(MapUtils.getString(resMap, "resultCode"));
            }
            returnMap.put("result", resXml);
			dataMap.setOutParam(returnMap);
			
		}catch (Exception e) { 
			DataMapUtil.setDataMapResult(dataMap, ResultConstant.R_INTERFACE_EXCEPTION,ExceptionUtils.getFullStackTrace(e));
		}
		log.debug("dataMap:{}",JacksonUtil.getInstance().ObjectTojson(dataMap));
		return dataMap;
    }

}