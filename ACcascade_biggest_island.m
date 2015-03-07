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

k=[1:1:n_branches]

ref_idx=k;
%reference indices for to and from
ref_a=[];
ref_b=[];
for j=1:length(k)
    ref_a=[ref_a;mpc.branch(k(j),1)]; 
    ref_b=[ref_b;mpc.branch(k(j),2)]; 
end

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

ccr=extract_islands(mpc,1);
%the iteration step for cascading failure starts
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
    
    
   
    
    n_branches_ccr=numel(ccr.branch(:,1));
    idx=[];
    for l=1:n_branches_ccr
        idx=[idx;l];
    end
    
    a=[];
    b=[];

    for j=1:length(idx)
        a=[a;ccr.branch(idx(j),1)]; 
        b=[b;ccr.branch(idx(j),2)]; 
    end
    
    for i=1:length(a)
        for j=1:length(ref_a)
            if a(i)==ref_a(j) & b(i)==ref_b(j);
                idx(i)=ref_idx(j);
            end
        end
    end
    
    idx=idx';
    f=ccr.branch(:,1); %registers the branches into which power injected
    f=f'
    n_branches_ccr=numel(ccr.branch(:,1));
    n_branches_ccr
    %gives the indices of branches
    k=[];
    for l=1:n_branches_ccr
        k=[k;l];
    end
    k=k';
    
    reference_ccr=[];
    
    for z=1:length(idx)
        reference_ccr=[reference_ccr;reference(idx(z))];
    end
    reference_ccr=reference_ccr';
    
    
    results=runpf(ccr);  
    
    
    fake_bus_id=[];
    for tau=1:numel(ccr.bus(:,1))
        fake_bus_id=[fake_bus_id;ccr.bus(tau,1)];
    end
    fake_bus_id=fake_bus_id';   
    
    real_bus_id=[1:1:numel(ccr.bus(:,1))];
    
    for j=1:numel(ccr.branch(:,1))
        for chi=1:numel(ccr.bus(:,1))
            if f(j)==fake_bus_id(chi)
                f(j)=real_bus_id(chi);
            end
        end
    end
    
   
    
    b=[];
    for t=1:n_branches_ccr %calculates the new current after removal of three lines
        Sf_new = results.branch(k(:,t), PF) + 1j * results.branch(k(:,t), QF);
        
        Vf_new = results.bus(f(:,t), VM) * exp(1j * results.bus(f(:,t), VA));
        If_new = abs(conj( Sf_new / Vf_new )); %% complex current injected into branch k at bus f
        b = [b; If_new];
    end
    b=b'

    for m=1:n_branches_ccr
        if(b(:,m)>reference_ccr(:,m))|(b(:,m)==0);
            ccr.branch(k(:,m),BR_STATUS)=0;
        end
    end

    branch_stat2=[];
    for p=1:n_branches_ccr
        q=ccr.branch(k(:,p),BR_STATUS); %gives the status of lines after the first trial
        branch_stat2=[branch_stat2;q];
    end
    
    branch_stat2=branch_stat2'
    
    array_equality=isequal(branch_stat2,branch_stat1);
    array_equality2=isequal(b,zero);
    numel(ccr.bus(:,1))
    if array_equality==1 & array_equality2~=1
        %count2=count2+1
        text='no cascading failure triggered %s\n';
        disp(text)
        break
    end
    branch_stat1=branch_stat2
   
    
    ccr=extract_islands(ccr,1);
    %best_gen_idx = [find(ccr.gen(:,PG)==max(ccr.gen(:,PG)), 1)]
    %mant=find(fake_bus_id==best_gen_idx);
    %best_gen_idx=real_bus_id(mant)
    %ccr.bus(best_gen_idx, BUS_TYPE) = REF;
    
    
end

count2
numel(ccr.bus(:,1))
    


   
    

    
    
    



    



