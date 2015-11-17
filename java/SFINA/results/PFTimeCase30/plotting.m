%% averages
mtpwrAC=mean(flowTime(1,2:199))
err_mtpwrAC = std(flowTime(1,2:199))
mtpwrDC=mean(flowTime(2,2:199))
err_mtpwrDC=std(flowTime(2,2:199))
ipssAC=mean(flowTime(3,2:199))
err_ipssAC=std(flowTime(3,2:199))
ipssDC=mean(flowTime(4,2:199))
err_ipssDC=std(flowTime(4,2:199))
times = [mtpwrAC mtpwrDC ipssAC ipssDC]
errors = [err_mtpwrAC err_mtpwrDC err_ipssAC err_ipssDC]
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