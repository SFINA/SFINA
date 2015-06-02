function T = update_ccr_power(mpc,ccr,reference,results2)
define_constants;
%gives the indices of branches

%mpc=loadcase('case57.m');
k=[1:1:numel(mpc.branch(:,1))];



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
%gives the indices of branches
k=[];
for l=1:n_branches_ccr
    k=[k;l];
end
k=k';
    
reference_ccr=[];
    
for z=1:length(idx)
    reference_ccr=[reference_ccr;reference(idx(z))];
end
reference_ccr=reference_ccr';
    

%mpopt = mpoption('PF_ALG', 1,'PF_MAX_IT',20);
%results2 = runpf(ccr, mpopt);
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
    Sf_new = abs(results2.branch(k(:,t), PF) + 1j * results2.branch(k(:,t), QF));
        
    
    
    b = [b; Sf_new];
end
b=b'

for m=1:n_branches_ccr
    if(b(:,m)>reference_ccr(:,m))
        %|(b(:,m)==0);
        ccr.branch(k(:,m),BR_STATUS)=0;
    end
end
T=ccr

end

