function ID = branc_topology(mpc,ccr)
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
    
ID=idx'
end
