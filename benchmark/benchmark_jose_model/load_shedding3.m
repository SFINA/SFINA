function [mpc_out,mpc_casc_out,shedding,blackout] = load_shedding3(mpc,mpc_casc,iteration)
shedding=0;
blackout=0;
mpc_out=mpc;

define_constants


[C,ia,ib] = intersect(mpc.bus_name,mpc_casc.bus_name);
[C_gen,gen_a,gen_b] = intersect(mpc.gen_name,mpc_casc.gen_name);

slack_bus=find(mpc_casc.bus(:,BUS_TYPE)==3);
if isempty(slack_bus)
    mpc_casc.bus(mpc_casc.gen(1,1)==mpc_casc.bus(:,1),BUS_TYPE)=3;
    slack_bus=find(mpc_casc.bus(:,BUS_TYPE)==3);
end
slack_index=find(mpc_casc.gen(:,GEN_BUS)==mpc_casc.bus(slack_bus,BUS_I));

if iteration < 14
    mpc_casc.bus(mpc_casc.bus(:,PD)~=0,PD)= mpc_casc.bus(mpc_casc.bus(:,PD)~=0,PD)*0.95;
    mpc_casc.bus(mpc_casc.bus(:,QD)~=0,QD)= mpc_casc.bus(mpc_casc.bus(:,QD)~=0,QD)*0.95;
    mpc_out.bus(ia,[PD QD])=mpc_casc.bus(ib,[PD QD]);
else
    mpc_casc.bus(:,[PD QD]) = 0;
    mpc_out.bus(ia,[PD QD])=mpc_casc.bus(ib,[PD QD]);
    mpc_casc.gen(:,[PG QG]) = 0;
    mpc_out.gen(gen_a,[PG QG])= mpc_casc.gen(gen_b,[PG QG]);
    display('blackout');
    blackout=1;
    
end

mpc_casc_out=mpc_casc;

