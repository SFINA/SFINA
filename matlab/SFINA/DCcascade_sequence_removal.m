%14, 30, 118 and 2383wp.m CHECKED AND ABSOLUTELY CORRECT
define_constants;

alpha=2; %sets the limit for the capacity
mpc = loadcase('case2383wp.m');
results=rundcpf(mpc);

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

for t=1:n_branches
    Sf = results.branch(k(:,t), PF) + 1j * results.branch(k(:,t), QF);
    Vf = results.bus(f(:,t), VM) * exp(1j * results.bus(f(:,t), VA));
    If = abs(conj( Sf / Vf )); %% complex current injected into branch k at bus f
    ref_if = alpha*If;
    a = [a; ref_if];
end

reference=a' %gives the reference current on transmission lines 

%remove branche(s)



%initializing the branch statuses
b=[];
branch_stat1=[]; 
branch_stat2=[];

%forming the zero array to compare the current when blackout occurs
zero=[];
for z=1:n_branches
    zero=[zero;0];
end
zero=zero';

%initializing the iteration steps and isolated buses
array_equality2=isequal(b,zero);
count2=0;
isolated_bus=[];

%the iteration step for cascading failure starts
percentage_branches_burned=[];
percentage_isolated_buses=[];
percentage_gen_isolated=[];
total_load=[];

breakage=1;
while 1
    if breakage==0
        break
    end
    prompt = 'breakage or not (Enter 0 if breakage):';
    breakage = input(prompt)
    
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
    
    while 1
        array_equality3=isequal(branch_stat2,zero);  
        if array_equality2==1|array_equality3==1
        %length(isolated_bus)>=0.6*n_bus
        %count1=count1+1
            text='cascading failure triggered %s';
            disp(text)
            break
        end
   
        count2=count2+1
        results=rundcpf(mpc);     

        b=[];
        for t=1:n_branches %calculates the new current after removal of three lines
            Sf_new = results.branch(k(:,t), PF) + 1j * results.branch(k(:,t), QF);
            Vf_new = results.bus(f(:,t), VM) * exp(1j * results.bus(f(:,t), VA));
            If_new = abs(conj( Sf_new / Vf_new )); %% complex current injected into branch k at bus f
            b = [b; If_new];
        end
        b=b'

        for m=1:n_branches
            if(b(:,m)>reference(:,m))|(b(:,m)==0);
                mpc.branch(k(:,m),BR_STATUS)=0;
            end
        end

        branch_stat2=[];
        for p=1:n_branches
            q=mpc.branch(k(:,p),BR_STATUS); %gives the status of lines after the first trial
            branch_stat2=[branch_stat2;q];
        end
    
        branch_stat2=branch_stat2'
    
        [x,isolated_bus]=find_islands(mpc);
        isolated_bus=isolated_bus
        for z=1:length(isolated_bus)
            mpc.bus(isolated_bus(z),BUS_TYPE)=4;
        end
    
        
        array_equality=isequal(branch_stat2,branch_stat1);
        array_equality2=isequal(b,zero);
    
        if array_equality==1 & array_equality2~=1
            %count2=count2+1
            text='no cascading failure triggered %s\n';
            disp(text)
            break
        end
        branch_stat1=branch_stat2
    
    %branches result

        survived_branch=0;
        for i=1:length(branch_stat2)
            if branch_stat2(i)==1
                survived_branch=survived_branch+1;
            end
        end
        survived_branch;

        percentage_branches_burned=[percentage_branches_burned;(1-(survived_branch/n_branches))*100];

    %result isolated buses
        length(isolated_bus);
        percentage_isolated_buses=[percentage_isolated_buses;(length(isolated_bus)/n_bus)*100];
    
    %isolated generator

        num_isolated_gen=0;
        isolated_gen=[];
        for u=1:length(isolated_bus)
            for v=1:length(generators)
                if generators(v)==isolated_bus(u)
                    num_isolated_gen=num_isolated_gen+1;
                    isolated_gen=[isolated_gen;generators(v)];
                end
            end
        end

    %generators result
        total_generator_number=numel(generators);
        num_isolated_gen;
        isolated_gen';
        percentage_gen_isolated=[percentage_gen_isolated;(num_isolated_gen/total_generator_number)*100];
        %load distribution
        nzld = find((results.bus(:, PD) | results.bus(:, QD)) & results.bus(:, BUS_TYPE) ~= NONE);
        total_load = [total_load;sum(results.bus(nzld, PD))];
    
   
    end
   
end


percentage_branches_burned=percentage_branches_burned
percentage_isolated_buses=percentage_isolated_buses
%percentage_gen_isolated=percentage_gen_isolated'
g=sprintf('%d ',percentage_branches_burned);
total_load=total_load
num_it=1:numel(total_load)
Iterations=num_it'

Total_Load = total_load;

Branches_Burned = percentage_branches_burned;
Isolated_Buses = percentage_isolated_buses
%Write table
T = table(Iterations,Total_Load,Branches_Burned,Isolated_Buses)
writetable(T)

%fprintf('total iteration is %d\n',count2)

fileID = fopen('exp.txt','w');

fprintf(fileID,'AFTERMATH OF THE SIMULATION %g\n','');
fprintf(fileID,text,'');
fprintf(fileID,'total number of buses %g\n',n_bus);
fprintf(fileID,'total number of branches %g\n',n_branches);
fprintf(fileID,'branch index initially removed %g\n',result);
fprintf(fileID,'total iteration is %g\n',count2);
fprintf(fileID,'percentage of branches burned %s\n',g);
fprintf(fileID,'percentage of isolated buses formed %g\n',percentage_isolated_buses);
fprintf(fileID,'percentage of isolated generators formed %g\n',percentage_gen_isolated);

fclose(fileID);




   
    

    
    
    



    



