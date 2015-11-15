%% averages
mtpwrAC=sum(flowTime(1,2:20))/19
mtpwrDC=sum(flowTime(3,2:20))/19
ipssAC=sum(flowTime(2,2:20))/19
ipssDC=sum(flowTime(4,2:20))/19

%% plotting
time = linspace(2,20,19)

figure
hold on
plot(time,flowTime(1,2:20),'x-', time,flowTime(3,2:20),'x-', time,flowTime(2,2:20),'x-', time,flowTime(4,2:20),'x-')
legend('AC Matpower, avg 117.7 ms', 'DC Matpower, avg 174.1 ms', 'AC Interpss, avg 22.2 ms', 'DC Interpss, avg 11.2 ms')
xlabel('t')
ylabel('time of flow calculation [ms]')
title('Measuring power flow simulation time (case30)')
grid on
box on
hold off