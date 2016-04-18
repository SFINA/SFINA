function [mpc_out,mpc_casc_out,shedding] = load_shedding(mpc,mpc_casc)
shedding=0;
mpc_out=mpc;

define_constants

[C,ia,ib] = intersect(mpc.bus_name,mpc_casc.bus_name);

slack_bus=find(mpc_casc.bus(:,BUS_TYPE)==3);
if isempty(slack_bus)
    mpc_casc.bus(mpc_casc.gen(1,1)==mpc_casc.bus(:,1),BUS_TYPE)=3;
    slack_bus=find(mpc_casc.bus(:,BUS_TYPE)==3);
end
slack_index=find(mpc_casc.gen(:,GEN_BUS)==mpc_casc.bus(slack_bus,BUS_I));

if (sum(mpc_casc.gen(:,PG))<sum(mpc_casc.bus(mpc_casc.bus(:,PD)~=0,PD)))
    mpc_casc.bus(mpc_casc.bus(:,PD)~=0,PD)= mpc_casc.bus(mpc_casc.bus(:,PD)~=0,PD)*1*(sum(mpc_casc.gen(:,PG))/sum(mpc_casc.bus(mpc_casc.bus(:,PD)~=0,PD)));
    mpc_casc.bus(mpc_casc.bus(:,QD)~=0,QD)= mpc_casc.bus(mpc_casc.bus(:,QD)~=0,QD)*1*(sum(mpc_casc.gen(:,QG))/sum(mpc_casc.bus(mpc_casc.bus(:,QD)~=0,QD)));
    mpc_out.bus(ia,[PD QD])=mpc_casc.bus(ib,[PD QD]);
    
end

mpc_casc_out=mpc_casc;

