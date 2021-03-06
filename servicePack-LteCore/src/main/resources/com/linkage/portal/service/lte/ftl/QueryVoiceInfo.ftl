<?xml version="1.0" encoding="UTF-8"?>
<ContractRoot>
	<TcpCont>
		<TransactionID>${TcpCont.TransactionID}</TransactionID>
		<ActionCode>${TcpCont.ActionCode}</ActionCode>
		<BusCode>BUS61004</BusCode>
		<ServiceCode>SVC83002</ServiceCode>
		<ServiceContractVer>SVC8300220131028</ServiceContractVer>
		<ServiceLevel>${TcpCont.ServiceLevel}</ServiceLevel>
		<SrcOrgID>${TcpCont.SrcOrgID}</SrcOrgID>
		<SrcSysID>${TcpCont.SrcSysID}</SrcSysID>
		<SrcSysSign>123</SrcSysSign>
		<DstOrgID>100000</DstOrgID>
		<DstSysID>1000000203</DstSysID>
		<ReqTime>${TcpCont.ReqTime}</ReqTime>
	</TcpCont>
	<SvcCont>
		<VoiceUDRQueryReq>
			<Destination_Id>${destinationId}</Destination_Id>
			<BillingCycle>${billingCycle}</BillingCycle>
			<ServType>${servType}</ServType>
			<Query_Flag>${queryFlag}</Query_Flag>
			<Query_Event_Type>${eventType}</Query_Event_Type>
			<Pages>${curPage}</Pages>
			<Count>${pageSize}</Count>
			<MvnoId>${ownerId}</MvnoId>
		</VoiceUDRQueryReq>
	</SvcCont>
</ContractRoot>
