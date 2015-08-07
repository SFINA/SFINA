clc;
define_constants;
mpc=loadcase('case5');
n_node=numel(mpc.bus(:,1))
from=mpc.branch(:,1)';
to=mpc.branch(:,2)';
real_from=mpc.branch(:,1)';
real_to=mpc.branch(:,2)';
results=rundcpf(mpc)

for i=1:numel(from)
    if results.branch(i,PF)<0
        real_from(i)=to(i);
        real_to(i)=from(i);
    end
end

real_to=real_to

real_from=real_from
PD=abs(results.branch(:,PF))

i=1;
PD_array=[];
entropy=[];
while i<n_node+1
    a=[find(real_from==i)];
    PD(a)
    norm_line_load=PD(a)/sum(PD(a));log_norm_line_load=abs(log10(PD(a)/sum(PD(a))))
    product=norm_line_load.*log_norm_line_load;
    entropy=[entropy;sum(product)];
    PD_array=[PD_array;sum(PD(a))];
    i=i+1;    
end

PD_array=PD_array';

%the weighted measures
electrical_node_significance=PD_array/sum(PD_array) %higher value suggest overloading this line causes severe damage
entropy=entropy' %highter value suggested load is distributed homogeneously, put battery to re-route power here
