function [final_total_PD,frequency_line_fail,load] =...
AC_casc_f(mpc,pst,attack,model,line_rating)

%{
Runs a cascading failure.
    [final_total_PD,frequency_line_fail,load] = 
                                AC_casc_f(mpc,pst,attack,model,line_rating)
    
    Runs a cascading failure (AC and DC power flows).
                                
    Inputs:
           mpc: MATPOWER case data structure
           pst: vector containing the nodes on which the Phase Shift
           Transformers are located. If the vector is empty, no
           optimization would be performed.
           attack: vector containing the failed lines.
           model: Structure that specify the model of the cascading
           failures. The main options are DC power flow with and without
           generation limits. AC power flow with or without voltage
           constraints and generation limits.
           line_rating: determines the capcity of the lines depending of
           its initial power flow. If its is set to 0, the line rating is
           taken from the case data.
   Outputs:
           final_total_PD: Total Power Demand at the end of the cascading
           failure.
           frequency_line_fail: vector flag, 1=failed.
           load: matrix with the line loading. Colums represent every
           iteration and columns represent the lines.
    
    The power flow is compute with MATPOWER(R)
    The Phase Shift Transformer is formulated with YALMIP and solve with
    MOSEK(R)
           
%}

% Define MATPOWER constants
define_constants;

% Define Power Flow options
opt = mpoption('PF_ALG', 1);
opt = mpoption(opt, 'OUT_ALL', 0);
opt = mpoption(opt,'pf.alg','NR');
if model == 3 || model == 3.5
    opt = mpoption(opt,'model','DC');
end

n_node = length(mpc.bus(:,BUS_I));      % Number of nodes
n_branches= length(mpc.branch(:,1));    % Number of Lines
n_gen=length(mpc.gen(:,1));             % Number of Generators



% Key for the nodes
mpc.bus_name=cell(n_node,1);
for b = 1:n_node
    mpc.bus_name{b} = sprintf('bus %d', mpc.bus(b, BUS_I));
end

% Key for the lines
mpc.branch_name=cell(n_branches,1);
for b = 1:n_branches
    mpc.branch_name{b} = sprintf('branch %d', b);
end

% Key for the Generators
mpc.gen_name=cell(n_gen,1);
for b = 1:n_gen
    mpc.gen_name{b} = sprintf('gen %d', b);
end

custom.branch{1} = { 'branch_name' };
custom.bus{1} = { 'bus_name' };
custom.gen{1} = { 'gen_name' };


% Initial Power Flow before the removal of lines define by the attack
% vector
results_pf=runpf(mpc,opt);

% Seting the line limits
if line_rating~=0
    non_zero_PF=find(results_pf.branch(:,PF));
    power_ref(non_zero_PF)=...
        abs(line_rating*results_pf.branch(non_zero_PF,PF)');
    power_ref(power_ref==0)=max(power_ref(non_zero_PF));
    mpc.branch(1:n_branches,RATE_C)=power_ref';
end
mpc.branch(:,RATE_C)=mpc.branch(:,RATE_A);
power_ref=mpc.branch(1:n_branches,RATE_C)';


% Line Loading before cascading
load(:,1) = results_pf.branch(:,PF)./power_ref';

% Base Power Demand
total_PD(1)=sum(results_pf.bus(:,PD));

% Initial Power Generation
gen_PG=results_pf.gen(:,PG);

%Initialize frequency_line_fail
frequency_line_fail=zeros(1,n_branches);

%Initialize failed Buses
bus_fail=zeros(1,n_node);

% Remove lines define by vector attack
mpc.branch(attack,BR_STATUS)=0;

% Advance the iteration
iteration=2;

% Check for overload lines
fail_branch=find(abs(load(:,iteration-1))>1);
% Remove overload lines
mpc.branch(fail_branch,BR_STATUS)=0;

% This variable is going to be remove
shedding=0;

%DO
while (1)
    % Initialize line rating and power generation vector for each iteration
    load(:,iteration)=zeros(n_branches,1);
    gen_PG(:,iteration)=zeros(n_gen,1);
    
    % Check for Islands
    mpc_casc=extract_islands(mpc, [], custom);
    n_islands(iteration)=numel(mpc_casc);
    
    % Initialize Power Demand for every iteration
    total_PD(iteration)=0;
    
    % Set Phase Shift to 0
    mpc.branch(pst,SHIFT)=0;
    voltage_limit=ones(n_islands(iteration),1);
    
    % Iterate over each island
    for i=1:n_islands(iteration);
        
        % Check if the Island has any generation at all
        if (~isempty(mpc_casc{i}.gen))
            
            % Obtain the Keys of the lines and generation within the Island
            [C_gen,ia_gen,ib_gen] = ...
                intersect(results_pf.gen_name,mpc_casc{i}.gen_name);
            [C,ia,ib] = ...
                intersect(results_pf.branch_name,mpc_casc{i}.branch_name);
            
            % Vector with checked generation limits
            visited_gen=zeros(1,length(ib_gen));
            % If the generator max power is 0, set it as checked
            visited_gen(mpc_casc{i}.gen(ib_gen,PMAX)==0)=1;
            
            % Initialize blackout flag, 1=blackout
            blackout=0;
            
            % Model 1 correspond to AC Power Flow with generation,
            % voltage limits and shedding algorithm
            
            if model == 1 || model == 1.5
                
                while (all(voltage_limit))
                    % Start Cascade algorith in each island
                    
                    [results_casc,success]=runpf(mpc_casc{i},opt);
                    % Check for convergence
                    if success == 1
                        
                        old_visited=visited_gen;
                        if all(visited_gen) % If all genereations are set to its limit
                            % do load shedding
                            [mpc,mpc_casc{i},shedding] = load_shedding(mpc,mpc_casc{i});
                            break
                        end
                        % Check generations limits
                        [mpc,mpc_casc{i},visited_gen] = check_limits(mpc,mpc_casc{i},results_casc,visited_gen);
                        % All genertions are in the limits
                        if old_visited==visited_gen
                            % check for voltage limits and save flag for
                            % model 1
                            if model == 1
                                [mpc,mpc_casc{i},voltage_limit(i)] =  check_voltage(mpc,mpc_casc{i},results_casc);
                            end
                            break                                
                            
                        end
                     
                    % If it did not converge, start load shedding     
                    elseif success == 0 && blackout == 0
                        
                        max_iter=15;
                        iter=1;
                        while ~success && shedding < 0.99 && iter < max_iter
                            display(success)
                            [mpc,mpc_casc{i},shedding,blackout]=load_shedding3(mpc,mpc_casc{i},iter);
                            [results_casc,success]=runpf(mpc_casc{i},opt);
                            iter=iter+1;
                        end
                    
                    % If did not converge, set it as blackout    
                    elseif blackout == 1
                        break                        
                    end   
                end
                            
            % Model 2 correspond to AC with shedding algorithm    
            elseif model == 2
                
                [results_casc,success]=runpf(mpc_casc{i},opt);
                if success==0
                    max_iter=15;
                    iter=1;
                    while ~success && shedding < 0.99 && iter < max_iter
                        display(success)
                        [mpc,mpc_casc{i}]=load_shedding3(mpc,mpc_casc{i},iter);
                        [results_casc,success]=runpf(mpc_casc{i},opt);
                        iter=iter+1;
                    end
                end
            % Model 3 correspond to DC and 3.5 with shedding algorithm    
            elseif model == 3 || model == 3.5
                
                [mpc,mpc_casc{i},shedding] = load_shedding(mpc,mpc_casc{i});
                [results_casc,success]=runpf(mpc_casc{i},opt);
                if model == 3.5
                    while (1)
                        old_visited=visited_gen;
                        [mpc,mpc_casc{i},visited_gen] = check_limits(mpc,mpc_casc{i},results_casc,visited_gen);
                        if old_visited==visited_gen
                            break
                        end
                    end
                end
            % Model 4 correspond to AC with load shedding according to PD=PG
            elseif model == 4
                
                [mpc,mpc_casc{i},shedding] = load_shedding(mpc,mpc_casc{i});
            end
            
            % Save results if there is no blackout and no voltage
            % violations for each island
            if blackout == 0 && all(voltage_limit)
                
                if ~isempty(pst)
                    [mpc_casc{i},active_pst,active_ib]=pst_action(mpc_casc{i},pst);
                    mpc.branch(pst(active_ib),10)=mpc_casc{i}.branch(active_pst,10);
                end
                
                
                [results_casc,success]=runpf(mpc_casc{i},opt);
                islands(i,iteration)=1;
                
                load(ia,iteration)= results_casc.branch(ib,PF)./power_ref(ia)';
                gen_PG(ia_gen,iteration)= results_casc.gen(ib_gen,PG);
                total_PD(iteration)=total_PD(iteration)+sum(results_casc.bus(:,PD));
            end
        end
    end
    %Save results for the iterations
    fail_branch=find(abs(load(:,iteration))>1);
    total_fail_branch(iteration)=numel(fail_branch);
    mpc.branch(fail_branch,BR_STATUS)=0;
    
    frequency_line_fail(fail_branch) = frequency_line_fail(fail_branch)+1;
    
    % Exit if there are no events
    if total_fail_branch(iteration) == 0 && all(voltage_limit)
        break;
    end
    
    % Advance iteration if no voltage limits are violated
    if all(voltage_limit)
        iteration=iteration+1;
    end
end

final_total_PD=total_PD;