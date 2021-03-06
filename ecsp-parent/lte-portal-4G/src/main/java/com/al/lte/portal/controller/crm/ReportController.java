package com.al.lte.portal.controller.crm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import com.al.common.utils.DateUtil;
import com.al.common.utils.EncodeUtils;
import com.al.ec.serviceplatform.client.ResultCode;
import com.al.ecs.common.entity.JsonResponse;
import com.al.ecs.common.entity.PageModel;
import com.al.ecs.common.util.JsonUtil;
import com.al.ecs.common.util.MDA;
import com.al.ecs.common.util.PageUtil;
import com.al.ecs.common.util.PropertiesUtils;
import com.al.ecs.common.web.ServletUtils;
import com.al.ecs.common.web.SpringContextUtil;
import com.al.ecs.exception.AuthorityException;
import com.al.ecs.exception.BusinessException;
import com.al.ecs.exception.ErrorCode;
import com.al.ecs.exception.InterfaceException;
import com.al.ecs.exception.ResultConstant;
import com.al.ecs.spring.annotation.log.LogOperatorAnn;
import com.al.ecs.spring.annotation.session.AuthorityValid;
import com.al.ecs.spring.controller.BaseController;
import com.al.lte.portal.bmo.crm.CartBmo;
import com.al.lte.portal.bmo.crm.OrderBmo;
import com.al.lte.portal.bmo.crm.ReportBmo;
import com.al.lte.portal.bmo.staff.StaffBmo;
import com.al.lte.portal.bmo.staff.StaffChannelBmo;
import com.al.lte.portal.common.CommonMethods;
import com.al.lte.portal.common.CommonUtils;
import com.al.lte.portal.common.DataConcealUtil;
import com.al.lte.portal.common.EhcacheUtil;
import com.al.lte.portal.common.FTPServiceUtils;
import com.al.lte.portal.common.SysConstant;
import com.al.lte.portal.model.SessionStaff;

/**
 * 统计报表控制层
 * 
 * @author wukf
 * @version V1.0 2013-08-07
 * @createDate 2013-08-07 上午10:03:44
 * @modifyDate
 * @copyRight 亚信联创EC研发部
 */
@Controller("com.al.lte.portal.controller.crm.ReportController")
@RequestMapping("/report/*")
public class ReportController extends BaseController {

    @Autowired
    @Qualifier("com.al.lte.portal.bmo.crm.ReportBmo")
    private ReportBmo reportBmo;

    @Autowired
    @Qualifier("com.al.lte.portal.bmo.crm.CartBmo")
    private CartBmo cartBmo;

    @Autowired
    @Qualifier("com.al.lte.portal.bmo.crm.OrderBmo")
    private OrderBmo orderBmo;

    @Autowired
    @Qualifier("com.al.lte.portal.bmo.staff.StaffBmo")
    private StaffBmo staffBmo;

    @Autowired
    @Qualifier("com.al.lte.portal.bmo.staff.StaffChannelBmo")
    private StaffChannelBmo staffChannelBmo;
    
	@Autowired
	@Qualifier("com.al.lte.portal.common.FTPServiceUtils")
	private FTPServiceUtils ftpServiceUtils;

    /**
     * 受理工号查询和渠道查询(现用于受理单查询页面的渠道和受理工号查询)
     * @param session
     * @param model
     * @param request
     * @param paramMap
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/qureyStaffAndChl", method = { RequestMethod.POST })
    public String qureyStaffAndChl(@RequestBody Map<String, Object> param, Model model, HttpServletResponse response) {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        String htmlDialog = "";
        Integer totalSize = 1;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> returnMap = null;
        String pageIndex = param.get("pageIndex") == null ? "" : param.get("pageIndex").toString();
        String pageSize = param.get("pageSize") == null ? "" : param.get("pageSize").toString();
        int iPage = 1;
        int iPageSize = 10;
        iPage = Integer.parseInt(pageIndex);
        iPageSize = Integer.parseInt(pageSize);

        Map<String, Object> queryParam = new HashMap<String, Object>(param);
        model.addAttribute("pageType", queryParam.get("pageType"));

        if ("queryChannel".equals(queryParam.get("queryFlag"))) {//**渠道查询**
            htmlDialog = "query-channel-dialog";
            queryParam.remove("queryFlag");

            if (iPage == 0) {//首次打开弹窗页面，不进行数据查询
                PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(iPage, iPageSize, totalSize < 1 ? 1
                        : totalSize, list);

                model.addAttribute("pageModel", pm);
                model.addAttribute("pageParam", param);

                return "cart/" + htmlDialog;
            }

            /*if(queryParam.get("channelName")!=null && "".equals(queryParam.get("channelName")))
            	queryParam.remove("channelName");*/
            try {
                returnMap = this.staffChannelBmo.qryChannelListByCond(queryParam, null, sessionStaff);
                if (ResultCode.R_SUCC.equals(returnMap.get("interfaceCode"))) {//调用接口成功
                    if (ResultCode.R_SUCC.equals(returnMap.get("resultCode"))) {//系管返回是否异常(0成功-1失败-1异常)
                        if (returnMap.get("total") != null) {
                            totalSize = Integer.parseInt(returnMap.get("total").toString());
                            if (totalSize > 0) {
                                list = (List<Map<String, Object>>) returnMap.get("result");
                            }
                        }
                    } else if (ResultCode.R_FAILURE.equals(returnMap.get("resultCode"))) {//0成功-1失败-1异常	

                    }
                } else if (ResultCode.R_FAILURE.equals(returnMap.get("interfaceCode"))) {//调用接口异常
                    returnMap.get("msg");
                }

            } catch (InterfaceException ie) {
                super.failedStr(model, ErrorCode.QUERY_CHANNEL, ie, queryParam);
            } catch (IOException e) {

            } catch (Exception e) {
                super.failedStr(model, ErrorCode.QUERY_CHANNEL, e, queryParam);
            }
        } else if ("queryStaff".equals(queryParam.get("queryFlag"))) {//**受理工号查询**

            htmlDialog = "query-staffId-dialog";
            queryParam.remove("queryFlag");

            if (iPage == 0) {//首次打开弹窗页面，不进行数据查询
                PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(iPage + 1, iPageSize, totalSize < 1 ? 1
                        : totalSize, list);

                model.addAttribute("pageModel", pm);
                model.addAttribute("pageParam", param);

                return "cart/" + htmlDialog;
            }

            queryParam.remove("channelName");
            /*if(queryParam.get("staffName")!=null && "".equals(queryParam.get("staffName")))
            	queryParam.remove("staffName");
            if(queryParam.get("staffId")!=null && "".equals(queryParam.get("staffId")))
            	queryParam.remove("staffId");*/
            try {
                returnMap = this.staffBmo.queryStaffList(queryParam, null, sessionStaff);
                String resultCode = (String) returnMap.get("resultCode");
                if (resultCode.equals(ResultCode.R_SUCC)) {//0-成功，-1异常
                    if (returnMap.get("totalNum") != null) {
                        totalSize = Integer.parseInt(returnMap.get("totalNum").toString());
                        if (totalSize > 0) {
                            list = (List<Map<String, Object>>) returnMap.get("result");
                        }
                    }
                } else if (resultCode.equals(ResultCode.R_FAILURE)) {//0-成功，-1异常
                    super.failedStr(model, ErrorCode.QUERY_STAFF_INFO, "员工查询返回结果异常", queryParam);
                } else if ("3".equals(resultCode)) {//员工信息查询接口：未查到员工信息

                }
            } catch (Exception e) {
                super.failedStr(model, ErrorCode.QUERY_STAFF_INFO, e, queryParam);
            }
        }

        PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(iPage, iPageSize, totalSize < 1 ? 1 : totalSize,
                list);

        model.addAttribute("pageModel", pm);
        model.addAttribute("pageParam", param);

        return "cart/" + htmlDialog;
    }

    /**
     * 转至受理订单查询页面
     * @param model
     * @param session
     * @param flowNum
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/cartMain", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String main(@RequestParam Map<String, Object> param,Model model, HttpSession session, @LogOperatorAnn String flowNum) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/cartMain"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        //判别用户是否具有查询某地区下的所有渠道的权限
        String iseditOperation = null;
        try {
            if (iseditOperation == null) {
                iseditOperation = staffBmo.checkOperatSpec(SysConstant.QRYCHANNELAUTH_CODE, sessionStaff);
                //暂不将该权限写入session
                //ServletUtils.setSessionAttribute(super.getRequest(),SysConstant.SESSION_KEY_JUMPAUTH+"_"+sessionStaff.getStaffId(), iseditOperation);
            }
        } catch (Exception e) {
            iseditOperation = "1";
        }
        model.addAttribute("QryChannelAuth", iseditOperation);
        //		model.addAttribute("QryChannelAuth", "0");

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "detail");
        model.addAttribute("flag", param.get("strParam"));  // 在途单 标志
        return "/cart/cart-main";
    }

    /**
     * 购物车列表查询
     * @param session
     * @param model
     * @param request
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/cartList", method = RequestMethod.GET)
    public String list(HttpSession session, Model model, WebRequest request) throws BusinessException {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        HttpSession httpSession = ServletUtils.getSession(super.getRequest());
        Map<String, Object> param = new HashMap<String, Object>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Integer nowPage = 1;
        Integer pageSize = 10;
        Integer totalSize = 0;
        //重打回执，发票重打、发票补打，需要传送工号；暂存单查询需要传tSOrder；
        String pageType = request.getParameter("pageType");
        String pageType4EC = request.getParameter("pageType4EC"); // 工单查询（电渠）
        String permissionsType = request.getParameter("permissionsType");
        //个人权限需要传工号
        if (StringUtils.isNotBlank(permissionsType) && permissionsType.equals("personal")) {
            param.put("staffId", sessionStaff.getStaffId());
        }
        if (null != pageType && !"".equals(pageType)) {
            if ("voucher".equals(pageType) || "reInvoice".equals(pageType) || "addInvoice".equals(pageType)) {
                param.put("staffId", sessionStaff.getStaffId());
            } else if ("queryCashier".equals(pageType)) {
                param.put("tSOrder", request.getParameter("tSOrder"));
            } else if ("saveOrder".equals(pageType)) {
                param.put("tSOrder", request.getParameter("tSOrder"));
                if (request.getParameter("partyId") != null) {
                    param.put("partyId", request.getParameter("partyId"));
                }
            } else if ("link".equals(pageType) && "link4EC".equals(pageType4EC)) {
            	param.put("extCustOrderId", request.getParameter("extCustOrderId"));
            }
        }
        
        //查询分段受理暂存单时门户后端判断如果没有“收银台查询权限”，不能查询该工号未分配的渠道，并提示异常
        if("queryCashier".equals(pageType)){
        	String qryChannelAuth = (String) ServletUtils.getSessionAttribute(super.getRequest(), SysConstant.CASHIER_CHANNEL_QUERY+"_"+sessionStaff.getStaffId());;
        	if(!"0".equals(qryChannelAuth)){
        		String channelId = request.getParameter("channelId");
        		boolean invalidChannel = true;
        		if(channelId == null || channelId.trim().length() == 0){
        			invalidChannel = true;
        		} else {
        			List<Map> channelList = (List<Map>)session.getAttribute(SysConstant.SESSION_KEY_STAFF_CHANNEL);
        			for(Map channelRow : channelList){
        				String id = MapUtils.getString(channelRow, "id", "");
        				if(id.equals(channelId)){
        					invalidChannel = false;
        					break;
        				}
        			}
        		}
        		if(invalidChannel){
        			throw new BusinessException(ErrorCode.CUST_ORDER, param, null, new Throwable("分段受理暂存单查询异常，未分配收银台查询权限，不能查询未分配的渠道"));
        		}
        	}
        }
        
        /*
         * 修改于2015-7-24 By zhangyu
         * 由于“收银台查询”和“受理订单查询”共用此代码，但二者“受理时间”限制不同，为“收银台查询”添加标志位，修改如下：
         * */
        String startDt = request.getParameter("startDt");
        String endDt = request.getParameter("endDt");
        String channelId = request.getParameter("channelId");
        
        // 因后台需要根据时间判断是否在转储库查询，因此通过购物车流水查询时，截取购物车时间
        String olNbr = request.getParameter("olNbr");
        if (StringUtils.isBlank(startDt) && StringUtils.isBlank(endDt) && StringUtils.isNotBlank(olNbr)) {
        	String dataPattern = "^\\d+?(20\\d{6})\\d+$";
        	Pattern r = Pattern.compile(dataPattern);
        	Matcher m = r.matcher(olNbr);
        	if (m.find()) {
        		startDt = m.group(1);
        		endDt = startDt;
        	}
        }
        try {

            //add by liusd 20151103 防止SQL injection 
            if (StringUtils.isNotBlank(startDt) && (startDt.length()> 8 || !DateUtil.isRightDate(startDt, "yyyyMMdd"))) {
                param.put("startDt", startDt);
                throw new BusinessException(ErrorCode.CUST_ORDER, param, null, new Throwable("startDt data format invalid"));
            }
            if (StringUtils.isNotBlank(endDt) && (endDt.length()> 8 || !DateUtil.isRightDate(endDt, "yyyyMMdd"))) {
                param.put("endDt", endDt);
                throw new BusinessException(ErrorCode.CUST_ORDER, param, null, new Throwable("endDt data format invalid"));
            }
            if (StringUtils.isNotBlank(channelId) && !NumberUtils.isNumber(channelId)) {
                param.put("channelId", channelId);
                throw new BusinessException(ErrorCode.CUST_ORDER, param, null, new Throwable("channelId data format invalid"));
            }
            String queryFlag = request.getParameter("queryFlag");
            if ("queryCashierList".equals(queryFlag)) {//来自收银台查询请求
                param.put("startDt", startDt);
                param.put("endDt", endDt);
            } else {//受理单查询等其他业务查询
                param.put("startDt", startDt);
                param.put("endDt", startDt);
            }
            param.put("areaId", request.getParameter("areaId"));
            param.put("qryBusiOrder", request.getParameter("qryBusiOrder"));
            param.put("channelId", request.getParameter("channelId"));
            param.put("busiStatusCd", request.getParameter("busiStatusCd"));
            param.put("olStatusCd", request.getParameter("olStatusCd"));
            param.put("qryNumber", request.getParameter("qryNumber"));
            param.put("olNbr", request.getParameter("olNbr"));
            param.put("couponNumber", request.getParameter("couponNumber") == null ? "" : request
                    .getParameter("couponNumber"));//终端串码

            param.put("channelId", channelId);

            if (request.getParameter("staffId") == null) {
                param.put("staffId", "");//员工ID(主键)
            } else {
                param.put("staffId", request.getParameter("staffId"));//员工ID(主键)
            }

            //param.put("qryBusiOrder", "1");
            param.put("qryCnt", "Y");
            param.put("qryTime", "1");

            nowPage = Integer.parseInt(request.getParameter("nowPage").toString());
            pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
            param.put("nowPage", nowPage);
            param.put("pageSize", pageSize);
            param.put("ifTransfer", request.getParameter("ifChansfer"));
            Map<String, Object> map = cartBmo.queryCarts(param, null, sessionStaff);
            if (map != null && map.get("orderLists") != null) {
                list = (List<Map<String, Object>>) map.get("orderLists");
                totalSize = MapUtils.getInteger(map, "totalCnt", 1);
                
                //暂存单查询、收银台查询限制能力开放和界面集成的单子在集团CRM进行受理，为避免前端修改订单数据，将订单信息存于会话 ZhangYu 2016-01-24
                Map<String, Object> orderListsInfo = new HashMap<String, Object>();
                if("queryCashier".equals(pageType)){//分段受理单
                    Map<String, Object> reductionOrderLists = new HashMap<String, Object>();//存放分段受理订单信息
                	for(Map<String, Object> orderList : list){
                		reductionOrderLists.put(orderList.get("olId").toString(), orderList.get("olTypeCd"));//购物车ID和订单类型
                    }
                    orderListsInfo.put("reductionOrderLists",reductionOrderLists);
                }else if("saveOrder".equals(pageType)){//暂存单
                    Map<String, Object> saveOrderLists = new HashMap<String, Object>();//存放暂存单信息
                	for(Map<String, Object> orderList : list){
                		saveOrderLists.put(orderList.get("olId").toString(), orderList.get("olTypeCd"));//购物车ID和订单类型
                    }
                    orderListsInfo.put("saveOrderLists",saveOrderLists);
                }
                httpSession.setAttribute("orderListsInfo", orderListsInfo);
                
                //判断某个购物车是否为新装，取订购销售品节点的subBusiOrders，判断是否有产品新装
                /*       		for (int i = 0; list != null && i < list.size(); i++) {
                       			Map<String, Object> cartMap = list.get(i);
                       			List<Map<String, Object>> orderList = (List<Map<String, Object>>) cartMap.get("list");
                       			boolean newProdFlag = false;
                       			cartMap.put("newProdFlag", "false");
                       			for (int j = 0; orderList != null && j < orderList.size(); j++) {
                       				Map<String, Object> orderMap = orderList.get(j);
                       				String actionClass = MapUtils.getString(orderMap, "actionClass", "");
                       				String boActionTypeCd = MapUtils.getString(orderMap, "boActionTypeCd", "");
                       				if ("1200".equals(actionClass) && "S1".equals(boActionTypeCd)) {
                       					List<Map<String, Object>> subList = (List<Map<String, Object>>) orderMap.get("subBusiOrders");
                       					for (int k = 0; subList != null && k < subList.size(); k++) {
                       						Map<String, Object> subMap = subList.get(k);
                       						String subActionClass = MapUtils.getString(subMap, "actionClass", "");
                               				String subBoActionTypeCd = MapUtils.getString(subMap, "boActionTypeCd", "");
                               				if ("1300".equals(subActionClass) && "1".equals(subBoActionTypeCd)) {
                               					newProdFlag = true;
                               					break;
                               				}
                       					}
                       				}
                       				if (newProdFlag) {
                       					break;
                       				}
                       			}
                       			if (newProdFlag) {
                       				cartMap.put("newProdFlag", "true");
                       				list.set(i, cartMap);
                       			}
                       		}*///是否新装目前由后台直接返回标识newProdFlag，在每个订单项中体现
            }
            PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(nowPage, pageSize, totalSize < 1 ? 1
                    : totalSize, list);
            model.addAttribute("pageModel", pm);
            model.addAttribute("code", map.get("code"));
            model.addAttribute("mess", map.get("mess"));
            model.addAttribute("pageType", request.getParameter("pageType")); //link:环节显示 detail:详情显示
            String fileAdminFlag  = "";
            if (null != pageType && !"".equals(pageType) && "link".equals(pageType)){
            	//归档管理员权限
                fileAdminFlag = (String) ServletUtils.getSessionAttribute(super.getRequest(), SysConstant.FILE_ADMIN
                        + "_" + sessionStaff.getStaffId());
                try {
                    if (fileAdminFlag == null) {
                    	fileAdminFlag = staffBmo.checkOperatSpec(SysConstant.FILE_ADMIN, sessionStaff);
                        ServletUtils.setSessionAttribute(super.getRequest(), SysConstant.FILE_ADMIN + "_"
                                + sessionStaff.getStaffId(), fileAdminFlag);
                    }
                } catch (BusinessException e) {
                	fileAdminFlag = "1";
                } catch (InterfaceException ie) {
                	fileAdminFlag = "1";
                } catch (Exception e) {
                	fileAdminFlag = "1";
                }
            }
            model.addAttribute("fileAdminFlag", fileAdminFlag);
            String refundFlag = request.getParameter("refundFlag");
            //无纸化分省开关 
            String areaId=request.getParameter("areaId");
            boolean paperlessSwitch=CommonMethods.areaIdMacthSwitch(MDA.RETURN_RECEIPT,areaId,3,"ON");
            if(paperlessSwitch){
		     model.addAttribute("paperlessSwitch",paperlessSwitch);
            }
            if (refundFlag != null && "refund".equals(refundFlag)) {
                return "/charge/order-refund-list";
            } else {
                return "/cart/cart-list";
            }
        } catch (BusinessException be) {

            return super.failedStr(model, be);
        } catch (InterfaceException ie) {

            return super.failedStr(model, ie, param, ErrorCode.CUST_ORDER);
        } catch (Exception e) {
            log.error("购物车查询/report/cartList方法异常", e);
            return super.failedStr(model, ErrorCode.CUST_ORDER, e, param);
        }
    }

    /**
     * 购物车详情查询
     * @param request
     * @param model
     * @param session
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/cartInfo", method = RequestMethod.GET)
    public String orderDetail(WebRequest request, Model model, HttpSession session,
            @RequestParam Map<String, Object> paramMap) throws BusinessException {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        paramMap.put("user", "");
        Map cartInfo = null;
        try {
            cartInfo = cartBmo.queryCartOrder(paramMap, null, sessionStaff);
            model.addAttribute("cart", cartInfo);
            model.addAttribute("code", cartInfo.get("code"));
            model.addAttribute("mess", cartInfo.get("mess"));
            model.addAttribute("olId", MapUtils.getString(paramMap, "olId", ""));
            return "/cart/cart-info";

        } catch (BusinessException be) {

            return super.failedStr(model, be);
        } catch (InterfaceException ie) {

            return super.failedStr(model, ie, paramMap, ErrorCode.CUST_ORDER_DETAIL);
        } catch (Exception e) {
            log.error("购物车详情/report/cartInfo方法异常", e);
            return super.failedStr(model, ErrorCode.CUST_ORDER_DETAIL, e, paramMap);
        }
    }

    /**
     * 订单详情查询
     * @param request
     * @param model
     * @param session
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/cartOfferInfo", method = RequestMethod.GET)
    public String cartDetail(HttpServletRequest request, Model model, HttpSession session) throws BusinessException {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        Map param = new HashMap();
        String olId = request.getParameter("olId");
        String boId = request.getParameter("boId");
        String offerId = request.getParameter("offerId");
        String prodId = request.getParameter("prodId");

        param.put("olId", olId);
        param.put("boId", boId);
        param.put("offerId", offerId);
        param.put("prodId", prodId);
        //param.put("rela_type_cd", "");
        param.put("user", "so");

        Map order = null;
        try {
            order = cartBmo.queryCartOrderInfo(param, null, sessionStaff);
            String isViewOperation = EhcacheUtil.getOperatCode(SysConstant.VIEWSENSI_CODE,
                    SysConstant.SESSION_KEY_VIEWSENSI, request, sessionStaff);
            model.addAttribute("isViewOperation", isViewOperation);
            model.addAttribute("order", order);
            model.addAttribute("code", order.get("code"));
            model.addAttribute("mess", order.get("mess"));
            model.addAttribute("boId", boId);
            return "/cart/order-item-detail";
        } catch (BusinessException be) {

            return super.failedStr(model, be);
        } catch (InterfaceException ie) {

            return super.failedStr(model, ie, param, ErrorCode.CUST_ITEM_DETAIL);
        } catch (Exception e) {
            log.error("购物车详情/report/cartOfferInfo方法异常", e);
            return super.failedStr(model, ErrorCode.CUST_ITEM_DETAIL, e, param);
        }
    }

    /**
     * 转至受理流程查询页面
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/preCartLink", method = RequestMethod.GET)
    @AuthorityValid(isCheck = false)
    public String preCartLink(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/preCartLink"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "link");

        //获取员工权限 personal admin monitor
        String permissionsType = CommonMethods.checkPreLinkOperatSpec(staffBmo, super.getRequest(), sessionStaff);
        model.addAttribute("permissionsType", permissionsType);
        return "/cart/cart-main";
    }

    @RequestMapping(value = "/preCartLink4EC", method = RequestMethod.GET)
    @AuthorityValid(isCheck = false)
    public String preCartLink4EC(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/preCartLink4EC"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "link");
        model.addAttribute("pageType4EC", "link4EC");

        //获取员工权限 personal admin monitor
        String permissionsType = CommonMethods.checkPreLinkOperatSpec(staffBmo, super.getRequest(), sessionStaff);
        model.addAttribute("permissionsType", permissionsType);
        
        //判别用户是否具有查询某地区下的所有渠道的权限
        /*String iseditOperation = null;
        try {
            if (iseditOperation == null) {
                iseditOperation = staffBmo.checkOperatSpec(SysConstant.QRYCHANNELAUTH_CODE, sessionStaff);
            }
        } catch (Exception e) {
            iseditOperation = "1";
        }
        model.addAttribute("QryChannelAuth", iseditOperation);*/
        return "/cart/cart-main";
    }
    /**
     * 转至回执打印页面
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/cartForVoucher", method = RequestMethod.GET)
    @AuthorityValid(isCheck = false)
    public String cartForVoucher(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/preCartLink"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "voucher");

        return "/cart/cart-main";
    }

    /**
     * 重打发票
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/cartForReInvoice", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String cartForReInvoice(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/preCartLink"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "reInvoice");

        return "/cart/cart-main";
    }

    /**
     * 电子发票
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/cartForReInvoiceEl", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String cartForReInvoiceEl(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/preCartLink"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));

        return "/order/electronics-invoice-query";
    }

    /**
     * 补打发票
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/cartForAddInvoice", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String cartForInvoice(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/preCartLink"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "addInvoice");

        return "/cart/cart-main";
    }

    /**
     * 查询购物车环节
     * @param param
     * @param flowNum
     * @param session
     * @param model
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/cartLink", method = RequestMethod.GET)
    public String queryCartLink(@RequestParam Map<String, Object> param, @LogOperatorAnn String flowNum,
            HttpSession session, Model model) throws BusinessException {

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        try {
            Map<String, Object> resultMap = cartBmo.queryCartLink(param, flowNum, sessionStaff);
            model.addAttribute("link", resultMap);
            model.addAttribute("olNbr", param.get("olNbr"));
            model.addAttribute("areaId", param.get("areaId"));
            model.addAttribute("channelId", param.get("channelId"));
            return "/cart/cart-link-list";
        } catch (BusinessException be) {
            return super.failedStr(model, be);
        } catch (InterfaceException ie) {
            return super.failedStr(model, ie, param, ErrorCode.QUERY_CART_LINK);
        } catch (Exception e) {
            log.error("购物车环节详情/report/cartLink方法异常", e);
            return super.failedStr(model, ErrorCode.QUERY_CART_LINK, e, param);
        }
    }

    /**
     * 购物车失败环节重发
     * @param paramMap
     * @param flowNum
     * @param response
     * @return
     */
    @RequestMapping(value = "/resendCustOrder", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse resendCustOrder(@RequestBody Map<String, Object> params, @LogOperatorAnn String flowNum,
            HttpServletResponse response) {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        try {
            Map<String, Object> resultMap = cartBmo.resendCustOrder(params, flowNum, sessionStaff);
            if (MapUtils.getString(resultMap, "resultCode", "-1").equals(ResultCode.R_SUCC)) {
                return successed("done");
            } else {
                return failed(MapUtils.getString(resultMap, "resultMsg", "未获取到失败原因"), 1);
            }
        } catch (BusinessException be) {
            return failed(be);
        } catch (InterfaceException ie) {
            return failed(ie, params, ErrorCode.RESEND_CUST_ORDER);
        } catch (Exception e) {
            return failed(ErrorCode.RESEND_CUST_ORDER, e, params);
        }
    }

    /**
     * 工单状态查询
     */
    @RequestMapping(value = "/constructionState", method = RequestMethod.GET)
    public String queryConstructionState(@RequestParam Map<String, Object> param, @LogOperatorAnn String flowNum,
            HttpSession session, HttpServletResponse response, Model model) throws BusinessException {

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        try {
            Map<String, Object> resultMap = cartBmo.queryConstructionState(param, flowNum, sessionStaff);
            if (resultMap.get("resultCode").equals("0")) {
                model.addAttribute("flag", 0);
                model.addAttribute("resultList", resultMap.get("resultList"));
            } else {
                response.addHeader("respCode", "1");
                response.addHeader("respMsg", EncodeUtils.urlEncode(MapUtils.getString(resultMap, "resultMsg",
                        "工单查询失败，且获取不到错误信息"), "UTF-8"));
                model.addAttribute("flag", 1);
            }
            return "/cart/construction-state";
        } catch (BusinessException be) {
            return super.failedStr(model, be);
        } catch (InterfaceException ie) {
            return super.failedStr(model, ie, param, ErrorCode.QUERY_CONSTRUCTION_STATE);
        } catch (Exception e) {
            log.error("施工单状态查询/report/constructionState方法异常", e);
            return super.failedStr(model, ErrorCode.QUERY_CONSTRUCTION_STATE, e, param);
        }
    }

    /**
     * 转至一卡双号业务监控页面
     * @return 
     */
    @RequestMapping(value = "/queryOneCardTwoNumber", method = RequestMethod.GET)
    @AuthorityValid(isCheck = false)
    public String queryProdAccessNumByUim(Model model, HttpSession session, @LogOperatorAnn String flowNum)
            throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/cartMain"));
        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        return "/orderQuery/query_one_card_two_number_main";
    }

    /**
     * 查询一卡双号信息
     * @param session
     * @param model
     * @param request
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryOCTN", method = RequestMethod.GET)
    public String queryOCTN(HttpSession session, Model model, WebRequest request,
            @RequestParam Map<String, Object> param) throws BusinessException {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Integer nowPage = MapUtils.getInteger(param, "nowPage", 1);
        Integer pageSize = MapUtils.getInteger(param, "pageSize", 10);
        Integer totalSize = 0;
        try {
            param.put("nowPage", nowPage);
            param.put("pageSize", pageSize);
            Map<String, Object> map = cartBmo.queryOCTN(param, null, sessionStaff);
            if (map != null && map.get("result") != null) {
                list = (List<Map<String, Object>>) map.get("result");
                Map<String, Object> page = (Map<String, Object>) map.get("page");
                totalSize = MapUtils.getInteger(page, "totalSize", 1);
            }
            PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(nowPage, pageSize, totalSize < 1 ? 1
                    : totalSize, list);
            model.addAttribute("pageModel", pm);
            model.addAttribute("code", map.get("code"));
            model.addAttribute("mess", map.get("mess"));
            return "/orderQuery/query_one_card_two_number_list";
        } catch (BusinessException be) {
            return super.failedStr(model, be);
        } catch (InterfaceException ie) {
            return super.failedStr(model, ie, param, ErrorCode.QUERY_OCTN);
        } catch (Exception e) {
            return super.failedStr(model, ErrorCode.QUERY_OCTN, e, param);
        }
    }

    /**
     * 收银台查询
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/queryCashier", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String queryCashier(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/queryCashier"));
        
        
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
        		SysConstant.SESSION_KEY_LOGIN_STAFF);
        //判别用户是否具有 收银台渠道查询权限
        String qryChannelAuth = (String) ServletUtils.getSessionAttribute(super.getRequest(), SysConstant.CASHIER_CHANNEL_QUERY+"_"+sessionStaff.getStaffId());;
        try {
            if (qryChannelAuth == null) {
                qryChannelAuth = staffBmo.checkOperatSpec(SysConstant.CASHIER_CHANNEL_QUERY, sessionStaff);
                ServletUtils.setSessionAttribute(super.getRequest(),SysConstant.CASHIER_CHANNEL_QUERY+"_"+sessionStaff.getStaffId(), qryChannelAuth);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            qryChannelAuth = "-1";
        }
        model.addAttribute("QryChannelAuth", qryChannelAuth);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = f.format(c.getTime());


        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "queryCashier");

        model.addAttribute("p_startDt", currentTime);
        model.addAttribute("p_endDt", currentTime);

        return "/cashier/cashier-main";
    }

    /**
     * 换卡进度查询
     * @param model
     * @param session
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/queryCardProgress", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String queryCardProgress(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/cartMain"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        c.add(Calendar.DATE, -7);
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);

        model.addAttribute("p_startDt", startTime);
        model.addAttribute("p_endDt", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        model.addAttribute("pageType", "cardProgress");

        return "/cart/cardprogress-main";
    }

    /**
     * 自助换卡查询进度接口
     */
    @RequestMapping(value = "/cardProgressQuery", method = { RequestMethod.POST })
    public String cardProgressQuery(@RequestBody Map<String, Object> param, Model model, HttpServletResponse response) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        try {
            Map<String, Object> rMap = orderBmo.cardProgressQuery(param, null, sessionStaff);
            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            if (rMap != null && ResultCode.R_SUCC.equals(rMap.get("resultCode").toString())) {
                int total = MapUtils.getIntValue(rMap, "pnCount", 1);
                Object result = rMap.get("result");
                Map<String, Object> tempMap = (Map<String, Object>) result;
                resultList = (List<Map<String, Object>>) tempMap.get("orderInfos");
                if (resultList != null && resultList.size() > 0) {
                    PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(MapUtils
                            .getIntValue(param, "pageNo", 1), MapUtils.getIntValue(param, "pageSize", 10), total,
                            resultList);
                    model.addAttribute("pageModel", pm);
                }
            }
            return "/cart/cardprogress-list";
        } catch (BusinessException be) {
            return super.failedStr(model, be);
        } catch (InterfaceException ie) {
            return super.failedStr(model, ie, param, ErrorCode.CARD_PROGRESS_QUERY);
        } catch (Exception e) {
            log.error("自助换卡查询进度接口方法异常", e);
            return super.failedStr(model, ErrorCode.CARD_PROGRESS_QUERY, e, param);
        }
    }

    //暂存单查询界面
    @RequestMapping(value = "/querySaveOrder", method = RequestMethod.GET)
    @AuthorityValid(isCheck = false)
    public String querySaveOrder(
            @RequestParam(value = "current", required = false, defaultValue = "business") String current, Model model,
            HttpSession session, @LogOperatorAnn String flowNum) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/querySaveOrder"));
        session.removeAttribute("ValidateAccNbr");
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = f.format(c.getTime());
        String startTime = f.format(c.getTime());
        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);
        model.addAttribute("p_startTime", startTime);
        model.addAttribute("p_endTime", endTime);
        model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
        model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
        //暂存单查询受理渠道选择-所在渠道权限
        String zcd_chan_select = (String) ServletUtils.getSessionAttribute(super.getRequest(), "ZCD_CHAN_SELECT_"
                + sessionStaff.getStaffId());
        try {
            if (zcd_chan_select == null) {
                zcd_chan_select = staffBmo.checkOperatSpec("ZCD_CHAN_SELECT", sessionStaff);
                ServletUtils.setSessionAttribute(super.getRequest(), "ZCD_CHAN_SELECT_" + sessionStaff.getStaffId(),
                        zcd_chan_select);
            }
        } catch (BusinessException e) {
            zcd_chan_select = "1";
        } catch (InterfaceException ie) {
            zcd_chan_select = "1";
        } catch (Exception e) {
            zcd_chan_select = "1";
        }
        //暂存单查询受理渠道选择-所在地市权限
        String zcd_region_select = (String) ServletUtils.getSessionAttribute(super.getRequest(), "ZCD_REGION_SELECT"
                + sessionStaff.getStaffId());
        try {
            if (zcd_region_select == null) {
                zcd_region_select = staffBmo.checkOperatSpec("ZCD_REGION_SELECT", sessionStaff);
                ServletUtils.setSessionAttribute(super.getRequest(), "ZCD_REGION_SELECT_" + sessionStaff.getStaffId(),
                        zcd_region_select);
            }
        } catch (BusinessException e) {
            zcd_region_select = "1";
        } catch (InterfaceException ie) {
            zcd_region_select = "1";
        } catch (Exception e) {
            zcd_region_select = "1";
        }
        //获取员工权限
//        String permissionsType = CommonMethods.checkStaffOperatSpec(staffBmo, super.getRequest(), sessionStaff);
//        model.addAttribute("permissionsType", permissionsType);
        model.addAttribute("zcd_chan_select", zcd_chan_select);
        model.addAttribute("zcd_region_select", zcd_region_select);
        session.setAttribute("saveOrder", "true");
        return "/orderUndo/query-save-order";
    }
    
    /**
     * 一卡双号虚号省退订
     */
    @RequestMapping(value = "/unsubVirtualNumPrepare", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String unsubVirtualNumPrepare(Model model, HttpSession session) throws AuthorityException {
        model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/unsubVirtualNumPrepare"));

        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        return "/cart/virtual-num-main";
    }
    
    /**
     * 根据虚号查主号
     */
    @RequestMapping(value = "/queryMainInfo", method = { RequestMethod.POST })
    public String queryVirtualInfo(@RequestBody Map<String, Object> param, Model model, HttpServletResponse response) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        try {
            Map<String, Object> resMap = orderBmo.queryMainInfo(param, null, sessionStaff);
            if (ResultCode.R_SUCC.equals(resMap.get("resultCode"))) {
                ArrayList dataList = (ArrayList) resMap.get("data");
                Map<String, Object> accNbrMap = (Map<String, Object>) dataList.get(0);
                model.addAttribute("accNbrMap", accNbrMap);
            }
            model.addAttribute("resMap", resMap);
            model.addAttribute("param", param);
            return "/cart/unsub-cardnbr";
        } catch (BusinessException be) {
            return super.failedStr(model, be);
        } catch (InterfaceException ie) {
            return super.failedStr(model, ie, param, ErrorCode.QUERY_MAININFO);
        } catch (Exception e) {
            log.error("一卡双号根据虚号查询主号接口方法异常", e);
            return super.failedStr(model, ErrorCode.QUERY_MAININFO, e, param);
        }
    }
    /**
     *一卡双号虚号省退订新增黑名单 
    **/
    @RequestMapping(value = "/addBlackUserInfo", method = RequestMethod.POST)	
    public String addBlackUserInfo(Model model,HttpServletRequest request,
    		HttpServletResponse response,
    		@LogOperatorAnn String flowNum,
    		@RequestParam("evidenceFile") MultipartFile file) {
        SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        JsonResponse jsonResponse = null;
		PropertiesUtils propertiesUtils = (PropertiesUtils) SpringContextUtil.getBean("propertiesUtils");
		Map<String, Object> ftpResultMap = null;
		Map<String, Object> rMap = null;
		String mainAccNbr = request.getParameter("mainAccNbr");
		String mainAreaId = request.getParameter("mainAreaId");
		String reason = request.getParameter("blacklistReason");
		String virtualAccNbr = request.getParameter("virtualAccNbr");
		String virtualAreaId = request.getParameter("virtualAreaId");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("areaId", virtualAreaId);
		ArrayList<Map<String, Object>> data =  new ArrayList<Map<String, Object>>();
		Map<String, Object> dataMaps = new HashMap<String, Object>();
		dataMaps.put("mainAccNbr", mainAccNbr);
		dataMaps.put("mainAreaId", mainAreaId);
		dataMaps.put("reason", reason);
		dataMaps.put("staffCd", sessionStaff.getStaffCode());
		dataMaps.put("virtualAccNbr", virtualAccNbr);
		dataMaps.put("virtualAreaId", virtualAreaId);
		String message = "";
		String code =ResultCode.R_FAIL;
		if (null != file) {
			String fileName = file.getOriginalFilename();
			try {
				//上传文件
				if(!"ON".equals(propertiesUtils.getMessage("CLUSTERFLAG"))){
					//上传到单台FTP服务器
					ftpResultMap = ftpServiceUtils.fileUpload2FTP(file.getInputStream(), fileName, "evidenceFile");
				} else{
					//根据省份与多台FTP服务器的映射，上传文件到某台FTP服务器
					ftpResultMap = ftpServiceUtils.fileUpload2FTP4Cluster(file.getInputStream(), fileName, "evidenceFile", sessionStaff.getProvinceCode());
				}
			} catch (Exception e) {
				jsonResponse = super.failed(ErrorCode.FTP_UPLOAD_ERROR, e, rMap);
			};

			//若上传文件发生异常，则直接返回异常信息不再继续执行
			if(jsonResponse == null){
				try {
					if (ftpResultMap != null && ResultCode.R_SUCCESS.equals(ftpResultMap.get("code").toString())) {
						//上传成功后，入参中填充FTP信息
						dataMaps.put("fileUrl",ftpResultMap.get("ftpInfos"));
						data.add(dataMaps);
						param.put("data", data);
						//新增黑名单
						Map<String, Object> returnMap = orderBmo.addBlackUserInfo(param, null, sessionStaff);
						if (ResultCode.R_SUCC.equals(returnMap.get("resultCode"))) {
							message = "增加黑名单成功";
							ArrayList<Map<String, Object>> retrunData =  new ArrayList<Map<String, Object>>();
							if(returnMap.get("data")!=null){
								retrunData = (ArrayList<Map<String, Object>>) returnMap.get("data");
								Map<String, Object> respData = retrunData.get(0);
								message =  (String) respData.get("failReason");
							}
							code = ResultCode.R_SUCC;
			            }else{
			            	jsonResponse = super.failed(returnMap.get("resultMsg"), ResultConstant.SERVICE_RESULT_FAILTURE.getCode());
			            }
		 			}else{
		 				if(ftpResultMap == null || ftpResultMap.get("mess") == null)
		 					jsonResponse = super.failed("黑名单证据文件上传失败", ResultConstant.SERVICE_RESULT_FAILTURE.getCode());
		 				else{
		 					jsonResponse = super.failed(ftpResultMap.get("mess").toString(), ResultConstant.SERVICE_RESULT_FAILTURE.getCode());
		 				}
		 			}
				} catch (BusinessException be) {
					jsonResponse = super.failed(be);
				} catch (InterfaceException ie) {
					jsonResponse = super.failed(ie, rMap, ErrorCode.ADD_BLACK_USERINFO);
				} catch (Exception e) {
					jsonResponse = super.failed(ErrorCode.ADD_BLACK_USERINFO, e, rMap);
				}
			}else{
					jsonResponse = super.failed("黑名单证据文件上传失败", ResultConstant.SERVICE_RESULT_FAILTURE.getCode());
			}
		}else{
			jsonResponse = super.failed("文件读取失败,加入黑名单失败", ResultConstant.SERVICE_RESULT_FAILTURE.getCode());
		}
		model.addAttribute("message",message);
		model.addAttribute("code",code);
		if(jsonResponse!=null){
			model.addAttribute("errorStack",jsonResponse);
		}
		return "/cart/blacklist-add-confirm";
    }
    
    /**
     * 电子档案查询-主页面
     * 
     * @param model
     * @param session
     * @param flowNum
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/queryElecRecodeMain", method = RequestMethod.GET)
    @AuthorityValid(isCheck = true)
    public String queryElecRecodeMain(Model model, HttpSession session) throws AuthorityException {
    	model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/queryElecRecodeMain"));
        
    	SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
        
    	//判别用户是否具有查询电子档案的权限
        /*String isCanQueryElecRecoed = null;
        try {
            if (isCanQueryElecRecoed == null) {
            	isCanQueryElecRecoed = staffBmo.checkOperatSpec(SysConstant.ELEC_RECODE_AUTH_CODE, sessionStaff);
                //暂不将该权限写入session
                //ServletUtils.setSessionAttribute(super.getRequest(),SysConstant.ELEC_RECODE_AUTH_CODE, isCanQueryElecRecoed);
            }
        } catch (Exception e) {
        	isCanQueryElecRecoed = "1";
        }*/

        Map<String, Object> defaultAreaInfo = CommonMethods.getDefaultAreaInfo_MinimumC3(sessionStaff);
        
    	Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String end = sdf.format(calendar.getTime());
		calendar.add(Calendar.DATE, -1);
		String start = sdf.format(calendar.getTime());
		model.addAttribute("startDt", start);
		model.addAttribute("endDt", end);
		model.addAttribute("recodeFlag","Y");
		
		model.addAttribute("p_areaId", defaultAreaInfo.get("defaultAreaId"));
	    model.addAttribute("p_areaId_val", defaultAreaInfo.get("defaultAreaName"));
		//model.addAttribute("isCanQueryElecRecoed", isCanQueryElecRecoed);
	    List<Map<String,String>> p_srcFlagList = MDA.PSRCFLAGLIST;
	    model.addAttribute("p_srcFlagList", p_srcFlagList );
        return "/cart/elec_record_main";
    }
    
    /**
     * 电子档案查询-查询列表
     * 
     * @param model
     * @param session
     * @param flowNum
     * @return
     * @throws AuthorityException
     * @throws UnsupportedEncodingException 
     */
    @RequestMapping(value = "/queryElecRecodeList", method = RequestMethod.GET)
    public String queryElecRecodeList(HttpServletRequest request,Model model, HttpSession session) throws AuthorityException, UnsupportedEncodingException {
    	model.addAttribute("current", EhcacheUtil.getCurrentPath(session, "report/queryEleRecodeList"));
        
    	SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
    	
    	Map<String,Object> inParam = new HashMap<String,Object>();
    	String areaId = request.getParameter("areaId");
    	String custName = request.getParameter("custName");
    	
    	inParam.put("areaId",areaId);
    	inParam.put("olNbr",request.getParameter("olNbr"));
    	//不传默认为WEB无纸化，翼销售需传 "APP",苹果直营店传"APPLE"
    	inParam.put("srcFlag",request.getParameter("srcFlag"));
    	
    	if(null != custName && !custName.equals("")){
    		custName = EncodeUtils.urlDecode(custName);
    		inParam.put("custName", EncodeUtils.urlDecode(custName));
    	}
    	
    	//inParam.put("certType", request.getParameter("certType"));
    	//inParam.put("certNumber", request.getParameter("certNumber"));
    	inParam.put("accNbr",request.getParameter("accNbr"));
    	inParam.put("startDt", request.getParameter("startDt"));
    	inParam.put("endDt", request.getParameter("endDt"));
        inParam.put("ifTransfer", request.getParameter("ifTransfer"));
    	
    	int pageSize = Integer.parseInt(request.getParameter("pageSize"));
    	int pageIndex = Integer.parseInt(request.getParameter("pageIndex"));
    	int totalSize = 0;
    	
    	inParam.put("nowPage", pageIndex);
    	inParam.put("pageSize", pageSize);
        
    	try{
    		
    		Map<String, Object> returnMap = orderBmo.queryElecRecordList(inParam, null, sessionStaff);
    		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
    		if (returnMap != null && ResultCode.R_SUCC.equals(returnMap.get("resultCode"))) {
    			list = (List<Map<String, Object>>) returnMap.get("receiptOrders");
    			if(list!=null && list.size()>0){
    				for(int i=0;i<list.size();i++){
    					Map<String,Object> receiptOrder = list.get(i);
    					if(null!=receiptOrder.get("custName")){
    						String custN = receiptOrder.get("custName").toString();
    						if(!custN.equals("") && !custN.contains("*")){
    							receiptOrder.put("custName",DataConcealUtil.toConceal(custN, "1"));
    						}
    					}
    					if(null!=receiptOrder.get("certNumber")){
    						String certN = receiptOrder.get("certNumber").toString();
    						if(!certN.equals("") && !certN.contains("*")){
    							receiptOrder.put("certNumber",DataConcealUtil.toConceal(certN, "2"));
    						}
    					}
    				}
    			}
    			if(null != returnMap.get("totalCnt")){
    				totalSize = MapUtils.getInteger(returnMap, "totalCnt");
    			}
            }
    		PageModel<Map<String, Object>> pm = PageUtil.buildPageModel(pageIndex, pageSize, totalSize < 1 ? 1
                    : totalSize, list);
    		
            model.addAttribute("pageModel", pm);
            model.addAttribute("areaId", areaId);
            model.addAttribute("code", returnMap.get("resultCode"));
            model.addAttribute("mess", returnMap.get("resultMsg"));
    	} catch (BusinessException be) {
            return super.failedStr(model, be);
        } catch (InterfaceException ie) {
            return super.failedStr(model, ie, inParam, ErrorCode.QUERY_ELEC_RECORD);
        } catch (Exception e) {
            return super.failedStr(model, ErrorCode.QUERY_ELEC_RECORD, e, inParam);
        }
        return "/cart/elec_record_list";
    }
    
    /**
     * 电子档案查询-回执下载
     * 
     * @param model
     * @param session
     * @param flowNum
     * @return
     * @throws AuthorityException
     */
    @RequestMapping(value = "/downLoadElecRecord", method = RequestMethod.POST)
    public void downLoadElecRecord(@RequestParam("downElecRecodeFile") String params,
			@LogOperatorAnn String flowNum, 
			HttpServletRequest request,
			HttpServletResponse response, 
			HttpSession session) throws AuthorityException {
    	
    	SessionStaff sessionStaff = (SessionStaff) ServletUtils.getSessionAttribute(super.getRequest(),
                SysConstant.SESSION_KEY_LOGIN_STAFF);
    	
    	Map<String,Object> inParam = new HashMap<String,Object>();
    	try{
    		
    		inParam = JsonUtil.toObject(params, Map.class);
    		
//    		//不传默认为WEB无纸化，翼销售需传 "APP"
//        	inParam.put("srcFlag", "APP");
    		
    		Map<String, Object> resultMap = orderBmo.downLoadElecRecordPdf(inParam, null, sessionStaff);
			if (resultMap!=null&& ResultCode.R_SUCC.equals(resultMap.get("resultCode").toString())) {
				String fileName = System.currentTimeMillis()+"_"+inParam.get("olId");
				CommonUtils.downLoadPdf(resultMap, fileName, response);
			} else {
				//试试转到错误页面
				request.setAttribute("errorMsg", resultMap.get("resultMsg"));
				request.getRequestDispatcher("/error/500.jsp").forward(request, response);
			}
    	} catch (BusinessException e) {
			try {
				request.getRequestDispatcher("/error/500.jsp").forward(request, response);
			} catch (Exception e1) {
			}
		} catch (InterfaceException ie) {
			JsonResponse jsonResponse = failed(ie, inParam, ErrorCode.DOWN_LOAD_ELEC_RECORD);
			Map<String, Object> errorMap = new HashMap<String, Object>();
			errorMap.put("code", "-2");
			errorMap.put("data", jsonResponse.getData());
			String errorJson = JsonUtil.toString(errorMap);
			request.setAttribute("errorJson", errorJson);
			try {
				request.getRequestDispatcher("/error/500.jsp").forward(request, response);
			} catch (Exception e) {
			}
		} catch (Exception e) {
			JsonResponse jsonResponse = failed(ErrorCode.DOWN_LOAD_ELEC_RECORD, e, inParam);
			Map<String, Object> errorMap = new HashMap<String, Object>();
			errorMap.put("code", "-2");
			errorMap.put("data", jsonResponse.getData());
			String errorJson = JsonUtil.toString(errorMap);
			request.setAttribute("errorJson", errorJson);
			try {
				request.getRequestDispatcher("/error/500.jsp").forward(request, response);
			} catch (Exception e1) {
			} 
		}
    }
}
