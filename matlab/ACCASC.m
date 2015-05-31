%14, 30, 118 and 2383wp.m 

clc; 
clear all; 
close all;
define_constants;

%input parameter
fileread('input_parameters_alpha.txt')
split=strsplit(ans,'=');

ans1=char(split(5));
ans2=char(split(4));
type=ans1;
results=ans2;
last=strtok(type);
last1=strtok(results);
type=last
results=last1

prompt = 'Input the case file: ';
str = input(prompt,'s'); 
mpc = loadcase(str); %here give str as case57 for example

%input AC or DC
opt = mpoption('PF_ALG', 1);
opt = mpoption(opt, 'OUT_ALL', 0);


if isequal(results,'runpf(mpc,opt)')==1
    results_pf=runpf(mpc,opt)    
else isequal(results,'runpf(mpc,opt)')==0
    results_pf=rundcpf(mpc,opt)
end

%status of mpc case struct
node = mpc.bus(:,BUS_I); %number of buses in the initial system
length_initial=length(node);
n_branches=numel(mpc.branch(:,1)); %number of branches in the initial system
f=mpc.branch(:,1); %registers the branches into which power injected
f=f'; %transposing into array
generators=mpc.gen(:,1); %registers total number of generators
generators=generators';

%identifying slack bus
slack=find(mpc.bus(:,BUS_TYPE)==3);


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

current='current';
power='power';
voltage='voltage';

%stores reference current and power
[ref_current,ref_power,volt_ref_max,volt_ref_min]=powerflow(mpc,results_pf)

if strcmp(type,current)==1
    %powerflow(mpc)
    reference=ref_current;
    %reference=ans;
elseif strcmp(type,power)==1
    reference=ref_power;
     %powerflow_power(mpc)
     %reference=ans;
end


%remove branche(s)

bran=str2num(fileread('input_parameters_branches.txt'))

result=[];
for i=1:length(bran)
    result=[result;bran(i)]
end
result=result';

%different attack strategies simultaneous and sequential
st =char(split(7));
string=st;
last3=strtok(string);
string=last3

%prompt = 'Enter number of attacks to make in seq (if you said simultaneous just enter 1):';
 if strcmp(string,'sequence')==1
    num_attack = length(bran)
 else strcmp(string,'sequence')==0
    num_attack = 1
 end



count2=0;
number_islands1=0;
number_islands2=0;
branch_status1=0;
branch_status2=0;
branch_stat2=[];
%branch_stat1=[];
total_load=[sum(mpc.bus(:,PD))];
total_generation=[sum(mpc.gen(:,PG))];
num_branches=[n_branches];
num_islan=[1];

%generators data here

T_GEN=mpc.gen(:,1);
all_1_gen(1:length(mpc.bus(:,1)))=0;
all_1_nan(1:length(mpc.bus(:,1)))=NaN

final_gen_matrix=[];
for i=2:21
    for jj=1:length(T_GEN)
        all_1_gen(T_GEN(jj))=1;
        all_1_nan(T_GEN(jj))=mpc.gen(jj,i);
    end
    final_gen_matrix=[final_gen_matrix;all_1_nan];
    all_1_nan(1:length(mpc.bus(:,1)))=NaN;
end

%from here gencost begins
for i=1:7
    for jj=1:length(T_GEN)
        all_1_gen(T_GEN(jj))=1;
        all_1_nan(T_GEN(jj))=mpc.gencost(jj,i);
    end
    final_gen_matrix=[final_gen_matrix;all_1_nan];
    all_1_nan(1:length(mpc.bus(:,1)))=NaN;
end
%from here gencost ends

final_gen_matrix=transpose(final_gen_matrix)

real_gen=final_gen_matrix(:,1);reactive_gen=final_gen_matrix(:,2);Qmax=final_gen_matrix(:,3);Qmin=final_gen_matrix(:,4);Vg=final_gen_matrix(:,5);mBase=final_gen_matrix(:,6);
status=final_gen_matrix(:,7);Power_max=final_gen_matrix(:,8);Power_min=final_gen_matrix(:,9);Pc1=final_gen_matrix(:,10);Pc2=final_gen_matrix(:,11);Qc1min=final_gen_matrix(:,12);Qc1max=final_gen_matrix(:,13);
Qc2min=final_gen_matrix(:,14);Qc2max=final_gen_matrix(:,15);ramp_agc=final_gen_matrix(:,16);ramp_10=final_gen_matrix(:,17);
ramp_30=final_gen_matrix(:,18);ramp_q=final_gen_matrix(:,19);apf=final_gen_matrix(:,20);model=final_gen_matrix(:,21);startup=final_gen_matrix(:,22);shutdown=final_gen_matrix(:,23);n_cost=final_gen_matrix(:,24);
cost_coeff_1=final_gen_matrix(:,25);cost_coeff_2=final_gen_matrix(:,26);cost_coeff_3=final_gen_matrix(:,27);

node=mpc.bus(:,BUS_I);
 GENS=all_1_gen';
 GENERATORS_array={};
 for genn=1:length(GENS)
     GENERATORS_array{genn}='GEN';
 end
 
 for g=1:length(GENS)
     if GENS(g)==0
         GENERATORS_array{g}='BUS';
         
     end
 end
 TYPE=GENERATORS_array';



for att=1:num_attack
    
    if strcmp(string,'sequence')==1
        mpc.branch(result(att),BR_STATUS)=0;
        
    elseif strcmp(string,'sequence')==0
        for res=1:length(result)
            
            
            mpc.branch(result(res),BR_STATUS)=0;
        end
        
    end
    
    init_island=numel(extract_islands(mpc));
    all_case_ccr=[];
    for init=1:init_island
        all_case_ccr=[all_case_ccr;extract_islands(mpc,init)];
    end
    
    while 1
        count2=count2+1;
        
        %check succes or failure of convergence
        suc=[];
        for island=1:length(all_case_ccr)
            opt = mpoption('PF_ALG', 1,'PF_MAX_IT',30);
            
            opt = mpoption(opt, 'OUT_ALL', 0);
            
            if isequal(results,'runpf(mpc,opt)')==1
                [resu,success]=runpf(all_case_ccr(island),opt);
                
            else isequal(results,'runpf(mpc,opt)')==0
                [resu,success]=rundcpf(all_case_ccr(island),opt);
                
            end
            
            if success==0
                opt = mpoption('PF_ALG', 2,'PF_MAX_IT_FD',70);
                
                opt = mpoption(opt, 'OUT_ALL', 0);
                if isequal(results,'runpf(mpc,opt)')==1
                    
                    [resu,success]=runpf(all_case_ccr(island),opt);
                else isequal(results,'runpf(mpc,opt)')==0
                    
                    [resu,success]=rundcpf(all_case_ccr(island),opt);
                end
                
                if success==0
                    
                    suc=[suc;0];
                end
            else success==1
                suc=[suc;1];
                
                
            end
        end
        
        succ=suc'
        zero_success=find(suc==0);
        all_case_ccr(zero_success)=[];
        
        %does load shedding
        for island=1:length(all_case_ccr)
            actual_pd=all_case_ccr(island).bus(:, PD);
            if sum(all_case_ccr(island).bus(:, PD))>sum(all_case_ccr(island).gen(:,PG))
                
                all_case_ccr(island).bus(:, PD)=(sum(all_case_ccr(island).gen(:,PG))/sum(all_case_ccr(island).bus(:,PD)))*actual_pd
            end
            
            
        end
        
        
        %updates ccr with some branches exceeding limit, maybe islands formed
        %CURRENT AND POWER
        updated_ccr=[];
        
        for island=1:length(all_case_ccr)
            if strcmp(type,current)==1
                if isequal(results,'runpf(mpc,opt)')==1
                    results2=runpf(all_case_ccr(island),opt)
                else isequal(results,'runpf(mpc,opt)')==0
                    results2=rundcpf(all_case_ccr(island),opt)
                end
                %[T,b,bb,r,x,b1,rateA,rateB,rateC,ratio,angle,status,angmin,angmax] = extract_branch_topology(mpc,all_case_ccr(island),reference,results2)
                update_ccr(mpc,all_case_ccr(island),reference,results2);
                updated_ccr=[updated_ccr;ans]
                power=power
                current=current
            elseif strcmp(type,power)==1
                if isequal(results,'runpf(mpc,opt)')==1
                    results2=runpf(all_case_ccr(island),opt)
                else isequal(results,'runpf(mpc,opt)')==0
                    results2=rundcpf(all_case_ccr(island),opt)
                end
                update_ccr_power(mpc,all_case_ccr(island),reference,results2);
                updated_ccr=[updated_ccr;ans]
                
            end
        end
        
        %updates ccr with some branches exceeding limit, maybe islands formed
        %VOLTAGE
        if strcmp(type,voltage)==1
            updated_ccr=[]
            for island=1:length(all_case_ccr)
                %results2=runpf(all_case_ccr(island))
                if isequal(results,'runpf(mpc,opt)')==1
                    results2=runpf(all_case_ccr(island),opt)
                else isequal(results,'runpf(mpc,opt)')==0
                    results2=rundcpf(all_case_ccr(island),opt)
                end
                island_ccr=update_ccr_voltage(mpc,all_case_ccr(island),volt_ref_max,volt_ref_min,results2)
                updated_ccr=[updated_ccr;island_ccr]
            end
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
        
        %extract all islands, and store in array (including those with zero power generation)
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
        
        
        %voltage=topology(mpc,total_ccr)
        
        all_zero(1:length(node))=0;
        define_constants;
        
        remaining_bus=[];pd=[];qd=[];Gs=[];Bs=[];area=[];volt_mag=[];volt_ang=[];baseKV=[];zone=[];
        v_max=[];v_min=[];Vf=[];
        for top=1:length(total_ccr)
            remaining_bus=[remaining_bus;total_ccr(top).bus(:,BUS_I)];
            pd=[pd;total_ccr(top).bus(:,3)];qd=[qd;total_ccr(top).bus(:,4)];Gs=[Gs;total_ccr(top).bus(:,5)];
            Bs=[Bs;total_ccr(top).bus(:,6)];area=[area;total_ccr(top).bus(:,7)];volt_mag=[volt_mag;total_ccr(top).bus(:,8)];
            volt_ang=[volt_ang;total_ccr(top).bus(:,9)];baseKV=[baseKV;total_ccr(top).bus(:,10)];
            zone=[zone;total_ccr(top).bus(:,11)];v_max=[v_max;total_ccr(top).bus(:,12)];v_min=[v_min;total_ccr(top).bus(:,13)];
            
        end
        
        remaining_bus=remaining_bus
        
        pd=pd';qd=qd';Gs=Gs';Bs=Bs';area=area';volt_mag=volt_mag';volt_ang=volt_ang';baseKV=baseKV';v_max=v_max';zone=zone';
        v_min=v_min';
       
        
        
        conc_bus_array=cat(1,pd,qd,Gs,Bs,area,volt_mag,volt_ang,baseKV,zone,v_max,v_min)
        conc_bus_array=transpose(conc_bus_array)
        
        REMAINING_BUS=remaining_bus
        length(REMAINING_BUS)
        
        
        %conc_bus_array
        final_bus_matrix=[];
        for i=1:11
            
            
            for kaka=1:length(pd)
                all_zero(REMAINING_BUS(kaka))=conc_bus_array(kaka,i);
            end
            
            final_bus_matrix=[final_bus_matrix;all_zero];
            all_zero(1:length(all_zero))=0;
        end
        final_bus_matrix=transpose(final_bus_matrix)
        pd=final_bus_matrix(:,1);qd=final_bus_matrix(:,2);Gs=final_bus_matrix(:,3);Bs=final_bus_matrix(:,4);
        area=final_bus_matrix(:,5);volt_mag=final_bus_matrix(:,6);volt_ang=final_bus_matrix(:,7);
        baseKV=final_bus_matrix(:,8);zone=final_bus_matrix(:,9);v_max=final_bus_matrix(:,10);v_min=final_bus_matrix(:,11);
        
        TYPE{slack}='SLACK_BUS';
        busandmeta=table(node,TYPE,pd,qd,Gs,Bs,area,volt_mag,volt_ang,baseKV,zone,v_min,v_max,real_gen,reactive_gen,Qmax,Qmin,Vg,mBase,Power_max,Power_min,Pc1,Pc2,Qc1min,Qc1max,Qc2min,Qc2max,ramp_agc,ramp_10,ramp_30,ramp_q,apf,model,startup,shutdown,n_cost,cost_coeff_1,cost_coeff_2,cost_coeff_3) %also added voltage mag-angle
        writetable(busandmeta,sprintf('bus_and_meta_information%d.txt',count2));
        
        %branch and meta begins
        bus_index=[];
        for island=1:length(total_ccr)
            
            branc_topology(mpc,total_ccr(island));
            %update_ccr(mpc,total_ccr(island),reference,results2)
            %update_ccr_power(mpc,total_ccr(island),reference,results2)
            bus_index=[bus_index;ans'];
            %bus_index=[bus_index;ID];
        end
        
        bus_index=bus_index';
        
        TOTAL_BR=length(mpc.branch(:,1));
        
        TOT_BR=1:TOTAL_BR;
        TOT_BR=TOT_BR';
        branch_all_1(1:length(TOT_BR))=1;
        branch_all_1;
        
        for mm=1:length(bus_index)
            branch_all_1(bus_index(mm))=0;
        end
        ISOLAT_BR=branch_all_1';
        R=mpc.branch(:,3);X=mpc.branch(:,4);B1=mpc.branch(:,5);RATEA=mpc.branch(:,6);RATEB=mpc.branch(:,7);
        RATEC=mpc.branch(:,8);RATIO=mpc.branch(:,9);ANGLE=mpc.branch(:,10);STATUS=mpc.branch(:,11);
        ANGMIN=mpc.branch(:,12);ANGMAX=mpc.branch(:,13);branch_id=[1:1:length(mpc.branch(:,1))];
        
        removed_branch=find(ISOLAT_BR==1)
        for i=1:length(removed_branch)
            R(removed_branch(i))=0;X(removed_branch(i))=0;B1(removed_branch(i))=0;
            RATEA(removed_branch(i))=0;RATEB(removed_branch(i))=0;RATEC(removed_branch(i))=0;
            RATIO(removed_branch(i))=0;ANGLE(removed_branch(i))=0;STATUS(removed_branch(i))=0;
            ANGMIN(removed_branch(i))=0;ANGMAX(removed_branch(i))=0;
        end
        resistance=R;reactance= X;susceptance=B1;rateA=RATEA;rateB=RATEB;rateC=RATEC;ratio=RATIO;angle=ANGLE;
        status=STATUS;angmin=ANGMIN;angmax=ANGMAX;
        branch_id=branch_id'
          
        
        branchandmeta=table(branch_id,resistance,reactance,susceptance,rateA,rateB,rateC,ratio,angle,status,angmin,angmax)
        writetable(branchandmeta,sprintf('br_and_meta_information%d.txt',count2));
       
        
        branch_status2=numel(FROM_BUS);
       
        number_islands2=numel(total_ccr);
        
        total_load=[total_load;new_power_demand];
        total_generation=[total_generation;new_power_generation];
        num_branches=[num_branches;branch_status2];
        num_islan=[num_islan;number_islands2] %this is cumulative
        if number_islands2==number_islands1 & branch_status2==branch_status1
            
            
            text='cascading failure triggered';
            disp(text)
            break
        end
        
        
        
        number_islands1=number_islands2;
        branch_status1=branch_status2;
        %branch_stat1=branch_stat2;
        branch_stat2=[];
        
        
        all_case_ccr=total_ccr
        total_ccr=[];
    end
end

number_islands1
count2

total_load=total_load
num_it=1:numel(total_load)
Iterations=num_it'


total_load(length(total_load))=[];
total_generation(length(total_generation))=[];
num_branches(length(num_branches))=[];
num_islan(length(num_islan))=[];
Iterations(length(Iterations))=[];

Total_Load = total_load;
Total_generation=total_generation;
Number_Branches=num_branches;
Number_Islands=num_islan;

plot(Iterations,Total_Load)
xlabel('Iterations') % x-axis label
ylabel('Load') % y-axis label
%Write table
T = table(Iterations,Total_Load,Total_generation,Number_Branches,Number_Islands)


