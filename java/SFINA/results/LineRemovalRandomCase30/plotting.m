time = linspace(1,25,25)
figure
hold on
plot(time,totalLoading(2,:),'x-', time,totalLoading(1,:),'x-')
legend('Incremental rating reduction','Restoring network at each time')
xlabel('t')
ylabel('rel. served load')
title('Successively reducing line ratings (SFINA: case30, Matpower backend, AC)')
hold off