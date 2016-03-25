function [mpc_out,mpc_casc_out,voltage_limit] = check_voltage(mpc,mpc_casc,results_casc)

mpc_out=mpc;

define_constants


open_bus= mpc_casc.bus(:,BUS_TYPE)~=4;
under_voltage = results_casc.bus(:,VM)<0.95;
over_voltage= results_casc.bus(:,VM)>1.05;
failed_bus = open_bus.*over_voltage+ open_bus.*under_voltage;



if any(failed_bus)
    [~,bus_a,~] = intersect(mpc.bus_name,mpc_casc.bus_name(failed_bus==1));
    disc_branch=any(ismember(mpc_casc.branch(:,[F_BUS,T_BUS]),mpc_casc.bus(failed_bus==1,BUS_I)),2);
    mpc_casc.branch(disc_branch,BR_STATUS)=0;
    [~,disc_branch_mpc,~]=intersect(mpc.branch_name,mpc_casc.branch_name(disc_branch));
    mpc_out.branch(disc_branch_mpc,BR_STATUS)=0;
    mpc_casc.bus(failed_bus==1,BUS_TYPE)=4;
    mpc_out.bus(bus_a,BUS_TYPE)=4;
    voltage_limit=0;
else
    voltage_limit=1;
end

mpc_casc_out = mpc_casc;