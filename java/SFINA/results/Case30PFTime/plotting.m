%% PF time calculations
mtpwrAC=mean(flowTime(1,2:100))
err_mtpwrAC = std(flowTime(1,2:100))
mtpwrDC=mean(flowTime(2,2:100))
err_mtpwrDC=std(flowTime(2,2:100))
ipssAC=mean(flowTime(3,2:100))
err_ipssAC=std(flowTime(3,2:100))
ipssDC=mean(flowTime(4,2:100))
err_ipssDC=std(flowTime(4,2:100))
times = [mtpwrAC mtpwrDC ipssAC ipssDC]
errors = [err_mtpwrAC err_mtpwrDC err_ipssAC err_ipssDC]
save data.mat

%% total time calc
mtpwrACtot=mean(totalTime(1,2:100))
err_mtpwrACtot = std(totalTime(1,2:100))
mtpwrDCtot=mean(totalTime(2,2:100))
err_mtpwrDCtot=std(totalTime(2,2:100))
ipssACtot=mean(totalTime(3,2:100))
err_ipssACtot=std(totalTime(3,2:100))
ipssDCtot=mean(totalTime(4,2:100))
err_ipssDCtot=std(totalTime(4,2:100))
timesTot = [mtpwrAC mtpwrACtot; mtpwrDC mtpwrDCtot; ipssAC ipssACtot; ipssDC ipssDCtot]
errorsTot = [err_mtpwrAC err_mtpwrACtot; err_mtpwrDC err_mtpwrDCtot; err_ipssAC err_ipssACtot; err_ipssDC err_ipssDCtot]
save data.mat

%% bar plot
bar(times,'r')
hold on
errorbar(times, errors,'linestyle','none','color','black')
hold off
ylabel('simulation time (ms)')
set(gca,'fontsize',20)
l = cell(1,4);
l{1}='M, AC'; l{2}='M, DC'; l{3}='I, AC'; l{4}='I, DC'; 
set(gca,'xticklabel',l)

%% plot with total simu time

barwitherr(errorsTot,timesTot)
colormap gray
legend('Flow Calculation', 'Total Simulation','location','northwest')
ylabel('Simulation Time [ms]')
l = cell(1,4);
l{1}='Matpower AC'; l{2}='Matpower DC'; l{3}='InterPSS AC'; l{4}='InterPSS DC'; 
set(gca,'xticklabel',l,'fontsize',18,'ticklength',[0.02 0.1])
xticklabel_rotate([],30,[],'fontsize',18)


%% plotting
time = linspace(1,100,100)

figure
hold on
plot(time,flowTime(1,:),'x-',time,flowTime(2,:),'x-',time,flowTime(3,:),'x-',time,flowTime(4,:),'x-')
legend('AC Matpower, avg 117.7 ms', 'DC Matpower, avg 174.1 ms', 'AC Interpss, avg 22.2 ms', 'DC Interpss, avg 11.2 ms')
xlabel('t')
ylabel('time of flow calculation [ms]')
title('Measuring power flow simulation time (case30)')
grid on
box on
hold off