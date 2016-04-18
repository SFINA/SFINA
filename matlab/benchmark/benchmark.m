clc;
clear all;
close all;
define_constants;

prompt = 'Input the case file: ';
str = input(prompt,'s');
mpc = loadcase(str); %here give str as case57 for example
ccr=loadcase(str);

node = mpc.bus(:,BUS_I);

n_node = length(mpc.bus(:,BUS_I));
n_branches= length(mpc.branch(:,1));
node_index=(1:n_node)';
branch_index=[(1:n_branches)', mpc.branch(:,F_BUS), mpc.branch(:,T_BUS) ];
from_bus=[mpc.branch(:,F_BUS)]';

mpc.bus_name=cell(n_node,1);

for b = 1:n_node
    mpc.bus_name{b} = sprintf('bus %d', mpc.bus(b, BUS_I));
end

mpc.branch_name=cell(n_branches,1);

for b = 1:n_branches
    mpc.branch_name{b} = sprintf('branch %d', b);
end

custom.branch{1} = { 'branch_name' };
custom.bus{1} = { 'bus_name' };

%input AC or DC
opt = mpoption('PF_ALG', 1);
opt = mpoption(opt, 'OUT_ALL', 0);

results_pf=rundcpf(mpc,opt)
init_pf=results_pf.branch(1:n_branches,PF)';

%REFERENCES
%power
if isempty(mpc.branch(1:n_branches,RATE_A))==0
    ref_power=mpc.branch(1:n_branches,RATE_A)';
    
else
    ref_power=2*results_pf.branch(1:n_branches,PF)';
    zero_power=find(ref_power==0);
    ref_power(zero_power)=max(ref_power)
    
end




power_ref=ref_power;
norm_load(:,1)=(results_pf.branch(:,PF)-init_pf')./(power_ref'-init_pf');
load(:,1) = results_pf.branch(:,PF)./power_ref';
branches(:,1)=numel(find(results_pf.branch(:,BR_STATUS)==1))


%initialize total load
total_PD(1)=sum(results_pf.bus(:,PD));


table=readtable('event_loader.txt');
num_time=table{:,1}';
table_time=tabulate(num_time);
freq_num_time=table_time(:,2)';
total_time=numel(freq_num_time);
zero=zeros(1,numel(mpc.branch(:,1)))';
zero1=zeros(1,numel(mpc.branch(:,1)))';

iteration=2;

%initilaizing stopping criteria
branches(:,1)=numel(mpc.branch(:,1))
n_islands(1)=0;

zero=zeros(1,numel(mpc.branch(:,1)))';%real_power
zero_react_from=zeros(1,numel(mpc.branch(:,1)))';%reactive_power
zero_to=zeros(1,numel(mpc.branch(:,1)))';%real_power_to
zero_react_to=zeros(1,numel(mpc.branch(:,1)))';%reactive_power_to

zero1=zeros(1,numel(mpc.branch(:,1)))';%current
zero_bus_mag=zeros(1,numel(mpc.bus(:,1)))';%volt_mag
zero_bus_ang=zeros(1,numel(mpc.bus(:,1)))'%volt_ang


branch_stat_matrix=[];
norm_power_matrix=[];
norm_power_increase_matrix=[];
cum_norm_power_matrix=[];
cum_norm_power_increase_matrix=[];
count_micro=0
count_macro=[];
for i=1:1:n_branches
    mpc.branch(i,BR_STATUS)=0;
    
    while (1)
        count_micro=count_micro+1;
        load(:,iteration)=zeros(n_branches,1);
        mpc_casc=extract_islands(mpc, [], custom);
        n_islands(iteration)=numel(mpc_casc);
        total_PD(iteration)=0;
        for i=1:n_islands(iteration)
            
            if (~isempty(mpc_casc{i}.gen))
                
                
                actual_pd=mpc_casc{i}.bus(:, PD);
                actual_qd=mpc_casc{i}.bus(:, PD);
                if sum(mpc_casc{i}.bus(:, PD))>sum(mpc_casc{i}.gen(:,PG))
                    
                    mpc_casc{i}.bus(:, PD)=(sum(mpc_casc{i}.gen(:,PG))/sum(mpc_casc{i}.bus(:,PD)))*actual_pd;
                    mpc_casc{i}.bus(:, QD)=(sum(mpc_casc{i}.gen(:,QG))/sum(mpc_casc{i}.bus(:,QD)))*actual_qd;
                end
                [results_casc,success]=rundcpf(mpc_casc{i},opt);
                
                
                
                if success==0
                    display(success)
                    opt = mpoption('PF_ALG', 1,'PF_MAX_IT',20);
                    [results_casc,success]=runpf(mpc_casc{i},opt);
                    if success==0
                        display(success)
                        [results_casc,success]=rundcpf(mpc_casc{i},opt);
                    end
                end
                
                
                
                n_branches_casc=length( results_casc.branch(:,PF));
                [C,ia,ib] = intersect(results_pf.branch_name,results_casc.branch_name);
                [D,im,in] = intersect(results_pf.bus_name,results_casc.bus_name);
                
                %power array
                Sf = results_casc.branch(ib, PF);
                %carmen=find(Sf==NaN)
                zero(ia)=results_casc.branch(ib, PF);
                zero_react_from(ia)=results_casc.branch(ib,QF);
                zero_to(ia)=results_casc.branch(ib,16);
                zero_react_to(ia)=results_casc.branch(ib,17);
                
                
                
                
                load(ia,iteration)=abs(Sf)./abs(power_ref(ia)');
                %norm_load(ia,iteration)=(Sf-init_pf(ia)')./(power_ref(ia)'-init_pf(ia)');
                norm_load(ia,iteration)=(abs(Sf)-abs(init_pf(ia)'))./(abs(power_ref(ia)')-abs(init_pf(ia)'));
                
                
                
                
                total_PD(iteration)=total_PD(iteration)+sum(results_casc.bus(:,PD));
                
                
                
            end
            
        end
        
        
        
        fail_branch=find(abs(load(:,iteration))>1)
        
        mpc.branch(fail_branch,BR_STATUS)=0;
        
        %cumulative loading
        size_load=size(load)
        out_line=find(mpc.branch(:,BR_STATUS)==0);
        cum_load_array=load(:,size_load(2));
        cum_load_array(out_line)=1;
        cum_norm_power_matrix=[cum_norm_power_matrix cum_load_array];
        
        cum_load_increase_array=norm_load(:,size_load(2));
        cum_load_increase_array(out_line)=1;
        cum_norm_power_increase_matrix=[cum_norm_power_increase_matrix cum_load_increase_array];
        
        
        branches(:,iteration)=numel(find(mpc.branch(:,BR_STATUS)==1))
        
        mpc_casc=extract_islands(mpc, [], custom);
        n_islands(iteration)=numel(mpc_casc);
        
        if branches(iteration)==branches(iteration-1) & n_islands(iteration)==n_islands(iteration-1)
            
            break;
        end
        
        iteration=iteration+1;
        
    end
    count_macro=[count_macro;count_micro];
    count_micro=0;
    % [branch_index,mpc.branch(:,BR_STATUS),load]
    sum(mpc.branch(:,BR_STATUS)==0)
    %plot((total_PD(1:end-1)/total_PD(1)))
    % sum(results_pf.branch(:,10)~=0)
    (total_PD(1:end-1)/total_PD(1))
    
    size_load=size(load)
    %norm_power_matrix(:,i)=load(:,size_load(2))
    
    %branch_stat_matrix(:,i)=mpc.branch(:,BR_STATUS)
    
    outed_line=find(mpc.branch(:,BR_STATUS)==0);
    load_array=load(:,size_load(2));
    load_array(outed_line)=1;
    
    norm_load_array=norm_load(:,size_load(2));
    norm_load_array(outed_line)=1;
    
    %norm_power_matrix=[norm_power_matrix load(:,size_load(2))]
    norm_power_matrix=[norm_power_matrix load_array];
    branch_stat_matrix=[branch_stat_matrix mpc.branch(:,BR_STATUS)];
    
    norm_power_increase_matrix=[norm_power_increase_matrix norm_load_array];
    
    zero=zeros(1,numel(mpc.branch(:,1)))';%real_power
    zero_react_from=zeros(1,numel(mpc.branch(:,1)))';%reactive_power
    zero_to=zeros(1,numel(mpc.branch(:,1)))';%real_power_to
    zero_react_to=zeros(1,numel(mpc.branch(:,1)))';%reactive_power_to
    
    mpc=ccr;
    mpc.bus_name=cell(n_node,1);

    for b = 1:n_node
        mpc.bus_name{b} = sprintf('bus %d', mpc.bus(b, BUS_I));
    end
    
    mpc.branch_name=cell(n_branches,1);
    
    for b = 1:n_branches
        mpc.branch_name{b} = sprintf('branch %d', b);
    end
    
    custom.branch{1} = { 'branch_name' };
    custom.bus{1} = { 'bus_name' };
end

norm_power_matrix;
branch_stat_matrix=1-branch_stat_matrix;

norm_power_matrix(isnan(norm_power_matrix)) = 0;
norm_power=[sum(norm_power_matrix,2)]';

norm_stat=[sum(branch_stat_matrix,2)]';

norm_power_increase_matrix(isnan(norm_power_increase_matrix)) = 0;
norm_power_increase=[sum(norm_power_increase_matrix,2)]';

x_axis=1:n_branches;

x=x_axis;
y=x;
%x_count=norm_power./n_branches;
x_count=norm_power_increase./n_branches;
%y_count=norm_stat./sum(norm_stat);
y_count=norm_stat./n_branches;
z_count=norm_power./n_branches;

%normalized_total_damage
normalized_total_damage=sum(norm_stat)./n_branches

%normalized_net_power__increase
normalized_net_power_increase=sum(x_count)

% subplot(3,1,1)
% bar(y_count);
% xlabel('Link')
% ylabel('Probability of Removal')
% 
% subplot(3,1,2)
% bar(z_count);
% xlabel('Link')
% ylabel('Normalized Average Power')
% 
% subplot(3,1,3)
% bar(x_count);
% xlabel('Link')
% ylabel('Normalized Average Power Increase')

%pearson coefficient heatmap
branch_stat_matrix=branch_stat_matrix;
norm_power_matrix=norm_power_matrix;
norm_power_increase_matrix=norm_power_increase_matrix;

% row_coeff=[];
% matrix_coeff=[];
% 
% power_row_coeff=[];
% power_matrix_coeff=[];
% 
power_increase_row_coeff=[];
power_increase_matrix_coeff=[];
%
for j=1:n_branches
    for i=1:n_branches
        %         row_coeff=[row_coeff corr(branch_stat_matrix(:,j),branch_stat_matrix(:,i))];
        %         power_row_coeff=[power_row_coeff corr(norm_power_matrix(:,j),norm_power_matrix(:,i))];
        power_increase_row_coeff=[power_increase_row_coeff corr(norm_power_increase_matrix(:,j),norm_power_increase_matrix(:,i))];
        %
    end
    %     matrix_coeff=[matrix_coeff;row_coeff];
    %     power_matrix_coeff=[power_matrix_coeff;power_row_coeff];
    power_increase_matrix_coeff=[power_increase_matrix_coeff;power_increase_row_coeff];
    %
    %     row_coeff=[];
    %     power_row_coeff=[];
    power_increase_row_coeff=[];
end

sum_cumulative_load=sum(cum_norm_power_matrix,1)./n_branches;

sum_cumulative_load_increase=sum(cum_norm_power_increase_matrix,1)./n_branches;


% subplot(2,1,1)
% cdfplot(sum_cumulative_load)
% title('Cumulative Distribution Load')
% xlabel('Load')
% ylabel('% Cases')
% 
% subplot(2,1,2)
% cdfplot(sum_cumulative_load_increase)
% title('Cumulative Distribution Load Increase')
% xlabel('Load Increase')
% ylabel('% Cases')



% x_axis=[];
% for i=1:20
%     x_axis=[x_axis i*ones(1,20)];
% end
%     
% y_axis_branch=[];
% y_axis_power=[];
% y_axis_power_increase=[];
% for i=1:20
%     y_axis_branch=[y_axis_branch matrix_coeff(:,i)'];
%     y_axis_power=[y_axis_power power_matrix_coeff(:,i)'];
%     y_axis_power_increase=[y_axis_power_increase power_increase_matrix_coeff(:,i)'];
% end
% 
% %for 2D
% subplot(3,1,1)
% scatter(x_axis,y_axis_branch);
% xlabel('Link')
% ylabel('Pearson Coeff (Branch)')
% 
% subplot(3,1,2)
% scatter(x_axis,y_axis_power);
% xlabel('Link')
% ylabel('Pearson Coeff (Power)')
% 
% subplot(3,1,3)
% scatter(x_axis,y_axis_power_increase);
% xlabel('Link')
% ylabel('Pearson Coeff (Power Increase)')
% 
% %for 3D
% z_dim=[];
% for j=1:20
%     z_dim=[z_dim 1:20];
% end
% 
% % subplot(3,1,1)
% % scatter3(x_axis,z_dim,y_axis_branch);
% % xlabel('Link_x')
% % ylabel('Link_y')
% % zlabel('Pearson Coeff (Branch)')
% % 
% % subplot(3,1,2)
% % scatter3(x_axis,z_dim,y_axis_power);
% % xlabel('Link_x')
% % ylabel('Link_y')
% % zlabel('Pearson Coeff (Power)')
% % 
% % subplot(3,1,3)
% % scatter3(x_axis,z_dim,y_axis_power_increase);
% % xlabel('Link_x')
% % ylabel('Link_y')
% % zlabel('Pearson Coeff (Power Increase)')

 %A = power_increase_matrix_coeff;
% imagesc(A)
% colorbar
% colormap(redblue(100))
% caxis([-1 1]);
% title('Case 57 Pearson Power Increase')
% xlabel('Link_x')
% ylabel('Link_y')


%dlmwrite('case57_power_increase.txt', A, 'delimiter', ',');

count_macro=count_macro';
%sum_cumulative_load=sum_cumulative_load'
%C = mat2cell(sum_cumulative_load, count_macro) %saves as C{1},C{2},...access elemets as C{1}(1)

sum_cumulative_load_increase=sum_cumulative_load_increase';
C = mat2cell(sum_cumulative_load_increase, count_macro); %saves as C{1},C{2},...access elemets as C{1}(1)


maximum=max(count_macro)+1;
B=[];
D=[];
for i=1:n_branches
    C{i}(maximum)=0;
    if size(C{i})==[1 maximum];
        C{i}=C{i}'
    end
    B=[B;C{i}];
    D=[D C{i}];
end
B=B';
D=D;
%cdfplot(sum_cumulative_load)
entry=[];
for j=1:maximum-1
    entry=[entry;nnz(D(j,:))];
end

entry=entry';
strin=strread(num2str(entry),'%s');
strin=strin';

colorVec = hsv(maximum-1)
legendset = [];
M=B(1:maximum:(maximum*n_branches));
for i=2:maximum
    plt=cdfplot(M);
    area=trapz(M);
    %legendset = [legendset '' num2str(entry(i-1))];
    set(plt,'color',colorVec(i-1,:))
    hold on;
    M=[M B(i:maximum:(maximum*n_branches))];
    
end
legend(strin,'Location','NW')


hold on;
% basecase=ones(1,n_branches) * 0.5;
%basecase_sum=sum(abs(results_pf.branch(1:n_branches,PF)')./abs(ref_power))/n_branches;
basecase_sum=0;
basecase=ones(1,n_branches) * basecase_sum;
cdfplot(basecase)
% title('case57')
% legend(strin,'base','Location','NW')
title('case57 With 50% Rating -- Load Increase')
