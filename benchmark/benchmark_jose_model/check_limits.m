function [mpc_out,mpc_casc_out,visited_gen] = check_limits(mpc,mpc_casc,results_casc,visited_gen)

mpc_out=mpc;

define_constants

[C_gen,gen_a,gen_b] = intersect(mpc.gen_name,mpc_casc.gen_name);

slack_bus=find(mpc_casc.bus(:,BUS_TYPE)==3);
if isempty(slack_bus)
    mpc_casc.bus(mpc_casc.gen(1,1)==mpc_casc.bus(:,1),BUS_TYPE)=3;
    slack_bus=find(mpc_casc.bus(:,BUS_TYPE)==3);
end
slack_index=find(mpc_casc.gen(:,GEN_BUS)==mpc_casc.bus(slack_bus,BUS_I));

if results_casc.gen(slack_index,PG) > results_casc.gen(slack_index,PMAX)
    mpc_casc.gen(slack_index,PG) = mpc_casc.gen(slack_index,PMAX);
    mpc_casc.gen(slack_index,QG) = mpc_casc.gen(slack_index,QMAX);
    visited_gen(slack_index)=1;
    if ~all(visited_gen) && ~isempty(max(mpc_casc.gen((mpc_casc.gen(:,PMAX).*~visited_gen'>0),PMAX)))
        
        new_slack=find(mpc_casc.gen(:,PMAX).*~visited_gen'== max(mpc_casc.gen((mpc_casc.gen(:,PMAX).*~visited_gen'>0),PMAX)));
        if ~isempty(new_slack)
            mpc_casc.bus(slack_bus,BUS_TYPE)=2;
            mpc_casc.bus(ismember(mpc_casc.bus_name, sprintf('bus %d', mpc_casc.gen(new_slack(1),BUS_I))),BUS_TYPE)=3;
        else
            mpc_casc.bus(:,PD)=mpc_casc.bus(:,PD)*0.95;
            visited_gen(:)=1;
        end
    else
        visited_gen(:)=1;
        mpc_casc.bus(:,PD)=mpc_casc.bus(:,PD)*0.95;
    end

elseif results_casc.gen(slack_index,PG) < results_casc.gen(slack_index,PMIN)
    mpc_casc.gen(slack_index,PG) = mpc_casc.gen(slack_index,PMIN);
    mpc_casc.gen(slack_index,QG) = mpc_casc.gen(slack_index,QMIN);
    visited_gen(slack_index)=1;
    if ~all(visited_gen) && ~isempty(max(-mpc_casc.gen((mpc_casc.gen(:,PMAX).*~visited_gen'>0),PMAX)))
        new_slack=find(-mpc_casc.gen(:,PMAX).*~visited_gen'== max(-mpc_casc.gen((mpc_casc.gen(:,PMAX).*~visited_gen'>0),PMAX)));
        if ~isempty(new_slack)
            mpc_casc.bus(slack_bus,BUS_TYPE)=2;
            mpc_casc.bus(ismember(mpc_casc.bus_name, sprintf('bus %d', mpc_casc.gen(new_slack(1),BUS_I))),BUS_TYPE)=3;
        else
            mpc_casc.bus(:,PD)=mpc_casc.bus(:,PD)*0.95;
            visited_gen(:)=1;
        end
    else
        visited_gen(:)=1;
        mpc_casc.bus(:,PD)=mpc_casc.bus(:,PD)*0.95;
    end
end
mpc_out.gen(gen_a,[PG QG])= mpc_casc.gen(gen_b,[PG QG]);
mpc_casc_out = mpc_casc;
