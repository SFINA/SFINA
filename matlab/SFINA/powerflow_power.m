function y = powerflow_power(mpc)

define_constants;

alpha=2; %sets the limit for the capacity
%mpc = loadcase('case2383wp.m');

%mpopt = mpoption('PF_ALG', 1,'PF_MAX_IT',20);
%results = rundcpf(mpc, mpopt);
results=runpf(mpc);

n_bus = numel(mpc.bus(:,1));
n_branches=numel(mpc.branch(:,1));
f=mpc.branch(:,1); %registers the branches into which power injected
f=f';
generators=mpc.gen(:,1); %registers total number of generators
generators=generators';

%gives the indices of branches
k=[];
for l=1:n_branches
    k=[k;l];
end
k=k';

%caclulates the reference currents
a=[]

power_ref=mpc.branch(1:n_branches,RATE_A);
reference=power_ref'

%a=a';
y=reference
%y=a';

end


