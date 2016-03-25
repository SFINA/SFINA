function ctr = ctr_br1(pst,mpc)

% Define options for MatPower(R)
mpoptions=mpoption('out.all',0);
mpoptions=mpoption(mpoptions,'model','DC');
% Number of PST on the system
N_pst=length(pst);

% Number of branches in the system
N_branch=length(mpc.branch(:,1));
% display(mpc.branch_name(pst));
% Iteration among the PSTs
for i = 1 : N_pst
    if mpc.branch(pst(i),11)==1
        % Runs the DC Power Flow with Matpoer(R)
        results=runpf(mpc,mpoptions);
        
        % Real Power injected to bus end before the action of the PST
        y1(i,:)=results.branch(:,14);
        
        actual=mpc.branch(pst(i),10);
        % Set the angle of the PST to 1 degree
        mpc.branch(pst(i),10)=actual+1;
        
        % Run DC power flow after the new value of the PST
        results=runpf(mpc,mpoptions);
        
        % Real Power injected to bus end after the change in angle of the PST
        y2(i,:)=results.branch(:,14);
        
        % Difference between the Real Power before and after the action of the
        % PST, divided by the maximum Power each line can withstand
        weigth(i,:)=(y1(i,:)-y2(i,:))./mpc.branch(:,8)';
        
        % Restore value of PST to 0 degrees for next calculation
        mpc.branch(pst(i),10)=actual;
    else
        weigth(i,:)=zeros(1,N_branch);
        
    end
    
end

ctr=weigth;