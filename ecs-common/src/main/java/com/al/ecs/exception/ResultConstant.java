package com.al.ecs.exception;
/** 
 *  结果编码定数类.
 *<P>
 *   结果编码定数类
 *<P>
 * @author			tang zheng yu
 * @version			V1.0 2011-12-23 
 * @CreateDate	2011-12-23 上午11:32:28
 * @CopyRight	亚信联创电信CRM研发部
 */

public final class ResultConstant {

	/*------此结果码，门户层用来统一返回给前端用户展示----------*/
	/** 成功. */
	public static final Result SUCCESS = new Result(0, "成功");
	/** 失败. */
	public static final Result FAILD = new Result(1, "失败");
	/** 接口调用失败. */
	public static final Result INTF_FAILD = new Result(11, "接口调用失败");
	/** 接口调用异常. */
	public static final Result INTF_EXCEPTION = new Result(12, "接口调用异常");
	/** 打印异常. */
	public static final Result PRINT_EXCEPTION = new Result(21, "打印异常");
	/** 系统错误或异常. */
	public static final Result SYS_ERROR = new Result(1000, "门户层系统错误或异常");
	/** 用户入参不合法,错误. */
	public static final Result IN_PARAM_FAILTURE = new Result(1001, "用户提交入参错误");
	/** 业务层返回结果失败或异常. */
	public static final Result SERVICE_RESULT_FAILTURE = new Result(1002, "业务层返回结果失败");
	/** 业务层返回结果为空. */
	public static final Result SERVICE_RESULT_EMPTY = new Result(1006, "业务层返回结果为空");
	/** DAO层返回结果失或异常. */
	public static final Result DAO_RESULT_FAILTURE = new Result(1003, "DAO层返回结果失败");
	/** 数据校验不通过. */
	public static final Result DATA_NOT_VALID_FAILTURE = new Result(1004, "数据业务校验不通过");
	/** 图片验证码不对. */
	public static final Result IMAGE_CODE_INVALID = new Result(1005, "图片验证码不对!");
	/** 会话过期. */
	public static final Result SESSION_EXPIRE = new Result(1100, "会话过期");
	/** 用户未登录. */
	public static final Result SESSION_INVALID = new Result(1101, "用户未登录");
	/**用户验证失败超过十次，账号已被锁定*/
	public static final Result  USER_LOCK=new Result(1102,"验证失败超过十次，账号已被锁定");
	/** 针对ＵＲＬ，资源无权访问限访问. */
	public static final Result ACCESS_NOT_AUTHORITY_URL = new Result(1103, "无权限访问");
	/** 针对数据无权限访问. */
	public static final Result ACCESS_NOT_AUTHORITY_DATA= new Result(1104, "无权限访问");
	
	/** 请求过度频繁，如一定时间内超出一定数量请求. */
	public static final Result ACCESS_LIMIT_FAILTURE = new Result(1201, "用户请求次数过度");
	/** 非法请求. */
	public static final Result ACCESS_NOT_NORMAL = new Result(1202, "用户非法请求");
	/** 控制层异常拦截. */
	public static final Result ACTION_RESULT_FAILTURE = new Result(1500, "控制层各种异常信息拦截！");
	/** 文件上传处理异常. */
	public static final Result ACTION_RESULT_FILE_UPLOAD_FAILTURE = new Result(1601, "控制层文件上传处理异常");
	/** 文件下载处理异常. */
    public static final Result ACTION_RESULT_FILE_DOWNLOAD_FAILTURE = new Result(1602, "控制层文件下载处理异常");
	

    /*------此结果码，服务层用来统一返回给门户，前缀为POR为门户入参异常编码，SERV为服务层内部异常编码----------*/
    /*常用编码*/
    /** 成功. */
	public static final ServResult R_POR_SUCCESS = new ServResult("POR-0000", "成功");
	/**失败*/
	public static final ServResult R_POR_FAIL = new ServResult("POR-2004", "未知失败");
	/**门户入参异常：缺失入参*/
	public static final ServResult R_POR_PARAM_MISS = new ServResult("POR-2003", "门户入参异常：缺失入参");
	/**门户入参异常：所传非法参数*/
	public static final ServResult R_POR_ERROR_PARAM = new ServResult("POR-1006", "门户入参异常：所传非法参数");
	/**门户请求超时*/
	public static final ServResult R_POR_TIME_OUT_FAIL = new ServResult("POR-2005", "门户请求超时");	
	/**门户入参异常：所传门户编码未注册*/
	public static final ServResult R_POR_PORTAL_NO_REGIST = new ServResult("POR-1002", "门户入参异常：所传门户编码未注册");
	/**门户入参异常：所传门户编码不能使用*/
	public static final ServResult R_POR_PORTAL_PAUSE = new ServResult("POR-1003", "门户入参异常：所传门户编码不能使用");
	/**门户入参异常：所传门户密码错误*/
	public static final ServResult R_POR_PORTAL_PWD_WRONG = new ServResult("POR-1004", "门户入参异常：所传门户密码错误");
	/**门户入参异常：所传服务编码对应服务不具备使用权限*/
	public static final ServResult R_POR_SERV_NO_COMPETENCE = new ServResult("POR-1005", "门户入参异常：所传服务编码对应服务不具备使用权限");
	/**服务错误*/
	public static final ServResult R_POR_SERV_ERROR = new ServResult("POR-1008", "门户入参异常：服务编码所对应服务错误");
	/**服务暂停*/
	public static final ServResult R_POR_SERV_PAUSE = new ServResult("POR-1009", "门户入参异常：服务编码所对应服务暂停");
	/**服务故障*/
	public static final ServResult R_POR_SERV_FAULT = new ServResult("POR-1010", "门户入参异常：服务编码所对应服务故障");
	/**服务停止*/
	public static final ServResult R_POR_SERV_STOP = new ServResult("POR-1011", "门户入参异常：服务编码所对应服务停止");
	/**服务异常*/
	public static final ServResult R_POR_SERV_EXCEPTION = new ServResult("POR-1012", "门户入参异常：服务编码所对应服务异常");
	/**查询失败*/
	public static final ServResult R_POR_QUERY_FAIL = new ServResult("SEVR-2001", "查询失败");
	/**查询不到数据*/
	public static final ServResult R_POR_QUERY_NO_DATA = new ServResult("SEVR-2002", "查询不到数据");
	/**服务层未知错误*/
	public static final ServResult R_POR_UNKNOWN_ERROR = new ServResult("SEVR-1999", "服务平台内部未知错误");
	/**调用服务平台失败*/
	public static final ServResult R_POR_SERV_CALL_FAIL = new ServResult("SEVR-1001", "调用服务平台失败");
	/**拒绝访问*/
	public static final ServResult R_POR_ACCESS_DENIED = new ServResult("SEVR-1007", "服务平台拒绝访问");
	/**服务层数据库访问异常*/
	public static final ServResult R_SERV_DATABASE_EXCEPTION = new ServResult("SERV-1013", "服务平台内部数据库访问异常");
	/**服务平台内部数据转换异常*/
	public static final ServResult R_SERV_DATACONVERSION = new ServResult("SERV-1015", "服务平台内部数据转换异常");
	/**接口服务错误*/
	public static final ServResult R_SERV_INTF_ERROR = new ServResult("INTF-3001", "无法获取Operation，请确认该外围接口名称是否存在！");
	/**接口服务暂停使用*/
	public static final ServResult R_INTF_PAUSE = new ServResult("INTF-3002", "接口服务暂停使用");
	/**外围接口地址不可用*/
	public static final ServResult R_INTF_STOP = new ServResult("INTF-3003", "外围接口地址不可用");
	/**生成接口请求参数失败*/
	public static final ServResult R_INTF_PARAM_FAIL = new ServResult("INTF-3004", "接口请求参数生成失败");
	/**接口请求参数无效*/
	public static final ServResult R_INTF_PARAM_WRONG = new ServResult("INTF-3004", "接口请求参数无效");
	/**请求参数缺失*/
	public static final ServResult R_INTF_PARAM_MISS = new ServResult("INTF-3005", "请求参数缺失");
	/**外围接口访问异常*/
	public static final ServResult R_INTERFACE_CALL_EXCEPTION = new ServResult("INTF-3006", "外围接口访问异常");
	/**接口服务异常*/
	public static final ServResult R_INTERFACE_EXCEPTION = new ServResult("INTF-3007", "接口服务异常");
	/**接口服务未知错误*/
	public static final ServResult R_INTERFACE_UNKNOW_ERROR = new ServResult("INTF-3008", "接口服务未知错误");
	/**CSB相关错误*/
	public static final ServResult R_INTERFACE_CSB_ERROR = new ServResult("INTF-3009", "CSB相关错误");
	/**接口业务返回相关错误*/
	public static final ServResult R_INTERFACE_BUSI_ERROR = new ServResult("INTF-3010", "返回业务相关错误");
	/**接口服务返回参数异常*/
	public static final ServResult R_INTERFACE_RESP_EXCEPTION = new ServResult("INTF-3007", "接口服务异常");
	/** 靓号调级：靓号非在用状态（1004），无法受理 */
	public static final Result PHONE_LEVEL_MODIFY_STATUS_ERROR = new Result(1002, "号码非在用状态，无法受理");
}
