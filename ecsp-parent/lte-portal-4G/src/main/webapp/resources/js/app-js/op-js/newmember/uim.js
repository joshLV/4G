/**
 * uim卡管理
 * 
 * @author wukf
 * date 2013-08-22
 */
CommonUtils.regNamespace("prod","uim");

prod.uim = (function() {
	
	//uim卡号校验
	var _checkUim = function(prodId){
		var phoneNumber = OrderInfo.getAccessNumber(prodId);
		var offerId = "-1"; //新装默认，主销售品ID
		if(OrderInfo.actionFlag==1||OrderInfo.actionFlag==6||OrderInfo.actionFlag==14){ //新装需要选号
			if(phoneNumber==''){
				$.alert("提示","校验UIM卡前请先选号!");
				return false;
			}
		}
		if(OrderInfo.actionFlag==3 || OrderInfo.actionFlag==22 || OrderInfo.actionFlag==23 || OrderInfo.actionFlag==6 ){ //可选包变更，补换卡，加装副卡
			if(ec.util.isArray(OrderInfo.oldprodInstInfos)){//判断是否是纳入老用户
				$.each(OrderInfo.oldprodInstInfos,function(){
					if(this.prodInstId==prodId){
						offerId = this.mainProdOfferInstInfos[0].prodOfferInstId;
					}
				});
			}else{
				offerId = order.prodModify.choosedProdInfo.prodOfferInstId;
			}
		}
		var cardNo =$.trim($("#uim_txt_"+prodId).val());
		if(cardNo==undefined || cardNo==''){
			$.alert("提示","UIM卡不能为空!");
			return false;
		}
		var inParam = {
			"instCode" : cardNo,
			"phoneNum" : phoneNumber,
			"areaId"   : OrderInfo.getProdAreaId(prodId)
//			"areaId"   : '8320102'
		};

		var prodSpecId = OrderInfo.getProdSpecId(prodId);
		var mktResCd="";
		if(CONST.getAppDesc()==0){
			if(getIsMIFICheck(prodId)){
				mktResCd =getMktResCd(CONST.PROD_SPEC_ID.MIFI_ID);
			}else{
				mktResCd = getMktResCd(prodSpecId);
			}
			if(ec.util.isObj(mktResCd)){
//				inParam.mktResCd = mktResCd;
			}else{
				$.alert("提示","查询卡类型失败！");
				return;
			}
			if(OrderInfo.actionFlag==22 || OrderInfo.actionFlag==23){ //补换卡和异地补换卡
				if(prod.changeUim.is4GProdInst){ //如果已办理4G业务，则校验uim卡是否是4G卡
					inParam.onlyLTE = "1";
				}
			}
		}
		var data = query.prod.checkUim(inParam);//校验uim卡
		if(data==undefined || data.baseInfo==undefined){
			return false;
		}
		//根据uim返回数据组织物品节点
		var couponNum = data.baseInfo.qty ;
		if(couponNum==undefined||couponNum==null){
			couponNum = 1 ;
		}
		var coupon = {
			couponUsageTypeCd : "3", //物品使用类型
			inOutTypeId : "1",  //出入库类型
			inOutReasonId : 0, //出入库原因
			saleId : 1, //销售类型
			couponId : data.baseInfo.mktResId, //物品ID
			couponinfoStatusCd : "A", //物品处理状态
			chargeItemCd : "3000", //物品费用项类型
			couponNum : couponNum, //物品数量
			storeId : data.baseInfo.mktResStoreId, //仓库ID
			storeName : "1", //仓库名称
			agentId : 1, //供应商ID
			apCharge : 0, //物品价格
			couponInstanceNumber : data.baseInfo.mktResInstCode, //物品实例编码
			terminalCode : data.baseInfo.mktResInstCode,//前台内部使用的UIM卡号
			ruleId : "", //物品规则ID
			partyId : OrderInfo.cust.custId, //客户ID
			prodId :  prodId, //产品ID
			offerId : offerId, //销售品实例ID
			state : "ADD", //动作
			relaSeq : "" //关联序列	
		};
		if(CONST.getAppDesc()==0){
			coupon.cardTypeFlag=data.baseInfo.cardTypeFlag;//UIM卡类型
		}
		//释放按钮
		$("#uim_check_btn_"+prodId).attr("disabled",true);
		//var release=$("#uim_check_btn_"+prodId).attr('class');
		$("#uim_release_btn_"+prodId).removeAttr("disabled");
		
		$("#uim_txt_"+prodId).attr("disabled",true);
		if(getIsMIFICheck(prodId)){//判断是否通过MIFI 校验
			$("#isMIFI_"+prodId).val("yes");
		}else{
			$("#isMIFI_"+prodId).val("no");
		}
		OrderInfo.clearProdUim(prodId);
		OrderInfo.boProd2Tds.push(coupon);
		if(OrderInfo.actionFlag==22 && data.baseInfo.cardTypeFlag==1){
		//	_queryAttachOffer();
		  AttachOffer.queryCardAttachOffer(data.baseInfo.cardTypeFlag);  //加载附属销售品
	    }
	};
	
	/*
	 * 是否是MIFI卡类型校验
	 */
	var getIsMIFICheck=function(prodId){
		var prodIdTmp=(Math.abs(prodId)-1);
		if(AttachOffer.openServList.length>prodIdTmp){
			var specList = AttachOffer.openServList[prodIdTmp].servSpecList;
			for (var j = 0; j < specList.length; j++) {
				var spec = specList[j];
				if(spec.isdel!="Y" && spec.isdel!="C"){
					if(spec.servSpecId==CONST.PROD_SPEC_ID.MIFI_ID && CONST.APP_DESC==0){
						return true ; 
					}
				}
			}
		}
		return false;
	};
	
	var getMktResCd = function(prodSpecId){
		var param={
			"prodSpecId":prodSpecId
		};
		var url = contextPath+"/mktRes/uim/getMktResCd";
		$.ecOverlay("<strong>获取产品规格中,请稍等...</strong>");
		var response = $.callServiceAsJson(url,param);
		$.unecOverlay();
		if (response.code == "0") {
			return response.data;
		}else{
			return "";
		}
	};
	
	//uim卡号释放
	var _releaseUim = function(prodId){	
		var cardNo =$.trim($("#uim_txt_"+prodId).val());
		if(cardNo==undefined || cardNo==''){
			$.alert("提示","UIM卡不能为空!");
			return false;
		}
/*		var phoneNumber = OrderInfo.getAccessNumber(prodId);
		var inParam = {
			"oldInstCode":cardNo,
			"phoneNum":phoneNumber
		};
		var data = query.prod.releaseUim(inParam);//释放uim卡
		if(data==undefined){
			return false;
		}*/
		//释放UIM并更新门户记录
		var param = {
				numType : 2,
				numValue : cardNo
		};
		var jr = $.callServiceAsJson(contextPath+"/app/mktRes/phonenumber/releaseErrorNum", param);			
		if(jr.code==-2){
			$.alertM(jr.data);
			return;
		}
		if(jr.code==-1){
			$.alert("提示",jr.data);
			return;
		}
		$.alert("提示","成功释放UIM卡："+cardNo);
		if(OrderInfo.actionFlag==22){
			$('#attach').children().remove();
	    }
		$("#uim_check_btn_"+prodId).attr("disabled",false);
		//$("#uim_check_btn_"+prodId).removeClass("disablepurchase").addClass("purchase");
		$("#uim_release_btn_"+prodId).attr("disabled",true);
		//$("#uim_release_btn_"+prodId).removeClass("purchase").addClass("disablepurchase");
		$("#uim_txt_"+prodId).attr("disabled",false);
		$("#uim_txt_"+prodId).val("");
		OrderInfo.clearProdUim(prodId);
	};
	
	//保存旧卡
	var _setOldUim = function(offerId ,prodId,uim){
		var oldUim={
			couponUsageTypeCd : "3", //物品使用类型
			inOutTypeId : "2",  //出入库类型
			inOutReasonId : 0, //出入库原因
			saleId : 1, //销售类型
			couponId :uim.couponId, //物品ID
			couponinfoStatusCd : "A", //物品处理状态
			chargeItemCd : CONST.ACCT_ITEM_TYPE.UIM_CHARGE_ITEM_CD, //物品费用项类型
			couponNum : 1, //物品数量
			//storeId : oldUimInfo.storeId, //仓库ID
			storeId : 1, //仓库ID
			storeName : "11", //仓库名称
			agentId : 1, //供应商ID
			apCharge : 0, //物品价格
			couponInstanceNumber : uim.couponInsNumber, //物品实例编码
			terminalCode : uim.couponInsNumber,//前台内部使用的UIM卡号
			ruleId : "", //物品规则ID
			partyId : OrderInfo.cust.custId, //客户ID
			prodId : prodId, //产品ID
			offerId : offerId, //销售品实例ID
			state : "DEL", //动作
			relaSeq : "", //关联序列
			id:0,
			isOld : "T",  //旧卡
			is4GCard : uim.is4GCard
		};
		OrderInfo.boProd2OldTds.push(oldUim);
		order.prodModify.choosedProdInfo.extCouponInstanceId = uim.extCouponInstanceId;
		order.prodModify.choosedProdInfo.corCouponInstanceId = uim.corCouponInstanceId;
	};

	//根据UIM类型，设置产品是3G还是4G，并且保存uim卡
	var _setProdUim = function(action){
		OrderInfo.boProd2OldTds = []; //清空旧卡
		var flag = true;
		if(OrderInfo.actionFlag==6){
			if(action=="delViceCard"){
				var prod = {};
				$.each(OrderInfo.viceOfferSpec,function(){
					var prodId=this.prodId;
					$.each(OrderInfo.offer.offerMemberInfos,function(){
						if(this.objType==CONST.OBJ_TYPE.PROD&&this.objInstId==prodId){ //接入类产品
							prod.prodInstId = this.objInstId;
							prod.accNbr = this.accessNumber;
							var uim = query.prod.getTerminalInfo(prod);
							if(uim == undefined){  //查询旧卡信息失败返回
								flag = false;
								return false;
							}
							_setOldUim(this.offerId,this.objInstId,uim); //保存旧卡信息
							if(uim.is4GCard=="Y"){
								this.prodClass = CONST.PROD_CLASS.FOUR;
							}else{
								this.prodClass = CONST.PROD_CLASS.THREE;
							}
						}
					});
				});
			}
			else{
				var prod = {};
				$.each(OrderInfo.offer.offerMemberInfos,function(){
					if(this.objType==CONST.OBJ_TYPE.PROD){ //接入类产品
						prod.prodInstId = this.objInstId;
						prod.accNbr = this.accessNumber;
						var uim = query.prod.getTerminalInfo(prod);
						if(uim == undefined){  //查询旧卡信息失败返回
							flag = false;
							return false;
						}
						_setOldUim(this.offerId,this.objInstId,uim); //保存旧卡信息
						if(uim.is4GCard=="Y"){
							this.prodClass = CONST.PROD_CLASS.FOUR;
						}else{
							this.prodClass = CONST.PROD_CLASS.THREE;
						}
						var prodInfo = order.prodModify.choosedProdInfo; 
						if(prodInfo!=undefined && prodInfo.prodInstId==this.objInstId){
							prodInfo.prodClass = this.prodClass;
						}
					}
				});
			}
		}
		
		return flag;	
	};
	
	return {
		getMktResCd:getMktResCd,
		checkUim				: _checkUim,
		releaseUim 				: _releaseUim,
		setProdUim				: _setProdUim,
		setOldUim				: _setOldUim
	};
})();