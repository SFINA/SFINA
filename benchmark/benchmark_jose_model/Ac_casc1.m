clc;
clear all;
close all;
define_constants;

mpc=loadcase('case30');
ccr=loadcase('case30')
%2383wp
%2746wop
opt = mpoption('PF_ALG', 1);
opt = mpoption(opt, 'OUT_ALL', 0);
opt = mpoption(opt,'pf.alg','NR');
opt = mpoption(opt,'model','AC');

n_node = length(mpc.bus(:,BUS_I));
n_branches= length(mpc.branch(:,1));
n_gen=length(mpc.gen(:,1));
n_gencost = length(mpc.gencost(:,1));
node_index=(1:n_node)';
branch_index=[(1:n_branches)', mpc.branch(:,F_BUS), mpc.branch(:,T_BUS) ];
mpc.branch(1:n_branches,RATE_A)=mpc.branch(1:n_branches,RATE_A);

mpc.branch(:,RATE_C)=mpc.branch(:,RATE_A);
% mpc.branch([24 169 292 321 322 1816 2109],RATE_C)=2*mpc.branch([24 169 292 321 322 1816 2109],RATE_C);
power_ref=mpc.branch(1:n_branches,RATE_C)';

% mpc.gen([39,42,43,44],PMAX)=500;



N_pst=2;
% pst= btw_centrality(mpc,N_pst)';
% wh = btw_centrality(mpc,n_branches)';

% pst=[29 30 3 9 24];
% pst=[];

% pst=[29 30 3 9 38];
% pst=[26 30 3 9 24];
% pst=[26 30 3 9 24 56];
%pst=[26 30 3 9 30 630 610 625 612];
% pst=[12 30 3 9];
% pst=[5 23 42];

% for b = 1:length(pst)
%     pst_name{b} = sprintf('branch %d', pst(b));
% end
alpha=1.5;


mpc.bus_name=cell(n_node,1);

for b = 1:n_node
    mpc.bus_name{b} = sprintf('bus %d', mpc.bus(b, BUS_I));
end

mpc.branch_name=cell(n_branches,1);

for b = 1:n_branches
    mpc.branch_name{b} = sprintf('branch %d', b);
end

mpc.gen_name=cell(n_gen,1);

for b = 1:n_gen
    mpc.gen_name{b} = sprintf('gen %d', b);
end

custom.branch{1} = { 'branch_name' };
custom.bus{1} = { 'bus_name' };
custom.gen{1} = { 'gen_name' };

mpc_initial = mpc;
mpc.gencost(:,[5 6 7])=0;



% [mpc,success]=opf_adjust(mpc,opt);
% mpc=pst_action(mpc,pst);

results_pf=runpf(mpc,opt);
non_zero_PF=find(results_pf.branch(:,PF));

init_pf=results_pf.branch(1:n_branches,PF)';

power_ref(non_zero_PF)=abs(alpha*results_pf.branch(non_zero_PF,PF)');
power_ref(power_ref==0)=max(power_ref(non_zero_PF));

mpc.branch(1:n_branches,RATE_C)=power_ref';

load(:,1) = results_pf.branch(:,PF)./power_ref';
norm_load(:,1)=(results_pf.branch(:,PF)-init_pf')./(power_ref'-init_pf');

total_PD(1)=sum(results_pf.bus(:,PD));

% If_ref=current(results_pf);
% If_ref=alpha*If_ref;

gen_PG=results_pf.gen(:,PG);

%DO

% attack=btw_centrality1(mpc,1);
 
% attack = [19 25 28];

% attack = [26 39 3 42 1];
% attack = [9 28];
% attack = 54;
% attack = [19,19*2];

attack=4;

% attack = [10 19 25 28];
% attack = [13];
% attack = [19];
% attack = [4 10 12];
mpc.branch(attack,BR_STATUS)=0;
% mpc.bus(1,PD)=500;
iteration=2;


% mpc.bus(16,[PD QD])=0;

fail_branch=find(abs(load(:,iteration-1))>1);
mpc.branch(fail_branch,BR_STATUS)=0;

% wh1 = btw_centrality(mpc,n_branches)';
shedding=0;

mpc.gen(:,PMAX)=mpc.gen(:,PMAX)*1.5;

branch_stat_matrix=[];
norm_power_matrix=[];
norm_power_increase_matrix=[];
cum_norm_power_matrix=[];
cum_norm_power_increase_matrix=[];
count_micro=0
count_macro=[];
cum_link=[];

for i=1:1:n_branches
    mpc.branch(i,BR_STATUS)=0;
    %problem branch: [630x13 double]
    while (1)
        count_micro=count_micro+1;
        load(:,iteration)=zeros(n_branches,1);
        gen_PG(:,iteration)=zeros(n_gen,1);
        mpc_casc=extract_islands(mpc, [], custom);
        n_islands(iteration)=numel(mpc_casc);
        total_PD(iteration)=0;
        %    mpc.branch(pst,10)=0;
        for i=1:n_islands(iteration)
            %                 mpc_casc{i}=opf_adjust1(mpc_casc{i},opt);
            if (~isempty(mpc_casc{i}.gen))
                
                [C_gen,ia_gen,ib_gen] = intersect(results_pf.gen_name,mpc_casc{i}.gen_name);
                [C,ia,ib] = intersect(results_pf.branch_name,mpc_casc{i}.branch_name);
                visited_gen=zeros(1,length(ib_gen));
                blackout=0;
                
                while (1)
                    
                    [results_casc,success]=runpf(mpc_casc{i},opt);
                    
                    if success == 1
                        old_visited=visited_gen;
                        [mpc,mpc_casc{i},visited_gen] = check_limits(mpc,mpc_casc{i},results_casc,visited_gen);
                        if old_visited==visited_gen
                            break
                        end
                        
                    elseif success == 0 && blackout == 0
                        
                        max_iter=15;
                        iter=1;
                        while ~success && shedding < 0.99 && iter < max_iter && blackout == 0
                            display(success)
                            [mpc,mpc_casc{i},shedding,blackout]=load_shedding3(mpc,mpc_casc{i},iter);
                            [results_casc,success]=runpf(mpc_casc{i},opt);
                            iter=iter+1;
                        end
                        
                    elseif blackout == 1
                        break
                    end
                    
                end
                
                if blackout == 0
                    
                    %                [mpc_casc{i},active_pst,active_ib]=pst_action(mpc_casc{i},pst);
                    %                mpc.branch(pst(active_ib),10)=mpc_casc{i}.branch(active_pst,10);
                    
                    %                angle(:,iteration)=mpc.branch(pst,10);
                    
                    [results_casc,success]=runpf(mpc_casc{i},opt);
                    islands(i,iteration)=1;
                    
                    Sf=results_casc.branch(ib,PF);
                    load(ia,iteration)= abs(Sf)./abs(power_ref(ia)');
                    norm_load(ia,iteration)=(abs(Sf)-abs(init_pf(ia)'))./(abs(power_ref(ia)')-abs(init_pf(ia)'));
                    
                    gen_PG(ia_gen,iteration)= results_casc.gen(ib_gen,PG);
                    total_PD(iteration)=total_PD(iteration)+sum(results_casc.bus(:,PD));
                end
            end
        end
        
        fail_branch=find(abs(load(:,iteration))>1);
        total_fail_branch(iteration)=numel(fail_branch);
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
        
%         cum_boolean=zeros(n_branches,1);
%         cum_boolean(out_line)=1;
        cum_link=[cum_link mpc.branch(:,BR_STATUS)];
        
        branches(:,iteration)=numel(find(mpc.branch(:,BR_STATUS)==1))
        
        mpc_casc=extract_islands(mpc, [], custom);
        n_islands(iteration)=numel(mpc_casc);
        
        
        
        if total_fail_branch(iteration) == 0
            break;
        end
        
        iteration=iteration+1;
        
    end
    
    count_macro=[count_macro;count_micro];
    count_micro=0;
    size_load=size(load)
    outed_line=find(mpc.branch(:,BR_STATUS)==0);
    load_array=load(:,size_load(2));
    load_array(outed_line)=1;
    
    norm_load_array=norm_load(:,size_load(2));
    norm_load_array(outed_line)=1;
    
    %norm_power_matrix=[norm_power_matrix load(:,size_load(2))]
    norm_power_matrix=[norm_power_matrix load_array];
    branch_stat_matrix=[branch_stat_matrix mpc.branch(:,BR_STATUS)];
    
    norm_power_increase_matrix=[norm_power_increase_matrix norm_load_array];
 
    
    mpc=ccr;
    mpc.bus_name=cell(n_node,1);

    for b = 1:n_node
        mpc.bus_name{b} = sprintf('bus %d', mpc.bus(b, BUS_I));
    end
    
    mpc.branch_name=cell(n_branches,1);
    
    for b = 1:n_branches
        mpc.branch_name{b} = sprintf('branch %d', b);
    end
    
    mpc.gen_name=cell(n_gen,1);
    
    for b = 1:n_gen
    mpc.gen_name{b} = sprintf('gen %d', b);
    end
    
    custom.branch{1} = { 'branch_name' };
    custom.bus{1} = { 'bus_name' };
    custom.gen{1} = { 'gen_name' };
end



% [branch_index,mpc.branch(:,BR_STATUS),load]
sum(mpc.branch(:,BR_STATUS)==0)
plot(total_PD./total_PD(1))
display( total_PD./total_PD(1))
axis([-inf,inf,0.2,1])
% figure
% plot(gen_PG')
% sum(results_pf.branch(:,10)~=0)
total_fail_branch

% figure
% bar(abs(load'))
% [branch_index,load]


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
% power_increase_row_coeff=[];
% power_increase_matrix_coeff=[];
% 
% for j=1:n_branches
%     for i=1:n_branches
%         row_coeff=[row_coeff corr(branch_stat_matrix(:,j),branch_stat_matrix(:,i))]; 
%         power_row_coeff=[power_row_coeff corr(norm_power_matrix(:,j),norm_power_matrix(:,i))]; 
%         power_increase_row_coeff=[power_increase_row_coeff corr(norm_power_increase_matrix(:,j),norm_power_increase_matrix(:,i))]; 
%         
%     end
%     matrix_coeff=[matrix_coeff;row_coeff];
%     power_matrix_coeff=[power_matrix_coeff;power_row_coeff];
%     power_increase_matrix_coeff=[power_increase_matrix_coeff;power_increase_row_coeff];
%     
%     row_coeff=[];
%     power_row_coeff=[];
%     power_increase_row_coeff=[];
% end

sum_cumulative_load=sum(cum_norm_power_matrix,1)./n_branches;

sum_cumulative_load_increase=sum(cum_norm_power_increase_matrix,1)./n_branches;

cum_link=1-cum_link;
sum_cumulative_link=sum(cum_link,1);

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

% A = power_increase_matrix_coeff;
% imagesc(A)
% colorbar
% colormap(redblue(100))
% caxis([-1 1]);
% title('Case 57 Pearson Power Increase')
% xlabel('Link_x')
% ylabel('Link_y')


%dlmwrite('case57_power_increase.txt', A, 'delimiter', ',');

count_macro=count_macro';
%sum_cumulative_load_increase=sum_cumulative_load_increase'
%C = mat2cell(sum_cumulative_load_increase, count_macro) %saves as C{1},C{2},...access elemets as C{1}(1)

%sum_cumulative_link=sum_cumulative_link'
%C = mat2cell(sum_cumulative_link, count_macro)

sum_cumulative_load=sum_cumulative_load'
C = mat2cell(sum_cumulative_load, count_macro)

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
    %legendset = [legendset '' num2str(entry(i-1))];
    set(plt,'color',colorVec(i-1,:))
    hold on;
    M=[M B(i:maximum:(maximum*n_branches))];
    
end
legend(strin,'Location','NW')

%hold on;
% basecase=ones(1,n_branches) * 0.5;
%basecase_sum=sum(abs(results_pf.branch(1:n_branches,PF)')./abs(ref_power))/n_branches;
%basecase_sum=0;
%basecase=ones(1,n_branches) * basecase_sum;
%cdfplot(basecase

title('case30 Internal Rating Link')
