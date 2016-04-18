function [busandmeta,branchandmeta] = matpower_SFINA_function(mpc,node,count2,results)
define_constants;

all_1_gen(1:length(mpc.bus(:,1)))=0;
node=mpc.bus(:,BUS_I);

%identifying slack bus
slack=find(mpc.bus(:,BUS_TYPE)==3);

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

%from here gencost data
for i=1:7
    for jj=1:length(T_GEN)
        all_1_gen(T_GEN(jj))=1;
        all_1_nan(T_GEN(jj))=mpc.gencost(jj,i);
    end
    final_gen_matrix=[final_gen_matrix;all_1_nan];
    all_1_nan(1:length(mpc.bus(:,1)))=NaN;
end


final_gen_matrix=transpose(final_gen_matrix)

real_gen=final_gen_matrix(:,1);reactive_gen=final_gen_matrix(:,2);Qmax=final_gen_matrix(:,3);Qmin=final_gen_matrix(:,4);Vg=final_gen_matrix(:,5);mBase=final_gen_matrix(:,6);
status=final_gen_matrix(:,7);Power_max=final_gen_matrix(:,8);Power_min=final_gen_matrix(:,9);Pc1=final_gen_matrix(:,10);Pc2=final_gen_matrix(:,11);Qc1min=final_gen_matrix(:,12);Qc1max=final_gen_matrix(:,13);
Qc2min=final_gen_matrix(:,14);Qc2max=final_gen_matrix(:,15);ramp_agc=final_gen_matrix(:,16);ramp_10=final_gen_matrix(:,17);
ramp_30=final_gen_matrix(:,18);ramp_q=final_gen_matrix(:,19);apf=final_gen_matrix(:,20);model=final_gen_matrix(:,21);startup=final_gen_matrix(:,22);shutdown=final_gen_matrix(:,23);n_cost=final_gen_matrix(:,24);
cost_coeff_1=final_gen_matrix(:,25);cost_coeff_2=final_gen_matrix(:,26);cost_coeff_3=final_gen_matrix(:,27);


FROM_BUS=mpc.branch(:,1);
TO_BUS=mpc.branch(:,2);

all_zero(1:length(node))=0;
define_constants;



remaining_bus=mpc.bus(:,BUS_I);
pd=mpc.bus(:,3);qd=mpc.bus(:,4);Gs=mpc.bus(:,5);
Bs=mpc.bus(:,6);area=mpc.bus(:,7);volt_mag=mpc.bus(:,8);
volt_ang=mpc.bus(:,9);baseKV=mpc.bus(:,10);
zone=mpc.bus(:,11);v_max=mpc.bus(:,12);v_min=mpc.bus(:,13);
    
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
volt_mag=results.bus(:,8);volt_ang=results.bus(:,9);

id=node;
TYPE{slack}='SLACK_BUS';
Gs=mpc.bus(:,5);Bs=mpc.bus(:,6);area=mpc.bus(:,7);baseKV=mpc.bus(:,10);zone=mpc.bus(:,11);v_max=mpc.bus(:,12);v_min=mpc.bus(:,13);
type=TYPE;real_power=pd;reactive_power=qd;volt_max=v_max;volt_min=v_min;power_max=Power_max;power_min=Power_min;
busandmeta=table(id,type,real_power,reactive_power,Gs,Bs,area,volt_mag,volt_ang,baseKV,zone,volt_max,volt_min,real_gen,reactive_gen,Qmax,Qmin,Vg,mBase,power_max,power_min,Pc1,Pc2,Qc1min,Qc1max,Qc2min,Qc2max,ramp_agc,ramp_10,ramp_30,ramp_q,apf,model,startup,shutdown,n_cost,cost_coeff_1,cost_coeff_2,cost_coeff_3); %also added voltage mag-angle
writetable(busandmeta,sprintf('jose_bus_and_meta_information%d.txt',count2));

fid=fopen('flow_bus.txt','w')
str = fileread('jose_bus_and_meta_information0.txt')
str = regexprep(str,'NaN','-')
C = textscan(str,'%s%f%f%f%f%f%f%f%f','headerlines',1,'delimiter',';');
fprintf(fid,str)
fclose(fid)


delete('jose_bus_and_meta_information0.txt')

status=ones(numel(id),1);
busandtopo=table(id,status);
writetable(busandtopo,sprintf('jose_bus_and_topo_information%d.txt',count2));

fid=fopen('topology_bus.txt','w')
str = fileread('jose_bus_and_topo_information0.txt')
str = regexprep(str,'NaN','-')
C = textscan(str,'%s%f%f%f%f%f%f%f%f','headerlines',1,'delimiter',';');
fprintf(fid,str)
fclose(fid)


delete('jose_bus_and_topo_information0.txt')


id=[1:numel(mpc.branch(:,1))]';
from=mpc.branch(:,1);
real_power_from=results.branch(:,PF);reactive_power_from=results.branch(:,QF);
Sf = results.branch(:,PF) + 1j * results.branch(:,QF);
Vf = results.bus(from,VM).*exp(1j * results.bus(from,VA)*(pi/180));
If = abs(conj(Sf./Vf)); %% complex current injected into branch k at bus f
%k=find(If)
current=If;angle=results.branch(:,10);
real_power_to=results.branch(:,16);reactive_power_to=results.branch(:,17);numel(current)
numel(real_power_to)
resistance=mpc.branch(:,3);reactance=mpc.branch(:,4);susceptance=mpc.branch(:,5);rateA=mpc.branch(:,6);rateB=mpc.branch(:,7);rateC=mpc.branch(:,8);ratio=mpc.branch(:,9);angmin=mpc.branch(:,12);angmax=mpc.branch(:,13);
branchandmeta=table(id,current,real_power_from,reactive_power_from,real_power_to,reactive_power_to,resistance,reactance,susceptance,rateA,rateB,rateC,ratio,angle,angmin,angmax);
writetable(branchandmeta,sprintf('jose_br_and_meta_information%d.txt',count2));

fid=fopen('flow_branch.txt','w')
str = fileread('jose_br_and_meta_information0.txt')
str = regexprep(str,'NaN','-')
C = textscan(str,'%s%f%f%f%f%f%f%f%f','headerlines',1,'delimiter',';');
fprintf(fid,str)
fclose(fid)


delete('jose_br_and_meta_information0.txt')

%branch_topology
from_node_id=mpc.branch(:,1);to_node_id=mpc.branch(:,2);status=mpc.branch(:,BR_STATUS);
branchandtopo=table(id,from_node_id,to_node_id,status);
writetable(branchandtopo,sprintf('jose_br_and_topo_information%d.txt',count2));

fid=fopen('topology_branch.txt','w')
str = fileread('jose_br_and_topo_information0.txt')
str = regexprep(str,'NaN','-')
C = textscan(str,'%s%f%f%f%f%f%f%f%f','headerlines',1,'delimiter',';');
fprintf(fid,str)
fclose(fid)


delete('jose_br_and_topo_information0.txt')




