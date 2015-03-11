%14, 30, 118 and 2383wp.m 
define_constants;

alpha=2; %sets the limit for the capacity
mpc = loadcase('case2383wp.m');
results=runpf(mpc);

n_bus = numel(mpc.bus(:,1));
n_branches=numel(mpc.branch(:,1));
f=mpc.branch(:,1); %registers the branches into which power injected
f=f';
generators=mpc.gen(:,1); %registers total number of generators
generators=generators';

%gives the indices of branches

k=[1:1:n_branches];

ref_idx=k;
%reference indices for to and from
ref_a=[];
ref_b=[];
for j=1:length(k)
    ref_a=[ref_a;mpc.branch(k(j),1)]; 
    ref_b=[ref_b;mpc.branch(k(j),2)]; 
end

powerflow(mpc)
%stores reference current
reference=ans

%remove branche(s)

result=[];
line=1; %initializig with random transmission line
while 1
    if line==0
        break
    end
    
    prompt = 'Enter the transmission line to be removed (Enter 0 if done removing):';
    line = input(prompt)
    result=[result;line];
    
end
result=result';
result(result == 0) = [];

for res=1:length(result)
    
    
    mpc.branch(result(res),BR_STATUS)=0;
end

count2=0;

init_island=numel(extract_islands(mpc));
all_case_ccr=[];
for init=1:init_island
    all_case_ccr=[all_case_ccr;extract_islands(mpc,init)];
end


number_islands1=0;
number_islands2=0;
branch_status1=0;
branch_status2=0;
branch_stat2=[];


while 1
    count2=count2+1
    %replaces reindexing and calculates branch stat 2 calc.plus does pf,
    %removes branches exceeding limit
    for island=1:length(all_case_ccr)
        branchstatus(all_case_ccr(island),reference)
        branch_stat2=[branch_stat2;ans];
         
    end
    branch_stat2=branch_stat2';
    branch_status2=sum(branch_stat2)
    
    %updates ccr with some branches exceeding limit, maybe some islands are
    %there----> next count all these islands
    updated_ccr=[]
    
    for island=1:length(all_case_ccr)
        update_ccr(all_case_ccr(island),reference)
        updated_ccr=[updated_ccr;ans] %problem is here, coz all ccr's stored in ans. Now solved?
        %also creates new updated ccr from old (new one in which some
        %branches removed %HERE OUTPUT BRANCH_STATUS2
        
    end
    
    %SO THE updated_ccr also contains isolated buses, maybe remove those
    %for iso=1:length(updated_ccr)
     %   [x,isolated_bus]=find_islands(updated_ccr(iso));
      %  isolated_bus=isolated_bus
       % for z=1:length(isolated_bus)
        %    updated_ccr(iso).bus(isolated_bus(z),BUS_TYPE)=4;
        %end
    %end
    
    
    %in this part extract all islands, and store in array (including those
    %with zero power generation)
    all_case_ccr=[];
    total_ccr=[];
    
    for num_ccr=1:length(updated_ccr)
        for num_island_in_ccr=1:numel(extract_islands(updated_ccr(num_ccr)))
            all_case_ccr=[all_case_ccr;extract_islands(updated_ccr(num_ccr),num_island_in_ccr)];
            %total_ccr=[total_ccr;all_case_ccr];
            
        end
        %total_ccr=[total_ccr;all_case_ccr]
    end
    total_ccr=[total_ccr;all_case_ccr]
    TOTALISLANDS=numel(total_ccr)
    %in this empty space it produces multiple arrays with multiple islands,
    %each array has multiple islands coming from single island
    %now join them and make it new all_case_ccr the rest of alg from here
    %no change
    
    power_demand=[];
    power_generation=[];
    for island=1:length(total_ccr)
    
        power_demand=[power_demand;sum(total_ccr(island).bus(:,PD))];
        power_generation=[power_generation;sum(total_ccr(island).gen(:,PG))];
   
    end

    power_demand=power_demand'
    power_generation=power_generation'

    %find indices of PG=0 (which island has no generators)
    zero_power=find(power_generation==0);

    total_ccr(zero_power)=[];
    
    number_islands2=numel(total_ccr);
    if number_islands2==number_islands1 & branch_status2==branch_status1
        %count2=count2+1
        text='no cascading failure triggered %s\n';
        disp(text)
        break
    end
    number_islands1=number_islands2;
    branch_status1=branch_status2;
    branch_stat2=[]; %maybe do same for number_islands
    
    all_case_ccr =total_ccr; %DONE except for confused all_case and problem
    %mentioned above
    total_ccr=[];
end

number_islands1
count2
%PROBLEM LOOP RUNS ENDLESSLY
