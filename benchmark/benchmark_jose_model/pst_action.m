function [mpc_out,active_pst,active_ib] = pst_action (mpc,pst)

define_constants;
Settings=sdpsettings('solver','mosek','verbose',0);
mpoptions=mpoption('out.all',0);
mpoptions=mpoption(mpoptions,'model','DC');
n_branches= length(mpc.branch(:,1));

for b = 1:length(pst)
    pst_name{b} = sprintf('branch %d', pst(b));
end

[C,ia,ib] = intersect(mpc.branch_name,pst_name);
% mpc.branch(ia,10)=0;
results=runpf(mpc,mpoptions);
load = ((results.branch(:,14))./mpc.branch(:,RATE_C));



active_pst=[];
active_ib=[];
if (~isempty(ib))
    
    ctr = ctr_br1(ia,mpc)';
    if ~isempty(ctr)
        active_pst=ia(any(abs(ctr)>0.01));
        active_ib=ib(any(abs(ctr)>0.01));
%         n_ib=length(ib);
        
        actual=mpc.branch(active_pst,10);
        actual_angle=mpc.branch(ia,10);
        
%         cvx_begin
%             variable x(n_branches)
%             variable angle_pst(n_ib)
%             minimize( norm( x, 2 ) )
%             subject to
%                 x==load - (angle_pst'*ctr')'
%                 norm( angle_pst, 1 ) <= 7
%                 norm( angle_pst,1 )+ norm( actual_angle, 1 ) <= 16
%         cvx_end
%         
%         if ~isempty(active_pst)  && length (actual) == length((angle_pst(any(ctr>0.01)))) && ~any(isnan((angle_pst)))
%             mpc.branch(active_pst,10)= actual + (angle_pst(any(ctr>0.01)));
%         end
        
        x=sdpvar(n_branches,1);
        angle=sdpvar(length(ib),1);
        
        u=binvar(2*length(ib),1);
        y=binvar(2*n_branches,1);
        z=binvar(2*n_branches,1);
        w=binvar(2*n_branches,1);
        
        Constraints=[-7<=angle<=7,-30 <=x<= 30 ];
        Constraints=[Constraints,abs(angle)+abs(actual_angle)<=16];
        Constraints= [Constraints, x==load - (angle'*ctr')'];
        
        Constraints= [Constraints, implies(-0.6 >= x >= 0.6,y)];
        Constraints= [Constraints, implies(-0.8 >= x >= 0.8,z)];
        Constraints= [Constraints, implies(-0.95 >= x >= 0.95,w)];
        Constraints= [Constraints, implies(-0.95 >= x (ia) >= 0.95,u)];
        
%         Objective= 1*sum(y) + 10*sum(z) + 100*sum(w) + 1000*sum(u);
%         Objective= 1*sum(y./[btw_weight;btw_weight]) + 10*sum(z./[btw_weight;btw_weight]) + 100*sum(w./[btw_weight;btw_weight]);
        Objective = norm(x,2);
%         Objective = sum(abs(x));
%         sol=optimize(Constraints,Objective);
        sol=optimize(Constraints,Objective,Settings);
        
        
        if ~isempty(active_pst)  && length (actual) == length(value(angle(any(ctr>0.01)))) && ~any(isnan(value(angle)))
            mpc.branch(active_pst,10)= actual + value(angle(any(ctr>0.01)));
        end

        mpc_out=mpc;
        
    else
        mpc_out=mpc;
    end
else
    mpc_out=mpc;
end
