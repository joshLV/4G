<?xml version="1.0" encoding="UTF-8"?>
<ContractRoot>
	<TcpCont>
	    <TransactionID>${TcpCont.TransactionID}</TransactionID>
		<ActionCode>${TcpCont.ActionCode}</ActionCode>
		<BusCode>${TcpCont.BusCode}</BusCode>
        <ServiceCode>${TcpCont.ServiceCode}</ServiceCode>
        <ServiceContractVer>${TcpCont.ServiceContractVer}</ServiceContractVer>
		<ServiceLevel>${TcpCont.ServiceLevel}</ServiceLevel>
		<SrcOrgID>${TcpCont.SrcOrgID}</SrcOrgID>
		<SrcSysID>${TcpCont.SrcSysID}</SrcSysID>
		<SrcSysSign>${TcpCont.SrcSysSign}</SrcSysSign>
		<DstOrgID>${TcpCont.DstOrgID}</DstOrgID>
		<DstSysID>${TcpCont.DstSysID}</DstSysID>
		<ReqTime>${TcpCont.ReqTime}</ReqTime>	
	</TcpCont>
	<SvcCont>
		<RatableResourceQueryReq>
			<Bill_Information>
				<Acc_Nbr>${phoneNumber}</Acc_Nbr>
				<Destination_Attr>2</Destination_Attr>
				<Billing_Cycle>${billingCycle}</Billing_Cycle>
			</Bill_Information>
			<Mvno_Id>${ownerId}</Mvno_Id>
		</RatableResourceQueryReq>	
	</SvcCont>
</ContractRoot>
