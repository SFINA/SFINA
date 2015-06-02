function [island_ccr] = update_ccr_voltage(mpc,ccr,voltage_ref_max,voltage_ref_min,results2)
%function [T,b] = branchstatus_voltage(mpc,ccr,voltage_ref_max,voltage_ref_min,results2)
define_constants;
%gives the indices of branches

%mpc=loadcase('case57.m');
k=[1:1:numel(mpc.branch(:,1))];
%n_bus = numel(mpc.bus(:,1));


ref_idx=k;
%reference indices for to and from
ref_a=[];
ref_b=[];
for j=1:length(k)
    ref_a=[ref_a;mpc.branch(k(j),1)]; 
    ref_b=[ref_b;mpc.branch(k(j),2)]; 
end

n_branches_ccr=numel(ccr.branch(:,1));
idx=[];
for l=1:n_branches_ccr
    idx=[idx;l];
end
    
a=[];
b=[];

for j=1:length(idx)
    a=[a;ccr.branch(idx(j),1)]; 
    b=[b;ccr.branch(idx(j),2)]; 
end
    
for i=1:length(a)
    for j=1:length(ref_a)
        if a(i)==ref_a(j) & b(i)==ref_b(j);
            idx(i)=ref_idx(j);
        end
    end
end
    
idx=idx';
f=ccr.branch(:,1); %registers the branches into which power injected
f=f';
n_branches_ccr=numel(ccr.branch(:,1));
n_branches_ccr
n_bus_ccr = numel(ccr.bus(:,1));
%gives the indices of branches
k=[];
for l=1:n_branches_ccr
    k=[k;l];
end
k=k';
    
reference_ccr_max=[];

reference_ccr_min=[];    
%for z=1:numel(ccr.bus(:,1))
 %   reference_ccr_max=[reference_ccr_max;voltage_ref_max(idx(z))];
  %  reference_ccr_min=[reference_ccr_min;voltage_ref_min(idx(z))];
%end
%reference_ccr_max=reference_ccr_max';
%reference_ccr_min=reference_ccr_min';   

%mpopt = mpoption('PF_ALG', 1,'PF_MAX_IT',20);
%results2 = rundcpf(ccr, mpopt);
%results2=runpf(ccr);  
    
    
fake_bus_id=[];
for tau=1:numel(ccr.bus(:,1))
    fake_bus_id=[fake_bus_id;ccr.bus(tau,1)];
end
fake_bus_id=fake_bus_id';   
    
real_bus_id=[1:1:numel(ccr.bus(:,1))];
    
for j=1:numel(ccr.branch(:,1))
    for chi=1:numel(ccr.bus(:,1))
        if f(j)==fake_bus_id(chi)
            f(j)=real_bus_id(chi);
        end
    end
end
    
   
    
b=[];
for t=1:n_branches_ccr %calculates the new current after removal of three lines
    Vf = abs(results2.bus(f(:,t), VM) * exp(1j * results2.bus(f(:,t), VA)*(pi/180)));
        
    
    
    b = [b; Vf];
end
b=b'

%write down all f here
%convert to fake bus id (it is like inverse conversion)
kau=ccr.branch(:,1)';
%fake_bus_id=ccr.branch(:,1)';
%reference_ccr_max=voltage_ref_max(fake_bus_id(1:length(fake_bus_id)));
%reference_ccr_min=voltage_ref_min(fake_bus_id(1:length(fake_bus_id)));
%and then compare to the original stored bus id
reference_ccr_max=voltage_ref_max(kau(1:length(kau)))
reference_ccr_min=voltage_ref_min(kau(1:length(kau)))
k=1:1:n_branches_ccr
for m=1:n_branches_ccr
    if (reference_ccr_max(:,m)<b(:,m)) || (b(:,m)<reference_ccr_min(:,m))
        %|(b(:,m)==0);
        ccr.branch(k(:,m),BR_STATUS)=0;
    end
end
island_ccr=ccr
end

