%14, 30, 118 and 2383wp.m 
define_constants;

alpha=2; %sets the limit for the capacity
mpc = loadcase('case57.m');
results=runpf(mpc);

n_bus = numel(mpc.bus(:,1)); %number of buses in the initial system
n_branches=numel(mpc.branch(:,1)); %number of branches in the initial system
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

%stores reference current
powerflow(mpc)
reference=ans;

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



init_island=numel(extract_islands(mpc));
all_case_ccr=[];
for init=1:init_island
    all_case_ccr=[all_case_ccr;extract_islands(mpc,init)];
end

count2=0;
number_islands1=0;
number_islands2=0;
branch_status1=0;
branch_status2=0;
branch_stat2=[];
total_load=[sum(mpc.bus(:,PD))];
total_generation=[sum(mpc.gen(:,PG))];
num_branches=[n_branches];
num_islan=[0];
while 1
    count2=count2+1
    
    %for island=1:length(all_case_ccr)
        
     %   actual_pd=all_case_ccr(island).bus(:, PD);
      %  all_case_ccr(island).bus(:, PD)=(sum(all_case_ccr(island).gen(:,PG))/sum(all_case_ccr(island).bus(:,PD)))*actual_pd;
         
         
    %end
    
    for island=1:length(all_case_ccr)
        branchstatus(all_case_ccr(island),reference)
        branch_stat2=[branch_stat2;ans];
         
    end
    branch_stat2=branch_stat2';
    branch_status2=sum(branch_stat2);
    
    %updates ccr with some branches exceeding limit, maybe islands formed
    
    updated_ccr=[]
    
    for island=1:length(all_case_ccr)
        update_ccr(all_case_ccr(island),reference)
        updated_ccr=[updated_ccr;ans] 
        
    end
    
    %Deals with completely disconnected islands
    num_iso=[];
    for iso=1:length(updated_ccr)
        [x,isolated_bus]=find_islands(updated_ccr(iso));
        isolated_bus=isolated_bus;
        num_iso=[num_iso;length(isolated_bus)];
    end
    
    num_iso=num_iso';
    
    num_bus=[];
    for n_b=1:length(updated_ccr)
        num_bus=[num_bus;numel(updated_ccr(n_b).bus(:,1))];
    end        
    num_bus=num_bus';


    same=find(num_bus==num_iso);
    updated_ccr(same)=[];
    
    
    
    %in this part extract all islands, and store in array (including those
    %with zero power generation)
    all_case_ccr=[];
    total_ccr=[];
    
    for num_ccr=1:length(updated_ccr)
        for num_island_in_ccr=1:numel(extract_islands(updated_ccr(num_ccr)))
            all_case_ccr=[all_case_ccr;extract_islands(updated_ccr(num_ccr),num_island_in_ccr)];
            
            
        end
       
    end
    total_ccr=[total_ccr;all_case_ccr]
    TOTALISLANDS=numel(total_ccr)
    
    
    power_demand=[];
    power_generation=[];
    for island=1:length(total_ccr)
    
        power_demand=[power_demand;sum(total_ccr(island).bus(:,PD))];
        power_generation=[power_generation;sum(total_ccr(island).gen(:,PG))];
   
    end

    power_demand=power_demand'
    power_generation=power_generation'

    %find indices of PG=0 (finds which island has no generators and removes)
    zero_power=find(power_generation==0);

    total_ccr(zero_power)=[];
    
    %new power demand after PG=0 set
    new_power_demand=[];
    new_power_generation=[];
    for island=1:length(total_ccr)
        new_power_generation=[new_power_generation;sum(total_ccr(island).gen(:,PG))];
        new_power_demand=[new_power_demand;sum(total_ccr(island).bus(:,PD))];
        
    end

    new_power_demand=sum(new_power_demand')
    new_power_generation=sum(new_power_generation')
    
    
    
    
    number_islands2=numel(total_ccr);
    if number_islands2==number_islands1 & branch_status2==branch_status1
        
        text='cascading failure triggered';
        disp(text)
        break
    end
    
    total_load=[total_load;new_power_demand];
    total_generation=[total_generation;new_power_generation];
    num_branches=[num_branches;branch_status2];
    num_islan=[num_islan;number_islands1]
    
    from_bus=[];
    to_bus=[];
    for topo=1:length(total_ccr)
        from_bus=[from_bus;total_ccr(topo).branch(:,1)];
        to_bus=[to_bus;total_ccr(topo).branch(:,2)];
        %eval(sprintf('from_bus%d = from_bus', topo))
        %eval(sprintf('to_bus%d = to_bus', topo))
    end
    FROM_BUS=from_bus;
    TO_BUS=to_bus;
    topology=table(FROM_BUS,TO_BUS);
    writetable(topology,sprintf('topology_information%d.txt',count2));
    
    
    number_islands1=number_islands2;
    branch_status1=branch_status2;
    branch_stat2=[]; 
    
    all_case_ccr =total_ccr; 
    total_ccr=[];
end

number_islands1
count2

total_load=total_load
num_it=1:numel(total_load)
Iterations=num_it'

Total_Load = total_load;
Total_generation=total_generation;
Number_Branches=num_branches;
Number_Islands=num_islan;
%Write table
T = table(Iterations,Total_Load,Total_generation,Number_Branches,Number_Islands)
writetable(T,'mydata.txt')

